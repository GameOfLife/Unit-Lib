/*
\shared_buffer_out

** this UMapDef should be used in conjunction with a 'shared_buffer_in' unit **

The 'shared_buffer_out' UMapDef creates a unit that is able to share a (soundfile) buffer or UMap with other units further in the chain. The 'shared_buffer_out' unit needs to before the units that use it in the order of the chain. To retreive the value in an other unit use the 'shared_buffer_in' UMapDef, and make sure the 'id' setting is the same on both. The buffer will always be mono (for multichannel audiofiles only the first channel will be loaded).

id: the id (0-99) by which the buffer can be retreived by a 'shared_buffer_in' UMap. In the GUI this will show up as a colored box, which can be dragged and dropped onto the receiving unit.

--
this is an UMapDef definition file
part of the WFSCollider Class Library default UMapDefs set
*/

UMapDef( \shared_buffer_out, { |soundFile = #[0,1,0]|
	USharedBufferOut.kr( \id, soundFile[0] );
	UMapOut.kr( soundFile, false);
})
	.setSpec( \soundFile, MonoBufSndFileSpec() )
	.setSpec( \id, SharedBufferIDSpec( 0 ) )
	.category_( 'shared_io' )
	.canUseUMapFunc_({ |unit, key, umapdef|
		unit.getSpec( key ).isMemberOf( BufSndFileSpec ) or: {
			unit.getSpec( key ).isMemberOf( MonoBufSndFileSpec )
		};
	});