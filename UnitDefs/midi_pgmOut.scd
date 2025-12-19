/*
\midi_pgmOut

Creates a Unit that can send MIDI program values over a MIDI interface connected to the computer, or to other applications via virtual MIDI devices. It utilizes the standard MIDI tools available in SuperCollider. The Unit doesn't make sound, if it is the only Unit in a UChain the whole UChain will not make sound.

channel: the MIDI channel (0-15)
pgm: the MIDI program number (0-127)
bank: the optional MIDI bank value (-1-127, -1 = no bank)
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

FreeUdef( "midi_pgmOut", [
	[ \channel, 0, IntegerSpec(0,0,15), false, \init ],
	[ \pgm, 0, IntegerSpec(64,0,127), false, \init ],
	[ \bank, -1, IntegerSpec(64,-1,127), false, \init ],
	[ \midiOut, 0, IntegerSpec(0,0, 255), false, \init ],
])
.apxCPU_( 0 )
.prepareFunc_({ |server, unit, action, startPos = 0|
	var env, channel, pgm, bank, midiOut;
	env = unit.def.env;
	env.put( unit, () );
	channel = unit.get( \channel );
	pgm = unit.get( \pgm );
	bank = unit.get( \bank );
	midiOut = unit.get( \midiOut );
	midiOut = midiOut.value.asInteger;
	env[ unit ].channel = channel.value.asInteger;
	env[ unit ].pgm = pgm.value.asInteger;
	env[ unit ].bank = bank.value.asInteger;
	env[ unit ].midiOut = midiOut;

	UMIDIDict.start(false);

	[ channel, pgm, bank ].do({ |item|
		if( item.isKindOf( UMap ) ) { item.start };
	});
	action.value;
})
.createSynthFunc_({ |unit, server, startTime = 0 |
	var channel, pgm, bank, midiOutIndex, midiOut;
	if( unit.def.env[ unit ].notNil ) {
		#channel, pgm, bank, midiOutIndex = unit.def.env[ unit ].atAll( [ \channel, \pgm, \bank, \midiOut ] );
		unit.def.env[ unit ] = nil;
		midiOut = UMIDIDict.indexedMIDIOuts[ midiOutIndex ];
		if( midiOut.notNil ) {
			if( bank >= 0 ) {
				midiOut.control( channel, 0, bank );
			};
			midiOut.program( channel, pgm );
		} {
			"midi_pgmOut: MIDI out % not connected\n".postf( midiOutIndex );
		};
	};
	nil;
})
.createsSynth_( false )
.category_( 'midi' );