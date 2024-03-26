/*
\p_index_soundFile

Creates an UMap that can sequence a number of buffers in a UPattern. For proper use it is best to set the soundFiles
to 'global' so that they don't need to load during pattern playback. The UMap should not be used for an UChain that
is not an UPattern, this will cause issues with the buffers loading especially when not set to 'global'

soundFiles: (Array of BufSndFiles) this can be a collection of BufSndFiles of any size (max 256 is recommended)
index: (Integer) the index of the soundFile to be played. This can be an UMap too, for example p_int_step can either
loop through the files or randdomize.

--
this is an UMapDef definition file
part of the WFSCollider Class Library default UMapDefs set
*/

UPatDef( \p_index_soundFile, { |
		unit,
		soundFiles,
		index = 0|
	{ loop { soundFiles.next.wrapAt( index.next.asInteger ).yield; } }.r
})
	.category_( 'pattern' )
	.canUseUMapFunc_({ |unit, key, umapdef|
		unit.getSpec( key ).isKindOf( BufSndFileSpec );
	})
	.numChannels_( 3 )
    .setSpec( \value, AnythingSpec(), private: true )
	.setSpec( \index, IntegerSpec(0,0,256) )
	.setSpec( \soundFiles, MultiSndFileSpec(
		{ "@resources/sounds/a11wlk01-44_1.aiff".asBufSndFile } ! 8,
		false,
	) )
	.setDefault( \soundFiles,
		{ "@resources/sounds/a11wlk01-44_1.aiff".asBufSndFile } ! 8,
	);
	