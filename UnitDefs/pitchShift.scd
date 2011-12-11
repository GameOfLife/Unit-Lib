Udef( \pitchShift, { |semitones = 0, pitchDisp = 0.0, timeDisp = 0.0|
	var sig;
	RandSeed.ir(1, 12345);
	sig = UIn.ar( 0, 1 );
	sig = PitchShift.ar( sig, 0.2, semitones.midiratio, pitchDisp * 4, timeDisp * 0.2 );
	UOut.ar(0, sig );
})	
	.category_( \effect )
	.setSpec( \semitones, [ -24, 24, \lin, 0.01, 0 ].asSpec )