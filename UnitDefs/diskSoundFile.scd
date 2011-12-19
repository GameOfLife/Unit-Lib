(
var udefs;

udefs = [1,2,3,4,5,6,7,8,10,12,16,24,32].collect({ |numChannels|
	HiddenUdef( "diskSoundFile_%".format( numChannels ).asSymbol, {
		UMixOut.ar( 0, DiskSndFilePlayer.ar( numChannels ), 0, true );
	}, [ [ \soundFile, nil, DiskSndFileSpec(nil) ] ], \soundFile )
});

MultiUdef( \diskSoundFile, udefs, \soundFile )
	.chooseFunc_({ |args|
		var sf, numChannels;
		sf = (args ? []).pairsAt( \soundFile );
		if( sf.notNil ) { 
			"diskSoundFile_%".format( sf.numChannels ).asSymbol;
		} {
			\diskSoundFile_1
		};
	});

)