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

+ WFSScore {
	
	trimEventsFromStart { |startTimeOffset = 0, margin = 0.1|
		// enables playback from middle of score
		// use this only on an 'asFlatScore' copy of your original score
		var partialEvents;
		
		partialEvents = events
				.select({ |evt|
					(evt.startTime < startTimeOffset) &&
						{ (evt.startTime + evt.dur) > (startTimeOffset+margin) } })
				.collect({ |evt|
					var wfsSynthCopy;
					wfsSynthCopy = evt.wfsSynth.copyNew;
					evt.wfsSynth.synth = [ wfsSynthCopy ]; // redirect for rt parameter setting
					evt.wfsSynth = wfsSynthCopy; // replace wfsSynth
					evt; // return original event
				});
			
		partialEvents.do({ |evt|
				var offset;
				offset = startTimeOffset - evt.startTime;
				evt.startTime = startTimeOffset;
				evt.wfsSynth.dur = evt.dur - offset;
				if( evt.wfsSynth.wfsPath.class == WFSPath )
					{ 
					evt.wfsSynth.wfsPathStartIndex = 
						evt.wfsSynth.wfsPath.indexAtTime2( offset );
					};
				evt.wfsSynth.fadeInTime = (evt.wfsSynth.fadeInTime - offset).max(0);
				evt.wfsSynth.startTime = evt.wfsSynth.startTime + offset;
			});
		}
	
	playFlatSync { |wfsServers, startTimeOffset = 0|
		// only for sorted and flattened scores
		// assumes that the sync  pulses will start 
		var playEvents;
		wfsServers = wfsServers ? WFSServers.default;
		
		this.trimEventsFromStart( startTimeOffset );
	
		playEvents = events
			.select( _.muted.not ) // exclude muted
			.select( _.startTime >= startTimeOffset ); // select after startTimeOffset
		
		^Task({
			var currentTime;
			currentTime = startTimeOffset;
			playEvents.do({ |event|
				var delta;
				delta = (event.startTime - currentTime);
				delta.wait;
				event.wfsSynth.playNow( wfsServers, event.startTime );
				currentTime = currentTime + delta;
				});
			}, WFSSynth.clock).start;
		
		}
	}
	
+ WFSSynth {

	playNow{  |wfsServers, startTime = 0|
		if(WFSServers.default.isSingle){
			this.playNowSyncOffline(wfsServers,startTime)
		}{
			this.playNowSync3(wfsServers,startTime)
		}
	}

	playNowSyncOffline { |wfsServers, startTime = 0|
		var nodeID, serverIndex, servers, delayOffset;
		
		#serverIndex, servers, delayOffset =  
			wfsServers.nextArray( this.typeActivity );

		/*
		if( this.useSwitch )
			{ this.copyNew.intType_( \switch ).playNowSync2( wfsServers, startTime ); };
		*/
		this.prepareForPlayback;
					 
		WFS.debug( "% - s:%, %", WFS.secsToTimeCode( startTime ),			serverIndex,
			filePath );
		
		//nodeID = this.nextNodeID(startTime, true);
		
		nodeID = servers.nextNodeID;
		
		if ( sampleAccurateTiming )
			{ delayOffset = delayOffset + startTime.nodeIDTimeOffset  };
			
		this.loadFreeSync( servers, nodeID, delayOffset, serverIndex );
		
		clock.sched( WFSEvent.wait - 0.1, // sync latency = 0.1 
			{ loadedSynths.asCollection.do({ |synth|
				synth.synth.asCollection.do({ |subsynth|
					subsynth.server.sendBundle( 0.1, 
						subsynth.runMsg( true ) );
						});
				});
			});
		
		
	}


	playNowSync3 { |wfsServers, startTime = 0|
		var nodeID, serverIndex, servers, delayOffset;
		
		if( this.intType == \switch ) { "playing switch".postln; };
		//#serverIndex, servers, delayOffset =  
		//	wfsServers.nextArray( this.typeActivity );
		
		wfsServers = wfsServers ? WFSServers.default; 
		
		if( prefServer.notNil )
			{ servers = [ wfsServers.nextDictServer(  this.typeActivity, prefServer ) ];  }
			{ servers = wfsServers.nextDictServers( this.typeActivity );  };
		
		delayOffset = servers.collect({ |srv| wfsServers.syncDelayOf( srv ) / 44100 });
	
		if( this.useSwitch && (this.intType != \switch) )
			{ this.copyNew
				.intType_( \switch ).playNowSync3( wfsServers, startTime ); };
		
		this.prepareForPlayback;
					 
		WFS.debug( "% - s:%, %", WFS.secsToTimeCode( startTime ),			serverIndex,
			filePath );
		
		nodeID = this.nextNodeID(startTime, true);
		
		if ( sampleAccurateTiming )
			{ delayOffset = delayOffset + startTime.nodeIDTimeOffset  };
			
		this.loadFreeSync3( servers, nodeID, delayOffset );
		}
		
	loadFreeSync3 { |servers, nodeID, delayOffset = 0|
	
		this.loadSync( servers, nodeID, delayOffset );
		clock.sched( WFSEvent.wait + 0.5 + dur, 
			{ this.freeBuffers; isRunning = false; loadedSynths.remove( this );
				WFSServers.default.removeDictActivity( this.typeActivity, servers );
				WFS.debug("loaded synths: % (removed one)", loadedSynths.size);
			 }
			);
		// loads synth and buffers and frees them after 1.25 + duration
		// 1.25 = default load time (1) + extra 0.1 to be sure the synth is finished
		// this depends on the sync pulse system for playback, and should be called
		// approx. 1 second before actual play time
		}
	
	}