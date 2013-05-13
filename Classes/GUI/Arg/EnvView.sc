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

EnvPlotView {
	
	var <env, <view, <plotView, <ctrl;
	var <active = true;
	var <selected;
	var <selectedLine;
	var <>font, <>viewHeight = 14, <resize = 5;
	var <min = 0, <max = 1;
	var <showLegend = true;
	var <>action;
	var <>editMode = \move;
	var hitPoint;
	
	*new { |parent, bounds, env|
		^super.newCopyArgs( env ).init.makeView( parent, bounds );
	}
	
	init {
		env = env ?? { Env() };
		font = RoundView.skin !? { RoundView.skin.font } ?? { Font( Font.defaultSansFace, 10 ); };
	}
	
	*viewNumLines { ^6 }
	
	removeCtrl {
		ctrl.remove;
		ctrl = nil;
	}
	
	addCtrl {
		ctrl.remove;
		ctrl = SimpleController( env )
			.put( \times, { this.refresh })
			.put( \levels, { this.refresh })
			.put( \curves, { this.refresh });
	}
	
	refresh {
		if( plotView.userView.view.isClosed.not ) {
			{ plotView.refresh }.defer;
		};
	}
	
	selected_ { |index| 
		selected = index; 
		this.changed( \selected, selected );
		this.refresh 
	}
	
	selectedLine_ { |index| 
		selectedLine = index; 
		this.changed( \selectedLine, selectedLine );
		this.refresh 
	}
	
	active_ { |bool=true|
		active = bool;
		this.changed( \active );
		this.refresh;
	}
	
	showLegend_ { |bool=true|
		showLegend = bool;
		this.changed( \showLegend );
		this.refresh;
	}
	
	env_ { |new|
		if( new.notNil ) {
			env = new;
			if( plotView.userView.view.isClosed.not ) {
				this.addCtrl;
			};
			this.changed( \env, env );
			this.refresh;
		};
	}
	
	resize_ { |new|
		resize = new;
		view.resize_( resize );
	}
	
	getPoints { |bounds|
		var dur;
		dur = env.times.sum;
		bounds = bounds ? plotView.fromBounds;
		^env.levels.collect({ |level, i|
			Point(
				(env.times[..i-1].sum / dur) * bounds.width, 
				level.linlin( min, max, bounds.height, 0 )
			)
		});
	}
	
	setPoints { |pts, bounds|
		var dur, levels, times;
		dur = env.times.sum;
		pts = pts.sort({ |a,b| a.x <= b.x });
		#times, levels = pts.collect({ |pt|
			[
				(pt.x / bounds.width) * dur,
				pt.y.linlin( 0, bounds.height, max, min )
			]
		}).flop;
		env.levels = levels;
		env.times = times.differentiate[1..];
		env.changed( \levels );
	}
	
	makeView { |parent, bounds|
		
		if( bounds.isNil ) { bounds= 350 @ (this.class.viewNumLines * (viewHeight + 4)) };
		
		view = EZCompositeView( parent, bounds, gap: 4@4 );
		bounds = view.asView.bounds;
		view.resize_( resize ? 5 );
		
		this.addCtrl;
			
		plotView = ScaledUserView.withSliders( view, bounds.moveTo(0,0),  bounds.moveTo(0,0))
			.resize_( 5 )
			.scaleVEnabled_( false )
			.moveVEnabled_( false )
			.onClose_({ this.removeCtrl; });
			
		
		plotView.mouseDownAction = { |vw,x,y,mod|
			var bounds, pts;
			
			if( active ) {
				bounds = vw.fromBounds.moveTo(0,0);
				hitPoint = (x@y);
				pts = this.getPoints( bounds );
				this.selectedLine = nil;
				this.selected = pts.detectIndex({ |ptx|
					ptx.dist( hitPoint ) <= 6;
				});
				if( selected.isNil && { 
						(
							env.at( x.linlin(0,bounds.width,0,env.times.sum) )
								.linlin(min,max,bounds.height,0) - y 
						).abs < 6
					} ) {
					this.selectedLine = pts.lastIndexForWhich({ |ptx| ptx.x < hitPoint.x });
				};
			};
		};
			
		plotView.mouseMoveAction = { |vw,x,y,mod|
			var bounds;
			var pt, pts, oldX, curves;
			
			if( active ) {
				bounds = vw.fromBounds.moveTo(0,0);
				
				pts = this.getPoints( bounds );
				
				if( selected.notNil ) {
					switch( editMode,
						\move, {
							pt = pts[ selected ];
							pt.y = pt.y - (hitPoint.y - y);
							if( selected != 0 and: { selected != (pts.size - 1)} ) {
								pt.x = pt.x - (hitPoint.x - x);
							};
							
							hitPoint = x@y;
							pts = pts.sort({ |a,b| a.x <= b.x });
							this.selected = pts.indexOf( pt );
							this.setPoints( pts, bounds );
							
							action.value( this, env );
						},
						\elastic, {
							pt = pts[ selected ];
							oldX = pt.x;
							pt.y = pt.y - (hitPoint.y - y);
							if( selected != 0 and: { selected != (pts.size - 1)} ) {
								pt.x = pt.x - (hitPoint.x - x);
							};
							pts[..selected-1].do({ |item, i|
								item.x = item.x.linlin(0,oldX,0,pt.x);							});
							pts[selected+1..].do({ |item, i|
								item.x = item.x.linlin(oldX,bounds.width,pt.x,bounds.width);
							});
							hitPoint = x@y;
							pts = pts.sort({ |a,b| a.x <= b.x });
							this.selected = pts.indexOf( pt );
							this.setPoints( pts, bounds );
							
							action.value( this, env );
						},
						\extend, {
							pt = pts[ selected ];
							oldX = pt.x;
							pt.y = pt.y - (hitPoint.y - y);
							if( selected != 0 and: { selected != (pts.size - 1)} ) {
								pt.x = pt.x - (hitPoint.x - x);
							};
							pts[selected+1..].do({ |item, i|
								item.x = item.x + (pt.x - oldX);
							});
							hitPoint = x@y;
							pts = pts.sort({ |a,b| a.x <= b.x });
							this.selected = pts.indexOf( pt );
							this.setPoints( pts, bounds );
							
							action.value( this, env );
						});
				};
				
				if( selectedLine.notNil ) {
					curves = env.curves.asCollection.wrapExtend(env.times.size);
					if( curves[selectedLine].isNumber ) {
						if( pts[selectedLine].y > (pts[selectedLine+1].y) ) {
							curves[selectedLine] = curves[selectedLine] - 
								((( hitPoint.y - y ) / bounds.height) * 10);
						} {
							curves[selectedLine] = curves[selectedLine] + 
								((( hitPoint.y - y ) / bounds.height) * 10);
						};
						hitPoint = x@y;
						env.curves = curves;
						env.changed( \curves );
						action.value( this, env );
					};
				};
			};
			
			
		};
		
			
		plotView.drawFunc = { |vw|
			var freqs, svals, bounds, sline, slineX;
			var pts, strOffset = 11;
			
			if( GUI.id === 'swing' ) { strOffset = 14 };
			
			bounds = vw.fromBounds.moveTo(0,0);
			
			////// PREPARATION ///////
			
			svals = env.discretize( bounds.width ).linlin(min,max, bounds.height, 0, \none);
			
			// get draggable points
			pts = this.getPoints( bounds );
			
			////// DRAWING ///////
			
			// draw background
			Pen.color_( Color.white.alpha_(0.25) );
			Pen.roundedRect( bounds, 2 ).fill;
			
			// make cliprect
			Pen.roundedRect( bounds.insetBy(0,0), 2 ).clip;
			
			pts.do({ |pt, i|
				if( selected == i ) { 
					Pen.color = Color.yellow;
					Pen.addArc( pt, 5, 0, 2pi );
					Pen.fill;
				};
				Pen.color = Color.hsv( i.linlin( 0, pts.size, 0, 1 ), 0.75, 0.5 );
				Pen.addArc( pt, 5, 0, 2pi );
				Pen.stroke;
			});
			
			if( selectedLine.notNil ) {
				slineX = (pts.clipAt(selectedLine).x.asInt);
				sline = svals
					[slineX..pts.clipAt(selectedLine+1).x.ceil.asInt-1];
				Pen.color = Color.yellow;
				Pen.width = 3;
				Pen.moveTo( slineX@(sline[0]) );
				sline[1..].do({ |val, i|
					Pen.lineTo( (i+1+slineX)@val );
				});
				Pen.stroke;
			};
				
			// draw summed magResponse
			Pen.color = Color.blue(0.5);
			Pen.width = 1;
			Pen.moveTo( 0@(svals[0]) );
			svals[1..].do({ |val, i|
				Pen.lineTo( (i+1)@val );
			});
			Pen.stroke;
			
			// draw outer border
			Pen.extrudedRect( bounds, 2, 1, inverse: true );
		};
		

	}
}

