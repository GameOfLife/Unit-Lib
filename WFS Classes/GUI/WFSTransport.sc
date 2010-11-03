WFSTransport {
	classvar <window, <scoreMenu;
	classvar <>additionalResponders;
	classvar <>nowPlaying;
	classvar <>autoReturnToZero = false;
	classvar <>autoSwitchToNext = false;
	
	classvar <>endAction; // NEW ADDITION
	
	classvar <>videoAddr;
	classvar <>videoUpdateRate = 30;
	
	*new {  ^super.new.init; }
	
	*initClass { videoAddr = NetAddr( "192.168.2.13", 7004 ); }
	
	*refreshScoreMenu {
		if( window.notNil && { window.isClosed.not } ) 
			{ scoreMenu.items_( [ "current: " ++ 
					(WFSScoreEditor.current ? ( id: "none" )).id.asString] 
				++ WFSScoreEditor.all.select({ |item| item.id.notNil })
					.collect({ |item| item.id.asString }); )
			};

		}
	
	*pos { if( window.notNil && { window.isClosed.not } )
				{ ^window.pos } { ^0 }
			}
			
	*pos_ { |newPos| if( window.notNil && { window.isClosed.not } )
				{ window.pos = newPos } { this.new; this.pos = newPos };
			if( (  WFS.graphicsMode === \fast ) && { WFSScoreEditor.current.notNil } ) 
					{ WFSScoreEditor.current.update; }; 
			}
			
	*play {  if( window.notNil && { window.isClosed.not } )
				{ window.play };
		}
		
	*stop {  if( window.notNil && { window.isClosed.not } )
				{ window.stop };
		}
	
	*return {  if( window.notNil && { window.isClosed.not } )
				{ window.return };
		}
	
	*toFront {   if( window.notNil && { window.isClosed.not } )
		{ window.window.front };
		}
	
	init { 
		var loading, resp;
		var loadViews;
		if( window.notNil && { window.isClosed.not } ) { window.close };
		window = TransportWindow( "WFSTransport" );
		
		loadViews = ( 
			wait: WaitView( window.window, Rect(
				window.window.bounds.width - 35, 0, 30, 30 ) )
					//.speed_( 1.025 / WFSEvent.wait )
					.alphaWhenStopped_( 0 ),
			text: SCStaticText( window.window,  Rect(
				window.window.bounds.width - 130, 0, 120, 40 ) )
					.string_( "" ) 
					.font_( Font( "Monaco", 9 ) )
					.stringColor_( Color.red )
				);

		
		window.counterAction_( {  if( WFSScoreEditor.current.notNil ) 
									{ WFSScoreEditor.current.update; }; } );
		
		window.returnAction_( {  
				this.class.pos = 0;
				/* if( WFSScoreEditor.current.notNil ) 
						{ WFSScoreEditor.current.update; }; */
				 } );

		window.playAction = { |tw|
			var flatScore; // 29/09/08
			WFSTransport.nowPlaying = WFSScoreEditor.current;
			flatScore = WFSTransport.nowPlaying.score.asFlatScore( excludeMuted: true );
			tw.playRoutine = Routine({ 
				var updateSpeed;
				//{ tw.name = "WFSTransport : loading :::::::::"; }.defer;
				{ loadViews[ \text ].string_( "loading....." )
					.stringColor_( Color.red(0.75) );
					loadViews[ \wait ].start; }.defer;
				//loadViews[ \wait ].start;
				WFSServers.default.startCounter( (tw.pos * 44100) / 128 );
				
				videoAddr.sendMsg( "/video", "arm" );
				
				
				if( WFS.graphicsMode === \fast )
					{  updateSpeed = 10; }
					{  updateSpeed = 34; }; 
				
				resp = OSCresponder(WFSServers.default.m.addr,'/tr',{ arg time,responder,msg;
					
					if( msg[1] == (WFSServers.pulsesNodeID + 1) )
						{ if( (msg[3].floor % updateSpeed) == 0 ) 
							{ tw.pos = (msg[3] * 128) / 44100; 
								{ tw.update; }.defer; 
								
							if( WFSTransport.nowPlaying.notNil )
								{ if( window.pos > 
									WFSTransport.nowPlaying.score.duration.ceil )
										{ window.stop;
										if( autoSwitchToNext ) {
											
											{ WFSScoreEditor.makeNextCurrent;
											/*
											{ WFSScoreEditor.current.toFront;
											WFSTransport.toFront; }.defer; 
											*/ 
											}.defer;
											};

										if( autoReturnToZero ) { { window.return }.defer; };
										
										endAction.value; // 11/10/07
																				} {
									
							if( WFS.graphicsMode === \fast )
									{	{ WFSTransport.nowPlaying.update; }.defer; };
								}; };
						};
						
						
						if( (msg[3].floor % videoUpdateRate ) == 0 ) 
							{ videoAddr.sendMsg( "/video",  
									(msg[3] * 128) / 44100 ); 
					 WFSExternalSyncCenter.sendMsgs( (msg[3] * 128) / 44100, videoAddr );
							};		
								
						} 
						{ additionalResponders.value( time,responder,msg ) };
							 
					}).add;
					
				// clicktrack support added 29/10/08 ws :
				WFSClickTrack.prepare( WFSTransport.nowPlaying.score.clickTrackPath, tw.pos );				// end clicktrack support
				
				 WFSExternalSyncCenter.start( flatScore );
				
				5.do({ |i|
					(WFSEvent.wait / 5 ).wait;
					{ loadViews[ \text ].string_( "loading".extend(11-i, $. ) ) }.defer;
					});
					
				WFSClickTrack.start; // clicktrack support added 29/10/08 ws
				WFSServers.default.startPulses;
				
				videoAddr.sendMsg( "/video", "start" );
			
				{ loadViews[ \text ].string_( "playing" )
					.stringColor_( Color.green(0.5) );
					loadViews[ \wait ].stop; 
				}.defer;
				
				//{ tw.name = "WFSTransport : running  " }.defer;
			}).play;
			if( WFSTransport.nowPlaying.notNil )
				{  //WFSTransport.nowPlaying.score.playSync2( nil, tw.pos );
					flatScore.playFlatSync( nil, tw.pos ); //29/09/2008
					 };
		};
		
		RoundButton( window.window, Rect( window.window.bounds.width - 100, 62, 95, 16 ) )
			.states_( [ 	["show position", Color.black, Color.gray(0.7)], 
						["show position", Color.gray(0.7), Color.black] ] )
			.value_( if( WFS.graphicsMode == \fast ) { 1 } { 0 } )
			.action_( { |bt| 
				if( bt.value == 1 )
					{ WFS.graphicsMode = \fast;
						//this.class.pos_( this.class.pos ); 
						}
					{ WFS.graphicsMode = \slow };
				} )
			.canFocus_( false );
					
		RoundButton( window.window, Rect( window.window.bounds.width - 160, 62, 55, 16 ) )
			.states_( [ 	["prepare", Color.red(0.25), Color.red(0.75).alpha_(0.5) ] ] )
			.action_( { |bt| 
				WFSScore.current.writePathData;
				"WFSTransport:prepare
				All path data is now saved to a file and uploaded to both servers.
				do this once every time a new score is loaded or the score is changed".postln;
				} )
			.canFocus_( false );
			
		
		scoreMenu = SCPopUpMenu( window.window, 
				Rect( window.window.bounds.width - 100, 80, 95, 16 ) )
			.canFocus_( false )
			.items_( [ "current: " ++ 
					(WFSScoreEditor.current ? ( id: "none" )).id.asString] 
				++ WFSScoreEditor.all.select({ |item| item.id.notNil })
					.collect({ |item| item.id.asString }); )
			.font_( Font( "Monaco", 9 ) )
			.action_({ |popUp|	
				var selectedID;
				if( popUp.value != 0 )
					{ 	
					selectedID = popUp.items[ popUp.value ].interpret;
					popUp.value = 0;
					WFSScoreEditor.all.select({ |score| score.id == selectedID })
						[0].toFront.makeCurrent;
					window.window.front; };
				});
			
		SCStaticText( window.window, Rect( window.window.bounds.width - 140, 80, 40, 16 ) )
			.string_( "score:" )
			.font_( Font( "Monaco", 9 ) ); 
		
		window.stopAction = { |tw|
			OSCresponder.remove( resp );
			tw.playRoutine.stop;
			WFSScore.stop;
			WFSServers.default.stopPulses;
			WFSServers.default.stopCounter;
			
			WFSClickTrack.stop;
			WFSExternalSyncCenter.stop;
			WFSTransport.videoAddr.sendMsg( "/video", "stop" );
			//{ tw.name = "WFSTransport" }.defer;
			{ loadViews[ \text ].string_( "" ); loadViews[ \wait ].stop; }.defer;
			
			
			
			};
		}
	}
	


