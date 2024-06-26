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

BufSndFileView {

	classvar <timeMode = \frames; // or \seconds
	classvar <rateMode = \semitones; // or \ratio
	classvar <all;

	var <sndFile;
	var <parent, <view, <views;
	var <>action;
	var <viewHeight = 14;
	var <>autoCreateSndFile = false;
	var <>stringColor;

	*new { |parent, bounds, action, sndFile|
		^super.new.init( parent, bounds, action ).value_( sndFile ).addToAll;
	}

	init { |parent, bounds, inAction|
		action = inAction;
		this.makeView( parent, bounds );
	}

	addToAll {
		all = all.add( this );
	}

	*timeMode_ { |new = \frames|
		timeMode = new.asSymbol;
		all.do( _.setTimeMode( timeMode ) );
	}

	*rateMode_ { |new = \semitones|
		rateMode = new.asSymbol;
		all.do( _.setRateMode( rateMode ) );
	}

	doAction { action.value( this ) }

	value { ^sndFile }
	value_ { |newSndFile|
		if( sndFile != newSndFile ) {
			sndFile.removeDependant( this );
			sndFile = newSndFile;
			sndFile.addDependant( this );
			this.update;
		};
	}

	update {
		if( sndFile.notNil ) { this.setViews( sndFile ) };
	}

	resize_ { |resize|
		view.resize = resize ? 5;
	}

	remove {
		if( sndFile.notNil ) {
			sndFile.removeDependant( this );
		};
		all.remove( this );
	}

	setViews { |inSndFile|

		views[ \path ].value = inSndFile.path;
		views[ \path ].stringColor = if( inSndFile.exists ) {
			stringColor ?? { Color.black; }
		} { Color.red(0.66); };

		if( inSndFile.respondsTo( \hasGlobal ) ) {
			{ views[ \hasGlobal ].visible = true; }.defer;
			views[ \hasGlobal ].value = inSndFile.hasGlobal.binaryValue;
		} {
			{ views[ \hasGlobal ].visible = false; }.defer;
		};

		views[ \startFrame ].value = inSndFile.startFrame;
		views[ \startFrame ].clipHi = inSndFile.numFrames ? inf;

		views[ \startSecond ].value = inSndFile.startSecond;
		views[ \startSecond ].clipHi = inSndFile.fileDuration ? inf;

		views[ \endFrame ].value = inSndFile.endFrame;
		views[ \endFrame ].clipHi = inSndFile.numFrames ? inf;

		views[ \endSecond ].value = inSndFile.endSecond;
		views[ \endSecond ].clipHi = inSndFile.fileDuration ? inf;

		views[ \loop ].value = inSndFile.loop.binaryValue;

		views[ \rateRatio ].value = inSndFile.rate;
		views[ \rateSemitones ].value = inSndFile.rate.ratiomidi.round( 1e-6);

		{ views[ \numChannels ].string = " % (% channel%)".format(
			    inSndFile.fileDuration.asSMPTEString(1000),
				inSndFile.numChannels,
				if( inSndFile.numChannels == 1 ) { "" } { "s" }
			)
		}.defer;

	}

	setTimeMode { |mode = \frames|
		switch ( ( mode.asString[0] ? $f ).toLower,
			$s, { // \seconds
				views[ \startSecond ].visible_( true );
				views[ \startFrame ].visible_( false );
				views[ \endSecond ].visible_( true );
				views[ \endFrame ].visible_( false );
				{ views[ \timeMode ].string = " s" }.defer;

			},
			$f, { // \frames
				views[ \startSecond ].visible_( false );
				views[ \startFrame ].visible_( true );
				views[ \endSecond ].visible_( false );
				views[ \endFrame ].visible_( true );
				{ views[ \timeMode ].string = " smp" }.defer;
			}
		);
	}

	setRateMode { |mode = \semitones|
		switch( ( mode.asString[0] ? $s ).toLower,
			$r, { // \ratio
				views[ \rateRatio ].visible_( true );
				views[ \rateSemitones ].visible_( false );
				{ views[ \rateMode ].value = 0 }.defer;
			},
			$s, {
				views[ \rateRatio ].visible_( false );
				views[ \rateSemitones ].visible_( true );
				{ views[ \rateMode ].value = 1 }.defer;
			}
		);
	}

	setFont { |font|
		font = font ??
			{ RoundView.skin !? { RoundView.skin.font } } ??
			{ Font( Font.defaultSansFace, 10 ) };

		{
			views[ \startLabel ].font = font;
			views[ \timeMode ].font = font;
			views[ \rateLabel ].font = font;
			views[ \rateMode ].font = font;
		}.defer;

		views[ \path ].font = font;
		views[ \startFrame ].font = font;
		views[ \startSecond ].font = font;
		views[ \endFrame ].font = font;
		views[ \endSecond ].font = font;
		views[ \loop ].font = font;
		views[ \rateRatio ].font = font;
		views[ \rateSemitones ].font = font;

	}

	performSndFile { |selector ...args|
		if( sndFile.notNil ) {
			^sndFile.perform( selector, *args );
		} {
			if( autoCreateSndFile ) {
				this.value = BufSndFile.newBasic( );
				^sndFile.perform( selector, *args );
			} {
				^nil;
			};
		};
	}

	*viewNumLines { ^4 }

	makeView { |parent, bounds, resize|
		var globalDepFunc, updGlobal, skin;

		if( bounds.isNil ) { bounds= 350 @ (this.class.viewNumLines * (viewHeight + 4)) };

		stringColor = RoundView.skin !? _.stringColor ?? { Color.black };

		skin = RoundView.skin;

		view = EZCompositeView( parent, bounds, gap: 4@4 );
		bounds = view.asView.bounds;
		view.onClose_({ this.remove; });
		view.resize_( resize ? 5 );
		views = ();

		views[ \path ] = FilePathView( view, bounds.width @ viewHeight )
			.resize_( 2 )
			.action_({ |fv|
				this.performSndFile( \path_ , fv.value );
				this.performSndFile( \fromFile );
				action.value( this );
			});

		views[ \numChannels ] = StaticText( view, 62 + 84 @ viewHeight )
			.applySkin( RoundView.skin ? () )
			.string_( "" );

		view.view.decorator.shift( (bounds.width - 44) - 190, 0 );

		views[ \hasGlobal ] = SmoothButton( view, 40 @ viewHeight )
				.radius_( 2 )
				.label_( ["global", "global"] )
				.action_({ |bt|
					switch( bt.value,
						1, { this.performSndFile( \loadGlobal ) },
						0, { this.performSndFile( \disposeGlobal ) }
					);
				});
		updGlobal = {
			if( views[ \hasGlobal ].visible ) {
				views[ \hasGlobal ].value = this.performSndFile( \hasGlobal ).binaryValue;
			};
		};

		views[ \plot ] = SmoothButton( view, 40 @ viewHeight )
		.radius_( 2 )
		.label_( "plot" )
		.action_({ |bt|
			var w, f, sfv, sfZoom, mouseButton, dur, infoView;
			var closeFunc, moveRange, getMoveRange, mouseAction, getMousePos;

			RoundView.pushSkin( skin );

			f = this.performSndFile( \asSoundFile );

			w = Window(f.path.formatGPath, Rect(200, 200, 850, 400), scroll: false);

			dur = f.numFrames / f.sampleRate;

			w.addFlowLayout( 4@4, 4@4 );

			sfv = SoundFileView( w, w.bounds.insetAll( 4, 4, 4, 40 ) ).resize_(5);
			sfZoom = SmoothRangeSlider( w, (w.bounds.width - 8) @ 14 ).resize_(8);
			infoView = StaticText( w, (w.bounds.width - 92) @ 14 ).resize_(8)
			.applySkin( RoundView.skin );
			SmoothSlider( w, 80 @ 14 )
			.knobSize_(1)
			.resize_(9)
			.action_({ |sl|
				sfv.yZoom = sl.value.linlin(0,1,0,24).dbamp;
			});

			sfZoom.knobSize = 1;
			sfZoom.canFocus = false;

			sfv.bounds = sfv.bounds.insetAll( -4, -4, -4, 0 );

			sfv.soundfile = f;
			sfv.read(0, f.numFrames);

			sfZoom.lo_(0).range_(1)
			.action_({ |view|
				var divisor, rangeStart;
				rangeStart = view.lo;
				divisor = 1 - sfZoom.range;
				if(divisor < 0.0001) {
					rangeStart = 0;
					divisor = 1;
				};
				sfv.xZoom_(sfZoom.range * dur)
				.scrollTo(rangeStart / divisor)
			});

			sfv.background = Color.gray(0.6);
			sfv.gridColor = Color.gray(0.5,0.25);
			sfv.peakColor = Color.gray(0.8);
			sfv.rmsColor = Color.white;
			sfv.timeCursorColor = Color.blue(0.2).alpha_(0.5);
			sfv.setSelectionColor( 0, Color.blue(0.2).alpha_(0.2) );
			sfv.setSelectionColor( 1, Color.clear );
			sfv.currentSelection = 1;

			views[ \setPlotRange ] = {
				{
					sfv.setSelection( 0, [
						sndFile.startFrame,
						sndFile.endFrame - sndFile.startFrame
					]);
				}.defer;
			};

			views[ \setPlotRange ].value;

			getMousePos = { |sfv, x|
				(
					x.linlin( 0, sfv.bounds.width, 0, 1 ) * sfv.viewFrames +
					(sfv.scrollPos * (sfv.numFrames - sfv.viewFrames ))
				).asInteger;
			};

			getMoveRange = { |sfv, x|
				var selection = sfv.selection(0);
				var mousePos = getMousePos.value( sfv, x );
				if( mousePos.exclusivelyBetween(
					*(selection[1] * [1/4,3/4] + selection[0] )
				) ) {
					(selection * [1,0.5]).sum - mousePos;
				};
			};

			mouseAction = { |sfv, x, y|
				var selection = sfv.selection(0);
				var border;
				var mousePos = getMousePos.value( sfv, x );

				border = (selection * [1,0.5]).sum;

				case { moveRange.notNil } {
					sfv.setSelectionStart( 0,
						(mousePos - (selection[1] / 2) + moveRange )
						.max(0).min( sfv.numFrames - selection[1] ) );
					sfv.timeCursorOn = false;
				} {
					if( mousePos > border ) {
						sfv.setSelectionSize( 0, mousePos - selection[0] );
					} {
						sfv.setSelection( 0, [
							mousePos,
							selection[0] - mousePos + selection[1]
						] );
					};
					sfv.timeCursorOn = true;
					sfv.timeCursorPosition = mousePos;
				};
				selection = sfv.selection(0);
				infoView.string = " trim: % - % / % - %".format(
					selection[0], selection.sum,
					(selection[0] / f.sampleRate).asSMPTEString(1000),
					(selection.sum / f.sampleRate).asSMPTEString(1000),
				);
			};

			sfv.mouseDownAction = { |sfv, x, y|
				//var selection = sfv.selection(0);
				//var mousePos = getMousePos.value( sfv, x );
				moveRange = getMoveRange.value( sfv, x );
				if( moveRange.notNil ) {
					sfv.setSelectionColor( 0, Color.blue(0.2).alpha_(0.4) );
				};
				mouseAction.value( sfv, x, y );
			};

			sfv.mouseMoveAction = mouseAction;

			w.view.acceptsMouseOver = true;

			sfv.mouseOverAction = { |sfv, x, y|
				var pos, selection, border, mvr;
				pos = getMousePos.value( sfv, x );
				mvr = getMoveRange.value( sfv, x );
				if( mvr.isNil ) {
					selection = sfv.selection(0);
					border = (selection * [1,0.5]).sum;
					if( pos > border ) {
						sfv.timeCursorPosition = selection.sum;
					} {
						sfv.timeCursorPosition = selection[0];
					};
					sfv.timeCursorOn = true;
				} {
					sfv.timeCursorOn = false;
				};
				infoView.string = " pos: % / %".format(
					pos, (pos / f.sampleRate).asSMPTEString(1000)
				);
			};

			sfv.mouseLeaveAction = { |sfv, x, y|
				infoView.string = "";
				sfv.timeCursorOn = false;
			};

			sfv.mouseUpAction = {
				sfv.setSelectionColor( 0, Color.blue(0.2).alpha_(0.2) );
			};

			sfv.action = { |vw|
				var selection;
				selection = sfv.selection(0);
				sndFile.startFrame = selection[0];
				if( selection[1] == 0 ) {
					sndFile.endFrame = nil;
				} {
					sndFile.endFrame = selection.sum;
				};
			};

			closeFunc = { w.close; };

			w.onClose = {
				bt.onClose.removeFunc( closeFunc );
				views[ \setPlotRange ] = nil;
			};

			bt.onClose = bt.onClose.addFunc( closeFunc );


			w.front;

			RoundView.popSkin( skin );
		});

		BufSndFile.global.addDependant( updGlobal );
		views[ \hasGlobal ].onClose_({
			BufSndFile.global.removeDependant( updGlobal );
		});

		view.view.decorator.nextLine;

		views[ \startLabel ] = StaticText( view, 25 @ viewHeight )
			.applySkin( RoundView.skin ? () )
			.string_( "trim" );

		views[ \startComp ] = CompositeView( view, (((bounds.width - 73)/2).floor-2) @ viewHeight )
			.resize_( 2 );

		views[ \startSecond ] = SMPTEBox( views[ \startComp ],
				views[ \startComp ].bounds.moveTo(0,0) )
			.applySmoothSkin
		    .applySkin( RoundView.skin ? () )
			.resize_( 5 )
			.clipLo_( 0 )
			.action_({ |nb|
				this.performSndFile( \startSecond_ , nb.value );
				views[ \setPlotRange ].value;
				action.value( this );
			});

		views[ \startFrame] = SmoothNumberBox( views[ \startComp ],
				views[ \startComp ].bounds.moveTo(0,0) )
			.resize_( 5 )
			.clipLo_( 0 )
			.action_({ |nb|
				this.performSndFile( \startFrame_ , nb.value );
				views[ \setPlotRange ].value;
				action.value( this );
			})
			.visible_( false );

		views[ \endComp ] = CompositeView( view, (((bounds.width - 73)/2)-2) @ viewHeight )
			.resize_( 2 );

		views[ \endSecond ] = SMPTEBox( views[ \endComp ],
				views[ \endComp ].bounds.moveTo(0,0) )
			.applySmoothSkin
		    .applySkin( RoundView.skin ? () )
			.resize_( 5 )
			.clipLo_( 0 )
			.action_({ |nb|
				this.performSndFile( \endSecond_ , nb.value );
				views[ \setPlotRange ].value;
				action.value( this );
			});

		views[ \endFrame] = SmoothNumberBox( views[ \endComp ],
				views[ \endComp ].bounds.moveTo(0,0) )
			.resize_( 5 )
			.clipLo_( 0 )
			.action_({ |nb|
				this.performSndFile( \endFrame_ , nb.value );
				views[ \setPlotRange ].value;
				action.value( this );
			})
			.visible_( false );

		views[ \timeMode ] = StaticText( view, 40 @ viewHeight )
		.applySkin( RoundView.skin ? ())
		.string_( " smp" )
		.background_( Color.white.alpha_(0.25) )
		.resize_( 3 )
		.onClose_({ views[ \timeMenu ] !? _.deepDestroy; })
		.mouseDownAction_({
			views[ \timeMenu ] !? _.deepDestroy;
			views[ \timeMenu ] = Menu(
				MenuAction( "seconds", { this.class.timeMode = \seconds })
				.enabled_( this.class.timeMode != \seconds ),
				MenuAction( "frames", { this.class.timeMode =  \frames })
				.enabled_( this.class.timeMode != \frames ),
				MenuAction.separator,
				MenuAction( "Reset", {
					this.performSndFile( \startFrame_ , 0 );
					this.performSndFile( \endFrame_ , nil );
					views[ \setPlotRange ].value;
					action.value( this );
				})
			).uFront;
		});

		views[ \rateLabel ] = StaticText( view, 25 @ viewHeight )
			.applySkin( RoundView.skin ? () )
			.string_( "rate" );

		views[ \rateComp ] = CompositeView( view, (((bounds.width - 73)/2).floor-2) @ viewHeight )
			.resize_( 2 );

		views[ \rateRatio ] = SmoothNumberBox( views[ \rateComp ],
				views[ \rateComp ].bounds.moveTo(0,0) )
			.resize_( 5 )
			.scroll_step_( 0.1 )
			.clipLo_( 0 )
			.value_( 1 )
			.action_({ |nb|
				this.performSndFile( \rate_ , nb.value );
				action.value( this );
			});

		views[ \rateSemitones ] = SmoothNumberBox( views[ \rateComp ],
				views[ \rateComp ].bounds.moveTo(0,0) )
			.resize_( 5 )
			.action_({ |nb|
				this.performSndFile( \rate_ , nb.value.midiratio );
				action.value( this );
			})
			.visible_( false );

		views[ \rateMode ] = UPopUpMenu( view, (((bounds.width - 73)/2)-2) @ viewHeight )
		.items_( ['ratio', 'semitones' ] )
		.item_( this.class.rateMode )
		.action_({ |pu| this.class.rateMode = pu.item });

		views[ \loop ] = SmoothButton( view, 40 @ viewHeight )
			.radius_( 2 )
			.resize_( 3 )
		    //.hiliteColor_( RoundView.skin.hiliteColor ?? { Color.black.alpha_(0.33) } )
		    .label_( [ "loop", "loop" ] )
			.action_({ |bt|
				this.performSndFile( \loop_ , bt.value.booleanValue );
				action.value( this );
			});

		this.setFont;
		this.setTimeMode( timeMode );
		this.setRateMode( rateMode );
	}

}

DiskSndFileView : BufSndFileView {

	performSndFile { |selector ...args|
		if( sndFile.notNil ) {
			^sndFile.perform( selector, *args );
		} {
			if( autoCreateSndFile ) {
				this.value = DiskSndFile.newBasic( );
				^sndFile.perform( selector, *args );
			} {
				^nil;
			};
		};
	}
}
