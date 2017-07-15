/*
\p_sine

Creates an pattern UMap for sine wave. New values are generated each time the chain is started, or in a UPattern for each of the generated events.

freq: the frequency of the sine wave
phase: (-pi - pi) the start phase of the sine wave (can be modulated)
min: lowest output value
max: highest output value


--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UPatDef( \p_sine, { |unit,
		freq = 0.1, 
		phase = 0,
		min = 0.0,
		max = 1.0,
		timeBase = \seconds|
	{	
		var i = 0;
		var timer = UPattern.deltaTimer;
		loop {
			(((i % 1) * 2pi) + phase.next).sin.linlin(-1,1,min.next,max.next).yield;
			switch( timeBase.next, 
				\events, { i = i + freq.next; }, 
				\seconds, { i = i + (freq.next * timer.value); },
			);
		};
	}.r
}).category_( 'pattern' )
	.mappedArgs_( [ \min, \max ] )
	.setSpec( \phase, AngleSpec() )
	.setSpec( \freq, FreqSpec(0.01,300, default: 2) )
	.setSpec( \timeBase, ListSpec( [ 'events', 'seconds' ] ) );