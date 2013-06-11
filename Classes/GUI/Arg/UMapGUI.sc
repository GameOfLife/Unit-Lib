UMapGUI : UGUI {
	
	classvar >color;
	
	var <>header, <>userView, <>mainComposite;
	
	*viewNumLines { |unit|
		^super.viewNumLines( unit ) + 1.1;
	}
	
	*color { ^color ?? { color = Color.blue.blend( Color.white, 0.8 ).alpha_(0.5) }; }
	
	makeViews { |bounds|
		var margin = 0@0, gap = 4@4;
		
		if( bounds.isNil ) { 
			bounds = parent.asView.bounds.insetBy(4,4);
			if( parent.asView.class.name == \SCScrollTopView ) {
				bounds.width = bounds.width - 12;
			};
			if( parent.asView.class.name == \QScrollTopView ) {
				bounds.width = bounds.width - 20;
			};
		};
		bounds = bounds.asRect;
		bounds.height = this.class.getHeight( unit, viewHeight, margin, gap );
		mapChecker = UMapSetChecker( unit, { mapSetAction.value( this ) } );
		controller = SimpleController( unit );
		
		if( unit.class == MassEditUMap ) {
			unit.connect;
		};
		
		unit.valuesAsUnitArg;
		
		mainComposite = CompositeView( parent, bounds ).resize_(2);
		
		userView = UserView( mainComposite, bounds.moveTo(0,0) ).resize_(2);
		
		userView.drawFunc = { |vw|
			Pen.width = 1;
			Pen.fillColor = this.class.color;
			Pen.strokeColor = Color.black.alpha_(0.5);
			Pen.roundedRect( vw.bounds.moveTo(0,0).insetBy(0.5,0.5), 3 );
			Pen.fillStroke;
		};
		
		userView.canFocus_( false );
		
		composite = CompositeView( mainComposite, bounds.moveTo(0,0) ).resize_(2);
		composite.addFlowLayout( margin, gap );
		composite.onClose = {
			if( unit.class == MassEditUMap ) {
				unit.disconnect;
			}; 
			controller.remove;
			mapChecker.remove;
		 };
		 
		 this.makeSubViews( bounds );
	}
	
	makeHeader { |bounds|
		var boldFont;
		
		header = CompositeView( composite, bounds.width @ viewHeight )
			.resize_(2);
			
		boldFont = (RoundView.skin.tryPerform( \at, \font ) ?? 
			{ Font( Font.defaultSansFace, 12) }).boldVariant;
		
		StaticText( header, labelWidth @ viewHeight )
			.applySkin( RoundView.skin )
			.string_( unit.unitArgName.asString ++ ": " )
			.font_( boldFont )
			.align_( \right );

		StaticText( header, 
			Rect( labelWidth + 4, 0, (bounds.width - labelWidth), viewHeight ) 
		)
			.applySkin( RoundView.skin )
			.font_( boldFont )
			.string_( unit.defName );
			
		if( unit.class != MassEditUMap ) { // no close button until massedit updating is fixed
			SmoothButton( header, Rect( bounds.width - 14, 2, 12, 12 ) )
				.label_( 'x' )
				.radius_( 3 )
				.canFocus_( false )
				.action_({
					unit.remove;
				});
		};
	}
}