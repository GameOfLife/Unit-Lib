(
UMapDef( \map_control, { |bus = 0, rangeIn = #[0.0, 1.0], rangeOut = #[0.0,1.0], invert = 0, curve = 0,
	lag = 0.0, clipMode = 0|
	var sig;
	sig = In.kr(bus + UIn.firstControlBus);
	sig = sig.linlin(*rangeIn ++ [-1,1,\none]);
	sig = if( invert, sig.neg, sig );
	sig = Select.kr( curve.inRange(-0.001,0.001), [
		sig.lincurve(-1,1,-1,1, curve, \none ),
		sig
	]);
	sig = Select.kr( clipMode, [
		sig.clip2(1),
		sig.fold2(1),
		sig.wrap2(1),
		sig.tanh,
		sig.softclip,
		sig.distort,
		(sig.wrap2(1) * pi).sin,
		sig
	]);
	sig = sig.lag( lag );
	sig = sig.linlin(-1,1,*rangeOut ++ [ \none ]);
	UMapOut.kr(sig);
})
	.setSpec( \rangeIn, [-1,1].asSpec.asRangeSpec )
	.setSpec( \clipMode, ListSpec( (..7), 0, [ 
		\clip, \fold, \wrap, \tanh, \softclip, \distort, \wrap_sine, \none 
		] ) 
	)
	.setSpec( \invert, BoolSpec(false) )
	.setSpec( \curve, [-16,16,\lin,0,0].asSpec )
	.setSpec( \bus, PositiveIntegerSpec(0,0,32) )
	.mappedArgs_([ \rangeOut ])
	.category_(\input)
)
