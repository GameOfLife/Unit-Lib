/*
\shared_buffer_in

** this UMapDef should be used in conjunction with a 'shared_buffer' unit **

The shared_buffer_in UMapDef can receive a buffer pointer from a 'shared_buffer' unit earlier in the chain. This way a single buffer can be played by multiple buffer-playing units (i.e. bufSoundFile, grainSoundFile etc.).

id: the id (0-99) by which the point can be retreived from 'shared_buffer'
rate: the playback rate
loop: loop mode on/off for playback

--
this is an UMapDef definition file
part of the WFSCollider Class Library default UMapDefs set
*/

UMapDef( \shared_buffer_in, { 
	var buffer;
	buffer = USharedBufferIn.kr( \id );
	UMapOut.kr([buffer,\rate.kr(1),\loop.kr(0)], false);
})
	.setSpec( \id, SharedBufferIDSpec( 0 ) )
	.setSpec( \rate, [0,2,\lin,0,1].asSpec )
	.setSpec( \loop, BoolSpec(true) )
	.category_( 'shared_io' )
	.canUseUMapFunc_({ |unit, key, umapdef|
		unit.getSpec( key ).isMemberOf( BufSndFileSpec ) or: {
			unit.getSpec( key ).isMemberOf( MonoBufSndFileSpec )
		};
	});