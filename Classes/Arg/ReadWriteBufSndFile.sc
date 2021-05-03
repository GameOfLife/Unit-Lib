ReadWriteBufSndFile : BufSndFile {
	
	// read a soundfile and write it back to disk after ending
	// *warning* your existing soundfile will be overwritten!
	
	var <write = true;
	
	write_ { |new = true|
		write = new.booleanValue;
		this.changed( \write, write );
	}
	
	freeBuffer { |buf, action|
		if( buf.notNil ) {
			if( UEvent.nrtMode != true ) {
				OSCFunc({  |msg, time, addr, recvPort|
					buf.freeMsg; // cleans up internally
					action.value( buf );
				},  '/done', buf.server.addr, nil, ['/b_free', buf.bufnum ] ).oneShot;
				
				if( write == true ) {
					buf.write( buf.path, completionMessage: [  "/b_free", buf.bufnum ] );
				} {
					buf.server.addr.sendMsg( "/b_free", buf.bufnum );
				};
			} {
				if( write == true ) {
					buf.write( buf.path, completionMessage: buf.freeMsg );
				} {
					buf.free;
				};
				action.value( buf );
			};
			this.removeBuffer( buf );
		} {
		    	action.value;
		};
	}
	
	asReadWriteBufSndFile {
		^this;
	}
	
	asBufSndFile {
		^BufSndFile.newCopyVars( this );
	}
	
	storeOn { arg stream;
		stream << this.class.name << ".newBasic(" <<* [ // use newBasic to prevent file reading
		    path.formatGPath.quote, numFrames, numChannels, sampleRate,
             startFrame, endFrame, rate, loop
		]  << ")" << 
		if( this.write != true ) { ".write_(%)".format( this.write ) } { "" } <<
		if( this.channel != 0 ) { ".channel_(%)".format( this.channel ) } { "" } <<
		if( this.hasGlobal == true ) { ".hasGlobal_(true)" } { "" };
	}
	
}

ReadWriteBufSndFileSpec : BufSndFileSpec {
	
	*testObject { |obj|
		^obj.isKindOf( ReadWriteBufSndFile );
	}
	
	constrain { |value|
		value = value.asReadWriteBufSndFile;
		if( numChannels.notNil ) {
			if( numChannels.asCollection.includes( value.numChannels ).not ) {
				if( numChannels.asCollection.includes( value.useChannels.size ).not ) {
					value.useChannels = (..numChannels.asCollection[0]-1)
						.wrap( 0, value.numChannels );
				};
			};
		};
		^value;
	}
	
	map { |in| ^this.constrain( in ) }
	unmap { |in| ^in }
	
	default { 
		^nil.asReadWriteBufSndFile;
	}
	
	*newFromObject { |obj|
		^this.new( obj.numChannels );
	}
	
}

+ BufSndFile {
	asReadWriteBufSndFile {
		^ReadWriteBufSndFile.newCopyVars( this );
	}
}

+ Object {
	asReadWriteBufSndFile { 
		^BufSndFile.newBasic(Platform.resourceDir +/+ "sounds/a11wlk01-44_1.aiff", 107520, 1, 44100, 0, nil, 1, false)
	}
}