
UMapDef( \line_rel, { |range = #[0.0,1.0], up = 1, curve = 0, timeScale = 1.0|
	var sig, lineRange = [0,1];
	lineRange = if( up, lineRange, lineRange.reverse );
	sig = ULineRel.kr( lineRange[0], lineRange[1], nil, timeScale );
	sig = Select.kr( curve.inRange(-0.001,0.001), [
		sig.lincurve(0,1, range[0], range[1], curve ),
		sig.linlin(0,1, range[0], range[1] )
	]);
	UMapOut.kr(sig);
})
	.setSpec( \up, BoolSpec(true) )
	.setSpec( \curve, [-16,16,\lin,0,0].asSpec )
	.setSpec( \timeScale, [0.25,4,\exp,0,1].asSpec, \init )
	.category_( 'automation' )
	.mappedArgs_( [ \range ] );