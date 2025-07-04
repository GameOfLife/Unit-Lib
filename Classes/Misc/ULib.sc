/*
    Unit Library
    The Game Of Life Foundation. http://gameoflife.nl
    Copyright 2006-2013 Miguel Negrao, Wouter Snoei.

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

ULib {
    classvar <>servers;
	classvar <>lastPath;
	classvar <>window, <>eWindow;
	classvar <>envirSpecs;
	classvar <>sampleRate = 44100;

    *initClass {
        servers = [Server.default]
    }

	*allServers {
        ^servers.collect({ |s|
            if( s.isKindOf( LoadBalancer ) ) {
                s.servers
            } {
                s
            }
	}) !? _.flat
    }

	*waitForServersToBoot {
        while({ this.allServers.collect( _.serverRunning ).every( _ == true ).not; },
            { 0.2.wait; });
    }

	*sync {
        this.allServers.do( _.sync )
    }

    *sendDefs { |defs|
        this.allServers.do{ |s|
            defs.do{ |def|
                def.send(s)
            }
        }
    }

    *clear { |runCmdPeriod = true| // use after unexpected Server quit or lost connection
	    if( runCmdPeriod == true ) { CmdPeriod.run; };
	    UChain.clear;
	    U.clear;
	    ULib.allServers.do(_.notify_(true));
	    BufSndFile.reloadAllGlobal;
    }

	*closeServers {
		servers.do(_.quit);
		Server.default = Server.local;
		this.allServers.do({ |item|
			if( item != Server.local ) {
				Server.all.remove( item );
				Server.named.removeAt( item.name );
				ServerTree.objects !? _.removeAt( item );
				ServerBoot.objects !? _.removeAt( item );
				RootNode.roots.removeAt( item.name );
				NodeWatcher.all.removeAt( item.name );
			}
		});
		servers = nil;
		if( window.notNil && { window.isClosed.not }) {
			window.close;
		};
	}

	*setAllScoresActive { |bool = true|
		if( bool ) {
			UScoreEditorGUI.all.do({ |item|
				item.score.setUMapsActive( false, false );
				"  activating UScore '%':\n".postf( item.score.name );
				item.score.setUMapsActive( true, true );
				item.tranportBar.views.active.value = 1;
			});
		} {
			UScoreEditorGUI.all.do({ |item|
				"  de-activating UScore '%':\n".postf( item.score.name );
				item.score.setUMapsActive( false, true );
				item.tranportBar.views.active.value = 0;
			});
		};
	}

	*serversWindow { |name|
        var makePlotTree, makeMeter, font;
        var servers = ULib.allServers;
        var w, menuView;
		var statusView, latencyView;
		var width = 480;
		var gainSlider, gainSliderCtrl;
		var windowBottom = 10;

		if( window.notNil && { window.isClosed.not }) {
			window.close;
		};

		font = Font(Font.defaultSansFace, 11);

		if( ULib.allServers.size == 1 ) {
			width = 430;
		};

		if( thisProcess.platform.name == \windows ) {
			windowBottom = 58; // make room for the taskbar
		};

		w = Window(name ? "ULib servers", Rect(10, windowBottom, width, 24 +
			ULib.servers.collect({ |item|
				if( item.isKindOf( LoadBalancer ) ) {
					item.uViewHeight + 22
				} {
					22
				};
			}).sum + 26 +
			if( UMenuBarIDE.hasMenus ) { 22 } { 0 }
		), resizable: false
		).userCanClose_( false ).front;
        w.addFlowLayout;
		RoundView.pushSkin( UChainGUI.skin );

		if( UMenuBarIDE.hasMenus ) {
			menuView = UMenuBarIDE.createMenuStrip( w, (width-8) @ 18, [-4,-4,-4,0] );
		};

		ULib.servers.do{ |s, i|
			var ip, composite, startButton, startCtrl, killButton;
			composite = CompositeView( w, Rect( 0,0, width - 8, 18 ) );
			if( s.addr.isLocal ) {
				killButton = SmoothButton(composite, Rect(width-26,0, 18, 18))
				.states_( [["K"]] )
				.canFocus_( false )
				.font_( font )
				.action_({ Server.killAll });
				killButton.view.toolTip_( "Kills all local Server processes\n\nUse this if Server doesn't boot" );
			};
			if( s.isKindOf( LoadBalancer ) ) {
				if( s.addr.isLocal && { NetAddr.respondsTo( \myIP ) }) {
					ip = NetAddr.myIP;
				} {
					ip = s.addr.ip;
				};
				composite.background = Color.gray(0.5).alpha_(0.2);
				StaticText(composite, Rect( 22, 2, 200,16 ) )
				.font_( font.boldVariant )
				.toolTip_( "LoadBalancer %, % Servers\n\n".format( s.name, s.servers.size )  ++
					"Events are automatically divided over these servers\n" ++
					"to reach optimal CPU performance on a multi-core system"
				)
				.string_( " " ++ s.name + "/" + ip );
				startButton = SmoothButton(composite, Rect(0,0,18,18))
				.canFocus_( false )
				.label_( [ 'power', 'power' ] )
				.hiliteColor_( Color.green(0.5,0.5) )
				.action_({ |bt|
					switch( bt.value,
						1, { s.boot },
						0, { s.quit }
					);
				});
				startButton.view.toolTip_( "boot/quit LoadBalancer %\n\n".format( s.name ) ++
					"When booting each Server will wait for the previous one to boot"
				);
				startCtrl = { |obj, what|
					if( what === \serverRunning ) {
						case { s.serverRunning } {
							startButton.hiliteColor = Color.green(0.5,0.5);
							startButton.value = 1;
						} {
							s.serverBooting;
						} {
							startButton.hiliteColor = Color(1.0, 0.5, alpha: 0.5 );
							startButton.value = 1;
						} {
							startButton.value = 0;
							startButton.hiliteColor = Color(1.0, 0.5, alpha: 0.5 );
						};
					};
				};
				s.servers.do({ |srv|
					srv.addDependant( startCtrl );
				});
				startButton.onClose_({ s.servers.do(_.removeDependant( startCtrl ) ) });
				if( s.serverRunning == true ) {
					startButton.value = 1;
				};
				w.view.decorator.nextLine;
				s.uView(w, width-4);
			} {
				s.uView( composite, width-108-20 );
			};
			if( i == 0 ) {
				latencyView = EZSmoothSlider(composite, Rect(width - 108 - 20,1,100,16),nil, [0.02,1,\exp,0,0.02].asSpec)
			    .value_( Server.default.latency)
			    .font_( font )
			    .action_({ |v| Server.default.latency = v.value})
			    .numberWidth_( 40 );
				latencyView
			    .sliderView
			    .string_("Latency")
				.font_( font )
			    .knobColor_( Color.black.alpha_(0.25) );
				latencyView.sliderView.view.toolTip_( "latency (s) for starting events and changing parameters" );
			};

		};

		statusView = StaticText( w, (width - 8 - 40) @ 18 )
		.align_( \left )
		.toolTip_(
			"% status display\n\nShows how many events are playing and how many buffers loaded\n".format( name ) ++
			"If the numbers here show active UChains or Units when nothing is playing, press 'clear'"
		)
		.font_( font );

		SmoothButton( w, Rect( 0, 0, 36, 16 ) )
			.states_( [["clear"]] )
		    .font_( font )
		    .canFocus_( false )
			.action_( {
				ULib.clear;
		     } )
		.view.toolTip_(
			"Clear resets Servers and UChain/Unit synth/group dicts and reloads global Buffers.\n\n" ++
			"Use after Server restart or if things appear to be playing but actually don't (usually\n" ++
			"caused by overload). More elaborate reset options are in the WFSCollider menu"
		);


		gainSlider = EZSmoothSlider( w, (w.bounds.width - 8) @ 20,
			controlSpec: [ -60, 36, \lin, 1, -12, "db" ],
		).value_( UGlobalGain.gain ).action_({ |vw| UGlobalGain.gain = vw.value });

		gainSlider.numberView
		.font_( font.copy.bold_( true ) )
		.autoScale_( true )
		.step_( 1 )
		.scroll_step_( 1 )
		.formatFunc_({ |value| value.round(1).asInteger });
		gainSlider.sliderView
		.mode_( \move )
		.align_( \right )
		.string_("Level (dB)   ")
		.font_( font );
		gainSlider.sliderView.view.toolTip_( "Global level in dB" );

		gainSliderCtrl = SimpleController( UGlobalGain )
		.put( \gain, {
			gainSlider.value = UGlobalGain.gain.asInteger
		});

		SkipJack({
			var numbufs = 0;
			ULib.allServers.do({ |srv|
				Buffer.cachedBuffersDo( srv, { numbufs = numbufs + 1 });
			});
			statusView.string_(
				"UScores (open, playing) / UChains / Units / Buffers : (%, %) / % / % / %".format(
					UScoreEditorGUI.all.size,
					UScore.activeScores.size,
					UChain.groupDict.keys.size,
					U.synthDict.keys.size,
					numbufs
				)
			);
			latencyView.value = Server.default.latency;
		}, 1, { w.isClosed }, "ULib_status" );

        w.view.keyDownAction = { arg view, char, modifiers;
            // if any modifiers except shift key are pressed, skip action
            if(modifiers & 16515072 == 0) {

                case
				{char === $n } { fork{ servers.do{ |s| s.queryAllNodes(false); 0.5.wait; } } }
				{char === $N } { fork{ servers.do{ |s| s.queryAllNodes(true); 0.5.wait; } } }
                {char === $l } { makeMeter.() }
                {char === $p}  { makePlotTree.() }
                {char === $ }  { servers.do{ |s| if(s.serverRunning.not) { s.boot } } }
            };
        };
        makePlotTree = {
            var onClose, comp;
			var servers = ULib.allServers.select(_.isLocal);
            var window = Window.new("Node Tree(s)",
                Rect(128, 64, 1000, 400),
                scroll:true
            ).front;
            window.addFlowLayout(0@0,0@0);
            comp = servers.collect{ CompositeView(window,400@400) };
            window.view.hasHorizontalScroller_(false).background_(Color.grey(0.9));
            onClose = [servers, comp].flopWith{ |s,c| s.plotTreeView(0.5, c, { defer {window.close}; }) };
            window.onClose = {
                onClose.do( _.value );
            };
        };
        makeMeter = {
            var window = Window.new("Meter",
                Rect(128, 64, 1000, 1000),
				scroll: true
            ).front;
            window.addFlowLayout;
            servers.do{ |s|
                var numIns = s.options.numInputBusChannels;
                var numOuts = s.options.numOutputBusChannels;
                ServerMeterView(s, window, 0@0, numIns, numOuts)
            }
        };

		RoundView.popSkin;
		w.onClose_({ if( window == w ) { window = nil }; gainSliderCtrl.remove; });
		window = w;
        ^w
    }

	*envirTextArchive {
		var archive = "";
		~u_specs !? _.keysValuesDo({ |key, spec|
			if( spec.respondsTo( \originalSpec ) ) {
				spec = spec.originalSpec;
			};
			archive = archive ++ "'%'.uEnvirPut( %, % );\n".format(
				key,
				key.envirGet.cs,
				spec.cs,
			);
		});
		^archive;
	}

	*writeEnvir { |path, overwrite=false, ask=true, successAction, cancelAction|
	    var writeFunc;
	    writeFunc = { |overwrite, ask, path|
		    var text;

		    GlobalPathDict.relativePath = path.dirname;
		    text = this.envirTextArchive;
		    GlobalPathDict.relativePath = nil;

			path = path.replaceExtension( "uenvir" );

		    File.checkDo( path, { |f|
				f.write( text );
				successAction.value(path);
			}, overwrite, ask);
	    };

	    if( path.isNil ) {
		    ULib.savePanel( { |pth|
			    path = pth;
			    writeFunc.value(true,false,path);
		    }, cancelAction );
	    } {
		    writeFunc.value(overwrite,ask,path);
	    };
    }

	*readEnvir { |path, action|
		if( path.isNil ) {
			ULib.openPanel( { |path|
				action.value( path );
				path.load;
			});
		} {
			action.value( path );
			path = path.standardizePath.load;
		};
	}

	*envirWindow {
		var w, bounds, addViews, addButton, labelWidth;
		var usedKeys, makeGBView;

		envirSpecs = envirSpecs ?? {
			[
				'value', [0,1].asSpec,
				'freq', FreqSpec(2,20000),
				'amp', \amp.asSpec,
				'integer', IntegerSpec(),
				'boolean', BoolSpec(),
				'time', SMPTESpec()
			]
		};

		if( eWindow.notNil && { eWindow.isClosed.not }) {
			bounds = eWindow.bounds;
			eWindow.close;
		};

		w = Window( "ULib Environment", bounds, scroll: true ).front;
		w.addFlowLayout( );
		bounds = w.bounds;

		eWindow = w;

		RoundView.pushSkin( UChainGUI.skin );
		StaticText( w, (bounds.width - 12)@14 )
		.string_( " Environment (%)".format( ~u_specs.size ) )
		.applySkin( RoundView.skin )
		.background_( RoundView.skin.headerColor ? Color.white.alpha_(0.5) );
		w.asView.decorator.shift( -36 - 88, 0 );

		SmoothButton( w, 40@14 ).states_( [
			[ "read", nil, Color.green.alpha_(0.2) ]
		] ).action_({
			ULib.readEnvir( action: {
				w.onClose.value;
				{ this.envirWindow; }.defer(0.1);
			})
		})
		.radius_(2).canFocus_( false );

		SmoothButton( w, 40@14 ).label_([ "OSC", "OSC" ]).action_({ |bt|
			switch( bt.value,
				1, { UOSCsetterEnvir.enable },
				0, { UOSCsetterEnvir.disable }
			);
		})
		.radius_(2).canFocus_( false )
		.value_( UOSCsetterEnvir.default.notNil.binaryValue );

		addButton = SmoothButton( w, 14@14 ).label_( '+' )
		.action_({ |bt|
			addViews.do(_.visible_( true ));
			bt.enabled_(false);
		});
		SmoothButton( w, 14@14 ).label_( 'roundArrow' )
		.action_({ { this.envirWindow; }.defer(0.1); });
		if( ~u_specs.notNil && { ~u_specs.size > 0 }) {
			w.asView.decorator.shift( -36 - 132, 0 );
			SmoothButton( w, 40@14 ).states_( [
				[ "write", nil, Color.red.alpha_(0.2) ]
			] ).action_({ ULib.writeEnvir }).radius_(2).canFocus_( false );
			~u_specs.sortedKeysValuesDo({ |key, spec|
				var view, ctrl;
				view = spec.makeView( w, (bounds.width - 30)@14, "~" ++ key, { |vw, val|
					key.uEnvirPut( val );
				});
				spec.setView( view, key.envirGet );
				ctrl = SimpleController( currentEnvironment )
				.put( key, {
					if( spec == ~u_specs[ key ] ) {
						spec.setView( view, key.envirGet );
					} {
						w.onClose.value;
						{ this.envirWindow; }.defer;
					};
				});
				w.onClose = w.onClose.addFunc({ ctrl.remove; });
				SmoothButton( w, 14@14 )
				.label_( '-' )
				.action_({
					~u_specs.removeAt( key );
					{ this.envirWindow; }.defer(0.1);
				});
			})
		} {
			StaticText( w, (bounds.width - 12)@20 ).string_(
				" no Environment variables to display"
			).applySkin( RoundView.skin );
		};
		addViews = ();
		labelWidth = RoundView.skin.labelWidth ? 80;
		addViews[ \label ] = StaticText( w, 20@14 ).string_("~").align_(\right).applySkin( RoundView.skin );
		addViews[ \textBox ] = TextField( w, (labelWidth - 24) @ 14 )
		.action_({
			var spec, key;
			if( addViews[ \textBox ].string.size > 0 ) {
				key = addViews[ \textBox ].string.asSymbol;
				spec = envirSpecs[ (addViews[ \popUp ].value * 2) + 1 ];
				w.onClose.value; // remove controllers in case replacing a current val
				if( addViews[ \n ].value > 1 ) {
					key.uEnvirPut( { spec.default } ! (addViews[ \n ].value), spec );
				} {
					key.uEnvirPut( spec.default, spec );
				};
				{ this.envirWindow; }.defer(0.1);
			};
		})
		.applySkin( RoundView.skin );
		addViews[ \popUp ] = UPopUpMenu( w, (bounds.width - 8 - labelWidth - 52 - 44 - 18) @ 14 )
		.items_( envirSpecs[0,2..] )
		.applySkin( RoundView.skin );
		addViews[ \n ] = SmoothNumberBox( w, 40@14 ).clipLo_(1).value_(1);
		addViews[ \add ] = SmoothButton( w, 40@14 )
		.label_( "add" )
		.radius_( 0 )
		.action_({
			var spec, key;
			if( addViews[ \textBox ].string.size > 0 ) {
				key = addViews[ \textBox ].string.asSymbol;
				spec = envirSpecs[ (addViews[ \popUp ].value * 2) + 1 ];
				w.onClose.value;
				if( addViews[ \n ].value > 1 ) {
					key.uEnvirPut( { spec.default } ! (addViews[ \n ].value), spec );
				} {
					key.uEnvirPut( spec.default, spec );
				};
				{ this.envirWindow; }.defer(0.1);
			};
		});
		addViews[ \rmv ] = SmoothButton( w, 14@14 ).label_( '-' )
		.action_({
			addViews.do(_.visible_(false));
			addButton.enabled_( true );
		});
		addViews.do(_.visible_(false));

		StaticText( w, (bounds.width - 12)@14 )
		.string_( " Global Buffers (%)".format( BufSndFile.global.keys.size )  )
		.applySkin( RoundView.skin )
		.background_( RoundView.skin.headerColor ? Color.white.alpha_(0.5) );
		w.asView.decorator.shift( -36, 0 );
		SmoothButton( w, 14@14 ).label_( '+' )
		.action_({ |bt|
			ULib.openPanel( { |path|
				BufSndFile.new( path ).hasGlobal_( true );
				{ this.envirWindow; }.defer(0.1);
			}, multipleSelection: false);
		});
		SmoothButton( w, 14@14 ).label_( 'roundArrow' )
		.action_({ { this.envirWindow; }.defer(0.1); });
		if( BufSndFile.global.keys.size > 0 ) {
			usedKeys = BufSndFile.getUsedGlobalKeys;
			makeGBView = { |key, buf|
				var view;
				view = StaticText( w, w.view.bounds.width - 30 @ 14 )
				.string_( key.asString )
				.background_( Color.white.alpha_(0.25) )
				.align_( \center )
				.applySkin( RoundView.skin );
				view.setProperty(\wordWrap, false);
				if( key.asString.bounds( RoundView.skin.font ).width > view.bounds.width ) {
					view.align_( \right ).string_( key.asString ++ " " );
				};
				SmoothButton( w, 14@14 )
				.label_( '-' )
				.action_({
					BufSndFile.disposeGlobal( key );
					{ this.envirWindow; }.defer(0.1);
				});
			};
			if( usedKeys.size == BufSndFile.global.keys.size ) {
				BufSndFile.global.sortedKeysValuesDo({ |key, buf|
					makeGBView.( key, buf );
				});
			} {
				BufSndFile.global.sortedKeysValuesDo({ |key, buf|
					if( usedKeys.includes( key ) ) {
						makeGBView.( key, buf );
					};
				});
				StaticText( w, (bounds.width - 12)@20 ).string_(
					" Not used in any open UScore:"
				).applySkin( RoundView.skin );
				BufSndFile.global.sortedKeysValuesDo({ |key, buf|
					if( usedKeys.includes( key ).not ) {
						makeGBView.( key, buf );
					};
				});
			}
		} {
			StaticText( w, (bounds.width - 12)@20 ).string_(
				" no Global Buffers to display"
			).applySkin( RoundView.skin );
		};

		RoundView.popSkin;
	}

	*startup { |createServers = true, numServers = 4, options, startGuis = true|

		ULib.closeServers;

		UChain.makeDefaultFunc = {
			UChain( \bufSoundFile, \stereoOutput ).useSndFileDur
		};

		UnitRack.defsFolders = UnitRack.defsFolders.add(
			Platform.userAppSupportDir ++ "/UnitRacks/";
		);

		options = options ?? {
			ServerOptions()
			.maxSynthDefs_(2048)
		    .numWireBufs_(2048)
		};

		if(createServers) {
			if(numServers > 1) {
			servers = [LoadBalancer(*numServers.collect{ |i|
				Server("ULib_"++(i+1), NetAddr("127.0.0.1",57110+i), options)
				}).name_( "Unit Lib servers" ) ];
			}{
				servers = [Server("ULib server", NetAddr("127.0.0.1",57110), options)]
			};
			Server.default = this.allServers[0]
		};

		if( startGuis ) {
			if( (thisProcess.platform.class.asSymbol == 'OSXPlatform') && {
				thisProcess.platform.ideName.asSymbol === \scapp
			}) {
				UMenuBar();
			} {
				if( UMenuBarIDE.isEmpty ) {
					UMenuBarIDE();
				};
			};
			UGlobalEQ.gui;
			if( ((thisProcess.platform.ideName == "scqt") && (ULib.allServers.size == 1)).not  ) {
				ULib.serversWindow
			}
		};

		Udef.synthDefDir = Platform.userAppSupportDir +/+ "u_synthdefs/";

		if( Udef.synthDefDir.notNil ) { File.mkdir( Udef.synthDefDir ); };

		this.loadUDefs( false );

		if( Udef.synthDefDir.notNil ) {
			UEvent.nrtStartBundle = UEvent.nrtStartBundle.add( [ "/d_loadDir", Udef.synthDefDir ] )
		};


		StartUp.defer({
			{
				ULib.servers.do(_.bootSync);
				"\n\tUnit Lib started".postln
			}.fork( AppClock );
		});
	}

	*getDefaultUdefs{
		^(Udef.loadAllFromDefaultDirectory ++
			UMapDef.loadAllFromDefaultDirectory).select(_.notNil)
	}

	*getDefaultSynthDefs{
		var defs;
		Udef.loadOnInit = false;
		defs = this.getDefaultUdefs.collect(_.synthDef).flat.select(_.notNil);
		Udef.loadOnInit = true;
		^defs

	}

	*writeDefaultSynthDefs {
		this.getDefaultSynthDefs.do{ |def|
			"writting % SynthDef file".format(def.name).postln;
			def.justWriteDefFile;
		}
	}

	*loadUDefs { |loadDir = true|
		"ULib: loading all Udefs...".postln;

		Udef.loadOnInit = false;

		bench {
			Udef.loadAllFromDefaultDirectory;
			UMapDef.loadAllFromDefaultDirectory;
			UnitRack.loadAllFromDefaultDirectory;
		};

		Udef.loadOnInit = true;

		if( loadDir == true ) {
			ULib.allServers.do(_.loadDirectory( Udef.synthDefDir ? SynthDef.synthDefDir ));
		};
	}

	*openPanel { arg okFunc, cancelFunc, multipleSelection = false;
		var func;
		if( multipleSelection ) {
			func = { |paths|
				lastPath = paths.first;
				okFunc.( paths );
			};
		} {
			func = { |path|
				lastPath = path;
				okFunc.( path );
			};
		};
		^Dialog.openPanel( func, cancelFunc, multipleSelection, lastPath );
	}

	*savePanel { arg okFunc, cancelFunc, path;
		var func;
		func = { |path|
			lastPath = path;
			okFunc.( path );
		};
		^Dialog.savePanel( func, cancelFunc, path ? lastPath );
	}

}


	