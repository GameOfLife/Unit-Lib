/*
\p_time_quantize

Creates an pattern UMap intended to use on the 'timeToNext' value of UPatterns. It is capable of quantizing incoming time values to a grid based on tempo and note division. The quantization grid is absolute, based on the starting time of the UPattern. That means that if two patterns with a 'p_time_quantize' are started at the exact same time, they will always quantize together when their settings are the same, even if tempo and/or factor are modulated over time.

time: value or UMap to quantize
factor: number or fraction of beats to quantize to (0.125-8)
tempo: beats per minute

The 'input' arg ranges is mapped to that of the parameter to which the UMap is connected

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UPatDef( \p_time_quantize, { |unit, time = 1, factor = 1, tempo = 60|
	{
		var logical = 0, actual = 0, qtime = 0;
		var ttime, round;
		loop {
			ttime = time.next;
			logical = logical + ttime;
			round = (factor.next / tempo.next) * 60;
			qtime = logical.round( round ) - actual;
			actual = actual + qtime;
			qtime.yield;
		};
	}.r;
})
	.setSpec( \time, UAdaptSpec() )
	.setSpec( \factor, [0.125,8,\exp].asSpec )
	.setSpec( \tempo, [10,300,\exp,0,60].asSpec )
	.canUseUMapFunc_({ |unit, key, umapdef|
		unit.getSpec( key ).isKindOf( SMPTESpec );
	})
	.useMappedArgs_( false )
	.mappedArgs_([ \time ]).category_( 'pattern_time' );
