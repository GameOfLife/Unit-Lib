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
	\back_at_end: move back at the end of the current UPattern (depending on its duration)
	\hold: keep line at current value
delay: a delay time before the line starts

The 'a' and 'b' arg ranges are mapped to that of the parameter to which the UMap is connected

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UPatDef( \p_line, { |unit, a = 0.0, b = 1.0, curve = 0, time = 10, loop = \loop, delay = 0|
	{
		var current = 0, direction = 1, ttime, ddelay;
		var timer = UPattern.deltaTimer, now, endDur;
		current = UPattern.startPos;
		now = current;
		ddelay = delay.next;
		while { current < ddelay } {
			if( loop.next === \backwards ) {
				direction = -1;
				b.next.yield;
			} {
				direction = 1;
				a.next.yield;
			};
			current = current + timer.value;
			ddelay = delay.next;
		};
		current = current - ddelay;
		ttime = time.next;
		now = current;
		if( direction == -1 ) {
			current = ttime - current;
		};
		inf.do { |i|
			switch( loop.next,
				\loop, { current = current.wrap( 0.0, ttime );  direction = 1 },
				\backwards, { current = current.wrap( 0.0, ttime );  direction = -1; },
				\alternate, {
					case { current > ttime } {
						current = current.fold(0.0, ttime);
						direction = -1;
					} { current < 0 } {
						current = current.fold(0.0, ttime);
						direction = 1;
					};
				},
				\back_at_end, {
					current = current.clip(0.0, ttime);
					if( (UPattern.duration - (ttime + delay.next)) <= now ) {
						direction = -1;
					} {
						direction = 1;
					};

				},
				\hold, {
					current = current.clip(0.0, ttime);
					direction = 0;
				},
				\off, { current = current.clip(0.0, ttime); }
			);
			current.lincurve( 0, ttime, a.next, b.next, curve.next ).yield;
			#now, current = [now,current] + ([1,direction] * timer.value);
			ttime = time.next;
		};
	}.r;
}).mappedArgs_([\a, \b])
.useMappedArgs_( false )
.setSpec( \time, SMPTESpec() )
.setSpec( \a, UAdaptSpec() )
.setSpec( \b, UAdaptSpec() )
.setSpec( \curve, [-16,16,\lin,0,0].asSpec )
.setSpec( \delay, SMPTESpec() )
.canUseUMapFunc_({ |unit, key, umapdef|
	unit.isKindOf( UPattern ).not && {
		[ SimpleNumber, Array, Point ].any({ |class|
			unit.getDefault( key ).isKindOf( class )
		}) && {
			UAdaptSpec().canAdapt( unit.getSpec( key ) )
		};
	};
})
.setSpec( \loop, ListSpec( [ \off, \loop, \backwards, \alternate, \back_at_end, \hold ], 1 ) )
.category_( 'pattern_timebased' );
