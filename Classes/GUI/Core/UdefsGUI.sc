UdefsGUI {
	
	classvar <>current;
	
	var <view, <composites, <udefView, <umapDefView1, <umapDefView2;
	
	*new { |parent, bounds, makeCurrent = true|
		if( parent.isNil && { current.notNil && { current.view.isClosed.not } } ) {
			^current.front;
		} {
			^super.new.init( parent, bounds ).makeCurrent( makeCurrent );
		};
	}
	
	makeCurrent { |bool| if( bool == true ) { current = this } }
	
	front { view.findWindow.front }
	
	init { |parent, bounds|
		if( parent.notNil ) {
			bounds = bounds ?? { parent.bounds.moveTo(0,0).insetBy(4,4) };
		} {
			bounds = bounds ? Rect(
				Window.screenBounds.width - 705, 
				Window.screenBounds.height - 850, 
				600, 700
			);
		};
		
		view = EZCompositeView( parent ? "Udefs", bounds, true, 0@0, 0@0 ).resize_(5);
		bounds = view.bounds;
		view.onClose_({ 
			if( current == this ) { current = nil };
		});
		
		composites = 3.collect({ |i|
			 CompositeView( view, (bounds.width/3).floor @ (bounds.height) ).resize_(4)
		});
		
		udefView = UdefListView( composites[0] );
		umapDefView1 = UMapDefListView( composites[1], filter: \dynamic );
		umapDefView2 = UMapDefListView( composites[2], filter: \static );
	}
}