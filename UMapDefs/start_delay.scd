/*
\start_delay

This is the standard UMap for UPattern. The UMap adds a delay (i.e. a silend period) to the start of an UPattern. After the delay the UPattern will start playing.

delay: delay (in seconds)
pattern: the sustain / timeToNext for the rest of the UPatttern (can be any UMap from the 'upattern' category)

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

FuncUMapDef( \start_delay, { |unit, delay = 0, pattern = #[1,1]|
	var currentPat, pos = 0;
	currentPat = UPattern.nowCallingPattern;
	if( currentPat.notNil ) {
		pos = currentPat.localPos + currentPat.startPos;
	};
	if( pos < delay ) {
		[ 0, delay - pos ]
	} {
		pattern;
	}
})
	.category_( 'upattern' )
	.valueIsMapped_( false )
	.setSpec( \delay, SMPTESpec(0,3600) )
	.setSpec( \pattern, UPatternSpec() )
	.dontStoreValue_( true )
	.canUseUMapFunc_({ |unit, key, umapdef|
		unit.getSpec( key ).isKindOf( UPatternSpec );
	})