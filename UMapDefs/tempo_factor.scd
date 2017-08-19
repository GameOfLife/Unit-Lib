/*
\tempo_factor

This UMap is intended for use on the 'pattern' value of an UPattern. It allows the sustain and timeToNext values of the pattern to be set in a specific tempo (BPM)

sustain: sustain time as number or fraction of beats (0.125-8)
timeToNext: time to next as number or fraction of beats (0.125-8)
tempo: beats per minute

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

FuncUMapDef( \tempo_factor, { |unit, sustain = 1, timeToNext = 1, tempo = 60|
	 [sustain, timeToNext] / tempo * 60
})
	.category_( 'upattern' )
	.valueIsMapped_( false )
	.mappedArgs_( [] )
	.setSpec( \sustain, [0.125,8,\exp].asSpec )
	.setSpec( \timeToNext, [0.125,8,\exp].asSpec )
	.setSpec( \tempo, [10,300,\exp,0,60].asSpec )
	.setSpec( \value, DisplaySpec( SMPTESpec(), _.asSMPTEString(1000) ), private: true )
	.canUseUMapFunc_({ |unit, key, umapdef|
		unit.getSpec( key ).isKindOf( UPatternSpec );
	})