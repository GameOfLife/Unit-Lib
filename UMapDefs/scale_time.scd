/*
\scale_time

Creates an UMap that scales a time to a certain factor. This UMap can also be used on 'init' mode parameters. It can only be used for time (SMPTESpec) parameters.

time: number or fraction of beats (0.125-8)
scale: beats per minute

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

HybridUMapDef( \scale_time, { |time = 1, factor = 1|
	UMapDef.useMappedArgs = false; 
	UMapOut.kr( time * factor, false );
}, { |unit, time = 1, factor = 1|
	 time * factor
})
	.category_( 'map' )
	.valueIsMapped_( false )
	.setSpec( \time, SMPTESpec() )
	.setSpec( \factor, [0.125,8,\exp,0,1].asSpec )
	.canUseUMapFunc_({ |unit, key, umapdef|
		unit.getSpec( key ).isKindOf( SMPTESpec ) or: {
			unit.getSpec( key ).isKindOf( UAdaptSpec )
		}
	})