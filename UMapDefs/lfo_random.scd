/*
\lfo_random

Creates an UMap with a low frequency random oscillator. This oscillator can create random values at random time intervals

time: the range of possible times between new values
lag: a lag time imposed on the output of the oscillator
seed: random seed (positive whole number). The same seed will always result in exactly the signal on any computer.
range: the output range

The 'range' arg is mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/


UMapDef( \lfo_random, { |time = #[0.1,0.1], lag = 0, seed = 12345, range = #[0.0,1.0]|
	var sig;
	URandSeed.ir(seed);
	sig = Duty.kr( Dwhite(0,1).linexp(0,1,*time), 0, Dwhite(0,1)).lag(lag).linlin(0,1,*range);
	UMapOut.kr(sig);
})
	.setSpec( \time, RangeSpec(0.001,100, 0, inf, \exp, 0, [0.1,0.1]) )
	.setSpec( \lag, [0,10,\lin].asSpec )
	.setSpec( \seed, PositiveIntegerSpec(12345), \init )
	.mappedArgs_( [ \range ] )
	.category_( 'modulation' )
