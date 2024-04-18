+ Boolean {
	blend { |that, blendFrac = 0.5|
		that = that.asBoolean;
		^if( blendFrac >= 0.5 ) { that } { this };
	}
}