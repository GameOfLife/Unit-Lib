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

+ Spec {
	
	adaptFromObject { ^this }
	
	viewNumLines { ^1 }
}

+ Nil {
	viewNumLines { ^1 }
}

+ ControlSpec {
	
	makeView { |parent, bounds, label, action, resize|
		var view, vws, stp, labelWidth;
		vws = ();
		
		if( (minval != -inf) && { maxval != inf } ) {
			vws[ \valueView ] = 
				EZSmoothSlider( parent, bounds, label !? { label.asString ++ " " }, 
					this, { |vw| action.value( vw, vw.value ) },
					labelWidth: (RoundView.skin ? ()).labelWidth ? 80 );
			
			vws[ \view ] = vws[ \valueView ].view;
			vws[ \sliderView ] = vws[ \valueView ].sliderView;
			vws[ \sliderView ].centered_( true ).centerPos_( this.unmap( default ) );
			
		} {
			view = EZCompositeView( parent, bounds );
			vws[ \view ] = view.view;
			bounds = view.view.bounds;
			stp = this.step;
			if( stp == 0 ) { stp = 1 };
			
			if( label.notNil ) {
				labelWidth = (RoundView.skin ? ()).labelWidth ? 80;
				vws[ \labelView ] = StaticText( view, labelWidth @ 14 )
					.string_( label.asString ++ " " )
					.align_( \right )
					.resize_( 4 )
					.applySkin( RoundView.skin );
			} {
				labelWidth = -4;
			};
		
			vws[ \valueView ] = SmoothNumberBox( view, 
					Rect(labelWidth + 4,0,bounds.width-(labelWidth + 4),bounds.height)
				)
			    .action_({ |vw|
			        action.value( vw, vw.value );
			    } ).resize_(5)
				.step_( stp )
				.scroll_step_( stp )
				.clipLo_( this.minval )
				.clipHi_( this.maxval );	
		};
		if( resize.notNil ) { vws.view.resize = resize };
		^vws;	
	}
	
	setView { |vws, value, active = false|
		vws[ \valueView ].value = value;
		if( active ) { vws[ \valueView ].doAction };
	}
	
	mapSetView { |vws, value, active = false|
		vws[ \valueView ].value = this.map(value);
		if( active ) { vws[ \valueView ].doAction };
	}
	
	adaptFromObject { |object| // if object out of range; change range
		if( object.isArray ) {
			^this.asRangeSpec.adaptFromObject( object );
		} {
			if( object.inclusivelyBetween( minval, maxval ).not ) {
				^this.copy
					.minval_( minval.min( object ) )
					.maxval_( maxval.max( object ) )
			};
			^this;
		};
	}
}

+ ListSpec {
	
	makeView { |parent, bounds, label, action, resize|
		var multipleActions = action.size > 0;
		var vw;
		vw = EZPopUpMenu( parent, bounds, label !? { label.asString ++ " " }, 
			if( multipleActions ) {
				list.collect({ |item, i| 
					item.asSymbol -> { |vw| action[i].value( vw, list[i] ) };
				});
			} { list.collect({ |item, i| item.asSymbol -> nil })
			},
			initVal: defaultIndex
		);
		if( multipleActions.not ) {
			vw.globalAction = { |vw| action.value( vw, list[vw.value] ) };
		};
		vw.labelWidth = 80; // same as EZSlider
		vw.applySkin( RoundView.skin ); // compat with smooth views
		if( resize.notNil ) { vw.view.resize = resize };
		^vw
	}
	
	setView { |view, value, active = false|
		{  // can call from fork
			view.value = this.unmap( value );
			if( active ) { view.doAction };
		}.defer;
	}
	
	mapSetView { |view, value, active = false|
		{
			view.value = value;
			if( active ) { view.doAction };
		}.defer;
	}
	
	adaptFromObject { |object|
		if( list.any({ |item| item == object }).not ) {
			^this.copy.add( object )
		} {
			^this
		};
	}
	
	
}

