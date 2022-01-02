/*
\tempo_time

Creates an UMap that converts tempo to time. This UMap can also be used on 'init' mode parameters. It can only be used for time (SMPTESpec) parameters. The UMap also features a quantize option, designed to use in combination with 'timeToNext' on UPatterns. This option is useful for synchronizing multiple patterns that start asynchronously.

factor: number or fraction of beats (0.125-8)
tempo: beats per minute
quantize: (for use on 'timeToNext') quantize the time to the nearest beat or division. If quantize is set to 0 (default) nothing is changed.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

HybridUMapDef( \tempo_time, { |factor = 1, tempo = 60|
	UMapDef.useMappedArgs = false;
	UMapOut.kr( factor / tempo * 60, false );
}, { |unit, factor = 1, tempo = 60, quantize = 0.0|
	var time, currentTime, out;
	time = factor / tempo * 60;
	if( quantize > 0 ) {
		currentTime = thisThread.seconds;
		(currentTime.round( (quantize / tempo * 60).min(time) ) - currentTime + time)
	} {
		time;
	};
})
	.category_( 'convert' )
	.valueIsMapped_( false )
	.setSpec( \factor, [0.0625,16,\exp].asSpec )
	.setSpec( \tempo, [10,300,\exp,0,60].asSpec )
	.prepareArgsFunc_( { |args|
		args.replace( \division, \factor );
	} )
	.canUseUMapFunc_({ |unit, key, umapdef|
		unit.getSpec( key ).isKindOf( SMPTESpec ) or: {
			unit.getSpec( key ).isKindOf( UAdaptSpec )
		}
	})