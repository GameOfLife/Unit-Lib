UMIDIDict {
	classvar <>dict;
	classvar <>midiFuncs;
	classvar <>verbose = false;
	classvar <>portDict;
	classvar <>midiOuts;

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
				}).permanent_( true ),
				MIDIFunc.noteOff({ |val, num, chan, src|
					this.addNoteOff(src,chan,num,val);
				}).permanent_( true ),
				MIDIFunc.cc({ |val, num, chan, src|
					this.addControl(src,chan,num,val);
				}).permanent_( true ),
				MIDIFunc.bend({ |val, chan, src|
					this.addBend(src,chan,val);
				}).permanent_( true ),
				MIDIFunc.touch({ |val, chan, src|
					this.addTouch(src,chan,val);
				}).permanent_( true ),
				MIDIFunc.polytouch({ |val, num, chan, src|
					this.addPolytouch(src,chan,num,val);
				}).permanent_( true ),
				MIDIFunc.program({ |val, chan, src|
					this.addProgram(src,chan,val);
				})
			];
			this.makePortDict;
			this.makeMIDIOuts;
		};
	}

	*end {
		midiFuncs.do(_.free);
		midiFuncs = nil;
	}

	*restart {
		MIDIClient.init;
		MIDIIn.connectAll;
		this.start( true );
	}

	*findPort { |src|
		^MIDIClient.sources.detect({ |item| item.uid == src });
	}

	*makePortName { |device, port|
		if( device.isKindOf( MIDIEndPoint ) ) {
			port = device.name;
			device = device.device;
		};
		^[ device, port ].join("/").asSymbol
	}

	*makePortDict {
		portDict = portDict ?? { IdentityDictionary() };
		MIDIClient.sources.do({ |source|
			portDict[ source.uid ] = this.makePortName( source );
		});
	}

	*findUIDs { |portname|
		var uids;
		portDict.keysValuesDo({ |uid, name|
			if( name.matchOSCAddressPattern( portname ) ) {
				uids = uids.add( uid );
			};
		});
		^uids;
	}

	*makeMIDIOuts {
		midiOuts = midiOuts ?? { IdentityDictionary() };
		MIDIClient.destinations.do({ |destination, i|
			midiOuts[ this.makePortName( destination ) ] = MIDIOut( i, destination.uid ).latency_(0);
		});
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

	*addBend { |src, chan, val|
		this.addEvent( src, \bend, chan, val );
	}

	*addTouch { |src, chan, val|
		this.addEvent( src, \touch, chan, val );
	}

	*addPolytouch { |src, chan, num, val|
		this.addEvent( src, \polytouch, chan, num, val );
	}

	*addProgram { |src, chan, val|
		this.addEvent( src, \program, chan, val );
	}

	*getActiveNotes { |src = 'any', chan = 0|
		^dict[ src, \note, chan ] !? { |x|
			x.keys.select({ |key|
				dict[ src, \note, chan, key ] != 0
			}).asArray.sort;
		}
	}

	*getEvent { |src, type ...args|
		var uids;
		while { args.last.isNil && { args.size > 0 } } {
			args.pop;
		};
		if( src.isKindOf( Symbol ) ) {
			uids = this.findUIDs( src );
			switch( uids.size,
				0, { ^nil },
				1, { ^this.prGetEvent(uids[0], type, *args ) },
				{ ^uids
					.collect({ |uid| this.prGetEvent( uid, type, *args ) })
					.detect(_.notNil)
				}
			)
		} {
			^this.prGetEvent( src, type, *args );
		}
	}

	*prGetEvent { |src, type ...args|
		var nilIndex, keys;
		nilIndex = args.detectIndex(_.isNil);
		if( nilIndex.isNil ) {
			^dict.at( src ? \any, type, *args );
		} {
			keys = dict.at( src ? \any, type, *args[..nilIndex-1] ) !? _.keys;
			if( keys.size > 0 ) {
				^keys.asArray.collect({ |key|
					this.prGetEvent( src, type, *args.copy.put( nilIndex, key ) );
				}).select(_.notNil);
			} {
				^nil;
			};
		};
	}

	*matchDevice { |testDevice, device|
		if( device.isKindOf( Symbol ).not ) {
			device = this.portDict[ device ] ? '*/*';
		};
		^device.matchOSCAddressPattern( testDevice ? '*/*' );
	}

	*matchEvent { |testArray, eventArray|
		if( this.matchDevice( testArray[0], eventArray[0] ) ) {
			eventArray[1..].do({ |item,i|
				if( testArray[i+1].notNil ) {
					if( testArray[i+1] != item ) { ^false };
				};
			});
		} {
			^false
		};
		^true;
	}
}