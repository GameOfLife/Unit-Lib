UMapGUI : UGUI {
	
	classvar >color;
	
	var <>header, <>userView, <>mainComposite;
	var <>removeAction;
	var <>parentUnit;
	
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
		
		if( unit.isKindOf( MassEditU ) ) {
			mapCheckers = unit.units.collect({ |unit|
				if( unit.isKindOf( U ) ) {
					UMapSetChecker( unit, { mapSetAction.value( this ) } ); 
				};
			}).select(_.notNil);
			unit.connect;
		} {
			mapCheckers = [ UMapSetChecker( unit, { mapSetAction.value( this ) } ) ];
		};
		
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
		
		controller = SimpleController( unit );		
		composite = CompositeView( mainComposite, bounds.moveTo(0,0) ).resize_(2);
		composite.addFlowLayout( margin, gap );
		composite.onClose = {
			if( unit.class == MassEditUMap ) {
				unit.disconnect;
			}; 
			controller.remove;
			mapCheckers.do(_.remove);
		 };
		 
		 if( unit.guiCollapsed ) {
			 this.makeHeader(bounds);
		 } {
			 this.makeSubViews( bounds );
		 };
	}
	
	makeHeader { |bounds|
		var boldFont;
		
		header = CompositeView( composite, bounds.width @ viewHeight )
			.resize_(2);
			
		boldFont = (RoundView.skin.tryPerform( \at, \font ) ?? 
			{ Font( Font.defaultSansFace, 12) }).boldVariant;
		
		StaticText( header, labelWidth @ viewHeight )
			.applySkin( RoundView.skin )
			.font_( boldFont )
			.string_( unit.unitArgName.asString )
			.align_( \right );

		StaticText( header, 
			Rect( labelWidth + 4, 0, (bounds.width - labelWidth), viewHeight ) 
		)
			.applySkin( RoundView.skin )
			.font_( boldFont )
			.string_( ":" + unit.defName );
			
			SmoothButton( header, Rect( bounds.width - 14, 2, 12, 12 ) )
				.label_( '-' )
				.canFocus_( false )
				.action_({
					removeAction.value( unit );
				});
				
			SmoothButton( header, Rect( 2, 2, 12, 12 ) )
				.label_( ['down', 'play'] )
				.border_( 0.0000001 )
				.background_( nil )
				.hiliteColor_( nil )
				.canFocus_( false )
				.value_( unit.guiCollapsed.binaryValue )
				.action_({ |bt|
					unit.guiCollapsed = bt.value.booleanValue;
				});
			
			UserView( header, Rect( 16, 2,  bounds.width - 16 - 16, 12 ) )
				.canReceiveDragHandler_({
					View.currentDrag.isKindOf( UMapDef ) && {
						parentUnit !? (_.canUseUMap( unit.unitArgName, View.currentDrag )) 
							? false; 
					};
				})
				.receiveDragHandler_({
					unit.stop;
					unit.def = View.currentDrag;
				});
	}
}