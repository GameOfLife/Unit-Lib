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
				action.value( UMIDIDict.portDict[ src ] ? '*/*', type, chan, num, value );
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
		var makeMenu;

		vws = ();

		makeMenu = { |action|
			var sources, menus;
			UMIDIDict.start( false );
			sources = ();
			menus = [];
			MIDIClient.sources.do({ |src|
				sources[ src.device.asSymbol ] = sources[ src.device.asSymbol ].add( src.name.asSymbol );
			});
			sources.sortedKeysValuesDo({ |key, value|
				if( value.size == 1 ) {
					menus = menus.add( MenuAction( key.asString +/+ value[0], {
						action.value( [key, value[0]].join( "/" ).asSymbol )
					}) );
				} {
					menus = menus.add( Menu(
						*value.collect({ |name|
							MenuAction( name.asString, {
								action.value( [key, name].join( "/" ).asSymbol )
							})
						})
					).title_( key.asString );
					)
				};
			});

			menus = menus.add( MenuAction( "any (*/*)", { action.value( '*/*' ) } ) );

			menus = menus.add( MenuAction.separator );
			menus = menus.add( MenuAction( "refresh", {
				UMIDIDict.restart;
			} ) );

			Menu(*menus).front;
		};

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

		vws[ \val ] = this.default.copy;

		vws[ \doAction ] = { |vwx|
			action.value( vwx, vws[ \val ] );
		};

		numchanWidth = ((bounds.width - (3 * 44) - 4 - (labelWidth+4)) / 2).floor.asInteger;

		vws[ \numLabel ] = StaticText( vws[ \view ], 40@14 )
		.string_( "num " ).align_( \right ).applySkin( RoundView.skin );
		vws[ \num ] = SmoothNumberBox( vws[ \view ], numchanWidth@14 )
		.action_({ |nb|
			vws[ \val ][ 3 ] = nb.value.asInteger;
			vws.doAction;
		})
		.clipLo_( 0 ).clipHi_(127);
		vws[ \chanLabel ] = StaticText( vws[ \view ], 40@14 )
		.string_( "chan " ).align_( \right ).applySkin( RoundView.skin );
		vws[ \chan ] = PopUpMenu( vws[ \view ], numchanWidth@14 )
		.action_({ |pu|
			if( pu.value == 0 ) {
				vws[ \val ][ 2 ] = nil;
			} {
				vws[ \val ][ 2 ] = (pu.value - 1).asInteger;
			};
			vws.doAction;
		})
		.items_( [ "any" ] ++ (0..15).collect(_.asString) )
		.applySkin( RoundView.skin );

		vws[ \learn ] = SmoothButton( vws[ \view ], 40@14 )
		.label_( [ "learn", "learn" ] )
		.radius_(2)
		.action_({ |bt|
			switch( bt.value,
				1, { vws[ \learnFunc ] = this.learn({ |...learned|
					vws[ \val ] = learned;
					{ vws.setViews; }.defer;
					vws.doAction;
					bt.value = 0;
				});
				},
				0, { learnFuncs.remove( vws[ \learnFunc ] ); }
			);
		});

		vws[ \view ].decorator.nextLine;
		if( label.notNil ) { vws[ \view ].decorator.shift( labelWidth + 4, 0 ); };
		vws[ \devLabel ] = StaticText( vws[ \view ], 40@14 )
		.mouseDownAction_({
			makeMenu.value({ |res|
				vws[ \val ][ 0 ] = res;
				vws.setViews;
				vws.doAction;
			});
		})
		.string_( "device " ).align_( \right ).applySkin( RoundView.skin )
		.background_( Color.white.alpha_(0.25) );
		vws[ \device ] = TextField( view, bounds.width - 44 - (labelWidth+4) @ 14 )
		.string_( "*" )
		.action_({ |vw|
			if( vw.string.size == 0 ) {
				vw.string = "*";
			};
			vws[ \val ][ 0 ] = (vws[ \device ].string +/+ vws[ \port ].string).asSymbol;
			vws.doAction;
		})
		.applySkin( RoundView.skin );

		vws[ \view ].decorator.nextLine;
		if( label.notNil ) { vws[ \view ].decorator.shift( labelWidth + 4, 0 ); };
		vws[ \portLabel ] = StaticText( vws[ \view ], 40@14 )
		.mouseDownAction_({
			makeMenu.value({ |res|
				vws[ \val ][ 0 ] = res;
				vws.setViews;
				vws.doAction;
			});
		})
		.string_( "port " ).align_( \right ).applySkin( RoundView.skin )
		.background_( Color.white.alpha_(0.25) );
		vws[ \port ] = TextField( view, bounds.width - 44 - (labelWidth+4) @ 14 )
		.action_({ |vw|
			if( vw.string.size == 0 ) {
				vw.string = "*";
			};
			vws[ \val ][ 0 ] = (vws[ \device ].string +/+ vws[ \port ].string).asSymbol;
			vws.doAction;
		})
		.string_( "*" )
		.applySkin( RoundView.skin );

		vws[ \setViews ] = {
			var devStrings;
			devStrings = vws[ \val ][ 0 ].asString.split($/);
			vws[ \device ].string = devStrings.first;
			vws[ \port ].string = devStrings.last;
			if( vws[ \val ][ 2 ].notNil ) {
				vws[ \chan ].value = vws[ \val ][ 2 ]+1;
			} {
				vws[ \chan ].value = 0;
			};
			vws[ \num ].value = vws[ \val ][ 3 ];
		};

		vws.setViews;

		^vws;
	}

	setView { |view, value, active = false|
		//view[ \sndFileView ].value = value;
		//if( active ) { view.doAction };
	}

}