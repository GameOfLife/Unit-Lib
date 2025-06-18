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

	uExtractChannels { |channels = 0, outPath, chunkSize = 4194304, threaded = false, action|
		var	rawData, numChunks, test, numFrames;
		var outFile, pth, ext, numDigits, channelsData;

		numFrames.isNil.if({ numFrames = this.numFrames });
		numFrames = numFrames * numChannels;

		channels = channels.asArray;
		channels = channels.select({ |item| item < numChannels });
		if( channels.size == 0 ) {
			"SoundFile:uExtractChannels : no channels available to extract".postln; ^this;
		};

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

		outFile = this.class.new
		.headerFormat_( this.headerFormat )
		.sampleFormat_( this.sampleFormat )
		.numChannels_( channels.size )
		.sampleRate_(this.sampleRate);

		outFile.openWrite(
			"%.%".format( pth.format( channels ), ext )
		);

		this.seek(0, 0);

		while {
			(numFrames > 0) and: {
				rawData = FloatArray.newClear(min(numFrames, chunkSize));
				this.readData(rawData);
				rawData.size > 0
			}
		} {
			channelsData = rawData.clump( numChannels ).flop[ channels ].flop.flat.as(FloatArray);
			// write, and check whether successful
			// throwing the error invokes error handling that closes the files
			(outFile.writeData(channelsData) == false).if({
				MethodError("SoundFile writeData failed.", this).throw
			});
			numFrames = numFrames - chunkSize;
			if(threaded) { $..post; 0.0001.wait; };
		};
		if(threaded) { $\n.postln };
		outFile.close;
		action.value( outFile );
		^outFile
	}

	*uExtractChannels { |inPath, channels, outPath, chunkSize = 4194304, threaded = false, action, deleteOriginal = false|
		var sf;
		if( threaded == true && { thisThread.class != Routine } ) {
			{
				this.uExtractChannels( inPath, channels, outPath, chunkSize, threaded, action, deleteOriginal )
			}.forkIfNeeded( AppClock )
		} {
			sf = this.new;
			sf.openRead( inPath.standardizePath );
			^sf.uExtractChannels( channels, outPath, chunkSize, threaded, { |file|
				sf.close;
				if( deleteOriginal ) { File.delete( sf.path ); };
				"done extracting channels, created file:\n%\n".postf( file.path );
			});
		}
	}

	*uMerge { |inPaths, outPath, chunkSize = 262144, threaded = false, action|
		var inFiles, outFile, maxNumFrames, totalNumChannels, rawData, pth, ext;
		var numDigits;
		if( threaded == true && { thisThread.class != Routine } ) {
			{
				this.uMerge( inPaths, outPath, chunkSize, threaded, action )
			}.forkIfNeeded( AppClock )
		} {
			if( inPaths.isString ) { inPaths = [ inPaths ] };

			inPaths = inPaths.collect({ |item|
				if( item.find("*").notNil ) {
					item.standardizePath.pathMatch;
				} {
					[ item.standardizePath ];
				};
			}).flatten(1);

			inFiles = inPaths.collect({ |path|
				SoundFile.openRead( path );
			});

			maxNumFrames = inFiles.collect( _.numFrames ).maxItem;
			totalNumChannels = inFiles.collect( _.numChannels ).sum;

			// chunkSize must be a multiple of numChannels
			//chunkSize = (chunkSize/numChannels).floor * numChannels;

			if(threaded) {
				("_" ! (maxNumFrames / chunkSize).roundUp(1) ).join.postln;
			};

			if( outPath.isNil ) {
				var i = -1;
				while { inPaths.every({ |path| path.basename.find( inPaths[0].basename[..i+1] ).notNil }) } {
					i = i+1;
				};
				if( i > 0 ) {
					outPath = inPaths[0].dirname +/+ (inPaths[0].basename[..i]) ++ "_merged_%." ++ (inPaths[0].splitext.last);
				};
			};

			#pth, ext = (outPath ?? { inFiles.first.path }).standardizePath.splitext;

			if( pth.find( "%" ).isNil ) {
				pth = pth ++ "_%";
			};

			outFile = this.new
			.headerFormat_( inFiles.first.headerFormat )
			.sampleFormat_( inFiles.first.sampleFormat )
			.numChannels_( totalNumChannels )
			.sampleRate_(inFiles.first.sampleRate);

			numDigits = totalNumChannels.asString.size;

			outFile.openWrite(
				"%.%".format(
					pth.format( totalNumChannels.asStringToBase( 10, numDigits ) ++ "ch" ),
					ext
				)
			);

			inFiles.do( _.seek(0, 0) );

			while {
				(maxNumFrames > 0) and: {
					rawData = inFiles.collect({ |inFile|
						var data;
						data = FloatArray.newClear( chunkSize * inFile.numChannels );
						inFile.readData( data );
						data;
					});
					rawData.any({ |x| x.size > 0 });
				}
			} {
				rawData = rawData.collect({ |data, i|
					if( data.size == 0 ) { data = data.extend( inFiles[1].numChannels, 0 ); };
					data.clump( inFiles[i].numChannels ).flop.collect({ |item|
						item.extend( min( maxNumFrames, chunkSize ), 0.0 );
					});
				}).flatten(1).flop.flat.as( FloatArray );
				//clumps = rawData.clump( numChannels ).flop.collect(_.as(FloatArray));
				// write, and check whether successful
				// throwing the error invokes error handling that closes the files
				(outFile.writeData(rawData) == false).if({
					MethodError("SoundFile writeData failed.", this).throw
				});
				maxNumFrames = maxNumFrames - chunkSize;
				if(threaded) { $..post; 0.0001.wait; };
			};
			if(threaded) { $\n.postln };
			outFile.close;
			inFiles.do( _.close );
			action.value( outFile );
			^outFile
		}
	}


}