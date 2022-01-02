/*
\random_value

Creates an UMap that generates a new random value each time it is started. Typical use for this would be at the 'seed' parameter of any random-generator unit. This UMap can also be used on 'init' mode parameters.

range: the range in between which the value will be created
type: type of random.
	\white: normal "white" random
	\linear: linear distribution (low is most probable)
	\invlinear: inversed linear distribution (high is most probable)	\bilinear: bidirectional linear distribution (center is most probable)
	\mean: mean value of two random values
	\sum3: mean value of three random values - semi gaussian distribution
	\lp: lowest of two random values
	\hp: highest of two random values
value: the output value (can only be changed by the UMap itself)

The 'value' and 'range' arg ranges are mapped to that of the parameter to which the UMap is connected

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

FuncUMapDef( \random_value, { |unit, range = #[0.0,1.0], type = 'white'|
	switch( type,
		\white, { range[0] rrand: range[1]; },
		\linear, { 1.0.linrand.linlin(0,1,*range); },
		\invlinear, { 1.0.linrand.linlin(0,1,range[1], range[0]) },
		\bilinear, { 1.0.bilinrand.linlin(-1,1,*range); },
		\mean, { 2.collect({ range[0] rrand: range[1] }).mean },
		\sum3, { 1.0.sum3rand.linlin(-1,1,*range); },
		\lp, { 2.collect({ range[0] rrand: range[1] }).minItem },
		\hp, { 2.collect({ range[0] rrand: range[1] }).maxItem },
		{ range[0] rrand: range[1]; }
	);
})
	.mappedArgs_([\range])
	.setSpec( \type, ListSpec(['white', 'linear', 'invlinear', 'bilinear', 'mean', 'sum3', 'lp', 'hp' ]) )
	.category_( 'random' )