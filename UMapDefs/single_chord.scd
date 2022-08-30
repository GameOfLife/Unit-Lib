/*
\single_chord

This is the standard UMap for UPattern. The UMap turns the pattern into a single chord player. I.e. it plays a number of copies of the chain at the same time, with the same duration as the whole UPattern (can be inf)

n: number of voices (1-100, default 2)
strum: the voices of the chord will be started one-by-one over the strum time. A zero strum time (default) means all voices start immediately.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

FuncUMapDef( \single_chord, { |unit, n = 2, strum = 0|
	var count = UPattern.count;
	if( count < (n - 1) ) {
		[ inf,  strum / n ]
	} {
		[ inf, inf ]
	}
})
	.category_( 'upattern' )
	.valueIsMapped_( false )
	.setSpec( \strum, SMPTESpec(0,3600) )
	.setSpec( \n, IntegerSpec(2,1,10000) )
	.dontStoreValue_( true )
	.canUseUMapFunc_({ |unit, key, umapdef|
		unit.getSpec( key ).isKindOf( UPatternSpec );
	})