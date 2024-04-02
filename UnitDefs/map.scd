/*
\map

Maps a value between 0 and 1 to a specific range, with user-settable warp (linear, exponential etc.). The warp setting overrides that of the input spec. It can also be used to covert time or integers to a specific range for modulation.

input: the value to be mapped (0-1)
min: minimum map output (value == 0)
max: maxmum map output (value == 1)
warp: warp type
	-1: auto (default - warp type of input Spec)
	0: \step
	1: \lin
	2: \exp
	3: \cos
	4: \sin
	5: \curve
	6: \amp
curve: the curve amount when warp is set to \curve.

The 'min' and 'max' arg ranges are mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

HybridUMapDef( \map, { |input = 0.5, min = 0, max = 1, warp = -1, curve = 0|
	var spec;
	var clip = [(2 ** 24).neg+1, 2**24];
	UMapDef.useMappedArgs = false;
	spec = \u_spec.kr([0,1,1,-2,0]);
	min = min.clip(*clip);
	max = max.clip(*clip);
	warp = if( warp < 0, spec[2], warp );
	input = [ min, max, warp, curve, spec.last ].asSpecMapKr( input );
	UMapOut.kr(input);
}, { |unit, input = 0.5, min = 0, max = 1, warp = -1, curve = 0|
	var spec;
	var clip = [(2 ** 24).neg+1, 2**24];
	spec = unit.at( \u_spec ) ?? { [0,1,1,-2,0].asSpec };
	min = min.clip(*clip);
	max = max.clip(*clip);
	warp = if( warp < 0, { spec.asControlSpec.warp.asSpecifier }, { warp });
	if( warp.isKindOf( CurveWarp ) or: { warp == 5 }) { warp = curve };
	ControlSpec( min, max, warp, spec.asControlSpec.step ).map( input );
})
	.mappedArgs_( [ \min, \max, \warp ] )
	.insertArgName_( \input )
	.prepareArgsFunc_({ |args|
		var index;
		index = args.indexOf( \value );
		if( index.notNil && { index.even }) {
			args[index] = \input;
		};
		args;
	})
	.setSpec( \min, UAdaptSpec() )
	.setSpec( \max, UAdaptSpec() )
	.setSpec( \warp, UAdaptSpec({ |sp|
		var warp = \lin;
		if( sp.respondsTo( \warp ) ) {
			warp = sp.warp.asSpecifier;
		};
		ListSpec([-1,0,1,2,3,4,5,6],-1,[
			"auto (%)".format( warp ).asSymbol,
			\step,\lin,\exp,\cos,\sin,\curve,\amp
		])
	})
	)
	.setSpec( \curve, [-16,16,\lin,0,0] )
	.category_( 'map' )


