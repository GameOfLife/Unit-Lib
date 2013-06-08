UMapGUI : UGUI {
	
	var <>header;
	
	*viewNumLines { |unit|
		^super.viewNumLines( unit ) + 1;
	}
	
	makeHeader { |bounds|
		composite.background = Color.blue.blend( Color.white, 0.8 ).alpha_(0.4);
		
		header = CompositeView( composite, bounds.width @ viewHeight )
			.background_(  Color.blue.blend( Color.white, 0.8 ).alpha_(0.4) )
			.resize_(2);
		
		StaticText( header, labelWidth @ viewHeight )
			.applySkin( RoundView.skin )
			.string_( unit.unitArgName.asString ++ " " )
			.align_( \right );

		StaticText( header, 
			Rect( labelWidth + 4, 0, (bounds.width - labelWidth), viewHeight ) 
		)
			.applySkin( RoundView.skin )
			.string_( " UMap:" + unit.defName );
	}
}