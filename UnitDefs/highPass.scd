/*
\highPass

A resonant high-pass filter

freq: cutoff frequency.
rq: the reciprocal of Q. bandwidth / cutoffFreq

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

Udef( \highPass, { |freq = 440, rq = 1|
	var in;
	in = UIn.ar(0,1);
	in = BHiPass.ar( in, freq.clip(20,20000), rq );
	UOut.ar( 0, in );
})
	.category_( \private )
	.setSpec( \freq, \freq.asSpec )
	.setSpec( \rq, [ 0.01, 10, \exp, 0, 1 ].asSpec )
	.uchainInitFunc_({ |unit|
		var args;
		if( unit.def.category == \private ) {
			unit.defName = \filter;
			unit.set( \type, \highPass );
		};
	});