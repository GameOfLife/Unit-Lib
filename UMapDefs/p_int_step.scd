/*
\p_int_step

Creates an pattern UMap  New values are generated each time the chain is started, or in a UPattern for each of the generated events.

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

UPatDef( \p_int_step, { |unit, lo = 0, hi = 99, stepsize = 1, loop = \loop|
	{
		var current = 0, direction = 1, hist = [], arr, amb, lonext, hinext;
		if( loop.next == \backwards ) { current = 1; direction = -1 };
		current = lo.next.asInteger;
		current.asInteger.yield;
		inf.do { |i|
			switch( loop.next,
				\loop, {
					current = current + (direction * stepsize.next);
					current = current.wrap( lo.next, hi.next );
					direction = 1;
				},
				\backwards, {
					current = current + (direction * stepsize.next);
					current = current.wrap( lo.next, hi.next ); direction = -1; },
				\alternate, {
					current = current + (direction * stepsize.next);
					lonext = lo.next;
					hinext = hi.next;
					case { current > hinext } {
						current = current.fold(lonext,hinext);
						direction = -1;
					} { current < lonext } {
						current = current.fold(lonext,hinext);
						direction = 1;
					};
				},
				\rand, {
					arr = (lo.next..(hi.next));
					amb = arr.last - arr.first;
					hist.do({ |item| arr.remove( item ) });
					current = arr.choose;
					hist = hist.addFirst( current )[..stepsize.next.abs.min(amb) - 1];
				},
				\off, {
					current = current + (direction * stepsize.next);
					current = current.clip(lo.next,hi.next); direction = 1;
				}
			);
			current.asInteger.yield;
		};
	}.r;
})
.valueIsMapped_( false )
.setSpec( \stepsize, IntegerSpec() )
.setSpec( \lo, IntegerSpec() )
.setSpec( \hi, IntegerSpec() )
.canUseUMapFunc_({ |unit, key, umapdef|
	var spec;
	spec = unit.getSpec( key );
	[ IntegerSpec, UAdaptSpec ].any({ |class| spec.isKindOf( class ); }) or:
	(spec == URandSeed)
})
.setSpec( \loop, ListSpec( [ \off, \loop, \backwards, \alternate, \rand ], 1 ) )
.category_( 'pattern_series' );
