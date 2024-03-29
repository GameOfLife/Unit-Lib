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
		var labelWidth, labelSpace;
		this.class.refreshDevices;
		vw = ();
		labelWidth = RoundView.skin.labelWidth ? 120;
		labelSpace = labelWidth + 2;
		if( canCheckDevices ) {
			StaticText( parent, labelWidth @ 14 )
			.string_( "% in " .format( label ) )
			.align_( \right )
			.applySkin( RoundView.skin );

			vw.inPu = StaticText( parent, Rect( labelSpace, 0, bounds.width - labelSpace, 14 ) );

			StaticText( parent, Rect( 0, 18, labelWidth, 14 ) )
			.string_( "% out ".format( label ) )
			.align_( \right )
			.applySkin( RoundView.skin );

			vw.outPu = StaticText( parent, Rect( labelSpace,18, bounds.width - labelSpace, 14) );

			vw.inPu.mouseDownAction_({ |st|
				var actions, selected;

				actions = [
					MenuAction( "system default", {
						vw.inDevice = nil;
						vw.outDevice = nil;
						action.value( vw, this.formatDevice( nil ) );
					}).enabled_( vw.inDevice.notNil );
				];

				actions = actions.addAll(
					inDevices.collect({ |device|
						MenuAction( device.asString, {
							vw.inDevice = device;
							vw.outDevice = this.class.autoSelectOutDevice( device );
							action.value( vw, this.formatDevice([ vw.inDevice, vw.outDevice ]) );
						}).enabled_( vw.inDevice != device );
					})
				);

				selected = actions.detect({ |x| x.enabled.not });

				actions = actions.add( MenuAction.separator );
				actions = actions.add(
					MenuAction( "Add...", {
						SCRequestString( "", "please enter device name:", { |string|
							action.value( vw, this.constrain( string ) );
						});
					})
				);

				actions = [ MenuAction.separator( "device in" ) ] ++ actions;

				Menu( *actions ).front( action: selected );
			});

			vw.outPu.mouseDownAction_({ |st|
				var actions, selected;

				actions = [
					MenuAction( "system default", {
						vw.outDevice = nil;
						action.value( vw, this.formatDevice([ vw.inDevice, nil ]) );
					}).enabled_( vw.outDevice.notNil );
				];

				actions = actions.addAll(
					inDevices.collect({ |device|
						MenuAction( device.asString, {
							vw.outDevice = device;
							action.value( vw, this.formatDevice([ vw.inDevice, vw.outDevice ]) );
						}).enabled_( vw.outDevice != device );
					})
				);

				selected = actions.detect({ |x| x.enabled.not });

				actions = actions.add( MenuAction.separator );
				actions = actions.add(
					MenuAction( "Add...", {
						SCRequestString( "", "please enter device name:", { |string|
							action.value( vw, this.constrain( string ) );
						});
					})
				);

				actions = [ MenuAction.separator( "device out" ) ] ++ actions;

				Menu( *actions ).front( QtGUI.cursorPosition - (20@0), action: selected );
			});

			vw.setDevice = { |vwx, device|
				device = this.unpackDevice( device );
				vw.inDevice = device[0];
				vw.outDevice = device[1];
				vw.inPu.string = " %".format( vw.inDevice ? "system default" );
				vw.outPu.string = " %".format( vw.outDevice ? "system default" );
			};

			vw.doAction = { action.value( vw, this.formatDevice([ vw.inDevice, vw.outDevice ]) ); };

			vw.atAll([\inPu, \outPu]).do({ |pu|
				pu.applySkin( RoundView.skin ).background_( Color.white.alpha_(0.25) );
				pu.setProperty(\wordWrap, false);
				if( resize.notNil ) { pu.resize = resize };
			});
		} {
			vw.views = [
				StaticText( parent, labelWidth @ 14 ).string_( "device in " ).align_( \right ),
				StaticText( parent, Rect( labelSpace, 0, bounds.width - labelSpace, 14 ) ).string_( "jack" ),
				StaticText( parent, Rect( 0, 18, labelWidth, 14 ) ).string_( "device out " ).align_( \right ),
				StaticText( parent, Rect( labelSpace, 18, bounds.width - labelSpace, 14 ) ).string_( "jack" ),
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