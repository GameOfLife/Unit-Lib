/*
\p_brown

Creates an pattern UMap for brownian noise. New values are generated each time the chain is started, or in a UPattern for each of the generated events.

lo: minimum value of the noise
hi: maximum value of the noise
step: step size (0-1) as multiplier of the range between lo and hi
seed: random seed for the noise

The 'lo' and 'hi' arg ranges are mapped to that of the parameter to which the UMap is connected

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UPatDef( \p_brown, { |unit, lo = 0.0, hi = 1.0, step = 0.125, seed|
	{
		var stream, curr, llo, hhi;
		thisThread.randSeed = seed.next.asControlInput;
		curr = lo.next rrand: hi.next;
		curr.yield;
		loop {
			llo = lo.next;
			hhi = hi.next;
			curr = (curr + (step.next.xrand2 * (hhi - llo))).fold( llo, hhi );
			curr.yield;
		};
	}.r;
}).mappedArgs_([\lo, \hi ])
.setSpec( \seed, URandSeed )
.setDefault( \seed, URandSeed() )
.category_( 'pattern_random' );