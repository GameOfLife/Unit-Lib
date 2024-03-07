UMIDIFilterSpec : Spec {

	classvar <>learnFuncs, <>lastUpdate;

	var <>default, <>type = 'cc';

	// srcName, type, chan, num, val

	*new { |type = 'cc'|
		^super.new.type_( type ).default_( [ '*/*', type, 0, 0, nil ] );
	}

	constrain { |input|
		input = input.asArray.extend( 5, nil );
		if( type.notNil && { input[1] != type }) {
			input[1] = type;
		};
		^input;
	}

	storeArgs { ^[ type ] }

	*update { |midiDict, type, src, chan, num, value|
		var completed;
		if( lastUpdate != [ type, src, chan, num ] ) {
			completed = learnFuncs.detect({ |item|
				item.value( type, src, chan, num, value );
			});
			learnFuncs.remove( completed );
			if( learnFuncs.size == 0 ) {
				UMIDIDict.removeDependant( this );
			};
			lastUpdate = [ type, src, chan, num ];
		};
	}

	learn { |action|
		var learnFunc;
		learnFunc = { |inType, src, chan, num, value|
			if( type.isNil or: { inType === type } ) {
				action.value( src, type, chan, num, value );
				true;
			} { false };
		};
		learnFuncs = learnFuncs.add( learnFunc );
		if( learnFuncs.size == 1 ) { lastUpdate = nil; };
		UMIDIDict.addDependant( this.class );
		UMIDIDict.start( false );
		^learnFunc;
	}

	viewNumLines { ^3 }

	makeView { |parent, bounds, label, action, resize|
		var vws, view, labelWidth, numchanWidth;

		vws = ();

		bounds.isNil.if{bounds= 350 @ (this.viewNumLines * 18) };

		#view, bounds = EZGui().prMakeMarginGap.prMakeView( parent, bounds );
		 vws[ \view ] = view;
		 view.addFlowLayout(0@0, 2@2);

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

		vws[ \val ] = this.default.copy;

		vws[ \doAction ] = { |vwx|
			action.value( vwx, vws[ \val ] );
		};

		numchanWidth = ((bounds.width - (3 * 42) - 2 - (labelWidth+2)) / 2).floor.asInteger;

		vws[ \numLabel ] = StaticText( vws[ \view ], 40@14 )
		.string_( "num " ).align_( \right ).applySkin( RoundView.skin );
		vws[ \num ] = SmoothNumberBox( vws[ \view ], numchanWidth@14 )
		.clipLo_( 0 ).clipHi_(127);
		vws[ \chanLabel ] = StaticText( vws[ \view ], 40@14 )
		.string_( "chan " ).align_( \right ).applySkin( RoundView.skin );
		vws[ \chan ] = PopUpMenu( vws[ \view ], numchanWidth@14 )
		.items_( [ "any" ] ++ (0..15).collect(_.asString) )
		.applySkin( RoundView.skin );

		vws[ \learn ] = SmoothButton( vws[ \view ], 40@14 )
		.label_( [ "learn", "learn" ] )
		.radius_(2)
		.action_({ |bt|
			switch( bt.value,
				1, { vws[ \learnFunc ] = this.learn({ |...args| args.postln; bt.value = 0 }); },
				0, { learnFuncs.remove( vws[ \learnFunc ] ); }
			);
		});

		vws[ \view ].decorator.nextLine;
		if( label.notNil ) { vws[ \view ].decorator.shift( labelWidth + 2, 0 ); };
		vws[ \devLabel ] = StaticText( vws[ \view ], 40@14 )
		.string_( "device " ).align_( \right ).applySkin( RoundView.skin );
		vws[ \device ] = TextField( view, bounds.width - 42 - (labelWidth+2) @ 14 )
		.string_( "*" )
		.applySkin( RoundView.skin );

		vws[ \view ].decorator.nextLine;
		if( label.notNil ) { vws[ \view ].decorator.shift( labelWidth + 2, 0 ); };
		vws[ \portLabel ] = StaticText( vws[ \view ], 40@14 )
		.string_( "port " ).align_( \right ).applySkin( RoundView.skin );
		vws[ \port ] = TextField( view, bounds.width - 42 - (labelWidth+2) @ 14 )
		.string_( "*" )
		.applySkin( RoundView.skin );

		^vws;
	}

	setView { |view, value, active = false|
		//view[ \sndFileView ].value = value;
		//if( active ) { view.doAction };
	}

}