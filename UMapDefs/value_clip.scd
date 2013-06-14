UMapDef( \value_clip, { |value = 0.0, range = #[0.0, 1.0], curve = 0.0, invert = 0, clipMode = 0|
	var sig;
	sig = value;
	sig = sig.linlin(*range ++ [-1,1,\none]);
	sig = Select.kr( curve.inRange(-0.001,0.001), [
		sig.lincurve(-1,1,-1,1, curve, \none ),
		sig
	]);
	sig = if( invert, sig.neg, sig );
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
	sig = sig.linlin(-1,1,*range ++ [ \none ]);
	UMapOut.kr(sig);
})
	.mappedArgs_([ \value, \range ])
	.setSpec( \clipMode, ListSpec( (..7), 0, [ 
		\clip, \fold, \wrap, \tanh, \softclip, \distort, \wrap_sine, \none 
		] ) 
	)
	.setSpec( \curve, [-16,16,\lin,0,0].asSpec )
	.setSpec( \invert, BoolSpec(false) )
	.category_( 'filter' )