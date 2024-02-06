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
	classvar <>window;

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

		if( window.notNil && { window.isClosed.not }) {
			window.close;
		};

		font = Font(Font.defaultSansFace, 11);

		w = Window(name ? "ULib servers", Rect(10, 10, width, 24 +
			ULib.servers.collect({ |item| item.uViewHeight + 22 }).sum +
			if( UMenuBarIDE.allMenus.notNil ) { 28 } { 0 }
		), resizable: false
		).userCanClose_( false ).front;
        w.addFlowLayout;
		RoundView.pushSkin( UChainGUI.skin );

		if( UMenuBarIDE.allMenus.notNil ) {
			menuView = View( w, Rect(0,0,width - 8,18) );
			menuView.bounds = menuView.bounds.insetAll(0,-4,0,0);
			menuView.background_( Color.gray(0.9) );
			menuView.layout = HLayout( UMenuBarIDE.createToolbar.font_( Font( Font.defaultSansFace, 13 ) ) );
			menuView.layout.margins = 0;
			menuView.layout.spacing = 4;
		};

		ULib.servers.do{ |s, i|
			var ip, composite;
			composite = CompositeView( w, Rect( 0,0, width - 8, 18 ) );
			composite.background = Color.gray(0.8);
			if( s.addr.isLocal ) {
				SmoothButton(composite, Rect(width-26,0, 18, 18))
				.states_( [["K"]] )
				.canFocus_( false )
				.font_( font )
				.action_({ Server.killAll });
				if( NetAddr.respondsTo( \myIP ) ) {
					ip = NetAddr.myIP;
				};
			} {
				ip = s.addr.ip;
			};
			StaticText(composite, Rect( 2, 2, 200,16 ) )
			.font_( font.boldVariant )
			.string_( " " ++ s.name + "/" + ip );
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
			};
			w.view.decorator.nextLine;
			s.uView(w, width-4);
		};

		statusView = StaticText( w, (width - 8 - 40) @ 18 )
		.font_( font );

		SmoothButton( w, Rect( 0, 0, 36, 16 ) )
			.states_( [["clear"]] )
		    .font_( font )
		    .canFocus_( false )
			.action_( {
				ULib.clear;
		     } );

		SkipJack({
			var numbufs = 0;
			ULib.allServers.do({ |srv|
				Buffer.cachedBuffersDo( srv, { numbufs = numbufs + 1 });
			});
			statusView.string_(
				" UChains / Units / Buffers : % / % / %".format(
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
		w.onClose_({ if( window == w ) { window = nil } });
		window = w;
        ^w
    }

	*startup { |sendDefsOnInit = true, createServers = false, numServers = 4, options, startGuis = true|

		UChain.makeDefaultFunc = {
			UChain( \bufSoundFile, \stereoOutput ).useSndFileDur
		};

		UnitRack.defsFolders = UnitRack.defsFolders.add(
			Platform.userAppSupportDir ++ "/UnitRacks/";
		);

		if(createServers) {
			if(numServers > 1) {
			servers = [LoadBalancer(*numServers.collect{ |i|
				Server("ULib server "++(i+1), NetAddr("127.0.0.1",57110+i), options)
			})];
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
				UMenuWindow();
			};
			UGlobalGain.gui;
			UGlobalEQ.gui;
			if( ((thisProcess.platform.ideName == "scqt") && (ULib.allServers.size == 1)).not  ) {
				ULib.serversWindow
			}
		};


		//if not sending the defs they should have been written to disk once before
		// with writeDefaultSynthDefs
		if( sendDefsOnInit ) {
			var defs = this.getDefaultSynthDefs;
			ULib.allServers.do{ |sv| sv.waitForBoot({

				defs.do( _.load( sv ) );

			})
			}
		} {
			ULib.allServers.do(_.boot);
			Udef.loadOnInit = false;
			this.getDefaultUdefs;
			Udef.loadOnInit = true;
        };

		"\n\tUnit Lib started".postln
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

	*savePanel { arg okFunc, cancelFunc;
		var func;
		func = { |path|
			lastPath = path;
			okFunc.( path );
		};
		^Dialog.savePanel( func, cancelFunc, lastPath );
	}

}


	