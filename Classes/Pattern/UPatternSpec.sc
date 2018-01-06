UPatternSpec : Spec {
	
	classvar <>specs;
	
	*initClass {
		specs = [
			ArgSpec( 'sustain', 1, SMPTESpec(0, 3600), false, \init ),
			ArgSpec( 'timeToNext', 1, SMPTESpec(0.01, 3600), false, \init )
		];
	}
	
	constrain { |value| 
		value = value.asArray.wrapExtend(2);
		^value.collect({ |val, i| specs[i].spec.constrain( val ) });
	}
	
	default { ^[1,1] }
	
	expandArgSpecs { ^specs }
	
	expandValues { |obj|
		^obj
	}
	
	objFromExpandValues { |values|
		^values;
	}
	
	map { |value| ^value }
	unmap { |value| ^value }
	
	asControlSpec { ^ControlSpec( 0, 3600, \lin, 0, 1 ) }
	
	viewNumLines { ^2 }

	makeView { |parent, bounds, label, action, resize|
		var vws, view, labelWidth, halfHeight;
		
		bounds.isNil.if{bounds= 160 @ 18 };
		
		vws = ();
		#view, bounds = EZGui().prMakeMarginGap.prMakeView( parent, bounds );
		vws[ \view ] = view;
		vws[ \val ] = [1,1];
		
		if( label.notNil ) {
			labelWidth = (RoundView.skin ? ()).labelWidth ? 80;
			vws[ \labelView ] = StaticText( vws[ \view ], labelWidth @ 14 )
				.string_( label.asString ++ " " )
				.align_( \right )
				.resize_( 4 )
				.applySkin( RoundView.skin );
		} {
			labelWidth = -2;
		};
		
		halfHeight = (bounds.height/2);
		
		vws[ \specViews ] = specs.collect({ |argSpec, i|
			var view;
			view = argSpec.makeView( 
				vws[ \view ], 
				Rect(
					labelWidth + 2, 
					i * halfHeight, 
					bounds.width-(labelWidth + 2 + halfHeight + 2), 
					halfHeight-2
				),
				nil,
				{ |vw|
					vws[ \val ] = vws[ \val ].copy.put(i,vw.value);
					action.value( vws, vws[ \val ] );
				}
			);
			if( i == 0 ) {
				vws[ \expand ] = SmoothButton( vws[ \view ], 
					Rect( bounds.width - (halfHeight - 2), 0, halfHeight - 4, halfHeight - 4 ) )
				.label_( '+' )
				.action_({
					action.value( vws, UMap( \sustain_time ) )
				});
			};
			view;
		});
		
		vws[ \doAction ] = { action.value( vws, vws[ \val ] ); };
		
		^vws;
	}

	setView { |view, value, active = false|
		view[ \specViews ][0][\box].value = value[0];
		view[ \specViews ][1][\box].value = value[1];
		view[ \val ] = value;
		if( active ) { view.doAction };
	}
}

