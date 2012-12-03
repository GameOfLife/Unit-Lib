+ UChain {
	
	*defaultProtoUChain {
		var chain;
		chain = UChain.fromPreset( \default );
		chain[0].defName = \default;
		^chain;
	}
	
	*defaultProtoEvent {
		^this.defaultProtoUChain.asProtoEvent;
	}
	
	asProtoEvent {
		var evt;
		evt = ();
		evt[ \play ] = {
			var newChain;
			newChain = this.deepCopy;
			newChain.fromEnvironment;
			newChain.prepareWaitAndStart; // beware of different wait times per event
		};
		^evt;
	}
	
	fromEnvironment { |env|
		env = env ? currentEnvironment ?? { () };
		units.collect(_.keys).flatten(1).do({ |item|
			if( currentEnvironment[ item ].notNil ) {
				this.set( item, currentEnvironment[ item ].value );
			};
		});
		
		~fadeIn.value !? this.fadeIn_(_);
		~fadeOut.value !? this.fadeOut_(_);
		~gain.value !? this.gain_(_);
		~track.value !? this.track_(_);
		~sustain.value !? this.duration_(_);
	}
	
}

+ UScore {
	
	*fromPattern { |pattern, proto, startTime = 0, maxEvents = 200|
		^this.new.fromPattern( pattern, proto, startTime, maxEvents );
	}
	
	fromPattern { |pattern, proto, startTime = 0, maxEvents = 200|
		var newChain;
		var stream, time, count = 0, event;
		
		time = startTime ? 0;
		
		proto = proto ?? { 
			var chain;
			chain = UChain.fromPreset( \default );
			chain[0].defName = \default;
			chain;
		};
		
		stream = pattern.asStream;
		
		while { (event = stream.next(Event.default)).notNil && 
				{ (count = count + 1) <= maxEvents } } { 
			event.use({
				
				newChain = proto.deepCopy;
				newChain.fromEnvironment;
				newChain.startTime = time;
				
				this.add( newChain );
				
				time = time + event.delta;
			});	
		};
	}
}

+ Pattern {
	asUScore { |proto, maxEvents = 200|
		^UScore.fromPattern( this, proto, 0, maxEvents );
	}
}