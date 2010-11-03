
WFSPlotSmooth {
	classvar <>window;
	classvar <>playButton;
	
	*new { |title = "WFS", bounds, toFront = true, removeButtons = true|
		bounds = bounds ? Rect(128, 64, 400, 400);
		if( this.isOpen )
			{ 	window.onClose.value;
				if( removeButtons )
					{ if(playButton.notNil) { playButton.remove; playButton = nil; };  
					/*
					 window.view.children.do({ |view|
						 if( view.class == RoundButton )
						 	{ view.remove }; });
					*/
				};

					
				window.name = title.asString ++ ":plotSmooth";
				//this.addDragView;
				if( toFront ) { window.front };
			}
			{ window = SCWindow( title.asString ++ ":plotSmooth", bounds ).front;
			  window.view.background_( Color.black );
			  this.addDragView( window );
			  playButton = nil;
			};
		
		^window;
		}
	
	window { ^window }
	
	*addDragView { |theWindow| 
		SCUserView( theWindow, theWindow.view.bounds )
			.resize_(5)
			.canReceiveDragHandler_( { SCView.currentDrag.respondsTo( \plotSmooth ) })
			.receiveDragHandler_( { SCView.currentDrag.plotSmooth; } );
		
		   }
	
	*isOpen { ^( window.notNil && { window.dataptr.notNil } ); }
	*isClosed { ^( window.notNil && { window.dataptr.notNil } ).not; }
	
	*keep { window = nil; } // next *new call will open new window
	
	*close { if( this.isOpen ) { window.close } }
	
	}
	
	
	