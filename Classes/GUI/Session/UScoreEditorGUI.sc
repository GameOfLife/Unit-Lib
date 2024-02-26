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

UScoreEditorGUI : UAbstractWindow {

	var <scoreEditor;

	var <>scoreView, <tranportBar, topBar;
	var <usessionMouseEventsManager;
	var <scoreController;
	var <>askForSave = true;

	//*initClass { UI.registerForShutdown({ scoreEditor.askForSave = false }); }

	*initClass {
		CmdPeriod.add( this );
	}

	*cmdPeriod {
		this.all.do({ |item|
			item.tranportBar.views.active.value = 0;
		});
	}

	*new { |scoreEditor, bounds, parent|
		^super.new.init( scoreEditor )
			.addToAll
			.makeGui(bounds, parent)
	}

	*currentSelectedEvents{
	    ^this.current.selectedEvents
	}

	//gives a flat array
	*currentSelectedChains{
		^this.currentSelectedEvents.collect{ |ev|
			ev.getAllUChains
		}.flat
	}

	*selectedEventsDo{ |f|
		this.currentSelectedEvents.collect{ |ev|
			ev.allEvents
		}.flat.do(f)
	}

	*selectedChainsDo{ |f|
		this.currentSelectedChains.do(f)
	}

	*currentInnerScore{
		^UScoreView.current !? { |x| x.scoreList.last }
	}

    init { |inScoreEditor|
		scoreEditor = if(inScoreEditor.isKindOf(UScore)) {
            UScoreEditor(inScoreEditor)
        } {
            inScoreEditor;
        };
        scoreController = SimpleController(scoreEditor.score);
        scoreController.put(\name,{
            window.name = this.windowTitle
        });

        scoreController.put(\something,{
            { window.name = this.windowTitle }.defer;
        });

    }

	score { ^scoreEditor.score }
	editor { ^scoreEditor }
	currentScore { ^scoreEditor.currentScore }
	currentEditor { ^scoreEditor.currentEditor }
	selectedEvents{ ^scoreView.selectedEvents }

    windowTitle {
	    var dur;
	    dur = this.score.duration;
	    if( dur == inf ) {
		  	^("Score Editor : "++ this.score.name ++ " (infinite)" );
		} {
			^("Score Editor : "++ this.score.name ++ " (" ++ dur.asSMPTEString(1000) ++ ")" );
		};
    }

    remove {
        scoreController.remove;
    }
	makeGui { |bounds, parent|

		var font = Font( Font.defaultSansFace, 11 ), header, windowTitle, margin, gap, topBarH, tranBarH, centerView, centerBounds;
		var menuH, menuView;

        margin = 4;
        gap = 2;

        if( bounds.isNil ) {
	        if( this.score.notNil ) {
		       bounds = this.score.displayBounds;
	        };
        };

        this.newWindow(bounds, this.windowTitle,{

            if(UScoreEditorGUI.current == this) {
                UScoreEditorGUI.current = nil
            };
            this.remove;
            topBar.remove;
            scoreView.remove;
            tranportBar.remove;
            {
                if( (this.score.events.size != 0) && (this.score.isDirty) && askForSave ) {
                    SCAlert( "Do you want to save your score? (" ++ this.score.name ++ ")" ,
                        [ [ "Don't save" ], [ "Cancel" ], [ "Save" ],[ "Save as"] ],
						[ 	{ if( UScore.storeRecentScorePaths ) { { URecentScorePaths.fillMenu }.defer(0.1) } },
                            { UScoreEditorGUI(scoreEditor) },
                            { this.score.save(nil, {UScoreEditorGUI(scoreEditor)} ) },
                            { this.score.saveAs(nil,nil, {UScoreEditorGUI(scoreEditor)} ) }
					], background: UChainGUI.skin[ 'SCAlert' ] !? _.background  );
                } {
					if( UScore.storeRecentScorePaths ) { { URecentScorePaths.fillMenu }.defer(0.1) }
				};
            }.defer(0.1)
        }, UChainGUI.skin.scoreEditorWindow, margin:margin, gap:gap);
        view.addFlowLayout(margin@margin,gap@gap);
        bounds = window.bounds;
        margin = 4;
        gap = 2;
        topBarH = 22;
        tranBarH = 22;
		menuH = 18;

		view.minWidth = 675;
		view.minHeight = 150;

		if( UChainGUI.skin == UChainGUI.skins.light ) {
			RoundView.pushSkin( UChainGUI.skin ++ (
				SmoothButton: (
					border: 0.75,
					background:  Gradient( Color.gray(0.95), Color.gray(0.7), \v ),
					hiliteColor: Color.green.alpha_(0.5),
				),
				SmoothSimpleButton: (
					border: 0.75,
					background:  Gradient( Color.gray(0.95), Color.gray(0.7), \v ),
					hiliteColor: Color.green.alpha_(0.5),
				),
				PopUpMenu: (
					background: Color.gray( 0.8 ),
				)
			) );
		} {
			RoundView.pushSkin( UChainGUI.skin );
		};

		if( UMenuBarIDE.hasMenus ) {
			menuView = UMenuBarIDE.createMenuStrip( view, (bounds.width-8) @ menuH, [-4,-4,-4,0] );
		};

        centerBounds = Rect(0,0, bounds.width-8, bounds.height-( topBarH + tranBarH + menuH + (2*margin) + (3*gap) ));
        //centerView = CompositeView(view, centerBounds).resize_(5);
        scoreView = UScoreView(view, centerBounds, scoreEditor );

        //TOP
        topBar = UScoreEditorGui_TopBar(view,Rect(0,0, bounds.width-(2*margin), topBarH ),scoreView);
        view.decorator.nextLine;

        //CENTER
        scoreView.makeView;
        view.decorator.nextLine;

        //BOTTOM
        tranportBar = UScoreEditorGui_TransportBar(view,  Rect(0,0, bounds.width - (2*margin), tranBarH ), scoreView);

		RoundView.popSkin;

        if( UScore.storeDisplayBounds ) {
	        window.drawFunc = {
		        this.score.displayBounds = window.bounds;
	        };
       };
	}
}

+ UScore {
	gui { |bounds|
		^UScoreEditorGUI( UScoreEditor( this ), bounds );
	}
}	