USoundFilePlotWindow {
	var <soundFile, <startFrame, <numFrames, <>action, <window, <sfv;
	var <>canSelect = true;

	*new { |soundFile, startFrame, numFrames, action|
		^super.newCopyArgs( soundFile, startFrame, numFrames, action ).init;
	}

	init {
		numFrames = numFrames ? 0;
		if( soundFile.notNil ) {
			this.makeWindow;
		};
	}

	setPlotRange { |newStartFrame, newNumFrames|
		startFrame = newStartFrame ? startFrame;
		numFrames = newNumFrames ? numFrames;
		{ sfv.setSelection( 0, [ startFrame, numFrames ]); }.defer;
	}

	startFrame_ { |newStartFrame|
		this.setPlotRange( newStartFrame, numFrames );
	}

	numFrames_ { |newNumFrames|
		this.setPlotRange( startFrame, newNumFrames );
	}

	makeWindow {
		var dur, sfZoom, infoView;
		var closeFunc, moveRange, getMoveRange, mouseAction, getMousePos;
		var fillInfoViewIdle;

		RoundView.pushSkin( UChainGUI.skin );

		this.close;

		window = Window(soundFile.path.formatGPath, Rect(200, 200, 850, 400), scroll: false);

		dur = soundFile.numFrames / soundFile.sampleRate;

		window.addFlowLayout( 4@4, 4@4 );

		sfv = SoundFileView( window, window.bounds.insetAll( 4, 4, 4, 40 ) ).resize_(5);
		sfZoom = SmoothRangeSlider( window, (window.bounds.width - 8) @ 14 ).resize_(8);
		infoView = StaticText( window, (window.bounds.width - 92) @ 14 ).resize_(8)
		.applySkin( RoundView.skin );
		SmoothSlider( window, 80 @ 14 )
		.knobSize_(1)
		.resize_(9)
		.action_({ |sl|
			sfv.yZoom = sl.value.linlin(0,1,0,24).dbamp;
		});

		sfZoom.knobSize = 1;
		sfZoom.canFocus = false;

		sfv.bounds = sfv.bounds.insetAll( -4, -4, -4, 0 );

		sfv.soundfile = soundFile;
		sfv.read(0, soundFile.numFrames);

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

		this.setPlotRange;

		fillInfoViewIdle = {
			infoView.string = " dur: % / %".format(
				soundFile.numFrames, soundFile.duration.asSMPTEString(1000)
			);
		};

		fillInfoViewIdle.value;

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

			if( canSelect ) {
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
					(selection[0] / soundFile.sampleRate).asSMPTEString(1000),
					(selection.sum / soundFile.sampleRate).asSMPTEString(1000),
				);
			};
		};

		sfv.mouseDownAction = { |sfv, x, y|
			//var selection = sfv.selection(0);
			//var mousePos = getMousePos.value( sfv, x );
			if( canSelect ) {
				moveRange = getMoveRange.value( sfv, x );
				if( moveRange.notNil ) {
					sfv.setSelectionColor( 0, Color.blue(0.2).alpha_(0.4) );
				};
				mouseAction.value( sfv, x, y );
			};
		};

		sfv.mouseMoveAction = mouseAction;

		window.view.acceptsMouseOver = true;

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
				pos, (pos / soundFile.sampleRate).asSMPTEString(1000)
			);
		};

		sfv.mouseLeaveAction = { |sfv, x, y|
			fillInfoViewIdle.value;
			sfv.timeCursorOn = false;
		};

		sfv.mouseUpAction = {
			sfv.setSelectionColor( 0, Color.blue(0.2).alpha_(0.2) );
		};

		sfv.action = { |vw|
			var selection;
			selection = sfv.selection(0);
			startFrame = selection[0];
			numFrames = selection[1];
			action.value( this, startFrame, numFrames );
		};

		window.front;

		RoundView.popSkin;
	}

	close { if( window.notNil && { window.isClosed.not } ) { window.close }; }
}