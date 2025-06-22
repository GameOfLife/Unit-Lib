/*
\privateOut

Write sound to a private audio bus. A private bus in Unit-Lib is a bus that is not used by hardware, internal UChain or aux i/o.

bus: the bus that you want to use to read audio from. Starts counting from 0; 0 means the first input of your audio device.
numChannels: number of channels (*).

(*) only the following numbers of channels are allowed:
[1,2,3,4,5,6,7,8,9,10,12,16,24,25,32,36,48,49,64]

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

MultiChannelUdef( \privateOut, { |bus = 0|
	var sig;
	sig = UIn.ar(0, Udef.numChannels );
	UPrivateOut.ar( bus, sig * UEnv.kr( extraSilence: 0.2, useGlobalGain: 0 ) );
}, category: \private ).setSpec( \bus, PositiveIntegerSpec(0,0,inf) );