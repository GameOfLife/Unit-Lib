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

WFSScoreEditor {
	
	classvar <>current, <all;
	classvar <>askForSave = true;
	
	var <score, <isMainEditor, <parent;
	var <>window, selectedRects;
	var <snapActive, <>snapH, <>numTracks;
	var id;
	var <undoStates; // not finished yet
	
	*initClass { UI.registerForShutdown({ WFSScoreEditor.askForSave = false }); }
	
	*new { |wfsScore, isMainEditor = true, parent |
		^super.newCopyArgs( wfsScore, isMainEditor, parent)
			.init
			.newWindow
			.makeCurrent
			.addToAll;
		}
	
	init { 
		undoStates = [];
		snapActive = true;
		snapH = 0.25;
		numTracks = 16;
		this.createID;
		}
		
	setUndoState { undoStates = ( [ score.copyNew ] ++ undoStates ) }
	cleanUndoStates { undoStates = []; }
	
	getUndoState { |index = 0| undoStates[ index ];  }
	
		
	update { if( window.window.notNil && { window.window.dataptr.notNil } ) { 
			window.refresh; };
			if( parent.notNil ) { parent.update };
			 }
			 
	toFront { if( window.window.notNil && { window.window.dataptr.notNil } ) 
		{ window.window.front; };
		 }
			
	makeCurrent { if( isMainEditor ) 
		{ current = this; WFSTransport.refreshScoreMenu; }
		{ if( parent.notNil ) { current = parent; WFSTransport.refreshScoreMenu;  } }
		}
	
	createID { 
		if( isMainEditor )
			{ id = ((all.collect({ |item| item.id ? -1 }) ? []) ++ [-1]).maxItem + 1; }
		 }
	
	id { if( isMainEditor ) { ^id } { ^nil }  } // change later?
	
	addToAll { all = all.asCollection.add( this ); 
				WFSTransport.refreshScoreMenu;
				}
	removeFromAll { if( all.notNil ) { all.remove( this ); WFSTransport.refreshScoreMenu; }; }

	selectedEvents { ^score.events[ selectedRects ];  }
	
	newWindow {		
		
		var rects, events;
		
		var n, moveFlag, moveOrigin, movingRects; // moving
		
		var createRects, eventsFromRects, getNames, getObjects; // functions
		var possibleSelectedRects, selectionPoint, selectionStartPoint, selectionFunc, updateTransport = false, initialMouseDownSelection;
		var selectedPoint, position, minimumMov = 3, moveOriginAbs;
		var getTypeColors;
		var createSelectedRects;
		
		var addEventMenu, fileMenu;
	
		
		//n = numTracks; // number of tracks
		
		numTracks = ((score.events.collect( _.track ).maxItem ? 14) + 2).max(16);
		
		
		
		selectionPoint = 0@0;
		position = 0;
			
		createRects = { score.events.collect({ |event, i|
				Rect( event.startTime, event.track, event.dur, 1 )
			 	}); 
			 };
		
		createSelectedRects = { score.events[ selectedRects ].collect({ |event, i|
				Rect( event.startTime, event.track, event.dur, 1 )
			 	}); 
			 };
		
		eventsFromRects = { |rects| //, names, objects
			score.events.do({ |event, i|
				event.startTime = rects[i].left;
				event.track = rects[i].top;
				});
			};
			
		getNames = { score.events.collect({ |item, i|
			var audioType, outString;
			audioType = item.wfsSynth.audioType;
			outString = (case { audioType == \folder }
				{ i.asString ++ ": folder "++ item.wfsSynth.name++" (" ++ item.wfsSynth.events.size ++ " events)"  }
				{ [\buf, \disk].includes( audioType ) }
				{ i.asString ++ ": " ++ item.wfsSynth.filePath.basename; }
				{ audioType === \blip }
				{ i.asString ++ ": testevent (blip)"  }
				{ true }
				{ i.asString } );
			if( item.muted ) 
				{ outString ++ " (muted)" }
				{ outString };
			});
		};

			
		//getObjects = { score.events.collect({ |item, i| "xxx"; }); };
		
		getTypeColors = { score.events.collect({ |item, i|
				var color;
				color = if( item.isFolder )
					{ Color.white;  }
					{ 	( ( 	'buf': Color.blue, 
							'disk': Color.magenta,
							'blip': Color.red  )
							[ item.wfsSynth.audioType ] ? Color.gray ).blend(
						 ( ( 	'linear': Color.cyan(0.5), 
								'cubic': Color.cyan(0.75),
								'static': Color.green(0.5),
								'plane': Color.yellow(0.5) )
							[ item.wfsSynth.intType ] ? Color.gray ), 0.5 )
					};
				/*	
				if( item.muted )
					{ color.blend( Color.red(0.25), 0.5 ) }
					{ color }; */
				color;
				 }); 
			};

		window = ScaledUserView.window( "WFSScoreEditor (" ++ 
				(id ?? { "folder of " ++ ( parent !? { parent.id } ) } ) ++ ")", 
			Rect(230 + 20.rand2, 230 + 20.rand2, 680, 300),
			fromBounds: Rect( 0, 0, score.duration.ceil.max(1), numTracks ),
			viewOffset: [4, 25] );
			
		window.userView.background = Color.gray(0.8);
			 
		if( isMainEditor.not ) { window.window.view.background_( Color.gray(0.33).alpha_(0.5 ) ) };
			
		window.userView.view.canFocus_( false );
		
		window.drawHook = { this.makeCurrent };
		
		window.onClose = { 
			
			this.removeFromAll;
			
			{ 0.1.wait;  
			if( askForSave && isMainEditor ) 
				{ if( score.events.size != 0 )
					{ SCAlert( "Do you want to save your score? (" ++ id ++ ")" , 
						[ [ "Don't save" ], [ "Cancel" ], [ "Save" ] ], 
						[ 	nil, 
							{ WFSScoreEditor( score ); },  
							{ CocoaDialog.savePanel( 
								{ |path| score.writeWFSFile( path ); },
								{ WFSScoreEditor( score ); }); } 
							] ); 
						};
					}
			}.fork( AppClock ); // prevent crash at shutdown
			
			 };
		
		window.userView.gridLines = [score.duration.ceil,max(1), numTracks];
		window.userView.gridMode = ['blocks','lines'];
		window.maxZoom = [16,5];
		
		moveFlag = false;
		moveOrigin = 0@0;
		movingRects = nil;
		
		selectedRects = [ ];
		possibleSelectedRects = [ ];
			
		//window.window.acceptsMouseOver_( true );
		
		fileMenu = SCPopUpMenu( window.window, Rect( 4, 2, 50, 20 ) )
			.items_( [ "(file", /*)*/ "open score..", "save score..", 
					"-", "combine or append scores..", "-", "add audio file.." ] )
			.action_( { |v| 
				case { v.value == 2 } // save
					{ CocoaDialog.savePanel( { |path|
							score.writeWFSFile( path ); } ); }
					{ v.value == 1 } // open
					{ CocoaDialog.getPaths( { |paths|
							score = WFSScore.readWFSFile( paths[0] ) ? score;
							this.update;
							} ); }
					{ v.value == 4 } // combine multiple with current
					{ CocoaDialog.getPaths( { |paths|
						var nextFunc;
						nextFunc = { |index = 0|
							var path, newScore;
							path = paths[index];
							if( path.notNil )
								{ newScore = WFSScore.readWFSFile( path );
								  if( newScore.notNil )
								  	{ if( score.events.size == 0 )
								  		{ score = WFSScore.readWFSFile( paths[0] ) ? score;
											this.update;  nextFunc.value(index+1); }
										{ SCAlert( "where do you want to place '%'?"
											.format( path.basename ),
										[ "cancel", "skip",
											"as folder", 
											"at start", "current pos", "at end"],
										[ { }, { nextFunc.value(index+1) },
											{ score.events = score.events.add(
												   WFSEvent( 0, newScore ) );
											score.cleanOverlaps;
										   	this.update; 
										   	nextFunc.value(index+1);  },
										   { score.events = 
										   		score.events ++ newScore.events;
										   	score.cleanOverlaps;
										   	this.update; 
										   	nextFunc.value(index+1); },
										   { newScore.events.do({ |event|
												event.startTime = 
												   WFSTransport.pos + event.startTime;
												});
											score.events = score.events ++ newScore.events;
											score.cleanOverlaps;
											this.update;
											nextFunc.value(index+1);
											},
											{ newScore.events.do({ |event|
												event.startTime = 
												   event.startTime + score.duration;
												});
											score.events = score.events ++ newScore.events;
											score.cleanOverlaps;
											this.update;
											nextFunc.value(index+1);
											} ]	);
										};
									};
								};
							};
						nextFunc.value; });
					}
					{ v.value == 6 }
					{ addEventMenu.valueAction_( 1 ); };
				v.value = 0 } );
				
		if( isMainEditor.not ) {
			fileMenu.items =  [ "(file", /*)*/ "(open score..", /*)*/
					"save as separate score..", 
					"-", "combine or append scores..", "-", "add audio file.." ] 
			};
		
		SCStaticText( window.window, Rect( 62, 2, 56, 20 ) ).string_( "event" ).align_( \right );
		SCButton( window.window, Rect( 122, 2, 35, 20 ) )
			.states_( [[ "edit", Color.black, Color.yellow.alpha_(0.125) ]] )
			.action_({ |b| 
				var selectedEvent;
				selectedEvent = score.events[ selectedRects[0] ? 0 ];
				selectedEvent !?	{	
					selectedEvent.edit( window.window.bounds.rightTop + (5@0), parent: this );
					if( selectedEvent.isFolder )
						{ selectedEvent.wfsSynth.edit( this ); }; };				});
				
		/*
		SCButton( window.window, Rect( 162, 2, 60, 20 ) )
			.states_( [[ "duplicate", Color.black, Color.green.alpha_(0.125) ]] )
			.action_({ |b|
				var copiedEvents, newTrack;
				if( selectedRects.size > 0 )
					{ 
					copiedEvents = 
						score.events[ selectedRects ]
							.collect({ |event| 
								newTrack = event.track + 1;
								if( ( newTrack + 1 ) > numTracks )
									{ newTrack =  event.track - 1 };
								event.duplicate.track_( newTrack );
								});
					score.events = score.events ++ copiedEvents;
					score.cleanOverlaps;
					this.update;
					}
				});
		*/
			
		RoundButton( window.window, Rect( 158, 2, 35, 20 ) )
			.states_( [[ \delete, Color.black, Color.red.alpha_(0.125) ]] )
			.radius_( 0 )
			.action_({ |b|
				var copiedEvents;
				if( selectedRects.size > 0 )
					{ 
					selectedRects.do({ |item| score.events.removeAt( item ); });
					this.update;
					}
				});
				
		addEventMenu = SCPopUpMenu( window.window, Rect( 195, 2, 40, 20 ) )
			.items_( [ "(add", /*)*/ "audiofile..", "test event", "duplicate selected" ] )
			.action_( { |v| 
				var copiedEvents, newTrack;
				case { v.value == 1 }
					{ CocoaDialog.getPaths( { |paths|
							var newEvents;
							newEvents = paths.collect({ |path, i|
								var newEvent;
								newEvent = WFSEvent( 
									WFSTransport.pos, 
									WFSSynth( 
										'static_buf', 
										WFSPoint(0,0,0),
										Server.default,
										path ), i );
								newEvent.wfsSynth.useSoundFileDur;
								newEvent; });
							score.events = score.events ++ newEvents;
							score.cleanOverlaps;
							this.update;
							} ); }
					{ v.value == 2 }
					{ score.events = 
						score.events.add( WFSEvent( WFSTransport.pos ) );						score.cleanOverlaps;
							this.update;
					 }
					 { v.value == 3 }
					 { if( selectedRects.size > 0 )
						{ 
						copiedEvents = 
							score.events[ selectedRects ]
								.collect({ |event| 
									newTrack = event.track + 1;
									if( ( newTrack + 1 ) > numTracks )
										{ newTrack =  event.track - 1 };
									event.duplicate.track_( newTrack );
									});
						score.events = score.events ++ copiedEvents;
						score.cleanOverlaps;
						this.update;
						};
					};
				v.value = 0 } );
				
		RoundButton( window.window, Rect( 237, 2, 20, 20 ) )
			.states_( [[ \speaker, Color.black, Color.clear ]] )
			.radius_( 0 )
			.action_({ |b|
				var copiedEvents;
				if( selectedRects.size > 0 )
					{ 
					 score.events[ selectedRects ? [] ].do( _.toggleMute );
					this.update; 
					}
				});
				
		RoundButton( window.window, Rect( 259, 2, 20, 20 ) )
			.states_( [[ "M", Color.black, Color.clear ]] )
			.radius_( 0 )
			.action_({ |b|
				var eventsToMix = if(this.selectedEvents.size != 0)
				{
						this.selectedEvents
				}{
						score.events
				};
				WFSMixer(eventsToMix,List.new);
			});

		SCButton( window.window, Rect( 280, 2, 55, 20 ) )
			.states_( [[ "plot", Color.black, Color.clear ]] )
			.action_({ WFSMixedArray.with( 
				*( score.events.collect({ |event|
						if( event.isFolder.not )
							{ event.wfsSynth.wfsPath
								.currentTime_( event.startTime.neg +
									 WFSTransport.pos )
								} { nil };
						}).select( _.notNil ) ) ).plotSmooth; 
				});		
				
		SCButton( window.window, Rect( 340, 2, 55, 20 ) )
			.states_( [[ "plot all", Color.black, Color.clear ]] )
			.action_({ WFSMixedArray.with( 
				*( score.allEvents.collect({ |event|
						
							 event.wfsSynth.wfsPath
								.currentTime_( event.startTime.neg +
									 WFSTransport.pos )
								
						}).select( _.notNil ) ) ).plotSmooth; 
				});
		
		SCStaticText( window.window, Rect( 400, 2, 50, 20 ) ).string_( "snap" ).align_( \right );
		SCPopUpMenu( window.window, Rect( 454, 2, 50, 20 ) )
			.items_( [ "off", "0.001", "0.01", "0.1", "0.25", "0.333", "1" ] )
			.value_(4)
			.action_({ |v|
				if (v.value == 0)
					{ snapActive = false; }
					{ snapActive = true; };
					
				snapH = [0, 0.001, 0.01, 0.1, 0.25, 1/3, 1][ v.value ];
				});
				
		SCStaticText( window.window, Rect( 508, 2, 20, 20 ) ).string_( "s" );
		
		SCPopUpMenu( window.window, Rect( 605, 2, 72, 20 ) )
			.items_( [ "(options", /*)*/
				"-", "sort events", "overlapping events to new tracks",
				"-", "add track", "remove unused tracks",
				"-", "folder from selected events", "unpack selected folders",
				"-", "select all", "select similar",
				"-", "mute selected", "solo selected", "unmute selected", "unmute all",
				"-", "check all soundfiles", "copy all soundfiles to folder..",
				"-", "trim start of events at playhead", "trim end of events at playhead",
				"-", "batch tweak events"] )
			.resize_( 3 )
			//.background_( Color.gray(0.7) )
			.action_( { |popUp| 
				var folderEvents, folderStartTime = 0;
				var selectedTypes, errorString, copyToFolderFunc;
				case { popUp.value == 2  } // sort events
					{ score.sort; this.update; }
					{ popUp.value == 3 } // sort events
					{ score.cleanOverlaps; this.update; }
					{ popUp.value == 5  } // add track
					{ numTracks = numTracks + 1; this.update; }
					{ popUp.value == 6  } // remove unused tracks
					{ numTracks = 
						((score.events.collect( _.track )
							.maxItem ? 14) + 2).max( 16 );
						this.update;
					}
					{ popUp.value == 8 }
					{  if( selectedRects.size > 0 )
						{  folderEvents = score.events[ selectedRects ];
							folderEvents.do({ |item|
								score.events.remove( item ); });
						 folderStartTime = 
						 	folderEvents.sort[0].startTime;
						 score.events = score.events.add( 
								WFSEvent( folderStartTime,
									WFSScore(
										*folderEvents.collect({ |event|
											event.startTime_( 
												event.startTime - folderStartTime )
											}) ),
										folderEvents[0].track )
									);
						selectedRects = [];
							this.update;
						 } { 
				SCAlert( "Sorry, no events selected.\nUse shift to select multiple." ) }
						   }
					{ popUp.value == 9 }
					{ if( selectedRects.size > 0 && {	 						folderEvents = 
								score.events[ selectedRects ].select( _.isFolder );
							folderEvents.size > 0  } )
						{ folderEvents.do({ |folderEvent|
							score.events = score.events ++ 
								folderEvent.wfsSynth.events.collect({ |item|
									item.startTime_( item.startTime + folderEvent.startTime )
									});
							score.events.remove( folderEvent );
							}); 
						selectedRects = [];
						score.cleanOverlaps;
						this.update;
						} { SCAlert( "Sorry, no folders selected." ) };
					}
					{ popUp.value == 11 } // select all
					{ selectedRects = score.events.collect({ |item, i| i });
						this.update; }
					{ popUp.value == 12 } // select similar
					{ selectedTypes = score.events[ selectedRects ]
						.collect({ |event| 
							(event.wfsSynth.audioType.asString ++ "_" ++
								event.wfsSynth.intType).asSymbol });
					
					selectedRects = score.events.detectAll({ |event|
						selectedTypes.includes( (event.wfsSynth.audioType.asString ++ "_" ++
								event.wfsSynth.intType).asSymbol ); });
						this.update; }
					{ popUp.value == 14 } // mute
					{ score.events[ selectedRects ? [] ].do( _.mute );
						this.update; } 
					{ popUp.value == 15 } // solo
					{ if( selectedRects.size > 0 )
							{ score.events.do({ |event, i|
									if( selectedRects.includes( i ) )
										{ event.unMute } 
										{ event.mute   };
									});
								this.update; };
					} 
					{ popUp.value == 16 } // unmute sel
					{ score.events[ selectedRects ? [] ].do( _.unMute );
						this.update; }   
					{ popUp.value == 17 } // unmute all
					{ score.events.do( _.unMute );
						this.update; } 
					{ popUp.value == 19  } // check soundfile
					{ errorString = "";
					score.checkSoundFile( 
						 { |synth, sf| errorString = 
						 	errorString ++ "soundfile '" ++ 
						 		sf.path.deStandardizePath ++ "' has > 1 channels\n" },
						 { |synth, sf| errorString = 
						 	errorString ++ "soundfile '" ++
						 		sf.path.deStandardizePath ++ "' sampleRate != 44100\n" },
						 { |synth, sf| errorString = 
						 	errorString ++ "soundfile '" ++ 
						 		sf.path.deStandardizePath ++ "' could not be opened\n" } );
						 if( errorString.size > 0 )
						 	{ Document( "WFSScoreEditor:check all soundfiles - Report",
						 		 errorString ); }
						 	{ "\nWFSScoreEditor:check all soundfiles - no problems found"
						 		.postln };
					}
					{ popUp.value == 20  } // copy soundfiles;
					{ copyToFolderFunc = {CocoaDialog.savePanel({ |path|
						//score.copySoundFileTo( path.dirname, 
						// doneAction: { this.update } )
						var filePaths, duplicates, dupString;
						filePaths = score.collectFilePaths;
						duplicates = score.detectDupFileNames( filePaths );
						SCAlert( "There are % soundfiles used in this score"
								.format( filePaths.size ) ++
				"\nof which % have one or more duplicate\nnames in different folders."
								.format( duplicates.keys.size ) ++
				"\n\nDo you really want to copy to\n'%/'?".format( path ),
						[ "cancel", "inspect", "change folder", "ok" ],
						[ { },
						  { dupString = "WFSScore duplicate file report:\n";
						  	duplicates.sortedKeysValuesDo({ |key, value|
						  		dupString = dupString ++ key ++ ":\n\t";
						  		dupString = dupString ++ 
						  			filePaths.detect({ |item| 
						  				item.asString.basename.asSymbol == key }) ++ "\n";
						  		value.do({ |item| dupString = 
						  			dupString ++ "\t" ++ item ++ "\n" });
						  		});
						   Document.new.string_( dupString );
						  },
						  copyToFolderFunc,
						  { score.copyFilesToFolder( path, doneAction: { this.update } ); } ] );
						}); };
					copyToFolderFunc.value;
						
					}
					{ popUp.value == 22  } // trim events start
					{
						var cutFunction = { |events,pos,isFolder=false|
							
							events.do{ |event|
								var dur = event.dur;
								var start = event.startTime;
								if((start < pos) && ((start + dur) > pos)){
									if(event.wfsSynth.class == WFSSynth){
										event.dur = event.dur - (pos - start);
										event.wfsSynth.startFrame = 44100 * (pos - start);
										
										if(isFolder){
											event.startTime = 0;
										}{
											event.startTime = pos;
										}		
									}{
										cutFunction.(event.wfsSynth.events,pos-start,true);
										if(isFolder){
											event.startTime = 0;
										}{
											event.startTime = pos;
										}											}
								}
							}
						};
						var score = this.score;
						cutFunction.(score.events,WFSTransport.pos);
						this.update;
						
					}
					{ popUp.value == 23  } // trim events end
					{
						var cutFunction = { |events,pos|
							events.do{ |event|
								var dur, newdur, start = event.startTime;
								dur = event.dur;
								if((start < pos) && ((start + dur) > pos)){
									if(event.wfsSynth.class == WFSSynth){
										newdur = pos - event.startTime;
										event.dur = newdur;
									}{
										cutFunction.(event.wfsSynth.events,pos-start);
									}			
								}											}
						};
						cutFunction.(this.score.events,WFSTransport.pos);
						this.update;
						
					}
					{ popUp.value == 25  }
					{ WFSBatch.new };
						
						  
				popUp.value = 0; } );
		
					
		selectionFunc = { |x,y,shiftDown,isInside|
					
			if( isInside ){
				if(shiftDown){
					selectedRects = (selectedRects ++ rects.detectAll{ |rect,i| 
						rect.intersects( Rect.fromPoints(moveOrigin,[ x,y ].asPoint)) 
					}).as(Set).as(Array);			
				}{
					selectedRects = rects.detectAll{ |rect,i| 
						rect.intersects( Rect.fromPoints(moveOrigin,[ x,y ].asPoint)) 
					}
				};
			};
		};				
		
		window.userView
			.mouseDownAction_( { |v, x, y,mod,x2,y2| 	 // only drag when one event is selected for now
				var scaledPoint, rects, selectedIndexes, shiftDown;
				scaledPoint = [ x,y ].asPoint;
				shiftDown = ModKey( mod ).shift( \only );
				
				rects = createRects.value;
				selectedIndexes = rects.detectAll({ |rect|
						rect.containsPoint( scaledPoint  );
				});
				
				if(selectedIndexes.size == 0) {
					//starting drag select
					if(shiftDown.not){
						selectedRects = [];
						updateTransport = true;
					};					
					moveFlag = false;
				} {
					//starting move or shift select rects
					if(shiftDown){
						selectedIndexes.do{ |index|
							if(selectedRects.includes(index)){
								selectedRects.remove(index)
							} {
								selectedRects = selectedRects.add( index )
							};
							updateTransport = false;
						};						
					}{
						initialMouseDownSelection = selectedIndexes;
					};
					moveFlag = true; 
				};
				moveOrigin = scaledPoint;
				moveOriginAbs = [x2,y2].asPoint;
				
			} )				
			.mouseMoveAction_( { |v, x, y, mod, x2, y2, isInside| 
				var rects, shiftDown;
				
				if( (moveOriginAbs.x-x2).abs > minimumMov) {
					rects = createRects.value;
					shiftDown = ModKey( mod ).shift( \only );
					
					if( moveFlag ) {
					//moving	
						if( isInside ) {     
							movingRects = selectedRects.collect{ |i|
								var movingRect = rects[i].translate(  
									Point( x - moveOrigin.x, (y - moveOrigin.y).round( v.gridSpacingV))
								).max( 0 );
								if( snapActive ) { 
									movingRect.left_( 
										movingRect.left.round( snapH * v.gridSpacingH)
									) 
								};
								if( shiftDown ) {// change track only 
									// -- only works when shift was
									// pressed before mousedown
									movingRect.left_( 
										rects[ selectedRects[i] ].left 
									); 
								};
								(\rect: movingRect, \index: i);
							};
						} {
							movingRects = selectedRects.collect{ |i| (\rect: rects[i], \index:i) };
						};
					} {
					//drag selecting
						selectionFunc.(x,y,shiftDown,isInside);
						updateTransport = false;
					};
				}
			} )				
			.mouseUpAction_( { |v, x, y, mod, x2, y2, isInside|
				var sr;
				var rects, names, objects, currentObject, currentName;
				var copiedEvents, selectionRectangle;
				rects = createRects.value;
				
				if( moveFlag && { movingRects.notNil } ) { 
				//moving
					if(  isInside ) {
						if( ModKey( mod ).alt( \only ) ) {
						
							copiedEvents = selectedRects.collect{ |i| score.events[i].duplicate };
							score.events = score.events ++ copiedEvents;
							rects = rects ++ movingRects.collect(_.at(\rect));
							score.events = eventsFromRects.value( rects );
							selectedRects = ((rects.size-selectedRects.size) .. (rects.size -1));
							//firstSelectionIndex = selectedRects[0];
							if( WFSEventEditor.current.notNil && { selectedRects[0].notNil } ) {
								score.events[ selectedRects[0] ]
									.edit( parent: this );
								window.window.front 
							}														
						} { 
							movingRects.do{ |movRect| rects[movRect[\index]] = movRect[\rect] };
							score.events = eventsFromRects.value( rects ); 
						};						
						
					};
					movingRects = nil; 
					moveFlag = false; 
				} { 
					if(initialMouseDownSelection.notNil){
						selectedRects = initialMouseDownSelection;
						initialMouseDownSelection = nil;
					}{
						if(updateTransport){
							WFSTransport.pos_(moveOrigin.x);
						};	
					};
					if( WFSEventEditor.current.notNil && { selectedRects[0].notNil } ) {
						score.events[ selectedRects[0] ]
								.edit( parent: this );
							window.window.front 
					};
				};
				if( WFSEventEditor.current.notNil ){ 
					WFSEventEditor.current.update 
				};
				updateTransport = false;
						
			} )
				
			
			.keyDownAction_( { |v, a,b,c|
				if( c == 127 )
					{ if( selectedRects.size > 0 ) 
						{	rects.removeAt(  selectedRects[0] );
							score.events.removeAt( selectedRects[0] );
							selectedRects.removeAt(0);
						}; 		
					window.refresh; 
					};
				} )
			
				
			.beforeDrawFunc_( {
				numTracks = ((score.events.collect( _.track ).maxItem ? ( numTracks - 2)) + 2)
					.max( numTracks );
				window.userView.fromBounds = Rect( 0, 0, score.duration.ceil.max(1), numTracks );
				window.userView.gridLines = [score.duration.ceil.max(1), numTracks];
				} )
			
			.drawFunc_( {
					Color.yellow.alpha_(0.2).set;
					possibleSelectedRects.do({ |item| Pen.fillRect( item ) });
					if( movingRects.notNil )
						{	Color.red.set;
							movingRects.do{ |rect|
								Pen.fillRect( rect[\rect] )
							}
						};
				} )
						
			.unscaledDrawFunc_( { |v|
				var names, scPos, objects, colors, muted;
				var rect;
				
				
				//if( GUI.pen.respondsTo( \fillAxialGradient ) ) // too fancy
				if( false )
					{ rect = v.view.drawBounds.moveTo(0,0);
					  GUI.pen.use({ //GUI.pen.color = Color.gray(0.9);
					  GUI.pen.addRect( rect );
					  GUI.pen.fillAxialGradient( rect.leftTop, rect.leftBottom, 					  	Color.gray(1).alpha_(0.25),
					  	Color.gray(0.1).alpha_( 0.25 ) ); 
					 });
					 }
					 {rect = v.view.drawBounds.moveTo(0,0);
					  GUI.pen.use({	 //GUI.pen.color = Color.gray(0.9);
						  GUI.pen.addRect( rect.insetBy(0.5,0.5) );
						  GUI.pen.fillColor = Color.gray(0.7).alpha_(0.5);
						  GUI.pen.strokeColor = Color.gray(0.1).alpha_(0.5);
						  GUI.pen.fill;
						  });
					 };
					 
				Pen.width = 1;
				Color.black.alpha_(0.5).set;
				Pen.strokeRect( v.view.drawBounds.moveTo(0,0).insetBy(1,1) );
				
			
				rects = createRects.value;
				names = getNames.value;
				colors = getTypeColors.value;
				muted = score.events.collect( _.muted );
				
				scPos = v.translateScale( WFSTransport.pos@0 );
				
				Pen.font = Font( Font.defaultSansFace, 10 );
				
				v.translateScale( rects ).do({ |item, i| 
					var lineAlpha, selected, textrect;
					
					if(rect.intersects( item ))
						{	
						lineAlpha =  if( muted[i] ) { 0.5  } { 1.0  };
						
						if( selected = selectedRects.includes( i ) )
							{ Pen.width = 3;
							  Pen.color = Color.red;
							  Pen.strokeRect( item );
							 };
							   
						/*
							{ Pen.width = 1;
								Color.black.alpha_( lineAlpha ).set; };
						*/
						
						
						Pen.color = colors[i].alpha_(
							if( selected ) { 1.0 * lineAlpha } { 0.66 * lineAlpha }
							); 
							
						Pen.fillRect( item.insetBy(0.5,0.5) );
						
						
						Pen.color = Color.black.alpha_( lineAlpha );
						
						if( item.height > 4 )
							{
							textrect = item.sect( rect.insetBy(-3,0) );
							Pen.use({		
								Pen.addRect( textrect ).clip;
								Pen.stringLeftJustIn( 
									" " ++ names[i], 
									textrect );
								});
							}
						}
					});

				/*	
				v.translateScale( { |i| 0@i } ! n ).do({ |point, i|
					i.asString.drawAtPoint( 0@(point.y), Font( "Monaco", 9 ) )
					});
				*/
				
				Pen.width = 2;
			
				Pen.color = Color.black.alpha_(0.5);
				Pen.line( (scPos.x)@0, (scPos.x)@v.bounds.height);
				Pen.stroke;
				
				/*
				v.translateScale( { |i| 
					Rect.fromPoints(0@i, 0@i+1) } ! numTracks ).do({ |rect, i|
					if( ( rect.top >= -10 ) && { rect.top < (v.bounds.height - 10)  } )
						{ i.asString
							//.reverse.extend( 2, $ ).reverse
							.drawRightJustInM9( rect.left_( -14).width_(13),
								Color.black.alpha_(0.75) ) };
					});
				*/
				
				} )
				
			/*
			.unclippedUnscaledDrawFunc_( { |v|
				
				/*
				Pen.width = 2;
				Color.black.alpha_(0.5).set;
				Pen.moveTo( (-1)@(-1) );
				Pen.lineTo( v.bounds.width@(-1) );
				Pen.moveTo( 0@0 );
				Pen.lineTo( 0@v.bounds.height );
				Pen.stroke;
				
				Color.white.alpha_(0.5).set;
				Pen.moveTo( (0)@(v.bounds.height - 1) );
				Pen.lineTo( (v.bounds.width - 1)@(v.bounds.height -1));
				Pen.lineTo( (v.bounds.width - 1)@(0) );
				Pen.stroke;
				*/
				
			
				} );
			*/
				
		}
	
	}