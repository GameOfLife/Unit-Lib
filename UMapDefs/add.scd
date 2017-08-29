/*
\add

Creates an UMap adding (+) two values together. 

input: the value to add to
add: the amount to add
factor: multiplier for add (negative value results in subtraction0

The 'input' and 'add' arg ranges are mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/


HybridUMapDef( \add, { |input = 0.0, add = 0.0, factor = 1|
	var sig;
	UMapDef.useMappedArgs = false; // use raw values instead of mapped (0-1)
	sig = input + (add * factor);
	UMapOut.kr(sig);
}, { |unit, input = 0.0, add = 0.0, factor = 1|
	(input + (add * factor)).clip( *[unit.spec.minval, unit.spec.maxval] );
}
)
	.mappedArgs_([ \input, \add ])
	.prepareArgsFunc_({ |args|
		var index;
		index = args.indexOf( \value );
		if( index.notNil && { index.even }) {
			args[index] = \input;
		};
		args;
	})
	.setSpec( \factor, [-1,1,\lin,0,0].asSpec )
	.setSpec( \input, UAdaptSpec() )
	.setSpec( \add, UAdaptSpec() )
	.category_( 'operator' )