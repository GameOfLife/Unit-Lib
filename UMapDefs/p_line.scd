/*
\p_line

Creates an pattern UMap making a line steps between two values over time. New values are generated each time the chain is started, or in a UPattern for each of the generated events.

a: the start value
b: the end value
curve: a curve value for the line. A 0 (zero) curve creates a linear line, a positive curve value makes the line tend towards the start value, and v.v.
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

UPatDef( \p_line, { |unit, a = 0.0, b = 1.0, curve = 0, time = 10, loop = \loop|
	{
		var current = 0, direction = 1;
		var timer = UPattern.deltaTimer;
		if( loop.next === \backwards ) { direction = -1; };
		current = (direction * (UPattern.startPos / time.next)).clip2( 1.0e12 );
		inf.do { |i|
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
			current.lincurve( 0, 1, a.next, b.next, curve.next ).yield;
			current = (current + ((direction * timer.value/time.next).clip2( 1.0e12 )));
		};
	}.r;
}).mappedArgs_([\a, \b])
.useMappedArgs_( false )
.setSpec( \time, SMPTESpec() )
.setSpec( \a, UAdaptSpec() )
.setSpec( \b, UAdaptSpec() )
.setSpec( \curve, [-16,16,\lin,0,0].asSpec )
.canUseUMapFunc_({ |unit, key, umapdef|
	unit.isKindOf( UPattern ).not && {
		[ SimpleNumber, Array, Point ].any({ |class|
			unit.getDefault( key ).isKindOf( class )
		});
	};
})
.setSpec( \loop, ListSpec( [ \off, \loop, \backwards, \alternate ], 1 ) )
.category_( 'pattern' );
