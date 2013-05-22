	
	
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

	
	