/*
\lfNoise

A sample-and-hold noise generator. Generates random values at frequency rate.

freq: frequency (2-20000Hz)
amp: amplitude (0-1) of the noise
type: the type of noise:
	0: step or sample-and-hold noise; hard jumps at each value change
	1: linear interpolated noise
	2: cubic interpolated noise
	3: clip noise

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

Udef( \lfNoise, { |freq = 440, amp = 0.1, type = 0|
	var sig;
	URandSeed.ir();
	sig = [
		LFDNoise0.ar( freq, amp ),
		LFDNoise1.ar( freq, amp ),
		LFDNoise3.ar( freq, amp ),
		LFDClipNoise.ar( freq, amp ),
	];
	sig = SelectX.ar( type, sig );
	UMixOut.ar( 0, sig, 0, true )
} ).category_( \noise )
	.setSpec( \type, ListSpec( [0,1,2,3], 0, ['step', 'linear', 'cubic', 'clip'] ) )
	.setSpec( \freq, FreqSpec(2, 20000, default: 440) )