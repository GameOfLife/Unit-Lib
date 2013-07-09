
UMapDef( \lfo_noise, { |freq = 2, type = 0, seed = 12345, range = #[0.0,1.0]|
	var sig;
	URandSeed.ir(seed);
	sig = [
		LFDNoise0.kr( freq ),
		LFDNoise1.kr( freq ),
		LFDNoise3.kr( freq )
	];
	sig = Select.kr( type, sig ).linlin(-1,1,*range);
	UMapOut.kr(sig);
})
	.setSpec( \type, [0,2,\lin,1,0].asSpec )
	.setSpec( \freq, FreqSpec(0.01,300, default: 2) )
	.setSpec( \seed, PositiveIntegerSpec(12345), \init )
	.mappedArgs_( [ \range ] )
	.category_( 'modulation' )
