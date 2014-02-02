/*
\tanhDistort

Distorts the sound via a hyperbolic tangent function.
inGain: input gain (dB).outGain:output gain (dB).lag: smoothing time for parameter changes.

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

Udef( \tanhDistort, { |inGain = 0, outGain = 0, lag = 0.1|
	UOut.ar( 0, (UIn.ar( 0 ) * inGain.dbamp.lag(lag)).tanh * outGain.dbamp.lag(lag) )
} )
	.category_( \distortion )
	.setSpec( \inGain, [0, 60].asSpec )
	.setSpec( \outGain, [-60, 0].asSpec )
	.setSpec( \lag, [0,1,\lin].asSpec );