USoundFilePlotWindow {
	var <soundFile, <startFrame, <numFrames, <>action, <window, <sfv, <uvw;
	var <slices, <selectedSlices, <slicesSection, <>slicesAction;
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
		{
			sfv.setSelection( 0, [ startFrame, numFrames ]);
			uvw.refresh;
		}.defer;
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

		uvw = UserView( window, window.bounds.insetAll( 4, 4, 4, 40 ) ).resize_(5);
		uvw.background = Color.gray(0.6);
		uvw.bounds = uvw.bounds.insetAll( -4, -4, -4, 0 );
		window.asView.decorator.shift( 0, (window.bounds.height - 40).neg );

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
			.scrollTo(rangeStart / divisor);
			uvw.refresh;
		});

		sfv.background = Color.clear;
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

		this.prFillSliceView;
		this.showSliceView;

		window.front;

		RoundView.popSkin;
	}

	showSliceView {
		if( slices.size == 0 ) {
			if( sfv.bounds.top != 0 ) {
				sfv.bounds = sfv.bounds.insetAll(0,-28,0,0);
			};
		} {
			if( sfv.bounds.top != 28 ) {
				sfv.bounds = sfv.bounds.insetAll(0,28,0,0);
			};
		};
	}

	prFillSliceView {
		var savedSlices, clickedAt;

		slicesSection = [0,0];
		slices = slices ? [];

		uvw.drawFunc = { |vw|
			var bounds, scale, width, height, left, right, numFrames, frameToX;
			var slicePos;

			numFrames = sfv.numFrames;
			scale = numFrames / sfv.viewFrames;
			width = vw.bounds.width;
			height = vw.bounds.height;
			left = (sfv.scrollPos * ((width * scale) - width )).neg;
			right = left + (width * scale);

			frameToX = { |frame| frame.linlin(0, numFrames, left, right ); };

			Pen.width = 2;
			Pen.color = Color.yellow;
			slicePos = slices.collect( frameToX );
			slicePos.do({ |x, i|
				Pen.line( x @ 0, x @ height );
			});
			Pen.stroke;
			slicePos.do({ |x, i|
				Pen.line( x @ 0, (x + 14) @ 14 );
				Pen.lineTo( x @ 28 );
				Pen.lineTo( (x - 14) @ 14 );
				Pen.lineTo( x @ 0 );
			});
			Pen.fill;

			Pen.color = Color.yellow.blend( Color.red, 0.5 );
			slicePos.do({ |x, i|
				if( selectedSlices.asCollection.includes(i) ) {
					Pen.line( x @ 28, (x + 14) @ 14 );
					Pen.lineTo( x @ 0 );
					Pen.lineTo( (x - 14) @ 14 );
					Pen.lineTo( x @ 28 );
					Pen.lineTo( x @ height );
				};
			});
			Pen.stroke;

			Pen.color = Color.black;
			Pen.font = Font( Font.defaultSansFace, 11 );
			slicePos.do({ |x, i|
				Pen.stringCenteredIn( i.asString, Rect( x - 14, 0, 28, 28) );
			});

			Pen.color = Color.white.alpha_(0.5);
			Pen.fillRect(
				Rect.fromPoints(
					frameToX.( slicesSection[0] ) @ 0,
					frameToX.( slicesSection[1] ) @ 28
				)
			);
		};

		uvw.mouseDownAction = { |vw, x, y, mod|
			var bounds, scale, width, height, left, right, numFrames, frameToX;
			var clickedSlice;

			numFrames = sfv.numFrames;
			scale = numFrames / sfv.viewFrames;
			width = vw.bounds.width;
			height = vw.bounds.height;
			left = (sfv.scrollPos * ((width * scale) - width )).neg;
			right = left + (width * scale);

			frameToX = { |frame| frame.linlin(0, numFrames, left, right ); };

			savedSlices = slices.copy;
			clickedAt = nil;

			slicesSection = x.linlin( left, right, 0, numFrames ).dup;

			clickedSlice = slices.detectIndex({ |item|
				var itemPos;
				itemPos = frameToX.( item );
				(itemPos @ 14).dist( x@y ) <= 14;
			});

			if( mod.isShift ) {
				if( clickedSlice.notNil ) {
					if( selectedSlices.asCollection.includes( clickedSlice ).not ) {
						selectedSlices = selectedSlices.asCollection.add( clickedSlice );
					} {
						selectedSlices = selectedSlices.asCollection.select(_ != clickedSlice );
					};
				};
			} {
				if( clickedSlice.notNil ) {
					if( selectedSlices.size <= 1 or: { selectedSlices.includes( clickedSlice ).not }) {
						selectedSlices = [ clickedSlice ];
					};
					clickedAt = x;
				} {
					selectedSlices = nil;
				};
			};
			vw.refresh;
		};

		uvw.mouseMoveAction = { |vw, x, y, mod|
			var bounds, scale, width, height, left, right, numFrames, frameToX;
			var selection, sortedSection;

			numFrames = sfv.numFrames;
			scale = numFrames / sfv.viewFrames;
			width = vw.bounds.width;
			height = vw.bounds.height;
			left = (sfv.scrollPos * ((width * scale) - width )).neg;
			right = left + (width * scale);

			frameToX = { |frame| frame.linlin(0, numFrames, left, right ); };

			if( selectedSlices.size > 0 && { clickedAt.notNil }) {
				selection = savedSlices[ selectedSlices ];
				selection = selection + ((x - clickedAt) / (width / sfv.viewFrames));
				selection = selection.round(1).asInteger;
				if( selection.any( _ < 0 ) ) { selection = selection - selection.minItem };
				if( selection.any( _ >= numFrames) ) { selection = selection - ( selection.maxItem - numFrames ) };
				selectedSlices.do({ |item, i|
					slices.put( item, selection[i] );
				});
				this.changed( \slices, slices );
			} {
				slicesSection[1] = x.linlin(left, right, 0, numFrames);
				sortedSection = slicesSection.copy.sort;
				selectedSlices = slices.selectIndices({ |item| item.inclusivelyBetween( *sortedSection ) });
			};
			vw.refresh;
		};

		uvw.mouseUpAction = { |vw, x, y, mod|
			var selected;
			if( clickedAt.notNil ) {
				selected = slices[ selectedSlices ].asCollection;
				slices = slices.sort;
				selectedSlices = selected.collect({ |item| slices.indexOf( item ) });
				clickedAt = nil;
				this.changed( \slices, slices );
				slicesAction.value( this, slices );
			};
			slicesSection = [0,0];
			vw.refresh;
		};
	}

	slices_ { |newSlices|
		slices = newSlices.asCollection;
		this.changed( \slices, slices );
		{ uvw.refresh; this.showSliceView }.defer;
	}

	close { if( window.notNil && { window.isClosed.not } ) { window.close }; }
}