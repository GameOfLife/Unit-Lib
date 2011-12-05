Udef(\blip,{ |rate = 1, freq = 100, noiseLevel = 0.125, blipLevel = 1|
	var out, env;	
	RandSeed.ir( 12345 ); // always the same noise
	out = ( Blip.ar( freq, 100, blipLevel * 0.125) + 
		PinkNoise.ar( noiseLevel )) * 
		LFPulse.kr(10 * rate); 
	UMixOut.ar(0, out, 0, true )
}).category_( \synthesis )