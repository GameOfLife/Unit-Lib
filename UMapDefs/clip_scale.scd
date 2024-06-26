/*
\clip_scale

Creates an UMap for scaling a value to another range, with clipping and wrapping functionality.

input: the value upon which the scaling is applied (this can be another UMap as well)
fromRange: the used range of the input value
toRange: the range to map it to
invert: if true, the input range is inverted (i.e. low values become high and v.v.)
curve: a curvature value for the mapped range. If curve == 0, the range is linear.
clipMode: can be one of the following modes:
	0 - 'clip' (default): clip values outside fromRange
	1 - 'fold': fold values outside fromRange
	2 - 'wrap': wrap around values outside fromRange
	3 - 'none': no clipping applied (values can go outside toRange)
clipSoftness: only used for 'clip' and 'fold' (0-1).

The input, fromRange and toRange arg ranges are mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/


HybridUMapDef( \clip_scale, { |input = 0.0, fromRange = #[0.0, 1.0], toRange = #[0.0, 1.0], curve = 0.0, invert = 0, clipMode = 0, clipSoftness = 0.1|
	var sig;
	sig = input;
	fromRange[1] = fromRange[1].max(fromRange[0] + 1.0e-8);
	sig = sig.linlin(*fromRange ++ [-1,1,\none]);
	sig = Select.kr( curve.inRange(-0.001,0.001), [
		sig.lincurve(-1,1,-1,1, curve, \none ),
		sig
	]);
	sig = if( invert, sig.neg, sig );
	sig = Select.kr( clipMode, [
		sig.softclip2(1, clipSoftness),
		sig.softfold2(1, clipSoftness),
		Select.kr( clipSoftness > 0, [ sig.wrap2(1), sig.softwrap2(1, clipSoftness) ] ),
		sig
	]);
	sig = sig.linlin(-1,1,*toRange ++ [ \none ]);
	UMapOut.kr(sig);
}, { |unit, input = 0.0, fromRange = #[0.0, 1.0], toRange = #[0.0, 1.0], curve = 0.0, invert = 0, clipMode = 0, clipSoftness = 0.1|
	var sig;
	sig = input;
	fromRange[1] = fromRange[1].max(fromRange[0] + 1.0e-8);
	sig = sig.linlin(*fromRange ++ [-1,1,\none]);
	sig = sig.lincurve(-1,1,-1,1, curve, \none );
	sig = if( invert, sig.neg, sig );
	sig = switch( clipMode.asInteger,
		0, { sig.softclip2(1, clipSoftness) },
		1, { sig.softfold2(1, clipSoftness) },
		2, { if( clipSoftness > 0 ) { sig.wrap2(1) } { sig.softwrap2(1, clipSoftness) } },
		{ sig }
	);
	sig.linlin(-1,1,*toRange ++ [ \none ]);
})
	.mappedArgs_([ \input, \fromRange, \toRange ])
	.prepareArgsFunc_({ |args|
		var index;
		index = args.indexOf( \value );
		if( index.notNil && { index.even }) {
			args[index] = \input;
		};
		args;
	})
	.setSpec( \clipMode, ListSpec( (..3), 0, [ \clip, \fold, \wrap, \none ] ) )
	.setSpec( \clipSoftness, [0,1,\lin,0,0.1].asSpec )
	.setSpec( \curve, [-16,16,\lin,0,0].asSpec )
	.setSpec( \invert, BoolSpec(false) )
	.category_( 'convert' )