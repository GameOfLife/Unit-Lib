/*
\integer

Creates an UMap for rounding values to whole numbers (Integer), on a linear scale.

input: the value

The 'input' arg ranges are mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

HybridUMapDef( \integer, { |input = 0.5|
		UMapDef.useMappedArgs = false;
		UMapOut.kr(input);
	}, { |unit, input = 0.5|
		input.clip( *[unit.spec.minval, unit.spec.maxval] );
	}, \convert
)
	.valueIsMapped_( false )
	.setSpec( \input, UAdaptSpec( { |spec|
		IntegerSpec( spec.default, spec.minval, spec.maxval );
	} ) )
	.canUseUMapFunc_({ |unit, key, umapdef|
		var spec;
		UMapDef.defaultCanUseUMapFunc.value( unit, key, umapdef ) && {
			spec = unit.getSpec( key );
			spec.respondsTo( \minval ) && { spec.respondsTo( \maxval ) };
		};
	})
	.mappedArgs_( [ \input ] );


