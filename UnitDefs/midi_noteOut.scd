/*
\midi_noteOut

Creates a Unit that can send MIDI notes over a MIDI interface connected to the computer, or to other applications via virtual MIDI devices. It utilizes the standard MIDI tools available in SuperCollider. The Unit doesn't make sound, if it is the only Unit in a UChain the whole UChain will not make sound.

** Note; in this version of the Udef the stop button will not work, the note will always have the duration of the UChain. If the cmd-. key is pressed while a note is playing it will result in a hanging MIDI note. Also, if the UChain has an infinite duration, the note will not be stopped even if the UChain is stopped.

channel: the MIDI channel of the note (0-15)
note: the MIDI note number of the note (0-127)
velo: the MIDI velocity of the note (1-127)
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

FreeUdef( "midi_noteOut", [
	[ \channel, 0, IntegerSpec(0,0,15), false, \init ],
	[ \note, 64, IntegerSpec(64,0,127), false, \init ],
	[ \velo, 64, IntegerSpec(64,1,127), false, \init ],
	[ \midiOut, 0, IntegerSpec(0,0, 255), false, \init ],
	[ \u_dur, inf, [0,inf,\lin,0.0,0].asControlSpec, true, \init ],
])
.apxCPU_( 0 )
.prepareFunc_({ |server, unit, action, startPos = 0|
	var env, channel, note, velo, dur, midiOut;
	env = unit.def.env;
	env.put( unit, () );
	channel = unit.get( \channel );
	note = unit.get( \note );
	velo = unit.get( \velo );
	midiOut = unit.get( \midiOut );
	dur = unit.get( \u_dur );
	midiOut = midiOut.value.asInteger;
	env[ unit ].channel = channel.value.asInteger;
	env[ unit ].note = note.value.asInteger;
	env[ unit ].velo = velo.value.asInteger;
	env[ unit ].midiOut = midiOut;
	env[ unit ].dur = dur;
	env[ unit ].startPos = startPos ? 0;

	if(MIDIClient.initialized.not,{ MIDIIn.connectAll });
	if( env[ \midiOuts ].isNil ) {
		env[ \midiOuts ] = Order();
	};
	if( env[ \midiOuts ][ midiOut ].isNil ) {
		env[ \midiOuts ][ midiOut ] = MIDIOut( midiOut );
		env[ \midiOuts ][ midiOut ].latency_(0);
		env[ \midiOuts ][ midiOut ].connect( midiOut );
	};

	[ channel, note, velo ].do({ |item|
		if( item.isKindOf( UMap ) ) { item.start };
	});
	action.value;
})
.createSynthFunc_({ |unit, server, startTime = 0 |
	var channel, note, velo, midiOut, dur, startPos;
	if( unit.def.env[ unit ].notNil ) {
		#channel, note, velo, midiOut, dur, startPos = unit.def.env[ unit ]
		.atAll( [ \channel, \note, \velo, \midiOut, \dur, \startPos ] );
		unit.def.env[ unit ] = nil;
		midiOut = unit.def.env[ \midiOuts ][ midiOut ];
		midiOut.noteOn( channel, note, velo );
		if( dur != inf ) {
			{
				( dur - startPos ).wait;
				midiOut.noteOff( channel, note );
			}.fork;
		};
	};
	nil;
})
	.createsSynth_( false )
	.setSynthFunc_({ |unit ...keyValuePairs|
	    //keyValuePairs.postln;
		//unit.get( \key ).asSymbol.uEnvirPut( unit.get( \point ).value, WFSPointSpec(200) );
	})
	.category_( 'midi' );