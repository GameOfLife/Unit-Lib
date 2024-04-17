/*
\p_step

Creates an pattern UMap making linear steps between two values. New values are generated each time the chain is started, or in a UPattern for each of the generated events.

lo: the start value
hi: the end value
step: number of steps between lo and hi (negative values produce downward steps)
loop: loop mode;
	\off: no looping (will stay at end value after completing)
	\loop: (default) wrap around between lo and hi
	\alternate: move back and forth between lo and hi

The 'lo' and 'hi' arg ranges are mapped to that of the parameter to which the UMap is connected

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UPatDef( \p_step, { |unit, lo = 0.0, hi = 1.0, steps = 12, loop = \loop|
	{
		var current = 0, direction = 1;
		if( loop.next == \backwards ) { current = 1; direction = -1 };
		current.linlin( 0, 1, lo.next, hi.next ).yield;
		inf.do { |i|
			current = (current + ((direction/steps.next).clip2( 1.0e12 )));
			switch( loop.next,
				\loop, { current = current.wrap( 0.0, 1.0 ); direction = 1; },
				\backwards, { current = current.wrap( 0.0, 1.0 ); direction = -1; },
				\alternate, {
					case { current > 1 } {
						current = current.fold(0.0,1.0);
						direction = -1;
					} { current < 0 } {
						current = current.fold(0.0,1.0);
						direction = 1;
					};
				},
				\off, { current = current.clip(0.0,1.0); direction = 1; }
			);
			current.linlin( 0, 1, lo.next, hi.next ).yield;
		};
	}.r;
}).mappedArgs_([\lo, \hi])
.setSpec( \steps, [-100,100,\lin].asSpec )
.setSpec( \loop, ListSpec( [ \off, \loop, \backwards, \alternate ], 1 ) )
.category_( 'pattern_series' );
