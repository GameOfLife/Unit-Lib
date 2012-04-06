(
var udefs;

udefs = [1,2,3,4,5,6,7,8,10,12,16,24,32].collect({ |numChannels|
	HiddenUdef( "bufSoundFile_%".format( numChannels ).asSymbol, {
		UMixOut.ar( 0, BufSndFilePlayer.ar( numChannels ), 0, true );
	}, [ [ \soundFile, nil, BufSndFileSpec(nil) ] ], \soundFile )
});

MultiUdef( \bufSoundFile, udefs, \soundFile )
	.chooseFunc_({ |args|
		var sf, numChannels;
		sf = (args ? []).pairsAt( \soundFile );
		if( sf.notNil ) { 
			"bufSoundFile_%".format( sf.numChannelsForPlayBuf ).asSymbol;
		} {
			\bufSoundFile_1
		};
	})
	.nameFunc_({ |unit|
		var sf, path;
		if( (sf = unit.get( \soundFile )).notNil && { sf.path.notNil } ) {
			"bufSoundFile : " ++  sf.path.basename
		} {
			"bufSoundFile"
		};
	});

)