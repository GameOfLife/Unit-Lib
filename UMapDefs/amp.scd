/*
\amp

A simple gain stage. Can set a gain in dB and inverse phase.

gain: added gain (dB)
inverse: inverse phase (false/true) 
numChannels: number of channels

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

MultiChannelUdef( \amp, { |amp = 0.1, inverse = 0|
	var in;
	in = UIn.ar(0, Udef.numChannels );
	in = in * amp * inverse.linlin(0,1,1,-1);
	UOut.ar( 0, in );
})
	.category_( \utility )
	.setSpec( \amp, \amp.asSpec )
	.setSpec( \inverse, BoolSpec(false) );