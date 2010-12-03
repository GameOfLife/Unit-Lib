/*
    GameOfLife WFSCollider - Wave Field Synthesis spatialization for SuperCollider.
    The Game Of Life Foundation. http://gameoflife.nl
    Copyright 2006-2010 Wouter Snoei.

    GameOfLife WFSCollider software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    GameOfLife WFSCollider is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with GameOfLife WFSCollider.  If not, see <http://www.gnu.org/licenses/>.
*/

// Sync System (based on pulse via digital connection to clients)

+ WFSServers {

	*syncSynthDef { 
		
		^SynthDef( "wfs_sync_get_delay", { |out = 0, input = 1|
	
			var impulse, in, controlDur, outVal;
			controlDur = ControlRate.ir.reciprocal;

			impulse = DelayN.ar( Impulse.ar( 0 ), controlDur, controlDur );
			in = AudioIn.ar( input );
			outVal = ( controlDur - ( Timer.ar( in + impulse )) ) * SampleRate.ir;
			
			// stop the synth after value is established
			EnvGen.kr( Env([1,1], [ controlDur * 3 ] ), doneAction: 2 );
			
			// send the value over network (not reliable for remote servers )
			SendTrig.ar( impulse, 0, outVal ); 
			
			// set value on control bus
			Out.kr(out, A2K.kr( Latch.ar( outVal, impulse ) ) );
			});
		}
	
	*syncPulsesSynthDef { 
		^SynthDef( "wfs_sync_pulses", { |out = 14|  // adat output 1
			Out.ar(out, Impulse.ar( ControlRate.ir ) - 0.001 ); 
			// 0.001 offset to ensure zerocrossing
			});
		}
		
	sendSyncSynthDef { var def;
		def = this.class.syncSynthDef;
		multiServers.do({ |ms| 
			ms.servers.do({ |server|
				def.send( server );
				}); 
			});
		}
		
	loadSyncSynthDef { var def;
		def = this.class.syncSynthDef;
		multiServers.do({ |ms| 
			var dir;
			dir = dir ? ms.synthDefDir;
			if( dir.pathMatch.size != 0 )
				{ def.writeDefFile(dir);
					ms.servers.do( _.listSendMsg(
						["/d_load", dir ++ def.name ++ ".scsyndef"]
					) ); }
				{ ("WFSServers-loadSyncSynthDef: defFile could not be written in:\n\t"++
					dir ++ "\n\tThe SynthDef was sent instead").postln;
					this.sendSyncSynthDef; };
			});
		}
		
	loadSyncPulsesSynthDef {
		this.class.syncPulsesSynthDef.load( masterServer );
		
		}
		
	*pulseCountSynthDef {
		^SynthDef( "wfs_pulse_count", { |in = 1, startTime = 0, maxWrap = 16777216|  
				// startTime in control rate sample blocks
			/*
			MultiUnPause.ar( startTime + PulseCount.ar( AudioIn.ar( in ) )
					+ ((1000 / maxStartsPerCycle ) - 1), // reserved node ID's 0-999
				maxStartsPerCycle ); 
			*/
				
			MultiUnPause.ar( 
					Stepper.ar( AudioIn.ar( in ), 0, 
						0, (maxWrap - 1000) / maxStartsPerCycle,
						1, startTime ) + (( 1000 / maxStartsPerCycle )-1), 				
				maxStartsPerCycle ); 
					
			//Out.ar(0,0); 
			});
		}
	
	*localPulseCountSynthDef {
		^SynthDef( "wfs_pulse_count_local", { |in = 14, startTime = 0, maxWrap = 16777216| 
			var pointer, pulse;
			pulse = In.ar( in );
			//pointer = startTime.max(0) + PulseCount.ar( pulse ); 
			
			pointer = Stepper.ar( pulse, 0, 
					0, 16777216,
					1, startTime );
			
			MultiUnPause.ar( pointer.wrap( 0, (maxWrap / maxStartsPerCycle) - 
						((1000 / maxStartsPerCycle ))) 
					+ ((1000 / maxStartsPerCycle )), maxStartsPerCycle );
			SendTrig.ar( pulse, 0, pointer );
			
			//Out.ar(0,0); 
			});
		}
		
	sendPulseCountSynthDef { var def;
		def = this.class.pulseCountSynthDef;
		multiServers.do({ |ms| 
			ms.servers.do({ |server|
				def.send( server );
				}); 
			});
		}
		
	loadPulseCountSynthDefs {
		 var def;
		this.class.localPulseCountSynthDef.load( masterServer );
		def = this.class.pulseCountSynthDef;
		multiServers.do({ |ms| 
			var dir;
			dir = dir ? ms.synthDefDir;
			if( dir.pathMatch.size != 0 )
				{ def.writeDefFile(dir);
					ms.servers.do( _.listSendMsg(
						["/d_load", dir ++ def.name ++ ".scsyndef"]
					) ); }
				{ ("WFSServers-loadPulseCountSynthDef: defFile could not be written in:\n\t"++
					dir ++ "\n\tThe SynthDef was sent instead").postln;
					this.sendPulseCountSynthDef; };
			});
		}
		
	loadAllSync { 
		this.loadSyncSynthDef; 
		this.loadSyncPulsesSynthDef; 
		this.loadPulseCountSynthDefs;
		 }
		 
	startCounter { |startTime = 0|
		if( counterRunning.not )
			{
			masterServer.sendMsg( "/s_new", "wfs_pulse_count_local", pulsesNodeID + 1, 0, 1,
				"startTime", startTime, "in", pulsesOutputBus, "maxWrap", WFS.syncWrap );
			multiServers.do({ |ms| 
				ms.servers.do({ |server|
					server.sendMsg( "/s_new", "wfs_pulse_count", 
						pulsesNodeID + 1, 0, 1, "startTime", startTime, 
							"maxWrap", WFS.syncWrap );
					}); 
				});
			counterRunning = true;
			} { "WFSServers-startCounter: counter already running".postln; };
		}
		
	stopCounter { |startTime = 0|
		masterServer.sendMsg( "/n_free", pulsesNodeID + 1);
		multiServers.do({ |ms| 
			ms.servers.do({ |server|
				server.sendMsg( "/n_free", pulsesNodeID + 1 );
				}); 
			});
		counterRunning = false;
		}
		
	startPulses { |nodeID|
		if( nodeID.notNil ) { pulsesNodeID = nodeID };
		if( pulsesRunning.not )
			{ 	masterServer.sendMsg( "/s_new", "wfs_sync_pulses", pulsesNodeID,
					0, 1, "out", pulsesOutputBus );
				pulsesRunning = true; } // doesn't check!!
			{ "WFSServers-startPulses: pulses already running".postln };
		{ this.updatePulsesRunningView; }.defer;
		}
		
	stopPulses { |nodeID|
		if( nodeID.notNil ) { pulsesNodeID = nodeID };
		if( pulsesRunning )
			{ masterServer.sendMsg( "/n_free", pulsesNodeID );
			  pulsesRunning = false; }
			{ "WFSServers-stopPulses: pulses not running".postln };
		{ this.updatePulsesRunningView; }.defer;
		}

		
	updatePulsesRunningView {
		if( (window.notNil && { window.dataptr.notNil }) && { pulsesRunningView.notNil } )
			{ if( pulsesRunning )
				{ pulsesRunningView.string_( "pulses running" );  }
				{ pulsesRunningView.string_( "" );  };
			};
		}
		
	playSyncSynthDef {
		multiServers.do({ |ms| 
			ms.servers.do({ |server|
				server.sendMsg( "/s_new", "wfs_sync_get_delay", 
					1000, 0, 1, "out", syncDelayBusID );
				}); 
			});
		}
		
	getSyncBusValues { |action, silent=false|
		if( silent ) { silent = nil };
		silent !? { "\tmeasured sync delays:".postln; };
		multiServers.do({ |ms, i| 
			ms.servers.do({ |server, ii|
				Bus( \control, syncDelayBusID, 1, server )
					.get( { |val|
						syncDelays[ i ][ ii ] = val;
						silent !? { "\t\t%: %\n".postf(  server.name, val.round(1) ); };
						action.value( val, i, ii ); } );
				}); 
			});
		}
		
	getSync { |action, silent=false|
		if( silent ) { silent = nil };
		Routine({
			silent !? { "WFSServers-getSync:".postln; };
			this.startPulses;
			//this.sendSyncSynthDef;
			silent !? { "\tstarted sync pulse, measuring..".postln; };
			0.2.wait;
			this.playSyncSynthDef;
			0.5.wait;
			this.stopPulses;
			silent !? { "\tstopped sync pulse".postln; };
			this.getSyncBusValues( action, silent ? true );
			}).play;
		}	
		
	resetSync {
		multiServers.do({ |ms, i| 
			ms.servers.do({ |server, ii|
				Bus( \control, syncDelayBusID, 1, server )
					.set( 0 );
				syncDelays[i][ii] = 0;
				}); 
			});
		}	
	}
	
