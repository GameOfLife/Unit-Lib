/*
\envelope

An amplitude envelope.

env: an Env. It should have levels between 0 and 1, which will be mapped with according to the \amp ControlSpec.

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

Udef( \envelope, { 
	var in;
	in = UIn.ar(0);
	in = in * UEnvGen.ar( \env, \amp );
	UMixOut.ar( 0, in );
})
	.category_( \dynamics )