UFadeTimesSpec : Spec {
	
	classvar <>specs;
	
	*initClass {
		specs = [
			ArgSpec( 'fadeIn', 0, SMPTESpec(0, 3600), false, \init ),
			ArgSpec( 'fadeOut', 0, SMPTESpec(0, 3600), false, \init )
		];
	}
	
	constrain { |value| 
		value = value.asArray.wrapExtend(2);
		^value.collect({ |val, i| specs[i].spec.constrain( val ) });
	}
	
	default { ^[0,0] }
	
	expandArgSpecs { ^specs }
	
	expandValues { |obj|
		^obj
	}
	
	objFromExpandValues { |values|
		^values;
	}
	
	map { |value| ^value }
	unmap { |value| ^value }
	
	asControlSpec { ^ControlSpec( 0, 3600, \lin, 0, 1 ) }
	
	viewNumLines { ^1 }

	makeView { |parent, bounds, label, action, resize|
		var vws, view, labelWidth, halfHeight;
		
		bounds.isNil.if{bounds= 160 @ 18 };
		
		vws = ();
		#view, bounds = EZGui().prMakeMarginGap.prMakeView( parent, bounds );
		vws[ \view ] = view;
		vws[ \val ] = [0,0];
		
		if( label.notNil ) {
			labelWidth = (RoundView.skin ? ()).labelWidth ? 80;
			vws[ \labelView ] = StaticText( vws[ \view ], labelWidth @ 14 )
				.string_( label.asString ++ " " )
				.align_( \right )
				.resize_( 4 )
				.applySkin( RoundView.skin );
		} {
			labelWidth = -2;
		};
		
		vws[ \fadeIn ] = SmoothNumberBox( vws[ \view ], Rect( labelWidth + 2, 0, 40, 14 ) )
			.clipLo_(0)
			.scroll_step_(0.1)
			.formatFunc_( { |value| [ value.round(0.01), "s" ].join(" ") } )
			.background_( { |rect|
				var fadeInCurve = 0;
				if( UGUI.nowBuildingUnit.isKindOf( UPattern ) ) {
					fadeInCurve = UGUI.nowBuildingUnit.fadeInCurve;
				};
				Pen.use({
					var values;
					Pen.roundedRect( rect, 2 ).clip;
					Pen.color = Color(1.0, 1.0, 1.0, 0.5);
					Pen.fillRect( rect );
					values = (rect.width.asInt + 1).collect({ |i|
						i.lincurve(0, rect.width, rect.bottom, rect.top, fadeInCurve )
					});
					Pen.moveTo( rect.leftBottom );
					values.do({ |item, i|
						Pen.lineTo( (rect.left + i) @ item );
					});
					Pen.lineTo( rect.rightBottom );
					Pen.lineTo( rect.leftBottom );
					Pen.color = Color(0.5,0.5,0.5, if( vws[ \val ][0] > 0 ) { 0.5 } { 0.125 } );
					Pen.fill;
				});
			})
			.action_({ |nb|
				vws[ \val ][0] = nb.value;
				action.value( vws, vws[ \val ] );
			});
			
		vws[ \fadeOut ] = SmoothNumberBox( vws[ \view ], Rect( labelWidth + 2 + 44, 0, 40, 14 ) )
			.clipLo_(0)
			.scroll_step_(0.1)
			.formatFunc_( { |value| [ value.round(0.01), "s" ].join(" ") } )
			.background_( { |rect|
				var fadeOutCurve = 0;
				if( UGUI.nowBuildingUnit.isKindOf( UPattern ) ) {
					fadeOutCurve = UGUI.nowBuildingUnit.fadeOutCurve;
				};
				Pen.use({
					var values;
					Pen.roundedRect( rect, 2 ).clip;
					Pen.color = Color(1.0, 1.0, 1.0, 0.5);
					Pen.fillRect( rect );
					values = (rect.width.asInt + 1).collect({ |i|
						i.lincurve(0, rect.width, rect.top, rect.bottom, fadeOutCurve )
					});
					Pen.moveTo( rect.leftBottom );
					values.do({ |item, i|
						Pen.lineTo( (rect.left + i) @ item );
					});
					Pen.lineTo( rect.rightBottom );
					Pen.lineTo( rect.leftBottom );
					Pen.color = Color(0.5,0.5,0.5, if( vws[ \val ][1] > 0 ) { 0.5 } { 0.125 } );
					Pen.fill;
				});
			})
			.action_({ |nb|
				vws[ \val ][1] = nb.value;
				action.value( vws, vws[ \val ] );
			});
			
		vws[ \expand ] = SmoothButton( vws[ \view ], 
				Rect( bounds.width - bounds.height, 0, bounds.height - 2, bounds.height - 2 ) )
			.label_( '+' )
			.action_({
				action.value( vws, UMap( \expand, [ \fadeIn, vws[ \val ][0], \fadeOut, vws[ \val ][1] ]  ) )
			});
		
		vws[ \doAction ] = { action.value( vws, vws[ \val ] ); };
		
		^vws;
	}

	setView { |view, value, active = false|
		view[ \val ] = value;
		view[ \fadeIn ].value = value[0];
		view[ \fadeOut ].value = value[1];
		if( active ) { view.doAction };
	}
}