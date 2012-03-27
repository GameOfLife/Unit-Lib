Udef( \brownNoise, { |amp = 0.1, seed = 12345|
	URandSeed.ir(seed);
	UMixOut.ar( 0, BrownNoise.ar * amp, 0, true )
} ).category_( \synthesis )
	.setSpec( \seed, PositiveIntegerSpec(12345) )