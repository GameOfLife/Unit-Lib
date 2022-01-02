/*
\note_freq

Creates an UMap for converting chromatic midi-note values to a frequency, in semitones where 69 == 440Hz.

note: a midi note value (can be in between semitones as well)
round: rounding factor (0 - 12)
base: frequency of note 69 (A)
detune: detune amount in cents (-50 - 50)
transpose: transposition in semitones

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

HybridUMapDef( \note_freq, { |note = 64, round = 1, base = 440, detune = 0, transpose = 0|
	UMapDef.useMappedArgs = false; // use raw values instead of mapped (0-1)
	UMapOut.kr(
		base * 2.pow((note.round(round) + (detune/100)) + transpose - 69 / 12) );
}, { |unit, note = 64, round = 1, base = 440, detune = 0, transpose = 0|
	base * 2.pow((note.round(round) + (detune/100)) + transpose - 69 / 12);
})
	.mappedArgs_([ \note ])
	.setSpec( \note, UAdaptSpec({ |spec|
		ControlSpec( spec.minval.cpsmidi, spec.maxval.cpsmidi, \lin, 0, spec.default.cpsmidi );
	}) )
	.setSpec( \round, [0,12,\lin,0,1] )
	.setSpec( \base, [68.midicps, 70.midicps,\lin,0,440] )
	.setSpec( \detune, [-50,50,\lin,0,0, " cents"].asSpec )
	.setSpec( \transpose, [-12,12,\lin,1,0, " semitones"].asSpec )
	.canUseUMapFunc_({ |unit, key, umapdef|
		var spec;
		spec = unit.getSpec( key );
		spec.isKindOf( ControlSpec ) && { (spec.minval > 0) && { spec.maxval < inf } };
	})
	.category_( 'convert' )