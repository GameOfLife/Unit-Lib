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

BarMapView {
	
	var <value = 0;
	var <barMap;
	var <parent, <view, <views;
	var <>action;
	var <viewHeight = 14;
	var <visible = true;
	
	*new { |parent, bounds, barMap, action|
		^super.new.init( parent, bounds, barMap, action );
	}
	
	init { |parent, bounds, inBarMap, inAction|
		barMap = inBarMap ?? { BarMap(); };
		barMap.addDependant( this );
		action = inAction;
		case { bounds.isKindOf( Point ) } {
			viewHeight = bounds.y;
		} {  bounds.isKindOf( Rect ) } {
			viewHeight = bounds.height;
		};
		viewHeight.postln;
		this.makeView( parent, bounds );
	}
	
	doAction { action.value( this ) }
	
	value_ { |newValue = 0|
		value = newValue;
		this.update;
	}
	
	visible_ { |bool = true|
		if( bool != visible ) {
			visible = bool;
			this.update;
		};
		{ view.visible = visible }.defer;
	}
	
	update {
		if( visible ) { this.setViews( value ); };
	}
	
	resize_ { |resize|
		view.resize = resize ? 5;
	}
	
	remove {
		if( barMap.notNil ) { 
			barMap.removeDependant( this );
		};
	}
	
	setViews { |value|
		var new, x, div, sub;
		new = barMap.barAtBeat( value, true );
		x = (new[1] * 1000).round(1);
		div = (x / 1000).floor;
		sub = x - (div * 1000);
		views[ \bar ].value = new[0];
		views[ \division ].value = div;
		views[ \sub ].value = sub;
	}
		
	setFont { |font|
		font = font ??
			{ RoundView.skin !? { RoundView.skin.font } } ?? 
			{ Font( Font.defaultSansFace, viewHeight - 4 ) };
		
		views[ \bar ].font = font;
		views[ \division ].font = font;
		views[ \sub ].font = font;
	}
	
	autoScale_ { |bool|
		views[ \bar ].autoScale = bool;
		views[ \division ].autoScale = bool;
		views[ \sub ].autoScale = bool;
	}
	
	radius_ { |radius|
		radius = radius.asCollection.wrapExtend(4);
		views[ \bar ].radius = [ radius[0], 0, 0, radius[3] ];
		views[ \sub ].radius = [ 0, radius[1], radius[2], 0 ];
	}
	
	*viewNumLines { ^1 }
	
	makeView { |parent, bounds, resize|
		var getValue, centerWidth, sideWidth;
		if( bounds.isNil ) { bounds= 350 @ (this.class.viewNumLines * (viewHeight + 4)) };
		
		view = EZCompositeView( parent, bounds, gap: 2@2 );
		bounds = view.asView.bounds;
		view.onClose_({ this.remove; });
		view.resize_( resize ? 5 );
		views = ();
		
		getValue = { |update = true|
			var new;
			value = barMap.beatAtBar( views[ \bar ].value, 
				views[ \division ].value + (views[ \sub ].value / 1000) 
			);
		};
		
		centerWidth = (viewHeight * 1.25).floor;
		sideWidth = ( ( bounds.width - (centerWidth + 4) ) / 2 ).floor;
		
		views[ \bar ] = SmoothNumberBox( view, sideWidth @ viewHeight )
			.step_(1).scroll_step_(1)
			.radius_( [ viewHeight, 0, 0, viewHeight ] / 2 )
			.align_( \right )
			.action_({ |nb|
				getValue.();
				this.setViews( value );
				action.value( this );
			})
			.keyDownAction_({ |vw, char, modifiers, unicode, keycode, key|
				switch( unicode.getArrowKey ? key.getArrowKey,
					\left, { },
					\right, { views[ \division ].focus },
					{ vw.keyDown( char, modifiers, unicode, keycode, key ) }
				);
			});
			
		views[ \division ] = SmoothNumberBox( view, centerWidth @ viewHeight )
			.step_(1).scroll_step_(1)
			.radius_( 0 )
			.align_( \center )
			.action_({ |nb|
				getValue.();
				this.setViews( value );
				action.value( this );
			})
			.keyDownAction_({ |vw, char, modifiers, unicode, keycode, key|
				switch( unicode.getArrowKey ? key.getArrowKey,
					\left, { views[ \bar ].focus },
					\right, { views[ \sub ].focus },
					{ vw.keyDown( char, modifiers, unicode, keycode, key ) }
				);
			});
			
		views[ \sub ] = SmoothNumberBox( view, sideWidth @ viewHeight )
			.step_(1).scroll_step_(1)
			.radius_( [ 0, viewHeight, viewHeight, 0 ] / 2 )
			.formatFunc_({ |value| value.asString.padLeft(3,"0"); })
			.action_({ |nb|
				getValue.();
				this.setViews( value );
				action.value( this );
			}).keyDownAction_({ |vw, char, modifiers, unicode, keycode, key|
				switch( unicode.getArrowKey ? key.getArrowKey,
					\left, { views[ \division ].focus },
					\right, { },
					{ vw.keyDown( char, modifiers, unicode, keycode, key ) }
				);
			});
			
		this.setFont;
		this.setViews( value );
	}
	
}