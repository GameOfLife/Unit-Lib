/*
\rec_soundBuf

This UMapDef can be used to replace a regular BufSndFile (soundFile) on \bufSoundFile, \grainSoundFile, \loopSoundFile and \trigSoundFile units. It creates a single-channel (mono) buffer that can record audio from previous units in the chain, in realtime. The buffer is passed on to the unit for immediate playback. Note that the buffer is always empty at the start, and will be deleted after stopping the chain.

buffer: the buffer; In the editor the duration of the buffer can be set in seconds.
recLevel: level of recording input (0 means silence)
preLevel: level of the existing material in the buffer, to mix with the input
run: turns the recording on and off
loop: if enabled, the recording will loop (i.e. start at the beginning of the buffer when the end is reached), if off the recording will end when the end of the buffer is reached.
trigger: a trigger to start or reset the recording at the beginning of the buffer
playbackRate: the rate for playback (passed through to the playback unit)
playbackLoop: enables/disables looped playback (passed through to the playback unit)

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UMapDef( \rec_soundBuf, { |buffer = 0, recLevel = 1.0, preLevel = 0.0, run = 1.0, loop = 1.0|
	var sig;
	var trigger;
	sig = UIn.ar(0);
	trigger = \trigger.tr(0);
	RecordBuf.ar( sig, buffer, 0, recLevel, preLevel, run, loop, trigger );
	UMapOut.kr([buffer,\playbackRate.kr(1),\playbackLoop.kr(0)], false);
})
	.setSpec( \buffer, RichBufferSpec( 1, editMode: \duration ), \init )
	.setSpec( \run, BoolSpec(true) )
	.setSpec( \loop, BoolSpec(true) )
	.setSpec( \trigger, TriggerSpec() )
	.setSpec( \playbackRate, [0,2,\lin].asSpec )
	.setSpec( \playbackLoop, BoolSpec(true) )
	.prepareArgsFunc_( { |args| 
		var bus;
		bus = args.pairsAt( \bus );
		args ++ [ \u_i_ar_0_bus, bus ];
	 } )
	.category_( \input )
	.canUseUMapFunc_({ |unit, key, umapdef|
		unit.getSpec( key ).isMemberOf( BufSndFileSpec ) or: {
			unit.getSpec( key ).isMemberOf( MonoBufSndFileSpec )
		};
	});
