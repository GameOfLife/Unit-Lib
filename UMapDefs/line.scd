/*
\line

An line generator with a fixed duration.

a: start value
b: end value
curve: a curve value for the line. A 0 (zero) curve creates a linear line, a positive curve value makes the line tend towards the start value, and v.v.
duration: duration of the line in seconds
delay: delay time before the line starts

The 'range' arg range is mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UMapDef( \line, { |a = 0.0, b = 1.0, curve = 0, duration = 10, delay = 0|
	var sig;
	sig = UEnvGen.kr( Env([0,0,1],[delay, duration],\lin) );
	sig = Select.kr( curve.inRange(-0.001,0.001), [
		sig.lincurve(0, 1, a, b, curve ),
		sig.linlin(0, 1, a, b )
	]);
	UMapOut.kr(sig);
})
	.setSpec( \curve, [-16,16,\lin,0,0].asSpec )
	.setSpec( \duration, SMPTESpec() )
	.setSpec( \delay, SMPTESpec() )
	.category_( 'automation' )
	.mappedArgs_( [ \a, \b ] );