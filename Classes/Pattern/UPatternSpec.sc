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