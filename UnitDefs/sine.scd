/*
\sine

A sine wave oscillator.
http://en.wikipedia.org/wiki/Sine_wave

freq: frequency (Hz)
phase: phase (-pi - pi)
amp: amplitude (0-1)

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

Udef( \sine, { |freq = 440, phase = 0, amp = 0.1|
	UMixOut.ar( 0, SinOsc.ar( freq, phase, amp ), 0, true ) 
} ).category_( \oscillator )
	.setSpec( \freq, FreqSpec( 2, 20000 ) )
	.setSpec( \phase, AngleSpec() )
	.setSpecMode( \amp, \normal );