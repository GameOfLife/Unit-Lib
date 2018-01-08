/*
\p_uscore_status

Creates an UMap that tests the status of the UPattern's parent score. Different values (or UMaps) van be set depending on the playback status (playing, paused or stopped)

playingValue: value (or UMap) returned if score is playing
pausedValue: value (or UMap) returned if score is paused
stoppedValue: value (or UMap) returned if score is stopped

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UPatDef( \p_uscore_status, { |unit, playingValue = 1, pausedValue = 0.5, stoppedValue = 0|
	{
		var chain, score;
		inf.do {	|i|
			chain = UChain.nowPreparingChain ?? { UPattern.nowCallingPattern };
			if( chain.notNil ) {
				if( chain.parent.isKindOf( UPattern ) ) {
					chain = chain.parent;
				};
				score = UScore.activeScores.detect({ |score| score.events.includes( chain ) });
			};
			if( score.notNil ) {
				case { score.isPlaying or: score.isPreparing } {
					playingValue.next.yield;
				} { score.isPaused } {
					pausedValue.next.yield;
				} {
					stoppedValue.next.yield;
				};
			} { 
				stoppedValue.next.yield;
			};
		};
	}.r
})
	.mappedArgs_([ \playingValue, \pausedValue, \stoppedValue ])
	.useMappedArgs_( false )
	.category_( 'pattern' )
	.setSpec( \playingValue, UAdaptSpec() )
	.setSpec( \pausedValue, UAdaptSpec() )
	.setSpec( \stoppedValue, UAdaptSpec() )
	.canUseUMapFunc_({ |unit, key, umapdef|
		unit.isKindOf( UPattern ).not 
	})