+ SimpleNumber {
	secondsToNodeID { |n = 0, blockSize = 128|
		var range;
		range =  WFSServers.maxStartsPerCycle
		^(((this * ( 44100 / blockSize )).floor * range) + n).wrap(-1, WFS.syncWrap - 1001)
			+ 1000; // reserved node id's 0-999
		}
	
	nodeIDToSeconds { |blockSize = 128|
		^( (this - 1000) / WFSServers.maxStartsPerCycle ).floor * ( blockSize / 44100 );
		}
		
	nodeIDTimeOffset { |blockSize = 128|
		var ratio;
		ratio = ( 44100 / blockSize );
		^(this * ratio ).frac / ratio;
		}
	
	}
	

+ WFSEvent {

	typeActivity { ^wfsSynth.typeActivity;
		/*
		var type;
		type = wfsSynth.intType;
		^( 'linear': 12, 'static': 3.5, 'cubic': 18, 'index': 0.5, 'plane': 3.5 )[ type ] ? 0
		*/
		}
	}
	
+ WFSScore {

	*stop { WFSSynth.clock.clear; 
			SystemClock.clear;
			WFSSynth.freeAllSynths;
			WFSSynth.freeAllBuffers; 
			WFSSynth.resetUsedNodeIDs;
			WFSSynth.resetLoadedSynths;
			WFSServers.default.resetActivity;
			WFSServers.default.resetDictActivity;
		}
		
	stop { this.class.stop }
		
	}
	
