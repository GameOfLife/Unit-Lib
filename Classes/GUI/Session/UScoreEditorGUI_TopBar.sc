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

UScoreEditorGui_TopBar {

    var <>scoreView;
    var <header, <>views, <scoreEditorController, <scoreController;
	var <>stringColor;

    *new{ |parent, bounds, scoreView|
        ^super.newCopyArgs(scoreView).init(parent, bounds)
    }

    init{ |parent, bounds|
        this.makeGui(parent, bounds);
        this.addScoreEditorController;
    }

    remove{
        if(scoreEditorController.notNil) {
                scoreEditorController.remove;
        };
        if( scoreController.notNil ) {
	        scoreController.remove;
	    };
        header.remove;
    }

    scoreEditor{
         ^scoreView.currentEditor
    }

    addScoreEditorController{

        if(scoreEditorController.notNil) {
                scoreEditorController.remove;
        };
        scoreEditorController = SimpleController( scoreView.currentEditor );
        scoreEditorController.put(\undo, { this.resetUndoRedoButtons });
        scoreEditorController.put(\redo, { this.resetUndoRedoButtons });

        if( scoreController.notNil ) {
	        scoreController.remove;
	    };
        scoreController = SimpleController( scoreView.currentScore );
        scoreController.put(\something, { this.resetUndoRedoButtons });

    }

    resetUndoRedoButtons{
        views[\redo]
            	.enabled_(this.scoreEditor.redoSize != 0)
            	.stringColor_( [ Color.gray(0.5), stringColor ][ UScoreEditor.enableUndo.binaryValue ] );
        views[\undo]
            	.enabled_(this.scoreEditor.undoSize != 0)
            	.stringColor_( [ Color.gray(0.5), stringColor ][ UScoreEditor.enableUndo.binaryValue ] );
    }

    selectedEvents{
        ^scoreView.selectedEvents;
    }

    selectedEventsOrAll{
        ^scoreView.selectedEventsOrAll
    }

    makeGui{ |parent, bounds|
        var font, size, marginH, marginV;
		var umixer;
		var plusButtonTask;
		views = ();

	    marginH = 2;
	    marginV = 2;
		size = bounds.height - (2*marginV);

        header = CompositeView( parent, bounds );

		font = RoundView.skin !? _.font ?? { Font( Font.defaultSansFace, 11 ) };

		stringColor =  RoundView.skin !? _.stringColor ?? { Color.black };

		header.addFlowLayout(marginH@marginV);
		header.resize_(2);

		SmoothButton( header, size@size )
			.states_( [[ \i, nil, Color.blue.alpha_(0.125) ]] )
			.canFocus_(false)
		    .toolTip_( "Edit selected Event(s)" )
			.action_({ |b|
				scoreView.editSelected;
			});

		header.decorator.shift(10);

		SmoothButton( header, size@size )
			.states_( [[ '-' ]] )
		    .canFocus_(false)
		    .toolTip_( "Delete selected Event(s)" )
			.action_({
				scoreView.deleteSelected;
			});

		SmoothButton( header, size@size )
			.states_( [[ '+' ]] )
			.canFocus_(false)
		    .toolTip_( "Add Event" )
		    .mouseDownAction_({
			    plusButtonTask.stop;
			    if(scoreView.selectedEvents.isNil ) {
		    	    plusButtonTask = {
					    0.5.wait;
					    UMenuBarIDE.allMenus[ 'Edit' ].actions
					    .detect({ |act| act.string == "Add Multiple..." }).menu.front;
					    plusButtonTask = nil;
				    }.fork( AppClock );
			    } {
				    plusButtonTask = nil;
			    }
		    })
			.action_({
			    if(scoreView.selectedEvents.notNil) {
				    scoreView.duplicateSelected;
				} {
				    if( true ) {
					    plusButtonTask !? _.stop;
					    plusButtonTask = nil;
				        scoreView.currentEditor.addEvent
				    };
				}
			});

		SmoothButton( header, size@size )
			.states_( [[ { |bt, rect|
				var square;
				square = Rect.aboutPoint( rect.center,
						rect.width.min( rect.height ) / 5,
						rect.width.min( rect.height ) / 4 );

				Pen.line( square.leftBottom, square.leftTop );
				Pen.lineTo( square.rightTop );
				Pen.lineTo( square.right @ (square.top + (square.height / 2) ) );
				Pen.lineTo( square.left @ (square.top + (square.height / 2) ) );
				Pen.lineTo( square.leftBottom );
				Pen.fillStroke;

			 } ]] )
			.canFocus_(false)
		    .toolTip_( "Add Marker" )
			.action_({
			     scoreView.currentEditor.addMarker
			});

		header.decorator.shift(10);

		SmoothButton( header, size@size  )
 			.states_( [[ "[", nil, Color.clear ]] )
 			.canFocus_(false)
			.radius_( 0 )
			.font_( Font( font.name, 10 ).boldVariant )
			.radius_([9,0,0,9])
		    .toolTip_( "Clip selected Event(s) startTime to score position" )
			.action_({
				this.selectedEventsOrAll !? { |x| this.scoreEditor.trimEventsStartAtPos( x ) }
			});

		header.decorator.shift(-1);

		SmoothButton( header, size@size  )
			.states_( [[ "|", nil, Color.clear ]] )
			.canFocus_(false)
			.radius_(0)
			.font_( Font( font.name, 12 ).boldVariant )
		    .toolTip_( "Slice selected Event(s) at score position" )
			.action_({
				this.selectedEventsOrAll !? { |x| this.scoreEditor.splitEventsAtPos( x ) }
			});

		header.decorator.shift(-1);

		SmoothButton( header, size@size  )
			.states_( [[ "]", nil, Color.clear ]] )
			.canFocus_(false)
			.radius_([0,9,9,0])
			.font_( Font( font.name, 10 ).boldVariant )
		    .toolTip_( "Clip selected Event(s) endTime to score position" )
			.action_({
			    this.selectedEventsOrAll !? { |x| this.scoreEditor.trimEventsEndAtPos( x ) }
		    });

		header.decorator.shift(10);

		views[\undo] = SmoothButton( header, (size * 0.8)@size )
			.states_( [[ 'arrow_pi' ]] )
			.radius_( [1,0,0,1] * (size/2) )
			.canFocus_(false)
			.enabled_(false)
		    .stringColor_( [ Color.gray(0.5), stringColor ][ UScoreEditor.enableUndo.binaryValue ] )
		    .toolTip_( "Undo" )
			.action_({
				this.scoreEditor.undo
			});

		header.decorator.shift(-1);

		views[\redo] = SmoothButton( header, (size * 0.8)@size )
			.states_( [[ 'arrow' ]] )
			.radius_( [0,1,1,0] * (size/2) )
			.canFocus_(false)
			.enabled_(false)
			.stringColor_( [ Color.gray(0.5), stringColor ][ UScoreEditor.enableUndo.binaryValue ] )
		    .toolTip_( "Redo" )
			.action_({
				this.scoreEditor.redo
			});

		header.decorator.shift(-5);

		views[ \disableUndo ] = SmoothButton( header, 9@9 )
			.states_( [ ['+'], ['-'] ] )
			.hiliteColor_( nil )
			.value_( UScoreEditor.enableUndo.binaryValue )
			.canFocus_(false)
		    .toolTip_( "Enable/disable undo" )
			.action_({ |bt|
				switch( bt.value,
					1, {
						UScoreEditor.enableUndo = true;
						this.scoreEditor.changed( \undo );
					},
					0, {
						UScoreEditor.enableUndo = false;
						this.scoreEditor.clearUndo;
						this.scoreEditor.changed( \undo );
					}
				);
			});

		header.decorator.shift(10);

		SmoothButton( header, size@size  )
			.states_( [[ \speaker, nil, Color.clear ]] )
			.canFocus_(false)
		    .toolTip_( "Enable/disable selected Event(s)" )
			.action_({ |b|
				this.selectedEvents !? { |x|  this.scoreEditor.toggleDisableEvents( x ) }
			});

		SmoothButton( header, size@size  )
			.states_( [[ \lock, nil, Color.clear ]] )
			.canFocus_(false)
		    .toolTip_( "Lock startTime of selected Event(s)" )
			.action_({ |b|
				this.selectedEvents !? { |x|  this.scoreEditor.toggleLockEvents( x ) }
			});

		header.decorator.shift(10);

		SmoothButton( header, 40@size  )
		.states_( [[ "folder", nil, Color.clear ]] )
			.canFocus_(false)
		    .radius_( [1,0,0,1] * (size/2) )
		    .toolTip_( "Pack selected Event(s) in to Folder Event" )
			.action_({
			    this.selectedEvents !? { |x|
                        	this.scoreEditor.folderFromEvents(x);
				}
			});

		SmoothButton( header, 40@size  )
			.states_( [[ "unfold", nil, Color.clear ]] )
			.canFocus_(false)
		    .radius_( [0,1,1,0] * (size/2) )
		    .toolTip_( "Unpack selected Folder Event(s)" )
			.action_({
			    this.selectedEvents !? { |x|
                        	this.scoreEditor.unpackSelectedFolders(x)
			     }
			});

		header.decorator.shift(10);

		SmoothButton( header, 40@size  )
			.states_( [[ "mixer", nil, Color.clear ]] )
			.canFocus_(false)
	        .toolTip_( "Open Mixer" )
			.action_({ |b|
			    if( umixer.notNil && { umixer.parent.isClosed.not } ) {
				   umixer.parent.front;
			    } {
				   umixer = UMixer(scoreView.currentScore);
			    }
			}).onClose_({
			    if( umixer.notNil && { umixer.parent.isClosed.not } ) {
			        umixer.parent.close
		        };
		   });

		header.decorator.shift( header.decorator.indentedRemaining.width - (100 + size), 0 );

		StaticText( header, 30@size ).string_( "snap:" ).font_( font ).align_( \right )
			.resize_(3);

		{
			var snapValues, snapLabels, getLabel, setString, snapView;
			snapValues = (1/[inf,
				(ULib.servers[0].options.sampleRate ? 44100) / ULib.servers[0].options.blockSize,
				1000,100,10,32,16,12,8,6,5,4,3,2,1]
			);
			snapLabels = [
				"off", "cf",
				"0.001", "0.01", "0.1",
				"1/32", "1/16", "1/12", "1/8", "1/6", "1/5", "1/4", "1/3", "1/2", "1"
			];
			getLabel = {
				var index;
				index = snapValues.indexOfEqual( scoreView.snapH );
				if( index.notNil ) {
					snapLabels[ index ]
				} {
					scoreView.snapH.asStringWithFrac(3);
				}
			};
			setString = {
				snapView.string = " % / %".format(
					getLabel.value,
					scoreView.usessionMouseEventsManager !? _.mode ? "all"
				);
			};
			snapView = StaticText( header, 60@size )
			.font_( font )
			.align_( \center )
			.toolTip_( "Snap resolution and mode" )
			.applySkin( RoundView.skin )
			.background_( Color.white.alpha_(0.25) )
			.mouseDownAction_({
				Menu(
					Menu(
						*snapLabels.collect({ |item, i|
							MenuAction( item, {
								scoreView.snapH = snapValues[ i ];
								scoreView.snapActive = scoreView.snapH != 0;
								setString.value;
							}).enabled_( getLabel.value != item );
						})
					).title_( "Resolution" ),
					Menu(
						*[ 'all','move','resize','fades'] .collect({ |item, i|
							MenuAction( item.asString, {
								scoreView.usessionMouseEventsManager.mode = item;
								setString.value;
							}).enabled_( scoreView.usessionMouseEventsManager.mode != item );
						})
					).title_( "Mode" ),
				).front;
			})
			.resize_(3);

			snapView.setProperty(\wordWrap, false);
			snapView.bounds = snapView.bounds.insetBy(0,1);
			setString.value;
		}.value;


		SmoothButton( header, size@size )
			.label_( "Q" )
			.resize_(3)
			.canFocus_(false)
		    .toolTip_( "Quantize startTime of selected Event(s) to snap resolution" )
			.action_({
				this.selectedEvents !? { |x|
					this.scoreEditor.quantizeEvents(
						x, scoreView.snapH, scoreView.showTempoMap
					)
				} ?? {
					this.scoreEditor.quantizePos( scoreView.snapH, scoreView.showTempoMap );
				};
			});
    }

}
