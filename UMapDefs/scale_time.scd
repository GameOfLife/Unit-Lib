/*
\scale_time

Creates an UMap that scales a time to a certain factor. This UMap can also be used on 'init' mode parameters. It can only be used for time (SMPTESpec) parameters.

time: time to scale (in seconds)
factor: amount of scaling (lower values give smaller times)
add: amount of time to add after scaling

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

HybridUMapDef( \scale_time, { |time = 1, factor = 1, add = 0|
	UMapDef.useMappedArgs = false;
	UMapOut.kr( (time * factor) + add, false );
}, { |unit, time = 1, factor = 1, add = 0|
	 (time * factor) + add
})
	.category_( 'convert' )
	.valueIsMapped_( false )
	.setSpec( \time, SMPTESpec() )
	.setSpec( \factor, [0.1,10,\exp,0,1].asSpec )
	.setSpec( \add, SMPTESpec() )
	.canUseUMapFunc_({ |unit, key, umapdef|
		unit.getSpec( key ).isKindOf( SMPTESpec ) or: {
			unit.getSpec( key ).isKindOf( UAdaptSpec )
		}
	})