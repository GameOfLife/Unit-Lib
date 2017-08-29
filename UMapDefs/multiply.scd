/*
\multiply

Creates an UMap for multiplication (*) of two values.
 
input: the value to multiply
multiply: the multiplier
negative: multiply with negative value
factor: exponent of 10 to multiply the multiplier by

The input arg range is mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/


HybridUMapDef( \multiply, { |input = 0.0, multiply = 1.0, negative = 0, factor = 0|
	var sig;
	UMapDef.useMappedArgs = false; // use raw values instead of mapped (0-1)
	sig = input * (multiply * negative.linlin(0,1,1,-1) * (10**factor));
	UMapOut.kr(sig);
}, { |unit, input = 0.0, multiply = 1.0, negative = 0, factor = 0|
	(input * (multiply * negative.binaryValue.linlin(0,1,1,-1) * (10**factor)))
		.clip( *[unit.spec.minval, unit.spec.maxval] );
})
	.mappedArgs_([ \input ])
	.setSpec( \multiply, [0,2,\lin,0,1].asSpec )
		.prepareArgsFunc_({ |args|
		var index;
		index = args.indexOf( \value );
		if( index.notNil && { index.even }) {
			args[index] = \input;
		};
		args;
	})
	.setSpec( \negative, BoolSpec(false) )
	.setSpec( \factor, ListSpec( [-2,-1,0,1,2,3,4,5], 2, 
		[-2,-1,0,1,2,3,4,5].collect({ |x| (10**x).asString })
	 ) )
	.setSpec( \input, UAdaptSpec() )
	.category_( 'operator' )