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

PartConvBufferView {

	classvar <>externalIRs;

	var <partConvBuffer;
	var <parent, <view, <views;
	var <>action;
	var <viewHeight = 14;

	*new { |parent, bounds, action, partConvBuffer|
		^super.new.init( parent, bounds, action ).value_( partConvBuffer );
	}

	init { |parent, bounds, inAction|
		action = inAction;
		this.makeView( parent, bounds );
	}

	doAction { action.value( this ) }

	value { ^partConvBuffer }
	value_ { |newPartConvBuffer|
		if( partConvBuffer != newPartConvBuffer ) {
			partConvBuffer.removeDependant( this );
			partConvBuffer = newPartConvBuffer;
			partConvBuffer.addDependant( this );
			this.update;
		};
	}

	update {
		if( partConvBuffer.notNil ) { this.setViews( partConvBuffer ) };
	}

	resize_ { |resize|
		view.resize = resize ? 5;
	}

	remove {
		if( partConvBuffer.notNil ) {
			partConvBuffer.removeDependant( this );
		};
	}

	setViews { |inPartConvBuffer|

		views[ \path ].value = inPartConvBuffer.path;
		if( File.exists( inPartConvBuffer.path.getGPath ? "" ) ) {
			views[ \path ].stringColor = nil;
		} {
			views[ \path ].stringColor = Color.red(0.66);
		};

		{ views[ \duration ].string = (inPartConvBuffer.duration ? 0).asSMPTEString(1000); }.defer;
	}

	setFont { |font|
		font = font ??
			{ RoundView.skin !? { RoundView.skin.font } } ??
			{ Font( Font.defaultSansFace, 10 ) };

		{
			views[ \duration ].font = font;
		}.defer;

		views[ \path ].font = font;
		views[ \plot ].font = font;
	}

	performPartConvBuffer { |selector ...args|
		if( partConvBuffer.notNil ) {
			^partConvBuffer.perform( selector, *args );
		} {
			^nil
		};
	}

	*findExternalIRs {
		var types, surroundTypes;
		externalIRs = externalIRs ?? { OEM() };

		// apple
		types = (
			'OSD': "Discrete Surround",
			'OBF': "B-Format",
			'OST': "Stereo",
			'CTS': "True Stereo",
			'CBF': "12 x B-Format"
		);

		surroundTypes = [ 'OSD', 'OBF', 'CBF' ];

		externalIRs[ 'Apple Space Designer' ] = this.prFindExternalIRs( [
			"/Library/Audio/Impulse Responses/Apple/*/*/*.SDIR"
		], { |filename|
			filename.find( ".SDIR" ).notNil
		}, { |split|
			var kkey;
			if( types.keys.any({ |key|
				kkey = key;
				split.last.find( "-%".format(key) ).notNil;
			}) ) {
				if( surroundTypes.includes( kkey ) ) {
					[ "% (%)".format( types[ kkey ], kkey ) ] ++ split[1..];
				} {
					[ "% (%)".format( types[ kkey ], kkey ) ] ++ split;
				};
			} {
				[ "Mono/Stereo (old)" ] ++ split;
			};
		});

		// ableton live Convolution Pro
		externalIRs[ 'Ableton Live Convolution' ] = this.prFindExternalIRs( [
			"~/Music/Ableton/Factory Packs/Convolution Reverb/IRs/*/*.aif".standardizePath,
			"~/Music/Ableton/Factory Packs/Convolution Reverb/IRs/*/*.wav".standardizePath
		], { |filename|
			filename.find( ".wav" ).notNil or: (filename.find( ".aif" ).notNil)
		} );

		// Meldaproduction
		externalIRs[ 'MeldaProduction MConvolution' ] = this.prFindExternalIRs( [
			"/Library/Application Support/MeldaProduction/MeldaProduction IR/*/*.flac",
			"/Library/Application Support/MeldaProduction/MeldaProduction IR/*/*/*.flac"
		], { |filename|
			filename.find( ".flac" ).notNil
		} );
	}

	*findExternalIRsOnce {
		if( externalIRs.isNil ) {
			this.findExternalIRs
		};
	}

	*prFindExternalIRs { |sourcePaths, isIRTest, addCategoryFunc|
		var paths, list, types, surroundTypes, irs, pathOffset;

		paths = sourcePaths.collect(_.pathMatch).flatten(1);

		pathOffset = sourcePaths.first.split( $/ ).detectIndex({ |item| item.first == $* }) ? 0;

		if( paths.size > 0 ) {
			irs = OEM();

			paths.do({ |path|
				var sublist, split;
				sublist = irs;
				split = path.split($/)[pathOffset..];
				split = addCategoryFunc !? _.value( split ) ? split;
				split.do({ |item, i|
					var kkey, label;
					if( isIRTest.value( item ) ) {
						sublist[ \sdirs ] = sublist[ \sdirs ].add( path );
					} {
						if( sublist.keys !? { |keys| keys.includes( item.asSymbol ).not } ? true ) {
							sublist.put( item.asSymbol, OEM() );
						};
						sublist = sublist[ item.asSymbol ];
					};
				});
			});
		} {
			irs = \notfound;
		};
		^irs;
	}


	*canImport {
		this.findExternalIRsOnce;
		^externalIRs.every(_ == \notfound ).not;
	}

	*makeImportMenu { |action|
		var func, menu;

		if( this.canImport ) {

			menu = Menu();

			func = { |ls, mn|
				var sub;
				ls.keys.do({ |key|
					if( key == 'sdirs' ) {
						ls[ key ].do({ |path|
							mn.addAction( MenuAction( path.asString.basename, { action.value( path ) } ) )
						});
					} {
						sub = Menu().title_( key.asString );
						mn.addAction( sub );
						func.value( ls[ key ], sub );
					}
				});
			};

			externalIRs.keysValuesDo({ |key, value|
				if( value != \notfound ) {
					menu.addAction( MenuAction.separator( key.asString ) );
					func.value( value, menu );
				};
			});

			^menu.uFront;
		} {
			^nil
		}
	}

	*viewNumLines { ^2 }

	makeView { |parent, bounds, resize|

		var currentSkin, plotWindow;

		if( bounds.isNil ) { bounds= 350 @ (this.class.viewNumLines * (viewHeight + 4)) };

		view = EZCompositeView( parent, bounds, gap: 4@4 );
		bounds = view.asView.bounds;
		view.onClose_({ this.remove; });
		view.resize_( resize ? 5 );
		views = ();

		currentSkin = RoundView.skin;

		views[ \path ] = FilePathView( view, bounds.width @ viewHeight )
			.resize_( 2 )
			.action_({ |fv|
				if( fv.value.notNil && {
					(fv.value.pathExists != false) && { fv.value.extension.toLower != "partconv" }
					}
				) {
					SCAlert( "The file '%' doesn't appear to be a .partconv file\ndo you want to convert it?"
							.format( fv.value.basename ),
					 	[ "use anyway", "convert" ],
					 	[{
							this.performPartConvBuffer( \path_ , fv.value );
							this.performPartConvBuffer( \fromFile );
							action.value( this );
						}, {
							PartConvBuffer.convertIRFileMulti( fv.value,
								server: ULib.servers,
							    action: { |paths| fv.value = paths[0]; fv.doAction }
							);
						}]
					);
				} {
					this.performPartConvBuffer( \path_ , fv.value );
					this.performPartConvBuffer( \fromFile );
					action.value( this );
				};
			});

		views[ \duration ] = StaticText( view, (bounds.width - 44 - 64 - 64) @ viewHeight )
			.resize_( 2 )
			.applySkin( RoundView.skin ? () );

		views[ \import ] = StaticText( view, 60 @ viewHeight );

		if( this.class.canImport ) {
			views[ \import ]
			.applySkin( RoundView.skin )
			.string_( "import" )
			.align_( \center )
			.background_( Color.white.alpha_(0.25) )
			.mouseDownAction_({
				views[ \importMenu ] !? _.deepDestroy;
				views[ \importMenu ] = this.class.makeImportMenu({ |path|
					var savePath;
					savePath = (ULib.lastPath ? "~/").standardizePath.withoutTrailingSlash
					+/+ path.basename.replaceExtension( "partconv" );
					ULib.savePanel({ |pth|
						PartConvBuffer.convertIRFileMulti( path, pth.replaceExtension( "partconv" ),
							server: ULib.servers,
							action: { |paths| views[ \path ].value = paths[0]; views[ \path ].doAction }
						);
					}, path: savePath )
				})
			})
			.onClose_({ views[ \importMenu ] !? _.deepDestroy; })
		};

		views[ \danStowel ] = SmoothButton( view, 60 @ viewHeight )
		.radius_( 2 )
		.resize_( 3 )
		.label_( "generate" )
		.action_({
			var closeFunc;
			// generate danstowell
			if( views[ \genWindow ].isNil or: { views[ \genWindow ].isClosed } ) {
				views[ \genWindow ] = Window( "danstowell", Rect(592, 534, 294, 102) ).front;
				views[ \genWindow ].addFlowLayout;
				RoundView.pushSkin( currentSkin );
				StaticText( views[ \genWindow ], 50@18 ).string_( "duration" ).applySkin( RoundView.skin );
				views[ \genDur ] = SMPTEBox( views[ \genWindow ], 80@18 )
				.value_(1.3)
				.applySmoothSkin;
				SmoothButton( views[ \genWindow ], 80@18 )
				.extrude_(false)
				.label_( "generate" )
				.action_({
					Dialog.savePanel({ |path|
						PartConvBuffer.convertIRFile(
							PartConvBuffer.generateDanStowelIR( views[ \genDur ].value ),
							path.replaceExtension( "partconv" ),
							ULib.servers,
							{ |path|
								views[ \path ].value = path;
								views[ \path ].doAction
							}
						);
						closeFunc.value;
					});
				});

				RoundView.popSkin;

				closeFunc = { views[ \genWindow ] !? (_.close); };

				views[ \danStowel ].onClose = views[ \danStowel ].onClose.addFunc( closeFunc );

				views[ \genWindow ].onClose = {
					views[ \danStowel ].onClose.removeFunc( closeFunc );
					views[ \genWindow ] = nil;
				};
			} {
				views[ \genWindow ].front;
			};

		});


		views[ \plot ] = SmoothButton( view, 40 @ viewHeight )
		.radius_( 2 )
		.resize_( 3 )
		.label_( "plot" )
		.action_({ |bt|
			var closeFunc, sf;

			plotWindow !? _.close;

			sf = this.performPartConvBuffer( \asSoundFile );

			if( sf.notNil ) {
				plotWindow = USoundFilePlotWindow( sf, 0, 0)
				.canSelect_( false );

				closeFunc = { plotWindow !? _.close; };

				plotWindow.window.onClose = {
					bt.onClose.removeFunc( closeFunc );
					plotWindow = nil;
				};

				bt.onClose = bt.onClose.addFunc( closeFunc );
			};


			/*
			// this will have to go in a separate class
			var w, a, f, b, x;
			var closeFunc;

			x = partConvBuffer;
			f = this.performPartConvBuffer( \asSoundFile );

			w = Window(f.path, Rect(200, 200, 850, 400), scroll: false);
			a = SoundFileView.new(w, w.view.bounds);
			a.resize_(5);
			a.soundfile = f;
			a.read(0, f.numFrames);
			a.elasticMode_(1);
			a.gridOn = true;
			a.gridColor_( Color.gray(0.5).alpha_(0.5) );
			a.waveColors = Color.gray(0.2)!16;
			w.front;
			a.background = Gradient( Color.white, Color.gray(0.7), \v );

			closeFunc = { w.close; };

			w.onClose = { bt.onClose.removeFunc( closeFunc ) };

			bt.onClose = bt.onClose.addFunc( closeFunc );
			*/
		});


		this.setFont;
	}

}