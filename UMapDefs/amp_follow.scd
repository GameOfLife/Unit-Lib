/*
\amp_follow

Creates an UMap that listens to an audio bus from a previous unit in the chain, and follow it's amplitude.

fromRange: the amplitude range to be used as output
toRange: the range to which the output is scaled
attackTime: the attack time of the amplitude follower
releaseTime: the release time of the amplitude follower
invert: if true, the fromRange is inverted (i.e. low values become high and v.v.)
curve: a curvature value for toRange. If curve == 0, the range is linear.
clipMode: can be one of the following modes:
	0 - 'clip' (default): clip values outside fromRange
	1 - 'fold': fold values outside fromRange
	2 - 'wrap': wrap around values outside fromRange
	3 - 'none': no clipping applied (values can go outside toRange)
clipSoftness: softens the edges of 'clip' and 'fold' modes.

The 'toRange' arg range is mapped to that of the parameter to which the UMap is connected

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UMapDef( \amp_follow, { |filter = #[20,20000], fromRange = #[0.0, 1.0], toRange = #[0.0,1.0],
	attackTime = 0.01, releaseTime = 0.01,
	invert = 0, curve = 0, clipMode = 0, clipSoftness = 0.1|
	var sig, unranged;
	sig = UIn.ar(0);
	sig = BLowCut.ar( sig, filter[0], 2 );
	sig = BHiCut.ar( sig, filter[1], 2 );
	sig = Amplitude.ar( sig, attackTime, releaseTime );
	sig = A2K.kr( sig );
	fromRange[1] = fromRange[1].max(fromRange[0] + 1.0e-8);
	sig = sig.linlin(*fromRange ++ [0,1,\none]);
	sig = if( invert, 1-sig, sig );
	sig = Select.kr( curve.inRange(-0.001,0.001), [
		sig.lincurve(0,1,0,1, curve ),
		sig
	]);
	sig = Select.kr( clipMode, [
		sig.softclip2(1, clipSoftness),
		sig.softfold2(1, clipSoftness),
		sig.wrap2(1),
		sig
	]);
	sig = sig.linlin(0,1,*toRange ++ [ \none ]);
	UMapOut.kr(sig);
})
	.setSpec( \filter, \freq.asSpec.asRangeSpec )
	.setSpec( \fromRange, \amp.asSpec.asRangeSpec )
	.setSpec( \attackTime, [0,1,4].asSpec )
	.setSpec( \releaseTime, [0,1,4].asSpec )
	.setSpec( \clipMode, ListSpec( (..3), 0, [ \clip, \fold, \wrap, \none ] ) )
	.setSpec( \clipSoftness, [0,1,\lin,0,0.1].asSpec )
	.setSpec( \invert, BoolSpec(false) )
	.setSpec( \curve, [-16,16,\lin,0,0].asSpec )
	.prepareArgsFunc_( { |args|
		var bus;
		bus = args.pairsAt( \bus );
		args ++ [ \u_i_ar_0_bus, bus ];
	 } )
	.mappedArgs_([ \toRange ])
	.category_(\input)

