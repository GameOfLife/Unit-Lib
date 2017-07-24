/*
\p_time_step

Creates an pattern UMap intended to use on the 'timeToNext' or 'sustain' values of UPatterns. If n == 1, the pattern is transparent (i.e. directly returns input). For higher settings of n, a number of zeros (n-1) is returned before the next input value. When used on 'timeToNext' n translates into number of events started simultaneously (as in chords). When used on 'sustain', a setting of n > 1 will result in skipping n-1 events before actually playing the next (as sustain == zero means a silence/rest).

input: value or UMap to return after n-1 zeros
n: number of zeros plus one. Can be an UMap too.

The 'input' arg ranges is mapped to that of the parameter to which the UMap is connected

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UPatDef( \p_time_step, { |unit, input = 0, n = 1|
	{
		var nn;
		loop {
			nn = n.next;
			(nn-1).do({ 0.yield });
			input.next.yield;
		};
	}.r;
})
	.setSpec( \input, UAdaptSpec() )
	.setSpec( \n, IntegerSpec(1, 1, 99 ) )
	.canUseUMapFunc_({ |unit, key, umapdef|
		unit.getSpec( key ).isKindOf( SMPTESpec );
	})
	.useMappedArgs_( false )
	.mappedArgs_([ \input ]).category_( 'pattern_time' );
