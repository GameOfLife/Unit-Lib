/*
\random_time

Creates an UMap that generates a new random time value each time it is started. This UMap can also be used on 'init' mode parameters. It can only be used for time (SMPTESpec) parameters.

min: minimum time
max: maximum time
value: the output value (can only be changed by the UMap itself)

The 'value' and 'range' arg ranges are mapped to that of the parameter to which the UMap is connected

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

FuncUMapDef( \random_time, { |unit, min = 0, max = 1|
	min rrand: max; // result sets \value arg
})
	.category_( 'random' )
	.valueIsMapped_( false )
	.mappedArgs_( [] )
	.setSpec( \min, SMPTESpec() )
	.setSpec( \max, SMPTESpec() )
	.setSpec( \value, DisplaySpec( SMPTESpec(), _.asSMPTEString(1000) ) )
	.canUseUMapFunc_({ |unit, key, umapdef|
		unit.getSpec( key ).isKindOf( SMPTESpec );
	})