UPattern : UChain {
	
	classvar >seconds;
	classvar <>preparedThreads, <>expectedTimes, <>preparing = false;
	
	var <repeats = inf;
	var <sustain = 1;
	var <timeToNext = 1;
	var <>maxSimultaneousStarts = 100;
	var <>localPos = 0;
	var <>routine;
	var <>preparedEventsRoutine; // for prepared events
	var <>preparedEvents;
	var isPlaying = false;
	var task;
	
	prPrepareUnit { |unit|
		var prepareThese;
		prepareThese = unit.valuesToPrepare.select({ |item|
			item.isKindOf( UMap );
		});
		prepareThese.do({ |umap|
			if( umap.def.isKindOf( UPatDef ).not ) {
				this.prPrepareUnit( umap );
			};
		});
		prepareThese.do({ |umap|
			 if( umap.def.isKindOf( FuncUMapDef ) ) {
				umap.next;
			 };
		});
	}
	
	prPatternsToValues { |unit|
		unit.args.pairsDo({ |key, item|
			if( item.isKindOf( UMap ) ) {
				if( [ FuncUMapDef, ValueUMapDef ].any({ |def| item.def.isKindOf( def ) }) ) {
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
	
	argSpecsForDisplay { 
		^[
			ArgSpec( 'sustain', 1, SMPTESpec(0.001, 3600), false, \init ),
			ArgSpec( 'timeToNext', 1, SMPTESpec(0.01, 3600), false, \init )
		]
	}
	
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
	
	getSpec { |key|
		^switch( key,
			'sustain', { SMPTESpec(0.001, 3600) },
			'timeToNext', { SMPTESpec(0.01, 3600) },
		);
	}
	
	getDefault { |key|
		^switch( key,
			'sustain', { 1 },
			'timeToNext', { 1 },
		);
	}
	
	removeUMap { |key|
		switch( key,
			'sustain', { this.sustain = 1 },
			'timeToNext', { this.timeToNext = 1 },
		);
	}
	
	defName { ^'UPattern' }
	
	args { ^[ \sustain, this.sustain, \timeToNext, this.timeToNext ] }
	
	at { |index|
		^switch( index,
			\sustain, { this.sustain },
			\timeToNext, { this.timeToNext },
			{ units[ index ] }
		);
	}
	
	next { |duration, startTime = 0, track = 0|
		var next, was;
		next = UChain( *units.deepCopy );
		next.displayColor = this.getTypeColor;
		next.global = this.global;
		next.addAction = this.addAction;
		next.duration = duration ?? { this.getSustain; };
		next.fadeTimes = this.fadeTimes;
		next.startTime = startTime;
		next.track = track;
		was = UChain.nowPreparingChain;
		UChain.nowPreparingChain = next;
		next.parent = this;
		next.units.do({ |unit|
			this.prPrepareUnit( unit );
			this.prPatternsToValues( unit );
		});
		UChain.nowPreparingChain = was;
		^next;
	}
	
	repeats_ { |newRepeats|
		repeats = newRepeats ? inf;
		this.changed( \repeats, repeats );
	}
	
	sustain_ { |newSustain|
		sustain = newSustain.asUnitArg( this, \sustain );
		this.changed( \sustain, sustain );
	}
	
	timeToNext_ { |newTimeToNext|
		timeToNext = newTimeToNext.asUnitArg( this, \timeToNext );
		this.changed( \timeToNext, timeToNext );
	}
	
	prUnPrepare { |unit|
		if( unit.isKindOf( UMap ) ) {
			if( unit.def.isKindOf( FuncUMapDef ) ) {
				unit.u_prepared = false;
			};
			unit.values.do({ |val|
				this.prUnPrepare( val );
			});
		};
	}
	
	getSustain {
		if( sustain.isKindOf( UMap ) && { sustain.def.isKindOf( FuncUMapDef ) } ) {
			this.prUnPrepare( sustain );
			this.prPrepareUnit( sustain );
		};
		^sustain.next;
	}
	
	getTimeToNext {
		if( timeToNext.isKindOf( UMap ) && { timeToNext.def.isKindOf( FuncUMapDef ) } ) {
			this.prUnPrepare( timeToNext );
			this.prPrepareUnit( timeToNext );
		};
		^timeToNext.next;
	}
	
	isPlaying { ^isPlaying ? false }
	
	makeRoutine { |target, startPos = 0, action|
		^Routine({
			var time = 0, n = 0;
			var zeroCount = 0;
			var next, sustain, timeToNext;
			var track = 0, track0time = 0;
			while { ( this.releaseSelf == false or: { (time <= (duration - startPos)) }) 
					&& { n < repeats } && { zeroCount < maxSimultaneousStarts }
			} {
				timeToNext = this.getTimeToNext;
				sustain = this.getSustain;
				if( time > track0time ) { track = 0 };
				if( track == 0 ) { track0time = time + (sustain * 2); };
				next = this.next( sustain, time, track );
				track = track + 1;
				this.localPos = time;
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
		this.stop;
		waitTime = this.waitTime; // fixed waitTime for all events
		units.do({ |unit| this.prResetStreams( unit ); });
		if( this.sustain.isKindOf( UMap ) ) { this.prResetStreams( this.sustain ); };
		if( this.timeToNext.isKindOf( UMap ) ) { this.prResetStreams( this.timeToNext ); };
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
					chain.prepare( target, action: multiAction.getAction );
					preparedEvents = preparedEvents.add( chain );
				} {
					if( firstEvent ) {
						UPattern.expectedNext = startedPreparingTime + (time - waitTime) + timeToNext;
						nil.yield;
						(time - waitTime).wait;
						firstEvent = false;
					};
					preparedEvents = preparedEvents.add(
						chain.prepareWaitAndStart( target, startAction: {
							preparedEvents.remove( chain );
						} );
					);
				};
			} );
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
				chain.prepareWaitAndStart( target ); 
			} );
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
			chain.prepareWaitAndStart( target ); 
		} ); };
		task = PauseStream( routine ).play;
		preparedEventsRoutine.play;
	}
	
	prepareAndStart { |target, startPos = 0|
		this.prepare( target, startPos, {
			this.start;
		});
	}
		
	prepareWaitAndStart { |target, startPos = 0|
		this.stop;
		units.do({ |unit| this.prResetStreams( unit ); });
		isPlaying = true;
		this.changed( \start );
		task = PauseStream( this.makeRoutine( target, startPos, { |chain, target, time| 
			chain.prepareWaitAndStart( target ); 
		} ) ).play;
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
		var score, events, originalDur, track = 0, track0time = 0, nextTime;
		this.stop;
		units.do({ |unit| this.prResetStreams( unit ); });
		score = UScore().startTime_( this.startTime ).track_( this.track );
		originalDur = duration;
		if( duration == inf ) {
			duration = infDur;
		};
		UPattern.seconds = thisThread.seconds;
		events = Array( 2**13 );
		routine = this.makeRoutine( nil, 0, { |chain, target, time|
			events = events.add( chain );
		});
		while { (nextTime = routine.next).notNil; } { 
			UPattern.seconds = seconds + nextTime;
		};
		duration = originalDur;
		UPattern.seconds = nil;
		score.events = events;
		^score;
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
	
	storeModifiersOn { |stream|
		this.storeTags( stream );
		this.storeDisplayColor( stream );
		this.storeDisabledStateOn( stream );
		if( this.repeats != inf ) {
			stream << ".repeats_(" <<< this.repeats << ")";
		};
		if( this.sustain != 1 ) {
			stream << ".sustain_(" <<< this.sustain << ")";
		};
		if( this.timeToNext != 1 ) {
			stream << ".timeToNext_(" <<< this.timeToNext << ")";
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