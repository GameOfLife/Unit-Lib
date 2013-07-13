UGlobalControlKeySpec : Spec { 
	
	*new {
		^super.newCopyArgs();
	}
	
	map { |in| ^in }
	unmap { |in| ^in }
	
	constrain { |key|
		key = key.asSymbol;
		if( key.notNil && { UGlobalControl.current.keys.any({ |item| item == key }).not }) {
			UGlobalControl.current.put( key, 0.5 );
			"added '%' to UGlobalControl\n".postf( key );
		};
		^key;
	}
	
	default { ^UGlobalControl.current.keys[0] }
	
	makeView { |parent, bounds, label, action, resize|
		var multipleActions = action.size > 0;
		var vw;
		var ctrl;
		var fillPopUp, keys;
		vw = EZPopUpMenu( parent, bounds, label !? { label.asString ++ " " });
		fillPopUp = {
			if( keys != UGlobalControl.current.keys ) {				keys = UGlobalControl.current.keys.copy;
				vw.items = UGlobalControl.current.keys.collect({ |key|
					key -> { |vw| action.value( vw, key ) }
				}) ++ [
				     '' -> { },
					'add...' -> { |vw| 
						SCRequestString( "", "please enter key name:", { |string|
							action.value( vw, this.constrain( string.asSymbol ) );
						})
					}
				];
			}
		};
		fillPopUp.value;
		ctrl = { { fillPopUp.value }.defer; };
		UGlobalControl.addDependant( ctrl );
		vw.onClose_({ UGlobalControl.removeDependant( ctrl ); });
		vw.labelWidth = 80; // same as EZSlider
		vw.applySkin( RoundView.skin ); // compat with smooth views
		if( resize.notNil ) { vw.view.resize = resize };
		^vw
	}
	
	setView { |view, value, active = false|
		{  // can call from fork
			value = this.constrain( value );
			view.value = view.items.collect(_.key).indexOf( value ) ? 0;
			if( active ) { view.doAction };
		}.defer;
	}
	
	mapSetView { |view, value, active = false|
		{  // can call from fork
			view.value = view.items.collect(_.key).indexOf( value ) ? 0;
			if( active ) { view.doAction };
		}.defer;
	}

	
}