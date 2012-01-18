	
	
ULib {
	
	*startup {
		var defs;	
		
		Udef.userDefsFolder = File.getcwd +/+ "UnitDefs";
		
		UChain.makeDefaultFunc = {
			UChain( \bufSoundFile, \stereoOutput ).useSndFileDur
		};
		
		if(thisProcess.platform.class.asSymbol == 'OSXPlatform') {
			UMenuBar();
		};
		
		defs = Udef.loadAllFromDefaultDirectory.collect(_.synthDef).flat.select(_.notNil)
        ++WFSPrePanSynthDefs.generateAll.flat++WFSPreviewSynthDefs.generateAll;
        
        Server.default.waitForBoot({

            defs.do({|def|
                def.load( Server.default );
            });
            "\n\tUnit Lib started".postln
        });
        
        UGlobalGain.gui;
        UGlobalEQ.gui;
	}

}

	
	