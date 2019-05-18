/*
\spawn

This is the standard UMap for UPattern. If applied the UPattern can be used to spawn events live, using the .spawn command (only possible from SuperCollider code). BEWARE: this feature is experimental, and doesn't work yet for UPatterns that need preparation time (i.e. don't use with bufSoundFile that is not global, diskSoundFile, convolution or trajectory)

sustain: sustain of pattern event (in seconds)

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

FuncUMapDef( \spawn, { |unit, sustain = 1|
	if( (UPattern.nowCallingPattern !? _.localPos) == 0 ) { sustain = 0 };
	[ sustain, Condition(false) ]
})
	.category_( 'upattern' )
	.valueIsMapped_( false )
	.setSpec( \sustain, SMPTESpec(0,3600) )
	.setSpec( \timeToNext, SMPTESpec(0.01,3600) )
	.dontStoreValue_( true )
	.canUseUMapFunc_({ |unit, key, umapdef|
		unit.getSpec( key ).isKindOf( UPatternSpec );
	})