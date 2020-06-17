URecSndFile : AbstractSndFile {
	
	var <>diskBufferSize = 65536;
	var <headerFormat = "aiff", <sampleFormat = "int24";
	var <>score;

	*new{ |path, numChannels = 1, headerFormat = "aiff", sampleFormat = "int24" | 
		// path of existing file or SoundFile
		if( path.class == SoundFile ) {
			^this.newBasic( path.path, nil, numChannels )
				.headerFormat_( headerFormat )
				.sampleFormat_( sampleFormat );
		} {
			^this.newBasic( path, nil, numChannels )
				.headerFormat_( headerFormat )
				.sampleFormat_( sampleFormat );
		};
	}
	
	*ar { |channelsArray, key|
		var bufnum;
		key = key ? 'soundFile';
		if( key.isKindOf( Symbol ) ) {
		   Udef.addBuildSpec( ArgSpec(key, nil, URecSndFileSpec( channelsArray.size ) ) );
		   key = key.kr(0);
		};
		DiskOut.ar( key, channelsArray );
	}
	
	*checkExists { ^false }
	
	headerFormat_ { |newFormat = "aiff"|
		if( #[  aiff, wav, wave, riff, sun, next, sd2, ircam, raw, none, mat4, mat5, paf, svx, nist, voc, w64, pvf, xi, htk, sds, avr, flac, caf ]
			.includes( newFormat.toLower.asSymbol ).not ) {
				"URecSndFile.headerFormat: '%' not supported".format( newFormat ).warn;
		};
		headerFormat = newFormat;
		this.changed( \headerFormat, headerFormat );
	}
	
	sampleFormat_ { |newFormat = "int24"|
		if( #[ int8, int16, int24, int32, mulaw, alaw, float ]
			.includes( newFormat.toLower.asSymbol ).not ) {
				"URecSndFile.headerFormat: '%' not supported".format( newFormat ).warn;
		};
		sampleFormat = newFormat;
		this.changed( \sampleFormat, sampleFormat );
	}
		
	asBufSndFile { 
		var unit; // pass unit on to new object
		unit = this.unit;
		this.unit = nil;
		^BufSndFile.newCopyVars( this ).unit_( unit ); 
	}
	
	asMonoBufSndFile {
		^MonoBufSndFile.newCopyVars( this );
	}
	
	asDiskSndFile { 
		var unit; // pass unit on to new object
		unit = this.unit;
		this.unit = nil;
		^DiskSndFile.newCopyVars( this ).unit_( unit ); 
	}
	
	asControlInputFor { |server, startPos = 0| 
		^this.currentBuffer(server, startPos)
	}
	
	fromFile { |soundfile| // disabled
	}
	
	makeBuffer {  |server, startPos = 0, action, bufnum|  // startOffset in seconds
	    //startFrame, endFrame not used
		var test = true;
		var buf, addStartFrame = 0;
		var actualStartFrame;
		
		buf = Buffer.alloc(server, diskBufferSize.asInteger, numChannels, { arg buffer;
			buffer.writeMsg( path.getGPath, headerFormat, sampleFormat, 0, 0, true, { |buf|
				["/b_query", buf.bufnum]
			});
		}).doOnInfo_(action).cache;
		this.addBuffer( buf );
		^buf;
	}
	
	 freeBuffer { |buf, action|
		 if( UEvent.nrtMode != true ) {
			buf.checkCloseFree( action );
		 } {
			 buf.close;
			 buf.free;
			 action.value( buf );
		 };
		this.removeBuffer( buf );
	}

    unitNamePrefix{ ^"rec" }
    
    u_waitTime { ^1 }
    
    printOn { arg stream;
		stream << this.class.name << "(" <<* [
		    	path, numChannels, headerFormat, sampleFormat 
		]  <<")"
	}
	
	storeOn { arg stream;
		stream << this.class.name;
		this.storeParamsOn(stream);
		this.storeModifiersOn(stream);
	}
    
    /*
    storeOn { arg stream;
	    var hf = [];
	    if( sampleFormat != "int24" ) {
		    hf = [ headerFormat, sampleFormat ];
	    } {
		    if( headerFormat != "aiff" ) {
			    hf = [ headerFormat ];
		    };
	    };
	    stream << this.class.name << "(" <<* ([ // use newBasic to prevent file reading
		    path.formatGPath.quote, numChannels 
		] ++ hf) << ")"
	}
	*/
	
	storeArgs {
		^[ path.formatGPath, numChannels, headerFormat, sampleFormat ]
	}
	
}

