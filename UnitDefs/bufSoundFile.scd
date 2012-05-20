MultiChannelUdef( \bufSoundFile, {
		UMixOut.ar( 0, BufSndFilePlayer.ar( Udef.numChannels ), 0, true );
	}, [ [ \soundFile, nil, BufSndFileSpec(nil) ] ], \soundFile, true )
	.chooseFunc_({ |args|
		var sf, numChannels;
		sf = (args ? []).pairsAt( \soundFile );
		if( sf.notNil ) { sf.numChannelsForPlayBuf } { 1 };
	})
	.nameFunc_({ |unit|
		var sf, path;
		if( (sf = unit.get( \soundFile )).notNil && { sf.path.notNil } ) {
			sf.path.basename ++ " : bufSoundFile";
		} {
			"bufSoundFile"
		};
	});