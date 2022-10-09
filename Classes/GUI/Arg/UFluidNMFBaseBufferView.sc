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

UFluidNMFBaseBufferView {

	var <uFluidNMFBaseBuffer;
	var <parent, <view, <views;
	var <>action;
	var <viewHeight = 14;

	*new { |parent, bounds, action, uFluidNMFBaseBuffer|
		^super.new.init( parent, bounds, action ).value_( uFluidNMFBaseBuffer );
	}

	init { |parent, bounds, inAction|
		action = inAction;
		this.makeView( parent, bounds );
	}

	doAction { action.value( this ) }

	value { ^uFluidNMFBaseBuffer }
	value_ { |newuFluidNMFBaseBuffer|
		if( uFluidNMFBaseBuffer != newuFluidNMFBaseBuffer ) {
			uFluidNMFBaseBuffer.removeDependant( this );
			uFluidNMFBaseBuffer = newuFluidNMFBaseBuffer;
			uFluidNMFBaseBuffer.addDependant( this );
			this.update;
		};
	}

	update {
		if( uFluidNMFBaseBuffer.notNil ) { this.setViews( uFluidNMFBaseBuffer ) };
	}

	resize_ { |resize|
		view.resize = resize ? 5;
	}

	remove {
		if( uFluidNMFBaseBuffer.notNil ) {
			uFluidNMFBaseBuffer.removeDependant( this );
		};
	}

	setViews { |inuFluidNMFBaseBuffer|

		views[ \path ].value = inuFluidNMFBaseBuffer.path;
		if( File.exists( inuFluidNMFBaseBuffer.path.getGPath ? "" ) ) {
			views[ \path ].stringColor = Color.black;
		} {
			views[ \path ].stringColor = Color.red(0.66);
		};

		/*
		{ views[ \duration ].string = (inuFluidNMFBaseBuffer.duration ? 0).asSMPTEString(1000); }.defer;
		*/
	}

	setFont { |font|
		font = font ??
			{ RoundView.skin !? { RoundView.skin.font } } ??
			{ Font( Font.defaultSansFace, 10 ) };

		{
			//views[ \durationLabel ].font = font;
			//views[ \duration ].font = font;
			views[ \operations ].font = font;
		}.defer;

		views[ \path ].font = font;
		views[ \plot ].font = font;
	}

	performuFluidNMFBaseBuffer { |selector ...args|
		if( uFluidNMFBaseBuffer.notNil ) {
			^uFluidNMFBaseBuffer.perform( selector, *args );
		} {
			^nil
		};
	}

	*viewNumLines { ^3 }

	makeView { |parent, bounds, resize|

		if( bounds.isNil ) { bounds= 350 @ (this.class.viewNumLines * (viewHeight + 4)) };

		view = EZCompositeView( parent, bounds, gap: 4@4 );
		bounds = view.asView.bounds;
		view.onClose_({ this.remove; });
		view.resize_( resize ? 5 );
		views = ();

		views[ \path ] = FilePathView( view, bounds.width @ ( (viewHeight * 2) + 4) )
		.resize_( 2 )
		.action_({ |fv|
			if( fv.value.notNil ) {
				this.performuFluidNMFBaseBuffer( \path_ , fv.value, true );
				//this.performuFluidNMFBaseBuffer( \fromFile );
				action.value( this );
			};
		});

		views[ \operations ] = PopUpMenu( view, 80 @ viewHeight )
		.applySkin( RoundView.skin ? () )
		.items_( [
			"operations",
			"",
			"generate bases from file..",
			"",
			"reveal in Finder",
			"move to..",
			"copy to..",
			"save as.."
		] )
		.action_({ |pu|
			var pth, ext, closeFunc;
			switch( pu.value.asInteger,
				2, { // generate bases
					if( views[ \genWindow ].isNil or: { views[ \genWindow ].isClosed } ) {
						views[ \genWindow ] = Window( "Fluid NFM bases", Rect(592, 534, 294, 102) ).front;
						views[ \genWindow ].addFlowLayout;
						StaticText( views[ \genWindow ], 50@18 ).string_( "nr. of components" );
						views[ \genNum ] = SmoothNumberBox( views[ \genWindow ], 80@18 )
						.value_(2);
						SmoothButton( views[ \genWindow ], 80@18 )
						.border_(1)
						.extrude_(false)
						.label_( "choose soundfile" )
						.action_({
							var num;
							num = views[\genNum ].value;
							ULib.openPanel({ |path|
								Dialog.savePanel({ |outpath|
									UFluidNMFBaseBuffer.generateBases(
										num,
										path,
										outpath,
										{ |opath|
											{
												views[ \path ].value = outpath;
											    views[ \path ].doAction;
											}.defer(0.1);
										}
									)
								}, path: path.replaceExtension( "ufbases%".format( num ) ) );
							})
						});

						closeFunc = { views[ \genWindow ] !? (_.close); };

						views[ \operations ].onClose = views[ \operations ].onClose.addFunc( closeFunc );

						views[ \genWindow ].onClose = {
							views[ \operations ].onClose.removeFunc( closeFunc );
							views[ \genWindow ] = nil;
						};
					} {
						views[ \genWindow ].front;						};
				},
				4, {  // reveal in Finder
					pth = this.performuFluidNMFBaseBuffer( \path );
					if( pth.notNil ) {
						pth.getGPath.asPathFromServer.revealInFinder;
					};
				},
				5, { // move to..
					pth = this.performuFluidNMFBaseBuffer( \path );
					if( pth.notNil ) {
						pth = pth.getGPath;
						if( pth[..6] == "sounds/" ) {
							"can't move %, try copying instead\n".postf( pth.quote );
						};
						Dialog.savePanel({ |path|
							var res;
							res = pth.asPathFromServer.moveTo( path.dirname );
							if( res ) {
								this.performuFluidNMFBaseBuffer( \path_ ,
									path.dirname +/+ pth.basename
								);
							};
						});
					};
				},
				6, { // copy to..
					pth = this.performuFluidNMFBaseBuffer( \path );
					if( pth.notNil ) {
						Dialog.savePanel({ |path|
							var res;
							res = pth.getGPath.asPathFromServer.copyTo( path.dirname );
							if( res ) {
								this.performuFluidNMFBaseBuffer( \path_ ,
									path.dirname +/+ pth.basename
								);
							};
						});
					};
				},
				7, { // save as..
					pth = this.performuFluidNMFBaseBuffer( \path );
					if( pth.notNil ) {
						ext = pth.extension;
						Dialog.savePanel({ |path|
							var res;
							path =  path.replaceExtension( ext );
							res = pth.getGPath.asPathFromServer.copyFile(  path );
							if( res ) {
								this.performuFluidNMFBaseBuffer( \path_ , path );
							};
						});
					};
				}
			);
			pu.value = 0;
		});

		views[ \plot ] = SmoothButton( view, 40 @ viewHeight )
			.radius_( 3 )
			.border_( 1 )
			.resize_( 3 )
			.label_( "plot" )
			.action_({ |bt|

				// this will have to go in a separate class
				var w, a, f, b, x;
				var closeFunc;

				x = uFluidNMFBaseBuffer;
				f = this.performuFluidNMFBaseBuffer( \asSoundFile );

				w = Window(f.path, Rect(200, 200, 850, 400), scroll: false);
				a = SoundFileView.new(w, w.view.bounds);
				a.resize_(5);
				a.soundfile = f;
				a.read(0, f.numFrames);
				a.elasticMode_(1);
				a.gridOn = true;
				a.gridColor_( Color.gray(0.5).alpha_(0.5) );
				a.waveColors = Color.gray(0.2)!16;
				w.front;
				//a.background = Gradient( Color.white, Color.gray(0.7), \v );

				closeFunc = { w.close; };

				w.onClose = { bt.onClose.removeFunc( closeFunc ) };

				bt.onClose = bt.onClose.addFunc( closeFunc );
			});

		/*
		view.view.decorator.nextLine;

		views[ \durationLabel ] = StaticText( view, 40 @ viewHeight )
			.applySkin( RoundView.skin ? () )
			.string_( "duration" );

		views[ \duration ] = StaticText( view, (bounds.width - 88) @ viewHeight )
			.resize_( 2 )
			.applySkin( RoundView.skin ? () );
		*/

		this.setFont;
	}

}