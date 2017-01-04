/*
\round

Creates an UMap for rounding off (quantizing) values. The output value will be the nearest multiple of the 'round' value.

value: the value to be rounded
round: the value to round to

The 'value' and 'round' arg ranges are mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

d = UMapDef( \round, { |value = 0.5, round = 0|
	UMapDef.useMappedArgs = false;
	value = value.round( round );
	UMapOut.kr(value);
})
	.mappedArgs_( [ \value, \round ] )
	.category_( 'operator' )


