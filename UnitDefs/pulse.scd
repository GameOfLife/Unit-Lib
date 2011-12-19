Udef( \pulse, { |freq = 440, width = 0.5, amp = 0.1|
	UMixOut.ar( 0, Pulse.ar( freq, width, amp ), 0, true ) 
} ).category_( \synthesis )