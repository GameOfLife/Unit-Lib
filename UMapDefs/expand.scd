ExpandUMapDef( \expand, { |unit|
	unit.spec.expandArgSpecs;
}, \utility )
	.guiColor_( Color.white.alpha_(0.5) )
	.canUseUMapFunc_({ |unit, key, umapdef|
		unit.getSpec( key ).respondsTo( \expandArgSpecs );
	});