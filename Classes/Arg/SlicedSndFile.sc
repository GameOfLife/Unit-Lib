UDataBuf : BufSndFile {

	classvar <globalData;
	var <name;
	var <bufid;

	*initClass {
		globalData = ();
		globalClassKeyDict.put( \d, this );
	}

	*makeBufID { ^Date.localtime.hash }

	*new { |data, name = \data, id|
		^super.newCopyArgs.init( data, name, id );
	}

	init { |inData, inName, inID|
		this.bufid = inID;
		this.name = inName;
		this.data = inData;
	}

	id {
		^[
			this.class.globalKey,
			this.name,
			this.bufid,
		].join( $_ ).asSymbol
	}


	*prFromID { |id|
		var splitID, name, bufid;
		splitID = id.asString.split($_)[1..];
		bufid = splitID.last.interpret;
		splitID.pop;
		name = splitID.join( $_ ).asSymbol;
		^this.new( globalData[ id ], name, bufid );
	}

	bufid_ { |newBufID|
		bufid = newBufID ?? { this.class.makeBufID; };
		this.changed( \bufid, bufid );
	}

	name_ { |newName|
		name = newName ? \data;
		this.changed( \name, name );
	}

	makeUnique {
		var data;
		data = this.data.copy;
		this.bufid = this.class.makeBufID;
		this.data = data;
	}

	data { ^globalData[ this.id ] }

	dataForBuf { ^this.data.as( FloatArray ) }

	data_ { |newData|
		globalData[ this.id ] = newData;
		this.class.changed( \globalData, this.id, newData );
	}

	at { |index| ^this.data[ index ] }
	put { |index, value|
		this.data.put( index, value );
		this.class.changed( \globalData, this.id, this.data );
	}

	makeBuffer { |server, startPos = 0, action, bufnum, add = true|
		var buf;
		buf = Buffer.uSendCollection( server, this.dataForBuf, 1, -1, action: action );
		if( add ) { this.addBuffer( buf ); };
		^buf;
	}

	updateBuffers { |action|
		var buffers, data;
		buffers = this.getAllBuffers;
		if( buffers.size > 0 ) {
			data = this.dataForBuf;
			action = MultiActionFunc( action );
			buffers.do({ |buf|
				buf.uAdjustNumFrames( data.size, {
					{ buf.sendCollection( data, 0, -1, action.getAction ); }.fork;
				});
			});
		} {
			action.value(this);
		};
	}

	u_waitTime { ^1 }

	printOn { arg stream;
		stream << this.class.name << "(" <<* [
			this.data, name
		]  << ")"
	}

	storeOn { arg stream;
		stream << this.class.name << "(" <<* [ // use newBasic to prevent file reading
			this.data, name, this.bufid
		]  << ")" <<
		if( this.hasGlobal == true ) { ".hasGlobal_(true)" } { "" };
	}

}

UWaveTableBuf : UDataBuf {

	*initClass {
		globalClassKeyDict.put( \wt, this );
	}

	dataForBuf { ^this.data.as(Signal).asWavetable }

}

SlicedBufSndFile : BufSndFile {

	var <slices;
	var <>bufSndFiles;

	slices_ { |newSlices|
		slices = newSlices;
		this.changed( \slices, slices );
	}

	makeBufSndFile { |index = 0|
		var start, end;
		start = (slices ?? {[]})[index] ? 0;
		end = (slices ?? {[]})[index+1] ?? { this.numFrames; };
		^BufSndFile.newBasic( path.formatGPath, numFrames, numChannels, sampleRate, start, end, rate, loop )
		.useChannels_( useChannels )
	}

	fillBufSndFiles {
		bufSndFiles = slices.collect({ |slice,i| this.makeBufSndFile( i ); });
	}
}