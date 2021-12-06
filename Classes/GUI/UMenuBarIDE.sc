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



UMenuBarIDE {

	classvar <>currentMenuName;
	classvar <>sessionMenu;
	classvar <>skipJack, <>windowsMenu;

	*fillWindowsMenu {
		if( windowsMenu.notNil ) {
			windowsMenu.clear;

			UScoreEditorGUI.all.do({ |gui|
				windowsMenu.addAction(
					MenuAction( gui.windowTitle, { gui.toFront } )
				)
			});

			UChainGUI.all.do({ |gui|
				windowsMenu.addAction(
					MenuAction( gui.window.name, { gui.window !? _.front } )
				)
			});
		};
	}

	*new { |name = "Unit Lib"|

		currentMenuName = name;

/* MAIN */

		MainMenu.register( MenuAction( "New Score", {
				UScore.new.gui;
			}), name );

		MainMenu.register( MenuAction( "Open Score...", {
			UScore.openMultiple(nil, UScoreEditorGUI(_) )
		}), name );

		MainMenu.register( MenuAction( "Save Score", {
			UScore.current !! _.save
		}), name );

		MainMenu.register( MenuAction( "Save Score as...", {
			UScore.current !! _.saveAs
		}), name );

		MainMenu.register( MenuAction.separator("Export"), name );

		MainMenu.register( MenuAction( "Export as audio file..", {
			UScore.current !! { |x|
				Dialog.savePanel({ |path|
					x.writeAudioFile( path );
				});
			};
		}), name );

/* SESSION */

		MainMenu.register( MenuAction.separator("Sessions"), name );

		sessionMenu = Menu(

			MenuAction( "New Session", {
				USession.new.gui
			}),

			MenuAction( "Open Session...", {
				USession.read(nil, USessionGUI(_) )
			}),

			MenuAction( "Save Session", {
				USession.current !! _.save
			}),

			MenuAction( "Save Session as...",{
				USession.current !! _.saveAs
			}),

			MenuAction.separator,

			Menu(
				MenuAction( "Current Score", {
					USession.current !! { |session|
						UScore.current !! { |score|
							session.add( score )
						}
					}
				}),
				MenuAction( "Current Score Duplicated", {
					USession.current !! { |session|
						UScore.current !! { |score|
							session.add( score.deepCopy )
						}
					}
				}),
				Menu(
					MenuAction( "All", {
						USession.current !! { |session|
							UScoreEditorGUI.current !! { |editor|
								editor.selectedEvents !! { |events|
									session.add( events.collect(_.deepCopy) )
								}
							}
						}
					}),
					MenuAction( "All UChains", {
						USession.current !! { |session|
							UScoreEditorGUI.current !! { |editor|
								editor.selectedEvents !! { |events|
									session.add( events.collect{ |x| x.deepCopy.getAllUChains }.flat )
								}
							}
						}
					}),
					MenuAction( "All UChains as UChainGroup", {
						USession.current !! { |session|
							UScoreEditorGUI.current !! { |editor|
								editor.selectedEvents !! { |events|
									session.add(
										UChainGroup( *events.collect{ |x| x.deepCopy.getAllUChains }.flat )
									)
								}
							}
						}
					}),
					MenuAction( "All UChains in new UScore", {
						USession.current !! { |session|
							UScoreEditorGUI.current !! { |editor|
								editor.selectedEvents !! { |events|
									session.add(
										 UScore( *events.collect{ |x| x.deepCopy.getAllUChains }.flat )
									)
								}
							}
						}
					}),
				).title_( "Selected Events" )
			).title_("Add"),

			Menu(
				MenuAction( "UChain", {
					USession.current !! _.add(UChain())
				}),
				MenuAction( "UChainGroup", {
					USession.current !! _.add(UChainGroup())
				}),
				MenuAction( "UScore", {
					USession.current !! _.add(UScore())
				}),
				MenuAction( "UScoreList", {
					USession.current !! _.add(UScoreList())
				}),
			).title_("New"),
		).title_("Session");

		MainMenu.register( sessionMenu, name );

/* EDIT */
		MainMenu.register( MenuAction( "Copy", {
	        UScoreEditorGUI.currentSelectedEvents !! UScoreEditor.copy(_)
		}), "Edit" );

		MainMenu.register( MenuAction( "Paste", {
			UScoreEditorGUI.current !! { |x| x.scoreView.currentEditor.pasteAtCurrentPos }
		}), "Edit" );

		MainMenu.register( MenuAction.separator("Events"), "Edit" );

		MainMenu.register( MenuAction( "Add Event", {
			UScoreEditorGUI.current !! { |x| x.editor.addEvent }
		}), "Edit" );

		MainMenu.register( MenuAction( "Add Marker", {
			UScoreEditorGUI.current !! { |x| x.editor.addMarker }
		}), "Edit" );

		MainMenu.register( MenuAction( "Edit Selected", {
			UScoreEditorGUI.current !! { |x| x.scoreView.editSelected }
		}), "Edit" );

		MainMenu.register( MenuAction( "Delete Selected", {
			UScoreEditorGUI.current !! { |x| x.scoreView.deleteSelected }
		}), "Edit" );

		MainMenu.register( MenuAction.separator("Select"), "Edit" );

		MainMenu.register( MenuAction( "Select All", {
			UScoreEditorGUI.current !! { |x| x.scoreView.selectAll }

		}), "Edit" );

		MainMenu.register( MenuAction( "Select Similar", {
			UScoreEditorGUI.current !! { |x| x.scoreView.selectSimilar }
		}), "Edit" );

		//sort
		MainMenu.register( MenuAction.separator("Sort"), "Edit" );

		MainMenu.register( MenuAction( "Sort Events", {
			UScoreEditorGUI.current !! { |x|
				UScore.current.events.sort;
				UScore.current.changed( \numEventsChanged );
				UScore.current.changed( \something );
			};
		}), "Edit" );

		MainMenu.register( MenuAction( "Overlapping events to new tracks", {
			UScoreEditorGUI.current !! { |x| x.score.cleanOverlaps }
		}), "Edit" );

		MainMenu.register( MenuAction( "Remove empty tracks", {
			UScoreEditorGUI.current !! { |x| x.score.removeEmptyTracks }
		}), "Edit" );

		//mute, solo
		MainMenu.register( MenuAction.separator("Disable/Enable"), "Edit" );

		MainMenu.register( MenuAction( "Disable selected", {
			UScoreEditorGUI.current !! { |x| x.scoreView.disableSelected }
		}), "Edit" );

		MainMenu.register( MenuAction( "Enable selected", {
			UScoreEditorGUI.current !! { |x| x.scoreView.enableSelected }
		}), "Edit" );

		MainMenu.register( MenuAction( "Enable all", {
			UScoreEditorGUI.current !! { |x| x.editor.enableAll }
		}), "Edit" );

		MainMenu.register( MenuAction( "Enable selected and disable all others", {
			UScoreEditorGUI.current !! { |x| x.scoreView.soloEnableSelected }
		}), "Edit" );

		//tracks
		MainMenu.register( MenuAction.separator("Tracks"), "Edit" );

		MainMenu.register( MenuAction( "Add Track", {
			UScoreEditorGUI.current !! { |x| x.scoreView.addTrack }
		}), "Edit" );

		MainMenu.register( MenuAction( "Remove Unused Tracks", {
			UScoreEditorGUI.current !! { |x| x.scoreView.removeUnusedTracks }
		}), "Edit" );

	/* VIEW */

		MainMenu.register( MenuAction( "EQ", { UGlobalEQ.gui; }), "View");
		MainMenu.register( MenuAction( "Level", { UGlobalGain.gui; }), "View");
		MainMenu.register( MenuAction( "Udefs", { UdefsGUI(); }), "View");
		MainMenu.register( MenuAction( "Level meters", {
			ULib.servers.first.meter;
		}), "View");

		windowsMenu = Menu().title_( "Windows" );

		MainMenu.register( windowsMenu, "View" );

		this.fillWindowsMenu;

		if( skipJack.notNil ) { skipJack.stop };

		skipJack = SkipJack( { this.fillWindowsMenu }, 1 );
	}

	*add { |name, function, menuName|
		if( function === \separator ) {
			MainMenu.register( MenuAction.separator( name ), menuName ? currentMenuName ? "Unit Lib" );
		} {
			MainMenu.register( MenuAction( name, function ), menuName ? currentMenuName ? "Unit Lib" );
		};
	}
}

