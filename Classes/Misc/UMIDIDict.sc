UMIDIDict {
	classvar <>dict;
	classvar <>midiFuncs;
	classvar <>verbose = false;

	*initClass {
		dict = MultiLevelIdentityDictionary();
	}

	// structure:
	// latest message
	// uid : ()
	//   noteOn: Order()
	//      channel: Order()
	//          noteNumber: velocity
	//   noteOff: Order()
	//      channel: Order()
	//          noteNumber: velocity
	//   note: Order()
	//      channel: Order()
	//          noteNumber: velocity (0 = off)
	//   cc: Order()
	//      channel: Order()
	//          cc: value
	//   bend: Order()
	//      channel: value
	// ... more later?

	*start { |force = false|
		if( midiFuncs.isNil or: { midiFuncs.any({ |item|item.enabled.not }) } or: force ) {
			midiFuncs.do(_.free);
			if( MIDIClient.initialized.not ) {
				MIDIIn.connectAll;
			};
			midiFuncs = [
				MIDIFunc.noteOn({ |val, num, chan, src|
					this.addNoteOn(src,chan,num,val);
				}),
				MIDIFunc.noteOff({ |val, num, chan, src|
					this.addNoteOff(src,chan,num,val);
				}),
				MIDIFunc.cc({ |val, num, chan, src|
					this.addControl(src,chan,num,val);
				}),
				MIDIFunc.bend({ |val, chan, src|
					this.addBend(src,chan,val);
				}),
			];
		};
	}

	*end {
		midiFuncs.do(_.free);
		midiFuncs = nil;
	}

	*findPort { |src|
		^MIDIClient.sources.detect({ |item| item.uid == src });
	}

	*addEvent { |src, type ...args| // chan, num, val
		dict.put( src ? \unknown, type, *args );
		dict.put( \any, type, *args );
		this.changed( type, src, *args );
		if( verbose ) { "UMIDIDict:addEvent : %, %, %\n".postf( src, type.cs, args.join(", ") ); };
	}

	*addNoteOn { |src, chan, num, val|
		this.addEvent( src, \noteOn, chan, num, val );
		this.addEvent( src, \note, chan, num, val );
	}

	*addNoteOff { |src, chan, num, val|
		this.addEvent( src, \noteOff, chan, num, val );
		this.addEvent( src, \note, chan, num, 0 );
	}

	*addControl { |src, chan, num, val|
		this.addEvent( src, \cc, chan, num, val );
	}

	*addBend { |src, chan, num, val|
		this.addEvent( src, \bend, chan, num, val );
	}

	*getEvent { |src, type ...args|
		^dict.at( src ? \any, type, *args );
	}
}