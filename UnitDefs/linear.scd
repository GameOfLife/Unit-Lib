/*
\linear

gives an arg that doesn't have a linear ArgSpec a linear warp in the same range. For example; \freq args usually have non-linear warps (\exp in this case), as well as \amp args.

input: the value to be mapped (0-1)

The 'input' arg range is mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

HybridUMapDef( \linear, { |input = 0.5|
	UMapDef.useMappedArgs = false;
	UMapOut.kr(input);
}, { |unit, input = 0.5|
	input
})
	.mappedArgs_( [ \input ] )
	.setSpec( \input, UAdaptSpec({ |sp| sp.copy.warp_( \lin ) }) )
	.prepareArgsFunc_({ |args|
		var index;
		index = args.indexOf( \value );
		if( index.notNil && { index.even }) {
			args[index] = \input;
		};
		args;
	})
	.canUseUMapFunc_({ |unit, key, umapdef|
		var spec;
		UMapDef.defaultCanUseUMapFunc.value( unit, key, umapdef ) && {
			spec = unit.getSpec( key );
			spec.respondsTo( \warp_ ) && { spec.warp.isKindOf( LinearWarp ).not };
		};
	})
	.category_( 'map' )


