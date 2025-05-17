
SplitBufSndFile : BufSndFile {

	asBufSndFile {
		^BufSndFile.newCopyVars( this );
	}

	asControlInputFor { |server, startPos = 0|
		^[
			this.findGlobal( server ) ?? { this.currentBuffer(server, startPos).first },
			numChannels, rate, loop.binaryValue
		]
	}

	currentBuffers { |server| // returns all buffers if server == nil
		if( server.notNil ) {
			^this.buffers.select({ |item| item[0].server == server }) ? #[];
		};
		^this.buffers;
	}

	prReadMulti { |server, path, startFrame, numFrames, channels = 2, action, bufnum|
		var startBuf, buffers;
		startBuf = bufnum ?? { server.nextBufferNumber(numChannels); };
		action = MultiActionFunc( action );
		if( channels.isKindOf( Number ) ) {
			channels = (..channels-1);
		};
		^channels.collect({ |ch, i|
			Buffer.readChannel( server, path,
				channels: [ch],
				action: action.getAction,
				bufnum: startBuf + i
			)
		});
	}

	makeBuffer { |server, startPos = 0, action, bufnum, add = true|
		var bufs, test = true, addStartFrame = 0, localUsedFrames;

		localUsedFrames = this.usedFrames;

		if( numChannels.isNil ) {
			test = this.prReadFromFile; // get numchannels etc.
		};

		if( useStartPosForBuf && { startPos != 0 } ) {
			addStartFrame = this.secondsToFrames( startPos );
			if( localUsedFrames != -1 ) {
				localUsedFrames - addStartFrame;
			};
		};

		if( test ) {
			bufs = this.prReadMulti( server, path.getGPath,
				startFrame + addStartFrame, localUsedFrames,
				this.useChannels ? numChannels, action, bufnum
			);
			if( add ) { this.addBuffer( bufs ); };
			^bufs
		} {
			"SplitBufSndFile:makeBuffer : file not found".warn;
		};
	}

	freeBuffer { |bufs, action|
		if( bufs.notNil ) {
			if( UEvent.nrtMode != true ) {
				action = MultiActionFunc( action );
				bufs.do({ |buf|
					buf.checkFree( action.getAction );
				});
			} {
				bufs.do(_.free);
				action.value( bufs );
			};
			this.removeBuffer( bufs );
		} {
			action.value;
		};
	}

}
