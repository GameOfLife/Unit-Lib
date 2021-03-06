/*
\dust

Creates an UMap for producing random single-frame impulses.

density: average number of impulses per second
range: the output range

The 'range' arg range is mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UMapDef( \dust, { |density = 20, range = #[0.0,1.0]|
	var sig;
	URandSeed.ir();
	sig = Dust.kr( density ).range(*range);
	UMapOut.kr(sig);
})
	.setSpec( \density, [0.01,1000,\exp, 0, 20].asSpec )
	.mappedArgs_( [ \range ] )
	.category_( 'trigger' )