+ WFSSynth {

	typeActivity { var type;
		type = this.intType;
		/*
		// G5
		^( 'linear': 12, 'static': 3.5, 'cubic': 18, 'index': 0.5, 'plane': 3.5,
			'switch': 12 )[ type ] ? 0
		*/
		
		// intel
		/*
		^( 'linear': 12.5, 'static': 6, 'cubic': 19, 'index': 0.5, 'plane': 6,
			'switch': 12.5 )[ type ] ? 0
		*/
		
		// feb 2009 (AES)	
		^( 'linear': 6.6, 'static': 3.8, 'cubic': 10.4, 'index': 0.4, 'plane': 3.2,
			'switch': 6.6 )[ type ] ? 0
		}

	*resetUsedNodeIDs { usedNodeIDs = []; }
	
	*resetLoadedSynths { loadedSynths = []; }
	
		
	*nextNodeID { |startTime = 0, use = true|
		var startNodeID, nodeID, i = 0;
		var range;
		range = WFSServers.maxStartsPerCycle;
		startNodeID = startTime.secondsToNodeID;
		nodeID = startNodeID;
		while { usedNodeIDs.includes( nodeID ) }
			{ nodeID = startTime.secondsToNodeID( i = i+1 ); };
			
		if(  (nodeID != startNodeID) && { ((nodeID - startNodeID) / range).frac == 0 })
			{ ("WFSSynth-nextNodeID:" ++
				"\n\tthe maximum number of events for one control cycle (%) is reached:" ++
				"\n\tone or more events at % will be played %ms too late\n").postf(
					range,
					SMPTE( startTime ).toString,
					 ((nodeID - startNodeID) / range) * (128/44.1).round(0.1) ); };
		if ( use ) { usedNodeIDs = usedNodeIDs ++ [ nodeID ]; };
		^nodeID;
		}
		
	nextNodeID {  |startTime = 0, use = true|
		^this.class.nextNodeID( startTime, use );
		}
		
	freeSync { this.free } // same as non-sync
	releaseSync { this.release }
	freeBuffersSync { this.freeBuffers }
	
	loadBuffersSync { |servers|
			if( buffersLoaded.not )
			  {	
			  	if(wfsPath.class == WFSPath ) 
						{ wfsPath.loadBuffers2( servers ); };
					
				if( [ 'linear', 'cubic', 'switch','static','plane']
						.includes( wfsDefName.wfsIntType ) )
					{ delayBuffer = WFSPan2D.makeBuffer( servers ); };	
				//delayBuffer = WFSPan2D.makeBuffer( servers );
						
				servers.asCollection.do({ |oneServer|
										
					case { wfsDefName.wfsAudioType === 'buf' }
						{ sfBuffer = sfBuffer.asCollection.add(
							if( WFS.scVersion === \new,
							{	Buffer.readChannel( 
									oneServer, filePath, startFrame ? 0,
									numFrames: this.samplesPlayed, //only load needed
									channels: [0] // only the first channel
									)  },
							{	Buffer.read( 
									oneServer, filePath, startFrame ? 0,
									numFrames: (-1)
									)   })
							);
							if( WFSPan2D.silent )
								{ 
							if( sfBuffer.size > 1 )
									{ 
							WFS.debug( "'%' read to buffers %\n",
									filePath.basename, 
										sfBuffer.collect({ |buf|
											[oneServer.name, buf.bufnum]})
									//sfBuffer.collect(_.bufnum)
									 )
									};
								 };
						}
						{ wfsDefName.wfsAudioType === 'disk'}
						{ sfBuffer = sfBuffer.asCollection.add(						Buffer.cueSoundFile( oneServer, filePath, startFrame ? 0,
								numChannels:1,
								bufferSize: 32768 * 2
								 ) );
							if( WFSPan2D.silent )
								{ 
								if( sfBuffer.size > 1 )
									{  WFS.debug( "'%' cued in buffers %\n",
									filePath.basename, 
										sfBuffer.collect({ |buf|
											[oneServer.name, buf.bufnum]})) 
									}; };
						 	};
					});
				buffersLoaded = true;
				
			  } { "WFSSynth-loadBuffers: Buffers are probably already loaded".postln; };
		
			}
			
	loadSync { |servers, nodeID, delayOffset = 0|
			var defName;
			
			//[servers, nodeID, delayOffset].postln;
			
			if( loaded.not )
			  {
			  	this.loadBuffersSync( servers );
			  	
			  	clock.sched( WFSEvent.wait * 0.9, { 
			  	
			  		// wait with loading synth for 1/2 wait time
			  		// so all buffers have been allocated already
			  		
			  		if( useFocused.not )
			  			{ defName = 
			  		("WFS_" ++ this.intType ++ "Out_" ++ this.audioType).asSymbol;
			  			  }
			  			{ defName = wfsDefName };
			  	 	
			  	 	synth = WFSSynth.generateSynthSync( wfsDefName, wfsPath, nodeID,
							servers, delayBuffer, sfBuffer, delayOffset, 
							pbRate, level, loop, dur, input, args, fadeTimes,
							wfsPathStartIndex );
									
					loaded = true;
					loadedSynths = loadedSynths.asCollection.add( this );
					WFS.debug("loaded synths: % ( added one )",loadedSynths.size);
				});
				
			  } { "WFSSynth-load: WFSSynth is probably already loaded".postln; };
			}
			
	loadFreeSync { |servers, nodeID, delayOffset = 0, serverIndex = 0|
		this.loadSync( servers, nodeID, delayOffset );
		clock.sched( WFSEvent.wait + 0.25 + dur, 
			{ this.freeBuffers; isRunning = false; loadedSynths.remove( this );
				WFSServers.default.removeActivity( this.typeActivity, serverIndex );
				WFS.debug("loaded synths: % (removed one) / activity: %",
					loadedSynths.size,  wfsServers.activityIndex);

			
			 }
			);
		// loads synth and buffers and frees them after 1.25 + duration
		// 1.25 = default load time (1) + extra 0.1 to be sure the synth is finished
		// this depends on the sync pulse system for playback, and should be called
		// approx. 1 second before actual play time
		}
		
	*generateSynthSync { |wfsDefName, wfsPath, nodeID, servers, delayBuffer, sfBuffer, 
			delayOffset = 0, pbRate = 1, level = 1, loop = 1, dur = 5, input = 0, args,
				fadeTimes, wfsPathStartIndex = 0|
		
		var sfBufNum = 0, wfsDefIntType;
		var allArgs, localConfSizes, indexIndex, indexUse;
		
		fadeTimes = fadeTimes ? [0,0];
		
		wfsDefIntType = wfsDefName.wfsIntType;
		if( sfBuffer.notNil ) { sfBufNum = sfBuffer.asCollection.collect( _.bufnum ) };
		
		if( wfsDefIntType === 'index' )
			{ localConfSizes = ( WFSServers.default.wfsConfigurations !? 
					{ WFSServers.default.wfsConfigurations.collect( _.size ) } )
					? [ 96, 96 ]; 
			 indexIndex = wfsPath % localConfSizes;
			 localConfSizes = [0] ++ localConfSizes.integrate;
			 indexUse = localConfSizes[1..].collect({ |item, i|
			 		(( wfsPath >= localConfSizes[i]) && ( wfsPath < item )).binaryValue
			 		});
			 };
			
		
		case { wfsDefIntType == 'static' }
			{ 	allArgs = 
				[	\i_x, wfsPath.x,
					\i_y, wfsPath.y,
					\i_z, wfsPath.z,									\bufD, delayBuffer.asCollection.collect( _.bufnum ),
					\bufP, sfBufNum,
					\input, input,
					\totalTime, dur,
					\level, level,
					\loop, loop,
					\rate, pbRate,
					\outOffset, outOffset,
					\i_fadeInTime, fadeTimes.wrapAt(0),
					\i_fadeOutTime, fadeTimes.wrapAt(1) ]; }			{ wfsDefIntType == 'plane' }
			{ allArgs =
				[	\i_a, wfsPath.angle,
					\i_d, wfsPath.distance,
					\bufD, delayBuffer.asCollection.collect( _.bufnum ),
					\bufP, sfBufNum,
					\input, input,
					\totalTime, dur,
					\level, level,
					\loop, loop,
					\rate, pbRate,
					\outOffset, outOffset,
					\i_fadeInTime, fadeTimes.wrapAt(0),
					\i_fadeOutTime, fadeTimes.wrapAt(1)
					  ]; }
			{ wfsDefIntType == 'index' }
				 { allArgs =
					[	\i_index, indexIndex,
						\i_use, indexUse,
						// \bufD, delayBuffer.bufnum,
						\bufP, sfBufNum,
						\input, input,
						\totalTime, dur,
						\level, level,
						\loop, loop,
						\rate, pbRate,
						\outOffset, outOffset,
						\i_fadeInTime, fadeTimes.wrapAt(0),
						\i_fadeOutTime, fadeTimes.wrapAt(1)  ]; }
			{ true }
			{  allArgs = 
				[	\bufT , wfsPath.timesBuffer.asCollection.collect( _.bufnum ),
					\bufXYZ, wfsPath.positionsBuffer.asCollection.collect( _.bufnum ),
					\bufD, delayBuffer.asCollection.collect( _.bufnum ),
					\path_startIndex, wfsPathStartIndex,
					\bufP, sfBufNum,
					\input, input,
					\totalTime, dur,
					\rate, pbRate,
					\level, level,
					\loop, loop,
					\outOffset, outOffset,
					\i_fadeInTime, fadeTimes.wrapAt(0),
					\i_fadeOutTime, fadeTimes.wrapAt(1)  ];
			};
			
		//allArgs.dopostln;
		
		^Synth.newPausedWFS( wfsDefName, allArgs ++ args, servers, nodeID, delayOffset ); 
		}
	
	*freeAllBuffers { loadedSynths.do({ |item| item.freeBuffers }) }
	*freeAllSynths { loadedSynths.do( _.free ); }
	
	}
	
