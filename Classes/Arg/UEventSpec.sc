UEventSpec : Spec {
	var <>default;

	constrain { |value| ^(value ? default) }

	*testObject { |obj|
		^obj.isNil or: { obj.isKindOf( UEvent ) };
	}

	map { |value| ^value }
	unmap { |value| ^value }


	makeView { |parent, bounds, label, action, resize|
		var vws, view, labelWidth, ucgui;
		vws = ();

		// this is basically an EZButton

		bounds.isNil.if{bounds= 350@20};

		#view, bounds = EZGui().prMakeMarginGap.prMakeView( parent, bounds );
		vws[ \view ] = view;
		vws[ \val ] = this.default;
		vws[ \doAction ] = { action.value( vws, vws[ \val ] ) };

		ucgui = UChainGUI.nowBuildingUChainGUI;

		if( label.notNil ) {
			labelWidth = (RoundView.skin ? ()).labelWidth ? 80;
			vws[ \labelView ] = StaticText( vws[ \view ], labelWidth @ bounds.height )
				.string_( label.asString ++ " " )
				.align_( \right )
				.resize_( 4 )
				.applySkin( RoundView.skin );
		} {
			labelWidth = 0;
		};

		vws[ \buttonView ] = SmoothButton( vws[ \view ],
				Rect( labelWidth + 2, 0, 60, bounds.height ) )
		.label_( [ "new", "edit" ] )
		.hiliteColor_( nil )
		.action_({ |bt|
			var bounds;
			if( vws[ \val ].isNil ) {
				vws[ \val ] = UChain.default;
				action.value( vws, vws[ \val ] );
			};
			bt.value = 1;
			bounds = ucgui !? { |x| x.parent.asView.findWindow !? _.bounds };
			if( bounds.notNil ) { bounds = bounds.moveBy( bounds.width + 10, 0 ) };
			case { vws[ \val ].isKindOf( UChain ) } {
				vws[ \uchaingui ] !? _.close;
				vws[ \uchaingui ] = UChainGUI(
					bounds: bounds,
					chain: vws[ \val ],
					replaceCurrent: false,
					canMakeCurrent: false
				);
				vws[ \uchaingui ].newChainAction_({ |g, new|
					vws[ \val ] = new;
					action.value( vws, vws[ \val ] );
				});
			};
		});

		vws[ \view ].onClose_({
			vws[ \uchaingui ] !? _.close;
		});

		vws[ \buttonView ]
		.radius_( bounds.height / 8 )
		.resize_( 1 );

		if( resize.notNil ) { vws[ \view ].resize = resize };
		^vws;
	}

	setView { |view, value, active = false|
		view[ \val ] = value;
		view[ \buttonView ].value = view[ \val ].notNil.binaryValue;
		if( active ) { view.doAction };
	}

	mapSetView { |view, value, active = false|
		this.setView( view, value, active );
	}

}