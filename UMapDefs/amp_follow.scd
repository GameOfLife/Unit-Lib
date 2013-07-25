/*
\amp_follow

Creates an UMap that listens to an audio bus from a previous unit in the chain, and follow it's amplitude.

bus: the number of the bus (usually 0)
fromRange: the amplitude range to be used as output
toRange: the range to which the output is scaled
attackTime: the attack time of the amplitude follower
releaseTime: the release time of the amplitude follower
invert: if true, the fromRange is inverted (i.e. low values become high and v.v.)
curve: a curvature value for toRange. If curve == 0, the range is linear.
clipMode: can be one of the following modes (Symbol):
	'clip' (default): clip values outside fromRange
	'fold': fold values outside fromRange
	'wrap': wrap around values outside fromRange
	'tanh': distort values to range with a tanh function
	'softclip': softclip values outside fromRange
	'distort': use a non-linear distortion curve to keep all values within range
	'wrap_sine': wrap values around fromRange, and then map them to a sine curve. This means that values at the borders of fromRange will return at the center of toRange.
	'none': no clipping applied (values can go outside toRange)

The 'toRange' arg range is mapped to that of the parameter to which the UMap is connected

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UMapDef( \amp_follow, { |bus = 0, fromRange = #[0.0, 1.0], toRange = #[0.0,1.0], 
	attackTime = 0.01, releaseTime = 0.01,
	invert = 0, curve = 0, clipMode = 0, clipSoftness = 0.1|
	var sig, unranged;
	sig = In.ar(bus + UIn.firstPrivateBus);
	sig = Amplitude.kr( sig, attackTime, releaseTime );
	sig = sig.linlin(*fromRange ++ [0,1,\none]);
	sig = if( invert, 1-sig, sig );
	sig = Select.kr( curve.inRange(-0.001,0.001), [
		sig.lincurve(0,1,0,1, curve ),
		sig
	]);
	sig = sig.linlin(0,1,*toRange ++ [ \none ]);
	sig = Select.kr( clipMode, [
		sig.softclip2(1, clipSoftness),
		sig.softfold2(1, clipSoftness),
		sig.wrap2(1),
		sig
	]);
	UMapOut.kr(sig);
})
	.setSpec( \fromRange, \amp.asSpec.asRangeSpec )
	.setSpec( \attackTime, [0,1,4].asSpec )
	.setSpec( \releaseTime, [0,1,4].asSpec )
	.setSpec( \clipMode, ListSpec( (..3), 0, [ \clip, \fold, \wrap, \none ] ) )
	.setSpec( \clipSoftness, [0,1,\lin,0,0.1].asSpec )
	.setSpec( \invert, BoolSpec(false) )
	.setSpec( \curve, [-16,16,\lin,0,0].asSpec )
	.setSpec( \bus, PositiveIntegerSpec(0,0,32) )
	.mappedArgs_([ \toRange ])
	.category_(\input)

