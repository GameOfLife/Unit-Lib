(
UMapDef( \amp_follow, { |bus = 0, rangeIn = #[0.0, 1.0], rangeOut = #[0.0,1.0], 
	attackTime = 0.01, releaseTime = 0.01,
	invert = 0, curve = 0, clipMode = 0|
	var sig, unranged;
	sig = In.ar(bus + UIn.firstPrivateBus);
	sig = Amplitude.kr( sig, attackTime, releaseTime );
	sig = sig.linlin(*rangeIn ++ [0,1,\none]);
	sig = if( invert, 1-sig, sig );
	sig = Select.kr( curve.inRange(-0.001,0.001), [
		sig.lincurve(0,1,0,1, curve ),
		sig
	]);
	unranged = sig;
	sig = sig.linlin(0,1,*rangeOut ++ [ \none ]);
	sig = Select.kr( clipMode, [
		sig.clip(*rangeOut),
		sig.fold(*rangeOut),
		sig.wrap(*rangeOut),
		unranged.softclip.linlin( 0,1,*rangeOut),
		sig
	]);
	UMapOut.kr(sig);
})
	.setSpec( \rangeIn, \amp.asSpec.asRangeSpec )
	.setSpec( \attackTime, [0,1,4].asSpec )
	.setSpec( \releaseTime, [0,1,4].asSpec )
	.setSpec( \clipMode, ListSpec( (..4), 0, [ \clip, \fold, \wrap, \softclip, \none ] ) )
	.setSpec( \invert, BoolSpec(false) )
	.setSpec( \curve, [-16,16,\lin,0,0].asSpec )
	.setSpec( \bus, PositiveIntegerSpec(0,0,32) )
	.mappedArgs_([ \rangeOut ])
	.category_(\input)
)
