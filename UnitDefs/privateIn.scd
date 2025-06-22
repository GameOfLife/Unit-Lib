/*
\privateIn

Read sound from a private audio bus. A private bus in Unit-Lib is a bus that is not used by hardware, internal UChain or aux i/o.

bus: the bus that you want to use to read audio from.
numChannels: number of channels (*).

(*) only the following numbers of channels are allowed:
[1,2,3,4,5,6,7,8,9,10,12,16,24,25,32,36,48,49,64]

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

MultiChannelUdef( \privateIn, { |bus = 0|
	var input;
	input = UPrivateIn.ar( bus, Udef.numChannels );
	UOut.ar( 0, input );
}, category: \private ).setSpec( \bus, PositiveIntegerSpec(0,0,inf) );

