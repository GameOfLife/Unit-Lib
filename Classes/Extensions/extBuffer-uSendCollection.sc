+ Buffer {

	*uSendCollection { |server, collection, numChannels = 1, wait = -1, action|
		var buffer = this.alloc(server, ceil(collection.size / numChannels), numChannels);
		var pos, collstream, collsize, bundsize;
		if( UEvent.nrtMode != true ) {
			OSCFunc({ |msg, time, addr|
				{ buffer.sendCollection( collection, 0, wait, action ); }.fork;
			}, '/done', server.addr, argTemplate: [ '/b_alloc', buffer.bufnum ]).oneShot;
		} {
			collstream = CollStream.new;
			collstream.collection = collection;
			collsize = collection.size;
			pos = collstream.pos;
			while { pos < collsize } {
				// 1626 max size for setn under udp
				bundsize = min(1626, collsize - pos);
				server.listSendMsg(['/b_setn', buffer.bufnum, pos, bundsize]
					++ Array.fill(bundsize, { collstream.next }));
				pos = collstream.pos;
			};
		};
		^buffer;
	}

	*uReplaceBuffer { |buffer, numFrames, numChannels = 1, action, checkIfNeeded = true|
		var newBuf;
		if( checkIfNeeded && { buffer.numFrames == numFrames && { buffer.numChannels == numChannels } } ) {
			newBuf = buffer;
			action.value( newBuf );
		} {
			newBuf = Buffer( buffer.server, numFrames, numChannels, buffer.bufnum, buffer.sampleRate );
			OSCFunc({ |msg, time, addr|
				action.value( newBuf );
			}, '/done', newBuf.server.addr, argTemplate: [ '/b_alloc', newBuf.bufnum ]).oneShot;
			buffer.free({ newBuf.server.bufferAllocator.reserve( newBuf.bufnum ); newBuf.allocMsg });
		}
		^newBuf;
	}

	uAdjustNumFrames { |newNumFrames, action|
		if( numFrames != newNumFrames ) {
			numFrames = newNumFrames;
			OSCFunc({ |msg, time, addr|
				action.value( this );
			}, '/done', server.addr, argTemplate: [ '/b_alloc', bufnum ]).oneShot;
			//server.listSendMsg([\b_free, bufnum, this.allocMsg]);
			this.alloc; // should be safe to alloc without freeing first
		} {
			action.value( this );
		};
	}
}