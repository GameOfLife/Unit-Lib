MultiChannelUdef( \diskSoundFile, {
		UMixOut.ar( 0, DiskSndFilePlayer.ar( Udef.numChannels ), 0, true );
	}, [ [ \soundFile, nil, DiskSndFileSpec(nil) ] ], \soundFile, true )
	.chooseFunc_({ |args|
		var sf, numChannels;
		sf = (args ? []).pairsAt( \soundFile );
		if( sf.notNil ) { sf.numChannels } { 1 };
	})
	.nameFunc_({ |unit|
		var sf, path;
		if( (sf = unit.get( \soundFile )).notNil && { sf.path.notNil } ) {
			sf.path.basename ++ " : diskSoundFile";
		} {
			"diskSoundFile"
		};
	});
