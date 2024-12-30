/*
\midi_ccOut

Creates a Unit that can send MIDI control values over a MIDI interface connected to the computer, or to other applications via virtual MIDI devices. It utilizes the standard MIDI tools available in SuperCollider. The Unit doesn't make sound, if it is the only Unit in a UChain the whole UChain will not make sound. The value is sent when the UChain is started, or when the 'val' parameter is changed.

channel: the MIDI channel (0-15)
cc: the MIDI control number (0-127)
val: the MIDI control value (0-127)
midiOut: the index of the connected MIDI interface (0-..) The order of interfaces can be seen in the post window of SuperCollider and may look something like this:

MIDI Destinations:
    MIDIEndPoint("System", "Timer")
    MIDIEndPoint("System", "Announce")
    MIDIEndPoint("Midi Through", "Midi Through Port-0")
    MIDIEndPoint("SuperCollider", "out0")
    MIDIEndPoint("SuperCollider", "out1")

** Note; if the midiOut index points to a non-existing MIDI destination an error message will be displayed and playback may stop.

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

FreeUdef( "midi_ccOut", [
	[ \channel, 0, IntegerSpec(0,0,15), false, \init ],
	[ \cc, 64, IntegerSpec(64,0,127), false, \init ],
	[ \val, 64, IntegerSpec(64,0,127), false, \init ],
	[ \midiOut, 0, IntegerSpec(0,0, 255), false, \init ],
])
.apxCPU_( 0 )
.prepareFunc_({ |server, unit, action, startPos = 0|
	var env, channel, cc, val, midiOut;
	env = unit.def.env;
	env.put( unit, () );
	channel = unit.get( \channel );
	cc = unit.get( \cc );
	val = unit.get( \val );
	midiOut = unit.get( \midiOut );
	midiOut = midiOut.value.asInteger;
	env[ unit ].channel = channel.value.asInteger;
	env[ unit ].cc = cc.value.asInteger;
	env[ unit ].val = val.value.asInteger;
	env[ unit ].midiOut = midiOut;

	if(MIDIClient.initialized.not,{ MIDIIn.connectAll });
	if( env[ \midiOuts ].isNil ) {
		env[ \midiOuts ] = Order();
	};
	if( env[ \midiOuts ][ midiOut ].isNil ) {
		env[ \midiOuts ][ midiOut ] = MIDIOut( midiOut );
		env[ \midiOuts ][ midiOut ].latency_(0);
		env[ \midiOuts ][ midiOut ].connect( midiOut );
	};

	[ channel, cc, val ].do({ |item|
		if( item.isKindOf( UMap ) ) { item.start };
	});
	action.value;
})
.createSynthFunc_({ |unit, server, startTime = 0 |
	var channel, cc, val, midiOut;
	if( unit.def.env[ unit ].notNil ) {
		#channel, cc, val, midiOut = unit.def.env[ unit ].atAll( [ \channel, \cc, \val, \midiOut ] );
		unit.def.env[ unit ] = nil;
		midiOut = unit.def.env[ \midiOuts ][ midiOut ];
		midiOut.control( channel, cc, val );
	};
	nil;
})
	.createsSynth_( false )
.setSynthFunc_({ |unit ...keyValuePairs|
	var midiOut;
	keyValuePairs.pairsDo({ |a,b|
		if( a == \val ) {
			midiOut = unit.def.env[ \midiOuts ] !? _[ unit.get( \midiOut ).value.asInteger ];
			if( midiOut.notNil ) {
				midiOut.control(
					unit.get( \channel ).value.asInteger,
					unit.get( \cc ).value.asInteger,
					unit.get( \val ).value.asInteger,
				);
			};
		};
	});
})
	.category_( 'midi' );