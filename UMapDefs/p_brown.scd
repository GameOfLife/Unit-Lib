/*
\p_brown

Creates an pattern UMap for brownian noise. New values are generated each time the chain is started, or in a UPattern for each of the generated events.

lo: minimum value of the noise
hi: maximum value of the noise
step: step size (0-1) as multiplier of the total parameter range

The 'lo' and 'hi' arg ranges are mapped to that of the parameter to which the UMap is connected

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UPatDef( \p_brown, { |unit, lo = 0.0, hi = 1.0, step = 0.125|
	Pbrown( lo, hi, step, inf ).asStream;
}).mappedArgs_([\lo, \hi ]).category_( 'pattern' );