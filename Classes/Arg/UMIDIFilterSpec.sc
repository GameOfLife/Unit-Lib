UMIDIFilterSpec : Spec {

	classvar <>learnFuncs, <>lastUpdate;

	var <>default, <>type = 'cc', <>useNum = true;

	// srcName, type, chan, num, val

	*new { |type = 'cc', useNum = true|
		^super.new.type_( type ).useNum_( useNum ).default_( [ '*/*', type, 0, if( useNum ) { 0 } { nil }, nil ] );
	}

	constrain { |input|
		input = input.asArray.extend( 5, nil );
		if( type.notNil && { input[1] != type }) {
			input[1] = type;
		};
		if( useNum == false) {
			input[3] = nil;
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

	viewNumLines { ^2 }

	*formatDeviceString { |inString|
		case { inString.size == 0 } {
			^"*/*";
		} { inString.find( "/" ).isNil } {
			^inString ++ "/*";
		} { inString == "/" } {
			^"*/*"
		} { inString.split( $/ ).first.size == 0 } {
			^"*" ++ inString;
		} { inString.split( $/ ).last.size == 0 } {
			^inString ++ "*";
		} { ^inString }
	}

	makeView { |parent, bounds, label, action, resize|
		var vws, view, labelWidth, numchanWidth;
		var makeMenu;

		vws = ();

		makeMenu = { |action|
			var sources, menus, current;
			UMIDIDict.start( false );
			sources = ();
			menus = [];
			MIDIClient.sources.do({ |src|
				sources[ src.device.asSymbol ] = sources[ src.device.asSymbol ].add( src.name.asSymbol );
			});
			current = vws[ \val ][ 0 ].asString.split($/).collect(_.asSymbol);
			sources.sortedKeysValuesDo({ |key, value|
				var isCurrent, portIsCurrent;
				isCurrent = (current[0] == key);
				menus = menus.add(
					Menu(
						*value.collect({ |name|
							portIsCurrent = (current[1] == name);
							MenuAction( name.asString, {
								action.value( [key, name].join( "/" ).asSymbol )
							}).enabled_( portIsCurrent.not )
						}) ++ [
							MenuAction( "any port (*)", {
								action.value( [key, "*"].join( "/" ).asSymbol )
							}).enabled_( isCurrent.not or: { current[1] != '*' }),
						]
					).title_( if( isCurrent ) { key.asString + "*" } { key.asString } );
				);
				if( isCurrent && { menus.last.actions.every(_.enabled) } ) {
					menus.last.addAction(
						MenuAction( current[1].asString ).enabled_( false )
					);
				};
			});

			menus = menus.add(
				MenuAction( "any device/port (*/*)", { action.value( '*/*' ) } )
				.enabled_( vws[ \val ][0] !== '*/*' )
			);

			menus = menus.add(
				MenuAction( "Edit...", {
					SCRequestString( vws[ \val ][ 0 ].asString, "Please enter device/port name:", { |string|
						string = this.class.formatDeviceString( string );
						vws[ \val ][ 0 ] = string.asSymbol;
						vws.doAction;
					});
				})
			);

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

		numchanWidth = ((bounds.width - (49+16) - ( 2 * 36 ) - 4 - (labelWidth+4)) / 2).floor.asInteger;

		vws[\typeLabel ] = StaticText( vws[ \view ], 45@14 )
		.string_( "% ".format( this.type ) ).align_( \right ).applySkin( RoundView.skin )
		.font_( (RoundView.skin.font ? Font.default).copy.italic_( true ) );

		vws[ \numLabel ] = StaticText( vws[ \view ], numchanWidth@14 )
		.string_( "num" ).align_( \right ).applySkin( RoundView.skin );
		vws[ \num ] = SmoothNumberBox( vws[ \view ], 40@14 )
		.action_({ |nb|
			if( nb.value.asInteger == -1 ) {
				vws[ \val ][ 3 ] = nil;
			} {
				vws[ \val ][ 3 ] = nb.value.asInteger;
			};
			vws.doAction;
		})
		.clipLo_( -1 ).clipHi_(127)
		.allowedChars_( "+-.eE*/()%any" )
		.interpretFunc_({ |string|
			if( "any".any( string.includes( _ ) ) ) { -1 } { string.interpret; };
		})
		.formatFunc_({ |val| if( val == -1 ) { "any" } { val.asInteger.asString } });
		if( useNum == false ) { vws[ \numLabel ].visible_( false );  vws[ \num ].visible_( false );  };
		vws[ \chanLabel ] = StaticText( vws[ \view ], numchanWidth@14 )
		.string_( "chan" ).align_( \right ).applySkin( RoundView.skin );
		vws[ \chan ] = SmoothNumberBox( vws[ \view ], 40@14 )
		.action_({ |nb|
			if( nb.value.asInteger == -1 ) {
				vws[ \val ][ 2 ] = nil;
			} {
				vws[ \val ][ 2 ] = nb.value.asInteger;
			};
			vws.doAction;
		})
		.clipLo_( -1 ).clipHi_(15)
		.allowedChars_( "+-.eE*/()%any" )
		.interpretFunc_({ |string|
			if( "any".any( string.includes( _ ) ) ) { -1 } { string.interpret; };
		})
		.formatFunc_({ |val| if( val == -1 ) { "any" } { val.asInteger.asString } });

		vws[ \view ].decorator.nextLine;
		if( label.notNil ) { vws[ \view ].decorator.shift( labelWidth + 4, 0 ); };

		vws[ \learn ] = SmoothButton( vws[ \view ], 40@14 )
		.label_( [ "learn", "learn" ] )
		.radius_(2)
		.action_({ |bt|
			switch( bt.value,
				1, { vws[ \learnFunc ] = this.learn({ |...learned|
					vws[ \val ] = this.constrain( learned ) ? -1;
					{ vws.setViews; }.defer;
					vws.doAction;
					bt.value = 0;
				});
				},
				0, { learnFuncs.remove( vws[ \learnFunc ] ); }
			);
		});

		vws[ \device ] = StaticText( vws[ \view ], bounds.width - 44 - (labelWidth+4) @ 14 )
		.mouseDownAction_({
			makeMenu.value({ |res|
				vws[ \val ][ 0 ] = res;
				vws.setViews;
				vws.doAction;
			});
		})
		.string_( "device " ).align_( \right ).applySkin( RoundView.skin )
		.align_( \center )
		.background_( Color.white.alpha_(0.25) );
		vws[ \device ].setProperty(\wordWrap, false);

		vws[ \setViews ] = {
			{ vws[ \device ].string =  "device: %".format( vws[ \val ][ 0 ] ); }.defer;
			vws[ \chan ].value = vws[ \val ][ 2 ] ? -1;
			vws[ \num ].value = vws[ \val ][ 3 ] ? -1;
		};

		vws.setViews;

		^vws;
	}

	setView { |view, value, active = false|
		view[ \val ] = value;
		{ view.setViews }.defer;
		if( active ) { view.doAction };
	}

}