/*
\p_line

Creates an pattern UMap making a line steps between two values over time. New values are generated each time the chain is started, or in a UPattern for each of the generated events.

a: the start value
b: the end value
time: time to it takes from a to b;
loop: loop mode;
	\off: no looping (will stay at end value after completing)
	\loop: (default) wrap around between lo and hi
	\backwards: loop backwards
	\alternate: move back and forth between lo and hi

The 'lo' and 'hi' arg ranges are mapped to that of the parameter to which the UMap is connected

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UPatDef( \p_line, { |unit, a = 0.0, b = 1.0, time = 10, loop = \loop|
	{
		var current = 0, direction = 1, delta;
		var lastTime = UPattern.seconds;
		if( loop.next === \backwards ) { direction = -1; };
		current.linlin( 0, 1, a.next, b.next ).yield;
		inf.do { |i|
			delta = (UPattern.seconds( true ) - lastTime);
			current = (current + ((direction * delta/time.next).clip2( 1.0e12 )));
			switch( loop.next,
				\loop, { current = current.wrap( 0.0, 1.0 );  direction = 1 },
				\backwards, { current = current.wrap( 0.0, 1.0 );  direction = -1; },
				\alternate, { 
					case { current > 1 } {
						current = current.fold(0.0,1.0);
						direction = -1;
					} { current < 0 } {
						current = current.fold(0.0,1.0);
						direction = 1;
					};
				},
				\off, { current = current.clip(0.0,1.0); }
			);
			lp = nil;
			lastTime = UPattern.seconds;
			current.linlin( 0, 1, a.next, b.next ).yield;
		};
	}.r;
}).mappedArgs_([\a, \b])
.setSpec( \time, SMPTESpec() )
.setSpec( \loop, ListSpec( [ \off, \loop, \backwards, \alternate ], 1 ) )
.category_( 'pattern' );
