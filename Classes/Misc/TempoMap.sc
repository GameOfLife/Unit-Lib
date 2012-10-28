TempoMap {
	
	// a map to convert beats to seconds over a timeline
	// tempo 1 means: 1 beat is 1 second (60 BPM)
	
	// events: a sorted array with elements [ tempo, beat, time ]
	// the 'time' slot is updated automatically for all events when adding one
	
	var <events, <size;
	
	*new { |tempo = 1|
		^super.newCopyArgs.init( tempo )
	}
	
	init { |tempo = 1| // initial tempo
		events = [ [ tempo, 0, 0 ] ]; // first event always needs to be at 0 position
		size = 1;
		this.changed( \init );
	}
	
	events_ { |evts| // needs to be in the correct format
		events = evts ? events;
		this.prUpdateTimes;
		this.changed( \events );
	}
	
	*secondsToBeats { |time = 0, tempo = 1|
		^time * tempo;
	}
	
	*beatsToSeconds { |beat = 0, tempo = 1|
		^beat / tempo;
	}
	
	prUpdateTimes {
		var time = 0, lastBeat = 0, lastTempo = 1;
		size = events.size; // update size
		events.sort({ |a,b| a[1] <= b[1] });
		events.do({ |item| // update times
			var beat;
			beat = item[1];
			item[2] = time = time + this.class.beatsToSeconds( beat - lastBeat, lastTempo );
			lastBeat = beat;
			lastTempo = item[0];
		});
	}
	
	prUpdateBeats {
		var beat = 0, lastTime = 0, lastTempo = 1;
		size = events.size; // update size
		events.sort({ |a,b| a[2] <= b[2] });
		events.do({ |item| // update times
			var time;
			time = item[2];
			item[1] = beat = beat + this.class.secondsToBeats( time - lastTime, lastTempo );
			lastTime = time;
			lastTempo = item[0];
		});
	}
	
	deleteDuplicates { // remove events that have the same tempo as the event before
		var tempo;
		events = events.collect({ |item|
			if( tempo == item[0] ) {
				nil;
			} {
				tempo = item[0];
				item
			};
		}).select(_.notNil);
		size = events.size; // update size
		this.changed( \events );
	}
	
	put { |...args| // beat, tempo pairs
		args.pairsDo({ |beat, tempo|
			events.removeAllSuchThat({ |item| item[1] == beat });
			events.add([ tempo, beat, 0 ]);
		});
		this.prUpdateTimes;
		this.changed( \events );
	}
	
	prIndexAtBeat { |beat = 0|
		var i = 0;
		while { (i < size) && { events[i][1] <= beat } } {
			i = i+1;
		};
		^i-1;
	}
	
	prIndexAtTime { |time = 0|
		var i = 0;
		while { (i < size) && { events[i][2] <= time } } {
			i = i+1;
		};
		^i-1;
	}
	
	tempoAtBeat { |beat = 0|
		^events[ this.prIndexAtBeat( beat ) ][0];
	}
	
	tempoAtTime { |time = 0|
		^events[ this.prIndexAtTime( time ) ][0];
	}
	
	tempoAtBeat_ { |tempo = 1, beat = 0, add = false|
		if( add ) {
			this.put( beat, tempo );
		} {
			events[ this.prIndexAtBeat( beat ) ][0] = tempo;
			this.prUpdateTimes;
			this.changed( \events );
		};
	}
	
	tempoAtTime_ { |tempo = 1, time = 0, add = false| 
		// !! changes the beats of events after !!
		if( add ) {
			this.put( this.beatAtTime( time ), tempo );
		} {
			events[ this.prIndexAtTime( time ) ][0] = tempo;
			this.prUpdateBeats;
			this.changed( \events );
		};
	}

	timeAtBeat { |beat = 0|
		var evt;
		evt = events[ this.prIndexAtBeat( beat ) ];
		^evt[2] + ((beat - evt[1]) / evt[0]);
	}
	
	beatAtTime { |time = 0|
		var evt;
		evt = events[ this.prIndexAtTime( time ) ];
		^evt[1] + ((time - evt[2]) * evt[0]);
	}
}

BPMTempoMap : TempoMap {
	
	*new { |tempo = 60|
		^super.newCopyArgs.init( tempo )
	}

	*secondsToBeats { |time = 0, tempo = 1|
		^time * (tempo / 60);
	}
	
	*beatsToSeconds { |beat = 0, tempo = 1|
		^beat / (tempo / 60);
	}
	
}