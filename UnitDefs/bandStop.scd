/*
\bandStop

A band-stop filter

freq: center frequency.
bw: the bandwidth in octaves between -3 dB frequencies.

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

Udef( \bandStop, { |freq = 440, bw = 1|
	var in;
	in = UIn.ar(0,1);
	in = BBandStop.ar( in, freq.clip(20,20000), bw );
	UOut.ar( 0, in );
})
	.category_( \private )
	.setSpec( \freq, \freq.asSpec )
	.setSpec( \bw, [ 0.01, 10, \exp, 0, 1 ].asSpec )
	.uchainInitFunc_({ |unit|
		var args;
		if( unit.def.category == \private ) {
			args = [ \bw, unit.get( \bw ) ];
			unit.defName = \filter;
			unit.set( \type, \bandStop );
			unit.set( *args );
		};
	});