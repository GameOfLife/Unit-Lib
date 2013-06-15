
UMapDef( \rho_theta, { |rho = 0.5, theta = 0|
	var polar, x,y;
	polar = Polar( rho.linlin(0.5,1,0,1,\none), theta );
	x = polar.real.linlin(0,1,0.5,1,\none);
	y = polar.imag.linlin(0,1,0.5,1,\none);
	UMapOut.kr([x,y]);
})
	.setSpec( \theta, AngleSpec() )
	.mappedArgs_( [ \rho ] )
	.category_( 'point' );
