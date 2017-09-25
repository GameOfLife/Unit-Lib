/*
\p_coin

Creates an pattern UMap that chooses randomly between two values.

a: first value
b: second value
prob: probability for second value

The 'a' and 'b' arg ranges are mapped to that of the parameter to which the UMap is connected

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UPatDef( \p_coin, { |unit, a = 0.0, b = 1.0, prob = 0.5|
	{
		inf.do { |i|
			
			[a,b][ prob.next.coin.binaryValue ].next.yield;
		};
	}.r;
}).mappedArgs_([\a, \b])
.useMappedArgs_( false )
.canUseUMapFunc_({ |unit, key, umapdef| unit.isKindOf( UPattern ).not })
.setSpec( \a, UAdaptSpec() )
.setSpec( \b, UAdaptSpec() )
.category_( 'pattern_random' );
