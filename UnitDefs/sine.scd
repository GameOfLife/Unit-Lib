Udef( \sine, { |freq = 440, amp = 0.1|
	UMixOut.ar( 0, SinOsc.ar( freq, 0, amp ), 0, true ) 
} ).category_( \synthesis )
	.setSpecMode( \amp, \normal )