UPattern : UChain {
	
	classvar >seconds;
	classvar <>preparedThreads, <>expectedTimes, <>preparing = false;
	classvar <>argSpecs;
	classvar <>nowCallingPattern, <>currentSustain;
	
	var <repeats = inf;
	var <pattern = #[1,1];
	var <>maxSimultaneousStarts = 100;
	var <>localPos = 0;
	var <>startPos = 0;
	var <>routine;
	var <>preparedEventsRoutine; // for prepared events
	var <>preparedEvents;
	var isPlaying = false;
	var task;
	
	*initClass {
		argSpecs = [ ArgSpec( 'pattern', [1,1], UPatternSpec(), false, \init ) ];
	}
	
	init { |args|
		this.pattern = \sustain_time;
		super.init( args );
	}
	
	prPrepareUnit { |unit|
		var prepareThese;
		prepareThese = unit.valuesToPrepare.select({ |item|
			item.isKindOf( UMap );
		});
		prepareThese.do({ |umap|
			if( umap.subDef.isKindOf( UPatDef ).not ) {
				this.prPrepareUnit( umap );
			};
		});
		prepareThese.do({ |umap|
			 if( umap.subDef.isKindOf( FuncUMapDef ) ) {
				umap.next;
			 };
		});
	}
	
	prPatternsToValues { |unit|
		unit.args.pairsDo({ |key, item|
			if( item.isKindOf( UMap ) ) {
				if( [ FuncUMapDef, ValueUMapDef ].any({ |def| item.subDef.isKindOf( def ) }) ) {
					unit.set( key, item.value );
				} {
					this.prPatternsToValues( item );
				};
			};
		});
	}
	
	prResetStreams { |unit|
		if( unit.isKindOf( UMap ) ) {
			unit.resetStream;
		};
		unit.args.pairsDo({ |key, item|
			if( item.isKindOf( UMap ) ) {
				item.resetStream;
				this.prResetStreams( item );
			};
		});
	}
	
	getTypeColor {
		^case {
	        this.displayColor.notNil;
        } {
	        this.displayColor;
        } { 
	        this.duration == inf 
	   } {
	        Color(0.6, 0.6, 0.8)
        } {
	       this.releaseSelf == true;
        } {
	        Color(0.768, 0.55,0.768);
        } {
	        Color(0.48, 0.6, 0.6);
        };
	}
	
	argSpecsForDisplay { ^argSpecs }
	
	canUseUMap { |key, umapdef|
		^umapdef.allowedModes.includes( 'init' ) && {
			umapdef.unitCanUseUMap( this, key );	
		};
	}
	
	set { |key, value|
		this.perform( key.asSetter, value );
	}
	
	get { |key|
		^this.perform( key );
	}
	
	mapGet { |key|
		var spec = this.getSpec(key);
		^if( spec.notNil ) {
		    spec.unmap( this.get(key) )
		} {
		    this.get(key)
		}
	}
	
	insertUMap { |key, umapdef, args|
		var item, umap;
		if( umapdef.isKindOf( UMap ) ) {
			umap = umapdef;
			umapdef = umap.def;
		} {
			umapdef = umapdef.asUdef( UMapDef );
			if( umapdef.notNil ) {
				umap = UMap( umapdef,  args );
			};
		};
		if( umap.notNil ) {
			if( umap.def.canInsert ) {
				item = this[ key ];
				if( item.isUMap ) {
					this.set( key, umap );
					this.get( key ).set( umapdef.insertArgName, item );
				} {
					item = this.mapGet( key );
					this.set( key, umap );
					this.get( key ).mapSet( umapdef.insertArgName, item );
				};
			} {
				this.set( key, umap );
			};
		};
	}
	
	argNeedsUnmappedInput { ^false }
	
	argSpecs { ^argSpecs }
	getArgSpec { |key| ^if( key === \pattern ) { argSpecs[0] } }
	getSpec { |key| ^if( key === \pattern ) { argSpecs[0].spec } }
	getSpecMode { |key| ^if( key === \pattern ) { argSpecs[0].mode } }
	getDefault { |key| ^if( key === \pattern ) { argSpecs[0].default } }
	
	removeUMap { |key|
		switch( key,
			'pattern', { this.pattern = this.getDefault( \pattern ) },
		);
	}
	
	defName { ^'UPattern' }
	
	args { ^[ \pattern, this.pattern ] }
	
	at { |index|
		^switch( index,
			\pattern, { this.pattern },
			\sustain, { this.pattern.value[0] },
			\timeToNext, { this.timeToNext.value[1] },
			{ units[ index ] }
		);
	}
	
	next { |duration, startTime = 0, track = 0, score|
		var next, was;
		duration = duration ?? { this.getSustain; };
		this.class.currentSustain = duration;
		if( duration > 0 ) {	
			next = UChain( *units.deepCopy );
			next.displayColor = this.getTypeColor;
			next.global = this.global;
			next.addAction = this.addAction;
			next.duration = duration;
			next.fadeTimes = this.fadeTimes;
			next.startTime = startTime;
			next.track = track;
			next.ugroup = ugroup;
			next.voicerNote = this.voicerNote;
			next.voicerValue = this.voicerValue;
			was = UChain.nowPreparingChain;
			UChain.nowPreparingChain = next;
			next.parent = this;
			next.score = score;
			next.units.do({ |unit|
				this.prPrepareUnit( unit );
				this.prPatternsToValues( unit );
			});
			UChain.nowPreparingChain = was;
		};
		this.class.currentSustain = nil;
		^next;
	}
	
	repeats_ { |newRepeats|
		repeats = newRepeats ? inf;
		this.changed( \repeats, repeats );
	}
	
	sustain_ { |newSustain|
		if( this.pattern.isKindOf( UMap ) ) {
			this.pattern.set( \sustain, newSustain );
			this.changed( \pattern, pattern );
		} {
			if( newSustain.isNumber ) {
				this.pattern = [ newSustain, this.pattern[1] ];
			} {
				this.pattern = [ \sustain_time, 
					[ \sustain, newSustain, \timeToNext, this.timeToNext ]
				];
			};
		};
	}
	
	timeToNext_ { |newTimeToNext|
		if( this.pattern.isKindOf( UMap ) ) {
			this.pattern.set( \timeToNext, newTimeToNext );
			this.changed( \pattern, pattern );
		} {
			if( newTimeToNext.isNumber ) {
				this.pattern = [ this.pattern[0], newTimeToNext ];
			} {
				this.pattern = [ \sustain_time, 
					[ \sustain, this.sustain, \timeToNext, newTimeToNext ]
				];
			};
		};
	}
	
	sustain { 
		if( pattern.isKindOf( UMap ) ) {
			^pattern.sustain;
		} {
			^pattern[0];
		};
	}
	
	timeToNext { 
		if( pattern.isKindOf( UMap ) ) {
			^pattern.timeToNext;
		} {
			^pattern[1];
		};
	}
	
	pattern_ { |newPattern|
		pattern = newPattern.asUnitArg( this, \pattern );
		this.changed( \pattern, pattern );
	}
	
	prUnPrepare { |unit|
		if( unit.isKindOf( UMap ) ) {
			if( unit.subDef.isKindOf( FuncUMapDef ) ) {
				unit.u_prepared = false;
			};
			unit.values.do({ |val|
				this.prUnPrepare( val );
			});
		};
	}
	
	getPattern { // returns new sustain and timeToNext
		var out;
		this.class.nowCallingPattern = this;
		if( pattern.isKindOf( UMap ) && { pattern.subDef.isKindOf( FuncUMapDef ) } ) {
			this.prUnPrepare( pattern );
			this.prPrepareUnit( pattern );
		};
		out = pattern.next;
		this.class.nowCallingPattern = nil;
		^out;
	}
	
	isPlaying { ^isPlaying ? false }
	
	makeRoutine { |target, startPos = 0, action, score|
		this.startPos = startPos;
		^Routine({
			var time = 0, n = 0;
			var zeroCount = 0;
			var next, sustain, timeToNext;
			var track = 0, track0time = 0;
			while { ( this.releaseSelf == false or: { (time <= (duration - startPos)) }) 
					&& { n < repeats } && { zeroCount < maxSimultaneousStarts }
			} {
				#sustain, timeToNext = this.getPattern;
				if( time > track0time ) { track = 0 };
				if( track == 0 ) { track0time = time + (sustain * 2); };
				next = this.next( sustain, time, track, score );
				this.localPos = time;
				if( next.notNil ) { 
					track = track + 1; 
				};
				action.value( next, target, time, timeToNext );
				timeToNext.wait;
				time = time + timeToNext;
				if( timeToNext == 0 ) { zeroCount = zeroCount + 1 } { zeroCount = 0 };
				if( zeroCount >= maxSimultaneousStarts ) {
					"UPattern ending; maxSimultaneousStarts (%) reached\n"
						.postf( zeroCount )
				};
				n = n + 1;
			};
			isPlaying = false;
			this.changed( \end );
		})
	}
	
	prepare { |target, startPos = 0, action|
		var waitTime, firstEvent = true;
		var multiAction, firstAction, preparedEventsRoutineShouldEnd = false;
		var i = 0, nextTime, startedPreparingTime;
		if( this.isPlaying ) { this.stop; };
		waitTime = this.waitTime; // fixed waitTime for all events
		units.do({ |unit| this.prResetStreams( unit ); });
		if( this.pattern.isKindOf( UMap ) ) { this.prResetStreams( this.pattern ); };
		preparedEvents = Array(512);
		if( waitTime > 0 ) {
			multiAction = MultiActionFunc( action );
			firstAction = multiAction.getAction;
			isPlaying = nil;
			startedPreparingTime = thisThread.seconds;
			UPattern.preparing = true;
			routine = this.makeRoutine( target, startPos, { |chain, target, time, timeToNext| 
				if( time < waitTime ) {
					// "preparing %\n".postf( time.asSMPTEString );
					if( chain.notNil ) {
						chain.prepare( target, action: multiAction.getAction );
						preparedEvents = preparedEvents.add( chain );
					};
				} {
					if( firstEvent ) {
						UPattern.expectedNext = startedPreparingTime + (time - waitTime) + timeToNext;
						nil.yield;
						(time - waitTime).wait;
						firstEvent = false;
					};
					if( chain.notNil ) {
						preparedEvents = preparedEvents.add(
							chain.prepareWaitAndStart( target, startAction: {
								preparedEvents.remove( chain );
							} );
						);
					};
				};
			}, this.score );
			UPattern.seconds = startedPreparingTime - waitTime;
			while { (nextTime = routine.next).notNil; } { 
				i = i+1;
				UPattern.seconds = seconds + nextTime;
			};
			UPattern.seconds = nil;
			UPattern.preparedThreads = nil;
			UPattern.preparing = false;
			if( isPlaying == false ) { preparedEventsRoutineShouldEnd = true };
			// "% events prepared\n".postf( i );
			preparedEventsRoutine = Task({
				var waitTime, time = 0;
				preparedEvents.copy.do({ |chain|
					(chain.startTime - time).wait;
					time = chain.startTime;
					chain.start;
					preparedEvents.remove( chain );
				});
				if( preparedEventsRoutineShouldEnd ) { 
					isPlaying = false;
					this.changed( \end );
				};
			});
			firstAction.value;
		} {
			routine = this.makeRoutine( target, startPos, { |chain, target, time| 
				chain !? _.prepareWaitAndStart( target ); 
			}, this.score );
			preparedEventsRoutine = nil;
			action.value;
		};
	}
	
	start { |target, startPos = 0|
		task.stop; // first stop any existing playback task
		preparedEventsRoutine.stop;
		isPlaying = true;
		this.changed( \start );
		routine = routine ?? { this.makeRoutine( target, startPos, { |chain, target, time| 
			chain !? _.prepareWaitAndStart( target ); 
		}, this.score ); };
		task = PauseStream( routine ).play;
		preparedEventsRoutine.play;
		this.score = nil;
	}
	
	prepareAndStart { |target, startPos = 0|
		this.prepare( target, startPos, {
			this.start( startPos: startPos );
		});
	}
		
	prepareWaitAndStart { |target, startPos = 0|
		this.stop;
		units.do({ |unit| this.prResetStreams( unit ); });
		isPlaying = true;
		this.changed( \start );
		task = PauseStream( this.makeRoutine( target, startPos, { |chain, target, time| 
			chain !? _.prepareWaitAndStart( target ); 
		}, this.score ) ).play;
	}
	
	stop {
		task.stop;
		preparedEventsRoutine.stop;
		preparedEvents.do({ |chain|
			chain.stop;
			chain.dispose;
		});
		isPlaying = false;
		this.changed( \end );
	}
	
	release { this.stop }
	
	asUScore { |infDur = 60|
		var score, events, originalDur, rlseSelf, track = 0, track0time = 0, nextTime;
		this.stop;
		units.do({ |unit| this.prResetStreams( unit ); });
		if( this.pattern.isKindOf( UMap ) ) { this.prResetStreams( this.pattern ); };
		score = UScore().startTime_( this.startTime ).track_( this.track );
		originalDur = duration;
		if( duration == inf ) {
			duration = infDur;
		};
		rlseSelf = this.releaseSelf;
		if( rlseSelf == false ) {
			releaseSelf = true;
		};
		UPattern.seconds = thisThread.seconds;
		events = Array( 2**13 );
		routine = this.makeRoutine( nil, 0, { |chain, target, time|
			if( chain.notNil ) { events = events.add( chain ); };
		});
		while { (nextTime = routine.next).notNil; } { 
			UPattern.seconds = seconds + nextTime;
		};
		duration = originalDur;
		releaseSelf = rlseSelf;
		UPattern.seconds = nil;
		score.events = events;
		score.disabled = this.disabled;
		^score;
	}
	
	*startPos {
		var upat;
		upat = UChain.nowPreparingChain !? _.parent;
		if( upat.isKindOf( UPattern ).not ) {
			upat = UPattern.nowCallingPattern;
		};
		if( upat.isKindOf( UPattern ) ) {
			^upat.startPos;
		} {
			^0
		};
	}
	
	*expectedNext_ { |time|
		if( seconds.notNil && preparedThreads.notNil ) {
			expectedTimes = expectedTimes ?? {()};
			preparedThreads.do({ |key|
				expectedTimes[ key ] = time;
			});
			preparedThreads = Set();
		};
	}
	
	*seconds { |returnExpected = false|
		if( preparing && { seconds.notNil }) {
			preparedThreads = preparedThreads ?? { Set() };
			preparedThreads.add( thisThread );
		};
		^seconds ?? { 
			if( returnExpected && { expectedTimes.size > 0 }) {
				expectedTimes.removeAt( thisThread ) ?? { thisThread.seconds; };
			} {
				thisThread.seconds;
			};
		} 
	}
	
	*timer {
		var lastTime = this.seconds, time = 0;
		^{
			time = time + (this.seconds( true ) - lastTime);
			lastTime = this.seconds;
			time;
		};
	}
	
	*deltaTimer {
		var lastTime = this.seconds;
		^{
			var out = this.seconds( true ) - lastTime;
			lastTime = this.seconds;
			out;
		};
	}
	
	collectOSCBundleFuncs { |server, startOffset = 0, infdur = 60|
		^this.asUScore( infdur ).collectOSCBundleFuncs( server, startOffset, infdur );
	}
	
	collectOSCBundles { |server, startOffset = 0, infdur = 60|
		^this.asUScore( infdur ).collectOSCBundles( server, startOffset, infdur );
	}
	
	currentChains { ^groupDict.keys.select({ |item| item.parent === this }) }
	
	stopChains { |releaseTime| this.currentChains.do(_.release( releaseTime ) ) }
	
	asUChain { 
		^UChain( *this.units )
			.startTime_( this.startTime )
			.duration_( this.duration )
			.track_( this.track )
			.releaseSelf_( this.releaseSelf );
	}
	
	asUPattern { ^this }
	
	storeModifiersOn { |stream|
		var patArgs;
		this.storeTags( stream );
		this.storeDisplayColor( stream );
		this.storeDisabledStateOn( stream );
		if( this.repeats != inf ) {
			stream << ".repeats_(" <<< this.repeats << ")";
		};
		if( (this.sustain != 1) or: { this.timeToNext != 1 } or: { this.pattern.isKindOf( UMap ).not } or: { this.pattern.defName != 'sustain_time' }) {
			if( this.pattern.isKindOf( UMap ) ) {
				stream << ".pattern_(" <<< this.pattern.getSetArgs << ")";
			} {
				stream << ".pattern_(" <<< this.pattern << ")";
			}
		};
		if( ugroup.notNil ) {
			stream << ".ugroup_(" <<< ugroup << ")";
		};
		if( serverName.notNil ) {
			stream << ".serverName_(" <<< serverName << ")";
		};
		if( addAction != \addToHead ) {
			stream << ".addAction_(" <<< addAction << ")";
		};
		if( global != false ) {
			stream << ".global_(" <<< global << ")";
		};
		
	}
}

+ UChain {
	
	asUChain { ^this }
	
	asUPattern { 
		^UPattern( *this.units )
			.startTime_( this.startTime )
			.duration_( this.duration )
			.track_( this.track )
			.releaseSelf_( this.releaseSelf );
	}
}