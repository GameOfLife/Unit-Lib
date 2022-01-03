UAudioDeviceSpec : Spec {

	/*
	a device can be:
	- String (same name for input and output device)
	- Array[ String, String ] (input, output)
	- nil (system default)

	GUI will try to match output to input device, unless the
	user sets it manually.
	*/

	classvar <inDevices, <outDevices;
	classvar <>canCheckDevices = true;

	var default;

	viewNumLines { ^2 }

	*initClass {
		inDevices = [];
		outDevices = [];
		if( thisProcess.platform.name == \linux ) { canCheckDevices = false };
	}

	*new { |default|
		this.addDevice( default );
		^super.newCopyArgs(default);
	}

	*addDevice { |device|
		if( device.isString ) { // format: [ <inDevice>, <outDevice> ]
			device = device.dup;
		};
		if( device.notNil ) {
			this.addInDevices([ device[0] ]);
			this.addOutDevices([ device[1] ]);
		};
	}

	*addInDevices { |addInDevices|
		var added = false;
		addInDevices.do({ |item|
			if( item.notNil && { inDevices.any({ |device| item == device }).not } ) {
				inDevices = inDevices.add( item );
				added = true;
			};
		});
		if( added ) { this.inDevices = inDevices };
	}

	*addOutDevices { |addOutDevices|
		var added = false;
		addOutDevices.do({ |item|
			if( item.notNil && { outDevices.any({ |device| item == device }).not } ) {
				outDevices = outDevices.add( item );
				added = true;
			};
		});
		if( added ) { this.outDevices = outDevices };
	}

	*inDevices_ { |newInDevices|
		inDevices = newInDevices;
		this.changed( \inDevices );
	}

	*outDevices_ { |newOutDevices|
		outDevices = newOutDevices;
		this.changed( \outDevices );
	}

	*refreshDevices {
		if( canCheckDevices ) {
			this.addInDevices( ServerOptions.inDevices );
			this.addOutDevices( ServerOptions.outDevices );
		};
	}

	*checkDevice { |device|
		^if( canCheckDevices ) {
			if( device.isString ) { device = device.dup };
			if( device.notNil ) {
				ServerOptions.inDevices.any({ |item| item == device[0] }) &&
				ServerOptions.outDevices.any({ |item| item == device[1] })
			} {
				true;
			};
		} {
			true;
		}
	}

	*compareString { |str, other|
		var matrix, upper, corner, compareFunc;
		if( str.respondsTo( \editDistance ) ) {
			^str.editDistance( other );
		} {
			// This is the same algorithm as the primitive, just in
			// sclang to allow equality
			matrix = Array.iota(other.size + 1);

			if(str.isEmpty || other.isEmpty) {
				^str.size;
			};

			// use identity if not given another way to compare
			compareFunc = compareFunc ? { |a, b| a === b; };

			str.size.do { |indX|
				corner = indX;
				matrix[0] = indX + 1;

				other.size.do { |indY|
					upper = matrix[indY + 1];

					matrix[indY + 1] = if(compareFunc.value(str.at(indX), other.at(indY))) {
						corner;
					} {
						[upper, corner, matrix[indY]].minItem + 1;
					};

					corner = upper;
				};
			};

			^matrix[other.size];
		};
	}

	*autoSelectOutDevice { |inDevice|
		^if( inDevice.isNil ) {
			nil
		} {
			outDevices[
				outDevices.collect({ |item| this.compareString( item, inDevice ); }).minIndex
			];
		};
	}

	map { |in| ^in }
	unmap { |in| ^in }

	unpackDevice { |device|
		if( device.isString or: device.isNil ) { device = device.dup };
		^device;
	}

	formatDevice { |device|
		^case { device.isString } {
			device;
		} { device.isArray } {
			if( device[0] == device[1] ) {
				device[0];
			} {
				device;
			}
		}; // nil if device == nil
	}

	constrain { |device|
		if( this.class.checkDevice( device ).not ) {
			"AudioDeviceSpec:constrain - device '%' does not exist on this machine.\n\tThe server will use the system default device instead\n"
				.postf( device )
		};
		this.class.addDevice( device );
		^device;
	}

	makeView { |parent, bounds, label, action, resize|
		var multipleActions = action.size > 0;
		var vw;
		var ctrl;
		var fillPopUps;
		this.class.refreshDevices;
		vw = ();
		if( canCheckDevices ) {
			vw.inPu = EZPopUpMenu( parent, bounds.width @ 14, label !? { label.asString ++ " in " });
			vw.outPu = EZPopUpMenu( parent, Rect(0,18, bounds.width,14), label !? { label.asString ++ " out " });
			vw.fillInPu = {
				vw.inPu.items = [
					'system default' -> { |pu|
						vw.inDevice = nil;
						vw.outDevice = nil;
						action.value( vw, this.formatDevice( nil ) );
					}
				] ++ inDevices.collect({ |device|
					device.asSymbol -> { |pu|
						vw.inDevice = device;
						vw.outDevice = this.class.autoSelectOutDevice( device.postln ).postln;
						action.value( vw, this.formatDevice([ vw.inDevice, vw.outDevice ]) );
					}
				}) ++ [
					'' -> { },
					'add...' -> { |pu|
						SCRequestString( "", "please enter device name:", { |string|
							action.value( vw, this.constrain( string ) );
						})
					}
				];
			};
			vw.fillOutPu = {
				vw.outPu.items = [
					'system default' -> { |pu|
						vw.outDevice = nil;
						action.value( vw, this.formatDevice([ vw.inDevice, nil ]) );
					}
				] ++ outDevices.collect({ |device|
					device.asSymbol -> { |pu|
						vw.outDevice = device;
						action.value( vw, this.formatDevice([ vw.inDevice, vw.outDevice ]) );
					}
				}) ++ [
					'' -> { },
					'add...' -> { |pu|
						SCRequestString( "", "please enter device name:", { |string|
							action.value( vw, this.constrain( [ vw.inDevice, string ] ) );
						})
					}
				];
			};
			vw.setDevice = { |vwx, device|
				device = this.unpackDevice( device );
				vw.inDevice = device[0];
				vw.outDevice = device[1];
				vw.inPu.value = vw.inPu.items.collect(_.key)
				.indexOf( vw.inDevice.asSymbol ) ? 0;
				vw.outPu.value = vw.outPu.items.collect(_.key)
				.indexOf( vw.outDevice.asSymbol ) ? 0;
			};
			vw.fillInPu;
			vw.fillOutPu;
			ctrl = SimpleController( this.class )
			.put( \inDevices, {
				{ vw.fillInPu }.defer;
			})
			.put( \outDevices, {
				{ vw.fillOutPu }.defer;
			});
			vw.doAction = { vw.outPu.doAction; };
			vw.inPu.onClose_({ ctrl.remove });
			vw.atAll([\inPu, \outPu]).do({ |pu|
				pu.labelWidth = 80; // same as EZSlider
				pu.applySkin( RoundView.skin );
				if( resize.notNil ) { pu.view.resize = resize };
			});
		} {
			vw.views = [
				StaticText( parent, 120 @ 14 ).string_( "device in " ).align_( \right ),
				StaticText( parent, Rect( 124, 0, bounds.width - 124, 14 ) ).string_( "jack" ),
				StaticText( parent, Rect( 0, 18, 120, 14 ) ).string_( "device out " ).align_( \right ),
				StaticText( parent, Rect( 124, 18, bounds.width - 124, 14 ) ).string_( "jack" ),
			];
			vw.views.do(_.applySkin( RoundView.skin ));
		};
		^vw
	}

	setView { |view, value, active = false|
		{  // can call from fork
			value = this.constrain( value );
			view.setDevice( value );
			if( active ) { view.doAction };
		}.defer;
	}

	mapSetView { |view, value, active = false|
		{  // can call from fork
			view.setDevice( value );
			if( active ) { view.doAction };
		}.defer;
	}


}