PresetManagerGUI {

	var <presetManager, <>object;

	var <parent, <view, <views, <controller;
	var <viewHeight = 14, <labelWidth = 58;
	var <>action;
	var <font;

	*new { |parent, bounds, presetManager, object|
		^super.newCopyArgs( presetManager, object ).init( parent, bounds );
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

	resize { ^view.resize }
	resize_ { |resize|
		view.resize = resize ? 5;
	}

	remove {
		presetManager.removeDependant( this );
	}

	setViews { |inPresetManager|
		var match;
		views[ \undo ].visible = inPresetManager.lastObject.notNil;
		{
			views[ \presets ].items = (inPresetManager.presets ? [])[0,2..];
			if( views[ \presets ].items.size == 1 ) { views[ \presets ].value = 0 };
			match = presetManager.match( object );
			match = match ?  presetManager.lastChosen;
			if( match.notNil ) {
				views[ \presets ].items.indexOf( match ) !? { |value|
					views[ \presets ].value = value
				};
			};
		}.defer;
	}

	font_ { |newFont| this.setFont( newFont ) }

	setFont { |newFont|
		font = newFont ? font ??
			{ RoundView.skin !? { RoundView.skin.font } } ??
			{ Font( Font.defaultSansFace, 11 ) };

		{
			views[ \label ].font = font;
			views[ \presets ].font = font;
		}.defer;
	}

	makeView { |parent, bounds, resize|

		var presetWidth;

		if( bounds.isNil ) { bounds= 350 @ (this.class.viewNumLines * (viewHeight + 4)) };

		view = EZCompositeView( parent, bounds, gap: 4@4 );
		bounds = view.asView.bounds;
		view.onClose_({ this.remove; });
		view.resize_( resize ? 5 );
		views = ();

		views[ \label ] = StaticText( view, labelWidth @ viewHeight )
			.applySkin( RoundView.skin ? () )
			.string_( " presets" );

		views[ \undo ] = SmoothButton( view, viewHeight @ viewHeight )
			.radius_( viewHeight/2 )
			.label_( 'arrow_pi' )
			.action_({
				presetManager.undo( object );
			});

		views[ \apply ] = SmoothButton( view, viewHeight @ viewHeight )
			.radius_( viewHeight/2 )
			.label_( 'arrow' )
			.action_({
				views[ \presets ].doAction;
			});

		presetWidth = bounds.width - labelWidth - (viewHeight * 2) - 12;

		views[ \presets ] = UPopUpMenu( view, presetWidth @ viewHeight )
		.resize_(5)
		.extraMenuActions_({[
			MenuAction.separator,
			MenuAction( "Add...", {
				SCRequestString(
					(views[ \presets ].item ? "default").asString,
					"Please enter a name for this preset:",
					{ |string|
						presetManager.put( string, object );
						views[ \presets ].value = views[ \presets ]
							.items.indexOf( string.asSymbol );
						action.value( this );
					}
				);
			}),
			Menu(
				*views[ \presets ].items.collect({ |item|
					MenuAction( item.asString, {
						presetManager.removeAt( item );
						action.value( this );
					})
				})
			).title_( "Remove" ),
			MenuAction.separator,
			MenuAction( "Read from file...", {
				if( presetManager.filePath.notNil ) {
					SCAlert(
						"Do you want to read the default settings\nor import from a file?",
						[ "cancel", "import", "default" ],
						[ { }, {
							presetManager.read( \browse, action: { action.value( this ); } );
						}, {
							presetManager.read( action: { action.value( this ); } );
						} ];
					);
				} {
					presetManager.read( action: { action.value( this ); } );
				};
			}),
			MenuAction( "Write to file...", {
				if( presetManager.filePath.notNil ) {
					SCAlert(
						"Do you want to write to default settings\nor export to a file?",
						[ "cancel", "export", "default" ],
						[ { }, {
							presetManager.write( \browse,
								successAction: { action.value( this ); } );
						}, {
							presetManager.write( overwrite: true,
								successAction: { action.value( this ); } );
						} ];
					);
				} {
					presetManager.write( successAction: { action.value( this ); } );
				};
			})
		]})
			.action_({ |pu|
				var item;
				if( pu.items.size > 0 ) {
					item = pu.item;
					presetManager.apply( item, object );
					action.value( this );
				};
			});

		this.setFont;
		this.update;
	}

}

+ PresetManager {
	gui { |view, bounds|
		^PresetManagerGUI( view, bounds, this );
	}
}