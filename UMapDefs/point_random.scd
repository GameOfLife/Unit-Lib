UMapDef( \point_random, { |
		speed = 0.1, 
		center = #[0.5,0.5],
		radius = #[0.525,0.525],
		lag = 0,
		seed = 12345|
	var random;
	URandSeed.ir(seed);
	radius = radius.linlin(0,1,-1,1);
	center = center.linlin(0,1,-1,1);
	random = LFDNoise3.kr( speed.dup, radius, center );
	random = LPFLag.kr( random, lag );
	random = random.linlin(-1,1,0,1);
	UMapOut.kr( random );
})
	.category_( 'point' )
	.setSpec( \speed, [ 0, 100, 99.squared.log, 0, 0.1 ].asSpec )
	.setSpec( \center, PointSpec(Rect(0,0,1,1)) )
	.setSpec( \radius, PointSpec(Rect(0,0,1,1)) )
	.setSpec( \lag, [ 0, 1, \lin, 0, 0].asSpec )
	.setSpec( \seed, PositiveIntegerSpec(12345) )
	.mappedArgs_([ \radius, \center ]);