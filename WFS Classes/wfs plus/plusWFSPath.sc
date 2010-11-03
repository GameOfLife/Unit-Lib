+ WFSPath {
	
	sndFileData {
		^(positions.asArray.repeatLast(2).flop ++ [ times ++ [1000,1000,1000]]).flop.flat;
		}
	
	sndFileAdd { |sndFile, pos = 0|
		var sndFileData;
		sndFileData = this.sndFileData;
		sndFile.writeData( Signal.with( *sndFileData ) );
		sndFileStart = pos;
		sndFileName = sndFile.path;
		pos = pos + (sndFileData.size / 4);
		sndFileEnd = pos;
		^pos;
		}
	
	sndFileNumFr {
		^sndFileEnd - sndFileStart;
		}
	
	readBufferXYZ { |server, path, action|
		sndFileName = path ? sndFileName;
		if( sndFileStart.notNil &&  sndFileEnd.notNil )
			{ ^Buffer.readChannel(server, sndFileName, 
				sndFileStart, this.sndFileNumFr, [0,1,2], { |thisBuffer|
				action.value( thisBuffer );
				WFS.debug( "xyz buffer % from file \n\t%\n\tfor path % loaded on %",
					thisBuffer.bufnum, sndFileName, name, server.name ); } );  }
			{ "ERROR: WFSPath.readBufferXYZ unknown file".postln; }
		}
	
	readBufferTimes { |server, path, action|
		sndFileName = path ? sndFileName;
		if( sndFileStart.notNil &&  sndFileEnd.notNil )
			{ ^Buffer.readChannel(server, sndFileName, 
				sndFileStart, this.sndFileNumFr, [3], { |thisBuffer|
				action.value( thisBuffer ); 
				WFS.debug( "times buffer % from file \n\t%\n\tfor path % loaded on %",
					thisBuffer.bufnum, sndFileName, name, server.name ); } );
				}
			{ "ERROR: WFSPath.readBufferTimes unknown file".postln; }
		}
		
	loadBuffers2 {  |server, xyzBufnum, tBufnum, loadedAction|
			if( sndFileName.isNil )
				{ ^this.loadBuffers( server, xyzBufnum, tBufnum, loadedAction ) }
				{ ^this.loadBuffersFile( server, xyzBufnum, tBufnum, loadedAction ) };
		}
		
	loadBuffersFile {  |server, xyzBufnum, tBufnum, loadedAction|		var posXYZBuffer, timesBuffer;
		
		// server can be array of servers
		buffers = [nil, nil];
		
		server.asCollection.do({ |oneServer|
			
			buffers[0] = buffers[0].asCollection.add( 
				this.readBufferXYZ( oneServer, 
					action: { |thisBuffer|
						buffersLoaded[0] = true;
						if( WFSPan2D.silent.not )
							{  ("XYZ buffer (" ++ 
								thisBuffer.bufnum ++  ") loaded for path" ++ name ).postln; };
						if( buffersLoaded.every( _.value ) )
							{ loadedAction.value };
							} )
					);
							
			buffers[1] =  buffers[1].asCollection.add( 
				this.readBufferTimes( oneServer, 
					action: { |thisBuffer|
						buffersLoaded[1] = true;
						if( WFSPan2D.silent.not ) 
							{("Times buffer (" ++ 
								thisBuffer.bufnum ++  ") loaded for path" ++ name ).postln;  };
						if( buffersLoaded.every( _.value ) )
							{ loadedAction.value };
						} )
					);
				});
		
		^this;
		}
	
	}
	
+ WFSScore {
	
	writePathData { |filePath, updateServers = true|
		var paths, file, filePos = 0;
		filePath = filePath ? ("/wfspathdata/paths_" ++ Date.getDate.stamp ++ ".aiff");
		paths = this.allPositions.select({ |item| item.class == WFSPath; });
		file =  SoundFile.new.headerFormat_("AIFF").sampleFormat_("float")
			.path_( filePath.standardizePath ).numChannels_(4);
		file.openWrite( file.path );
		paths.do({ |pth| filePos = pth.sndFileAdd( file, filePos ); });
		file.close;
		if( updateServers )
			{ 
			("rsync -vrut --delete" + "/wfspathdata/".quote + 
				"gameoflife@192.168.2.11:/wfspathdata/".quote).unixCmd;
			("rsync -vrut --delete" + "/wfspathdata/".quote + 
				"gameoflife@192.168.2.12:/wfspathdata/".quote).unixCmd;
			};
		}
	
	allPositions { // doesn't copy, just points
		^events.collect({ |event|
			if( event.isFolder )
				{ event.wfsSynth.allPositions  }
				{ event.wfsSynth.wfsPath };
			}).flat;
		}
	
	}
	
