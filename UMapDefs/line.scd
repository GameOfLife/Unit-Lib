
UMapDef( \line, { |range = #[0.0,1.0], up = 1, duration = 10|
	var sig;
	range = if( up, range, range.reverse );
	sig = ULine.kr( range[0], range[1], duration );
	UMapOut.kr(sig);
})
	.setSpec( \up, BoolSpec(true) )
	.setSpec( \duration, SMPTESpec() );