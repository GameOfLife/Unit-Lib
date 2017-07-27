/*
\p_time_humanize

Creates an pattern UMap intended to use on the 'timeToNext' value of UPatterns. It can add a random factor to start times of events. The UMap will internally track the original time, so that it doesn't drift away.

time: value or UMap to quantize
amount: amount of deviation from the original start time (0-1 in seconds)

The 'time' arg ranges is mapped to that of the parameter to which the UMap is connected

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UPatDef( \p_time_humanize, { |unit, time = 1, amount = 0|
	{
		var logical = 0, actual = 0, qtime = 0;
		var ttime, aamt, random;
		loop {
			ttime = time.next;
			logical = logical + ttime;
			aamt = amount.next;
			random = aamt.rand;
			qtime = (logical + (random - ((aamt/2).min(ttime) ))) - actual;
			if( qtime <= 0 ) { qtime = random / 2 };
			actual = actual + qtime;
			qtime.yield;
		};
	}.r;
})
	.setSpec( \time, UAdaptSpec() )
	.setSpec( \amount, [0.0,1,9.squared.log,0,0].asSpec )
	.canUseUMapFunc_({ |unit, key, umapdef|
		key == \timeToNext or: {
			unit.getSpec( key ).isKindOf( SMPTESpec ) or: {
				unit.getSpec( key ).isKindOf( UAdaptSpec )
			}
		}
	})
	.useMappedArgs_( false )
	.mappedArgs_([ \time ]).category_( 'pattern_time' );
