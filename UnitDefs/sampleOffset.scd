/*
\sampleOffset

Creates a sample offset to enable a timing resolution of 1 sample instead of 128. Add after a generator unit.

* This Udef is here for testing purposes. If it proofs to work correctly this functionality will be built into all generator Udefs *

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

Udef( \sampleOffset, {
	var in;
	in = UIn.ar(0,1);
	in = USampleOffset.ar( in, \use.kr(1) );
	UOut.ar( 0, in );
})
	.setSpec( \use, BoolSpec( true ) )
	.category_( \utility );