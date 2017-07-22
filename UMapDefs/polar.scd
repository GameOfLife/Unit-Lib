/*
\polar

Creates an UMap intended for use on modulatable point parameters. It converts the point into a polar, with linear controls for rho and theta, which on their turn can be used to assign other UMaps to.

rho: the rho value (distance from 0@0)
theta: the theta value (angle from 0@0 )

The 'rho' arg range is mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UMapDef( \polar, { |rho = 0.5, theta = 0|
	var polar, x,y;
	polar = Polar( rho.linlin(0.5,1,0,1,\none), theta );
	x = polar.real.linlin(0,1,0.5,1,\none);
	y = polar.imag.linlin(0,1,0.5,1,\none);
	UMapOut.kr([x,y]);
})
	.setSpec( \theta, AngleSpec() )
	.uchainInitFunc_({ |unit, chain|
		var rho;
		rho = unit.rho;
		unit.def = \map_polar.asUdef( UMapDef );
		if( unit.rho.isKindOf( UMap ).not ) {
			if( rho.inclusivelyBetween(-10,10) ) {
				unit.rho = rho.linlin(-10,10,-1,1);
			} {
				unit.rho = rho.linlin(-200,200,-1,1);
				unit.radius = 200@200;
			};
		} {
			unit.radius = 200@200;
		};
	})
	.mappedArgs_( [ \rho ] )
	.category_( 'private' )
	.canUseUMapFunc_({ |unit, key, umapdef|
		unit.getSpec( key ).isKindOf( PointSpec );
	});
