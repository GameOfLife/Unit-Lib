/*
\p_wchoose_8

Creates an pattern UMap that chooses between 8 values based on a probability rate for each value.

value0..value7: values to choose from
weight0..weight8: weight of each value. If all weights are equal, probabilities are also equal

The 'value0'..'value7' arg ranges are mapped to that of the parameter to which the UMap is connected

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UPatDef( \p_wchoose_8, { |unit, 
	value0 = 0.5, value1 = 0.5, value2 = 0.5, value3 = 0.5,
	value4 = 0.5, value5 = 0.5, value6 = 0.5, value7 = 0.5, 
	weight0 = 1.0, weight1 = 1.0, weight2 = 1.0, weight3 = 1.0,
	weight4 = 1.0, weight5 = 1.0, weight6 = 1.0,  weight7 = 1.0|
	{
		loop {
			[
				value0, value1, value2, value3, 
				value4, value5, value6, value7, 
			].wchoose( [ 
					weight0.next, weight1.next, weight2.next, weight3.next, 
					weight4.next, weight5.next, weight6.next, weight7.next, 
				].normalizeSum 
			).next.yield;
		};
	}.r;
}).mappedArgs_([ \value0, \value1, \value2, \value3, \value4, \value5, \value6, \value7 ])
.useMappedArgs_( false )
.setSpec( \value0, UAdaptSpec() )
.setSpec( \value1, UAdaptSpec() )
.setSpec( \value2, UAdaptSpec() )
.setSpec( \value3, UAdaptSpec() )
.setSpec( \value4, UAdaptSpec() )
.setSpec( \value5, UAdaptSpec() )
.setSpec( \value6, UAdaptSpec() )
.setSpec( \value7, UAdaptSpec() )
.category_( 'pattern_choice' );
