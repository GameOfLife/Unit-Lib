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
	classvar <>skipJack, <>windowsMenu, <>windowsCtrl;
	classvar <>openRecentMenu;
	classvar <>preferencesFunc;
	classvar <>mode = \mainmenu, <>toolBar; // or \toolbar
	classvar >allMenus;
	classvar <>font;
	classvar <>menuStripMode = \views; // or \toolbar

	*initClass {
		if ( thisProcess.platform.name !== 'osx' ) {
			mode = \none;
			menuStripMode = \toolbar;
		};
	}

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
		var menuCreated = false;
		allMenus = allMenus ?? {()};
		if( allMenus[ name.asSymbol ].isNil ) { menuCreated = true; };
		allMenus[ name.asSymbol ] = allMenus[ name.asSymbol ].add( menuAction );
		switch( mode, \mainmenu, {
			MainMenu.register( menuAction, name.asString, 'unitlib' );
		}, \toolbar, {
			if( toolBar.isNil ) {
				toolBar = ToolBar().minWidth_(300).font_( font ).front;
			};
			if( menuCreated ) {
				toolBar.addAction( Menu( menuAction ).title_( name.asString ) );
			} {
				toolBar.actions.detect({ |item| item.string == name.asString }) !? { |x| x.menu.addAction( menuAction ) }
			};
		});
	}

	*hasMenus { ^allMenus.notNil }

	*allMenus {
		^allMenus.collect({ |item, key|
			var mn;
			mn = Menu()
			.title_( key.asString )
			.font_( font );
			item.do({ |action| mn.addAction( action ) });
			mn;
		});
	}

	*createToolbar {
		var menus;
		if( allMenus.notNil ) {
			menus = this.allMenus;
			^ToolBar( *[ currentMenuName.asSymbol, \File, \Edit, \View ].collect( menus[_] ) ).font_( font )
		};
	}

	*createMenuViews { |parent|
		var menus, headers;
		menus = this.allMenus.atAll( [ currentMenuName.asSymbol, \File, \Edit, \View ] );
		headers = menus.collect({ |menu, i|
			var header, ctrl, name;
			var makeTask, stopTask, task;
			name = menu.title;
			header = StaticText( parent, ( name.bounds( font ).width + 16)@( parent.bounds.height ) );
			header.align_( \center ).string_( name ).font_( font );

			makeTask = {
				//"create task % %\n".postf( name );
				task.stop;
				task = {
					var pos, res;
					loop {
						pos = QtGUI.cursorPosition;
						res = headers.detect({ |item|
							item.absoluteBounds.containsPoint( pos );
						});
						//[ name, res, pos ].postln;
						if( res.notNil && { header != res }) {
							res.mouseDown;
							task.stop;
						};
						0.05.wait;
					};
				}.fork( AppClock );
			};

			stopTask = {
				//"stopped task % %\n".postf( name );
				task.stop;
				task = nil;
			};

			menu = menus[i];
			header.mouseDownAction_({ |vw|
				var absoluteBounds, boundsKnown = true;
				menus.do({ |item| if( item != menu ) { item.visible_(false) }; });
				if( menu.visible.not ) {
					absoluteBounds = vw.absoluteBounds;
					boundsKnown = menu.bounds.height > 0;
					if( boundsKnown.not ) {
						menu.front( absoluteBounds.leftBottom );
					};
					if( (menu.bounds.height + absoluteBounds.bottom) > Window.availableBounds.bottom ) {
						menu.front( absoluteBounds.leftTop - (0@menu.bounds.height) );
					} {
						if( boundsKnown ) { menu.front( absoluteBounds.leftBottom ); };
					};
				};
			});
			menu.mouseLeaveAction = { |vw|
				//"menu leave % %\n".postf( name, vw.visible );
				if( vw.visible ) { makeTask.value; };
			};
			menu.mouseEnterAction = { |vw|
				//"menu enter %\n".postf( name );
				stopTask.value;
			};
			ctrl = SimpleController( menu )
			.put( \aboutToShow, {
				//"menu show %\n".postf( name );
				header.background = Color.black.alpha_(0.1);
				makeTask.value;
			})
			.put( \aboutToHide, {
				//"menu hide %\n".postf( name );
				header.background = Color.clear;
				stopTask.value;
			});
			header.onClose_({ ctrl.remove; stopTask.value; });
		});
		^headers;
	}

	*createMenuStrip { |parent, bounds, inset = #[0,-4,0,0]|
		var menuView;
		menuView = View( parent, bounds );
		menuView.resize_( 2 );
		menuView.bounds = menuView.bounds.insetAll( *inset );
		menuView.background_( RoundView.skin !? _.menuStripColor ?? { Color.gray(0.9) });
		if( menuStripMode == \views ) {
			menuView.addFlowLayout(0@0, 0@4);
			this.createMenuViews( menuView );
		} {
			menuView.layout = HLayout( this.createToolbar );
			menuView.layout.margins = 0;
			menuView.layout.spacing = 4;
		};
		^menuView;
	}

	*clear {
		var menus;
		allMenus = nil;
		if( mode === \mainmenu ) {
			MainMenu.registered.do({ |item|
				item.value.detect({ |acc| acc.key == 'unitlib' })
				.value.do({ |menu| menus =menus.add( menu ) })
			});
			menus.do({ |item| MainMenu.unregister( item ) });
		} {
			toolBar !? _.close;
			toolBar = nil;
		};
	}

	*new { |name = "Unit Lib"|

		this.clear; // clear the old menu first

		currentMenuName = name;

		font = font ? Font( Font.defaultSansFace, 13 );

/* MAIN */
		this.registerMenu( MenuAction( "Preferences...", {
			preferencesFunc.value;
		}).shortcut_( "Ctrl+P" ), name );

		this.registerMenu( MenuAction.separator("Reset"), name );

		this.registerMenu( MenuAction( "Stop current Scores", {
			UScoreEditorGUI.all.do({ |item| item.score.stop });
		}).shortcut_( "Ctrl+/" ), name );

		this.registerMenu( MenuAction( "Stop all (%-.)"
			.format( if( thisProcess.platform.name === 'osx' ) { "Cmd" } { "Ctrl"} ), {
				CmdPeriod.run;
		}), name );

		this.registerMenu( MenuAction( "Clear ULib", {
			ULib.clear( true );
		}), name );

		this.registerMenu( MenuAction( "(Re)activate all current Scores", {
			ULib.setAllScoresActive( true );
		}), name );

		this.registerMenu( MenuAction( "Reload global buffers", {
			 BufSndFile.reloadAllGlobal;
		}), name );

		this.registerMenu( MenuAction.separator( "Global Level" ), name );

		this.registerMenu( MenuAction( "Increase Level +1dB", {
			UGlobalGain.gain = (UGlobalGain.gain + 1).min(36);
		}).shortcut_( "Ctrl+Up" ), name );

		this.registerMenu( MenuAction( "Decrease Level -1dB", {
			UGlobalGain.gain = (UGlobalGain.gain - 1).max(-60);
		}).shortcut_( "Ctrl+Down" ), name );

		this.registerMenu( MenuAction( "Set Level 0dB", {
			UGlobalGain.gain = 0;
		}).shortcut_( "Ctrl+0" ), name );

		this.registerMenu( MenuAction( "Drop Level -60dB", {
			UGlobalGain.gain = -60;
		}).shortcut_( "Ctrl+-" ), name );

		this.registerMenu( MenuAction( "New Score", {
				UScore.new.gui;
		}).shortcut_( "Ctrl+N" ), "File" );

		this.registerMenu( MenuAction( "Open Score...", {
			UScore.openMultiple(nil, UScoreEditorGUI(_) )
		}).shortcut_( "Ctrl+O" ), "File" );

		openRecentMenu = Menu().title_("Open Recent").font_( font );

		this.registerMenu( openRecentMenu, "File" );

		URecentScorePaths.menu = openRecentMenu;

		URecentScorePaths.fillMenu;

		this.registerMenu( MenuAction( "Save Score", {
			UScore.current !! _.save;
		}).shortcut_( "Ctrl+S" ), "File" );

		this.registerMenu( MenuAction( "Save Score as...", {
			UScore.current !! _.saveAs;
		}).shortcut_( "Ctrl+Shift+S" ), "File" );

		this.registerMenu( MenuAction.separator("Export"), "File" );

		this.registerMenu( MenuAction( "Export as audio file..", {
			UScore.current !! { |x|
				ULib.savePanel({ |path|
					x.writeAudioFile( path );
				});
			};
		}), "File" );

		this.registerMenu( MenuAction( "Export selection as audio file..", {
			if( UScoreEditorGUI.currentSelectedEvents.notNil ) {
				ULib.savePanel({ |path|
					var evts, sc, minTime;
					evts = UScoreEditorGUI.currentSelectedEvents.collect(_.deepCopy);
					minTime = evts.collect(_.startTime).minItem;
					evts.do({ |evt| evt.startTime = evt.startTime - minTime; });
					sc = UScore( *evts );
					sc.writeAudioFile( path );
				});
			};
		}), "File" );

/* SESSION */

		this.registerMenu( MenuAction.separator("Sessions"), "File" );

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
				).title_( "Selected Events" ).font_( font )
			).title_("Add").font_( font ),

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
			).title_("New").font_( font ),
		).title_("Session").font_( font );

		this.registerMenu( sessionMenu, "File" );

