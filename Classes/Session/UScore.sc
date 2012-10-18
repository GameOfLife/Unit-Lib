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

	/*
	*   events: Array[UEvent]
	*/

	//public
	var <events, <name = "untitled";
	var pos = 0, <loop = false;
	var <playState = \stopped, <updatePos = true;
	var <soloed, <softMuted;


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
	}

	*current { ^UScoreEditorGUI.current !? { |x| x.score } }

	*new { |... events| 
		^super.new.init( events );
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
	    this.changed( \init );
	}

	events_ { |evs|
	    events = evs;
	    this.changed(\events);
	}
	
	loop_ { |bool|
		loop = bool;
		this.changed(\loop);
	}

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

    duplicate { ^UScore( *events.collect( _.duplicate ) ).name_( name ); }

    makeView{ |i,maxWidth| ^UScoreEventView(this,i,maxWidth) }
    isFolder{ ^true }
	//ARRAY SUPPORT
	at { |...path| 
		 var out;
		 out = events;
		 path.do({ |item|
			 out = out[ item ];
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

	getAllUChains{
	    ^events.collect(_.getAllUChains).flat
	}
	startTimes { ^events.collect( _.startTime ); }
	durations { ^events.collect( _.dur ); }
	
	duration { ^(this.startTimes + this.durations).maxItem ? 0; }
	dur { ^this.duration }
    isFinite{ ^this.duration < inf}
    
    finiteDuration { 
		var time = 0;
		events.do({ |evt|
			var dur;
			dur = evt.dur;
			if( dur == inf ) {
				time = time.max( evt.startTime + 10 );
			} {
				time = time.max( evt.startTime + dur );
			};
		});
		^time; 
	}
    displayDuration { // used by UScoreView
	   ^(this.finiteDuration + 10).max(10);
    }

    //mimic a UChain
    eventSustain{ ^inf }
    release{ ^this.stop }
    canFreeSynth{ ^events.collect(_.canFreeSynth).reduce('||') }

    cutStart{}

    cutEnd{}


	waitTime {
		^(events.collect(_.prepareTime).minItem ? 0).min(0).neg
	}

	fromTrack { |track = 0| ^this.class.new( events.select({ |event| event.track == track }) ); }
	
	sort { events.sort; }

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
			(item.startTime <= evt.endTime) and:
			(item.endTime >= evt.startTime ) and:
			(item.track == evt.track)
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
        var evs, prepareEvents, startEvents, releaseEvents, startAndReleaseEvents, allEvents, doPrepare, fStartAndRelease, fActualStartPos;

        fStartAndRelease = { |item| item.releaseSelf.not && (item.eventEndTime == item.startTime) };
        fActualStartPos = { |x| if(startEventsActiveAtStartPos) {x.startTime.max(startPos)}{ x.startTime } };

        evs = this.eventsThatWillPlay(startPos,startEventsActiveAtStartPos).sort;
		prepareEvents = if(assumePrepared){evs.select({ |item| item.prepareTime > startPos })}{evs};
		startEvents = evs.select({ |item| fStartAndRelease.(item).not }).sort({ |a,b| a.startTime <= b.startTime });
		releaseEvents = events
			.select({ |item| (item.releaseSelf != true) && { (item.duration < inf) && { item.eventEndTime >= startPos }
			&& item.isFolder.not && (item.eventEndTime != item.startTime) } })
			.sort({ |a,b| a.eventEndTime <= b.eventEndTime });
		startAndReleaseEvents = events.select(fStartAndRelease);

        // returns collection of [duration, type, event]
        // where type can be:
        // 0 - prepare, 1 -start, 2 - release, 3 - start and release
		allEvents = prepareEvents.collect{ |x| [x.prepareTime, 0, x]}
         ++ startEvents.collect{ |x| [ fActualStartPos.(x), 1, x]}
         ++ releaseEvents.collect{ |x| [x.eventEndTime, 2, x]}
         ++ startAndReleaseEvents.collect{ |x| [ fActualStartPos.(x), 3, x]};

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
                a[1] <= b[1]
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
			multiAction = MultiActionFunc( {
			    this.playState_(\prepared);
			    action.value;
			} );
			// targets = targets.asCollection.collect(_.asTarget); // leave this to UChain:prepare
			this.playState_(\preparing);
			eventsToPrepareNow.do({ |item|
			    item.prepare( targets, (startPos - item.startTime).max(0), action: multiAction.getAction );
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
		if( callStopFirst ) { this.stop(nil,false); }; // in case it was still running
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
                    var waitTime = 0.1;
                    var w = (startPos - preparePos);
                    if(w == inf){ waitError.warn; updatePosFunc.stop; }{w.wait};
                    while({playState == \playing}, {
                        waitTime.wait;
                        if(updatePos) {
                            this.changed(\pos);
                        }
                    });

                }).start;
            };
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
		[playTask, updatePosTask ].do(_.stop);
	}

    //stop synths
	stopChains { |releaseTime|
		events.do(_.release(releaseTime ? 0.1));
	}

	//stop everything
	stop{ |releaseTime, changed = true|
	    if([\playing,\paused].includes(playState) ) {
            //no nil allowed
            releaseTime = releaseTime ? 0.1;
            pos = this.pos;
            startedAt = nil;
            this.stopScore;
            this.stopChains(releaseTime);
            events.select(_.isFolder).do(_.stop);
            //events.do{ _.disposeIfNotPlaying };
             events.select({ |evt| evt.isFolder.not && { evt.preparedServers.size > 0 } })
            	.do(_.dispose);
            this.playState_(\stopped,changed);
            this.changed(\pos,this.pos);
            CmdPeriod.remove( this );
	    };
	    if([\preparing,\prepared].includes(playState)) {
	        this.playState_(\stopped,changed);
	    }
	}
	
	pause {
	    if(playState == \playing){
		    this.stopScore;
		    events.select(_.isFolder).do(_.pause);
		    pos = this.pos;
		    pausedAt = pos;
		    startedAt = nil;
		    this.playState_(\paused);
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
	
	
	collectOSCBundles { |server, startOffset = 0, infdur = 60|
		var array, wasCheckFree, out;
		
		if( disabled.not ) {
			server = server ? Server.default;
			wasCheckFree = AbstractRichBuffer.useCheckFree;
			AbstractRichBuffer.useCheckFree = false;
			
			array = events.collect({ |item|
				item.collectOSCBundleFuncs( server, item.startTime + startOffset, infdur );
			}).flatten(1);
			
			out = array.sort({ |a,b| a[0] <= b[0] }).collect({ |item|
				[ item[0] ] ++ server.makeBundle( false, item[1] )
			}).select({ |item| item.size > 1 });
			
			AbstractRichBuffer.useCheckFree = wasCheckFree;
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
	// SOLO / MUTE
	prSetHardMutes{
	     events.do{ |ev| ev.muted_( (softMuted.includes(ev) || ( (soloed.size != 0) && soloed.includes(ev).not )).postln ) }
	}

	prSMSet { |event, array, bool, setArray|
	    if(bool) {
            if( array.includes(event).not.postln ){
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
	    this.prSMSet(event, soloed, bool, { |x| soloed = x})
	}

	softMute { |event, bool|
	    this.prSMSet(event, softMuted, bool, { |x| softMuted = x})
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
		stream << "(*[";
		stream <<* args.collect({ |item|
			if( item.isKindOf( UEvent ) ) {
				"\n\t" ++ item.asCompileString
			} {
				item.asCompileString;
			};
		});
		stream << "])";
	}
	
	storeModifiersOn { |stream|
		this.storeName( stream );
		this.storeTags( stream );
		this.storeDisplayColor( stream );
	}
	
	storeName { |stream|
		if( name != "untitled" ) {
			stream << ".name_(" <<< name << ")";
		};
	}
	
	textArchiveFileExtension { ^"uscore" }

	onSaveAction { this.name = filePath.basename.removeExtension }
	
	readTextArchiveAction{ this.name = filePath.basename.removeExtension }

	name_ { |x| name = x; this.changed(\name) }
	
	deepClearTags {   
		UTagSystem.removeObject( this ); 
		events.do(_.deepClearTags);
	}
}