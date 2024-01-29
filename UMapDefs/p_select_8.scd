/*
\p_select_8

Creates an pattern UMap that can select a value out of 8.

index: index of the value (0-7)
value0..value7: values (can be pattern or function UMaps)

The 'value0'..'value7' arg ranges are mapped to that of the parameter to which the UMap is connected

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

var def, values;

values = 8.collect({ |i| ("value"++i).asSymbol });

def = UPatDef( \p_select_8, { |unit,
	index = 0,
	value0 = 0.5, value1 = 0.5, value2 = 0.5, value3 = 0.5,
	value4 = 0.5, value5 = 0.5, value6 = 0.5, value7 = 0.5
	|
	{
		inf.do { |i|
			[
				value0, value1, value2, value3,
				value4, value5, value6, value7
			].at( index.next.asInteger ).next.yield;
		};
	}.r;
}).mappedArgs_( values )
.useMappedArgs_( false )
.canUseUMapFunc_({ |unit, key, umapdef|
	unit.isKindOf( UPattern ).not && {
		[ Point, SimpleNumber, Array, Symbol ].any({ |class|
			unit.getDefault( key ).isKindOf( class )
		});
	};
})
.setSpec( \index, IntegerSpec(0,0,7) )
.category_( 'pattern_selection' );

values.do({ |val| def.setSpec( val, UAdaptSpec() ) });

def;




