/*
\auxIn

Read sound from one of the 32 internal 'aux' buses, or, in 'external' mode, from an audio input of your audio interface.

In 'internal' mode, the signal can be sent from another UChain using 'auxOut'. For this to work on every system, set the receiving UChain's addAction to \addToTail or \addAfter, and enable 'global'. As an alternative to 'global', it is also possible to use an ugroup; In that case, make sure that the sending ('auxOut') unit is in the same ugroup as the receiving one ('auxIn').

In 'external' mode the auxIn receives audio from an external input. This is similar to the soundIn Udef.

bus: the aux bus to read from (0-31)
type: \internal or \external

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

(
var defs, def;

defs = [
	Udef( \internal, { |bus = 0|
		var input;
		input = UAuxIn.ar( bus );
		UOut.ar( 0, input );
	}, addToAll: false, extraPrefix: \aux_ )
	.setSpec( \bus, UAuxIn.specs[ \bus ] ),

	Udef( \external, { |bus = 0|
		var input;
		input = SoundIn.ar( bus );
		input * if( bus > (NumInputBuses.ir - 1), 0, 1 );
		UOut.ar( 0, input );
	}, addToAll: false, extraPrefix: \auxIn_ )
	.setSpec( \bus, HardwareBusSpec( \input, 1 ) )
];

def = MultiUdef( \auxIn, defs, \input, \type, false );

def;
)