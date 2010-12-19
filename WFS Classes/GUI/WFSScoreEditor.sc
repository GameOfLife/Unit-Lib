/*
    GameOfLife WFSCollider - Wave Field Synthesis spatialization for SuperCollider.
    The Game Of Life Foundation. http://gameoflife.nl
    Copyright 2006-2010 Wouter Snoei, Miguel Negr‹o.

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
	var <>window;
	var <snapActive, <>snapH, <>numTracks;
	var id;
	var <undoStates; // not finished yet
	var <dirty = true;
	var <>wfsEventViews, <wfsMouseEventsManager;

	*initClass { UI.registerForShutdown({ WFSScoreEditor.askForSave = false }); }
	
	*new { |wfsScore, isMainEditor = true, parent |
		^super.newCopyArgs( wfsScore, isMainEditor, parent)
			.init
			.newWindow
			.makeCurrent
			.addToAll;
		}
	
	*open{
		Dialog.getPaths( { |paths|	
			var score = WFSScore.readWFSFile( paths[0] ).filePath_(paths[0]);
			WFSScoreEditor( score, true, nil);		}) 
	}
	
	init { 
		undoStates = [];
		snapActive = true;
		snapH = 0.25;
		numTracks = 16;
		this.createID;
		this.createWFSEventViews;
	}
	
	createWFSEventViews {
		wfsEventViews = score.events.collect{ |event,i|
			WFSEventView(event,i)
		};
		wfsMouseEventsManager = WFSMouseEventsManager(wfsEventViews,this);
	}
		
	setUndoState { undoStates = ( [ score.copyNew ] ++ undoStates ) }
	cleanUndoStates { undoStates = []; }
	
	getUndoState { |index = 0| undoStates[ index ];  }
			
	update { "updating".postln;
		if( window.window.notNil && { window.window.dataptr.notNil } ) { 
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
	
	addToAll { 
		all = all.asCollection.add( this ); 
		WFSTransport.refreshScoreMenu;
	}
	
	removeFromAll { if( all.notNil ) { all.remove( this ); WFSTransport.refreshScoreMenu; }; }

	selectedEvents { ^this.selectedEventViews.collect{ |eventView| eventView.event}  }
	
	selectedEventViews { 
		^wfsMouseEventsManager.selectedEvents
	}
	
	selectedEventsOrAll {
		var events = this.selectedEvents;
		if(events.size == 0)Ê{
			^score.events
		} {
			^events
		}	
	}
	
	save{
		if(score.filePath.notNil){	
			score.writeWFSFile( score.filePath ,true, false);
		}{
			this.saveAs
		}
	}
	
	saveAs{
		Dialog.savePanel( 
			{ |path| score.writeWFSFile( path ); score.filePath = path});
	} 
	
	combineAppend{
		Dialog.getPaths( { |paths|
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
	
	addAudioFiles{ 
		var copiedEvents, newTrack, newEventsSize;
		Dialog.getPaths( { |paths|
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
				this.createWFSEventViews;
				wfsEventViews.keep(newEvents.size.neg).do{ |event|
					event.selected_(true)
				};
				this.update;
				} ); 
	}
	
	addTestEvent{
		var copiedEvents, newTrack; 
		score.events = 
			score.events.add( WFSEvent( WFSTransport.pos ) );		score.cleanOverlaps;
		this.createWFSEventViews;
		this.update;
	}
	
	duplicateSelected{
		var copiedEvents, newTrack;
		var selectedEvents = this.selectedEvents;
		("		selectedEvents "++selectedEvents).postln;	
		if( selectedEvents.size > 0 ) { 
			copiedEvents = selectedEvents.collect({ |event| 
				newTrack = event.track + 1;
				if( ( newTrack + 1 ) > numTracks ) {
					newTrack =  event.track - 1 
				};
				event.duplicate.track_( newTrack );
			});
			score.events = score.events ++ copiedEvents;
			score.cleanOverlaps;
			this.createWFSEventViews;
			wfsEventViews.keep(copiedEvents.size.neg).do{ |event|
					event.selected_(true)
				};

			this.update;
			};
	}
	
	editSelected {
		
		var selectedEvent;
		selectedEvent = this.selectedEvents[0];
		selectedEvent !?	{	
			selectedEvent.edit( window.window.bounds.rightTop + (5@0), parent: this );
			if( selectedEvent.isFolder )
				{ selectedEvent.wfsSynth.edit( this ); }; };
	}
	
	deleteSelected {
		this.selectedEventViews.do({ |eventView|
			score.events.remove(eventView.event);
			wfsEventViews.remove(eventView);
		});		
		this.update;		
	}
	
	selectAll { 
		wfsEventViews.do({ |eventView| eventView.selected = true });
		this.update;
	}
	
	selectSimilar{
		var selectedTypes = WFSMouseEventsManager.selectedEvents
			.collect({ |eventView| 
				(eventView.event.wfsSynth.audioType.asString ++ "_" ++ eventView.event.wfsSynth.intType).asSymbol
			});
		wfsEventViews.do({ |eventView|
			if(selectedTypes.includes( 
				(eventView.event.wfsSynth.audioType.asString ++ "_" ++ eventView.event.wfsSynth.intType).asSymbol
			)) { eventView.selected = true }
		});
		this.update;		
	}
	
	muteSelected {
		this.selectedEvents.do( _.mute );
		this.update;
	}
	
	unmuteSelected {
		this.selectedEvents.do( _.unMute );
		this.update;	
	}
	
	unmuteAll {
		score.events.do( _.unMute );
		this.update;
	}
	
	soloSelected {
		var selectedEvents = this.selectedEvents;
		if( selectedEvents.size > 0 ) { 
			score.events.do({ |event|
				if( selectedEvents.includes( event ) ) {
					event.unMute
				} { 
					event.mute
				};
			});
			this.update;
		};
	}
	
	addTrack {
		numTracks = numTracks + 1; 
		this.update;
	}
		
	removeUnusedTracks {
		numTracks = ((score.events.collect( _.track )
			.maxItem ? 14) + 2).max( 16 );
		this.update;
	}	
	
	folderFromSelectedEvents {
		var folderEvents, folderStartTime;
		var selectedEvents = this.selectedEvents;
		if( selectedEvents.size > 0 ) {
			selectedEvents.do({ |item|
				score.events.remove( item ); 
			});
			folderStartTime = selectedEvents.sort[0].startTime;
			score.events = score.events.add( 
					WFSEvent( folderStartTime,
						WFSScore(
							*selectedEvents.collect({ |event|
								event.startTime_( 
									event.startTime - folderStartTime )
								}) ),
						selectedEvents[0].track )
					);
			this.createWFSEventViews;
			this.update;
		
		} {
			SCAlert( "Sorry, no events selected.\nUse shift to select multiple." )
		}
	}
	
	unpackSelectedFolders{
		var folderEvents;
		var selectedEvents = this.selectedEvents;
		if( selectedEvents.size > 0 and: {	 						folderEvents = selectedEvents.select( _.isFolder );
				folderEvents.size > 0  
				}
		) {
			folderEvents.do({ |folderEvent|
				score.events = score.events
					++ folderEvent.wfsSynth.events.collect({ |item|
						item.startTime_( item.startTime + folderEvent.startTime )
					});
				score.events.remove( folderEvent );
			}); 
			score.cleanOverlaps;
			this.createWFSEventViews;
			this.update;
		} { 
			SCAlert( "Sorry, no folders selected." ) 
		}	
	}
	
	cutEventsStart { |events,pos,isFolder=false,removeFadeIn = false|
			
		events.do{ |event|
			var dur = event.dur;
			var start = event.startTime;
			if((start < pos) && ((start + dur) > pos) ) {
				if(event.wfsSynth.class == WFSSynth ) {
					
					event.trimStart(pos,true);						
					if(isFolder){
						event.startTime = 0;
					};
					
				}{
					this.cutEventsStart(event.wfsSynth.events,pos-start,true,removeFadeIn);
					event.startTime = pos;
					if(isFolder){
						event.startTime = 0;
					};											}
			}
		}
	}
		
	trimEventsStartAtPos{
		this.cutEventsStart(this.selectedEventsOrAll,WFSTransport.pos);
		this.update;
	}
		
	cutEventsEnd { |events,pos,removeFadeOut = false|
		"WFSScoreEditor:cutEventsEnd".postln;
		events.do{ |event|
			
			if((event.startTime < pos) && ((event.startTime + event.dur) > pos) ) {
				
				if(event.isFolder) {
									
					this.cutEventsEnd(event.wfsSynth.events,pos - event.startTime);
				}{					
					event.trimEnd(pos,true);				
				}			
			}											}
	}	
	
	trimEventsEndAtPos{
		this.cutEventsEnd(this.selectedEventsOrAll,WFSTransport.pos);
		this.update;
	}
	
	splitEventsAtPos{
		var frontEvents, backEvents;
		frontEvents = this.selectedEventsOrAll.select({ |event|
			var dur = event.dur;
			var start = event.startTime;
			var pos = WFSTransport.pos;
 			(start < pos) && ((start + dur) > pos)
		});
		backEvents = frontEvents.collect(_.duplicate);
		score.events = score.events ++ backEvents;
		this.cutEventsStart(backEvents,WFSTransport.pos,removeFadeIn:true);
		this.cutEventsEnd(frontEvents,WFSTransport.pos,removeFadeOut:true);
		this.createWFSEventViews; 
		this.update;		

	}

	checkSoundFiles {
		var errorString = "";
		score.checkSoundFile( 
			 { |synth, sf| errorString = 
			 	errorString ++ "soundfile '" ++ 
			 		sf.path.deStandardizePath ++ "' has > 1 channels\n" },
			 { |synth, sf| errorString = 
			 	errorString ++ "soundfile '" ++
			 		sf.path.deStandardizePath ++ "' sampleRate != 44100\n" },
			 { |synth, sf| errorString = 
			 	errorString ++ "soundfile '" ++ 
			 		sf.path.deStandardizePath ++ "' could not be opened\n" }
		);
		if( errorString.size > 0 ) {
			Document( "WFSScoreEditor:check all soundfiles - Report", errorString ); 
		} {
			"\nWFSScoreEditor:check all soundfiles - no problems found".postln
		}
	}
	
	copySoundFiles {
		var copyToFolderFunc = {
			Dialog.savePanel({ |path|
				//score.copySoundFileTo( path.dirname, 
				// doneAction: { this.update } )
				var filePaths, duplicates, dupString;
				filePaths = score.collectFilePaths;
				duplicates = score.detectDupFileNames( filePaths );
				SCAlert( "There are % soundfiles used in this score".format( filePaths.size ) ++
					"\nof which % have one or more duplicate\nnames in different folders.".format( duplicates.keys.size ) ++
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
				  	{ score.copyFilesToFolder( path, doneAction: { this.update } ); } ] 
				);
			});
		};
		copyToFolderFunc.value;
	}
					
	newWindow {		
			
		numTracks = ((score.events.collect( _.track ).maxItem ? 14) + 2).max(16);
	
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
							{ Dialog.savePanel( 
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
		
		//window.window.acceptsMouseOver_( true );
		
		SCButton( window.window, Rect( 22, 2, 35, 20 ) )
			.states_( [[ \i, Color.black, Color.yellow.alpha_(0.125) ]] )
			.action_({ |b|
				this.editSelected
			});				
			
		RoundButton( window.window, Rect( 58, 2, 35, 20 ) )
			.states_( [[ \delete, Color.black, Color.red.alpha_(0.125) ]] )
			.radius_( 0 )
			.action_({ 
				this.deleteSelected				
			});
		
		RoundButton( window.window, Rect( 108, 2, 35, 20 ) )
			.states_( [[ "[", Color.black, Color.clear ]] )
			.radius_( 0 )
			.action_({ 
				this.trimEventsStartAtPos			
			});
			
		RoundButton( window.window, Rect( 144, 2, 35, 20 ) )
			.states_( [[ "]", Color.black, Color.clear ]] )
			.radius_( 0 )
			.action_({ 
				this.trimEventsEndAtPos				
			});	
			
		RoundButton( window.window, Rect( 180, 2, 35, 20 ) )
			.states_( [[ "|", Color.black, Color.clear ]] )
			.radius_( 0 )
			.action_({ 
				this.splitEventsAtPos				
			});	
		
		RoundButton( window.window, Rect( 237, 2, 20, 20 ) )
			.states_( [[ \speaker, Color.black, Color.clear ]] )
			.radius_( 0 )
			.action_({ |b|
				this.selectedEvents.do( _.toggleMute );
				this.update; 
			});
				
		RoundButton( window.window, Rect( 259, 2, 20, 20 ) )
			.states_( [[ "M", Color.black, Color.clear ]] )
			.radius_( 0 )
			.action_({ |b|
				WFSMixer(this.selectedEventsOrAll,List.new);
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
		
		window.userView
			.mouseDownAction_( { |v, x, y,mod,x2,y2| 	 // only drag when one event is selected for now
				var scaledPoint, shiftDown,altDown;
				
				scaledPoint = [ x,y ].asPoint;
				shiftDown = ModKey( mod ).shift( \only );
				altDown = ModKey( mod ).alt( \only );
				
				wfsMouseEventsManager.mouseDownEvent(scaledPoint,Point(x2,y2),shiftDown,altDown,v);

			} )				
			.mouseMoveAction_( { |v, x, y, mod, x2, y2, isInside| 
				var snap = if(snapActive){snapH * v.gridSpacingH}{0};
				wfsMouseEventsManager.mouseMoveEvent(Point(x,y),Point(x2,y2),v,snap);

			} )				
			.mouseUpAction_( { |v, x, y, mod, x2, y2, isInside|

				var shiftDown = ModKey( mod ).shift( \only );
				
				wfsMouseEventsManager.mouseUpEvent(Point(x,y),Point(x2,y2),shiftDown,v,isInside);
	
			} )			
			.keyDownAction_( { |v, a,b,c|
				var eventView;
				if( c == 127 ) {
					eventView = this.selectedEventViews[0];
					score.events.remove(eventView.event);
					wfsEventViews.remove(eventView);	
					this.update;		
				}
			})				
			.beforeDrawFunc_( {
				numTracks = ((score.events.collect( _.track ).maxItem ? ( numTracks - 2)) + 2)
					.max( numTracks );
				window.userView.fromBounds = Rect( 0, 0, score.duration.ceil.max(1), numTracks );
				window.userView.gridLines = [score.duration.ceil.max(1), numTracks];
				} )

			.unscaledDrawFunc_( { |v|
				var scPos, rect;
				rect = v.view.drawBounds.moveTo(0,0);
				"unscaledDrawFunc_".postln;
				//draw border
				GUI.pen.use({	 
					GUI.pen.addRect( rect.insetBy(0.5,0.5) );
					GUI.pen.fillColor = Color.gray(0.7).alpha_(0.5);
					GUI.pen.strokeColor = Color.gray(0.1).alpha_(0.5);
					GUI.pen.fill;
				});
				
				Pen.width = 1;
				Color.black.alpha_(0.5).set;
				Pen.strokeRect( rect.insetBy(1,1) );
				Pen.font = Font( Font.defaultSansFace, 10 );
												
				//draw events
				wfsEventViews.do({ |wfsEventView|
					wfsEventView.draw(v);
				});	
				
				//draw selection rectangle
				if(wfsMouseEventsManager.selectionRect.notNil) {
					Pen.color = Color.white;
					Pen.addRect(v.translateScale(wfsMouseEventsManager.selectionRect));
					Pen.stroke;					
					Pen.color = Color.grey(0.3).alpha_(0.4);
					Pen.addRect(v.translateScale(wfsMouseEventsManager.selectionRect));
					Pen.fill;
				};

				//draw Transport line
				Pen.width = 2;			
				Pen.color = Color.black.alpha_(0.5);
				scPos = v.translateScale( WFSTransport.pos@0 );
				Pen.line( (scPos.x)@0, (scPos.x)@v.bounds.height);
				Pen.stroke;				
						
		})
							
	}
}		