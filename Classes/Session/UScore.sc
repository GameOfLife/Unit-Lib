/*
    Unit Library
    The Game Of Life Foundation. http://gameoflife.nl
    Copyright 2006-2011 Miguel Negrao, Wouter Snoei.

    GameOfLife Unit Library: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    GameOfLife Unit Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with GameOfLife Unit Library.  If not, see <http://www.gnu.org/licenses/>.
*/

UScore : UEvent {

	classvar <>activeScores;
	classvar <>openFunc;
	classvar <>updateWaitTime = 0.1;
	classvar <>storeDisplayBounds = true;
	classvar <>storeRecentScorePaths = true;

	/*
	*   events: Array[UEvent]
	*/

	//public
	var <events, <name = "untitled";
	var pos = 0, <loop = false;
	var <playState = \stopped, <updatePos = true;
	var <soloed, <softMuted;
	var <>tempoMap;
	var <>pausedByUMarker = false;
	var <>displayBounds;


	/* playState is a finite state machine. The transitions graph:
                                       stop
        |---------------------------------------paused ----|
        |                                          ^       |
        |-----------|stop                  pause   |       | resume
        v           |                              |       |
     stopped --> preparing --> prepared -----> playing<----|
        ^   prepare       prepare |                |
        |   prepareAndStart       |                |
        |   prepareWaitAndStart   |                |
        |                         |                |
        |-------------------------|stop            |
        |------------------------------------------|stop

    */

	//private
	var <playTask, <updatePosTask, <startedAt, <pausedAt;

	*initClass {
		activeScores = Set();
		openFunc = { |path|
			this.readTextArchive( path );
		};
	}

	*current { ^UScoreEditorGUI.current !? { |x| x.score } }

	*currentSubScore { ^UScoreEditorGUI.current !? { |x|
			x.scoreView.scoreList.last;
		}
	}

	*findByName { |name|
		^UScoreEditorGUI.all.detect({ |se| se.score.name == name }) !? _.score;
	}

	*new { |... events|
		^super.new.init( events );
	}

	*open { |path, action|
        if( path.isNil ) {
		    ULib.openPanel( { |path|
				if( storeRecentScorePaths ) {
					URecentScorePaths.addPath( path );
				};
		        action.value( openFunc.(path) );
		    });
	    } {
            path = path.standardizePath;
			if( storeRecentScorePaths ) {
				URecentScorePaths.addPath( path );
			};
            action.value( openFunc.(path) );
	    };
	}

	*openMultiple { |paths, action| // action fired for each path
		if( paths.isNil ) {
		    ULib.openPanel( { |paths|
				if( storeRecentScorePaths ) {
					URecentScorePaths.addPaths( paths );
				};
			    paths.do({ |path|
					action.value( openFunc.(path) );
				});
		    }, multipleSelection: true );
	    } {
			paths = paths.collect(_.standardizePath);
			if( storeRecentScorePaths ) {
				URecentScorePaths.addPaths( paths );
			};
		    paths.do({ |path|
			    action.value( openFunc.(path) );
			});
	    };
	}

	/*
	* Syntaxes for UScore creation:
	* UScore( <UEvent 1>, <UEvent 2>,...)
	* UChain(startTime,<UEvent 1>, <UEvent 2>,...)
	* UChain(startTime,track,<UEvent 1>, <UEvent 2>,...)
	*/

	init{ |args|
		if( args[0].isNumber ) {
			startTime = args[0];
			args = args[1..]
		};
		if( args[0].isNumber ) {
			track = args[0];
			args = args[1..]
		};
	    events = if(args.size >0){args}{Array.new};
	    soloed = [];
	    softMuted = [];
	    tempoMap = TempoMap.default.deepCopy;
	    this.changed( \init );
	}

	events_ { |evs|
	    events = evs;
		this.prRecheckSoloMutes;
	    this.changed(\events);
	}

	loop_ { |bool|
		loop = bool;
		this.changed(\loop);
	}

	allowPause_ { |bool = false|
		allowPause = bool;
		this.changed( \allowPause, bool );
	}

	releaseSelf { ^true }
	releaseSelf_ { "%:releaseSelf - can't use releaseSelf\n".postf( this.class ) }

	isPlaying{ ^playState == \playing }
	isPaused{ ^playState == \paused }
	isPreparing{ ^playState == \preparing }
	isPrepared{ ^playState == \prepared }
	isStopped{ ^playState == \stopped }
	playState_{ |newState, changed = true|

	    if(changed){
	        this.changed(\playState,newState,playState);  //send newState oldState
	    };
	    playState = newState;

	    if( playState === \stopped ) {
		    activeScores.remove( this );
	    } {
		    activeScores.add( this );
	    };
	}

    duplicate { ^UScore( *events.collect( _.duplicate ) )
	    .name_( name )
	    .startTime_( startTime )
	    .track_( track )
	    .tempoMap_( tempoMap.duplicate )
	    .displayColor_( displayColor );
	}

    makeView{ |i,minWidth,maxWidth| ^UScoreEventView(this,i,minWidth, maxWidth) }
    isFolder{ ^true }
	//ARRAY SUPPORT
	at { |...path|
		 var out;
		 out = events;
		 path.do({ |item|
			 if( out.respondsTo( \at ) ) {
			 	out = out[ item ];
			 } { ^nil }
		 });
		 ^out
	}
	copySeries { |first, second, last| ^events.copySeries( first, second, last ) }
	collect { |func|  ^events.collect( func );  }
	do { |func| events.do( func ); }
	first { ^events.first }
	last { ^events.last }
	indexOf { |obj|
		var index;
		index = events.indexOf( obj );
		if( index.isNil ) {
			events.do({ |item, i|
				index = [ i, item.indexOf( obj ) ];
				if( index[1].notNil ) { ^index; }
			});
			^nil;
		} {
			^index
		};
	}

	setOrPerform { |key ...value|
		// args: list of keys, ending with the value to set
		var setter, parent, res;
		var pointCheckSet;
		key = key.asCollection;
		setter = key.last;
		key.pop;
		if( key.size > 0 ) {
			parent = this.at( *key );
		} {
			parent = this;
		};
		if( parent.notNil ) {
			pointCheckSet = { |unit, key, val|
				if( value.size > 1 ) {
					if( unit.getSpec( key ).isKindOf( PointSpec ) ) {
						unit.set( key, val.asPoint );
					} {
						unit.set( key, val );
					};
				} {
					unit.set( key, val[0] );
				};
			};
			if( parent.isKindOf( U ) ) {
				if( parent.keys.includes( setter.asString.split($.).first.asSymbol ) ) {
					pointCheckSet.( parent, setter, value );
				} {
					if( parent.respondsTo( setter ) ) {
						res = parent.perform( setter, value );
						if( res != parent ) {
							^res;
						};
					} {
						"UScore:set : can't set %, % to %"
							.format( key.join( ", " ), setter, *value )
							.warn;
					};
				};
			} {
				{ parent.perform( setter, *value ); }.try({
					{
						parent.findUnitsForKey( setter.asString.split($.).first.asSymbol ).do({ |unit|
							pointCheckSet.( unit, setter, value );
						}) ?? {
							"UScore:setOfPerform : no units with key '%' found in this UChain %"
							.format( setter, key.join( ", " ) )
							.warn;
						};
					}.try({
						"%:perform : can't perform %, % ( % )"
						.format( parent.class, key.join( ", " ), setter, value )
						.warn;
					});
				});
			};
		} {
			"UScore:setOrPerform : invalid args [%, %], %"
				.format( key.join( ", " ), setter, value )
				.warn;
		};
	}

    /*
    * newEvents -> UEvent or Array[UEvent]
    */
	add { |newEvents|
	    this.events_(events ++ newEvents.asCollection);
	    this.changed(\numEventsChanged)
	}
	<| { |newEvents| this.add(newEvents) }

	<< { |score|
	    ^UScore(*(events++score.events))
	}

	size { ^events.size }

	allEvents {
		var list = [];

		this.events.do({ |item|
				if( item.isFolder )
					{ list = list.addAll( item.allEvents ); }
					{ list = list.add( item ); }
				});

		^list;

	}

	allUChains{
	    ^events.collect(_.getAllUChains).flat
	}

	getAllUChains{
	    ^events.collect(_.getAllUChains).flat
	}
	getAllUMarkers {
		^events.collect(_.getAllUMarkers).flat
	}
	startTimes { ^events.collect( _.startTime ); }
	startTimes_ { |times|
		times = times.asCollection;
		events.do({ |evt, i|
			if( times[i].notNil ) {
				evt.startTime = times[i];
			};
		});
		this.changed( \events );
	}

	startBeats { ^events.collect({ |evt| tempoMap.beatAtTime( evt.startTime ) }); }
	startBeats_ { |beats|
		beats = beats.asCollection;
		events.do({ |evt, i|
			if( beats[i].notNil ) {
				evt.startTime = tempoMap.timeAtBeat( beats[i] )
			};
		});
		this.changed( \events );
	}

	durations { ^events.collect( _.dur ); }

	duration {
		var dur = 0;
		events.do({ |evt|
			dur = dur.max( evt.endTime );
			if( dur == inf ) { ^dur };
		});
		^dur;
	}

    finiteDuration { |addInf = 10|
	    var time = 0;
		events.do({ |evt|
			time = time.max( evt.startTime + evt.finiteDuration );
		});
		^time;
	}
    displayDuration { |margin = 10| // used by UScoreView
	   ^(this.finiteDuration(margin) + margin).max(margin);
    }

    //mimic a UChain
    eventSustain{ ^inf }
    release{ |time| ^this.stop( time ) }
    canFreeSynth{ ^events.collect(_.canFreeSynth).reduce('||') }

    cutStart{}

    cutEnd{}


	waitTime {
		^(events.collect(_.prepareTime).minItem ? 0).min(0).neg
	}

	fromTrack { |track = 0| ^this.class.new( events.select({ |event| event.track == track }) ); }

	sort { events.sort; this.changed( \numEventsChanged ); this.changed( \sort ); }

	connect { events.do(_.connect) }
	disconnect { events.do(_.disconnect) }

    //TRACK RELATED
	findEmptyTrack { |startTime = 0, endTime = inf|
		var evts, tracks;

		evts = events.select({ |item|
			(item.startTime <= endTime) and: (item.endTime >= startTime )
		});

		tracks = evts.collect(_.track);

		(tracks.maxItem+2).do({ |i|
			if( tracks.includes( i ).not ) { ^i };
		});
	}

	findEmptyRegion { |startTime, endTime, startTrack, endTrack|
		^events.select({ |item|
			( (item.startTime <= endTime) and: (item.startTime >= startTime ) ) or:
			( (item.endTime <= endTime) and: (item.endTime >= startTime ) )
		}).collect(_.track).maxItem !? (_+1) ?? {events.collect(_.track).maxItem} ? 0;

	}

	checkIfInEmptyTrack { |evt|
		var evts, tracks;

		evts = events.detect({ |item|
			(item === evt).not and: {
				(item.startTime <= evt.endTime) and: {
					(item.endTime >= evt.startTime ) and: {
						(item.track == evt.track)
					}
				}
			}
		});

		^evts.isNil;
	}

    moveEventToEmptyTrack { |evt|
        if( this.checkIfInEmptyTrack( evt ).not ) {
			evt.track = this.findEmptyTrack( evt.startTime, evt.endTime );
		}
    }

	addEventToEmptyTrack { |evt|
		this.moveEventToEmptyTrack(evt);
		this.events_(events.add( evt ));
		this.changed(\numEventsChanged)
	}

	addEventsToEmptyRegion { |events|
	    var startTime = events.collect(_.startTime).minItem;
	    var endTime = events.collect(_.endTime).maxItem;
	    var startTrack = events.collect(_.track).minItem;
	    var endTrack = events.collect(_.track).maxItem;
	    var startRegion =  this.findEmptyRegion(startTime, endTime, startTrack, endTrack);
	    this <| events.collect{ |x| x.track = x.track + startRegion - startTrack }
	}

	findCompletelyEmptyTrack {
		^( (events.collect(_.track).maxItem ? -1) + 1);
	}

	addEventToCompletelyEmptyTrack { |evt|
		evt.track = this.findCompletelyEmptyTrack;
		this.events_(events.add( evt ));

	}

    //need to add a
	cleanOverlaps {
		events.do{ |x| this.moveEventToEmptyTrack(x) }
    }

    removeEmptyTracks {
	    var usedTracks = events.collect(_.track).asInteger.as(Set).as(Array).sort;
	    events.do({ |evt| evt.track = usedTracks.indexOf( evt.track.asInteger ) ? evt.track; });
    }

	//SCORE PLAYING

    eventsThatWillPlay { |startPos, startEventsActiveAtStartPos = true|
        ^if(startEventsActiveAtStartPos){
            events.select({ |evt| (evt.eventEndTime >= startPos) && evt.disabled.not })
        } {
            events.select({ |evt| (evt.startTime >= startPos) && evt.disabled.not })
        }

    }

	eventsToPrepareNow{ |startPos=0 , loop = false|
	    var evs, allevs = this.eventsThatWillPlay(startPos);
	    evs = allevs.select(_.prepareTime <= startPos);
	    if( loop ){
	        evs = evs ++ events.select{ |x| (x.prepareTime <= 0) && ( (x.prepareTime + this.duration) <= startPos ) };
	    };
	    ^evs.sort({ |a,b| a.startTime <= b.startTime })
	}

    arrayForPlayTask{ |startPos=0, assumePrepared = false, startEventsActiveAtStartPos = true, loop = false|
        var evs, prepareEvents, startEvents, releaseEvents, allEvents, doPrepare, fStartAndRelease, fActualStartPos;

        fStartAndRelease = { |item| item.releaseSelf.not && (item.eventEndTime == item.startTime) };
        if( startEventsActiveAtStartPos ) {
	        fActualStartPos = { |x| x.startTime.max(startPos) };
        } {
	        fActualStartPos = { |x| x.startTime };
        };

        evs = this.eventsThatWillPlay(startPos,startEventsActiveAtStartPos);
		prepareEvents = if(assumePrepared){evs.select({ |item| item.prepareTime > startPos })}{evs};
		startEvents = evs.collect({ |item|
			item.score = this;
			[ fActualStartPos.(item), if( fStartAndRelease.( item ) ) { 3 } { 1 }, item ];
		});
		releaseEvents = Array( events.size );
		events.do({ |item|
			var endTime;
			if( (item.releaseSelf != true) && { item.duration < inf } ) {
				endTime = item.eventEndTime;
				if( (endTime >= startPos) && { endTime != item.startTime } ) {
					releaseEvents.add( [endTime + Server.default.latency, 2, item] );
				};
			};
		});

        // returns collection of [duration, type, event]
        // where type can be:
        // 0 - prepare, 1 -start, 2 - release, 3 - start and release
		allEvents = prepareEvents.collect{ |x| [x.prepareTime, 0, x]}
         ++ startEvents
         ++ releaseEvents; //.collect{ |x| [x.eventEndTime, 2, x]};

        if( loop ) {
            allEvents = allEvents ++
                events
                    .select{ |x| (x.prepareTime <= 0) && ( (this.duration + x.prepareTime) >= startPos) }
                    .collect{ |x|  [this.duration + x.prepareTime, 0, x] };
        };

        //if the time for the event to happen is different order them as usual
        //if they happen at the same time then the order is prepare < start < release
        allEvents = allEvents.sort{ |a,b|
            if(a[0] != b[0]) {
                a[0] <= b[0]
            } {
	            if( a[1] != b[1] ) {
		            a[1] <= b[1]
	            } {
		            a[2].isKindOf( UMarker )
	            };
            }
        };
        doPrepare = prepareEvents.size > 0
        ^[allEvents, doPrepare,  if(doPrepare){ prepareEvents[0].prepareTime }{nil}]
	}

    //prepare resources needed to play score, i.e. load buffers, send synthdefs.
	prepare { |targets, startPos = 0, action|
	    var eventsToPrepareNow, multiAction;
	    eventsToPrepareNow = this.eventsToPrepareNow(startPos, loop);
	    if( eventsToPrepareNow.size > 0 ) {
			var actions;
			multiAction = MultiActionFunc( {
			    this.playState_(\prepared);
			    action.value;
			} );
			// targets = targets.asCollection.collect(_.asTarget); // leave this to UChain:prepare
			this.playState_(\preparing);
			actions = eventsToPrepareNow.collect{ multiAction.getAction };
			eventsToPrepareNow.do({ |item, i|
				item.score = this;
			    item.prepare( targets, (startPos - item.startTime).max(0), action:actions[i] );
			});
	    } {
	        this.playState_(\preparing);
	        this.playState_(\prepared);
		    action.value; // fire immediately if nothing to prepare
	    };
	}

    //start immediately, assume prepared by default
    start { |targets, startPos = 0, updatePosition = true|
        ^this.prStart(targets, startPos, true, true, updatePosition, true, loop)
    }

    //prepares events as fast as possible and starts the playing the score.
	prepareAndStart{ |targets, startPos = 0, updatePosition = true|
        if(this.isPrepared) {
            this.start( targets, startPos, updatePosition, loop )
        } {
            this.prepare(targets, startPos, {
               this.start( targets, startPos, updatePosition, loop )
            }, loop);
        }
	}

    //prepare during waitTime and start after that, no matter if the prepare succeeded
    prepareWaitAndStart{ |targets, startPos = 0, updatePosition = true|
        this.playState_(\preparing);
        ^this.prStart(targets, startPos, false, true, updatePosition, true, loop)
    }

	prStart { |targets, startPos = 0, assumePrepared = false, callStopFirst = true, updatePosition = true,
	    startEventsActiveAtStartPos = true, loop = false|
	    CmdPeriod.add( this );
		if( callStopFirst ) { this.stop(nil,false, false); }; // in case it was still running
        this.prStartTasks( targets, startPos, assumePrepared, updatePosition, startEventsActiveAtStartPos, loop );
	}

	prStartTasks { |targets, startPos = 0, assumePrepared = false, updatePosition = true, startEventsActiveAtStartPos = true, loop = false|
        var prepareEvents, startEvents, releaseEvents, prepStartRelEvents, preparePos,
            allEvents, deltaToStart,dur, actions, needsPrepare, updatePosFunc, waitError;

        waitError = "prStartTasks - was going to call .wait on inf";

        if(startEventsActiveAtStartPos) {
            actions = [
                { |event,startOffset| event.prepare( targets, startOffset ) },
                { |event, startOffset| event.start(targets, startOffset) },
                { |event| event.release },
                { |event, startOffset| event.startAndRelease(targets, startOffset) }
            ];
        } {
            actions = [
                { |event| event.prepare( targets ) },
                { |event| event.start(targets) },
                { |event| event.release },
                { |event| event.startAndRelease(targets) }
            ];
        };

        dur = this.duration;

        if( loop ) {
	        // if a score is infinitely long, it cannot loop
            case { dur == inf } {
	              this.loop = loop = false;
               	"can not loop score with infinite events in it".warn;
            } { (dur + (events.collect(_.prepareTime).sort[0])) < 0 } {
	       //if the firt event to be prepared has prepareTime bigger then the duration of the score
            //then it is impossible to prepare the event while the score is playing.
                this.loop = loop = false;
                "Score is too small, will not loop score. Would not have enough time to prepare events.".warn;
            };
        };
        #allEvents,needsPrepare, preparePos = this.arrayForPlayTask(startPos, assumePrepared, startEventsActiveAtStartPos, loop);

        //this allows to be able to get the current pos when the update task is not running
		startedAt = [ startPos, SystemClock.seconds ];

        //this is for prepareWaitAndStart
        preparePos = if(needsPrepare){ preparePos.min(startPos) }{ startPos };
        deltaToStart = startPos - preparePos;

        updatePosFunc = {
            if( updatePosition ) {
                updatePosTask = Task({
                    var w = (startPos - preparePos);
                    if(w == inf){ waitError.warn; updatePosFunc.stop; }{w.wait};
                    while({playState == \playing}, {
                        updateWaitTime.wait;
                        if(updatePos) {
                            this.changed(\pos);
                        }
                    });

                }).start;
            };
        };

        if( pausedByUMarker ) {
	        UMarker.umarkerIndexIncrease;
	        pausedByUMarker = false;
	    };

        //if there is some time between preparing and starting to play
        //then wait before declaring that the score has started to play.
        if( allEvents.size > 0) {
            if(deltaToStart !=0){
                fork{
                    if(deltaToStart == inf){ waitError.warn;}{deltaToStart.wait};
                    this.playState_(\playing);
                }
            }{
                this.playState_(\playing);
            };
            playTask = Task({
                var pos = preparePos;
                var w;
                allEvents.do({ |item|
                    w = item[0] - pos;
                    if(w == inf){ waitError.warn }{w.wait};
                    pos = item[0];
                    //"prepare % at %, %".format( events.indexOf(item),
                    //	pos, thisThread.seconds ).postln;
                    actions[item[1]].value(item[2], (startPos - item[2].startTime).max(0) );
                });
                if( this.isFinite ) {
                    w = dur - pos;
                    if(w == inf){ waitError.warn }{w.wait};
                    if(loop) {
                        this.pos = 0;
                        this.prStartTasks(targets, 0, assumePrepared, updatePosition,
                            startEventsActiveAtStartPos, loop)
                    } {
                        // the score has stopped playing i.e. all events are finished
                        startedAt = nil;
                        this.pos = 0;
                        this.playState_(\stopped);
                    }
                }
            }).start;

            updatePosFunc.value;
		    this.changed( \start, startPos );
        } {

            if(dur == inf) {
                //if there is nothing to play but the score is infinite just keep updating the position.
                this.playState_(\playing);
                updatePosFunc.value;
            } {
                if(loop) {
                    //if there is nothing to play in this run of the score and we are looping, just wait until the end
                    //and start again
                    updatePosFunc.value;
                    fork{
                        var w;
                        this.playState_(\playing);
                        w = dur - startPos;
                        if(w == inf){ waitError.warn }{w.wait};
                        this.pos = 0;
                        this.prStartTasks(targets, 0, assumePrepared, updatePosition,
                            startEventsActiveAtStartPos, loop)
                    }

                } {
                    this.playState_(\stopped);
                }
            }
        };

	}

	//stop just the spawning and releasing of events
	stopScore {
		pausedByUMarker = false;
		{ [playTask, updatePosTask ].do(_.stop); }.fork;
	}

    //stop synths
	stopChains { |releaseTime|
		events.do(_.release(releaseTime ? 0.025));
	}

	//stop everything
	stop{ |releaseTime, changed = true, dispose = true|
	    if([\playing,\paused].includes(playState) ) {
            //no nil allowed
            pos = this.pos;
            startedAt = nil;
            this.stopScore;
            this.stopChains(releaseTime);
            events.select({ |evt| evt.isFolder.not && { (evt.preparedServers.size > 0) &&
	            	{ evt.isPlaying.not } } })
	          .do(_.dispose);
            this.playState_(\stopped,changed);
            this.changed(\pos,this.pos);
            CmdPeriod.remove( this );
	    };
	    if([\preparing,\prepared].includes(playState)) {
			if(dispose) {
				this.dispose
			};
	        this.playState_(\stopped,changed);
	    }
	}

	pause {
	    if(playState == \playing){
		    this.stopScore;
		    events.select(_.allowPause).do(_.pause);
		    pos = this.pos - 1e-12;
		    pausedAt = pos;
		    startedAt = nil;
		    this.playState_(\paused);
		    this.pos = pos;
		}
	}

	prSubScoreResume{ |targets|
	    if(playState == \paused){
		    this.prStart( targets, pausedAt, true, false, true, false, false );
		    events.select(_.isFolder).do( _.prSubScoreResume(targets) );
		    pausedAt = nil;
		}
	}

	resume { |targets|
	    if(playState == \paused){
		    this.prStart( targets, pausedAt, true, false, true, false, loop );
		    events.select(_.isFolder).do( _.prSubScoreResume(targets) );
		    pausedAt = nil;
		}
	}

	togglePlayback { |targets|
		case { this.isStopped } {
			this.prepareAndStart( targets, this.pos, true, this.loop);
		} { this.isPaused } {
			this.resume( targets );
		} { this.isPrepared } {
			this.start( targets, this.pos, true);
		} {
			this.stop;
		};
	}

	playAnyway { |targets|
		// play, resume or keep playing (but not stop)
		if( targets.isNumber ) { targets = nil };
		case { this.isPaused } {
			this.resume( targets );
		} { this.isPrepared } {
			this.start( targets, this.pos, true);
		} { this.isStopped } {
			this.prepareAndStart( targets, this.pos, true, this.loop);
		};
	}

	playAtMarker { |markerIndex = 0, onFail, targets| // or from start if not found
		var markerPos;
		case { markerIndex.isNumber } {
			markerPos = this.markerPositions[markerIndex];
		} { markerIndex.asSymbol === \next } {
			markerPos = ([0] ++ this.markerPositions).detect({ |item| this.pos <= item });
		} { markerIndex.asSymbol === \prev } {
			markerPos = this.markerPositions.detect({ |item| this.pos > item }) ? 0;
		} { markerIndex.asSymbol === \this_or_next } {
			markerPos = this.markerPositions.detect({ |item|
				this.pos.equalWithPrecision( item, 0.1 ) or:
				{ this.pos < item }
			});
		} {
			markerPos = events.select({ |item|
				item.isKindOf( UMarker );
			}).detect({ |m| m.name.find( markerIndex.asString ).notNil }) !? _.startTime;
		};
		markerPos = markerPos ? onFail;
		if( markerPos.notNil ) {
			if( this.pos.equalWithPrecision( markerPos, 0.1 ) ) {
				case { this.isPlaying } {
					// keep playing
				} { this.isPaused } {
					this.resume( targets );
				} {
					this.prepareAndStart( targets, markerPos );
				};
			} {
				case { this.isPlaying or: this.isPaused } {
					this.stop;
					{
						0.1.wait;
						this.prepareAndStart( targets, markerPos );
					}.fork;
				} {
					this.prepareAndStart( targets, markerPos );
				};
			};
		} {
			"UScore.playAtMarker: marker not found".postln;
		};
	}

	dispose { events.do(_.dispose) }

	collectOSCBundleFuncs { |server, startOffset = 0, infdur = 60|
		var array, wasCheckFree, out;

		if( disabled.not ) {
			server = server ? Server.default;

			array = events.collect({ |item|
				item.collectOSCBundleFuncs( server, item.startTime + startOffset, infdur );
			}).flatten(1);

			^array.sort({ |a,b| a[0] <= b[0] })
		} {
			^[];
		};
	}

	collectOSCBundles { |server, startOffset = 0, infdur = 60, includeUMaster = true|
		var array, wasCheckFree, out;
		var masterDur;

		if( disabled.not ) {
			server = server ? Server.default;

			this.useNRT({

				if( includeUMaster && { UMaster.hasStarted } ) {
					masterDur = this.duration;
					if( masterDur == inf ) { masterDur = infdur };
					array = UMaster.collectOSCBundleFuncs( server, 0, masterDur );
				};

				array = array.addAll(
					events.collect({ |item|
						item.collectOSCBundleFuncs( server, item.startTime + startOffset, infdur );
					}).flatten(1)
				);

				out = array.sort({ |a,b| a[0] <= b[0] }).collect({ |item|
					server.makeBundle( false, item[1] ).collect({ |bundle|
						[ item[0], bundle ]
					});
				}).flatten(1).select({ |item| item.size > 1 });

			});
			^out;
		} {
			^[];
		};
	}

	cmdPeriod {
		this.stop;
		CmdPeriod.remove( this ); // always remove;
	}

	/*
	In case the score is not self-updating the pos variable, then the current pos (which might go on forever as the score plays)
	is given by the ammount of time elapsed since the score started playing;
	*/
	pos {
		^if( startedAt.notNil && this.isPlaying ) {
			((SystemClock.seconds - startedAt[1]) + startedAt[0]);
		} {
			pos ? 0;
		};
	}

	pos_ { |x|
	    pos = x;
	    this.changed(\pos, x);
	}

	updatePos_ { |x|
	    updatePos = x;
	    this.changed(\updatePos,x)
	}

	// markers

	markerPositions {
		^events.select({ |item|
			item.isKindOf( UMarker );
		}).collect(_.startTime).sort;
	}

	jumpTo { |pos = 0|
		case { this.isPlaying } {
			this.stop;
			this.pos = pos;
			{ this.prepareAndStart( startPos: pos ); }.fork;
		} { this.isPaused } {
			// don't jump
		} {
			this.pos = pos;
		};
	}

	toNextMarker { |includeEnd = true|
		var markerPositions, dur, newPos;
		markerPositions = this.markerPositions;
		if( includeEnd ) {
			markerPositions = markerPositions.add( this.finiteDuration(0) );
		};
		if( (newPos = markerPositions.detect({ |item| item > this.pos })).notNil ) {
			this.jumpTo( newPos );
		};
	}

	toPrevMarker { |includeStart = true|
		var markerPositions, dur, nowPos, newPos;
		markerPositions = this.markerPositions;
		if( includeStart && { markerPositions.includes( 0 ).not } ) {
			markerPositions = [0] ++ markerPositions;
		};
		nowPos = this.pos;
		if( this.isPlaying && { nowPos >= 0.5 } ) { nowPos = nowPos - 0.5 };
		if( (newPos = markerPositions.reverse.detect({ |item| item < nowPos })).notNil ) {
			this.jumpTo( newPos );
		};
	}

	// SOLO / MUTE
	prSetHardMutes{
		this.getAllUChains.do{ |ev| ev.muted_( (softMuted.includes(ev) || ( (soloed.size != 0) && soloed.includes(ev).not )) ) }
	}

	prSMSet { |event, array, bool, setArray|
	    if(bool) {
            if( array.includes(event).not ){
                setArray.(array.add(event));
                this.prSetHardMutes;
            }
        } {
            if( array.includes(event) ){
                array.remove(event);
                this.prSetHardMutes;
            }
        }
	}

	solo { |event, bool|
		(if(event.isUScoreLike) {
			event.getAllUChains
		}{
			[event]
		}).do{ |ev|
			this.prSMSet(ev, soloed, bool, { |x| soloed = x})
		};
	}

	softMute { |event, bool|
		(if(event.isUScoreLike) {
			event.getAllUChains
		}{
			[event]
		}).do{ |ev|
			this.prSMSet(ev, softMuted, bool, { |x| softMuted = x})
		};
	}

	prRecheckSoloMutes {
		var all = this.getAllUChains;
		soloed = soloed.select{ |x| all.includes(x) };
		softMuted = softMuted.select{ |x| all.includes(x) };
		this.prSetHardMutes;
	}

	printOn { arg stream;
		stream << this.class.name << "( " << name << ", " << events.size <<" events )"
	}

	getInitArgs {
		var numPreArgs = -1;

		if( track != 0 ) {
			numPreArgs = 1
		} {
			if( startTime != 0 ) {
				numPreArgs = 0
			}
		};

		^([ startTime, track ][..numPreArgs]) ++ events;
	}

	storeArgs { ^this.getInitArgs }

	storeParamsOn { arg stream;
		var args = this.storeArgs;
		var useArray = args.size > 254;
		stream << "(";
		if( useArray ) {
			stream << "*[";
		};
		stream <<* args.collect({ |item|
			if( item.isKindOf( UEvent ) ) {
				"\n\t" ++ item.asCompileString
			} {
				item.asCompileString;
			};
		});
		if( useArray ) {
			stream << "]";
		};
		stream << ")";
	}

	storeModifiersOn { |stream|
		if( tempoMap != TempoMap.default ) {
			stream << ".tempoMap_(" <<< tempoMap << ")";
		};
		if( loop == true ) {
			stream << ".loop_(" <<< loop << ")";
		};
		this.storeName( stream );
		this.storeTags( stream );
		this.storeDisplayColor( stream );
		this.storeDisabledStateOn( stream );
		this.storeDisplayBoundsOn( stream );
	}

	storeName { |stream|
		if( name != "untitled" ) {
			stream << ".name_(" <<< name << ")";
		};
	}

	storeDisplayBoundsOn { |stream|
		if( displayBounds.notNil ) {
			stream << ".displayBounds_(" <<< displayBounds << ")";
		};
	}

	textArchiveFileExtension { ^"uscore" }

	onSaveAction {
		this.name = filePath.basename.removeExtension;
		if( storeRecentScorePaths ) { URecentScorePaths.addPath( filePath ) };
	}

	readTextArchiveAction{ this.name = filePath.basename.removeExtension }

	windowBounds {
		^UScoreEditorGUI.all.detect({ |item|
			item.score == this
		}) !? { |gui| gui.window.bounds };
	}

	windowBounds_ { |rect|
		UScoreEditorGUI.all.detect({ |item|
			item.score == this
		}) !? { |gui|
			case { rect.isKindOf( Rect ) } {
				gui.window.bounds = rect;
			} { rect.isKindOf( Point ) } {
				gui.window.bounds = gui.window.bounds.extent_( rect );
			};
		};
	}

	name_ { |x| name = x; this.changed(\name) }

	deepClearTags {
		UTagSystem.removeObject( this );
		events.do(_.deepClearTags);
	}

	selectUMaps { |selectFunc|
		var c, selected = [];
		selectFunc = selectFunc ?? { { true } }; // select all
		c = { |chain|
			if( chain.isKindOf( UScore ) ) {
				chain.events.do({ |evt| c.( evt ) });
			} {
				selected = selected ++ ( chain.selectUMaps( selectFunc ) );
			};
		};
		c.( this );
		^selected;
	}

	makeUMapMap {
		^this.makeValueMap( _.isUMap, true );
	}

	makeValueMap { |selectFunc, includeUMaps = false|
		var f, c, lib = ();
		f = { |unit, path|
			unit.values.do({ |value, i|
				var newPath;
				newPath = path +/+ unit.keys[i];
				if( value.isUMap ) {
					if( includeUMaps ) {
						if( selectFunc.( value ) != false ) {
							lib[ newPath.asSymbol ] = value;
						};
					};
					f.( value, newPath );
				} {
					if( selectFunc.( value ) != false ) {
						lib[ newPath.asSymbol ] = value;
					};
				};
			});
		};
		c = { |chain, path|
			if( chain.isKindOf( UScore ) ) {
				chain.events.do({ |evt, i| c.( evt, path +/+ i ) });
			} {
				chain.units.do({ |unit, i|
					f.( unit, path +/+ i );
				});
				if( chain.isKindOf( UPattern ) ) {
					if( chain.pattern.isUMap ) {
						f.( chain.pattern, path +/+ "pattern" );
					}
				}
			};
		};
		c.( this, this.name );
		^lib;
	}

	getGlobalBufferIDs {
		^this.events.select(_.respondsTo( \getGlobalBufferIDs )).collect(_.getGlobalBufferIDs).flatten(1);
	}


}