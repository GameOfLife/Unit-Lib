/*
\shared_buffer

** this Udef should be used in conjunction with 'shared_buffer_in' UMaps **

The 'shared_buffer' Udef creates a unit that is able to share a (soundfile) buffer or UMap with other units further in the chain. The 'shared_buffer' unit needs to before the units that use it in the order of the chain. To retreive the value in an other unit use the 'shared_buffer_in' UMapDef, and make sure the 'id' setting is the same on both. The buffer will always be mono (for multichannel audiofiles only the first channel will be loaded).

soundFile: a soundfile object of which the buffer will be shared. Note: the 'rate' and 'loop' settings of the soundfile are ignored, and can be set separately in the corresponding shared_buffer_in UMaps.
id: the id (0-99) by which the buffer can be retreived by a 'shared_buffer_in' UMap. In the GUI this will show up as a colored box, which can be dragged and dropped onto the receiving unit.

--
this is an Udef definition file
part of the Unit lib default UDefs set
*/

Udef( \shared_buffer, { |soundFile = #[0,1,0]|
	UEnv.kr( extraSilence: 0.2, useGlobalGain: 0 );
	USharedBufferOut.kr( \id, soundFile[0] );
})
	.setSpec( \soundFile, MonoBufSndFileSpec() )
	.setDefault( \soundFile, nil )
	.category_( 'shared_io' );