+ StringSpec {
	
	makeView { |parent, bounds, label, action, resize| 
		var vws, view, labelWidth;
		vws = ();
		
		// this is basically an EZButton
		
		bounds.isNil.if{bounds= 320@20};
		
		#view, bounds = EZGui().prMakeMarginGap.prMakeView( parent, bounds );
		 vws[ \view ] = view;
		 		
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
		
		vws[ \string ] = TextField( view, 
			Rect( labelWidth + 2, 0, bounds.width-(labelWidth+2), bounds.height )
		)	.resize_(2)
			.applySkin( RoundView.skin ? () )
			.action_({ |tf|
				action.value( vws, tf.value );
			});

		if( resize.notNil ) { vws[ \view ].resize = resize };
		^vws;
	}
	
	setView { |view, value, active = false|
		{ view[ \string ].value = this.constrain( value ); }.defer;
		if( active ) { view[ \string ].doAction };
	}

}

+ SMPTESpec {

	makeView { |parent, bounds, label, action, resize|
		var vws, view, labelWidth;
		
		bounds.isNil.if{bounds= 160 @ 18 };
		
		vws = ();
		#view, bounds = EZGui().prMakeMarginGap.prMakeView( parent, bounds );
		vws[ \view ] = view;
		
		if( label.notNil ) {
			labelWidth = (RoundView.skin ? ()).labelWidth ? 80;
			vws[ \labelView ] = StaticText( vws[ \view ], labelWidth @ 14 )
				.string_( label.asString ++ " " )
				.align_( \right )
				.resize_( 4 )
				.applySkin( RoundView.skin );
		} {
			labelWidth = -4;
		};
		
		vws[ \box ] = SMPTEBox( vws[ \view ], 
				Rect(labelWidth + 4,0,bounds.width-(labelWidth + 4),bounds.height)
			)
			.applySmoothSkin
		    .action_({ |vw|
		        action.value( vw, vw.value );
		    } ).resize_(5)
		    .fps_( fps )
			.clipLo_( minval )
			.clipHi_( maxval );
		    
		if( resize.notNil ) { vws[ \view ].resize = resize };
		^vws;
	}

	setView { |view, value, active = false|
		view[ \box ].value = value;
		if( active ) { view.doAction };
	}

}

+ BoolSpec {	
	
	makeView { |parent, bounds, label, action, resize| 
		var vws, view, labelWidth;
		vws = ();
		
		// this is basically an EZButton
		
		bounds.isNil.if{bounds= 160@20};
		
		#view, bounds = EZGui().prMakeMarginGap.prMakeView( parent, bounds );
		 vws[ \view ] = view;
		 		
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
		
		if( trueLabel.isNil && falseLabel.isNil ) {
			vws[ \buttonView ] = SmoothButton( vws[ \view ], 
					Rect( labelWidth + 2, 0, bounds.height, bounds.height ) )
				.label_( [ "", 'x' ] )
		} {	
			vws[ \buttonView ] = SmoothButton( vws[ \view ], 
					Rect( labelWidth + 2, 0, bounds.width-(labelWidth+2), bounds.height ) )
				.label_( [ falseLabel, trueLabel ] );
		};
		
		vws[ \buttonView ]
				.radius_( bounds.height / 8 )
				.value_( this.unmap( this.constrain( default ) ) )
				.action_({ |bt| action.value( vws, this.map( bt.value ) ) })
				.resize_( 1 );

		if( resize.notNil ) { vws[ \view ].resize = resize };
		^vws;
	}
	
	setView { |view, value, active = false|
		view[ \buttonView ].value = this.unmap( this.constrain( value ) );
		if( active ) { view[ \buttonView ].doAction };
	}
	
	mapSetView { |view, value, active = false|
		view[ \buttonView ].value = this.map(  value );
		if( active ) { view[ \buttonView ].doAction };
	}
}
	
