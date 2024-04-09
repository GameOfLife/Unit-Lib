/*
\in_range

This UMap can switch between two values, depending on an incoming value. If the value is in between the lower and higher value of the range, the result is 'true', otherwise it is 'false'. All parameters can be UMaps.

input: the value to test (0-1)
range: the range (explanation above)
trueValue: the value (or UMap) to output when the result is true
falseValue: the value (or UMap) to output when the result is false

trueValue and falseValue arg ranges are mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

HybridUMapDef( \in_range, { |input = 0.0, range = #[0.25,0.75], trueValue = 1.0, falseValue = 0.0|
	var index, output;
	UMapDef.useMappedArgs_( false );
	index = InRange.kr( input, *range );
	output = Select.kr( index, [ falseValue, trueValue ] );
	UMapOut.kr( output );
}, { |unit, input = 0.0, range = #[0.25,0.75], trueValue = 1.0, falseValue = 0.0|
	if( input.inclusivelyBetween(*range ) ) { trueValue } { falseValue };
})
.setSpec( \trueValue, UAdaptSpec() )
.setSpec( \falseValue, UAdaptSpec() )
.mappedArgs_([ \trueValue, \falseValue ])
.category_( 'detection' )