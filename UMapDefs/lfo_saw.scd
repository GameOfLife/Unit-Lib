
UMapDef( \lfo_saw, { |freq = 2, phase = 0, width = 0.0, range = #[0.0,1.0]|
	var sig;
	sig = VarSaw.kr(freq, phase, width).range(*range);
	UMapOut.kr(sig);
})
	.setSpec( \phase, [0,1,\lin,0,0], \init )
	.setSpec( \freq, FreqSpec(0.01,300, default: 2) )
	.mappedArgs_( [ \range ] )
	.category_( 'modulation' )
