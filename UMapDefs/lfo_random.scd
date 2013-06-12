
UMapDef( \lfo_random, { |time = #[0.1,0.1], lag = 0, seed = 12345, range = #[0.0,1.0]|
	var sig;
	URandSeed.ir(seed);
	sig = Duty.kr( Dwhite(*time), 0, Dwhite(0,1)).lag(lag).linlin(0,1,*range);
	UMapOut.kr(sig);
})
	.setSpec( \time, RangeSpec(0.001,100, 0, inf, \exp, 0, [0.1,0.1]) )
	.setSpec( \lag, [0,10,\lin].asSpec )
	.setSpec( \seed, PositiveIntegerSpec(12345), \init )
	.mappedArgs_( [ \range ] )
	.category_( 'modulation' )
