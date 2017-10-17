/*
\p_envelope

Creates an pattern UMap with an envelope generator. New values are generated each time the chain is started, or in a UPattern for each of the generated events.

env: the envelope
loop: loop mode;
	\off: no looping (will stay at end value after completing)
	\loop: (default) wrap around between lo and hi
	\alternate: move back and forth between lo and hi

The 'env' arg ranges is mapped to that of the parameter to which the UMap is connected

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UPatDef( \p_envelope, { |unit, env, loop = \loop|
	{
		var current = UPattern.startPos, direction = 1, delta;
		var timer = UPattern.deltaTimer;
		var envx;
		inf.do { |i|
			envx = env.next;
			switch( loop.next,
				\loop, { current = current.wrap( 0.0, env.duration );  direction = 1 },
				\alternate, { 
					case { current > 1 } {
						current = current.fold(0.0,env.duration);
						direction = -1;
					} { current < 0 } {
						current = current.fold(0.0,env.duration);
						direction = 1;
					};
				},
				\off, { current = current.clip(0.0,env.duration); }
			);
			envx[ current ].yield;
			current = (current + ((direction * timer.value).clip2( 1.0e12 )));
		};
	}.r;
}).mappedArgs_([\env])
.setSpec( \env, UEnvSpec( EnvM() ) )
.setDefault( \env, EnvM() )
.setSpec( \loop, ListSpec( [ \off, \loop, \backwards, \alternate ], 1 ) )
.category_( 'pattern' );
