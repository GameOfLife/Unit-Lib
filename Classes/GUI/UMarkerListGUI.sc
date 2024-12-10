UMarkerListGUI {

	var <score, <window, <views;
	var <markerDict, <current;
	var <>onClose;

	*new { |score, bounds|
		^super.newCopyArgs( score ).init( bounds );
	}

	init { |bounds|
		this.makeWindow( bounds );
	}

	closeWindow {
		if( window.notNil && { window.isClosed.not }) {
			window.close;
			window = nil;
		};
	}

	findScoreEditor {
		^UScoreEditorGUI.all.detect({ |item| item.score == score });
	}

	makeWindow { |bounds|
		var t, transportView;
		this.closeWindow;
		window = Window( score.name, bounds ?? { Rect(56.0, 319.0, 815.0, 700.0) } ).front;
		views = ();

		this.findScoreEditor !? _.askForSave_( false );

		window.asView.addFlowLayout( 0@0, 4@4 );
		window.asView.decorator.shift(4,4);

		transportView = UTransportView( score, window.asView, 30 );
		transportView.views.counter.canFocus_( false );

		window.onClose = {
			transportView.remove;
			this.findScoreEditor !? { |gui|
				gui.askForSave = true;
				gui.window.front
			} ? score.gui;
			onClose.value( this );
		};

		views[ \transport ] = transportView;

		SmoothButton(window,30@15)
		.states_([[\up,Color.black,Color.clear]])
		.border_(1).background_(Color.grey(0.8))
		.radius_(5)
		.canFocus_(false)
		.action_({
			var gui;
			gui = this.findScoreEditor;
			if( gui.notNil ) {
				gui.window.front;
			} {
				score.gui.askForSave_( false );
			};
		});

		t = TreeView( window, window.bounds.insetAll(0,0,0,38).moveTo(0,0) ).resize_(5);
		t.columns = ["id", "name", "time", "progress"];
		t.setColumnWidth( 0, 80 );
		t.setColumnWidth( 1, 300 );
		t.font = Font( Font.defaultSansFace, 16 );
		views[ \treeView ] = t;

		this.fillMarkers;
	}


	setItem {
		var which;
		which = markerDict.values.detect({ |item|
			(item.endTime ? inf) > (score.pos + 0.0001);
		});
		if( current.isNil or: { current != (which !? _.index) } ) {
			current = (which !? _.index);
			{ views.treeView.currentItem = which !? _.treeItem; }.defer;
		};
	}

	fillMarkers {
		var allMarkers, ctrl;

		markerDict = OEM();
		allMarkers = score.events
		.select({ |event|
			event.isKindOf( UMarker );
		}).sort({ |a,b| a.startTime <= b.startTime; });

		allMarkers = allMarkers.select({ |marker|
			marker.autoPause == true or: {
				marker.startTime == 0;
			} or: {
				marker == allMarkers.last;
			};
		});

		allMarkers.do({ |marker,i|
			var id, name, t, view, treeItem, slider, setProgressFunc;
			var next = allMarkers[i+1], color;
			id = marker.name.split( $ ).first;
			name = marker.name.split( $ )[1..].join(" ");
			view = View();
			color = (marker.displayColor !? _.copy ?? { Color.yellow }).alpha_(0.75);
			treeItem = views.treeView
			.addItem([ id, name, marker.startTime.asSMPTEString(1000), "" ])
			.setColor(0, color)
			.setColor(1, color)
			.setColor(2, color)
			.setView( 3, view );

			markerDict.put( marker, (
				id: id,
				name: name,
				view: view,
				marker: marker,
				startTime: marker.startTime,
				endTime: next !? _.startTime,
				treeItem: treeItem,
				index: i
			));

			{
				slider = SmoothSlider( view, view.bounds.moveTo(0,0).insetBy(1,1) )
				.resize_( 5 )
				.knobSize_( 0 )
				.canFocus_( false );

				if( next.notNil ) {
					slider.action_({ |sl|
						if( score.playState == \stopped ) {
							score.pos = sl.value.linlin(0,1,marker.startTime, next.startTime)
						};
					});
				};

				setProgressFunc = {
					var val;
					if( next.notNil ) {
						val = score.pos.linlin( marker.startTime, next.startTime, 0, 1 );
						if( slider.value != val ) { slider.value = val; };
					};
				};

				markerDict[ marker ][ \slider ] = slider;
				markerDict[ marker ][ \setProgress ] = setProgressFunc;

			}.defer(0.1);
		});

		views.treeView.onItemChanged = { |tv|
			var currentTV, dict;
			currentTV = tv.currentItem;
			if( score.playState == \stopped ) {
				dict = markerDict.values.detect({ |item|
					item.treeItem == currentTV;
				});
				if( dict.notNil ) {
					if( score.pos < dict.startTime or: { score.pos >= (dict.endTime ? inf) } ) {
						score.pos = dict.startTime;
					};
				};
			} {
				if( current.notNil && {
					markerDict.values[ current ].treeItem != currentTV
				}) {
					tv.currentItem = markerDict.values[ current ].treeItem;
				};
			}
		};

		views.treeView.keyDownAction = { |vw, which|
			switch( which,
				$ , { score.playAnyway; },
				$., { score.stop; },
				$,, { score.playAnyway; },
			);
		};

		ctrl = SimpleController( score );
		ctrl.put( \pos, {
			markerDict.do({ |item|
				item[ \setProgress ].value;
			});
			this.setItem;
		});

		views.treeView.onClose = { ctrl.remove };
	}

}