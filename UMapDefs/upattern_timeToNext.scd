/*
\upattern_timeToNext

Creates an UMap that passes the current timeToNext of the current UPattern, if available. It can only be applied to SMPTESpec arguments (i.e. time arguments).

factor: a multiplication factor for the returned time (0.1-10)

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

FuncUMapDef( \upattern_timeToNext, { |unit, factor = 1.0|
	(UPattern.currentTimeToNext ? 1) * factor;
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