/*
\time_freq

Creates an UMap for converting the duration of a period in a frequency to a time value.

time: time for one period
factor: multiplication factor (0.1-10) for the resulting frequency

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UMapDef( \time_freq, { |time = 0.002, factor = 1|
	UMapDef.useMappedArgs = false; // use raw values instead of mapped (0-1)
	UMapOut.kr( factor / time.max(1.0e-12) );
})
	.setSpec( \time, UAdaptSpec({ |spec|
		SMPTESpec( 1/(spec.minval.max(1.0e-12)), 1/spec.maxval );
	}) )
	.setSpec( \factor, [ 0.1, 10, \exp ].asSpec )
	.canUseUMapFunc_({ |unit, key, umapdef|
		unit.getSpec( key ).isKindOf( FreqSpec );
	})
	.mappedArgs_( [ \time ] )
	.category_( 'convert' )