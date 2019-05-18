/*
\uchain_duration

Creates an UMap that passes on the duration of the current UChain. If applied in a 'pattern' arg of an UPattern, the duration of the UPattern is used instead.

factor: a multiplication factor for the returned duration (0.1-10)

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

FuncUMapDef( \uchain_duration, { |unit, factor = 1|
	(UChain.duration ? 1) * factor
})
	.category_( 'uchain_info' )
	.valueIsMapped_( false )
	.mappedArgs_( [] )
	.setSpec( \factor, [ 0, 10, 1.calcCurve(0,10) ].asSpec )
	.setSpec( \value, DisplaySpec( SMPTESpec(), _.asSMPTEString(1000) ), private: true )
	.canUseUMapFunc_({ |unit, key, umapdef|
		var spec;
		spec = unit.getSpec( key );
		[ SMPTESpec, UAdaptSpec ].any({ |item| spec.isKindOf( item ) });
	})