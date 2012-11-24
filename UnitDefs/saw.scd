/*
\saw

A band-limited sawtooth wave generator.
http://en.wikipedia.org/wiki/Sawtooth_wave

freq: frequency (Hz)
amp: amplitude (0-1)
lag: smoothing time for all parameters

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

Udef( \saw, { |freq = 440, amp = 0.1, lag = 0|
	UMixOut.ar( 0, Saw.ar( freq.lag( lag ), amp.lag( lag ) ), 0, true ) 
} ).category_( \oscillator )
	.setSpec( \lag, [0,1,\lin].asSpec );