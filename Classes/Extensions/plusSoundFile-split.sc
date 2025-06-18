+ SoundFile {
	uSplit { |outPath, chunkSize = 4194304, threaded = false, action|
		var	rawData, numChunks, test, numFrames;
		var outFiles, pth, ext, numDigits, clumps;

		numFrames.isNil.if({ numFrames = this.numFrames });
		numFrames = numFrames * numChannels;

		// chunkSize must be a multiple of numChannels
		chunkSize = (chunkSize/numChannels).floor * numChannels;

		if(threaded) {
			numChunks = (numFrames / chunkSize).roundUp(1);
			 ("_"!numChunks).join.postln;
		};

		#pth, ext = (outPath ?? { this.path }).standardizePath.splitext;

		if( pth.find( "%" ).isNil ) {
			pth = pth ++ "_%";
		};

		outFiles = numChannels.collect({ |i|
			this.class.new
			.headerFormat_( this.headerFormat )
			.sampleFormat_( this.sampleFormat )
			.numChannels_( 1 )
			.sampleRate_(this.sampleRate)
		});

		numDigits = numChannels.asString.size;

		outFiles.do({ |f, i|
			f.openWrite(
				"%.%".format(
					pth.format( (i+1).asStringToBase( 10, numDigits ) ),
					ext
				)
			);
		});

		this.seek(0, 0);

		while {
			(numFrames > 0) and: {
				rawData = FloatArray.newClear(min(numFrames, chunkSize));
				this.readData(rawData);
				rawData.size > 0
			}
		} {
			clumps = rawData.clump( numChannels ).flop.collect(_.as(FloatArray));
			// write, and check whether successful
			// throwing the error invokes error handling that closes the files
			outFiles.do({ |outFile,i|
				(outFile.writeData(clumps[i]) == false).if({
					MethodError("SoundFile writeData failed.", this).throw
				});
			});
			numFrames = numFrames - chunkSize;
			if(threaded) { $..post; 0.0001.wait; };
		};
		if(threaded) { $\n.postln };
		outFiles.do(_.close);
		action.value( outFiles );
		^outFiles
	}

	*uSplit { |inPath, outPath, chunkSize = 4194304, threaded = false, action, deleteOriginal = false|
		var sf;
		if( threaded == true && { thisThread.class != Routine } ) {
			{ this.uSplit( inPath, outPath, chunkSize, threaded, action, deleteOriginal ) }.forkIfNeeded( AppClock )
		} {
			sf = this.new;
			sf.openRead( inPath.standardizePath );
			^sf.uSplit( outPath, chunkSize, threaded, { |files|
				sf.close;
				if( deleteOriginal ) { File.delete( sf.path ); };
				"done splitting, created % files:\n%\n".postf(
					files.size,
					files.collect(_.path).join("\n")
				);
			});
		}
	}

}