+ Synth {

	*newPausedWFS { arg defName, args, servers, nodeID, delayOffset = 0, addAction=\addToHead;
		var synths, addNum, inTargets;
		
		//wfsServers = wfsServers ? WFSServers.default;
		servers = servers.asCollection;
			
		inTargets = servers.collect( _.asTarget );
		addNum = addActions[addAction];
		nodeID = nodeID ? servers[0].nextNodeID;
		
		//"newSynth nodeID: %\n".postf( nodeID );

		synths = servers.collect({ |server| 
			this.basicNew(defName, server, nodeID); 
			});
			
		if (addNum < 2)
			{ synths.do({ |synth, i|
				synth.group = inTargets[i]; }); }
			{ synths.do({ |synth, i|
				synth.group = inTargets[i].group; }); };
		
		servers.do({ |server, i|
			server.sendBundle(nil, 
				[9, defName, nodeID, addNum, inTargets[i].nodeID, 
					\i_delayOffset, delayOffset.asCollection.wrapAt( i ) ] ++ 
						args.atArgValue( i ) ++
						WFSEQ.currentArgsDict.asArgsArray, 
				[12, nodeID, 0]); // "s_new" + "/n_run"
				});
		
		^synths;
		}
	
}

+ Collection {
	atArgValue { |index = 0|
			
		// [ \freq, [440, 330], \level, 1 ].atArgValue(1) 
		//  returns: [ \freq, 330, \level, 1 ]
		
		var out;
		out = [];
		this.pairsDo({ |a,b|
			out = out ++ [ a, b.asCollection.wrapAt( index ) ];
			});
		^out;
		}
	}