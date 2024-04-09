UMapGUI : UGUI {

	classvar <>nowBuildingUMap;

	var <>header, <>userView, <>mainComposite;
	var <>removeAction;
	var <>parentUnit;
	var <removeButton;
	var <>wasBuildingUMap;

	*viewNumLines { |unit|
		^super.viewNumLines( unit ) + 1 + (2/18);
	}

	makeViews { |bounds|
		var margin = 0@0, gap = 4@4;

		wasBuildingUMap = nowBuildingUMap;
		nowBuildingUMap = unit;

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
			Pen.fillColor = unit.guiColor;
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

		 bounds = bounds.insetAll(0,0,2,0);
		 this.makeSubViews( bounds );

		 nowBuildingUMap = wasBuildingUMap;
	}

	makeHeader { |bounds|
		var boldFont;
		var umapdragbinInsert;
		var umapdragbinReplace;
		var infoString;
		var dragging;
		var isPattern, chain;
		var massEditWindowButton, currentUChainGUI, skin;
		var unitInitFunc, unitInChain, pathToUMap;

		chain = UChainGUI.nowBuildingChain;
		unitInChain = UGUI.nowBuildingUnit;

		if( unitInChain.notNil ) {
			pathToUMap = unitInChain.getUMapPath( unit );
		};

		isPattern = chain.isKindOf( UPattern );

		currentUChainGUI = UChainGUI.nowBuildingUChainGUI;
		skin = RoundView.skin;

		unitInitFunc = { |unit, what ...args|
			if( what === \init ) { // close all views and create new
				if( UMapSetChecker.stall != true ) { chain.changed( \units ); };
			};
		};

		header = CompositeView( composite, bounds.width @ viewHeight )
			.resize_(2);

		boldFont = (RoundView.skin.tryPerform( \at, \font ) ??
			{ Font( Font.defaultSansFace, 12) }).boldVariant;

		StaticText( header, labelWidth @ viewHeight )
			.applySkin( RoundView.skin )
			//.font_( boldFont )
			.string_( unit.unitArgName.asString )
			.align_( \right );

		StaticText( header,
			Rect( labelWidth + 4, 0, (bounds.width - labelWidth), viewHeight )
		)
			.applySkin( RoundView.skin )
			.font_( boldFont )
			.string_( ":" + unit.fullDefName );

		if( unit.isKindOf( MassEditUMap ).not ) {
			UDragSource( header, Rect( bounds.width - 12 - 12 - 4, 2, 12, 12 ) )
			.beginDragAction_({
				{ UChainGUI.current.view.refresh }.defer(0.1);
				dragging = unit.deepCopy;
			})
			.background_( Color.gray(0.8,0.8) )
			.string_( "" );
		} { // mass edit window
			massEditWindowButton = SmoothButton( header,
				Rect( bounds.width - 18 - 12 - 4, 2, 18, 12 )
			)
			.label_( 'up' )
			.radius_( 2 )
			.action_({
				var allUnits, userClosed = true, massEditWindow, backupIndex;
				if( currentUChainGUI.massEditWindow.notNil && {
					currentUChainGUI.massEditWindow.isClosed.not
				}) {
					backupIndex = currentUChainGUI.massEditWindowIndex;
					currentUChainGUI.massEditWindow.close;
					currentUChainGUI.massEditWindowIndex = backupIndex;
				};
				RoundView.pushSkin( skin );
				massEditWindow = Window( unit.defName,
					currentUChainGUI.window.bounds.moveBy( currentUChainGUI.window.bounds.width + 10, 0 ),
					scroll: true ).front;
				massEditWindow.addFlowLayout;
				header.onClose_({
					if( massEditWindow.notNil && { massEditWindow.isClosed.not }) {
						userClosed = false;
						massEditWindow.close;
					};
				});
				currentUChainGUI.massEditWindow = massEditWindow;
				allUnits = unit.units.collect({ |item, ii|
					var ugui;
					//if( notMassEdit ) { ii = ii + (realIndex - unit.units.size) };
					//ugui = item.gui( massEditWindow );
					if( item.isUMap ) {
						StaticText( massEditWindow,
							(massEditWindow.bounds.width - 20 ) @ 14 )
						.applySkin( RoundView.skin )
						.string_(
							" % [ % ]".format( pathToUMap.join("."), ii )
						)
						.background_( Color.white.alpha_(0.5) )
						.resize_(2)
						.font_(
							(RoundView.skin.tryPerform( \at, \font ) ??
								{ Font( Font.defaultSansFace, 12) }).boldVariant
						);
						massEditWindow.view.decorator.nextLine;
						ugui = UMapGUI( massEditWindow, (massEditWindow.bounds.width - 20) @ 14, item );
						ugui.removeAction_({ |umap|
							UMapSetChecker.stall = true;
							umap.stop;
							parentUnit.units[ ii ].removeUMap( unit.unitArgName );
							UMapSetChecker.stall = false;
						});
						ugui.parentUnit = parentUnit.units[ ii ];
						ugui.mapSetAction = {
							chain.changed( \units );
						};
						[ item ] ++ item.getAllUMaps;
					} {
						StaticText( massEditWindow,
							(massEditWindow.bounds.width - 20) @ 14 )
						.applySkin( RoundView.skin )
						.string_(
							" % [ % ]".format( pathToUMap.join("."), ii )
						)
						.background_( Color.white.alpha_(0.5) )
						.resize_(2)
						.font_(
							(RoundView.skin.tryPerform( \at, \font ) ??
								{ Font( Font.defaultSansFace, 12) }).boldVariant
						);
						massEditWindow.view.decorator.nextLine;
						ugui = UGUI( massEditWindow, (massEditWindow.bounds.width - 20) @ 14,
							parentUnit.units[ii], [ unit.unitArgName ] );
						ugui.mapSetAction = {
							chain.changed( \units );
						};
						nil
					};
				}).select(_.notNil).flatten(1);
				allUnits.do({ |item|
					item.addDependant( unitInitFunc )
				});
				currentUChainGUI.massEditWindowIndex = pathToUMap;
				massEditWindow.onClose_({|win|
					allUnits.do(_.removeDependant(unitInitFunc));
					if( userClosed && {
						(currentUChainGUI.massEditWindow !? _.view) === win
					}) {
						currentUChainGUI.massEditWindowIndex = nil;
					};
				});
				RoundView.popSkin( skin );
			}).resize_(3);

			if( currentUChainGUI.massEditWindowIndex == pathToUMap ) {
				currentUChainGUI.addAfterBuildAction({
					massEditWindowButton.doAction;
				});
			};
		};

			removeButton = SmoothButton( header, Rect( bounds.width - 12, 2, 12, 12 ) )
				.label_( '-' )
				.canFocus_( false )
				.action_({
					removeAction.value( unit );
				});

		if( true && {
			unit.unitArgName == \pattern;
		}) {
			removeButton.visible = false;
		};

			SmoothButton( header, Rect( 2, 2, 12, 12 ) )
				.label_( ['down', 'play'] )
				.border_( 0 )
				.background_( nil )
				.hiliteColor_( nil )
				.canFocus_( false )
				.value_( unit.guiCollapsed.binaryValue )
				.action_({ |bt|
					unit.guiCollapsed = bt.value.booleanValue;
				});

		umapdragbinInsert = UDragBin( header, // insert UMap
			Rect( 14, 2, labelWidth - 10, 12 )
		)
		.canReceiveDragHandler_({ |vw, x,y|
			View.currentDrag.isKindOf( UMapDef ) && {
				(parentUnit !? (_.canUseUMap( unit.unitArgName, View.currentDrag ))
					? false) && {
					View.currentDrag.canInsert
				};
			};
		})
		.receiveDragHandler_({
			unit.stop;
			UMapSetChecker.stall = true;
			parentUnit.insertUMap( unit.unitArgName, View.currentDrag );
			UMapSetChecker.stall = false;
		});

		umapdragbinInsert.mouseDownAction_({
			var res;
			res = this.makeUMapDefMenu({ |def|
				(parentUnit !? (_.canUseUMap( unit.unitArgName, def )) ? false) && { def.canInsert };
			}, { |def, args|
				unit.stop;
				UMapSetChecker.stall = true;
				parentUnit.insertUMap( unit.unitArgName, def, args );
				UMapSetChecker.stall = false;
			}, {
				umapdragbinInsert.background = nil;
			}, includePattern: isPattern);
			if( res.notNil ) {
				umapdragbinInsert.background = Color.blue(0.9).alpha_(0.25);
			};
		});

		umapdragbinInsert.mouseUpAction_({
			umapdragbinInsert.background = nil;
		});

		umapdragbinReplace = UDragBin( header, // replace UMap
			Rect( labelWidth + 8, 2, (bounds.width - labelWidth - 22 - 6 - 16 ), 12 )
		)
		.canReceiveDragHandler_({ |vw, x,y|
			View.currentDrag.isKindOf( UMapDef ) && {
				parentUnit !? (_.canUseUMap( unit.unitArgName, View.currentDrag )) ? false;
			};
		})
		.receiveDragHandler_({
			unit.stop;
			unit.def = View.currentDrag;
		});


		if( UChainGUI.showInfoStrings ) {
			infoString = unit.def !? _.getInfoString;

			if( infoString.notNil  ) {
				umapdragbinReplace.toolTip_( infoString );
			};
		};

		umapdragbinReplace.mouseDownAction_({
			var res;
			res = this.makeUMapDefMenu({ |def, args|
				parentUnit !? (_.canUseUMap( unit.unitArgName, def )) ? false;
			}, { |def, args|
				unit.stop;
				unit.def = def;
				if( args.notNil ) {
					if( unit.isKindOf( MassEditUMap ) ) {
						args = args.collect({ |item, i|
							if( i.odd ) {
								Array.fill( unit.units.size, { item });
							} {
								item;
							}
						});
					};
					unit.set( *args )
				};
			}, {
				umapdragbinReplace.background = nil;
			}, { |def, subdefkey |
				if( subdefkey.notNil ) {
					unit.get( def.defNameKey ) == subdefkey;
				} {
					unit.def.name == def.name
				};
			}, includePattern: isPattern );
			if( res.notNil ) {
				umapdragbinReplace.background = Color.blue(0.9).alpha_(0.25);
			};
		});

		umapdragbinReplace.mouseUpAction_({
			umapdragbinReplace.background = nil;
		});
	}
}