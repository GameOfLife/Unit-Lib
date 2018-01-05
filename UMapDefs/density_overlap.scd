/*
\density_overlap

This UMap is intended for use on the 'pattern' value of an UPattern. It creates new events based on density and overlap settings.

density: number of events per second
variation: amount of random variation on start times
overlap: amount of overlap in duration per event, as a factor of the average time between two events.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

FuncUMapDef( \density_overlap, { |unit, density = 10, variation = 0.0, overlap = 4|
	variation = (variation * 6).midiratio;
	[ overlap/density, 1.0.rand.linexp(0,1,1/variation/density,variation/density) ]
})
	.category_( 'upattern' )
	.valueIsMapped_( false )
	.mappedArgs_( [] )
	.setSpec( \density, [0.1,100,\exp,0,10].asSpec )
	.setSpec( \overlap, [0,16,((4/16).reciprocal-1).squared.log,0,1].asSpec )
	.dontStoreValue_( true )
	.canUseUMapFunc_({ |unit, key, umapdef|
		unit.getSpec( key ).isKindOf( UPatternSpec );
	})