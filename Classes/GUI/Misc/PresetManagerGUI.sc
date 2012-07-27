PresetManagerGUI {
	
	var <presetManager;
	
	var <parent, <view, <views, <controller;
	var <viewHeight = 14, <labelWidth = 50;
	var <>action;
	var <font;
	
	*new { |parent, bounds, presetManager|
		^super.newCopyArgs( presetManager ).init( parent, bounds );
	}
	
	init { |inParent, bounds|
		parent = inParent;
		presetManager.addDependant( this );
		if( parent.isNil ) { 
			parent = Window( "PresetManager : %".format( presetManager.object ) ).front 
		};
		this.makeView( parent, bounds );
	}
	
	*getHeight { |viewHeight, margin, gap|
		viewHeight = viewHeight ? 14;
		margin = margin ?? {0@0};
		gap = gap ??  {4@4};
		^(margin.y * 2) + viewHeight
	}
	
	doAction { action.value( this ) }
	
	update {
		this.setViews( presetManager );
	}
	
	*viewNumLines { ^1 }
	
	resize_ { |resize|
		view.resize = resize ? 5;
	}
	
	remove {
		presetManager.removeDependant( this );
	}
	
	setViews { |inPresetManager|
		var match;
		{ 
			views[ \presets ].items = (inPresetManager.presets ? [])[0,2..];
			if( views[ \presets ].items.size == 1 ) { views[ \presets ].value = 0 };
			match = presetManager.match;
			if( match.notNil ) {
				views[ \presets ].value = views[ \presets ].items.indexOf( match );
			};
		}.defer;
	}
	
	font_ { |newFont| this.setFont( newFont ) }
	
	setFont { |newFont|
		font = newFont ? font ??
			{ RoundView.skin !? { RoundView.skin.font } } ?? 
			{ Font( Font.defaultSansFace, 10 ) };
		
		{
			views[ \label ].font = font;
			views[ \presets ].font = font;
		}.defer;
		
		views[ \read ].font = font;
		views[ \write ].font = font;
	}
	
	makeView { |parent, bounds, resize|
		
		if( bounds.isNil ) { bounds= 350 @ (this.class.viewNumLines * (viewHeight + 4)) };
		
		view = EZCompositeView( parent, bounds, gap: 4@4 );
		bounds = view.asView.bounds;
		view.onClose_({ this.remove; });
		view.resize_( resize ? 5 );
		views = ();
		
		views[ \label ] = StaticText( view, labelWidth @ viewHeight )
			.applySkin( RoundView.skin ? () )
			.string_( " presets" );

		views[ \presets ] = PopUpMenu( view, 120 @ viewHeight )
			.applySkin( RoundView.skin ? () )
			.action_({ |pu|
				var item;
				if( pu.items.size > 0 ) {
					item = pu.item;
					presetManager.apply( item );
					action.value( this );
				};
			});
			
		views[ \remove ] = SmoothButton( view, viewHeight @ viewHeight )
			.radius_( viewHeight/2 )
			.label_( '-' )
			.action_({
				presetManager.removeAt( views[ \presets ].item );
				action.value( this );
			});
		
		views[ \add ] = SmoothButton( view, viewHeight @ viewHeight )
			.radius_( viewHeight/2 )
			.label_( '+' )
			.action_({
				SCRequestString( 
					(views[ \presets ].item ? "default").asString, 
					"Please enter a name for this preset:",
					{ |string|
						presetManager.put( string );
						action.value( this );
					}
				);
			});
			
		views[ \read ] = SmoothButton( view, 40 @ viewHeight )
			.states_( [ [ "read", Color.black, Color.green(1,0.25) ] ] )
			.action_({
				presetManager.read( action: { action.value( this ); } );
			});
		
		views[ \write ] = SmoothButton( view, 40 @ viewHeight )
			.states_( [ [ "write", Color.black, Color.red(1,0.25) ] ] )
			.action_({
				presetManager.write( successAction: { action.value( this ); } );
			});
			
		this.setFont;
		this.update;
	}

}