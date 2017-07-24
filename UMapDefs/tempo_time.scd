/*
\tempo_time

Creates an UMap that converts tempo to time. This UMap can also be used on 'init' mode parameters. It can only be used for time (SMPTESpec) parameters.

factor: number or fraction of beats (0.125-8)
tempo: beats per minute

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

FuncUMapDef( \tempo_time, { |unit, factor = 1, tempo = 60|
	 factor / tempo * 60
})
	.category_( 'convert' )
	.valueIsMapped_( false )
	.mappedArgs_( [] )
	.setSpec( \factor, [0.125,8,\exp].asSpec )
	.setSpec( \tempo, [10,300,\exp,0,60].asSpec )
	.setSpec( \value, DisplaySpec( SMPTESpec(), _.asSMPTEString(1000) ), private: true )
	.prepareArgsFunc_( { |args|
		args.replace( \division, \factor );
	} )
	.canUseUMapFunc_({ |unit, key, umapdef|
		unit.getSpec( key ).isKindOf( SMPTESpec );
	})