+ PointSpec {
	
	makeView { |parent, bounds, label, action, resize|
		var vws, view, labelWidth;
		var localStep;
		var startVal;
		vws = ();
		
		vws[ \val ] = 0@0;
		
		localStep = step.copy;
		if( step.x == 0 ) { localStep.x = 1 };
		if( step.y == 0 ) { localStep.y = 1 };
		
		bounds.isNil.if{bounds= 160@20};
		
		#view, bounds = EZGui().prMakeMarginGap.prMakeView( parent, bounds );
		 vws[ \view ] = view;
		 		
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
		
		vws[ \x ] = SmoothNumberBox( vws[ \view ], Rect( labelWidth + 2, 0, 40, bounds.height ) )
			.action_({ |nb|
				vws[ \val ] = nb.value @ vws[ \y ].value;
				action.value( vws, vws[ \val ]);
			})
			//.step_( localStep.x )
			.scroll_step_( localStep.x )
			.clipLo_( rect.left )
			.clipHi_( rect.right )
			.value_(0);
			
		vws[ \xy ] = XYView( vws[ \view ], 
			Rect( labelWidth + 2 + 42, 0, bounds.height, bounds.height ) )
			.action_({ |xy|
				startVal = startVal ?? { vws[ \val ].copy; };
				vws[ \x ].value = (startVal.x + (xy.x * localStep.x))
					.clip( rect.left, rect.right );
				vws[ \y ].value = (startVal.y + (xy.y * localStep.y.neg))
					.clip( rect.top, rect.bottom );
				action.value( vws, vws[ \x ].value @ vws[ \y ].value );
			})
			.mouseUpAction_({
				vws[ \val ] = vws[ \x ].value @ vws[ \y ].value;
				startVal = nil;
			});
			
		vws[ \y ] = SmoothNumberBox( vws[ \view ], 
				Rect( labelWidth + 2 + 42 + bounds.height + 2, 0, 40, bounds.height ) )
			.action_({ |nb|
				vws[ \val ] = vws[ \x ].value @ nb.value;
				action.value( vws,  vws[ \val ] );
			})
			//.step_( localStep.y )
			.scroll_step_( localStep.y )
			.clipLo_( rect.top )
			.clipHi_( rect.bottom )
			.value_(0);
				
		^vws;
	}
	
	setView { |view, value, active = false|
		var constrained;
		constrained = this.constrain( value );
		view[ \x ].value = constrained.x;
		view[ \y ].value = constrained.y;
		view[ \val ] = constrained;
		if( active ) { view[ \x ].doAction };
	}
	
	mapSetView { |view, value, active = false|
		var mapped;
		mapped = this.map( value );
		view[ \x ].value = mapped.x;
		view[ \y ].value = mapped.y;
		view[ \val ] = mapped;
		if( active ) { view[ \x ].doAction };
	}
	
}

+ RealVector3DSpec {

    makeView { |parent, bounds, label, action, resize|
		var vws, view, labelWidth;
		var localStep;
		var startVal;
		vws = ();

		vws[ \val ] = RealVector3D[0,0,0];

		localStep = step.collect{ |x| if(x == 0 ){ 1 }{ x } };

		bounds.isNil.if{bounds= 160@20};

		#view, bounds = EZGui().prMakeMarginGap.prMakeView( parent, bounds );
		 vws[ \view ] = view;

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

		vws[ \coord ] = 3.collect{ |i|

            SmoothNumberBox( vws[ \view ], Rect( labelWidth + 2 + (i*42), 0, 40, bounds.height ) )
                .action_({ |nb|
                    vws[ \val ] = vws[ \coord ].collect(_.value).as(RealVector3D);
                    action.value( vws, vws[ \val ]);
                })
                //.step_( localStep[i] )
                .scroll_step_( localStep[i] )
                .clipLo_( nrect.sortedConstraints[i][0] )
                .clipHi_( nrect.sortedConstraints[i][1] )
                .value_(0);
         };

		^vws;
	}

	setView { |view, value, active = false|
		var constrained;
		constrained = this.constrain( value );
		[view[ \coord ], constrained].flopWith(_.value(_));
		view[ \val ] = constrained;
		if( active ) { view[ \x ].doAction };
	}

	mapSetView { |view, value, active = false|
		var mapped;
		mapped = this.map( value );
		[view[ \coord ], mapped].flopWith(_.value(_));
        view[ \val ] = mapped;
		if( active ) { view[ \x ].doAction };
	}

}

