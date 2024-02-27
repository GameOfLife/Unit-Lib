/*
\p_stutter

Creates an pattern UMap for repeating values. The output values of a connected (pattern) UMap is repeated n times until the next value is called.

input: value or UMap to repeat
n: number of repetitions

The 'input' arg ranges is mapped to that of the parameter to which the UMap is connected

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UPatDef( \p_stutter, { |unit, input = 0, n = 1|
	Pdup( n, input ).asStream;
})
	.setSpec( \input, UAdaptSpec() )
	.setSpec( \n, IntegerSpec(1, 1,((2**31) - 1).asInteger) )
	.canUseUMapFunc_({ |unit, key, umapdef|
		unit.isKindOf( UPattern ).not && {
			[ Point, SimpleNumber, Array, Symbol, Boolean ].any({ |class|
				unit.getDefault( key ).isKindOf( class )
			});
		};
	})
	.useMappedArgs_( false )
	.mappedArgs_([ \input ]).category_( 'pattern_filter' );