/*
\uchain_duration

Creates an UMap that passes on the duration of the current UChain. It has no parameters, and can only be applied to SMPTESpec arguments (i.e. time arguments).

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

FuncUMapDef( \uchain_duration, { |unit|
	var chain;
	chain = UChain.nowPreparingChain;
	if( chain.notNil ) {
		chain.duration;
	} {
		1;
	};
})
	.category_( 'function' )
	.valueIsMapped_( false )
	.mappedArgs_( [] )
	.setSpec( \value, DisplaySpec( SMPTESpec(), _.asSMPTEString(1000) ), private: true )
	.canUseUMapFunc_({ |unit, key, umapdef|
		unit.getSpec( key ).isKindOf( SMPTESpec );
	})