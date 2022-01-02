/*
\amp

A simple amplitude gain stage. Can set an amplitude (multiplier) and inverse phase. Note that this Udef can only make the input signal softer, not louder.

amp: amplitude (0-1)
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
	.setSpec( \amp, \amp.asSpec, \normal )
	.setSpec( \inverse, BoolSpec(false) );