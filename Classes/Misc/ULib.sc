	
	
ULib {
    classvar <>servers;

    *initClass {
        servers = [Server.default]
    }

	*allServers {
        ^servers.collect{ |s|
            if( s.isKindOf( LoadBalancer ) ) {
                s.servers
            } {
                s
            }
        }.flat
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

    *serversWindow {
        var makePlotTree, makeMeter, killer;
        var servers = ULib.allServers;
        var w = Window("ULib servers", Rect(10, 10, 390, 3 + ( servers.size * 29))).front;
        w.addFlowLayout;
        killer = Button(w, Rect(0,0, 20, 18));
        killer.states = [["K"]];
        killer.canFocus = false;
        killer.action = { Server.killAll };
        w.view.decorator.postln.nextLine;
        servers.do{ |s| s.makeView(w) };
        w.view.keyDownAction = { arg view, char, modifiers;
            // if any modifiers except shift key are pressed, skip action
            if(modifiers & 16515072 == 0) {

                case
                {char === $n } { servers.do( _.queryAllNodes(false) ) }
                {char === $N } { servers.do( _.queryAllNodes(true) ) }
                {char === $l } { makeMeter.() }
                {char === $p}  { makePlotTree.() }
                {char === $ }  { servers.do{ |s| if(s.serverRunning.not) { s.boot } } }
            };
        };
        makePlotTree = {
            var onClose, comp;
            var servers = ULib.allServers;
            var window = Window.new("Node Tree(s)",
                Rect(128, 64, 1000, 400),
                scroll:true
            ).front;
            var x = CompositeView(window.view, Rect(0,0,4000,4000));
            x.addFlowLayout(0@0,0@0);
            comp = servers.collect{ CompositeView(x,400@400) };
            window.view.hasHorizontalScroller_(false).background_(Color.grey(0.9));
            onClose = [servers, comp].flopWith{ |s,c| s.plotTreeView(0.5, c, { defer {window.close}; }) };
            window.onClose = {
                onClose.do( _.value );
            };
        };
        makeMeter = {
            var window = Window.new("Meter",
                Rect(128, 64, 1000, 1000),
            ).front;
            var x = CompositeView(window.view, Rect(0,0, 1000, 1000));
            x.addFlowLayout;
            servers.do{ |s|
                var numIns = s.options.numInputBusChannels;
                var numOuts = s.options.numOutputBusChannels;
                ServerMeterView(s, x, 0@0, numIns, numOuts)
            }
        };
        ^w
    }

	*startup {
		var defs;	
		
		UChain.makeDefaultFunc = {
			UChain( \bufSoundFile, \stereoOutput ).useSndFileDur
		};
		
		if( (thisProcess.platform.class.asSymbol == 'OSXPlatform') && {
				thisProcess.platform.ideName.asSymbol === \scapp 
		}) {
			UMenuBar();
		} {
			UMenuWindow();
		};
		
		Udef.loadOnInit = false;
 		defs = Udef.loadAllFromDefaultDirectory.collect(_.synthDef).flat.select(_.notNil);
 		Udef.loadOnInit = true;
 		
 		UnitRack.defsFolders = UnitRack.defsFolders.add( 
			Platform.userAppSupportDir ++ "/UnitRacks/";
		);

        ULib.servers.do{ |sv| sv.waitForBoot({

            defs.do( _.load( sv ) );
            
        });
	   "\n\tUnit Lib started".postln
        };
        
        UGlobalGain.gui;
        UGlobalEQ.gui;
	}

}

	
	