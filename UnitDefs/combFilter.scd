/*
\combFilter

A comb filter.

freq: frequency.
decayTime: decay time in seconds
dry: level of dry (unprocessed) signal
lag: smoothing time for the freq parameter.

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

Udef( \combFilter, { |freq = 440, decayTime = 1, dry = 0, lag = 0|
	var in;
	in = UIn.ar(0,1);
	in = CombC.ar( in, 1/2, 1/freq.lag(lag), decayTime ) + (in * dry);
	UOut.ar( 0, in );
})
	.category_( \filter )
	.setSpec( \freq, FreqSpec( 2, 20000 ))
	.setSpec( \decayTime, [ 0.01, 10, \exp, 0, 0.1 ].asSpec )
	.setSpec( \dry, \amp.asSpec )
	.setSpec( \lag, [0,1,\lin].asSpec )