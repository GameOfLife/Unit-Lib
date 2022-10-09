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
	classvar <>openRecentMenu;
	classvar <>mode = \mainmenu, <>toolBar; // or \toolbar

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

	*registerMenu { |menuAction, name|
		var mn;
		if( mode === \mainmenu ) {
			MainMenu.register( menuAction, name.asString, 'unitlib' );
		} {
			if( toolBar.isNil ) {
				toolBar = ToolBar(
					Menu(
						menuAction
					).title_( name.asString )
				).minWidth_(300).front;
			} {
				mn = toolBar.actions.detect({ |item| item.string == name.asString });
				if( mn.isNil ) {
					toolBar.addAction( Menu(
						menuAction
					).title_( name.asString )
					);
				} {
					mn.menu.addAction( menuAction );
				};
			};
		};
	}

	*clear {
		var menus;
		if( mode === \mainmenu ) {
			MainMenu.registered.do({ |item|
				item.value.detect({ |acc| acc.key == 'unitlib' })
				.value.do({ |menu| menus =menus.add( menu ) })
			});
			menus.do({ |item| MainMenu.unregister( item ) });
		};
	}

	*new { |name = "Unit Lib"|

		this.clear; // clear the old menu first

		currentMenuName = name;

/* MAIN */

		this.registerMenu( MenuAction( "New Score", {
				UScore.new.gui;
			}), name );

		this.registerMenu( MenuAction( "Open Score...", {
			UScore.openMultiple(nil, UScoreEditorGUI(_) )
		}), name );

		openRecentMenu = Menu().title_("Open Recent");

		this.registerMenu( openRecentMenu, name );

		URecentScorePaths.menu = openRecentMenu;

		URecentScorePaths.fillMenu;

		this.registerMenu( MenuAction( "Save Score", {
			{ UScore.current !! _.save }.defer(0.01);
		}), name );

		this.registerMenu( MenuAction( "Save Score as...", {
			{ UScore.current !! _.saveAs }.defer(0.01);
		}), name );

		this.registerMenu( MenuAction.separator("Export"), name );

		this.registerMenu( MenuAction( "Export as audio file..", {
			UScore.current !! { |x|
				ULib.savePanel({ |path|
					x.writeAudioFile( path );
				});
			};
		}), name );

/* SESSION */

		this.registerMenu( MenuAction.separator("Sessions"), name );

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

		this.registerMenu( sessionMenu, name );

/* EDIT */
		this.registerMenu( MenuAction( "Copy", {
	        UScoreEditorGUI.currentSelectedEvents !! UScoreEditor.copy(_)
		}), "Edit" );

		this.registerMenu( MenuAction( "Paste", {
			UScoreEditorGUI.current !! { |x| x.scoreView.currentEditor.pasteAtCurrentPos }
		}), "Edit" );

		this.registerMenu( MenuAction.separator("Events"), "Edit" );

		this.registerMenu( MenuAction( "Add Event", {
			UScoreEditorGUI.current !! { |x| x.editor.addEvent }
		}), "Edit" );

		this.registerMenu( MenuAction( "Add Marker", {
			UScoreEditorGUI.current !! { |x| x.editor.addMarker }
		}), "Edit" );

		this.registerMenu( MenuAction( "Edit Selected", {
			UScoreEditorGUI.current !! { |x| x.scoreView.editSelected }
		}), "Edit" );

		this.registerMenu( MenuAction( "Delete Selected", {
			UScoreEditorGUI.current !! { |x| x.scoreView.deleteSelected }
		}), "Edit" );

		this.registerMenu( MenuAction.separator("Select"), "Edit" );

		this.registerMenu( MenuAction( "Select All", {
			UScoreEditorGUI.current !! { |x| x.scoreView.selectAll }

		}), "Edit" );

		this.registerMenu( MenuAction( "Select Similar", {
			UScoreEditorGUI.current !! { |x| x.scoreView.selectSimilar }
		}), "Edit" );

		//sort
		this.registerMenu( MenuAction.separator("Sort"), "Edit" );

		this.registerMenu( MenuAction( "Sort Events", {
			UScoreEditorGUI.current !! { |x|
				UScore.current.events.sort;
				UScore.current.changed( \numEventsChanged );
				UScore.current.changed( \something );
			};
		}), "Edit" );

		this.registerMenu( MenuAction( "Overlapping events to new tracks", {
			UScoreEditorGUI.current !! { |x| x.score.cleanOverlaps }
		}), "Edit" );

		this.registerMenu( MenuAction( "Remove empty tracks", {
			UScoreEditorGUI.current !! { |x| x.score.removeEmptyTracks }
		}), "Edit" );

		//mute, solo
		this.registerMenu( MenuAction.separator("Disable/Enable"), "Edit" );

		this.registerMenu( MenuAction( "Disable selected", {
			UScoreEditorGUI.current !! { |x| x.scoreView.disableSelected }
		}), "Edit" );

		this.registerMenu( MenuAction( "Enable selected", {
			UScoreEditorGUI.current !! { |x| x.scoreView.enableSelected }
		}), "Edit" );

		this.registerMenu( MenuAction( "Enable all", {
			UScoreEditorGUI.current !! { |x| x.editor.enableAll }
		}), "Edit" );

		this.registerMenu( MenuAction( "Enable selected and disable all others", {
			UScoreEditorGUI.current !! { |x| x.scoreView.soloEnableSelected }
		}), "Edit" );

		//tracks
		this.registerMenu( MenuAction.separator("Tracks"), "Edit" );

		this.registerMenu( MenuAction( "Add Track", {
			UScoreEditorGUI.current !! { |x| x.scoreView.addTrack }
		}), "Edit" );

		this.registerMenu( MenuAction( "Remove Unused Tracks", {
			UScoreEditorGUI.current !! { |x| x.scoreView.removeUnusedTracks }
		}), "Edit" );

	/* VIEW */

		this.registerMenu( MenuAction( "EQ", { UGlobalEQ.gui; }), "View");
		this.registerMenu( MenuAction( "Level", { UGlobalGain.gui; }), "View");
		this.registerMenu( MenuAction( "Udefs", { UdefsGUI(); }), "View");
		this.registerMenu( MenuAction( "Level meters", {
			ULib.servers.first.meter;
		}), "View");

		windowsMenu = Menu().title_( "Windows" );

		this.registerMenu( windowsMenu, "View" );

		this.fillWindowsMenu;

		if( skipJack.notNil ) { skipJack.stop };

		skipJack = SkipJack( { UMenuBarIDE.fillWindowsMenu }, 1 );
	}

	*add { |name, function, menuName|
		if( function === \separator ) {
			this.registerMenu( MenuAction.separator( name ), menuName ? currentMenuName ? "Unit Lib" );
		} {
			this.registerMenu( MenuAction( name, function ), menuName ? currentMenuName ? "Unit Lib" );
		};
	}
}

