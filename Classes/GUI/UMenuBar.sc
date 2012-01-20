/*
    Unit Library
    The Game Of Life Foundation. http://gameoflife.nl
    Copyright 2006-2011 Miguel Negrao, Wouter Snoei.

    GameOfLife Unit Library: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    GameOfLife Unit Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with GameOfLife Unit Library.  If not, see <http://www.gnu.org/licenses/>.
*/



UMenuBar {

    *new { |index = 3|
		
		var wfsMenu, scoreMenu, pathMenu, helpMenu, viewMenu, defaultMenu, addEvent,
		 events, menus, sessionMenu, sessionAdd, sessionNewAdd;

		menus = ();
/* USession */
		sessionMenu = SCMenuGroup.new(nil, "Session", index);
		SCMenuItem.new(sessionMenu,  "New").action_({
			USession.new.gui
		}).setShortCut("n",true);
		
		SCMenuItem.new(sessionMenu, "Open...").action_({
			USession.read(nil, USessionGUI(_) )
		});
		
		SCMenuItem.new(sessionMenu, "Save").action_({
			USession.current !! _.save
		})
		.setShortCut("s",true);			
			
		SCMenuItem.new(sessionMenu, "Save as...").action_({
			USession.current !! _.saveAs
		})
		.setShortCut("S",true);
		SCMenuSeparator.new(sessionMenu);
/* USession - ADD OBJECTS */
        sessionAdd = SCMenuGroup.new(sessionMenu, "Add");
        sessionNewAdd = SCMenuGroup.new(sessionAdd, "New");
		SCMenuItem.new(sessionNewAdd, "UChain").action_({
			USession.current !! _.add(UChain())
		});
		SCMenuItem.new(sessionNewAdd, "UChainGroup").action_({
        	USession.current !! _.add(UChainGroup())
        });
        SCMenuItem.new(sessionNewAdd, "UScore").action_({
            USession.current !! _.add(UScore())
        });
        SCMenuItem.new(sessionNewAdd, "UScoreList").action_({
            USession.current !! _.add(UScoreList())
        });

        SCMenuItem.new(sessionAdd, "Current score").action_({
                        USession.current !! { |session|
                            UScore.current !! { |score|
                                session.add( score )
                            }
                        }
                    }).setShortCut("A",true);

        SCMenuItem.new(sessionAdd, "Current score duplicated").action_({
                USession.current !! { |session|
                    UScore.current !! { |score|
                        session.add( score.deepCopy )
                    }
                }
            }).setShortCut("A",true);


        SCMenuItem.new(sessionAdd, "Selected events").action_({
            USession.current !! { |session|
                UScoreEditorGUI.current !! { |editor|
                    editor.selectedEvents !! { |events|
                        session.add( events.collect(_.deepCopy) )
                    }
                }
            }
        }).setShortCut("A",true);

        SCMenuItem.new(sessionAdd, "Selected events flattened").action_({
            USession.current !! { |session|
                UScoreEditorGUI.current !! { |editor|
                    editor.selectedEvents !! { |events|
                        session.add( events.collect{ |x| x.deepCopy.getAllUChains }.flat )
                    }
                }
            }
        }).setShortCut("A",true);

        SCMenuItem.new(sessionAdd, "Selected events into a UChainGroup").action_({
                    USession.current !! { |session|
                        UScoreEditorGUI.current !! { |editor|
                            editor.selectedEvents !! { |events|
                                session.add( UChainGroup(* events.collect{ |x| x.deepCopy.getAllUChains }.flat ) )
                            }
                        }
                    }
                }).setShortCut("A",true);

		//events

/* EVENTS */
		events = SCMenuGroup.new(nil, "Scores", index + 1);

/* USCORE */

		SCMenuItem.new(events,  "New").action_({
			UScore.new.gui;
		});

		SCMenuItem.new(events, "Open...").action_({
			UScore.openWFS(nil, UScoreEditorGUI(_) )
		});

		SCMenuItem.new(events, "Save").action_({
			UScore.current !! _.save
		});

		SCMenuItem.new(events, "Save as...").action_({
			UScore.current !! _.saveAs
		});

		SCMenuSeparator.new(events);
		
		SCMenuItem.new(events, "Export as audio file..").action_({
			UScore.current !! { |x| 
				Dialog.savePanel({ |path|
					x.writeAudioFile( path );
				});
			};
		});
		
		SCMenuSeparator.new(events);

		SCMenuItem.new(events, "Add Event").action_({
			UScoreEditorGUI.current !! { |x| x.editor.addEvent }
		}).setShortCut("A",true);

		SCMenuItem.new(events, "Edit").action_({
			UScoreEditorGUI.current !! { |x| x.scoreView.editSelected }
		}).setShortCut("i",true);

		SCMenuItem.new(events, "Delete").action_({
			UScoreEditorGUI.current !! { |x| x.scoreView.deleteSelected }
		}).setShortCut("r",true);

		SCMenuSeparator.new(events);

	    SCMenuItem.new(events, "Copy").action_({
	        UScoreEditorGUI.currentSelectedEvents !! UScoreEditor.copy(_)
		}).setShortCut("C",true);

		SCMenuItem.new(events, "Paste").action_({
			UScoreEditorGUI.current !! { |x| x.scoreView.currentEditor.pasteAtCurrentPos }
		}).setShortCut("P",true);
		
		SCMenuSeparator.new(events);
				
		SCMenuItem.new(events, "Select All").action_({
			UScoreEditorGUI.current !! { |x| x.scoreView.selectAll }

		}).setShortCut("a",true);	
		
		SCMenuItem.new(events, "Select Similar").action_({
			UScoreEditorGUI.current !! { |x| x.scoreView.selectSimilar }
		});		
		
		//sort
		SCMenuSeparator.new(events);
		
		SCMenuItem.new(events, "Overlapping events to new tracks").action_({
			UScoreEditorGUI.current !! { |x| x.score.cleanOverlaps }
		});
		
		//mute, solo
		SCMenuSeparator.new(events);
		
		SCMenuItem.new(events, "Disable selected").action_({
			UScoreEditorGUI.current !! { |x| x.scoreView.disableSelected }
		}).setShortCut("m",true);
		
		SCMenuItem.new(events, "Enable selected").action_({
			UScoreEditorGUI.current !! { |x| x.scoreView.enableSelected }
		}).setShortCut("u",true);
		
		SCMenuItem.new(events, "Enable all").action_({
			UScoreEditorGUI.current !! { |x| x.editor.enableAll }
		});
		
		SCMenuItem.new(events, "Enable selected and disable all others").action_({
			UScoreEditorGUI.current !! { |x| x.scoreView.soloEnableSelected }
		}).setShortCut("p",true);

		//tracks
		SCMenuSeparator.new(events);
		
		SCMenuItem.new(events, "Add Track").action_({
			UScoreEditorGUI.current !! { |x| x.scoreView.addTrack }
		});
		
		SCMenuItem.new(events, "Remove Unused Tracks").action_({
			UScoreEditorGUI.current !! { |x| x.scoreView.removeUnusedTracks }
		});
		
		//view
		viewMenu = SCMenuGroup.new(nil, "View", index + 3);
		SCMenuItem.new(viewMenu, "EQ").action_( { UGlobalEQ.gui; });		SCMenuItem.new(viewMenu, "Level").action_( { UGlobalGain.gui; });
		SCMenuItem.new(viewMenu, "UDefs").action_( { UdefListView(); });
		SCMenuItem.new(viewMenu, "Meter").action_({
			ULib.servers.first.meter;
		});
	}
}

