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

WFSSyncCenter {

	classvar <remoteServers, <>localServer;
	classvar <>outBus = 14, <>inBus = 0;
	classvar <>remoteRefBlock, <>remoteBlocks, <>remoteOffsets;
	classvar <>localBlock, <>refTime;
	classvar <>history, <>localHistory;
	classvar <>nodeID = 900; // remote: nodeID, local: nodeID + 1
	classvar <>trigID = 10; // block: trigID, offset: trigID + 1
	classvar <>responders;
	classvar <>busy = false;
	
	classvar <>guiEnv;
	
	*initClass {
		history = []; localHistory = []; guiEnv = ();
		}
		
	*predictBlock { |time, server|
		var index;
		index = this.indexOfServer( server );
		if( index.notNil && { index >= 0 } )
				{ ^remoteBlocks[index] + (localBlock - remoteRefBlock) +
				  ( 	(time - refTime) * (localServer.sampleRate/localServer.options.blockSize) )
				}
				{ "%:predictBlock : server % not found".format( this, server ).warn; ^nil; }
		}
		
	*predictBlockDelta { |deltaTime, server, warn = true|
		^this.predictBlock( thisThread.seconds + deltaTime, server );
		}
		
	*predictLocalBlock { |time|
			^localBlock + ( (time - refTime) * (localServer.sampleRate/localServer.options.blockSize) );
			}
			
			
	*offsetForServer { |server|
		var index;
		index = this.indexOfServer( server );
		if( index.notNil )
			{ ^remoteOffsets[ index ]; }
			{ ^0 };
		}
			
	*startSynth1 { |latency = 0.1, defName = "default", args, target, addAction = \addToHead, nodeID|
		var synth, upSynth, block, delay, server;
		
		// Synth should contain argument "i_sync_delay" which gets a delay value in seconds.
		// To ensure sample accurate sync this delay should be applied on all outputs,
		// with a maxTime of 2 / ControlRate.ir. This replaces the regular OffsetOut
		
		defName = defName.asDefName;
		server = target.asTarget.server;
		
		synth = Synth.basicNew( defName, server, nodeID );
		upSynth = Synth.basicNew( "wfx_sync_unpauseAtBlock", server );
		block = this.predictBlockDelta( latency, server );
		delay = (this.offsetForServer( server ) +
				(block.frac * localServer.options.blockSize)) / localServer.sampleRate;
			 
		server.sendBundle(nil, 
			synth.newMsg( target, [ \i_sync_delay, delay ] ++ args, addAction ),
			synth.runMsg( false ),
			upSynth.newMsg( target, [ \block, block.floor, \nodeID, synth.nodeID ], \addToTail )
			);
		
		^synth;
		}
		
	*startSynth { |latency = 0.1, defName = "default", args, target, addAction = \addToHead, nodeID|
		// target may be an array
		var res; 
		res = (target ? localServer).asCollection.collect({ |target|
			this.startSynth1( latency, defName, args, target, addAction, nodeID );
				});
		if( res.size == 1 ) // output single if only 1 result, output array or nil otherwise
			{ ^res[0] }
			{ ^res };
		}
		

	
	*reset {  remoteBlocks = nil!remoteServers.size;
			 remoteOffsets = nil!remoteServers.size;  }
			  
	*remoteServers_ {  |newArray, reset = true|
		remoteServers = newArray;
		if( reset )
			{ remoteBlocks = nil!newArray.size;
			  remoteOffsets = nil!newArray.size;
			  this.setupResponders; };
		}
	
	*addRemoteServer { |server| // allways update
		if( remoteServers.includes( server ).not )
			{ remoteServers = remoteServers ++ [server];
			  remoteBlocks = remoteBlocks ++ [nil];
			  remoteOffsets = remoteBlocks ++ [nil];
			  this.setupResponders;
			 }
		}
	
	
	*indexOfServer { |server|
		^remoteServers.indexOf( server ) ?? { if( server == localServer ) { -1 } { nil } };
		}
	
	*setupResponders {
		if( responders.notNil )
			{  responders.do( _.remove ); };
			
		responders = remoteServers.collect({ |srv, i|
			OSCresponderNode( srv.addr, "/tr", { |time, resp, msg|						if(msg[ 1 ] == nodeID)
							{ switch( msg[2],
									trigID, { remoteBlocks[i] = msg[3]; this.updateGUI(msg); },
									trigID+1, { remoteOffsets[i] = msg[3]; this.updateGUI(msg); } );   
							};
						}).add;
				});		
		}
	
	*getBlocks {
	
		Task({
			var latency = 0.05, remoteSynths = [];
			busy = true;
			history = history ++ [[ remoteRefBlock, remoteBlocks.copy, remoteOffsets.copy ]];
			localHistory = localHistory ++ [[  refTime, localBlock ]];
			
			 remoteBlocks = nil!remoteServers.size;
			 remoteOffsets = nil!remoteServers.size;  
			 remoteRefBlock = nil;
			
			// start remote listening synths
			remoteServers.do({ |srv, i|
				remoteSynths = remoteSynths.add( 
					Synth.basicNew( "wfx_sync_getBlockRemote", srv, nodeID ) );
				srv.sendMsg( *remoteSynths[i].newMsg( srv, args: [ \in, inBus, \id, trigID ] ) );
				});
				
			0.2.wait; /* <- they should be running now;
				 checking could hold up the whole sequence if one server fails.
				 Maybe change this to a register-check routine with max wait time later;
				 that could shorten the whole routine. */
				 
			// wait for rounded next block moment
			(((thisThread.seconds + latency)
					.roundUp( localServer.options.blockSize/localServer.sampleRate ) -
				thisThread.seconds) - latency).wait;
		
			// send local sync message
			localServer.sendBundle( latency,  // bundled message to ensure local sync
				[ "/s_new", "wfx_sync_getBlockLocal", nodeID + 1, 1,  // addtoTail
						localServer.asTarget.nodeID,
						"out", outBus, "pulseLevel", 0.1, "id", trigID ] );
			
			OSCresponderNode( localServer.addr, "/tr", { |time, resp, msg|						if( (msg[ 1 ] == (nodeID+1)) && { msg[2] == trigID } )
							{ localBlock = msg[3]; remoteRefBlock = msg[3]; resp.remove; this.updateGUI(msg); };
						}).add;
			
			refTime = thisThread.seconds + latency;
			busy = false;
			
			}).start;
	
		}
	
	*getLocalBlock {
		Task({	var latency = 0.05, remoteSynths = [];
			localHistory = localHistory ++ [[  refTime, localBlock ]];
				 
			// wait for rounded next block moment
			(((thisThread.seconds + latency)
					.roundUp( localServer.options.blockSize/localServer.sampleRate ) -
				thisThread.seconds) - latency).wait;
		
			// send local sync message
			localServer.sendBundle( latency,  // bundled message to ensure local sync
				[ "/s_new", "wfx_sync_getBlockLocal", nodeID + 1, 1,  // addtoTail
						localServer.asTarget.nodeID,
						"out", outBus, "pulseLevel", 0.0, "id", trigID ] );
			
			OSCresponderNode( localServer.addr, "/tr", { |time, resp, msg|						if( (msg[ 1 ] == (nodeID+1)) && { msg[2] == trigID } )
							{ localBlock = msg[3]; resp.remove; this.updateGUI(msg); };
						}).add;
			
			refTime = thisThread.seconds + latency;
			}).start;
		}
		
		
	*gui {
		guiEnv.use({
			var lastAddr;
			~font = GUI.font.new( GUI.font.defaultMonoFace, 9 );
			~window = GUI.window.new( "WFSSyncCenter", Rect(355, 12, 250, 515) ).front;
			~window.view.decorator = FlowLayout( ~window.view.bounds );
			
			~getButton = RoundButton( ~window, 100@16 )
					.states_( [[ "get offsets" ]] )
					.font_( ~font )
					.action_({ this.getBlocks; });
					
			~getLocalButton = RoundButton( ~window, 120@16 )
					.states_( [[ "get local offset" ]] )
					.font_( ~font )
					.action_({ this.getLocalBlock; });
					
			~window.view.decorator.nextLine;
			
			GUI.staticText.new( ~window, 70@20 );
			
			~window.view.decorator.nextLine;
			
			GUI.staticText.new( ~window, 70@20 )
				.string_( localServer.name )
				.background_( Color.blue(0.25).alpha_(0.25) )
				.font_( ~font ).align_( \center );
			
			~localBlock = GUI.staticText.new( ~window, 70@20 )
				.font_( ~font )
				.string_( localBlock.asString );
			
			~refTime = GUI.staticText.new( ~window, 80@20 )
				.font_( ~font )
				.string_( refTime !? { refTime.asTimeString } );
			
			//~window.view.decorator.nextLine;
			//LineView( ~window, 1@1 );
			//~window.view.decorator.nextLine;
			
			~remoteBlocks = nil!remoteBlocks.size;
			~remoteOffsets = nil!remoteOffsets.size;
			
			remoteServers.do({ |server, i|
				if( server.addr.addr != lastAddr )
					{ ~window.view.decorator.nextLine;
					LineView( ~window, 1@1 );
					~window.view.decorator.nextLine;
					GUI.staticText.new( ~window, 100@15 )
						.string_( server.addr.addr.asIPString )
						.font_( ~font );
					lastAddr = server.addr.addr;
					};

				~window.view.decorator.nextLine;
				
				GUI.staticText.new( ~window, 70@20 )
					.string_( server.name )
					.background_( Color.green(0.25).alpha_(0.25) )
					.font_( ~font ).align_( \center );
					
				~remoteBlocks[i] = GUI.staticText.new( ~window, 70@20 )
					.font_( ~font );
				if(  remoteBlocks[i].notNil && { remoteRefBlock.notNil } )
					{ ~remoteBlocks[i].string =  remoteBlocks[i] - remoteRefBlock }; 
					//.string_( remoteBlocks[i] - remoteRefBlock );
					
				GUI.staticText.new( ~window, 10@20 );
				
				~remoteOffsets[i] = GUI.staticText.new( ~window, 30@20 )
					.font_( ~font )
					.string_( remoteOffsets[i].asString )
					.background_( Color.white.alpha_(0.25) )
					.align_( \center );
				});
			});
		}
			
	*updateGUI { |post| // post.postln;  
		{ 
		if( guiEnv[ \window ].notNil && {  guiEnv[ \window ].isClosed.not } )
			{
			//remoteBlocks.postln;
			guiEnv.use({
				remoteBlocks.do({ |item,i|
					if( remoteRefBlock.notNil && item.notNil )
						{ ~remoteBlocks[i].string = item - remoteRefBlock;
						 try { if( (item - remoteRefBlock) != 
						 	(history.last[1][i] - history.last[0]) )
						  { ~remoteBlocks[i].background = Color.red; }
						  {  ~remoteBlocks[i].background =  Color.clear; };
						   	};
						  };
					 remoteOffsets[i] !? {~remoteOffsets[i].string = remoteOffsets[i]; };
					});
				
				localBlock !? { ~localBlock.string = localBlock.asString; };
				~refTime.string =  refTime !? { refTime.asTimeString };
				});
			};
		 }.defer; }
		 
	*sendSynthDefs {
		this.synthDefs.do({ |def|
			def.send( localServer );
			remoteServers.do({ |server|
				def.send( server );
				});
			});
		}
	
	*synthDefs { ^[
		SynthDef( "wfx_sync_getBlockLocal", { |out = 14, pulseLevel = 0.1, id = 10|
				var pulse, krPulse, block;
				
				// create pulse for sending to the remote servers
				pulse = Impulse.ar(0);
				Out.ar( out, pulse  * pulseLevel);
				
				// measure internal block
				krPulse = Impulse.kr(0);
				block = AbsoluteBlock.kr;
				SendTrig.kr( krPulse, id, block );
				
				// free immediately
				FreeSelf.kr( krPulse );
				}),
				
		SynthDef( "wfx_sync_getBlockRemote", { |in = 0, id = 10|
				var pulse, krPulse, block, offset;
				
		
				// DEBUG
				pulse = Trig1.ar( AudioIn.ar( in+1, 1 ) - 0.000001 );
				
				//pulse = TDelay.ar( Trig1.ar(Impulse.ar(0)), 0.26.round(1/ControlRate.ir)  );
				// end DEBUG
				
				krPulse = T2K.kr( pulse );
				
				// get and send block and offset
				block = Latch.kr( AbsoluteBlock.kr, krPulse );
				offset = Latch.ar( BlockOffset.ar, pulse );
				SendTrig.kr( krPulse, id, block );
				SendTrig.ar( pulse, id + 1, offset );
				
				// free after reception
				FreeSelf.kr( krPulse );
				}),
				
		SynthDef( "wfx_sync_unpauseAtBlock", { |block = 0, nodeID = 2000|
				var trig, late;
				
				// late if block > wanted block
				late = Trig1.kr( AbsoluteBlock.kr > block-1 );
				Poll.kr( Delay1.kr(late), nodeID, "late" );
				
				// unpause a node at a specific block
				trig = SetResetFF.kr( InRange.kr( AbsoluteBlock.kr, block-1, block-1 ) );
				Pause.kr( Delay1.kr( trig + late ) , nodeID );
				
				// free after unpausing
				FreeSelf.kr(  Delay1.kr( (trig + late ) - 0.001 ) );	
				})
		]
		}
	}