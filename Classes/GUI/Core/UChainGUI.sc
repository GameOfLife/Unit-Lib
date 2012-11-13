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
	
	classvar <>skin;
	classvar <>current;
	classvar <>singleWindow = true;
	
	var <chain, <score;
	
	var <parent, <composite, <views, <startButton, <uguis;
	var <>presetView;
	var <>action;
	var originalBounds;
	
	*initClass {
		
		skin = ( 
			labelWidth: 80, 
			hiliteColor: Color.black.alpha_(0.33),
			SmoothButton: (
				border: 0.75,
				background:  Gradient( Color.white, Color.gray(0.85), \v ) 
			),
			SmoothSimpleButton: (
				border: 0.75,
				background:  Gradient( Color.white, Color.gray(0.85), \v ) 
			),
		);
	
		StartUp.defer({ skin.font = Font( Font.defaultSansFace, 10 ); });

	}
	
	*new { |parent, bounds, chain, score|
		^super.newCopyArgs( chain, score ).init( parent, bounds );
	}
	
	init { |inParent, bounds|
		parent = inParent;
		
		if( skin.font.class != Font.implClass ) { // quick hack to make sure font is correct
			skin.font = Font( Font.defaultSansFace, 10 );
		};
		
		if( parent.isNil ) { 
			parent = chain.class.asString;
		};
		if( parent.class == String ) {
			if( singleWindow && current.notNil ) {
				parent = current.parent.asView.findWindow;
				current.views.singleWindow.focus;
				current.remove;
				this.makeViews( bounds );
				this.makeCurrent;
				parent.front;
			} {
				parent = Window(
					parent, 
					bounds ?? { Rect(128 rrand: 256, 64 rrand: 128, 342, 420) }, 
					scroll: false
				).front;
				this.makeViews( bounds );
				this.makeCurrent;
			};
		} {
			this.makeViews( bounds );
			this.makeCurrent;
		};
		
	}
	
	makeCurrent { current = this }
	
	makeViews { |bounds|
		RoundView.useWithSkin( skin ++ (RoundView.skin ? ()), {
			this.prMakeViews( bounds );
		});
	}
	
	getHeight { |units, margin, gap|
		^units.collect({ |unit|
			UGUI.getHeight( unit, 14, margin, gap ) + 14 + gap.y + gap.y;
		}).sum + (4 * (14 + gap.y));
	}
	
	prMakeViews { |bounds|
		var margin = 0@0, gap = 4@4;
		var heights, units;
		var labelWidth, releaseTask;
		var controller;
		// var unitInitFunc;
		
		labelWidth = 80;
		
		if( RoundView.skin.notNil ) { labelWidth = RoundView.skin.labelWidth ? 80 };
		
		views = ();
		
		originalBounds = bounds.copy;
		
		bounds = bounds ?? { parent.asView.bounds.insetBy(4,4) };
		if( parent.asView.class.name == 'SCScrollTopView' ) {
			bounds.width = bounds.width - 12;
		};
		units = chain.units;
				
		controller = SimpleController( chain );
		
		composite = CompositeView( parent, bounds ).resize_(5);
		composite.addFlowLayout( margin, gap );
		composite.onClose = { |vw|
			controller.remove; 
			if( composite == vw && { current == this } ) { current = nil } 
		};
		
		// startbutton
		views[ \startButton ] = SmoothButton( composite, 14@14 )
			.label_( ['power', 'power'] )
			.radius_(7)
			.background_( Color.clear )
			.border_(1)
			.hiliteColor_( Color.green )
			.action_( [ { 
					var startAction;
					releaseTask.stop;
					if( chain.releaseSelf or: (chain.dur == inf) ) {
						chain.prepareAndStart;
					} {
						startAction = { 
							chain.start;
							releaseTask = {
								(chain.dur - chain.fadeOut).max(0).wait;
								chain.release;
							}.fork;
						};
						chain.prepare( action: startAction );
							
					};
				}, { 
					releaseTask.stop;
					chain.release 
				} ]
		 	);
		
		composite.decorator.shift( bounds.width - 14 - 80, 0 );
		
		views[ \singleWindow ] = SmoothButton( composite, 74@14 )
			.label_( [ "single window", "single window" ] )
			.border_( 1 )
			.hiliteColor_( Color.green )
			.value_( this.class.singleWindow.binaryValue )
			.resize_(3)
			.action_({ |bt|
				this.class.singleWindow = bt.value.booleanValue;
			});
		
		if( chain.groups.size > 0 ) {
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
				
			composite.decorator.nextLine;
			
			// startTime
			StaticText( composite, labelWidth@14 )
				.applySkin( RoundView.skin )
				.string_( "startTime" )
				.align_( \right );
				
			views[ \startTime ] = SMPTEBox( composite, 84@14 )
				.applySmoothSkin
				.applySkin( RoundView.skin )
				.clipLo_(0)
				.value_( score.startTime )
				.action_({ |nb|
					score.startTime_( nb.value );
				});
			
			composite.decorator.nextLine;
		} {	
			// startTime
			StaticText( composite, labelWidth@14 )
				.applySkin( RoundView.skin )
				.string_( "startTime" )
				.align_( \right );
				
			views[ \startTime ] = SMPTEBox( composite, 84@14 )
				.applySmoothSkin
				.applySkin( RoundView.skin )
				.clipLo_(0)
				.action_({ |nb|
					chain.startTime_( nb.value );
				});
			
			composite.decorator.nextLine;
			
			// duration
			StaticText( composite, labelWidth@14 )
				.applySkin( RoundView.skin )
				.string_( "dur" )
				.align_( \right );
				
			views[ \dur ] = SMPTEBox( composite, 84@14 )
				.applySmoothSkin
				.applySkin( RoundView.skin )
				.clipLo_(0)
				.action_({ |nb|
					if( nb.value == 0 ) {
						chain.dur_( inf );
					} {
						chain.dur_( nb.value );
					};
				});
				
			views[ \infDur ] = SmoothButton( composite, 25@14 )
				.border_( 1 )
				.radius_( 3 )
				.label_( [ "inf", "inf" ] )
				.hiliteColor_( Color.green )
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
	
			views[ \fromSoundFile ] = SmoothButton( composite, 90@14 )
				.border_( 1 )
				.radius_( 3 )
				.label_( "from soundFile" )
				.action_({ chain.useSndFileDur });
				
			composite.decorator.nextLine;
			
			// fadeTimes
			StaticText( composite, labelWidth@14 )
				.applySkin( RoundView.skin )
				.string_( "fadeTimes" )
				.align_( \right );
			
			views[ \fadeIn ] = SmoothNumberBox( composite, 40@14 )
				.clipLo_(0)
				.scroll_step_(0.1)
				.action_({ |nb|
					chain.fadeIn_( nb.value );
				});
				
			views[ \fadeOut ] = SmoothNumberBox( composite, 40@14 )
				.clipLo_(0)
				.scroll_step_(0.1)
				.action_({ |nb|
					chain.fadeOut_( nb.value );
				});
				
			views[ \releaseSelf ] = SmoothButton( composite, 70@14 )
				.border_( 1 )
				.radius_( 3 )
				.label_( [ "releaseSelf", "releaseSelf" ] )
				.hiliteColor_( Color.green )
				.action_({ |bt|
					chain.releaseSelf = bt.value.booleanValue;
				});
				
			composite.decorator.nextLine;
		};
		
		// gain
		StaticText( composite, labelWidth@14 )
			.applySkin( RoundView.skin )
			.string_( "gain" )
			.align_( \right );
		
		views[ \gain ] = SmoothNumberBox( composite, 40@14 )
			.clipHi_(24) // just to be safe)
			.action_({ |nb|
				chain.setGain( nb.value );
			});
			
		views[ \muted ] = SmoothButton( composite, 40@14 )
			.border_( 1 )
			.radius_( 3 )
			.label_( [ "mute", "mute" ] )
			.hiliteColor_( Color.red )
			.action_({ |bt|
				switch( bt.value, 
					0, { chain.muted = false },
					1, { chain.muted = true }
				);
			});
			
		controller
			.put( \start, { views[ \startButton ].value = 1 } )
			.put( \end, { 
				if( units.every({ |unit| unit.synths.size == 0 }) ) {
					views[ \startButton ].value = 0;
					releaseTask.stop;
				};
			} )
			.put( \gain, { views[ \gain ].value = chain.getGain } )
			.put( \muted, { views[ \muted ].value = chain.muted.binaryValue } )
			.put( \units, { 
				if( composite.isClosed.not ) {
					{
						composite.children[0].focus; // this seems to prevent a crash..
						composite.remove;
						this.makeViews( originalBounds );
						this.makeCurrent;
					}.defer(0.01);
				};
			})
			.put( \init, { 
				if( composite.isClosed.not ) {
					{
						composite.children[0].focus; // this seems to prevent a crash..
						composite.remove;
						this.makeViews( originalBounds );
						this.makeCurrent;
					}.defer(0.01);
				};
			});
			
		if( score.isNil ) {
			controller
				.put( \startTime, { views[ \startTime ].value = chain.startTime ? 0; })
				.put( \dur, { var dur;
					dur = chain.dur;
					if( dur == inf ) {
						views[ \dur ].enabled = false; // don't set value
						views[ \infDur ].value = 1;
						views[ \releaseSelf ].hiliteColor = Color.green.alpha_(0.25);
						views[ \releaseSelf ].stringColor = Color.black.alpha_(0.5);
					} {
						views[ \dur ].enabled = true;
						views[ \dur ].value = dur;
						views[ \infDur ].value = 0;
						views[ \releaseSelf ].hiliteColor = Color.green.alpha_(1);
						views[ \releaseSelf ].stringColor = Color.black.alpha_(1);
					};
				})
				.put( \fadeIn, { views[ \fadeIn ].value = chain.fadeIn })
				.put( \fadeOut, { views[ \fadeOut ].value = chain.fadeOut })
				.put( \releaseSelf, {  
					views[ \releaseSelf ].value = chain.releaseSelf.binaryValue;
				})
		};
		
		chain.changed( \gain );
		chain.changed( \mute );
		chain.changed( \startTime );
		chain.changed( \dur );
		chain.changed( \fadeIn );
		chain.changed( \fadeOut );
		chain.changed( \releaseSelf );
		
		uguis = this.makeUnitViews(units, margin, gap );
	}
	
	makeUnitHeader { |units, margin, gap|
		var comp, header, min, io, defs, code;
		var notMassEdit;
		
		notMassEdit = chain.class != MassEditUChain;
		
		comp = CompositeView( composite, (composite.bounds.width - (margin.x * 2))@16 )
			.resize_(2);
		
		header = StaticText( comp, comp.bounds.moveTo(0,0) )
				.applySkin( RoundView.skin )
				.string_( " units" )
				.align_( \left )
				.resize_(2);
				
		if( notMassEdit ) {
            io = SmoothButton( comp, Rect( comp.bounds.right - 60, 1, 60, 12 ) )
                .label_( "i/o" )
                .border_( 1 )
                .radius_( 2 )
                .action_({
                    var parent;
                    parent = composite.parent;
                    {
                        composite.remove;
                        UChainIOGUI( parent, originalBounds, chain );
                    }.defer(0.01);

                }).resize_(3);
            code = SmoothButton( comp,
                    Rect( comp.bounds.right - (40 + 4 + 60), 1, 40, 12 ) )
                .label_( "code" )
                .border_( 1 )
                .radius_( 2 )
                .action_({
                    var parent;
                    parent = composite.parent;
                    {
                        composite.remove;
                        UChainCodeGUI( parent, originalBounds, chain );
                    }.defer(0.01);
                }).resize_(3);
		};
		
		defs = SmoothButton( comp, 
				Rect( comp.bounds.right - (
					4 + 38 + (notMassEdit.binaryValue * (4 + 40 + 4 + 60))
					), 1, 42, 12 
				) 
			)
			.label_( "udefs" )
			.border_( 1 )
			.radius_( 2 )
			.action_({
				UdefListView();
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
		var scrollerMargin = 12;
		
		if( GUI.id == \qt ) { scrollerMargin = 20 };
		
		notMassEdit = chain.class != MassEditUChain;
		
		
		width = scrollView.bounds.width - scrollerMargin - (margin.x * 2);
		
		unitInitFunc = { |unit, what ...args|
			if( what === \init ) { // close all views and create new
				chain.changed( \units );
			};
		};
		
		if( units.size == 0 ) {
			comp = CompositeView( scrollView, width@14 )
				.resize_(2);
			
			header = StaticText( comp, comp.bounds.moveTo(0,0) )
				.applySkin( RoundView.skin )
				.string_( " empty: drag unit or Udef here" )
				.background_( Color.yellow.alpha_(0.25) )
				.resize_(2)
				.font_( 
					(RoundView.skin.tryPerform( \at, \font ) ?? 
						{ Font( Font.defaultSansFace, 12) }).boldVariant 
				);
				
			uview = UserView( comp, comp.bounds.moveTo(0,0) );
				
			uview.canReceiveDragHandler_({ |sink|
				var drg;
				drg = View.currentDrag;
				case { drg.isKindOf( Udef ) } 
					{ true }
					{ drg.isKindOf( UnitRack ) }
                    { true }
					{ [ Symbol, String ].includes( drg.class ) }
					{ Udef.all.keys.includes( drg.asSymbol ) }
					{ drg.isKindOf( U ) }
					{ true }
					{ false }
			})
			.receiveDragHandler_({ |sink, x, y|
					case { View.currentDrag.isKindOf( U ) } {
						chain.units = [ View.currentDrag.deepCopy ];
					}{ View.currentDrag.isKindOf( Udef ) }{
						chain.units = [ U( View.currentDrag ) ];
					}{ View.currentDrag.isKindOf( UnitRack ) } {
                        chain.units = View.currentDrag.units;
                    }{   [ Symbol, String ].includes( View.currentDrag.class )  } {
						chain.units = [ U( View.currentDrag.asSymbol ) ];
					};
			})
		};
		
		ug = units.collect({ |unit, i|
			var header, comp, uview, plus, min, defs, io;
			var addBefore;
				
					
			addBefore = UserView( scrollView, width@6 )
				.resize_(2);
			
			if( notMassEdit ) {
				addBefore.canReceiveDragHandler_({ |sink|
						var drg;
						drg = View.currentDrag;
						case { drg.isKindOf( Udef ) } 
							{ true }
							{ drg.isKindOf( UnitRack ) }
	                        { true }
							{ [ Symbol, String ].includes( drg.class ) }
							{ Udef.all.keys.includes( drg.asSymbol ) }
							{ drg.isKindOf( U ) }
							{ true }
							{ false }
					})
					.receiveDragHandler_({ |sink, x, y|
							var ii;
							case { View.currentDrag.isKindOf( U ) } {
								ii = chain.units.indexOf( View.currentDrag );
								if( ii.notNil ) {
									chain.units[ii] = nil;
									chain.units.insert( i, View.currentDrag );
									chain.units = chain.units.select(_.notNil);
								} {
									chain.units = chain.units.insert( i, 
										View.currentDrag.deepCopy );
								};
							} { View.currentDrag.isKindOf( Udef ) } {
								chain.units = chain.units.insert( i,  U( View.currentDrag ) );
							}{ View.currentDrag.isKindOf( UnitRack ) } {
	                           		chain.insertCollection( i, View.currentDrag.units ++ [ unit ]);                        
	                           }{   [ Symbol, String ].includes( View.currentDrag.class )  } {
								chain.units = chain.units.insert( i, 
									U( View.currentDrag.asSymbol )
								);
							};
					});
			};
		
			comp = CompositeView( scrollView, width@14 )
				.resize_(2);
			
			header = StaticText( comp, comp.bounds.moveTo(0,0) )
				.applySkin( RoundView.skin )
				.string_( " " ++ i ++ ": " ++ if(unit.def.class == LocalUdef){"[Local] "}{""}++unit.defName )
				.background_( Color.white.alpha_(0.5) )
				.resize_(2)
				.font_( 
					(RoundView.skin.tryPerform( \at, \font ) ?? 
						{ Font( Font.defaultSansFace, 12) }).boldVariant 
				);
			
			
			//if( chain.class != MassEditUChain )
				
			uview = UserView( comp, comp.bounds.moveTo(0,0) );
			
			uview.canReceiveDragHandler_({ |sink|
				var drg;
				drg = View.currentDrag;
				case { drg.isKindOf( Udef ) } 
					{ true }
					{ drg.isKindOf( UnitRack ) }
                        { true }
					{ [ Symbol, String ].includes( drg.class ) }
					{ Udef.all.keys.includes( drg.asSymbol ) }
					{ drg.isKindOf( U ) }
					{ true }
					{ false }
			})
			.receiveDragHandler_({ |sink, x, y|
				var u, ii;
				case { View.currentDrag.isKindOf( U ) } {
					u = View.currentDrag;
					ii = chain.units.indexOf( u );
					if( ii.notNil ) { 
						chain.units[ii] = unit; 
						chain.units[i] = u;
					} {
						chain.units[ i ] = u.deepCopy;
					};
					chain.units = chain.units; // force refresch
					
				} { View.currentDrag.isKindOf( UnitRack ) } {
                        chain.insertCollection( i, View.currentDrag.units );
                    } { View.currentDrag.isKindOf( Udef ) } {
					unit.def = View.currentDrag;
				} {   [ Symbol, String ].includes( View.currentDrag.class )  } {
					unit.def = View.currentDrag.asSymbol.asUdef;
				};
			})
			.beginDragAction_({ 
				unit;
			});
			
			if( notMassEdit ) {	
				min = SmoothButton( comp, 
							Rect( comp.bounds.right - (12 + 4 + 12), 1, 12, 12 ) )
						.label_( '-' )
						.border_( 1 )
						.action_({
							chain.units = chain.units.select(_ != unit);
						}).resize_(3);
				
				if( units.size == 1 ) {
					min.enabled = false;
				};
				
				plus = SmoothButton( comp, 
						Rect( comp.bounds.right - (12 + 2), 1, 12, 12 ) )
					.label_( '+' )
					.border_( 1 )
					.action_({
						chain.units = chain.units.insert( i, unit.deepCopy );
					}).resize_(3);
			};	
					
			unit.addDependant( unitInitFunc );
			header.onClose_({ unit.removeDependant( unitInitFunc ) });
			unit.gui( scrollView, 
				scrollView.bounds.copy.width_( 
					scrollView.bounds.width - scrollerMargin - (margin.x * 2) 
				)  
			);
		});
		
		if( notMassEdit && { units.size > 0 } ) {
			addLast = UserView( scrollView, width@6 )
				.resize_(2);
					
			addLast.canReceiveDragHandler_({ |sink|
					var drg;
					drg = View.currentDrag;
					case { drg.isKindOf( Udef ) } 
						{ true }
						{ drg.isKindOf( UnitRack ) }
                        { true }
						{ [ Symbol, String ].includes( drg.class ) }
						{ Udef.all.keys.includes( drg.asSymbol ) }
						{ drg.isKindOf( U ) }
						{ true }
						{ false }
				})
				.receiveDragHandler_({ |sink, x, y|
						var ii;
						case { View.currentDrag.isKindOf( U ) } {
							ii = chain.units.indexOf( View.currentDrag );
							if( ii.notNil ) {
								chain.units[ii] = nil;
								chain.units = chain.units.select(_.notNil) ++
									[ View.currentDrag ];
							} {
								chain.units = chain.units ++ [ View.currentDrag.deepCopy ];
							};
							
						} { View.currentDrag.isKindOf( Udef ) } {
							chain.units = chain.units ++ [ U( View.currentDrag ) ];
						}{ View.currentDrag.isKindOf( UnitRack ) } {
                            chain.units = chain.units ++ View.currentDrag.units;
                        }{   [ Symbol, String ].includes( View.currentDrag.class )  } {
							chain.units = chain.units ++ [ U( View.currentDrag.asSymbol ) ];
						};
				});
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
			
		if( notMassEdit ) {
			
			CompositeView( composite, (composite.bounds.width - (margin.x * 2)) @ 2 )
				.background_( Color.black.alpha_(0.25) )
				.resize_(8);
			
			presetView = PresetManagerGUI( 
				composite, 
				composite.bounds.width @ PresetManagerGUI.getHeight,
				UChain.presetManager,
				chain
			).resize_(7)
		};
			
		^this.makeUnitSubViews( scrollView, units, margin, gap );
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
}

+ UChain {
	gui { |parent, bounds, score| ^UChainGUI( parent, bounds, this, score ) }
}