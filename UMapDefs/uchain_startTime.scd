/*
\uchain_startTime

Creates an UMap that passes on the startTime of the current UChain. It has no parameters, and can only be applied to SMPTESpec arguments (i.e. time arguments).

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

FuncUMapDef( \uchain_startTime, { |unit|
	var chain;
	chain = UChain.nowPreparingChain;
	if( chain.notNil ) {
		chain.startTime;
	} {
		1;
	};
})
	.category_( 'function' )
	.valueIsMapped_( false )
	.mappedArgs_( [] )
	.setSpec( \value, DisplaySpec( SMPTESpec(), _.asSMPTEString(1000) ), private: true )
	.canUseUMapFunc_({ |unit, key, umapdef|
		var spec;
		spec = unit.getSpec( key );
		[ SMPTESpec, UAdaptSpec ].any({ |item| spec.isKindOf( item ) });
	})