/* EDIT */
		this.registerMenu( MenuAction( "Copy", {
	        UScoreEditorGUI.currentSelectedEvents !! UScoreEditor.copy(_)
		}).shortcut_( "Ctrl+C" ), "Edit" );

		this.registerMenu( MenuAction( "Paste", {
			UScoreEditorGUI.current !! { |x| x.scoreView.currentEditor.pasteAtCurrentPos }
		}).shortcut_( "Ctrl+V" ), "Edit" );

		this.registerMenu( MenuAction.separator("Events"), "Edit" );

		this.registerMenu( MenuAction( "Add Event", {
			UScoreEditorGUI.current !! { |x| x.editor.addEvent }
		}).shortcut_( "Ctrl++" ), "Edit" );

		this.registerMenu( MenuAction( "Add Marker", {
			UScoreEditorGUI.current !! { |x| x.editor.addMarker }
		}).shortcut_( "Ctrl+M" ), "Edit" );

		this.registerMenu( MenuAction( "Duplicate Selected", {
			UScoreEditorGUI.current !! { |x| x.scoreView.duplicateSelected }
		}).shortcut_( "Ctrl+D" ), "Edit" );

		this.registerMenu( MenuAction( "Edit Selected", {
			UScoreEditorGUI.current !! { |x| x.scoreView.editSelected }
		}).shortcut_( "Ctrl+I" ), "Edit" );

		this.registerMenu( MenuAction( "Delete Selected", {
			UScoreEditorGUI.current !! { |x| x.scoreView.deleteSelected }
		}), "Edit" );

		this.registerMenu( MenuAction.separator("Select"), "Edit" );

		this.registerMenu( MenuAction( "Select All", {
			UScoreEditorGUI.current !! { |x| x.scoreView.selectAll }

		}).shortcut_( "Ctrl+A" ), "Edit" );

		this.registerMenu( MenuAction( "Select Similar", {
			UScoreEditorGUI.current !! { |x| x.scoreView.selectSimilar }
		}), "Edit" );

		this.registerMenu( MenuAction( "Select Next", {
			UScoreEditorGUI.current !! { |x| x.scoreView.selectNext }
		}).shortcut_( "Shift+Right" ), "Edit" );

		this.registerMenu( MenuAction( "Select Previous", {
			UScoreEditorGUI.current !! { |x| x.scoreView.selectPrev }
		}).shortcut_( "Shift+Left" ), "Edit" );

		this.registerMenu( MenuAction( "Selection Add Next", {
			UScoreEditorGUI.current !! { |x| x.scoreView.selectAddNext; }
		}).shortcut_( "Shift+Down" ), "Edit" );

		this.registerMenu( MenuAction( "Selection Remove Last", {
			UScoreEditorGUI.current !! { |x| x.scoreView.selectRemoveLast }
		}).shortcut_( "Shift+Up" ), "Edit" );


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

		this.registerMenu( MenuAction( "EQ", { UGlobalEQ.gui; }).shortcut_( "Ctrl+E" ), "View");
		this.registerMenu( MenuAction( "Level (large)", { UGlobalGain.gui; }).shortcut_( "Ctrl+L" ), "View");
		this.registerMenu( MenuAction( "Udefs", { UdefsGUI(); }).shortcut_( "Ctrl+U" ), "View");
		this.registerMenu( MenuAction( "Environment", {
			ULib.envirWindow;
		}), "View");

		windowsMenu = Menu().title_( "Windows" ).font_( font );

		windowsCtrl.remove;
		windowsCtrl = SimpleController( windowsMenu )
		.put( \aboutToShow, {
			this.fillWindowsMenu;
		});

		this.registerMenu( windowsMenu, "View" );
	}

	*add { |name, function, menuName|
		if( function === \separator ) {
			this.registerMenu( MenuAction.separator( name ), menuName ? currentMenuName ? "Unit Lib" );
		} {
			this.registerMenu( MenuAction( name, function ), menuName ? currentMenuName ? "Unit Lib" );
		};
	}
}
