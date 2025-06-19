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

		{
			if( inuFluidNMFBaseBuffer.notNil && { inuFluidNMFBaseBuffer.numFrames.notNil }) {
				views[ \info ].string = "% bases (%)".format(
					inuFluidNMFBaseBuffer.numChannels,
					(inuFluidNMFBaseBuffer.numFrames - 1) * 2,
				)
			} {
				views[ \info ].string = "0 bases (-)";
			};
		}.defer;
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

	*viewNumLines { ^2 }

	makeView { |parent, bounds, resize|
		var plotWindow;

		if( bounds.isNil ) { bounds= 350 @ (this.class.viewNumLines * (viewHeight + 4)) };

		view = EZCompositeView( parent, bounds, gap: 4@4 );
		bounds = view.asView.bounds;
		view.onClose_({ this.remove; });
		view.resize_( resize ? 5 );
		views = ();

		views[ \path ] = FilePathView( view, bounds.width @ viewHeight )
		.resize_( 2 )
		.action_({ |fv|
			if( fv.value.notNil ) {
				this.performuFluidNMFBaseBuffer( \path_ , fv.value, true );
				//this.performuFluidNMFBaseBuffer( \fromFile );
				action.value( this );
			};
		});

		views[ \info ] = StaticText( view, (bounds.width - 60 - 40 - 8) @ viewHeight )
		.applySkin( RoundView.skin );

		views[ \operations ] = SmoothButton( view, 60 @ viewHeight )
		.radius_(2)
		.label_( "generate" )
		.action_({
			var closeFunc;
			if( views[ \genWindow ].isNil or: { views[ \genWindow ].isClosed } ) {
				views[ \genWindow ] = Window( "Fluid NFM bases", Rect(592, 534, 294, 102) ).front;
				views[ \genWindow ].addFlowLayout;

				RoundView.pushSkin( UChainGUI.skin );

				StaticText( views[ \genWindow ], 200@18 )
				.string_( "# components per soundfile" )
				.applySkin( RoundView.skin )
				.align_( \right );
				views[ \genNum ] = SmoothNumberBox( views[ \genWindow ], 80@18 )
				.clipLo_(1)
				.value_(2);

				views[ \genWindow ].asView.decorator.nextLine;

				StaticText( views[ \genWindow ], 200@18 )
				.string_( "windowSize" )
				.applySkin( RoundView.skin )
				.align_( \right );
				views[ \genWinSize ] = UPopUpMenu( views[ \genWindow ], 80@18 )
				.items_( [ 1024, 2048, 4096 ] )
				.value_( 0 );

				views[ \genWindow ].asView.decorator.nextLine;

				SmoothButton( views[ \genWindow ], 284@18 )
				.label_( "choose soundfile(s)" )
				.action_({
					var num, winSize;
					num = views[\genNum ].value;
					winSize = views[ \genWinSize ].item;
					ULib.openPanel({ |paths|
						Dialog.savePanel({ |outpath|
							{
								var ops;
								paths.do({ |path|
									var cond = Condition(false);
									"generating base(s) for \n\t%\n".postf( path );
									UFluidNMFBaseBuffer.generateBases(
										num,
										path,
										nil,
										winSize,
										{ |opath|
											ops = ops.add( opath );
											cond.test = true;
											cond.signal;
										}
									);
									cond.wait;
								});
								if( paths.size > 1 ) {
									"Merging files...".postln;
									SoundFile.uMerge( ops, threaded: true, action: { |op|
										{
											views[ \path ].value = op.path;
											views[ \path ].doAction;
										}.defer(0.1);
									});
								} {
									0.1.wait;
									views[ \path ].value = ops[0];
									views[ \path ].doAction;
								};
							}.fork( AppClock )
						}, path: paths[0].replaceExtension( "ufbases" ) );
					}, multipleSelection: true )
				});

				closeFunc = { views[ \genWindow ] !? (_.close); };

				views[ \operations ].onClose = views[ \operations ].onClose.addFunc( closeFunc );

				views[ \genWindow ].onClose = {
					views[ \operations ].onClose.removeFunc( closeFunc );
					views[ \genWindow ] = nil;
				};
				RoundView.popSkin;
			} {
				views[ \genWindow ].front;

			};
		});

		views[ \plot ] = SmoothButton( view, 40 @ viewHeight )
		.radius_( 2 )
		.resize_( 3 )
		.label_( "plot" )
		.action_({ |bt|
			var closeFunc, sf;

			plotWindow !? _.close;

			sf = this.performuFluidNMFBaseBuffer( \asSoundFile );

			if( sf.notNil ) {
				plotWindow = USoundFilePlotWindow( sf, 0, 0)
				.canSelect_( false );

				closeFunc = { plotWindow !? _.close; };

				plotWindow.window.onClose = {
					bt.onClose.removeFunc( closeFunc );
					plotWindow = nil;
				};

				bt.onClose = bt.onClose.addFunc( closeFunc );
			};
		});

		this.setFont;
	}

}