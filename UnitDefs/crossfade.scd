/*
\crossfade

Equal power crossfade between two sources.

crossfade: amount of left/right
numChannels: number of channels

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

MultiChannelUdef( \crossfade, { |crossfade = 0.5, level = 1.0|
	var in1, in2, out;
	in1 = UIn.ar(0, Udef.numChannels, endPoint: true );
	in2 = UIn.ar( Udef.numChannels, Udef.numChannels, endPoint: true );
	out = XFade2.ar( in1, in2, crossfade.linlin(0,1,-1,1), level);
	UOut.ar( 0, out );
})
	.category_( \utility )