+ UnitSphericalSpec {
    	makeView { |parent, bounds, label, action, resize|
		var vws, view, labelWidth;
		var localStep;
		var startVal;
		vws = ();

		vws[ \val ] = UnitSpherical(0,0);

		localStep = step.copy;
		if( step.theta == 0 ) { localStep.theta = 1 };
		if( step.phi == 0 ) { localStep.phi = 1 };

		bounds.isNil.if{bounds= 160@20};

		#view, bounds = EZGui().prMakeMarginGap.prMakeView( parent, bounds );
		 vws[ \view ] = view;

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

		vws[ \theta ] = SmoothNumberBox( vws[ \view ], Rect( labelWidth + 2, 0, 40, bounds.height ) )
			.action_({ |nb|
				vws[ \val ] = UnitSpherical( nb.value, vws[ \phi ].value );
				action.value( vws, vws[ \val ]);
			})
			//.step_( localStep.theta )
			.scroll_step_( localStep.theta )
			.value_(0);

		vws[ \thetaphi ] = XYView( vws[ \view ],
			Rect( labelWidth + 2 + 42, 0, bounds.height, bounds.height ) )
			.action_({ |xy|
				startVal = startVal ?? { vws[ \val ].copy; };
				vws[ \theta ].value = (startVal.theta + (xy.x * localStep.theta));
				vws[ \phi ].value = (startVal.phi + (xy.y * localStep.phi.neg));
				action.value( vws, UnitSpherical( vws[ \theta ].value, vws[ \phi ].value ) );
			})
			.mouseUpAction_({
				vws[ \val ] = UnitSpherical( vws[ \theta ].value, vws[ \phi ].value );
				startVal = nil;
			});

		vws[ \phi ] = SmoothNumberBox( vws[ \view ],
				Rect( labelWidth + 2 + 42 + bounds.height + 2, 0, 40, bounds.height ) )
			.action_({ |nb|
				vws[ \val ] = UnitSpherical( vws[ \theta ].value, nb.value );
				action.value( vws,  vws[ \val ] );
			})
			//.step_( localStep.phi )
			.scroll_step_( localStep.phi )
			.value_(0);

		^vws;
	}

	setView { |view, value, active = false|
		var constrained;
		constrained = this.constrain( value );
		view[ \theta ].value = constrained.theta;
		view[ \phi ].value = constrained.phi;
		view[ \val ] = constrained;
		if( active ) { view[ \theta ].doAction };
	}

	mapSetView { |view, value, active = false|
		var mapped;
		mapped = this.map( value );
		view[ \x ].value = mapped.theta;
		view[ \y ].value = mapped.phi;
		view[ \val ] = mapped;
		if( active ) { view[ \x ].doAction };
	}

}

