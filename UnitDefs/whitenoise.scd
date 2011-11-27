Udef( \whiteNoise, { |amp = 0.1, seed = 12345|
	RandID.ir( IRand(0,63) ); // not full-proof yet
	RandSeed.ir(1,seed);
	UOut.ar( 0, WhiteNoise.ar * amp, true )
} ).category_( \synthesis )
	.setSpec( \seed, PositiveIntegerSpec(12345) )