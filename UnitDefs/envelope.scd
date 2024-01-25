/*
\envelope_amp

An amplitude envelope with audio-rate precision.

env: an Env. It should have levels between 0 and 1, which will be mapped with according to the \amp ControlSpec.
timeScale: time scale multiplier

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

MultiChannelUdef( \envelope_amp, {
	var in;
	in = UIn.ar(0);
	in = in * UEnvGen.ar( \env, \amp, \timeScale );
	UOut.ar( 0, in );
})
	.category_( \utility )