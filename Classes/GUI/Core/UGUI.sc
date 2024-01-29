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

UGUI {

	classvar <>nowBuildingUnit;

	var <unit;

	var <parent, <composite, <views, <controller;
	var <viewHeight = 14, <labelWidth = 80;
	var <>action;
	var <>mapSetAction, <>mapCheckers;

	*new { |parent, bounds, unit|
		^super.newCopyArgs( unit ).init( parent, bounds );
	}

	init { |inParent, bounds|
		parent = inParent;
		if( parent.isNil ) { parent = Window( unit.defName ).front };
		this.makeViews( bounds );
	}

	*getHeight { |unit, viewHeight, margin, gap|
		viewHeight = viewHeight ? 14;
		margin = margin ?? {0@0};
		gap = gap ??  {4@4};
		^(margin.y * 2) + ( this.viewNumLines( unit ) * (viewHeight + gap.y) ) - gap.y;
	}

	*viewNumLines { |unit|
		^(unit.argSpecsForDisplay ? [])
			.collect({|x|
				if( unit[ x.name ].isKindOf( UMap ) ) {
					UMapGUI.viewNumLines( unit[ x.name ] );
				} {
					x.spec.viewNumLines
				};
			}).sum;
	}

	makeViews { |bounds|
		this.prMakeViews( bounds );
	}

	prMakeViews { |bounds|
		var margin = 0@0, gap = 4@4;

		nowBuildingUnit = unit;

		if( bounds.isNil ) {
			bounds = parent.asView.bounds.insetBy(4,4);
			if( parent.asView.class.name == \SCScrollTopView ) {
				bounds.width = bounds.width - 16;
			};
			if( parent.asView.class.name == \QScrollTopView ) {
				bounds.width = bounds.width - 20;
			};
		};
		bounds = bounds.asRect;
		bounds.height = this.class.getHeight( unit, viewHeight, margin, gap );
		controller = SimpleController( unit );

		if( unit.isKindOf( MassEditU ) ) {
			mapCheckers = unit.units.collect({ |unit|
				if( unit.isKindOf( U ) ) {
					UMapSetChecker( unit, { mapSetAction.value( this ) } );
				} { nil };
			}).select(_.notNil);
			unit.connect;
		} {
			mapCheckers = [ UMapSetChecker( unit, { mapSetAction.value( this ) } ) ];
		};

		composite = CompositeView( parent, bounds ).resize_(2);
		composite.addFlowLayout( margin, gap );
		composite.onClose = {
			if( unit.class == MassEditU ) {
				unit.disconnect;
			};
			controller.remove;
			mapCheckers.do(_.remove);
		 };

		 this.makeSubViews( bounds );

		 nowBuildingUnit = nil;
	}

	makeSubViews { |bounds|
		views = ();

		this.makeHeader(bounds);

		if( GUI.id == \cocoa ) { View.currentDrag = nil; };

		unit.argSpecsForDisplay.do({ |argSpec, i|
			var vw, key, value;
			var decLastPos;
			var umapdragbin;
			var viewNumLines;

			key = argSpec.name;
			value = unit.at( key );

			if( argSpec.notNil && (argSpec.spec.viewNumLines != 0)) {
				if( value.isUMap ) {
					vw = UMapGUI( composite, composite.bounds.insetBy(0,-24), value );
					vw.parentUnit = unit;
					vw.mapSetAction = { mapSetAction.value( this ) };
					vw.removeAction = { |umap|
						UMapSetChecker.stall = true;
						umap.stop;
						unit.removeUMap( key );
						UMapSetChecker.stall = false;
					};
				} {
					vw = ObjectView( composite, bounds.width * argSpec.width, unit, key,
						argSpec.spec, controller,
						switch( argSpec.mode,
						 	\nonsynth, { key ++ " (l)" },
						 	\init, { key ++ " (i)" }
						 )
					);
					vw.testValue = { |value| value.isKindOf( UMap ).not };
					vw.action = { action.value( this, key, value ); };

					if( [ \nonsynth ].includes(argSpec.mode).not ) {						viewNumLines = argSpec.spec.viewNumLines;

						composite.decorator.shift( (bounds.width * argSpec.width.neg) - 3, 0 );

						umapdragbin = UDragBin( composite, labelWidth @ viewHeight )
							.canReceiveDragHandler_({ |vw, x,y|
								(View.currentDrag.isKindOf( UMapDef ) && {
									unit.canUseUMap( key, View.currentDrag );
								}) or: (View.currentDrag.isKindOf( UMap ) && {
									unit.canUseUMap( key, View.currentDrag.def );
								});
							});

						umapdragbin.receiveDragHandler_({
							UMapSetChecker.stall = true;
							unit.insertUMap( key, View.currentDrag );
							UMapSetChecker.stall = false;
						});

						umapdragbin.mouseDownAction_({
							this.makeUMapDefMenu({ |def| unit.canUseUMap( key, def ); }, { |def| unit.insertUMap( key, def ); }, { umapdragbin.background = nil; });
							umapdragbin.background = Color.blue(0.9).alpha_(0.25);
						});

						umapdragbin.mouseUpAction_({
							umapdragbin.background = nil;
						});

						composite.decorator.shift( (bounds.width * argSpec.width) - (labelWidth + 3), 0 );
					};
				};
				views[ key ] = vw;
			}

		});

		if( views.size == 0 ) {
			controller.remove;
			mapCheckers.do(_.remove);
		};
	}

	makeHeader { }

	makeUMapDefMenu { |test, action, hideAction, matchTest|
		var uDefsList = [], ctrl, menuList, menu;
		var uDefsDict = ();

		UMapDef.all !? { |all|
			all.keys.asArray.sort.do({ |key|
				var category, index, udef;
				udef = all[ key ];
				if( test.value( udef ) ) {
					category = udef.category;
					if( category != \private ) {
						uDefsDict[ udef.defType ] = uDefsDict[ udef.defType ] ?? {()};
						uDefsDict[ udef.defType ][ category ] = uDefsDict[ udef.defType ][ category ].add( udef );
					};
				};
			});
		};

		[ \dynamic, \mixed, \control, \value, \pattern ].do({ |key|
			if( uDefsDict.keys.includes( key ) ) {
				uDefsList = uDefsList.add( key );
				uDefsDict[ key ] !? _.sortedKeysValuesDo({ |key, value|
					uDefsList = uDefsList.add( [ key, value ] );
				});
			};
		});

		ctrl = { |menu, what|
			if( what === \aboutToHide ) {
				hideAction.value;
				menu.removeDependant( ctrl );
				menu.destroy;
			};
		};

		menuList = uDefsList.collect({ |item|
			var includesChecked = false;
			if( item.isKindOf( Symbol ) ) {
				MenuAction.separator( item.asString );
			} {
				Menu( *item[1].collect({ |def|
					var checked;
					checked = matchTest.value( def ) ? false;
					if( checked ) { includesChecked = true; };
					MenuAction( def.name, {
						action.value( def );
						menu.removeDependant( ctrl );
						menu.destroy;
					}).enabled_( checked.not ).font_( Font( Font.defaultSansFace, 12 ) );
				})).title_( if( includesChecked ) { item[0] ++ " *" } { item[0] } );
			}
		});

		if( menuList.size == 2 ) {
			menu = menuList[1];
		} {
			menu = Menu( *menuList ).font_( Font( Font.defaultSansFace, 12 ) );
		};

		menu.front;

		^menu.addDependant( ctrl );
	}

	resize_ { |resize| composite.resize_(resize) }
	reset { unit.reset }

	font_ { |font| views.values.do({ |vw| vw.font = font }); }
	viewHeight_ { |height = 16|
		views.values.do({ |vw| vw.view.bounds = vw.view.bounds.height_( height ) });
		composite.decorator.reFlow( composite );
	}
	labelWidth_ { |width=50|
		labelWidth = width;
		views.values.do(_.labelWidth_(width));
	}

	view { ^composite }
}

UMapSetChecker {

	classvar <>stallAction;
	classvar <stall;

	var unit, <>action, argDict;

	*new { |unit, action|
		^super.newCopyArgs( unit, action ).init;
	}

	*stall_ { |bool = true|
		if( bool == false ) { stallAction.value; stallAction = nil; };
		stall = bool;
	}

	init {
		argDict = ();
		unit.args.pairsDo({ |key, value|
			if( value.isUMap ) {
				argDict[ key ] = value;
			};
		});
		unit.addDependant( this );
	}

	remove { unit.removeDependant( this ) }

	update { |obj, key, value|
		if( value.isUMap ) {
			if( argDict[ key ] !== value ) {
				argDict[ key ] = value;
				if( stall == true ) {
					stallAction = action;
				} {
					action.value( this, key, value );
				};
			};
		} {
			if( argDict[ key ].notNil ) {
				argDict[ key ] = nil;
				if( stall == true ) {
					stallAction = action;
				} {
					action.value( this, key, value );
				};
			};
		}
	}
}

+ U {
	gui { |parent, bounds| ^UGUI( parent, bounds, this ) }
}