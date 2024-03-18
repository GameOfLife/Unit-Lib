/*
\soundIn

Read sound from a hardware audio input.
WFSCollider users: This unit will only get sound input on the master server. To send the sound to the rendering servers, use a \wfsMasterOut unit, and a new UChain starting with a \wfsServerIn.

bus: the bus that you want to use to read audio from. Starts counting from 0; 0 means the first input of your audio device.
numChannels: number of channels (*).

(*) only the following numbers of channels are allowed:
1,2,3,4,5,6,7,8,10,12,16,24,32

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

var def = MultiChannelUdef( \soundIn, { |bus = 0|
	var input;
	input = ((..Udef.numChannels - 1) + bus).collect({ |item| SoundIn.ar( item ) });
	input = input.collect({ |input|
		input * if( bus > (NumInputBuses.ir - 1), 0, 1 );
	});
	UOut.ar( 0, input );
} )
	.category_( \input );

def.udefs.do({ |def|
	def.setSpec( \bus, HardwareBusSpec(\input, def.name ) );
});

def