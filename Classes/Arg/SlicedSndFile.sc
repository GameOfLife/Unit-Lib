UDataBuf : BufSndFile {

	classvar <globalData;
	var <name;
	var <>bufid;

	*initClass { globalData = (); }

	*makeBufID { ^Date.localtime.hash }

	*new { |data, name = \data|
		^super.newCopyArgs.init( data, name );
	}

	init { |inData, inName|
		bufid = this.class.makeBufID;
		name = inName ? \data;
		globalData[ this.id ] = inData;
	}

	id {
		^[
			this.name,
			this.bufid,
		].join( $_ ).asSymbol
	}

	data { ^globalData[ this.id ] }

	data_ { |newData| globalData[ this.id ] = newData }

	makeBuffer { |server, startPos = 0, action, bufnum, add = true|
		var buf;
		buf = Buffer.uSendCollection( server, this.data.as( FloatArray ), 1, action: action );
		if( add ) { this.addBuffer( buf ); };
		^buf;
	}

	u_waitTime { ^1 }

	printOn { arg stream;
		stream << this.class.name << "(" <<* [
			this.data, name
		]  << ")"
	}

	storeOn { arg stream;
		stream << this.class.name << "(" <<* [ // use newBasic to prevent file reading
			this.data, name
		]  << ")" <<
		if( this.hasGlobal == true ) { ".hasGlobal_(true)" } { "" };
	}

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