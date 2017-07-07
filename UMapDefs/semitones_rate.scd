/*
\semitones_rate

Creates an UMap for converting a fractional (playback) rate to semitones of transposition.

semitones: amount of semitones transposition

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UMapDef( \semitones_rate, { |semitones = 0|
	UMapDef.useMappedArgs = false; // use raw values instead of mapped (0-1)
	UMapOut.kr( semitones.midiratio );
})
	.mappedArgs_([ \semitones ])
	.setSpec( \semitones, UAdaptSpec({ |spec|
		ControlSpec( spec.minval.ratiomidi, spec.maxval.ratiomidi, \lin, 
			0, spec.default.ratiomidi );
	}) )
	.canUseUMapFunc_({ |unit, key, umapdef|
		var spec;
		spec = unit.getSpec( key );
		spec.isKindOf( ControlSpec ) && { (spec.minval > 0) && { spec.maxval < inf } };
	})
	.category_( 'convert' )