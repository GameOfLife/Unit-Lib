/*
\divide

Creates an UMap for division (/) of two values.

input: the value
divide: the divider
negative: divide by negative value
factor: exponent of 10 to multiply the divider by

The input arg range is mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/


HybridUMapDef( \divide, { |input = 0.0, divide = 1.0, negative = 0, factor = 0|
	var sig;
	UMapDef.useMappedArgs = false; // use raw values instead of mapped (0-1)
	sig = input / (divide * negative.linlin(0,1,1,-1) * (10**factor));
	UMapOut.kr(sig);
}, { |unit, input = 0.0, divide = 1.0, negative = 0, factor = 0|
	(input / (divide * negative.binaryValue.linlin(0,1,1,-1) * (10**factor)))
		.clip( *[unit.spec.minval, unit.spec.maxval] );
})
	.mappedArgs_([ \input ])
	.prepareArgsFunc_({ |args|
		var index;
		index = args.indexOf( \value );
		if( index.notNil && { index.even }) {
			args[index] = \input;
		};
		args;
	})
	.setSpec( \divide, [0.1,10,\exp,0,1].asSpec )
	.setSpec( \negative, BoolSpec(false) )
	.setSpec( \factor, ListSpec( [-2,-1,0,1,2,3,4,5], 2,
		[-2,-1,0,1,2,3,4,5].collect({ |x| (10**x).asString })
	 ) )
	 .setSpec( \input, UAdaptSpec() )
	.category_( 'math' )