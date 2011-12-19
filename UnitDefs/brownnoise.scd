Udef( \brownNoise, { |amp = 0.1, seed = 12345|
	RandID.ir( IRand(0,63) ); // not full-proof yet
	RandSeed.ir(1,seed);
	UMixOut.ar( 0, BrownNoise.ar * amp, 0, true )
} ).category_( \synthesis )
	.setSpec( \seed, PositiveIntegerSpec(12345) )