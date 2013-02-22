/*
\impulse

A non-bandlimited impulse generator. Outputs single sample impulses.

freq: frequency (Hz)
scale: a multiplier for the frequency (if scale is 0, only a single impulse is produced)
amp: amplitude (0-1)

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

Udef( \impulse, { |freq = 440, scale = 1, amp = 0.1, lag = 0|
	UMixOut.ar( 0, Impulse.ar( freq.lag( lag ) * scale,  amp.lag( lag ) ), 0, true ) 
} ).category_( \oscillator )
	.setSpec( \freq, FreqSpec( 2, 20000 ) )
	.setSpec( \scale, [0,2,\lin,0.125,1].asSpec )
	.setSpec( \lag, [0,1,\lin].asSpec );