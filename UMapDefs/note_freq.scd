/*
\note_freq

Creates an UMap for converting diatonic mini-note values to a frequency, in semitones where 69 == 440Hz.

note: a midi note value 
round: rounding factor (0 - 12)
detune: detune amount in cents (-50 - 50)

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UMapDef( \note_freq, { |note = 64, round = 1, detune = 0|
	UMapDef.useMappedArgs = false; // use raw values instead of mapped (0-1)
	UMapOut.kr( (note.round(round) + (detune/100)).midicps );
})
	.mappedArgs_([ \note ])
	.setSpec( \note, UAdaptSpec({ |spec|
		ControlSpec( spec.minval.cpsmidi, spec.maxval.cpsmidi, \lin, 0, 64 );
	}) )
	.setSpec( \round, [0,12,\lin,0,1] )
	.setSpec( \detune, [-50,50,\lin,0,0, " cents"].asSpec )
	.canUseUMapFunc_({ |unit, key, umapdef|
		var spec;
		spec = unit.getSpec( key );
		spec.isKindOf( ControlSpec ) && { spec.warp.isKindOf( ExponentialWarp ) };
	})
	.category_( 'freq' )