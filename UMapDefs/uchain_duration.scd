/*
\uchain_duration

Creates an UMap that passes on the duration of the current UChain. It has no parameters, and can only be applied to SMPTESpec arguments (i.e. time arguments).

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

FuncUMapDef( \uchain_duration, { |unit|
	var chain;
	chain = UChain.nowPreparingChain ?? { UPattern.nowCallingPattern };
	if( chain.notNil ) {
		chain.duration;
	} {
		1;
	};
})
	.category_( 'uchain_info' )
	.valueIsMapped_( false )
	.mappedArgs_( [] )
	.setSpec( \value, DisplaySpec( SMPTESpec(), _.asSMPTEString(1000) ), private: true )
	.canUseUMapFunc_({ |unit, key, umapdef|
		var spec;
		spec = unit.getSpec( key );
		[ SMPTESpec, UAdaptSpec ].any({ |item| spec.isKindOf( item ) });
	})