EnvEditView {
	
	var <env, <view, <listView, <argViews, <ctrl;
	var <>viewHeight = 14;
	var <resize = 8;
	var <>font;
	var <selected = 0;
	var <>action;
	
	*new { |parent, bounds, env|
		^super.newCopyArgs( env ).init.makeView( parent, bounds );
	}
	
	init {
		env = env ?? { Env() };
		font = RoundView.skin !? { RoundView.skin.font } ?? { Font( Font.defaultSansFace, 10 ); };
	}
	
	removeCtrl {
		ctrl.remove;
		ctrl = nil;
	}
	
	addCtrl {
		ctrl.remove;
		ctrl = SimpleController( env )
			.put( \setting, { |obj, what, name, argName, value|
				var nameIndex, vw;
				if( listView.isClosed.not ) {
					if( name.isNil ) {
						this.update;
					} {
						nameIndex = env.names.indexOf( name );
						if( nameIndex.notNil ) {
							vw = argViews[ nameIndex ][ argName ];
							if( vw.notNil ) {
								vw.value = value;
							};
						};
					};
				};
			});
	}
	
	update {
		var argNames;
		if( listView.isClosed.not ) {
			argNames = env.argNames;
			argNames.do({ |names, i|
				names.do({ |argName|
					argViews[ i ][ argName ].value = env.get( i, argName );
				});
			});
		};
	}
	
	*viewNumLines { ^2 }
	
	selected_ { |new|
		if( selected != new ) {	
			selected = new;
			if( selected.notNil ) {
				argViews.do({ |item, i|
					item[ \comp ].visible = (i == selected);
				});
				{ 	listView.value = selected; 
					view.view.background = Color.hsv( 
						(selected ? 0).linlin( 0, env.names.size, 0, 1 ), 
						0.75, 0.5 
					).alpha_( 0.25 );
				}.defer;
			};
			this.changed( \selected, selected );
		};
	}
	
	env_ { |new|
		if( new.notNil ) {
			env = new;
			if( listView.isClosed.not ) {
				this.addCtrl;
			};
			this.changed( \env, env );
			this.update;
			//this.refresh;
		};
	}
	
	resize_ { |new|
		resize = new;
		view.resize_( resize );
	}
	
	
	makeView { |parent, bounds|
		var comp, argNames, specs;
		
		if( bounds.isNil ) { bounds = 350 @ (this.class.viewNumLines * (viewHeight + 4)) };
			
		view = EZCompositeView( parent, bounds, margin: 0@2, gap: 2@2 );
		bounds = view.asView.bounds;
		view.resize_( resize ? 5 );
		argViews = [];
		
		this.addCtrl;
		
		listView = PopUpMenu( view, 80 @ viewHeight )
			.font_( font )
			.value_( selected ? 0 )
			.applySkin( RoundView.skin)
			.items_( 
				env.levels.collect({ |item, i|
					"node" + i;
				}) 
			)
			.action_({ |pu|
				this.selected = pu.value;
			});
			
		listView.onClose_({ this.removeCtrl });
		
		view.decorator.nextLine;
		
		comp = CompositeView( view, bounds.width @ (viewHeight + 4))
			.resize_(2)
			.background_( Color.white.alpha_(0.25) );
		
		argNames = [ \level, \time, \curve ];
		specs = [ [0,1].asSpec, SMPTESpec(), [-32,32].asSpec ];
		
		view.view.background = Color.hsv( 
			(selected ? 0).linlin( 0, env.names.size, 0, 1 ), 
			0.75, 0.5 
		).alpha_( 0.25 );
			
		env.levels.do({ |name, i|
			var vws;
			vws = ();
			
			vws[ \comp ] = CompositeView( comp, bounds.width @ (viewHeight + 4) )
				.resize_(2);
				
			vws[ \comp ].addFlowLayout( 2@2, 2@2 );
			
			argNames[i].do({ |argName, ii|
				var spec, step;
				
				StaticText(  vws[ \comp ], 25 @ viewHeight )
					.string_( argName.asString ++ " " )
					.align_( \right )
					.font_( font )
					.applySkin( RoundView.skin );
					
				spec = specs[i][ii];
				step =  spec !? { spec.step } ? 1;
				if( step == 0 ) { step = 1 };
				
				vws[ argName ] = SmoothNumberBox( vws[ \comp ], 40 @ viewHeight  )
					.value_( env.get( name, argName ) )
					.font_( font )
					.clipLo_( spec !? { spec.minval } ? -inf )
					.clipHi_( spec !? { spec.maxval } ? inf  )
					.step_( step )
					.scroll_step_( step )
					.action_({ |nb|
						env.set( name, argName, nb.value );
						action.value( this, env );
					});
				
			});
			
			vws[ \comp ].visible = ((selected ? 0) == i);
			
			argViews = argViews.add( vws );
		});
			
	}
	
}

