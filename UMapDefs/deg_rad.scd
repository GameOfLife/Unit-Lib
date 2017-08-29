/*
\deg_rad

Creates an UMap for converting radians to degrees, to be used on any UMap with an angle setting in radians (-pi to pi)

deg: amount of degrees (-180-180)

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

HybridUMapDef( \deg_rad, { |deg = 0|
	UMapDef.useMappedArgs = false; // use raw values instead of mapped (0-1)
	UMapOut.kr( deg * pi / 180 );
}, { |unit, deg = 0|
	 deg * pi / 180;
})
	.setSpec( \deg, [-180, 180, \lin, 0, 1 ] )
	.canUseUMapFunc_({ |unit, key, umapdef|
		unit.getSpec( key ).isKindOf( AngleSpec );
	})
	.category_( 'convert' )