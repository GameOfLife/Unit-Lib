/*
    Unit Library
    The Game Of Life Foundation. http://gameoflife.nl
    Copyright 2006-2011 Miguel Negrao, Wouter Snoei.

    GameOfLife Unit Library: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    GameOfLife Unit Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with GameOfLife Unit Library.  If not, see <http://www.gnu.org/licenses/>.
*/

UChainGUI {

	classvar <>skin, <>skins;
	classvar <>current;
	classvar <>all;
	classvar <>singleWindow = true;
	classvar <>packUnitsDefault = true;
	classvar <>scrollViewOrigin;
	classvar <>startTimeMode = \time; // \time, \bar
	classvar <>durationMode = \duration; // \duration, \endTime, \endBar
	classvar <>nowBuildingChain, <>nowBuildingUChainGUI;
	classvar <>showInfoStrings = true;
	classvar <>recentUdefs;
	classvar <>showPrivateUdefs = false;

	var <chain, <score, <parentScore;

	var <parent, <composite, <views, <startButton, <uguis;
	var <>presetView;
	var <>action;
	var originalBounds;
	var <packUnits = true;
	var <>scrollView;
	var <>massEditWindowIndex, <>massEditWindow;
	var <>tempoMap;
	var <>undoManager;
	var <>afterBuildAction;
	var <>canMakeCurrent = true;
	var <>newChainAction;

	var <>autoRestart = false;

	*initClass {

		skins = OEM(
			\light,  (
				labelWidth: 100,
				menuStripColor: Color.gray(0.9),
				headerColor: Color.white.alpha_(0.5),
				stringColor: Color.black,
				hiliteColor: Color.black.alpha_(0.33),
				scoreEditorWindow: Color.gray(0.65),
				RoundButton: (
					border: 0,
					background: Color.white.alpha_(0.5),
					hiliteColor: Color.green.alpha_(0.5),
				),
				SmoothButton: (
					border: 0,
					background: Color.white.alpha_(0.5),
					hiliteColor: Color.green.alpha_(0.5),
				),
				qPalette: { QPalette.light },
			),

			\medium, (
				labelWidth: 100,
				menuStripColor: Color.gray(0.8),
				headerColor: Color.white.alpha_(0.33),
				stringColor: Color.black,
				hiliteColor: Color.black.alpha_(0.33),
				scoreEditorWindow: Color.gray(0.65),
				disabledColor: Color.gray(0.69803921568627, 0.5 ),
				RoundButton: (
					border: 0,
					background: Color.white.alpha_(0.33),
					hiliteColor: Color.green.alpha_(0.33),
				),
				SmoothButton: (
					border: 0,
					background: Color.white.alpha_(0.33),
					hiliteColor: Color.green.alpha_(0.33),
				),
				SmoothNumberBox: (
					background: Color.white.alpha_(0.25)
				),
				SMPTEBox: (
					background: Color.white.alpha_(0.25)
				),
				XYView: (
					background: Color.white.alpha_(0.25)
				),
				qPalette: { QPalette.auto(Color.grey(0.75), Color.grey(0.7))
					.base_( Color.grey(0.75) )
					.setColor( Color.grey( 0.1 ), \shadow )
				},
			),

			\dark, (
				labelWidth: 100,
				menuStripColor: Color.gray(0.2),
				hiliteColor: Color.black.alpha_(0.33),
				headerColor: Color.black.alpha_(0.25),
				stringColor: Color.white,
				scoreEditorWindow: Color.gray(0.4),
				disabledColor: Color.gray(0.29803921568627, 0.5 ),
				TextField: (
					background: Color.gray(0.3),
				),
				RoundButton: (
					border: 0,
					background: Color.white.alpha_(0.125),
					hiliteColor: Color.green(1.0,0.33),
				),
				SmoothButton: (
					border: 0,
					background: Color.white.alpha_(0.125),
					hiliteColor: Color.green(1.0,0.33),
				),
				SmoothSlider: (
					knobColor: Color.white,
					//background:  Color.white.alpha_(0.05),
					hiliteColor: Color.white.alpha_(0.2),
				),
				SmoothRangeSlider: (
					knobColor: Color.white,
					//background:  Color.white.alpha_(0.05),
					hiliteColor: Color.white.alpha_(0.2),
				),
				SMPTEBox: (
					background: Color.white.alpha_(0.15)
				),
				SmoothNumberBox: (
					background: Color.white.alpha_(0.15),
					normalColor: Color.white,
					typingColor: Color.red.alpha_(0.66),
				),
				SCAlert: (
					background: Color.gray(0.2)
				),
				qPalette: { QPalette.dark },
			),

			\very_dark, (
				labelWidth: 100,
				menuStripColor: Color.gray(0.0),
				hiliteColor: Color.black.alpha_(0.33),
				headerColor: Color.white.alpha_(0.25),
				stringColor: Color.gray(0.75),
				scoreEditorWindow: Color.gray(0.2),
				disabledColor: Color.gray(0.098039215686275, 0.5 ),
				TextField: (
					background: Color.gray(0.3),
				),
				RoundButton: (
					border: 0,
					background: Color.white.alpha_(0.125),
					hiliteColor: Color.green(1.0,0.33),
				),
				SmoothButton: (
					border: 0,
					background: Color.white.alpha_(0.125),
					hiliteColor: Color.green(1.0,0.33),
				),
				SmoothSlider: (
					knobColor: Color.white,
					//background:  Color.white.alpha_(0.05),
					hiliteColor: Color.white.alpha_(0.2),
				),
				SmoothRangeSlider: (
					knobColor: Color.white,
					//background:  Color.white.alpha_(0.05),
					hiliteColor: Color.white.alpha_(0.2),
				),
				SMPTEBox: (
					background: Color.white.alpha_(0.15)
				),
				SmoothNumberBox: (
					background: Color.white.alpha_(0.15),
					normalColor: Color.white,
					typingColor: Color.red.alpha_(0.66),
				),
				SCAlert: (
					background: Color.gray(0.2)
				),
				qPalette: {
					QPalette.auto( Color.grey(0.2), Color.grey(0.1) )
					.base_( Color.grey(0.1) )
					.baseText_( Color.gray(0.75) )
					.windowText_( Color.gray(0.75) )
					.setColor(Color.grey(0.08), \shadow)
					.highlight_( Color(0.25, 0.37, 0.57) )
				}
			),

			\light_old,  (
				labelWidth: 100,
				menuStripColor: Color.gray(0.9),
				headerColor: Color.white.alpha_(0.5),
				stringColor: Color.black,
				hiliteColor: Color.black.alpha_(0.33),
				scoreEditorWindow: Color.gray(0.65),
				RoundButton: (
					border: 0.9,
					background:  Gradient( Color.white, Color.gray(0.85), \v ),
					hiliteColor: Color.green.alpha_(0.5),
				),
				SmoothButton: (
					border: 0.9,
					background:  Gradient( Color.white, Color.gray(0.85), \v ),
					hiliteColor: Color.green.alpha_(0.5),
				),
				qPalette: QPalette.light,
			),

			\dark_old, (
				labelWidth: 100,
				menuStripColor: Color.gray(0.2),
				hiliteColor: Color.black.alpha_(0.33),
				headerColor: Color.black.alpha_(0.25),
				stringColor: Color.white,
				scoreEditorWindow: Color.gray(0.4),
				TextField: (
					background: Color.gray(0.3),
				),
				RoundButton: (
					border: 0.75,
					background: Gradient( Color.gray(0.6), Color.gray(0.45), \v ),
					hiliteColor: Color.green(1.0,0.33),
				),
				SmoothButton: (
					border: 0.75,
					background: Gradient( Color.gray(0.6), Color.gray(0.45), \v ),
					hiliteColor: Color.green(1.0,0.33),
				),
				SmoothSlider: (
					knobColor: Color.white,
					//background:  Color.white.alpha_(0.05),
					hiliteColor: Color.white.alpha_(0.2),
				),
				SmoothRangeSlider: (
					knobColor: Color.white,
					//background:  Color.white.alpha_(0.05),
					hiliteColor: Color.white.alpha_(0.2),
				),
				SMPTEBox: (
					background: Color.white.alpha_(0.15)
				),
				SmoothNumberBox: (
					background: Color.white.alpha_(0.15),
					normalColor: Color.white,
					typingColor: Color.red.alpha_(0.66),
				),
				SCAlert: (
					background: Color.gray(0.2)
				),
				qPalette: { QPalette.dark },
			),

		);

		skin = skins.light;

		StartUp.defer({
			skins.keysValuesDo({ |key, skin|
				skin.font = Font( Font.defaultSansFace, 11 );
			});
		});

		all = [];

	}

	*new { |parent, bounds, chain, score, replaceCurrent, canMakeCurrent = true|
		^super.newCopyArgs( chain, score ).canMakeCurrent_( canMakeCurrent ).init( parent, bounds, replaceCurrent );
	}

	init { |inParent, bounds, replaceCurrent|
		var oldBounds, oldTitle;
		parent = inParent;

		packUnits = if( chain.isKindOf( MassEditUChain ) ) { false; } { packUnitsDefault; };

		parentScore = UScore.currentSubScore;

		tempoMap = parentScore !? _.tempoMap ?? { TempoMap() };

		if( skin.font.class != Font.implClass ) { // quick hack to make sure font is correct
			skin.font = Font( Font.defaultSansFace, 11 );
		};

		if( parent.isNil ) {
			parent = chain.class.asString;
		};
		if( parent.class == String ) {
			if( (singleWindow and: { replaceCurrent != false }) && current.notNil && { current.window.class == Window.implClass } ) {
				parent = current.parent.asView.findWindow;
				oldBounds = parent.bounds;
				oldTitle = parent.name;
				parent.close;
				parent = Window(
					oldTitle,
					oldBounds,
					scroll: false
				).front;
				this.makeViews( bounds );
				this.makeCurrent;
				this.addToAll;
				this.doAfterBuildAction;
			} {
				parent = Window(
					parent,
					bounds ?? { Rect(425, 300, 372, 600) },
					scroll: false
				).front;
				this.makeViews( );
				this.makeCurrent;
				this.addToAll;
				this.doAfterBuildAction;
			};
		} {
			this.makeViews( bounds );
			this.makeCurrent;
			this.addToAll;
			this.doAfterBuildAction;
		};

		parent.asView.minWidth_( 372 ).minHeight_(200);

	}

	chain_ { |newChain|
		if( chain != newChain && { newChain.notNil } ) {
			chain = newChain;
			this.rebuild;
			newChainAction.value( this, chain );
		};
	}

	rebuild {
		{
			var oldBounds, oldTitle, parentWindow;
			parentWindow = parent.asView.findWindow;
			oldBounds = parentWindow.bounds;
			oldTitle = parentWindow.name;
			scrollViewOrigin = this.scrollView.visibleOrigin;
			parentWindow.close;
			parent = Window(
				oldTitle,
				oldBounds,
				scroll: false
			).front;
			this.makeViews();
			this.makeCurrent;
			this.addToAll;
			this.doAfterBuildAction;
			parent.asView.minWidth_( 372 ).minHeight_(200);
		}.defer;
	}

	makeCurrent { if( canMakeCurrent ) { current = this } }

	doAfterBuildAction { afterBuildAction.value; afterBuildAction = nil }

	addAfterBuildAction { |action|
		afterBuildAction = afterBuildAction.addFunc( action );
	}

	addToAll { all = all.add( this ) }
	removeFromAll { all.remove( this ) }

	makeViews { |bounds|
		RoundView.useWithSkin( skin ++ (RoundView.skin ? ()), {
			this.prMakeViews( bounds );
		});
		this.fixPopUpMenus;
	}

	undo { |amt = 1|
		undoManager !? { chain.handleUndo( undoManager.undo( amt ) ) };
	}

	getHeight { |units, margin, gap|
		^units.collect({ |unit|
			UGUI.getHeight( unit, 14, margin, gap ) + 14 + gap.y + gap.y;
		}).sum + (4 * (14 + gap.y));
	}

	getUnits {
		var units;
		if( this.packUnits ) {
			units = Array( chain.units.size );
			chain.units.do({ |item, i|
				case { i == 0 or: { item.isKindOf( MassEditU ) or:
						{ item.def.isKindOf( LocalUdef ) } }
				} {
					units.add( item );
				} { item.subDef == units.last.subDef } {
					if( units.last.isKindOf( MassEditU ) ) {
						units.last.units = units.last.units.add(item);
					} {
						units[ units.size - 1 ] = MassEditU([ units.last, item ]);
					};
				} { units.add( item ); }
			});
			^units;
		} {
			^chain.units
		};
	}

	setUnits { |units|
		var actualUnits = [];
		units.do({ |item|
			case { item.isKindOf( MassEditU ) } {
				item.units.do({ |item|
					actualUnits = actualUnits.add( item );
				});
			} {
				actualUnits = actualUnits.add( item );
			};
		});
		chain.units = actualUnits;
	}

	packUnits_ { |bool = true|
		packUnits = bool;
		packUnitsDefault = packUnits;
		chain.changed( \units )
	}

	canPackUnits {
		var wasPackUnits, res;
		wasPackUnits = packUnits;
		packUnits = true;
		res = this.getUnits.any( _.isKindOf( MassEditU ) );
		packUnits = wasPackUnits;
		^res;
	}

	prMakeViews { |bounds|
		var margin = 0@0, gap = 4@4;
		var heights, units;
		var labelWidth;
		var controller;
		var udefController;
		var scoreController;
		var massEditController;
		// var unitInitFunc;

		nowBuildingChain = chain;
		nowBuildingUChainGUI = this;

		labelWidth = 80;

		if( RoundView.skin.notNil ) { labelWidth = RoundView.skin.labelWidth ? 80 };

		views = ();

		originalBounds = bounds.copy;

		bounds = bounds ?? { parent.asView.bounds.insetBy(4,4) };
		if( parent.asView.class.name == 'SCScrollTopView' ) {
			bounds.width = bounds.width - 12;
		};

		units = this.getUnits;

		units.do(_.checkDef);

		controller = SimpleController( chain );
		udefController = SimpleController( Udef.all );
		massEditController = { |...args| chain.update(*args) };

		composite = CompositeView( parent, bounds ).resize_(5);
		composite.addFlowLayout( margin, gap );
		composite.onClose = { |vw|
			controller.remove;
			scoreController.remove;
			udefController.remove;
			if( chain.isKindOf( MassEditUChain ) ) {
				chain.removeDependantFromChains( massEditController );
			};
			this.removeFromAll;
			if( composite == vw && { current == this } ) { current = nil }
		};

		// startbutton
		views[ \startButton ] = SmoothButton( composite, 14@14 )
			.label_( ['power', 'power'] )
			.radius_(7)
		    .toolTip_( "play/stop only this UChain" )
			.action_( [ {
					chain.prepareAndStart;							}, {
					chain.release
				} ]
		 	);

		if( UMenuBarIDE.hasMenus && { thisProcess.platform.name == \linux } ) {
			views.startButton.setContextMenuActions(
				*UMenuBarIDE.allMenus.atAll(
					[ UMenuBarIDE.currentMenuName.asSymbol, \File, \Edit, \View ]
				)
			);
		};

		 composite.decorator.shift( bounds.width - 14 - 90 - 88, 0 );

		if( chain.isKindOf( MassEditUChain ) ) {
			chain.addDependantToChains( massEditController );
		} {

			composite.decorator.shift( -34, 0 );

			undoManager = undoManager ?? { UndoManager() };

			if( chain.handlingUndo ) {
				chain.handlingUndo = false;
			} {
				undoManager.add( chain );
			};

			UndoView( composite, 30@14, chain, undoManager ).view
			.toolTip_( "undo/redo" )
			.resize_(3);
		};

		views[ \displayColor ] = UserView( composite, 84@14 )
			.resize_(3)
		    .toolTip_( "set displayColor" )
			.drawFunc_({ |vw|
				var wd = 8, smallRect;
				if( (score ? chain).displayColor.notNil ) {
					Pen.roundedRect(vw.drawBounds, wd);
					(score ? chain).displayColor.penFill(vw.drawBounds, 1, nil, chain) ;
					smallRect = Rect( vw.bounds.width - wd, 0, wd, wd );
					Pen.color = Color.gray(0.66,0.75);
					Pen.addOval( smallRect, 2 );
					Pen.fill;
					Pen.color = Color.black;
					DrawIcon( '-', smallRect );
				} {
					Pen.roundedRect(vw.drawBounds, wd);
					(score ? chain).getTypeColor.penFill( vw.drawBounds );
				};
			})
		.mouseDownAction_({ |vw, x,y|
			var wd = 8, smallRect;
			smallRect = Rect( vw.bounds.width - wd, 0, wd, wd );
			if( smallRect.containsPoint( x@y ) ) {
				(score ? chain).displayColor = nil;
				vw.refresh;
			} {
				this.makeColorEditorWindow( views, score, chain, parentScore );
			};
		})
			.keyDownAction_({ |vw, a,b,cx|
				if( cx == 127 ) { (score ? chain).displayColor = nil };
			})
			.beginDragAction_({ (score ? chain).displayColor })
			.canReceiveDragHandler_({
				var obj;
				obj = View.currentDrag;
				if( obj.class == String ) {
					obj = { obj.interpret }.try;
				};
				obj.respondsTo( \penFill );
			})
			.receiveDragHandler_({
				if( View.currentDrag.class == String ) {
					(score ? chain).displayColor = View.currentDrag.interpret;
				} {
					(score ? chain).displayColor = View.currentDrag;
				};
			})
			.onClose_({ if( views[ \colorEditor ].notNil ) {
					views[ \colorEditor ].view.findWindow.close;
				};
			});

		views[ \singleWindow ] = SmoothButton( composite, 84@14 )
			.label_( [ "single window", "single window" ] )
			//.hiliteColor_( RoundView.skin.hiliteColor ? Color.green )
			.value_( this.class.singleWindow.binaryValue )
		    .toolTip_( "if 'single window' is enabled any edited UChain will\n" ++
			    "replace the current window. If disabled a new window will be created" ++
			    "for every edited UChain"
			)
			.resize_(3)
			.action_({ |bt|
				this.class.singleWindow = bt.value.booleanValue;
			});

		if( canMakeCurrent == false ) { views[ \singleWindow ].visible = false };

		if( chain.isPlaying ) {
			views[ \startButton ].value = 1;
		};

		composite.decorator.nextLine;

		if( score.notNil ) {
			// score name
			StaticText( composite, labelWidth@14 )
				.applySkin( RoundView.skin )
				.string_( "name" )
				.align_( \right );

			views[ \name ] = TextField( composite, 84@14 )
				.applySkin( RoundView.skin )
				.string_( score.name )
				.action_({ |tf|
					score.name_( tf.string );
				});

			views[ \allowPause ] = SmoothButton( composite, 80@14 )
				.radius_( 3 )
				.label_( [ "allowPause", "allowPause" ] )
				.hiliteColor_( Color.green )
				.action_({ |bt|
					score.allowPause = bt.value.booleanValue;
				});

			composite.decorator.nextLine;

			// startTime
			UPopUpMenu( composite, labelWidth@14 )
			.align_( \right )
			.items_( [ "startTime", "startBar" ] )
			.value_( [ \time, \bar ].indexOf( startTimeMode ) ? 0 )
			.action_({ |pu|
				startTimeMode = [ \time, \bar ][ pu.value ];
				views[ \startTime ].visible = (startTimeMode === \time );
				views[ \startBar ].visible = (startTimeMode === \bar );
			});

			views[ \startTime ] = SMPTEBox( composite, 84@14 )
				.applySmoothSkin
				.applySkin( RoundView.skin )
				.clipLo_(0)
				.visible_( startTimeMode === \time )
				.action_({ |nb|
					score.startTime_( nb.value );
				});

			composite.decorator.shift( -88, 0 );

			views[ \startBar ] = TempoBarMapView( composite, 84@14, tempoMap  )
				.applySkin( RoundView.skin )
				.radius_(2)
				.clipLo_(0)
				.visible_( startTimeMode === \bar )
				.action_({ |nb|
					score.startTime_( nb.value );
				});

			views[ \lockStartTime ] = SmoothButton( composite, 14@14 )
				.label_([ 'unlock', 'lock' ])
				.radius_(2)
			    .toolTip_( "lock/unlock startTime" )
				.value_( score.lockStartTime.binaryValue )
				.action_({ |bt|
					score.lockStartTime = bt.value.booleanValue;
				});

			composite.decorator.nextLine;
		} {
			// startTime
			views[ \startMenu ] = UPopUpMenu( composite, labelWidth@14 )
			.align_( \right )
			.items_( [ "startTime", "startBar" ] )
			.toolTip_( "startTime or startBar" )
			.value_( [ \time, \bar ].indexOf( startTimeMode ) ? 0 )
			.action_({ |pu|
				startTimeMode = [ \time, \bar ][ pu.value ];
				views[ \startTime ].visible = (startTimeMode === \time );
				views[ \startBar ].visible = (startTimeMode === \bar );
			});

			if( chain.isKindOf( MassEditUChain ) ) {
				views[ \startMenu ].extraMenuActions = {[
					MenuAction.separator,
					MenuAction( "Edit", {
						var spec, vws = (), times;
						RoundView.pushSkin( UChainGUI.skin );
						times = chain.uchainsOrUMarkers.collect(_.startTime);
						spec = SMPTESpec(0,inf).massEditSpec( times );
						spec.makeEditWindow( views, times, "startTime", { |values|
							//views[ \editWin ].setValues( values );
							chain.uchainsOrUMarkers.do({ |item, i|
								item.startTime = values[i].max(0);
							});
						} );
						views[ \updateEditWin ] = { |vws|
							vws[ \editWin ] !? _.setValues( chain.uchainsOrUMarkers.collect(_.startTime) );
						};
						RoundView.popSkin( UChainGUI.skin );
					})
				]};
				views[ \startMenu ].onClose = views[ \startMenu ].onClose.addFunc({ views[ \editWin ] !? _.close });
			};

			views[ \startTime ] = SMPTEBox( composite, 84@14 )
				.applySmoothSkin
				.applySkin( RoundView.skin )
				.clipLo_(0)
				.visible_( startTimeMode === \time )
				.action_({ |nb|
					chain.startTime_( nb.value );
				});

			composite.decorator.shift( -88, 0 );

			views[ \startBar ] = TempoBarMapView( composite, 84@14, tempoMap  )
				.applySkin( RoundView.skin )
				.radius_(2)
				.clipLo_(0)
				.visible_( startTimeMode === \bar )
				.action_({ |nb|
					chain.startTime_( nb.value );
				});

			views[ \lockStartTime ] = SmoothButton( composite, 14@14 )
				.label_([ 'unlock', 'lock' ])
			    .toolTip_( "lock/unlock startTime" )
				.radius_(2)
				.action_({ |bt|
					chain.lockStartTime = bt.value.booleanValue;
				});

			composite.decorator.nextLine;

			if( chain.isKindOf( MassEditUChain ).not or: { chain.uchains.size > 0 } ) {
				// duration
				UPopUpMenu( composite, labelWidth@14 )
				.align_( \right )
				.toolTip_( "duration, endTime or endBar" )
				.items_( #[ duration, endTime, endBar ] )
				.value_(  #[ duration, endTime, endBar ].indexOf( durationMode ) ? 0 )
				.action_({ |pu|
					durationMode = pu.item;
					views[ \dur ].visible = (durationMode === \duration );
					views[ \endTime ].visible = (durationMode === \endTime );
					views[ \endBar ].visible = (durationMode === \endBar );
					views[ \startTime ].visible = (startTimeMode === \time );
					views[ \startBar ].visible = (startTimeMode === \bar );
				});

				views[ \dur ] = SMPTEBox( composite, 84@14 )
					.applySmoothSkin
					.applySkin( RoundView.skin )
					.clipLo_(0)
					.visible_( durationMode === \duration )
					.action_({ |nb|
						if( nb.value == 0 ) {
							chain.dur_( inf );
						} {
							chain.dur_( nb.value );
						};
					});

				composite.decorator.shift( -88, 0 );

				views[ \endTime ] = SMPTEBox( composite, 84@14 )
					.applySmoothSkin
					.applySkin( RoundView.skin )
					.clipLo_(0)
					.visible_( durationMode === \endTime )
					.action_({ |nb|
						if( nb.value <= chain.startTime ) {
							chain.dur_( inf );
						} {
							chain.dur_( nb.value - chain.startTime );
						};
					});

				composite.decorator.shift( -88, 0 );

				views[ \endBar ] = TempoBarMapView( composite, 84@14, tempoMap  )
					.applySkin( RoundView.skin )
					.radius_(2)
					.clipLo_(0)
					.visible_( durationMode === \endBar )
					.action_({ |nb|
						if( nb.value <= chain.startTime ) {
							chain.dur_( inf );
						} {
							chain.dur_( nb.value - chain.startTime );
						};
					});

				views[ \infDur ] = SmoothButton( composite, 40@14 )
					.radius_( 2 )
					.label_( [ "inf", "inf" ] )
				    .toolTip_( "set infinite duration" )
					.action_({ |bt|
						var dur;
						switch( bt.value,
							0, { dur = views[ \dur ].value;
								if( dur == 0 ) {
									dur = 1;
								};
								chain.dur_( dur ) },
							1, { chain.dur_( inf ) }
						);
				});

				views[ \fromSoundFile ] = SmoothButton( composite, 40@14 )
					.radius_( 2 )
				    .toolTip_( "automatically match duration to the longest soundfile in the chain" )
					.label_( "auto" )
					.action_({ chain.useSndFileDur });


				views[ \releaseSelf ] = SmoothButton( composite, 84@14 )
					.radius_( 2 )
				.toolTip_( "releaseSelf\n\nif releaseSelf is enabled the UChain will stop itself" +
					"after the duration has passed. If releaseSelf is disabled, the UChain" +
					"will be ended by the score or the user. This can be useful in cases" +
					"where a Marker is used with 'autoPause' enabled; if the endTime of the" +
					"UChain is after such a marker and the startTime before it, it will only" +
					"be ended after the user has unpaused the UScore, making it play indefinitely" +
					"until that moment. If releaseSelf is enabled in such a case the UChain will" +
					"just stop after the duration is finished, regardless of the UScore playback status"
				)
					.label_( [ "releaseSelf", "releaseSelf" ] )
					.action_({ |bt|
						chain.releaseSelf = bt.value.booleanValue;
					});

				composite.decorator.nextLine;

				// fadeTimes
				StaticText( composite, labelWidth@14 )
					.applySkin( RoundView.skin )
					.string_( "fadeTimes" )
					.align_( \right );

				views[ \numberBoxBackground ] = RoundView.skin[ 'SmoothNumberBox' ] !? _.background ? Color(1.0, 1.0, 1.0, 0.5);

				views[ \fadeIn ] = SmoothNumberBox( composite, 40@14 )
					.clipLo_(0)
					.scroll_step_(0.1)
					.formatFunc_( { |value| [ value.round(0.01), "s" ].join(" ") } )
					.background_( { |rect|
						Pen.use({
							var values;
							Pen.roundedRect( rect, 2 ).clip;
							Pen.color = views[ \numberBoxBackground ];
							Pen.fillRect( rect );
							values = (rect.width.asInteger + 1).collect({ |i|
								i.lincurve(0, rect.width, rect.bottom, rect.top, chain.fadeInCurve )
							});
							Pen.moveTo( rect.leftBottom );
							values.do({ |item, i|
								Pen.lineTo( (rect.left + i) @ item );
							});
							Pen.lineTo( rect.rightBottom );
							Pen.lineTo( rect.leftBottom );
							Pen.color = Color(0.5,0.5,0.5, if( chain.fadeInTime > 0 ) { 0.5 } { 0.125 } );
							Pen.fill;
						});
					})
					.action_({ |nb|
						chain.fadeIn_( nb.value );
					});

				views[ \fadeOut ] = SmoothNumberBox( composite, 40@14 )
					.clipLo_(0)
					.scroll_step_(0.1)
					.formatFunc_( { |value| [ value.round(0.01), "s" ].join(" ") } )
					.background_( { |rect|
						Pen.use({
							var values;
							Pen.roundedRect( rect, 2 ).clip;
							Pen.color = views[ \numberBoxBackground ];
							Pen.fillRect( rect );
							values = (rect.width.asInteger + 1).collect({ |i|
								i.lincurve(0, rect.width, rect.top, rect.bottom, chain.fadeOutCurve )
							});
							Pen.moveTo( rect.leftBottom );
							values.do({ |item, i|
								Pen.lineTo( (rect.left + i) @ item );
							});
							Pen.lineTo( rect.rightBottom );
							Pen.lineTo( rect.leftBottom );
							Pen.color = Color(0.5,0.5,0.5, if( chain.fadeOutTime > 0 ) { 0.5 } { 0.125 } );
							Pen.fill;
						});
					})
					.action_({ |nb|
						chain.fadeOut_( nb.value );
					});

				views[ \expandFades ] = SmoothButton( composite, 12@12 )
					.label_( '+' )
					.action_({
						chain.fadeTimes = UMap( \expand, [ \fadeIn, chain.fadeInTime, \fadeOut, chain.fadeOutTime ] );
					});


				if( chain.isKindOf( UPattern ) ) {
					if( chain.fadeTimes.isKindOf( UMap ) ) {
						views[ \fadeIn ].enabled_( false );
						views[ \fadeOut ].enabled_( false );
						views[ \expandFades ].enabled_( false );
					} {
						views[ \fadeIn ].enabled_( true );
						views[ \fadeOut ].enabled_( true );
						views[ \expandFades ].enabled_( true );
					};
				} {
					views[ \expandFades ].visible_( false );
				};

				composite.decorator.shift( 28, 0 );

				views[ \global ] = SmoothButton( composite, 40@14 )
					.radius_( 2 )
				.toolTip_( "'global'\n\nThe 'global' parameter is only relevant in a multi-server" +
					"setup. In such a setup a 'global' UChain will play on all servers" +
					"at the same time, instead of only one.\n\nThe most obvious use if this" +
					"is when an 'auxIn' Udef is used, receiving audio from another UChain" +
					"with 'auxOut'. The global setting makes sure the UChain runs on all servers" +
					"so that the uchain with the 'auxOut' can be on any of them. Beware that this" +
					"multiplies the total CPU usage by as many times as there are servers active." +
					"Also beware that if 'global' is used on an UChain that generates audio, the" +
					"audio will be much louder as it is played on all servers simultaneously." +
					"\n\nOn a single-server setup (default) this parameter makes no difference," +
					"but please be aware that if you enable it it will make a difference when" +
					"your UScore is played on a system that has multiple servers (such as an" +
					"actual WFS system)."
				)
					.label_( [ "global", "global" ] )
					.action_({ |bt|
						chain.global = bt.value.booleanValue;
					});

				views[ \ugroup ] = UPopUpMenu( composite, 84@14 )
				.toolTip_( "UGroup\n\nUGroups are only relevant on multi-server setups." +
					"You can create a UGroup to make sure that the UChains in it are" +
					"played on the same server.\n\nThis can be useful for example with" +
					"'auxIn' / 'auxOut' combinations. Beware that if you put many events" +
					"simultaneously in a UGroup the system may be less efficient, as the" +
					"load cannot be divided over multiple cores in such occasion. If" +
					"you have many events speaking to the same 'auxIn' event it is better" +
					"to set that (receiving) event to 'global' instead and not use UGroups." +
					"In cases however where there are only a few events it may be more efficient" +
					"to use UGroups.\n\nOn a single-server system (default) the UGroup setting doesn't" +
					"make any audible or functional difference, but if you you use things" +
					"that involve exchange of audio between multiple UChains and you want your work to" +
					"be played correctly on a multi-server setup (such as an actual WFS system) you should" +
					"be mindful about these settings"
				)
				.align_( \center )
				.title_( "ugroup" )
				.extraMenuActions_({[
					MenuAction.separator,
					MenuAction( "New...", {
						SCRequestString( "default", "Please enter a unique name for a new UGroup", { |string|
							string = string.asSymbol;
							if( UGroup.all.collect(_.id) !? { |x| x.includes( string ).not } ? true ) {
								UGroup( string );
								chain.ugroup = string;
							} {
								"UGroup '%' already exists\n".postf( string );
								chain.ugroup = string;
							};
						})
					})
				]})
				.action_({ |vw|
					if( vw.item.isString ) {
						chain.ugroup = nil;
					} {
						chain.ugroup = vw.item;
					};
				});

				views[ \fillUGroup ] = { |vws|
					var groups;
					groups = UGroup.all.collect(_.id) ? [];
					if( chain.ugroup.notNil && { groups.includes(chain.ugroup).not }) {
						groups = groups ++ [ chain.ugroup ]
					};
					vws[ \ugroup ].items = [ "(no ugroup)" ] ++ groups;
					vws[ \ugroup ].value = vws[ \ugroup ].items.indexOf( chain.ugroup ) ? 0;
				};

				views.fillUGroup;

				composite.decorator.nextLine;

				// fadeCurves
				StaticText( composite, labelWidth@14 )
					.applySkin( RoundView.skin )
					.string_( "fadeCurves" )
					.align_( \right );

				views[ \fadeInCurve ] = SmoothNumberBox( composite, 40@14 )
					.clipLo_(-20)
				    .clipHi_(20)
					.scroll_step_(0.1)
					.formatFunc_( { |value| value.round(0.1).asString } )
					.action_({ |nb|
						chain.fadeInCurve_( nb.value );
					});

				views[ \fadeOutCurve ] = SmoothNumberBox( composite, 40@14 )
					.clipLo_(-20)
				    .clipHi_(20)
					.scroll_step_(0.1)
					.formatFunc_( { |value| value.round(0.1).asString } )
					.action_({ |nb|
						chain.fadeOutCurve_( nb.value );
					});

				composite.decorator.shift( 88, 0 );

				views[ \addAction ] = UPopUpMenu( composite, 84@14 )
				.align_( \center )
				.toolTip_( "addAction\n\nThe addAction determines the order of execution for" +
					"multiple UChains. It is relevant for example when using 'auxIn' / 'auxOut'" +
					"combinations across various UChains (also on single-server systems). Typically" +
					"a UChain sending audio via 'auxOut' should be in 'addToHead' mode (default), and" +
					"a UChain receiving audio via 'auxIn' should be in 'addToTail' mode.\n\nThere are" +
					"also 'addAfter' and 'addBefore', which are SuperCollider standard addActions" +
					"but they don't make any real difference in the context of Unit-Lib/WFSCollider" +
					"where they will behave more or less the same as 'addToTail' and 'addToHead'" +
					"\n\nMore on addActions can be found in the SuperCollider documentation"
				)
				.title_( "addAction" )
				.items_( #[ addBefore, addToHead, addToTail, addAfter, mixed ] )
				.value_( #[ addBefore, addToHead, addToTail, addAfter, mixed ].indexOf( chain.addAction ) )
				.action_({ |pu|
					chain.addAction = pu.item ?? { pu.items[1]; };
					if( pu.items.includes( \mixed ) && { chain.addAction != \mixed } ) {
						pu.items = #[ addBefore, addToHead, addToTail, addAfter ];
					};
				});

				if( chain.addAction != \mixed ) {
					views[ \addAction ].items = #[ addBefore, addToHead, addToTail, addAfter ];
				};

				composite.decorator.nextLine;
			}
		};

		if( chain.isKindOf( MassEditUChain ).not or: { chain.uchains.size > 0 } ) {

			if( chain.isKindOf( MassEditUChain ) && { chain.uchains.size > 1 } ) {
				UPopUpMenu( composite, labelWidth@14 )
				.string_( "gain " )
				.align_( \right )
				.extraMenuActions_({[
					MenuAction.separator,
					MenuAction( "Edit gains", {
						var spec, gains;
						RoundView.pushSkin( UChainGUI.skin );
						gains = chain.uchains.collect(_.gain);
						spec = ControlSpec(-96,24,\lin,0,0).massEditSpec( gains );
						spec.makeEditWindow( views, gains, "gain", { |values|
							//views[ \editWin ].setValues( values );
							chain.uchains.do({ |item, i|
								item.setGain( values[i] );
							});
							views.updateEditWin;
						} );
						views[ \updateEditWin ] = { |vws|
							vws[ \editWin ] !? _.setValues( chain.uchains.collect(_.gain ) );
						};
						RoundView.popSkin( UChainGUI.skin );
					}),
					MenuAction( "Edit mutes", {
						var spec, mutes;
						RoundView.pushSkin( UChainGUI.skin );
						mutes = chain.uchains.collect(_.muted);
						spec = BoolSpec( false, "mute", "mute" ).massEditSpec( mutes );
						spec.makeEditWindow( views, mutes, "mute", { |values|
							//views[ \editWin ].setValues( values );
							chain.uchains.do({ |item, i|
								item.muted_( values[i] );
							});
							views.updateEditWin;
						} );
						views[ \updateEditWin ] = { |vws|
							vws[ \editWin ] !? _.setValues( chain.uchains.collect(_.muted ) );
						};
						RoundView.popSkin( UChainGUI.skin );
					})
				]})
			} {
				// gain
				StaticText( composite, labelWidth@14 )
				.applySkin( RoundView.skin )
				.string_( "gain" )
				.align_( \right );
			};

			views[ \gain ] = SmoothNumberBox( composite, 40@14 )
				.clipHi_(24) // just to be safe)
				.action_({ |nb|
					chain.setGain( nb.value );
				});

			views[ \gain ].view.toolTip_( "output gain of the UChain in dB" );

			views[ \muted ] = SmoothButton( composite, 40@14 )
				.radius_( 2 )
				.label_( [ "mute", "mute" ] )
			    .hiliteColor_( Color.red(1,0.75) )
				.action_({ |bt|
					switch( bt.value,
						0, { chain.muted = false },
						1, { chain.muted = true }
					);
				});

			composite.decorator.nextLine;

			controller.put( \gain, {
				views[ \gain ].value = chain.getGain;
				{ views[ \displayColor ].refresh }.defer;
				views.updateEditWin;
			} );
			controller.put( \muted, {
				views[ \muted ].value = chain.muted.binaryValue;
				views.updateEditWin;
			} );
		};

		if( chain.isKindOf( MassEditUChain ) && { chain.umarkers.size > 0 } ) {

			StaticText( composite, labelWidth@14 )
				.applySkin( RoundView.skin )
			    .string_( "autoPause" )
				.align_( \right );

			views[ \autoPause ] = BoolSpec(true).massEditSpec( chain.autoPause ).makeView( composite, 126@14, action: { |vws, value|
				chain.autoPause = value
			} );
		};

		controller
			.put( \start, { views[ \startButton ].value = 1 } )
			.put( \end, {
				if( units.every({ |unit| unit.synths.size == 0 }) ) {
					views[ \startButton ].value = 0;
				};
			} )
			.put( \units, {
				var bounds, title;
				if( composite.isClosed.not ) {
					if( chain.class == MassEditUChain ) {
						chain.init;
					} {
						controller.remove;
						this.rebuild;
					};
				};
			})
			.put( \init, {
				var bounds, title;
				if( composite.isClosed.not ) {
					controller.remove;
					this.rebuild;
				};
			});

		udefController.put( \added, { |obj, msg, def|
			if( chain.units.any({ |u| u.defName == def.name }) ) {
				{ chain.changed( \units ); }.defer(0.1);
			};
		} );

		if( score.isNil ) {
			controller
				.put( \displayColor, { { views[ \displayColor ].refresh; views.setColorEditor }.defer; } )
				.put( \lockStartTime, {
					views[ \lockStartTime ].value = chain.lockStartTime.binaryValue;
				});

			if( chain.isKindOf( MassEditUChain ) && { chain.uchains.size == 0 } ) {
				controller
					.put( \startTime, {
						views[ \startTime ].value = chain.startTime ? 0;
						views[ \startBar ].value = chain.startTime ? 0;
					})
			} {
				controller
					.put( \startTime, {
						views[ \startTime ].value = chain.startTime ? 0;
						views[ \startBar ].value = chain.startTime ? 0;
						views.updateEditWin;
						if( chain.dur == inf ) {
							views[ \endTime ].value = chain.startTime ? 0;
							views[ \endBar ].value = chain.startTime ? 0;
						} {
							views[ \endTime ].value = (chain.startTime + chain.dur) ? 0;
							views[ \endBar ].value = (chain.startTime + chain.dur) ? 0;
						};
					})
				.put( \dur, { var dur;
					dur = chain.dur;
					if( dur == inf ) {
						views[ \dur ].enabled = false; // don't set value
						views[ \endTime ].enabled = false; // don't set value
						views[ \endBar ].enabled = false; // don't set value
						views[ \infDur ].value = 1;
						views[ \releaseSelf ].enabled_( false );
						//views[ \releaseSelf ].hiliteColor = Color.green.alpha_(0.25);
						//views[ \releaseSelf ].stringColor = Color.black.alpha_(0.5);
					} {
						views[ \dur ].enabled = true;
						views[ \endTime ].enabled = true;
						views[ \endBar ].enabled = true;
						views[ \dur ].value = dur;
						views[ \endTime ].value = chain.startTime + dur;
						views[ \endBar ].value = chain.startTime + dur;
						views[ \infDur ].value = 0;
						views[ \releaseSelf ].enabled_( true );
						//views[ \releaseSelf ].hiliteColor = Color.green.alpha_(1);
						//views[ \releaseSelf ].stringColor = Color.black.alpha_(1);
					};
					{ views[ \displayColor ].refresh; }.defer;
				})
				.put( \fadeIn, { views[ \fadeIn ].value = chain.fadeInTime })
				.put( \fadeOut, { views[ \fadeOut ].value = chain.fadeOutTime })
				.put( \fadeInCurve, {
					views[ \fadeInCurve ].value = chain.fadeInCurve;
					{ views[ \fadeIn ].refresh }.defer;
				})
				.put( \fadeOutCurve, {
					views[ \fadeOutCurve ].value = chain.fadeOutCurve;
					{ views[ \fadeOut ].refresh }.defer;
				})
				.put( \releaseSelf, {
					views[ \releaseSelf ].value = chain.releaseSelf.binaryValue;
					{ views[ \displayColor ].refresh; }.defer;
				})
				.put( \ugroup, {
					var groups;
					{ views.fillUGroup; }.defer;
				})
				.put( \global, {
					views[ \global ].value = chain.global.binaryValue;
				})
				.put( \addAction, {
					{ views[ \addAction ].item = chain.addAction }.defer;
				});
			};

		} {
			scoreController = SimpleController( score );
			scoreController
				.put( \displayColor, { { views[ \displayColor ].refresh; }.defer; } )
				.put( \allowPause, { views[ \allowPause ].value = score.allowPause.binaryValue } )
				.put( \startTime, {
					views[ \startTime ].value = score.startTime ? 0;
					views[ \startBar ].value = score.startTime ? 0;
				})
				.put( \lockStartTime, {
					views[ \lockStartTime ].value = score.lockStartTime.binaryValue;
				});
			score.changed( \startTime );
			score.changed( \lockStartTime );
		};

		[
			\gain, \muted, \startTime, \lockStartTime, \dur,
			\fadeIn, \fadeOut, \fadeInCurve, \fadeOutCurve,
			\releaseSelf,  \global, \addAction
		].do({ |item|
			controller.update(chain, item);
		});

		composite.getParents.last.findWindow !? _.toFrontAction_({
			this.makeCurrent;
		});

		uguis = this.makeUnitViews(units, margin, gap );

		nowBuildingChain = nil;
		nowBuildingUChainGUI = nil;
	}

	makeUnitHeader { |units, margin, gap|
		var comp, toc, header, min, io, defs, mapdefs, code;
		var notMassEdit, headerInset = 0;

		notMassEdit = chain.isKindOf( MassEditUChain ).not;

		comp = CompositeView( composite, (composite.bounds.width - (margin.x * 2))@16 )
			.resize_(2);

		if( notMassEdit && { this.canPackUnits }) {
			RoundButton( comp, 13 @ 13 )
				.border_(0)
				.background_( nil )
				.label_([ 'down', 'play' ])
				.hiliteColor_(nil)
				.value_( packUnits.binaryValue )
				.action_({ |bt|
					chain.handlingUndo = true; // don't add a state
					this.packUnits = bt.value.booleanValue;
				});
			headerInset = 14;
		};

		header = StaticText( comp, comp.bounds.moveTo(0,0).insetAll( headerInset, 0,0,0 ) )
				.applySkin( RoundView.skin )
				.string_( if( notMassEdit ) { " units" } { " units (accross multiple events)" } )
				.align_( \left )
				.resize_(2);

		if( notMassEdit ) { header.toolTip_( "units\n\nEach UChain is built out of one or more Units. Each unit is a" +
			"functional entity, creating sound, processing sound or providing audio input or output on your system." +
			"These units are \"chained\" together to form the UChain, where each unit passes it's audio on to the next.\n\n" +
			"The signal flow in this graphical representation is downwards; the sound is passed on from top to bottom." +
			"For each unit you can set arguments and you can change functionality by selecting another 'Udef' from the" +
			"menu when you click on the unit's header or from the Udefs window. Units can also deleted or" +
			"duplicated using the '+' and '-' controls, and new units can be inserted in the chain by clicking" +
			"on the narrow bars between the existing Units." ++
			"\n\nThe arguments of the units can be modulated using 'UMaps'. If you click on the parameter name" +
			"a menu will appear with the available UMap options for that parameter. The UMaps are themselves also a type of" +
			"Unit, which means that their parameters in turn can be modulated with UMaps again, allowing nested structures of" +
			"modulation.\n\nInformation about the functionality of each Udef can be seen in the tooltip when hovering the unit's" +
			"header, or in the code files of the Udefs, accessible via the Udefs window or the 'code' panel in this window"
		) };

		if( notMassEdit ) {
            io = SmoothButton( comp, Rect( comp.bounds.right - 40, 1, 40, 12 ) )
                .label_( "i/o" )
			    .toolTip_( "open input/output bus settings between units" )
                .radius_( 2 )
                .action_({
	                UChainIOGUI(
	                	this.window.name, originalBounds,
	                	chain, replaceCurrent: true
	                );
                }).resize_(3);

            code = SmoothButton( comp,
                    Rect( comp.bounds.right - (40 + 4 + 40), 1, 40, 12 ) )
                .label_( "code" )
			    .toolTip_( "open code edit options" )
                .radius_( 2 )
                .action_({
	                UChainCodeGUI(
	                	this.window.name, originalBounds,
	                	chain, replaceCurrent: true
	                );
                }).resize_(3);
		};

		toc = UPopUpMenu( comp, Rect( comp.bounds.right - (
					2 + 40 + 2 + 4 + 40 + (notMassEdit.binaryValue * (4 + 40 + 4 + 40))
					), 1, 42, 12
				) )
		.valueChangesString_( false )
		.string_( "toc" )
		.align_( \center )
		.resize_(3)
		.title_( "Table of contents" )
		.extraMenuActions_({
			[
				MenuAction( "(top)", { scrollView.visibleOrigin = 0@0 });
			] ++ uguis.collect({ |ugui|
				MenuAction( ugui.unit.fullDefName, {
					scrollView.visibleOrigin = 0 @ (ugui.view.bounds.top - 18);
				});
			});
		});

		defs = SmoothButton( comp,
				Rect( comp.bounds.right - (
					2 + 40 + (notMassEdit.binaryValue * (4 + 40 + 4 + 40))
					), 1, 42, 12
				)
			)
			.label_( "udefs" )
		    .toolTip_( "open Udefs window.\n\nYou can drag Udefs to the chain from there or open" +
			    "their corresponding code files."
		    )
			.radius_( 2 )
			.action_({
				UdefsGUI();
			}).resize_(3);

		CompositeView( comp, Rect( 0, 14, (composite.bounds.width - (margin.x * 2)), 2 ) )
			.background_( Color.black.alpha_(0.25) )
			.resize_(2);

	}

	makeUnitSubViews { |scrollView, units, margin, gap|
		var unitInitFunc;
		var comp, uview;
		var addLast, ug, header;
		var width;
		var notMassEdit;
		var scrollerMargin = 16;
		var realIndex = 0;
		var upatGUI, upatCtrls, upatHeader, upatComp;
		var uDefMenuFunc, plusButtonTask;
		var addBetweenColor;

		addBetweenColor = RoundView.skin.headerColor !? { |x|
			x.copy.alpha_( x.alpha / 2 )
		} ?? { Color.white.alpha_(0.25) };

		uDefMenuFunc = { |unit, action, hideAction, checkCategory|
			var uDefsList = [], ctrl, menu, checkedMenu, makeItem, includesChecked = false;
			var uDefsDict = ();

			Udef.all !? { |all|
				all.keys.asArray.sort.do({ |key|
					var category, index, udef;
					udef = all[ key ];
					category = udef.category;
					if( showPrivateUdefs or: { category != \private }) {
						uDefsDict[ udef.ioType ] = uDefsDict[ udef.ioType ] ?? {()};
						uDefsDict[ udef.ioType ][ category ] = uDefsDict[ udef.ioType ][ category ].add( udef );
					};
				});
			};

			recentUdefs = recentUdefs.select({ |item|
				item.name.notNil
			});

			if( recentUdefs.size > 0 ) {
				uDefsList = uDefsList.add( \recent );
				uDefsList = uDefsList.addAll( recentUdefs );
			};

			[ \generator, \modifier, \endpoint, \other ].do({ |key|
				uDefsList = uDefsList.add( key );
				uDefsDict[ key ] !? _.sortedKeysValuesDo({ |key, value|
					uDefsList = uDefsList.add( [ key, value ] );
				});
			});

			ctrl = { |menu, what|
				if( what === \aboutToHide ) {
					hideAction.value;
					menu.removeDependant( ctrl );
					menu.deepDestroy;
				};
			};

			makeItem = { |def|
				var checked;
				checked = unit !? { unit.def.name == def.name; } ? false;
				if( checked ) { includesChecked = true };
				case {
					def.isKindOf( MultiUdef ) && {
						def.getArgSpec( def.defNameKey ).private.not
					}
				} {
					Menu(
						MenuAction.separator( def.defNameKey.asString ),
						*def.getSpec( def.defNameKey ).list.collect({ |subdefkey|
							var subChecked = false;
							if( checked ) {
								subChecked = unit !? { unit.get( def.defNameKey ) == subdefkey } ? false;
							};
							MenuAction( subdefkey.asString, {
								action.value( def, [ def.defNameKey, subdefkey ] );
								recentUdefs.remove( def );
								recentUdefs = (recentUdefs ? []).addFirst( def )[..2];
								menu.removeDependant( ctrl );
								menu.deepDestroy;
							}).enabled_( subChecked.not ).font_( Font( Font.defaultSansFace, 12 ) );
						})
					).title_( if( checked ) { def.name.asString ++ " *" } { def.name.asString } )
					.font_( Font( Font.defaultSansFace, 12 ) );
				} {
					def.isKindOf( MultiMonoUdef )
				} {
					Menu(
						MenuAction.separator( 'numChannels' ),
						*def.getSpec( 'numChannels' ).list.collect({ |subdefkey|
							var subChecked = false;
							if( checked ) {
								subChecked = unit !? { unit.get( 'numChannels' ) == subdefkey } ? false;
							};
							MenuAction( subdefkey.asString, {
								action.value( def, [ 'numChannels', subdefkey ] );
								recentUdefs.remove( def );
								recentUdefs = (recentUdefs ? []).addFirst( def )[..2];
								menu.removeDependant( ctrl );
								menu.deepDestroy;
							}).enabled_( subChecked.not ).font_( Font( Font.defaultSansFace, 12 ) );
						})
					).title_( if( checked ) { def.name.asString ++ " *" } { def.name.asString } )
					.font_( Font( Font.defaultSansFace, 12 ) );
				} {
					MenuAction( def.name, {
						action.value( def );
						recentUdefs.remove( def );
						recentUdefs = (recentUdefs ? []).addFirst( def )[..2];
						menu.removeDependant( ctrl );
						menu.deepDestroy;
					}).enabled_( checked.not ).font_( Font( Font.defaultSansFace, 12 ) );
				};
			};

			menu = Menu( *uDefsList.collect({ |item, i|
				var submenu;
				includesChecked = false;
				case { item.isKindOf( Symbol ) } {
					if( checkCategory == item ) {
						checkedMenu = i;
					};
					MenuAction.separator( item.asString );
				} { item.isKindOf( Udef ) } {
					makeItem.value( item );
				} {
					submenu = Menu( *item[1].collect({ |def|
						makeItem.value( def );
					})).title_( if( includesChecked ) { item[0] ++ " *" } { item[0] } )
					.font_( Font( Font.defaultSansFace, 12 ) );
					if( includesChecked ) { checkedMenu = i };
					submenu;
				}
			})).font_( Font( Font.defaultSansFace, 12 ) );

			if( checkedMenu.notNil ) {
				menu.uFront( action: menu.actions[ checkedMenu ] ? nil );
			} {
				menu.uFront;
			};

			menu.addDependant( ctrl );
		};

		if( GUI.id == \qt ) { scrollerMargin = 20 };

		notMassEdit = chain.class != MassEditUChain;


		width = scrollView.bounds.width - scrollerMargin - (margin.x * 2);

		unitInitFunc = { |unit, what ...args|
			if( what === \init ) { // close all views and create new
				if( UMapSetChecker.stall != true ) { chain.changed( \units ); };
			};
		};

		if( units.size == 0 && { chain.isKindOf( MassEditUChain ).not } ) {
			comp = CompositeView( scrollView, width@100 )
				.resize_(2);

			header = StaticText( comp, comp.bounds.width @ 14 )
				.applySkin( RoundView.skin )
				.string_( " empty: click or drag unit or Udef here" )
				.background_( Color.yellow.alpha_(0.125) )
				.resize_(2)
				.font_(
					(RoundView.skin.tryPerform( \at, \font ) ??
						{ Font( Font.defaultSansFace, 12) }).boldVariant
				);

			uview = UDragBin( comp, comp.bounds.width @ 100 );
			uview.background_( Color.white.alpha_(0.25) );
			uview.color_( Color.gray(0.2) );

			uview.canReceiveDragHandler_({ |sink|
				var drg;
				drg = View.currentDrag;
				case { drg.isUdef }
					{ true }
					{ drg.isKindOf( UnitRack ) }
                    { true }
					{ [ Symbol, String ].includes( drg.class ) }
					{ Udef.all.keys.includes( drg.asSymbol ) }
					{ drg.isKindOf( U ) && { drg.isKindOf( UMap ).not } }
					{ true }
					{ false }
			})
			.receiveDragHandler_({ |sink, x, y|
					case { View.currentDrag.isKindOf( U ) } {
						chain.units = [ View.currentDrag.deepCopy ];
					}{ View.currentDrag.isUdef }{
						chain.units = [ U( View.currentDrag ) ];
					}{ View.currentDrag.isKindOf( UnitRack ) } {
                        chain.units = View.currentDrag.units;
                    }{   [ Symbol, String ].includes( View.currentDrag.class )  } {
						chain.units = [ U( View.currentDrag.asSymbol ) ];
					};
			});

			uview.mouseDownAction_({
				uDefMenuFunc.value(nil, { |def, args|
					chain.units = chain.units.add( U( def, args ) );
				}, {
					uview.background = addBetweenColor;
				}, \endpoint );
				uview.background =  addBetweenColor.copy.val_(0.5);
			});
		};

		if( chain.isKindOf( UPattern ) ) {

			upatComp = CompositeView( scrollView, width@14 )
			.background_( Color.green.alpha_(0.11) )
			.resize_(2);

			upatHeader = StaticText( upatComp, Rect(2,0, width, 14 ) )
			.applySkin( RoundView.skin )
			.string_( "UPattern" )
			.font_(
				(RoundView.skin.tryPerform( \at, \font ) ??
					{ Font( Font.defaultSansFace, 12) }).boldVariant
			);

			SmoothButton( upatComp, Rect( upatComp.bounds.right - (12 + 2) - (80 + 2), 1, 80, 12 ) )
			.label_( "as new score" )
			.radius_( 0 )
			.action_({ |bt|
				chain.asUScore.gui;
			});

			SmoothButton( upatComp,
					Rect( upatComp.bounds.right - (12 + 2), 1, 12, 12 ) )
				.label_( '-' )
			    .toolTip_( "convert the UPattern back into a regular UChain" )
				.action_({ |bt|
					var new, index;
					{
						new = chain.asUChain;
						if( parentScore.notNil ) {
							index = parentScore.indexOf( chain );
							if( index.notNil ) {
								parentScore.events[ index ] = new;
								parentScore.changed(\numEventsChanged);
								parentScore.changed(\events);
								parentScore.changed(\something);
							};
						};
					    this.chain = new;
						//new.gui( score: score );
					}.defer(0.1);
				}).resize_(3);

			upatGUI = UGUI(
				scrollView,
				scrollView.bounds.copy.width_(
					scrollView.bounds.width - scrollerMargin - (margin.x * 2)
				),
				chain,
			);
			upatGUI.mapSetAction = { chain.changed( \units ); };

			if( upatGUI.views.pattern.isKindOf( UMapGUI ) ) {
				upatGUI.views.pattern.removeButton.visible_(false)
			};

			[ \pattern, \fadeTimes ].do({ |key|
				var item;
				item = chain.perform( key );
				if( item.isUMap ) {
					upatCtrls = upatCtrls.add( SimpleController( item ).put( \init, { chain.changed( \units ) }) );
					upatCtrls = upatCtrls.addAll(
						item.getAllUMaps.collect({ |umap|
							SimpleController( umap ).put( \init, { chain.changed( \units ) });
						})
					);
				};
			});

			upatHeader.onClose_({
				upatCtrls.do(_.remove);
			});
		} {
			if( chain.isKindOf( MassEditUChain ).not ) {
				upatComp = CompositeView( scrollView, width@14 )
				.background_( Color.green.alpha_(0.055) )
				.resize_(2);

				upatHeader = StaticText( upatComp, Rect(2,0, width, 14 ) )
				.applySkin( RoundView.skin )
				.string_( "UPattern" )
				.stringColor_( (RoundView.skin.stringColor ?? { Color.black }).copy.alpha_(0.5) )
				.font_(
					(RoundView.skin.tryPerform( \at, \font ) ??
						{ Font( Font.defaultSansFace, 12) }).boldVariant
				);

				SmoothButton( upatComp,
					Rect( upatComp.bounds.right - (12 + 2), 1, 12, 12 ) )
				.label_( '+' )
				.toolTip_( "convert this UChain into an UPattern\n\n" ++
					"UPatterns can generate multiple events based on settings of a single UChain" +
					"When UPattern is enabled extra UMaps become available in the 'pattern' section" +
					"These UMaps can create new values per event. Also a 'pattern' parameter becomes" +
					"visible, allowing to determine the timing of the events in various ways"
				)
				.action_({ |bt|
					var new, index;
					{
						new = chain.asUPattern;
						if( parentScore.notNil ) {
							index = parentScore.indexOf( chain );
							if( index.notNil ) {
								parentScore.events[ index ] = new;
								parentScore.changed(\numEventsChanged);
								parentScore.changed(\events);
								parentScore.changed(\something);
							};
						};
						this.chain = new;
						//new.gui( score: score );
					}.defer(0.1);
				}).resize_(3);
			} {
				if( chain.uchains.any(_.isKindOf( UPattern ) ) ) {
					var upats;

					upatComp = CompositeView( scrollView, width@14 )
					.background_( Color.green.blend( Color.yellow, 0.75 ).alpha_(0.11) )
					.resize_(2);

					upatHeader = StaticText( upatComp, Rect(2,0, width, 14 ) )
					.applySkin( RoundView.skin )
					.string_( "UPattern" )
					.font_(
						(RoundView.skin.tryPerform( \at, \font ) ??
							{ Font( Font.defaultSansFace, 12) }).boldVariant
					);

					upats = chain.uchains.select( _.isKindOf( UPattern ) );
					if( upats.size == 1 ) {
						upats = upats[0]
					} {
						upats = MassEditUPattern( upats );
					};

					upatGUI = UGUI(
						scrollView,
						scrollView.bounds.copy.width_(
							scrollView.bounds.width - scrollerMargin - (margin.x * 2)
						),
						upats,
					);
					upatGUI.mapSetAction = { chain.changed( \units ); };

					if( upatGUI.views.pattern.isKindOf( UMapGUI ) ) {
						upatGUI.views.pattern.removeButton.visible_(false)
					};

					[ \pattern, \fadeTimes ].do({ |key|
						var item;
						item = upats.perform( key );
						if( item.isUMap ) {
							upatCtrls = upatCtrls.add( SimpleController( item ).put( \init, { chain.changed( \units ) }) );
							upatCtrls = upatCtrls.addAll(
								item.getAllUMaps.collect({ |umap|
									SimpleController( umap ).put( \init, { chain.changed( \units ) });
								})
							);
						};
					});

					upatHeader.onClose_({
						upatCtrls.do(_.remove);
					});
				};
			}
		};

		ug = units.collect({ |unit, i|
			var header, comp, infoString, uview, plus, min, defs, io;
			var addBefore, indexLabel, ugui;
			var currentUMaps;
			var massEditWindowButton;

			indexLabel = realIndex.asString;

			if( notMassEdit && { unit.isKindOf( MassEditU ) } ) {
				realIndex = realIndex + unit.units.size;
				indexLabel = indexLabel ++ ".." ++ (realIndex -1);
			} {
				realIndex = realIndex + 1;
			};

			addBefore = UDragBin( scrollView, width@7 )
				.color_( Color.gray(0.2) )
				.resize_(2);

			if( notMassEdit ) {
				addBefore.background_( addBetweenColor );
				addBefore.canReceiveDragHandler_({ |sink|
						var drg;
						drg = View.currentDrag;
						case { drg.isUdef }
							{ true }
							{ drg.isKindOf( UnitRack ) }
	                        { true }
							{ [ Symbol, String ].includes( drg.class ) }
							{ Udef.all.keys.includes( drg.asSymbol ) }
							{ drg.isKindOf( U ) && { drg.isKindOf( UMap ).not } }
							{ true }
							{ false }
					})
					.receiveDragHandler_({ |sink, x, y|
							var ii;
							case { View.currentDrag.isKindOf( U ) } {
								ii = units.indexOf( View.currentDrag );
								if( ii.notNil ) {
									units[ii] = nil;
									units.insert( i, View.currentDrag );
									this.setUnits( units.select(_.notNil) );
								} {
									this.setUnits(
										units.insert( i, View.currentDrag.deepCopy )
									);
								};
							} { View.currentDrag.isUdef } {
								this.setUnits( units.insert( i, U( View.currentDrag ) ) );
							}{ View.currentDrag.isKindOf( UnitRack ) } {
								this.setUnits(
									units[..i-1] ++ View.currentDrag.units ++ units[i..]
								);
	                           }{   [ Symbol, String ].includes( View.currentDrag.class )  } {
								this.setUnits(
									units.insert( i, U( View.currentDrag.asSymbol ) )
								);
							};
					});

				addBefore.mouseDownAction_({
					uDefMenuFunc.value(nil, { |def, args|
						this.setUnits( units.insert( i, U( def, args ) ) );
					}, {
						addBefore.background = addBetweenColor;
					}, if( i == 0 ) { \generator } { \modifier } );
					addBefore.background = addBetweenColor.copy.val_(0.5);
				});

				addBefore.mouseUpAction_({
					addBefore.background = addBetweenColor;
				});

			} {
				if( chain.canInsertAt( i ) ) {
					addBefore.background = addBetweenColor;

					addBefore.mouseDownAction_({
						uDefMenuFunc.value(nil, { |def, args|
							chain.insert( i, def , args );
						}, {
							addBefore.background = addBetweenColor;
						}, \modifier );
						addBefore.background = addBetweenColor.copy.val_(0.5);
					});

					addBefore.mouseUpAction_({
						addBefore.background = addBetweenColor;
					});

				} {
					addBefore.canFocus = false;
				};
			};

			comp = CompositeView( scrollView, width@14 )
				.background_( if( notMassEdit )
				    { RoundView.skin.headerColor ?? { Color.white.alpha_(0.5) } }
					{ Color.yellow.alpha_( 0.165 ) }
				)
				.resize_(2);

			SmoothButton( comp, Rect( 2, 0, 12, 12 ) )
				.label_( ['down', 'play'] )
				.border_( 0 )
				.background_( nil )
				.hiliteColor_( nil )
				.value_( unit.guiCollapsed.binaryValue )
				.mouseUpAction_({ |bt,x,y,mod|
				    var bool;
				    bool = bt.value.booleanValue.not;
				    if( mod == 524288 ) { // option / alt key; collapse all
					    UMapSetChecker.stall = true;
					    units.do({ |unit|
						    unit.guiCollapsed = bool;
						    unit.getAllUMaps.do({ |umap|
							    umap.guiCollapsed = bool;
						    });
					    });
					    UMapSetChecker.stall = false;
					    unit.changed( \init );
				    } {
					    unit.guiCollapsed = bt.value.booleanValue.not;
				    };
				});

			header = StaticText( comp, comp.bounds.moveTo(0,0).insetAll( 16,0,0,0) )
				.applySkin( RoundView.skin )
				.string_(
					" " ++ indexLabel ++ ": " ++
					if( unit.def.class == LocalUdef ) { "[Local] " } { "" } ++
					unit.fullDefName
				)
				.resize_(2)
				.font_(
					(RoundView.skin.tryPerform( \at, \font ) ??
						{ Font( Font.defaultSansFace, 12) }).boldVariant
				);

			uview = UDragBin( comp, comp.bounds.moveTo(0,0).insetAll( 16,0,48,0) );
			uview.color_( Color.gray(0.2) );

			if( showInfoStrings ) {
				infoString = unit.def !? _.getInfoString;

				if( infoString.notNil  ) {
					uview.toolTip_( infoString );
				};
			};

			uview.mouseDownAction_({
				uDefMenuFunc.value(unit, { |def, args|
					var umcWas;
					umcWas = UMapSetChecker.stall;
					UMapSetChecker.stall = true;
					if( unit.def != def ) {
						unit.def = def;
					};
					if(args.notNil) {
						if( unit.isKindOf( MassEditU ) ) {
							args = args.collect({ |item, i|
								if( i.odd ) {
									Array.fill( unit.units.size, { item });
								} {
									item;
								}
							})
						};
						unit.set(*args);
					};
					UMapSetChecker.stall = false;
					unit.changed( \init );
				}, { uview.background = nil; });
				uview.background = Color.gray(0.3).alpha_(0.5);
			});

			uview.mouseUpAction_({
				uview.background = nil;
			});

			uview.canReceiveDragHandler_({ |sink|
				var drg;
				drg = View.currentDrag;
				case { drg.isUdef }
					{ true }
					{ drg.isKindOf( UnitRack ) }
                        { true }
					{ [ Symbol, String ].includes( drg.class ) }
					{ Udef.all.keys.includes( drg.asSymbol ) }
					{ drg.isKindOf( U ) && { drg.isKindOf( UMap ).not } }
					{ true }
					{ false }
			})
			.receiveDragHandler_({ |sink, x, y|
				var u, ii;
				case { View.currentDrag.isKindOf( U ) } {
					u = View.currentDrag;
					ii = units.indexOf( u );
					if( ii.notNil ) {
						units[ii] = unit;
						units[i] = u;
					} {
						units[ i ] = u.deepCopy;
					};
					this.setUnits( units );

				} { View.currentDrag.isKindOf( UnitRack ) } {
                        this.setUnits( units[..i-1] ++ View.currentDrag.units ++ units[i+1..] );
                    } { View.currentDrag.isUdef } {
					unit.def = View.currentDrag;
				} {   [ Symbol, String ].includes( View.currentDrag.class )  } {
					unit.def = View.currentDrag.asSymbol.asUdef;
				};
			});

			if( unit.isKindOf( MassEditU ) ) {
				massEditWindowButton = SmoothButton( comp,
					Rect( comp.bounds.right -
						((18 + 4) + if( notMassEdit){12 + 4 + 12 + 4 + 12}{0}),
						1, 18, 12 )
					)
					.label_( 'up' )
					.radius_( 2 )
					.action_({
						var allUnits, userClosed = true;
						if( massEditWindow.notNil && { massEditWindow.isClosed.not }) {
							massEditWindow.close;
						};
						RoundView.pushSkin( skin );
						massEditWindow = Window( unit.defName,
							this.window.bounds.moveBy( this.window.bounds.width + 10, 0 ),
							scroll: true ).front;
						massEditWindow.addFlowLayout;
						comp.onClose_({
							if( massEditWindow.notNil && { massEditWindow.isClosed.not }) {
								userClosed = false;
								massEditWindow.close;
							};
						});
						allUnits = unit.units.collect({ |item, ii|
							var ugui;
							if( notMassEdit ) { ii = ii + (realIndex - unit.units.size) };
							StaticText( massEditWindow,
									(massEditWindow.bounds.width - 8 - scrollerMargin) @ 14 )
								.applySkin( RoundView.skin )
								.string_( " " ++ ii ++ ": " ++ item.defName )
								.background_( Color.white.alpha_(0.5) )
								.resize_(2)
								.font_(
									(RoundView.skin.tryPerform( \at, \font ) ??
										{ Font( Font.defaultSansFace, 12) }).boldVariant
								);
							massEditWindow.view.decorator.nextLine;
							ugui = item.gui( massEditWindow, (massEditWindow.bounds.width - 8 - scrollerMargin) @ 14 );
							ugui.mapSetAction = {
								chain.changed( \units );
							};
							[ item ] ++ item.getAllUMaps;
						}).flatten(1);
						allUnits.do({ |item|
							item.addDependant( unitInitFunc )
						});
						massEditWindowIndex = i;
						massEditWindow.onClose_({ |win|
							allUnits.do(_.removeDependant(unitInitFunc));
						    if( userClosed && {
							    (massEditWindow !? _.view) === win
						    } ) {
							massEditWindowIndex = nil;
							};
						});
						RoundView.popSkin( skin );
					}).resize_(3);

				if( massEditWindowIndex == i ) {
					massEditWindowButton.doAction;
				};
			} {
				if( massEditWindowIndex == i ) {
					massEditWindowIndex = nil;
				};
			};

			if( notMassEdit ) {

				UDragSource( comp, Rect( comp.bounds.right - (12 + 4 + 12 + 4 + 12 ), 1, 12, 12 ) )
					.beginDragAction_({
						{ UChainGUI.current.view.refresh }.defer(0.1);
						unit;
					})
					.background_( Color.gray(0.8,0.8) )
					.string_( "" );

				min = SmoothButton( comp,
							Rect( comp.bounds.right - (12 + 4 + 12), 1, 12, 12 ) )
						.label_( '-' )
						.action_({
							var u = unit;
							if( u.isKindOf( MassEditU ) ) {
								u = u.units.last;
							};
					        u.deactivateUMaps;
							chain.units = chain.units.select(_ != u);
						}).resize_(3);

				if( units.size == 1 ) {
					min.enabled = false;
				};

				plus = SmoothButton( comp,
					Rect( comp.bounds.right - (12 + 2), 1, 12, 12 ) )
				.label_( '+' )
				.mouseDownAction_({
					plusButtonTask.stop;
					plusButtonTask = {
						var actions, currentSize, openAction;
						0.5.wait;
						currentSize = if( unit.isKindOf( MassEditU ) ) {
							unit.units.size;
						} {
							1
						};
						actions = [1,2,3,4,5,6,7,8,10,12,16,24,32].collect({ |item|
							var act;
							act = MenuAction( item, {
								if( unit.isKindOf( MassEditU ) ) {
									if( unit.units.size != item ) {
										case { item == 1 } {
											unit = unit.units.first;
											units.put( i, unit );
										} { item > (unit.units.size) } {
											(item - unit.units.size).do({ |ii|
												unit.units_(
													unit.units.add(
														unit.units.last
														.deepCopy.increaseIOs
													), false
												);
											});
										} {
											unit.units_(
												unit.units[..item-1], false
											);
										};
										this.setUnits( units );
									};
								} {
									if( item != 1 ) {
										(item - 1).do({ |ii|
											units = units
											.insert( i+1+ii,
												unit.deepCopy.increaseIOs( ii+1 )
											);
										});
										this.setUnits( units );
									};
								};
							});
							if( item == currentSize ) {
								act.enabled = false;
								openAction = act;
							};
							act;
						});
						Menu(
							MenuAction.separator( "numCopies" ),
							*actions
						).uFront( action: openAction );
						plusButtonTask = nil;
					}.fork( AppClock )
				})
				.action_({
					var copy;
					if( plusButtonTask.notNil ) {
						plusButtonTask.stop;
						plusButtonTask = nil;
						if( unit.isKindOf( MassEditU ) ) {
							unit.units_( unit.units.add( unit.units.last.deepCopy.increaseIOs ), false );
						} {
							units = units.insert( i+1, unit.deepCopy.increaseIOs );
						};
						this.setUnits( units );
					};
				}).resize_(3);

				if(  unit.isKindOf( MassEditU ).not && { unit.audioOuts.size > 0 } ) {					SmoothButton( comp,
						Rect( comp.bounds.right - (45 + 4 + 12 + 4 + 12 + 4 + 12),
							1, 45, 12 )
						)
						.label_( "bounce" )
						.radius_( 2 )
						.action_({
							Dialog.savePanel( { |path|
								chain.bounce( chain.units.indexOf( unit ), path );
							});
						}).resize_(3);
				};
			} {
				if( chain.canRemoveAt( i ) ) {
					min = SmoothButton( comp,
						Rect( comp.bounds.right - 38, 1, 12, 12 ) )
					.label_( '-' )
					.action_({
						chain.removeAt( i ).deactivateUMaps;
					}).resize_(3);
				};
			};

			unit.addDependant( unitInitFunc );
			currentUMaps = unit.getAllUMaps;
			currentUMaps.do(_.addDependant( unitInitFunc ));
			header.onClose_({
				unit.removeDependant( unitInitFunc );
				currentUMaps.do(_.removeDependant( unitInitFunc ));
			});
			ugui = unit.gui( scrollView,
				scrollView.bounds.copy.width_(
					scrollView.bounds.width - scrollerMargin - (margin.x * 2)
				)
			);
			ugui.mapSetAction = { chain.changed( \units ) };
			ugui;
		});

		if( notMassEdit && { units.size > 0 } ) {
			addLast = UDragBin( scrollView, width@7 )
				.resize_(2)
				.color_( Color.gray(0.2) )
			    .background_( addBetweenColor )
				.canFocus_(false);

			addLast.canReceiveDragHandler_({ |sink|
					var drg;
					drg = View.currentDrag;
					case { drg.isUdef }
						{ true }
						{ drg.isKindOf( UnitRack ) }
                        { true }
						{ [ Symbol, String ].includes( drg.class ) }
						{ Udef.all.keys.includes( drg.asSymbol ) }
						{ drg.isKindOf( U ) && { drg.isKindOf( UMap ).not } }
						{ true }
						{ false }
				})
				.receiveDragHandler_({ |sink, x, y|
						var ii;
						case { View.currentDrag.isKindOf( U ) } {
							ii = units.indexOf( View.currentDrag );
							if( ii.notNil ) {
								units[ii] = nil;
								this.setUnits( units.select(_.notNil) ++
									[ View.currentDrag ] );
							} {
								this.setUnits( units ++ [ View.currentDrag.deepCopy ] );
							};

						} { View.currentDrag.isUdef } {
							chain.units = chain.units ++ [ U( View.currentDrag ) ];
						}{ View.currentDrag.isKindOf( UnitRack ) } {
                            chain.units = chain.units ++ View.currentDrag.units;
                        }{   [ Symbol, String ].includes( View.currentDrag.class )  } {
							chain.units = chain.units ++ [ U( View.currentDrag.asSymbol ) ];
						};
				});

			addLast.mouseDownAction_({
				uDefMenuFunc.value(nil, { |def, args|
					chain.units = chain.units ++ [ U( def, args ) ];
				}, {
					addLast.background = addBetweenColor;
				}, \endpoint );
				addLast.background =  addBetweenColor.copy.val_(0.5);
			});

			addLast.mouseUpAction_({
				addLast.background = addBetweenColor;
			});
		};

		if( scrollViewOrigin.notNil ) {
			if( GUI.id == \qt ) {
				{
					scrollView.visibleOrigin = scrollViewOrigin; 					scrollViewOrigin = nil;
				}.defer(0.1);
			} {
				scrollView.visibleOrigin = scrollViewOrigin; 				scrollViewOrigin = nil;
			};
		};

		^ug;

	}

	makeUnitViews { |units, margin, gap|

		var scrollView, presetManagerHeight = 0, notMassEdit;

		notMassEdit = chain.class != MassEditUChain;

		if( notMassEdit ) {
			presetManagerHeight = PresetManagerGUI.getHeight + 12;
		};

		this.makeUnitHeader( units, margin, gap );

		composite.decorator.nextLine;

		scrollView = ScrollView( composite,
			(composite.bounds.width)
				@ (composite.bounds.height -
					( presetManagerHeight ) -
					( composite.decorator.top )
				)
		);

		scrollView
			.hasBorder_( false )
			.hasHorizontalScroller_( false )
			.autohidesScrollers_( false )
			.resize_(5)
			.addFlowLayout( margin, gap );

		this.scrollView = scrollView;

		if( notMassEdit ) {

			CompositeView( composite, (composite.bounds.width - (margin.x * 2)) @ 2 )
				.background_( Color.black.alpha_(0.25) )
				.resize_(8);

			presetView = PresetManagerGUI(
				composite,
				composite.bounds.width @ PresetManagerGUI.getHeight,
				UChain.presetManager,
				chain
			).resize_(8)
		};

		^this.makeUnitSubViews( scrollView, units, margin, gap );
	}

	makeColorEditorWindow { |views, score, chain, parentScore|
		var isOverview, spec;
		if( views[ \colorEditor ].isNil ) {
			if( score.notNil or: { chain.isKindOf( MassEditUChain ).not }) {
				if( (score ? chain).displayColor.isNil or: {
					[ USoundFileOverview, Color ].includes( (score ? chain).displayColor.class );
				} ) {
					isOverview = { (score ? chain).displayColor.isKindOf( USoundFileOverview ); };
					RoundView.pushSkin( skin );
					spec = ColorSpec( (score ? chain).getTypeColor.asColor );
					views[ \colorEditor ] = spec.makeView( "UChain displayColor",
						action: { |vws, color|
							if( isOverview.value ) {
								(score ? chain).displayColor = (score ? chain).displayColor.color_( color );
							} {
								(score ? chain).displayColor = color;
							}
						}
					);
					if( score.isNil && { chain.isMemberOf( UChain ) }) {
						views[ \overviewButton ] = SmoothButton( views[ \colorEditor ].view.findWindow,
							Rect( 4, views[ \colorEditor ].view.bounds.bottom - 4,
								views[ \colorEditor ].view.bounds.width - 4 - 22, 18
							)
						)
						.action_({
							if( isOverview.value ) { chain.displayColor.clear; };
							USoundFileOverview().fromUChain( chain, nil, nil, true, parentScore );
						})
						.label_( if( isOverview.value ) { "refresh overview" } { "calculate overview" });
						views[ \overviewRemoveButton ] = SmoothButton(  views[ \colorEditor ].view.findWindow,
							Rect( views[ \colorEditor ].view.bounds.width - 18,
								views[ \colorEditor ].view.bounds.bottom - 4, 18, 18
						) )
						.action_({
							if( isOverview.value ) {
								chain.displayColor.clear;
								chain.displayColor = chain.displayColor.color;
							} {
								chain.displayColor = nil;
							};
						}).label_( '-' );
						views[ \overviewRemoveButton ].enabled_( isOverview.value );
					};
					views[ \colorEditor ].view.onClose = {
						views[ \colorEditor ] = nil
					};
					views[ \setColorEditor ] = {
						views[ \colorEditor ] !? { spec.setView( views[ \colorEditor ], (score ? chain).getTypeColor.asColor ) };
						views[ \overviewRemoveButton ] !? _.enabled_( isOverview.value );
						views[ \overviewButton ] !? _.label_(
							if( isOverview.value ) { "refresh overview" } { "calculate overview" }
						);
						views[ \overviewButton ] !? _.refresh;
					};
					RoundView.popSkin;

				} {
					"no editor available for %\n".postf(
						(score ? chain).displayColor.class
					);
				};
			} {
				if( chain.getTypeColors.size > 0 ) {
					isOverview = { chain.uchains.any({ |c| c.displayColor.isKindOf( USoundFileOverview ); }) };
					spec = ColorArraySpec( chain.getTypeColors );
					RoundView.pushSkin( skin );
					views[ \colorEditor ] = spec.makeView( "MassEditUChain displayColors",
						action: { |vws, colors|
							chain.setDisplayColors( colors );
						}
					);
					if( score.isNil && { chain.uchains.any( _.isMemberOf( UChain ) ) } ) {
						views[ \overviewButton ] = SmoothButton( views[ \colorEditor ].view.findWindow,
							Rect( 4, views[ \colorEditor ].view.bounds.bottom - 4,
								views[ \colorEditor ].view.bounds.width - 4 - 22, 18
							)
						)
						.action_({
							if( parentScore.isNil or: { parentScore.filePath.isNil } ) {
								ULib.savePanel({ |pth|
									pth = pth.dirname +/+ "overviews";
									File.makeDir( pth );
									{
										var cond = Condition(false);
										chain.uchains.select( _.isMemberOf( UChain ) ).do({ |evt, i|
											var usn;
											usn = USoundFileOverview();
											usn.fromUChain( evt,
												usn.createFileNameFromPath( pth, parentScore !? { |x| x.events.indexOf( chain ) } ? 0 ),
												{ cond.test = true; cond.signal },
												true, parentScore
											);
											cond.wait;
											evt.changed( \displayColor );
											cond.test = false;
										});
										"done creating overviews".postln;
									}.fork;
								});
							} {
								{
									var cond = Condition(false);
									chain.uchains.select( _.isMemberOf( UChain ) ).do({ |evt, i|
										USoundFileOverview().fromUChain( evt, nil,
											{ cond.test = true; cond.signal },
											true, parentScore
										);
										cond.wait;
										evt.changed( \displayColor );
										cond.test = false;
									});
									"done creating overviews".postln;
								}.fork;
							};
						})
						.label_( if( isOverview.value ) { "refresh overviews" } { "calculate overviews" });
						views[ \overviewRemoveButton ] = SmoothButton(  views[ \colorEditor ].view.findWindow,
							Rect( views[ \colorEditor ].view.bounds.width - 18,
								views[ \colorEditor ].view.bounds.bottom - 4, 18, 18
						) ).label_( '-' );
						views[ \overviewRemoveButton ].enabled_( isOverview.value );
					};
					views[ \colorEditor ].view.onClose = {
						views[ \colorEditor ] = nil
					};
					views[ \setColorEditor ] = {
						views[ \colorEditor ] !? {
							spec.setView( views[ \colorEditor ], chain.getTypeColors );
							views[ \overviewRemoveButton ] !? _.enabled_( isOverview.value );
							views[ \overviewButton ] !? _.label_(
								if( isOverview.value ) { "refresh overviews" } { "calculate overviews" }
							);
							views[ \overviewButton ] !? _.refresh;
						};
					};
					RoundView.popSkin;
				} {
					"no editor available\n".postf();
				};
			};
		} {
			views[ \colorEditor ].view.findWindow.front;
		};
	}

	remove {
		composite.remove;
	}

	window {
		^composite.getParents.last.findWindow;
	}
	windowName {
		^this.window.name;
	}

	windowName_ { |name|
		this.window.name = name;
	}

	close {
		if( composite.isClosed.not ) {
			composite.getParents.last.findWindow.close;
		};
	}

	resize_ { |resize| composite.resize_(resize) }

	font_ { |font| uguis.do({ |vw| vw.font = font }); }

	view { ^composite }

	fixPopUpMenus {
		var func;
		if( Platform.ideName == "scqt" ) {
			func = { |vw|
				if( vw.isKindOf( PopUpMenu ) ) {
					vw.mouseWheelAction = { |vw|
						vw.enabled_( false );
						{ vw.enabled_( true ) }.defer(0.1);
					};
				} {
					vw.children.do({ |child| func.( child ) });
				};
			};
			func.( this.window.view );
		}
	}
}

+ UChain {
	gui { |parent, bounds, score| ^UChainGUI( parent, bounds, this, score ) }
}