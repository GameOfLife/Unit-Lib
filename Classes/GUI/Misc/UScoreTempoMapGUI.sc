UScoreTempoMapGUI {
	
	classvar <>current;
	
	var <parent, <barMapGUI, <tempoMapGUI;
	
	*new { |parent, score, openNew = false|
		if( openNew.not && { current.notNil }) {
			current.score = score;
			current.parent.asView.findWindow.front;
			^current;
		} {
			^super.new.init( parent, score ).makeCurrent;
		};
	}
	
	init { |inParent, score|
		if( inParent.isNil ) {
			parent = Window( bounds: Rect(530, 580, 390, 140) ).front;
			parent.addFlowLayout;
		} {
			parent = inParent;
		};
		this.makeViews( score );
	}
	
	makeCurrent { current = this }
	
	close { 
		var win;
		win = parent.asView.findWindow;
		if( win.notNil && { win.isClosed.not } ) {
			win.close; 
		};
	}
	
	score_ { |score|
		barMapGUI.barMap = score.tempoMap.barMap;
		barMapGUI.action = { score.changed( \pos ) };
		tempoMapGUI.tempoMap = score.tempoMap;
		tempoMapGUI.action = { score.changed( \pos ) };
	}
	
	makeViews { |score|
		if( parent.class == Window.implClass ) {
			parent.name = "TempoMap : %".format( score.name );
		};
		parent.onClose = {
			if( current == this ) {
				current = nil;
			};
		};
		RoundView.useWithSkin( UChainGUI.skin, {
			barMapGUI = BarMapGUI( parent, score.tempoMap.barMap, { score.changed( \pos ) } );
			tempoMapGUI = TempoMapGUI( parent, score.tempoMap, { score.changed( \pos ) } );
		});
	}
	
}