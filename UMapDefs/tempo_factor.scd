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

FuncUMapDef( \tempo_factor, { |unit, sustain = 1, timeToNext = 1, tempo = 60, quantize = 0.0|
	var timeCorrection, currentTime;
	timeToNext = timeToNext / tempo * 60;
	sustain = sustain / tempo * 60;
	if( quantize > 0 ) {
		currentTime = thisThread.seconds;
		currentTime = currentTime.round( (quantize / tempo * 60).min( timeToNext ) ) - currentTime;
		if( ((UPattern.nowCallingPattern !? _.localPos) == 0) && { currentTime.abs > 1.0e-10 } ) {
			sustain = 0; timeToNext = currentTime;
		} {
			//timeToNext = currentTime + timeToNext;
		}
	};
	[ sustain, timeToNext ];
})
	.category_( 'upattern' )
	.valueIsMapped_( false )
	.mappedArgs_( [] )
	.setSpec( \sustain, [0,8,1.calcCurve(0,8),0,1].asSpec )
	.setSpec( \timeToNext, [0.125,8,\exp,0,1].asSpec )
	.setSpec( \tempo, [10,300,\exp,0,60].asSpec )
	.setSpec( \value, DisplaySpec( SMPTESpec(), _.asSMPTEString(1000) ), private: true )
	.dontStoreValue_( true )
	.canUseUMapFunc_({ |unit, key, umapdef|
		unit.getSpec( key ).isKindOf( UPatternSpec );
	})