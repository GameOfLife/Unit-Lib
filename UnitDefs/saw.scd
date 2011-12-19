Udef( \saw, { |freq = 440, amp = 0.1|
	UMixOut.ar( 0, Saw.ar( freq, amp ), 0, true ) 
} ).category_( \synthesis )