+ RectSpec {
	
	makeView { |parent, bounds, label, action, resize|
		var vws, view, labelWidth, val = 0@0, wh = 0@0;
		var localStep, setCenter, setWH;
		var font;
		vws = ();
		
		font = Font( Font.defaultSansFace, 10 );
		
		localStep = 0.01@0.01;
		
		vws[ \rect ] = this.default;
		
		setCenter = { |center|
			vws[ \rect ] = vws[ \rect ].center_( center );
		};
		
		setWH = { |whx|
			vws[ \rect ].centeredExtent_( whx );
		};
		
		bounds.isNil.if{bounds= 320@20};
		
		#view, bounds = EZGui().prMakeMarginGap.prMakeView( parent, bounds );
		 vws[ \view ] = view;
		 
		view.addFlowLayout( 0@0, 2@2 );
		 		
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
		
		vws[ \centerLabel ] = StaticText( vws[ \view ], 12 @ bounds.height )
			.string_( "c" )
			.align_( \right )
			.font_( font )
			.applySkin( RoundView.skin );
		
		vws[ \x ] = SmoothNumberBox( vws[ \view ], 40 @ bounds.height )
			.action_({ |nb|
				setCenter.value( nb.value @ vws[ \y ].value );
				val = vws[ \rect ].center;
				action.value( vws, vws[ \rect ]);
			})
			//.step_( localStep.x )
			.scroll_step_( localStep.x )
			.clipLo_( rect.left )
			.clipHi_( rect.right )
			.value_(0);
			
		vws[ \xy ] = XYView( vws[ \view ],  bounds.height @ bounds.height )
			.action_({ |xy|
				vws[ \x ].value = (val.x + (xy.x * localStep.x))
					.clip( rect.left, rect.right );
				vws[ \y ].value = (val.y + (xy.y * localStep.y.neg))
					.clip( rect.top, rect.bottom );
				setCenter.value( val + (xy.value * localStep * (1 @ -1) ) );
				action.value( vws, vws[ \rect ] );
			})
			.mouseUpAction_({
				val = vws[ \x ].value @ vws[ \y ].value;
			});
			
		vws[ \y ] = SmoothNumberBox( vws[ \view ], 40 @ bounds.height )
			.action_({ |nb|
				setCenter.value( vws[ \x ].value @ nb.value );
				val = vws[ \rect ].center;
				action.value( vws, vws[ \rect ] );
			})
			//.step_( localStep.y )
			.scroll_step_( localStep.y )
			.clipLo_( rect.top )
			.clipHi_( rect.bottom )
			.value_(0);
			
		vws[ \whLabel ] = StaticText( vws[ \view ], 20 @ bounds.height )
			.string_( "w/h" )
			.align_( \right )
			.font_( font )
			.applySkin( RoundView.skin );
			
		vws[ \width ] = 
			SmoothNumberBox( vws[ \view ], 40 @ bounds.height )
			.action_({ |nb|
				setWH.value( nb.value @ vws[ \height ].value );
				wh = vws[ \rect ].extent;
				action.value( vws, vws[ \rect ]);
			})
			//.step_( localStep.x )
			.scroll_step_( localStep.x )
			.clipLo_( 0)
			.clipHi_( rect.right )
			.value_(0);
				
		vws[ \wh ] = XYView( vws[ \view ], bounds.height @ bounds.height )
			.action_({ |xy|
				vws[ \width ].value = (wh.x + (xy.x * localStep.x))
					.clip( 0, rect.width );
				vws[ \height ].value = (wh.y + (xy.y * localStep.y.neg))
					.clip( 0, rect.height );
				setWH.value( wh + (xy.value * localStep * (1 @ -1) ) );
				action.value( vws, vws[ \rect ] );
			})
			.mouseUpAction_({
				wh = vws[ \width ].value @ vws[ \height ].value;
			});

		vws[ \height ] = 
			SmoothNumberBox( vws[ \view ], Rect( labelWidth + 2, 0, 40, bounds.height ) )
			.action_({ |nb|
				setWH.value( vws[ \width ].value @ nb.value );
				wh = vws[ \rect ].extent;
				action.value( vws, vws[ \rect ]);
			})
			//.step_( localStep.x )
			.scroll_step_( localStep.x )
			.clipLo_( 0 )
			.clipHi_( rect.right )
			.value_(0);
				
		^vws;
	}
	
	setView { |view, value, active = false|
		var constrained;
		constrained = this.constrain( value );
		view[ \rect ] = constrained.copy;
		view[ \x ].value = constrained.center.x;
		view[ \y ].value = constrained.center.y;
		view[ \width ].value = constrained.width;
		view[ \height ].value = constrained.height;
		if( active ) { view[ \x ].doAction };
	}
	
	mapSetView { |view, value, active = false|
		var mapped;
		mapped = this.map( value );
		view[ \rect ] = mapped.copy;
		view[ \x ].value = mapped.center.x;
		view[ \y ].value = mapped.center.y;
		view[ \width ].value = mapped.width;
		view[ \height ].value = mapped.height;
		if( active ) { view[ \x ].doAction };
	}
	

}

