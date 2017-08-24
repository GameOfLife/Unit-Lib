/*
\p_choose_8

Creates an pattern UMap that chooses between 8 values, with an optional exclusion history.

value0..value7: values to choose from
histSize: size of history buffer; the amount of steps before a value can repeat itself.
seed: random seed

The 'value0'..'value7' arg ranges are mapped to that of the parameter to which the UMap is connected

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UPatDef( \p_choose_8, { |unit, 
	value0 = 0.5, value1 = 0.5, value2 = 0.5, value3 = 0.5,
	value4 = 0.5, value5 = 0.5, value6 = 0.5, value7 = 0.5,
	histSize = 0, seed
	|
	{
		var index, size, arr;
		thisThread.randSeed = seed.next.asControlInput;
		arr = (..7).scramble;
		loop {
			index = arr.removeAt( histSize.next.asInt rrand: 7 );
			arr = arr.addFirst( index );
			[
				value0, value1, value2, value3, 
				value4, value5, value6, value7, 
			].at( index ).next.yield;
		};
	}.r;
}).mappedArgs_([ \value0, \value1, \value2, \value3, \value4, \value5, \value6, \value7 ])
.useMappedArgs_( false )
.setSpec( \value0, UAdaptSpec() )
.setSpecMode( \value0, \sync )
.setSpec( \value1, UAdaptSpec() )
.setSpec( \value2, UAdaptSpec() )
.setSpec( \value3, UAdaptSpec() )
.setSpec( \value4, UAdaptSpec() )
.setSpec( \value5, UAdaptSpec() )
.setSpec( \value6, UAdaptSpec() )
.setSpec( \value7, UAdaptSpec() )
.setSpec( \histSize, IntegerSpec(0,0,7) )
.canUseUMapFunc_({ |unit, key, umapdef| unit.isKindOf( UPattern ).not; })
.setSpec( \seed, URandSeed )
.setDefault( \seed, URandSeed() )
.category_( 'pattern_random' );
