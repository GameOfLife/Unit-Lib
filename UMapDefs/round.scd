/*
\round

Creates an UMap for rounding off (quantizing) values. The output value will be the nearest multiple of the 'round' value.

input: the value to be rounded
round: the value to round to

The 'input' and 'round' arg ranges are mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

HybridUMapDef( \round, { |input = 0.5, round = 0|
		UMapDef.useMappedArgs = false;
		input = input.round( round );
		UMapOut.kr(input);
	}, { |unit, input = 0.5, round = 0|
		input.round( round ).clip( *[unit.spec.minval, unit.spec.maxval] );
	}, \operator
)
	.prepareArgsFunc_({ |args|
		var index;
		index = args.indexOf( \value );
		if( index.notNil && { index.even }) {
			args[index] = \input;
		};
		args;
	})
	.valueIsMapped_( false )
	.setSpec( \round, UAdaptSpec( { |spec|
		var center;
		spec = spec.copy;
		if( spec.isKindOf( FreqSpec ) ) {
			spec = ControlSpec.newFrom( spec );
		};
		if( spec.respondsTo( \warp ) ) {
			if( spec.warp.asSpecifier == \exp ) {
				center = spec.map( 0.5 );
				center = center / spec.maxval;
				spec.warp = (center.reciprocal-1).squared.log;
			};
			spec.warp = spec.warp.asSpecifier;
		};
		spec.minval = 0;
		spec;
	} ) )
	.setSpec( \input, UAdaptSpec() )
	.mappedArgs_( [ \input, \round ] );


