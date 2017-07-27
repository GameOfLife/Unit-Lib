/*
\sustain_time

This is the standard UMap for UPattern. The UMap passes on the sustain and timeToNext values to the UPattern engine.

sustain: sustain of pattern event (in seconds)
timeToNext: time to next pattern event (in seconds)

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

FuncUMapDef( \sustain_time, { |unit, sustain = 1, timeToNext = 1|
	 [ sustain, timeToNext ]
})
	.category_( 'upattern' )
	.valueIsMapped_( false )
	.setSpec( \sustain, SMPTESpec(0,3600) )
	.setSpec( \timeToNext, SMPTESpec(0.01,3600) )
	.prepareArgsFunc_( { |args|
		args.replace( \division, \factor );
	} )
	.dontStoreValue_( true )
	.canUseUMapFunc_({ |unit, key, umapdef|
		unit.getSpec( key ).isKindOf( UPatternSpec );
	})