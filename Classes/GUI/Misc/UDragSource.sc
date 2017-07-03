UDragSource {
	
	classvar <>mouseSynth;
	
	var <>view;
	var <>beginDragAction;
	
	*initClass {
		Platform.case(
			\osx, {
				SynthDef( "UDragSource_mouseState", {
					var state = MouseButton.kr( 0, 1, 0 );
					FreeSelf.kr( HPZ1.kr( state ) < 0 );
				}).writeOnce
			}
		);
	}
	
	*viewClass { ^DragSource }
	
	*new { |parent, bounds|
		^super.new.view_( this.viewClass.new( parent, bounds ) ).init;
	}
	
	init {
		if( GUI.id == \cocoa ) {
			view.beginDragAction_({ |vw|
				mouseSynth = Synth( 'UDragSource_mouseState' ).onFree({
					{ 
						if( View.currentDrag.notNil ) {
							View.currentDrag = nil; 
							UChainGUI.all.do({ |x| x.view.refresh });
				              UGlobalControlGUI.current !? {|x| x.view.view.refresh };
						};
					}.defer(0.1);
					mouseSynth = nil;
				});
				this.beginDragAction.value( vw );
			});
		} {
			view.beginDragAction_({ |vw|
				this.beginDragAction.value( vw );
			});
		};
	}
	
	applySkin { |skin|
		view.applySkin( skin );
	}
	
	doesNotUnderstand { arg ... args;
		var result = view.perform( *args );
		^if( result === view, { this }, { result }); // be sure to replace view with base
	}
}

UDragBoth : UDragSource {
	
	*viewClass { ^DragBoth }
	
}