UPattern : UChain {
	
	var <>repeats = inf;
	var <>sustain = 1;
	var <>timeToNext = 1;
	var <>maxSimultaneousStarts = 100;
	var <>localPos = 0;
	var <>routine;
	var <>preparedEventsRoutine; // for prepared events
	var <>preparedEvents;
	var task;
	
	prPrepareUnit { |unit|
		var prepareThese;
		prepareThese = unit.valuesToPrepare.select({ |item|
			item.isKindOf( UMap );
		});
		prepareThese.do({ |umap|
			this.prPrepareUnit( umap );
		});
		prepareThese.do({ |umap|
			 if( umap.def.isKindOf( FuncUMapDef ) ) {
				umap.def.doFunc( umap );
			 };
		});
	}
	
	prPatternsToValues { |unit|
		unit.args.pairsDo({ |key, item|
			if( item.isKindOf( UMap ) ) {
				if( item.def.isKindOf( UPatDef ) ) {
					unit.set( key, item.value );
				} {
					this.prPatternsToValues( item );
				};
			};
		});
	}
	
	prResetStreams { |unit|
		unit.args.pairsDo({ |key, item|
			if( item.isKindOf( UMap ) ) {
				item.resetStream;
				this.prResetStreams( item );
			};
		});
	}
	
	next { |duration|
		var next;
		next = UChain( *units.deepCopy );
		next.fadeIn = this.fadeIn;
		next.fadeOut = this.fadeOut;
		next.units.do({ |unit|
			this.prPrepareUnit( unit );
			this.prPatternsToValues( unit );
		});
		next.duration = duration ?? { this.getSustain; };
		next.parent = this;
		^next;
	}
	
	getSustain {
		if( sustain.isKindOf( UMap ) && { sustain.def.isKindOf( FuncUMapDef ) } ) {
			this.prPrepareUnit( sustain );
			sustain.def.doFunc( sustain );
		};
		^sustain.next;
	}
	
	getTimeToNext {
		if( timeToNext.isKindOf( UMap ) && { timeToNext.def.isKindOf( FuncUMapDef ) } ) {
			this.prPrepareUnit( timeToNext );
			timeToNext.def.doFunc( timeToNext );
		};
		^timeToNext.next;
	}
	
	isPlaying { ^task.isPlaying }
	
	makeRoutine { |target, startPos = 0, action|
		^Routine({
			var time = 0, n = 0;
			var zeroCount = 0;
			var next, timeToNext;
			while { ( this.releaseSelf == false or: { (time <= (duration - startPos)) }) 
					&& { n < repeats } && { zeroCount < maxSimultaneousStarts }
			} {
				next = this.next;
				timeToNext = this.getTimeToNext;
				this.localPos = time;
				action.value( next, target, time );
				timeToNext.wait;
				time = time + timeToNext;
				if( timeToNext == 0 ) { zeroCount = zeroCount + 1 } { zeroCount = 0 };
				if( zeroCount >= maxSimultaneousStarts ) {
					"UPattern ending; maxSimultaneousStarts (%) reached\n"
						.postf( zeroCount )
				};
				n = n + 1;
			};
			this.changed( \end );
		})
	}
	
	makePrepareFunc { |routine, action|
		var multiAction;

		
	}
	
	prepare { |target, startPos = 0, action|
		var waitTime, firstEvent = true;
		var multiAction, firstAction;
		var i = 0;
		this.stop;
		waitTime = this.waitTime; // fixed waitTime for all events
		units.do({ |unit| this.prResetStreams( unit ); });
		preparedEvents = Array(512);
		if( waitTime > 0 ) {
			multiAction = MultiActionFunc( action );
			firstAction = multiAction.getAction;
			routine = this.makeRoutine( target, startPos, { |chain, target, time| 
				if( time < waitTime ) {
					// "preparing %\n".postf( time.asSMPTEString );
					chain.prepare( target, action: multiAction.getAction );
					preparedEvents = preparedEvents.add( chain.startTime_( time ) );
				} {
					if( firstEvent ) {
						nil.yield;
						(time - waitTime).wait;
						firstEvent = false;
					};
					preparedEvents = preparedEvents.add(
						chain.startTime_( time ).prepareWaitAndStart( target, startAction: {
							preparedEvents.remove( chain );
						} );
					);
				};
			} );
			while { routine.next.notNil; } { i = i+1 };
			// "% events prepared\n".postf( i );
			preparedEventsRoutine = Task({
				var waitTime, time = 0;
				preparedEvents.copy.do({ |chain|
					(chain.startTime - time).wait;
					time = chain.startTime;
					chain.start;
					preparedEvents.remove( chain );
				});
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
		this.changed( \end );
	}
	
	release { this.stop }
	
	currentChains { ^groupDict.keys.select({ |item| item.parent === this }) }
	
	stopChains { |releaseTime| this.currentChains.do(_.release( releaseTime ) ) }
}