+ RangeSpec {
	
	makeView { |parent, bounds, label, action, resize|
		var vw = EZSmoothRanger( parent, bounds, label !? { label.asString ++ " " }, 
			this.asControlSpec, 
			{ |sl| sl.value = this.constrain( sl.value ); action.value(sl, sl.value) },
			labelWidth: (RoundView.skin ? ()).labelWidth ? 80
			).value_( this.default );
		// later incorporate rangeSpec into EZSmoothRanger
		if( resize.notNil ) { vw.view.resize = resize };
		^vw;		
	}
	
	setView { |vws, value, active = false|
		vws.value = value;
		if( active ) { vws.doAction };
	}
	
	mapSetView { |vws, value, active = false|
		vws.value = this.map(value);
		if( active ) { vws.doAction };
	}
	
	adaptFromObject { |object|
		if( object.isArray.not ) {
			^this.asControlSpec.adaptFromObject( object );
		} {	
			if(  (object.minItem < minval) or: (object.maxItem > maxval) ) {
				^this.copy
					.minval_( minval.min( object.minItem ) )
					.maxval_( maxval.max( object.maxItem ) )
			};
			^this;
		};
	}

}

+ BufSndFileSpec {
	
	viewNumLines { ^BufSndFileView.viewNumLines }
	
	viewClass { ^BufSndFileView }
	
	makeView { |parent, bounds, label, action, resize|
		var vws, view, labelWidth;
		
		vws = ();
		
		bounds.isNil.if{bounds= 350 @ (this.viewNumLines * 18) };
		
		#view, bounds = EZGui().prMakeMarginGap.prMakeView( parent, bounds );
		 vws[ \view ] = view;
		 view.addFlowLayout(0@0, 4@4);
		if( label.notNil ) {
			labelWidth = (RoundView.skin ? ()).labelWidth ? 80;
			vws[ \labelView ] = StaticText( vws[ \view ], labelWidth @ 14 )
				.string_( label.asString ++ " " )
				.align_( \right )
				.resize_( 4 )
				.applySkin( RoundView.skin );
		} {
			labelWidth = -4;
		};
		
		if( resize.notNil ) { vws[ \view ].resize = resize };
		
		vws[ \sndFileView ] = this.viewClass.new( vws[ \view ], 
			( bounds.width - (labelWidth+4) ) @ bounds.height, { |vw|
				action.value( vw, vw.value )
			} )
		
		^vws;
	}
	
	setView { |view, value, active = false|
		view[ \sndFileView ].value = value;
		if( active ) { view.doAction };
	}
}

+ DiskSndFileSpec {
	viewClass { ^DiskSndFileView }
}

+ MultiSndFileSpec {
	
	makeView { |parent, bounds, label, action, resize|
		var vws, view, labelWidth;
		var localStep;
		var font;
		var editAction;
		vws = ();
		
		font =  (RoundView.skin ? ()).font ?? { Font( Font.defaultSansFace, 10 ); };
		
		bounds.isNil.if{bounds= 320@20};
		
		view = EZCompositeView( parent, bounds, gap: 4@4 );
		bounds = view.asView.bounds;
		
		vws[ \view ] = view;
		
		vws[ \val ] = this.default ? [];
		
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

				
		editAction = { |vw|
			vws[ \val ] = vw.object;
			action.value( vws, vws[ \val ] );
		};
		
		vws[ \list ] = SmoothButton( view, 40 @ (bounds.height) )
			.label_( "list" )
			.border_( 1 )
			.radius_( 2 )
			.font_( font )
			.action_({
				if( vws[ \listdoc ].notNil ) {
					vws[ \listdoc ].close;
				};
				
				vws[ \listdoc ] = Document().string_(
					vws[ \val ].collect(_.path).join("\n")
				).promptToSave_(false);
			});
			
		view.view.onClose_({
			if( vws[ \listdoc ].notNil ) {
				vws[ \listdoc ].close;
			};
		});
		
		vws[ \copy ] = SmoothButton( view, 60 @ (bounds.height) )
			.label_( "copy all" )
			.border_( 1 )
			.radius_( 2 )
			.font_( font )
			.action_({
				var paths;
				Dialog.savePanel({ |path|
					path = path.dirname;
					paths = vws[ \val ].collect({ |item| 
						item.path.getGPath.asSymbol
					}).as(Set).as(Array).do({ |pth|
						pth.asString.copyTo( path );
					});
					vws[ \val ].do({ |item|
						item.path = path +/+ item.path.basename;
					});
				});
			});
			
		view.view.onClose_({
			if( vws[ \listdoc ].notNil ) {
				vws[ \listdoc ].close;
			};
		});

	
		^vws;
	}
	
	setView { |view, value, active = false|
		view[ \val ] = value;
	}
}

