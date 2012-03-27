	
	
ULib {
    classvar <>servers;

    *initClass {
        servers = [Server.default]
    }
	
	*startup {
		var defs;	
		
		Udef.userDefsFolder = File.getcwd +/+ "UnitDefs";
		
		UChain.makeDefaultFunc = {
			UChain( \bufSoundFile, \stereoOutput ).useSndFileDur
		};
		
		if(thisProcess.platform.class.asSymbol == 'OSXPlatform') {
			UMenuBar();
		};
		
 		defs = Udef.loadAllFromDefaultDirectory.collect(_.synthDef).flat.select(_.notNil);

        ULib.servers.do{ |sv| sv.waitForBoot({

            defs.do( _.load( sv ) );
            
        });
	   "\n\tUnit Lib started".postln
        };
        
        UGlobalGain.gui;
        UGlobalEQ.gui;
	}

}

	
	