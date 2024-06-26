/*
\bufSoundFile

A soundfile player. This plays soundfiles of any duration, with any (*) number of channels,
by loading the whole file into the memory.
Use this typically for shorter (less than apx 2 minutes) audiofiles.
For longer files it is advised to use \diskSoundFile instead

soundFile: a BufSndFile object, a region of an existig soundfile url on your hard drive.
The object knows the duration and file path of the soundfile, and can be set to clip
off the start and end of the file.
	- start: region startoffset (samples or seconds)
	- end: end of the region (samples or seconds)
	- loop: loop mode on or off (can be changed during playback)
	- rate: playback rate (semitones or ratio)
level: playback level (amplitude of the soundfile)

(*) only the following numbers of channels are allowed:
1,2,3,4,5,6,7,8,10,12,16,24,32

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

MultiChannelUdef( \bufSoundFile, {
		UMixOut.ar( 0, BufSndFilePlayer.ar( Udef.numChannels, \soundFile, \trigger.utr(1) )
			* \level.ukr( 1, \amp ), 0, true );
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