+ PartConvBufferSpec {
	
	viewNumLines { ^PartConvBufferView.viewNumLines }
	
	viewClass { ^PartConvBufferView }
	
	makeView { |parent, bounds, label, action, resize|
		var vws, view, labelWidth;
		
		vws = ();
		
		bounds.isNil.if{bounds= 350 @ (this.viewNumLines * 18) };
		
		#view, bounds = EZGui().prMakeMarginGap.prMakeView( parent, bounds );
		 vws[ \view ] = view;
		 view.addFlowLayout(0@0, 4@4);
		if( label.notNil ) {
			labelWidth = (RoundView.skin ? ()).labelWidth ? 80;
			vws[ \labelView ] = StaticText( vws[ \view ], labelWidth @ 14 )
				.string_( label.asString ++ " " )
				.align_( \right )
				.resize_( 4 )
				.applySkin( RoundView.skin );
		} {
			labelWidth = -4;
		};
		
		if( resize.notNil ) { vws[ \view ].resize = resize };
		
		vws[ \bufferView ] = this.viewClass.new( vws[ \view ], 
			( bounds.width - (labelWidth+4) ) @ bounds.height, { |vw|
				action.value( vw, vw.value )
			} )
		
		^vws;
	}
	
	setView { |view, value, active = false|
		view[ \bufferView ].value = value;
		if( active ) { view.doAction };
	}
}

+ IntegerSpec {

	makeView { |parent, bounds, label, action, resize|
		var vws, view, labelWidth;
		
		bounds.isNil.if{bounds= 160 @ 18 };
		
		vws = ();
		#view, bounds = EZGui().prMakeMarginGap.prMakeView( parent, bounds );
		vws[ \view ] = view;
		
		if( label.notNil ) {
			labelWidth = (RoundView.skin ? ()).labelWidth ? 80;
			vws[ \labelView ] = StaticText( vws[ \view ], labelWidth @ 14 )
				.string_( label.asString ++ " " )
				.align_( \right )
				.resize_( 4 )
				.applySkin( RoundView.skin );
		} {
			labelWidth = -4;
		};
		
		vws[ \box ] = SmoothNumberBox( vws[ \view ], 
				Rect(labelWidth + 2,0,bounds.width-(labelWidth + 2),bounds.height)
			)
		    .action_({ |vw|
		        action.value( vw, vw.value );
		    } ).resize_(5)
		    .allowedChars_( "" )
			.step_( step )
			.scroll_step_( step )
			.alt_scale_( alt_step / step )
			.clipLo_( this.minval )
			.clipHi_( this.maxval );
		    
		if( resize.notNil ) { vws[ \view ].resize = resize };
		^vws;
	}

	setView { |view, value, active = false|
		view[ \box ].value = value;
		if( active ) { view.doAction };
	}

}

