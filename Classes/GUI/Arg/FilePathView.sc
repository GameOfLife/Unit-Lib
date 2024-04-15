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

FilePathView {

	classvar <>clipboard;

	var <value;
	var <views, <view;
	var <viewHeight = 14;
	var <>action;
	var <>defaultStringColor;
	var <font;
	var <>allowEmpty = false;
	var <>menu;

	*new { |parent, bounds|
		^super.new.makeView( parent, bounds );
	}

	*viewNumLines { ^1 }

	makeString { |inPath|
		if( inPath.size == 0 ) { inPath = nil };
		^(inPath ? "(no file)").asString
	}

	setViews { |inPath|
		var string = this.makeString( inPath );
		{
			if( string.bounds( font ).width > views[ \filePath ].bounds.width ) {
				views[ \filePath ].align_( \right );
			} {
				views[ \filePath ].align_( \center );
			};
			views[ \filePath ].string = string ++ " ";
		}.defer;
	}

	value_ { |inPath|
		value = inPath;
		this.setViews( value );
	}

	font_ { |font|
		this.setFont( font );
	}

	setFont { |inFont|
		font = inFont ??
			{ RoundView.skin !? { RoundView.skin.font } } ??
			{ Font( Font.defaultSansFace, 10 ) };

		{
			views[ \filePath ].font = font;
		}.defer;
	}

	stringColor_ { |color|
		this.setStringColor( color ? defaultStringColor );
	}

	setStringColor { |color|
		{
			views[ \filePath ].stringColor = color ? defaultStringColor;
		}.defer;
	}

	resize_ { |resize|
		view.resize = resize ? 5;
	}

	doAction { action.value( this ) }

	canReceiveDragHandler {
		^{ |vw| View.currentDrag.class == String }
	}

	receiveDragHandler {
		^{
			this.value = View.currentDrag;
			action.value( this );
		};
	}

	browse { |action|
		this.browseSingle( action );
	}

	browseSingle { |action|
		ULib.openPanel( { |path|
			action.value( path );
		}, multipleSelection: false);
	}

	copyOrMove { |pth, mode = \copy, action, toPath| // \copy, \move, \saveAs
		var func;
		func = { |path|
			var res, newName;
			if( mode != \saveAs ) {
				newName = path.dirname +/+ pth.basename;
			} {
				newName = path.replaceExtension( pth.extension );
			};
			if( File.exists( newName ).not ) {
				if( mode != \move ) {
					File.copy( pth.getGPath.asPathFromServer, newName );
				} {
					pth.getGPath.asPathFromServer.moveTo( path.dirname );
				};
				action.value( newName );
			} {
				"file % already exists, changing url".postf( newName.quote );
				action.value( newName );
			};
		};
		if( pth.notNil && { pth.size > 0 } ) {
			if( toPath.isNil ) {
				ULib.savePanel( func );
			} {
				func.value( toPath );
			};
		};
	}

	makeMenu {
		var setAction = { |pth| this.value = pth; action.value( this ) };
		if( menu.notNil ) { menu.deepDestroy };
		menu = Menu(
			MenuAction( "Browse...", {
				this.browse( setAction );
			}),
			MenuAction( this.value ? "Enter path...", {
				SCRequestString( this.value, "Please enter file path:", { |string|
					setAction.value( string.standardizePath );
				})
			}),
			MenuAction.separator( "Operations" ),
			MenuAction( "Show file in Finder", {
				this.value.getGPath.asPathFromServer.dirname.openOS;
			}).enabled_( this.value.notNil ),
			MenuAction( "Move file to...", {
				this.copyOrMove( this.value, \move, setAction);
			}).enabled_( this.value.notNil && { this.value[..10] != "@resources/" } ),
			MenuAction( "Copy file to...", {
				this.copyOrMove( this.value, \copy, setAction);
			}).enabled_( this.value.notNil ),
			MenuAction( "Save file as...", {
				this.copyOrMove( this.value, \saveAs, setAction);
			}).enabled_( this.value.notNil ),
			MenuAction.separator( "Clipboard" ),
			MenuAction( "Copy pathname", {
				if( thisProcess.platform.name === \osx ) {
					"echo \"%\" | pbcopy".format( this.value.cs.escapeChar($") ).unixCmd;
				} {
					this.class.clipboard = this.value
				};
			}),
			MenuAction( "Paste pathname", {
				var res;
				if( thisProcess.platform.name === \osx ) {
					res = { "pbpaste".unixCmdGetStdOut.interpret }.try;
					if( res.isKindOf( String ) ) { setAction.value( res ); };
				} {
					if( this.class.clipboard.notNil ) {
						setAction.value( this.class.clipboard );
					};
				};
			}),
		);

		if( allowEmpty ) {
			menu.insertAction( 2,
				MenuAction( "Remove", { setAction.value( nil ); }).enabled_( this.value.notNil );
			);
		};
		menu.uFront;
	}

	makeView { |parent, bounds, resize|

		defaultStringColor = RoundView.skin.stringColor ?? { Color.black };

		bounds = bounds ?? { 350 @ (this.class.viewNumLines * (viewHeight + 4)) };

		view = EZCompositeView( parent, bounds, gap: 4@4 );
		view.resize_( resize ? 5 );
		views = ();
		bounds = view.view.bounds;

		views[ \filePath ] = StaticText( view, (bounds.width - (viewHeight + 4)) @ viewHeight )
		.applySkin( RoundView.skin )
		.align_( \center )
		.resize_( 2 )
		.background_( Color.white.alpha_(0.25) )
		.onClose_({ menu !? _.deepDestroy })
		.canReceiveDragHandler_( this.canReceiveDragHandler )
		.receiveDragHandler_( this.receiveDragHandler )
		.mouseDownAction_({ this.makeMenu });

		views[ \filePath ].setProperty(\wordWrap, false);

		views[ \browse ] = SmoothButton( view, viewHeight @ viewHeight )
			.radius_( 0 )
			.border_(0)
			.resize_( 3 )
			.label_( 'folder' )
		    .action_({ this.browse({ |pth| this.value = pth; action.value( this ); }) });

		this.setFont;
	}

}

MultiFilePathView : FilePathView {

	var <>fixedSize = true;
	var <>preProcessPathsFunc;

	makeString { |inPaths|
		if( inPaths.isString ) { inPaths = [ inPaths ] };
		if( inPaths.every({ |item| item == inPaths.first }) ) {
			if( inPaths.first.size == 0 ) {
				^"(no files)";
			} {
				^inPaths.first.asString + "(% files)".format( inPaths.size );
			}
		} {
			^"(mixed, % files)".format( inPaths.size );
		};
	}

	canReceiveDragHandler {
		^{ |vw| [ String, Array ].includes( View.currentDrag.class ) }
	}

	receiveDragHandler {
		^{
			var paths;
			if( View.currentDrag.isString ) {
				paths = [ View.currentDrag ];
			} {
				paths = View.currentDrag
			};
			this.setPaths( paths, { |paths| this.value = paths; action.value( this ) } );
		};
	}

	setPaths { |paths, action, preProcess = true|
		var newVal, string, single = false;
		if( preProcess == true && { preProcessPathsFunc.notNil }) {
			preProcessPathsFunc.value( this, paths, { |ppaths|
				this.setPaths( ppaths, action, false );
			})
			^this;
		};

		if( this.fixedSize ) {
			if( paths.size < (this.value.size) ) {
				string = "You selected % for % units.\n" ++
				"Use only for the first %,\n" ++
				"or % for all?";

				if( paths.size == 1 ) {
					string = string.format( "one file", this.value.size, "unit", "the same" )
				} {
					string = string.format(
						"% files".format( paths.size ), this.value.size,
						"% units".format( paths.size ), "wrap around"
					);
				};

				SCAlert( string,
					[ "cancel", "first %".format( paths.size ), "all" ],
					[ {}, {
						newVal = this.value;
						paths = paths[..newVal.size-1];
						paths.do({ |item, i|
							newVal[i] = item;
						});
						action.value( newVal );
					}, {
						action.value(
							this.value.collect({ |item, i|
								paths.wrapAt(i);
							})
						)
					} ]
				);
			} {
				action.value( paths[..this.value.size-1] );
			};
		} {
			action.value( paths );
		};
	}


	browse { |action|
		ULib.openPanel( { |paths|
			this.setPaths( paths, action );
		}, multipleSelection: true);
	}

	makeMenu {
		var uniquePaths = [];
		var setSingle = { |pth, index = 0|
			if( index.isArray ) {
				index.do({ |id|
					this.value[ id ] = pth;
				});
			} {
				this.value[ index ] = pth;
			};
			this.value = this.value;
			action.value( this );
		};

		this.value.do({ |path|
			if( uniquePaths.includesEqual( path ).not ) {
				uniquePaths = uniquePaths.add( path );
			};
		});

		if( menu.notNil ) { menu.deepDestroy };

		menu = Menu(
			MenuAction( "Browse...", {
				this.browse({ |paths| this.value = paths; action.value( this ) });
			}),
			Menu(
				*this.value.collect({ |pth, i|
					var xmenu = Menu(
						MenuAction( "Browse...", {
							this.browseSingle( { |px| setSingle.value( px, i ) } );
						}),
						MenuAction( "Enter String...", {
							SCRequestString( pth, "Please enter file path:", { |string|
								setSingle.value( string.standardizePath, i )
							});
						}),
						MenuAction.separator( "Operations" ),
						MenuAction( "Show file in Finder", {
							pth.getGPath.asPathFromServer.dirname.openOS;
						}).enabled_( pth.notNil ),
						MenuAction( "Move file to...", {
							this.copyOrMove( pth, \move, { |px| setSingle.value( px, i ) } );
						}).enabled_( pth.notNil && { pth[..10] != "@resources/" } ),
						MenuAction( "Copy file to...", {
							this.copyOrMove( pth, \copy, { |px| setSingle.value( px, i ) } );
						}).enabled_( pth.notNil ),
						MenuAction( "Save file as...", {
							this.copyOrMove( pth, \saveAs, { |px| setSingle.value( px, i ) } );
						}).enabled_( pth.notNil ),
					).title_( "%: %".format( i, pth ) );
					if( fixedSize == false ) {
						xmenu.addAction( MenuAction.separator );
						xmenu.addAction( MenuAction( "Remove", {
							this.value.removeAt( i );
							this.value = this.value;
							action.value( this );
						}).enabled_( this.value.size > 1 ) );
						xmenu.addAction( MenuAction( "Add...", {
							this.browseSingle({ |px|
								this.value = this.value.insert( i, px );
								action.value( this );
							});
						}) );
					};
					xmenu;
				});
			).title_( "Pathnames (% files)".format( this.value.size ) ),
			MenuAction.separator,
			MenuAction( "Post file paths", { this.value.do(_.postcs) }),
			MenuAction( "Copy all files to...", {
				ULib.savePanel({ |path|
					uniquePaths.do({ |px|
						var indices = this.value.indicesOfEqual( px );
						this.copyOrMove( px, \copy, { |pth|
							indices.do({ |index|
								this.value[ index ] = pth;
							});
							"copied % to\n   %\n".postf( px, pth );
						}, path );
					});
					this.value = this.value;
					action.value( this );
				});
			}).enabled_( this.value.notNil ),
		);

		if( uniquePaths.size != (this.value.size) ) {
			menu.insertAction( 1, Menu(
				*uniquePaths.collect({ |pth, i|
					var indices = this.value.indicesOfEqual( pth );
					var submenu = Menu(
						MenuAction( "Browse...", {
							this.browseSingle( { |px|
								setSingle.value( px, indices )
							} );
						}),
						MenuAction( "Enter String...", {
							SCRequestString( pth, "Please enter file path:", { |string|
								setSingle.value( string.standardizePath, indices )
							});
						}),
						MenuAction.separator( "Operations" ),
						MenuAction( "Show file in Finder", {
							pth.getGPath.asPathFromServer.dirname.openOS;
						}).enabled_( pth.notNil ),
						MenuAction( "Move file to...", {
							this.copyOrMove( pth, \move, { |px| setSingle.value( px, indices ) } );
						}).enabled_( pth.notNil && { pth[..10] != "@resources/" } ),
						MenuAction( "Copy file to...", {
							this.copyOrMove( pth, \copy, { |px| setSingle.value( px, indices ) } );
						}).enabled_( pth.notNil ),
						MenuAction( "Save file as...", {
							this.copyOrMove( pth, \saveAs, { |px| setSingle.value( px, indices ) } );
						}).enabled_( pth.notNil ),
					).title_( "% (%)".format( pth, indices.size ) );
					submenu;
				});
			).title_("Unique pathnames (%)".format( uniquePaths.size ) )
			);
		};
		menu.uFront;
	}



}