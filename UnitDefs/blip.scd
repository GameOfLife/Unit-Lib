/*
\blip

Band Limited ImPulse generator. All harmonics have equal amplitude. This is the equivalent of 'buzz' in MusicN languages.

freq: frequency (Hz)
numharm: number of harmonics (1-...)
amp: amplitude (0-1)

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

Udef( \blip, { |freq = 440, numharm = 200, amp = 0.1|
	UMixOut.ar( 0, Blip.ar( freq, numharm, amp ), 0, true )
} ).category_( \oscillator )
	.setSpec( \freq, FreqSpec( 2, 20000 ) )
	.setSpec( \numharm, PositiveIntegerSpec(200,1, 1000) )
	.setSpecMode( \amp, \normal );