+ FreqSpec {
	
	makeView { |parent, bounds, label, action, resize|
		var vws, view, labelWidth;
		var localStep;
		var modeFunc;
		var font;
		var editAction;
		var tempVal;
		vws = ();
		
		font =  (RoundView.skin ? ()).font ?? { Font( Font.defaultSansFace, 10 ); };
		
		localStep = step.copy;
		if( step == 0 ) { localStep = 1 };
		bounds.isNil.if{bounds= 320@20};
		
		view = EZCompositeView( parent, bounds, gap: 2@2 );
		bounds = view.asView.bounds;
				
		vws[ \view ] = view;
		
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
		
		vws[ \comp ] = CompositeView( view, (bounds.width - (labelWidth + 49)) @ (bounds.height) );
		
		vws[ \mode ] = PopUpMenu( view, 45 @ (bounds.height) )
			.font_( font )
			.applySkin( RoundView.skin ? () )
			.items_([ 'hz', 'midi', 'note' ])
			.action_({ |pu|
				mode = pu.item;
				this.setMode( vws, mode );
			});
		
		// hz mode
		vws[ \hz ] = EZSmoothSlider( vws[ \comp ], 
			vws[ \comp ].bounds.width @ (bounds.height),
			nil,  this, { |vw| action.value( vw, vw.value ) }
		).visible_( false );
		
		vws[ \hz ].sliderView.centered_( true ).centerPos_( this.unmap( default ) );
		
		// midi mode
		vws[ \midi ] =  EZSmoothSlider( vws[ \comp ], 
			vws[ \comp ].bounds.width @ (bounds.height),
			nil, 
			[ this.minval.cpsmidi, this.maxval.cpsmidi, \lin, 0.01, this.default.cpsmidi ].asSpec,
			{ |vw| action.value( vw, vw.value.midicps ) }
		).visible_( false );
		
		vws[ \midi ].sliderView.centered_( true ).centerPos_( 
			vws[ \midi ].controlSpec.unmap( default.cpsmidi ) 
		);
				
		// note mode
		vws[ \note ] = SmoothNumberBox( vws[ \comp ], 40 @ (bounds.height) )
			.action_({ |nb|
				action.value( vws, nb.value.midicps * (vws[ \cents ].value / 100).midiratio );
			})
			.scroll_step_( localStep )
			.clipLo_( this.minval.cpsmidi )
			.clipHi_( this.maxval.cpsmidi )
			.value_( 440.cpsmidi )
			.formatFunc_({ |val|
				val.midiname;
			})
			.interpretFunc_({ |string|
				string.namemidi;
			})
			.allowedChars_( "abcdefgABCDEFG#-" )
			.visible_( false );
			
		vws[ \cents ] = EZSmoothSlider( vws[ \comp ], 
				Rect( 44, 0, (vws[ \comp ].bounds.width - 44), bounds.height ),
				nil, [-50,50,\lin,0.1,0].asSpec
			).action_({ |sl|
				action.value( vws, vws[ \note ].value.midicps * (sl.value / 100).midiratio );
			})
			.visible_( false );
		
		vws[ \cents ].sliderView.centered_(true);
			
		this.setMode( vws, mode );
	
		^vws;
	}
	
	setMode { |view, newMode|
		[ \hz, \midi, \note ].do({ |item|
			view[ item ].visible = (item == newMode)	
		});
		view[ \cents ].visible = (newMode == \note);
	}
	
	setView { |view, value, active = false|
		view[ \hz ].value = value;
		view[ \midi ].value = value.cpsmidi;
		view[ \note ].value = value.cpsmidi.round(1);
		view[ \cents ].value = (value.cpsmidi - (view[ \note ].value)) * 100;
		{ 
			this.setMode( view, mode );
			view[ \mode ].value = view[ \mode ].items.indexOf( mode ) ? 0; 
		}.defer;
		if( active ) { view[ \hz ].doAction };
	}
	
	mapSetView { |view, value, active = false|
		this.setView( view, this.map( value ), active );
	}
}



+ EZPopUpMenu {
	
	labelWidth { ^labelView !? { labelView.bounds.width } ? 0 }
	
	labelWidth_ { |width = 80|
		var delta;
		if( layout === \horz && { labelView.notNil } ) { // only for horizontal sliders
			delta = labelView.bounds.width - width;
			labelView.bounds = labelView.bounds.width_( width );
			widget.bounds = widget.bounds
				.width_( widget.bounds.width + delta )
				.left_( widget.bounds.left - delta );
		};
	}
}