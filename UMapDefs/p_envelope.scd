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
		var current = 0, direction = 1, delta;
		var lastTime = UPattern.seconds;
		var envx;
		if( loop.next === \backwards ) { direction = -1; };
		envx = env.next;
		envx.at( 0 ).yield;
		inf.do { |i|
			delta = (UPattern.seconds( true ) - lastTime);
			current = (current + ((direction * delta).clip2( 1.0e12 )));
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
			lastTime = UPattern.seconds;
			envx[ current ].yield;
		};
	}.r;
}).mappedArgs_([\env])
.setSpec( \env, UEnvSpec( EnvM() ) )
.setDefault( \env, EnvM() )
.setSpec( \loop, ListSpec( [ \off, \loop, \backwards, \alternate ], 1 ) )
.category_( 'pattern' );
