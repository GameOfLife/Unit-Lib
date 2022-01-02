+ Buffer {

	*uSendCollection { |server, collection, numChannels = 1, wait = 0.02, action|
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

}