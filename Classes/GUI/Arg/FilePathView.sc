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

	*new { |parent, bounds, filePath|
		^super.new.makeView( parent, bounds );
	}

	*viewNumLines { ^1 }

	setViews { |inPath|
		{
			if( (inPath ? "").bounds( font ).width > views[ \filePath ].bounds.width ) {
				views[ \filePath ].align_( \right );
			} {
				views[ \filePath ].align_( \center );
			};
			views[ \filePath ].string = (inPath ? "(no file)").asString ++ " ";
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
		.canReceiveDragHandler_({ |vw| View.currentDrag.class == String })
		.receiveDragHandler_({ |vw|
			this.value = View.currentDrag;
			action.value( this );
		})
		.mouseDownAction_({
			Menu(
				MenuAction( "Browse...", {
					ULib.openPanel( { |path|
						this.value = path;
						action.value( this );
					}, multipleSelection: false);
				}),
				MenuAction( this.value, {
					SCRequestString( this.value, "Please enter file path:", { |string|
						this.value = string.standardizePath;
						action.value( this );
					})
				}),
				MenuAction.separator( "Operations" ),
				MenuAction( "Show file in Finder", {
					this.value.getGPath.asPathFromServer.dirname.openOS;
				}),
				MenuAction( "Move file to...", {
					var pth = this.value;
					if( pth.notNil && { pth.size > 0 } ) {
						ULib.savePanel({ |path|
							var res, newName;
							newName = path.dirname +/+ pth.basename;
							if( File.exists( newName ).not ) {
								res = pth.getGPath.asPathFromServer.moveTo( path.dirname );
								if( res ) {
									this.value = newName;
									action.value( this );
								};
							} {
								"file % already exists, changing url".postf( newName.quote );
								this.value = path.dirname +/+ pth.basename;
								action.value( this );
							};
						});
					};
				}).enabled_( this.value.notNil && { this.value[..10] != "@resources/" } ),
				MenuAction( "Copy file to...", {
					var pth = this.value;
					if( pth.notNil && { pth.size > 0 } ) {
						ULib.savePanel({ |path|
							var newName;
							newName = path.dirname +/+ pth.basename;
							if( File.exists( newName ).not ) {
								File.copy( pth.getGPath.asPathFromServer, newName );
								this.value = newName;
								action.value( this );
							} {
								"file % already exists, changing url".postf( newName.quote );
								this.value = path.dirname +/+ pth.basename;
								action.value( this );
							};
						});
					};
				}).enabled_( this.value.notNil ),
				MenuAction( "Save file as...", {
					var pth = this.value;
					if( pth.notNil && { pth.size > 0 } ) {
						ULib.savePanel({ |path|
							var newName;
							newName = path.replaceExtension( pth.extension );
							if( File.exists( newName ).not ) {
								File.copy( pth.getGPath.asPathFromServer, newName );
								this.value = newName;
								action.value( this );
							} {
								"file % already exists, changing url".postf( newName.quote );
								this.value = path.dirname +/+ pth.basename;
								action.value( this );
							};
						});
					};
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
						if( res.isKindOf( String ) ) {
							this.value = res;
							action.value( this );
						};
					} {
						if( this.class.clipboard.notNil ) {
							this.value = this.class.clipboard;
							action.value( this );
						};
					};
				}),
			).front;
		});

		views[ \filePath ].setProperty(\wordWrap, false);

		views[ \browse ] = SmoothButton( view, viewHeight @ viewHeight )
			.radius_( 0 )
			.border_(0)
			.resize_( 3 )
			.label_( 'folder' )
			.action_({
				ULib.openPanel( { |path|
				  this.value = path;
				  action.value( this );
				}, multipleSelection: false);
			});

		this.setFont;
	}

}