EnvView {
	
	var <view;
	var <plotView, <editView;
	var <plotCtrl, <editCtrl;
	var <presetManager, <presetView;
	var <>action, <>viewHeight = 14;
	var <resize = 5;
	
	*new { |parent, bounds, env, presets|
		^this.newCopyArgs.makeView( parent, bounds, env, presets );
	}
	
	*viewNumLines { ^EQEditView.viewNumLines + EQPlotView.viewNumLines + PresetManagerGUI.viewNumLines }
	
	resize_ { |new|
		resize = new;
		view.resize_( resize );
	}
	
	isClosed { ^view.isClosed }
	front { ^view.front }
	
	env { ^plotView.env }
	
	env_ { |new|
		plotView.env = new;
		editView.env = new;
	}
	
	value { ^this.env }
	value_ { |val| this.env = val }
	
	doAction { action.value( this ) }
	
	onClose_ { |func| view.onClose = func }
	
	close { view.findWindow.close } 
	
	makeView { |parent, bounds, env, presets|
		if( env.isNil ) { env = EQSetting() };
		
		if( bounds.isNil ) { bounds = 350 @ (this.class.viewNumLines * (viewHeight + 4)) };
			
		view = EZCompositeView( parent, bounds, gap: 2@2 );
		bounds = view.asView.bounds;
		view.resize_( resize ? 5 );
		
		plotView = EQPlotView( view, 
			bounds.copy.height_( bounds.height *
				( EQPlotView.viewNumLines / this.class.viewNumLines) ),
			env
		).resize_(5);
		
		editView = EQEditView( view, 
			bounds.copy.height_( bounds.height *
				( EQEditView.viewNumLines / this.class.viewNumLines) ),
			env
		).resize_(8);
		
		plotCtrl = SimpleController( plotView )
			.put( \selected, { editView.selected = plotView.selected } );
		
		editCtrl = SimpleController( editView )
			.put( \selected, { plotView.selected = editView.selected } );
		
		presetView = PresetManagerGUI( view, 
			bounds.copy.height_( bounds.height * ( 1 / this.class.viewNumLines) ),
			env.getEQdef.presetManager,
			env
		);
		
		presetView.resize_(8);

		view.asView.onClose_({ plotCtrl.remove; editCtrl.remove });
		
		plotView.action = { |obj, value| action.value( this, value ) };
		editView.action = plotView.action;
		
	}	
	
}