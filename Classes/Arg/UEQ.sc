UEQ : EQSetting {

	classvar <>allUnits;

	var <>unitArgName;

	 *initClass {
	    allUnits = IdentityDictionary();
	}

	set { |name, argName, value, constrain = true|
		super.set( name, argName, value, constrain );
		this.unitSet;
	}

	put { |...args|
		super.put( *args );
		this.unitSet;
	}

	setting_ { |new|
		super.setting_( new );
		this.unitSet;
	}

	asUnitArg { |unit, key|
		this.unitArgName = key; ^this;
	}

	unit_ { |aUnit|
		if( aUnit.notNil ) {
			case { this.unit == aUnit } {
				// do nothing
			} { allUnits[ this ].isNil } {
				allUnits[ this ] = [ aUnit, nil ];
			} {
				"Warning: unit_ \n%\nis already being used by\n%\n".postf(
					this.class,
					this.asCompileString,
					this.unit
				);
			};
		} {
			allUnits[ this ] = nil; // forget unit
		};
	}

	unit { ^allUnits[ this ] !? { allUnits[ this ][0] }; }

	unitSet { // sets this object in the unit to enforce setting of the synths
		if( this.unit.notNil ) {
				if( this.unitArgName.notNil ) {
				this.unit.set( this.unitArgName, this );
			};
		};
	}

	disposeFor {
		if( this.unit.notNil && { this.unit.synths.size == 0 }) {
			this.unit = nil;
		};
	}

	dispose {
		this.unit = nil;
	}

	asUEQ { ^this }

	*ar { |in, key, def, setting|
		^this.new( def, setting ).ar( in, key );
	}

	 ar { |in, key|
		key = (key ? \eq).asSymbol;
		Udef.addBuildSpec( ArgSpec( key, setting, UEQSpec( def, this ) ) );
	 	^super.ar( in, key.kr( setting.flat ) )
	 }
}

UEQSpec : Spec {

	var <>def, <>default;

	*new { |def, default|
		def = def ? \default;
		default = default ?? { UEQ( def ) };
		^super.newCopyArgs.def_( def ).default_( default );
	}

	constrain { |value|
		^value.asUEQ( def );
	}

	makeView { |parent, bounds, label, action, resize|
		var vws, view, labelWidth;
		var localStep;
		var modeFunc;
		var font;
		var editAction;
		var tempVal;
		var skin;
		vws = ();

		skin = RoundView.skin;
		font =  skin.font ?? { Font( Font.defaultSansFace, 10 ); };

		bounds.isNil.if{bounds= 160@20};

		view = EZCompositeView( parent, bounds, gap: 2@2 );
		bounds = view.asView.bounds;

		vws[ \view ] = view;
		vws[ \val ] = UEQ( def );

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

		StaticText( view, 60 @ (bounds.height) )
			.applySkin( RoundView.skin )
			.string_( def );

		vws[ \edit ] = SmoothButton( view, 60 @ (bounds.height) )
		.label_([{ |vw, bounds|
			var freqs, center, values, eqSetting, svals, min = 20, max = 20000, range = 12;
			eqSetting = vws[ \val ];
			bounds = vw.bounds;
			center = vw.bounds.height / 2;
			freqs = ({|i| i } ! (bounds.width+1));
			freqs = freqs.linexp(0, bounds.width, min, max );

			// get magResponses
			values = eqSetting.magResponses( freqs ).ampdb.clip(-200,200);

			// sum and scale magResponses
			svals = values.sum.linlin(range.neg,range, bounds.height, 0, \none);

			// draw summed magResponse
			Pen.strokeColor = Color.blue(0.5);
			Pen.fillColor = skin.hiliteColor ? Color.gray(0.5);
			Pen.moveTo( 0 @ center );
			svals.do({ |val, i|
				Pen.lineTo( i@val );
			});
			Pen.lineTo( (bounds.width) @ center );

			Pen.fillStroke;
		}])
			.border_( 1 )
			.radius_( 2 )
			.font_( font )
			.action_({
				var editor;
				if( vws[ \editor ].isNil or: { vws[ \editor ].isClosed } ) {
					RoundView.pushSkin( skin );
					editor = EQView( "UEQ % editor".format( def ), eqSetting: vws[ \val ] )
						.onClose_({
							if( vws[ \editor ] == editor ) {
								vws[ \editor ] = nil;
							};
						}).action_({ |vw, val| action.value( vws, val ) });
					RoundView.popSkin;
					vws[ \editor ] = editor;
				} {
					vws[ \editor ].front;
				};

			});

		view.view.onClose_({
			if( vws[ \editor ].notNil ) {
				vws[ \editor ].close;
			};
		});

		^vws;
	}

	map { |value| ^if( value.isKindOf( UEQ ) ) { value } { default.copy } }
	unmap { |value| ^value }

	setView { |view, value, active = false|
		view[ \val ] = value;
		view[ \edit ].refresh;
		if( view[ \editor ].notNil ) {
			view[ \editor ].value = value;
		};
	}

}

+ Symbol {
	asUEQ { ^UEQ( this ) }
}

+ Array {
	asUEQ { |def| ^UEQ( def, this ) }
}

+ Nil {
	asUEQ { |def| ^UEQ( def ) }
}