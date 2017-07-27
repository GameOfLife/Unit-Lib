/*
\tempo_time

This UMap is intended for use on the 'pattern' value of an UPattern. It allows the sustain value of the pattern to be set relative to the timeToNext.

legato: sustain time as multiplier of timeToNext (0-8)
timeToNext: time to next pattern event (in seconds)

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

FuncUMapDef( \legato_time, { |unit, legato = 1, timeToNext = 1|
	 [ legato * timeToNext, timeToNext ]
})
	.category_( 'upattern' )
	.valueIsMapped_( false )
	.mappedArgs_( [] )
	.setSpec( \legato, [0,10,\lin,0,1].asSpec )
	.setSpec( \timeToNext, SMPTESpec(0.01,3600) )
	.prepareArgsFunc_( { |args|
		args.replace( \division, \factor );
	} )
	.dontStoreValue_( true )
	.canUseUMapFunc_({ |unit, key, umapdef|
		unit.getSpec( key ).isKindOf( UPatternSpec );
	})