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
		this.class.addDevice( device );
		^device;
	}

	makeView { |parent, bounds, label, action, resize|
		var multipleActions = action.size > 0;
		var vw;
		var ctrl;
		var fillPopUps;
		var labelWidth, labelSpace, extraMenuActions;
		var font;
		this.class.refreshDevices;
		vw = ();
		labelWidth = RoundView.skin.labelWidth ? 120;
		labelSpace = labelWidth + 2;
		if( canCheckDevices ) {

			font = RoundView.skin.font ?? { Font( Font.defaultSansFace, 11 ) };

			StaticText( parent, labelWidth @ 14 )
			.string_( "% in " .format( label ) )
			.align_( \right )
			.applySkin( RoundView.skin );

			vw.inPu = UPopUpMenu( parent, Rect( labelSpace, 0, bounds.width - labelSpace, 14 ) );

			StaticText( parent, Rect( 0, 18, labelWidth, 14 ) )
			.string_( "% out ".format( label ) )
			.align_( \right )
			.applySkin( RoundView.skin );

			vw.outPu = UPopUpMenu( parent, Rect( labelSpace, 18, bounds.width - labelSpace, 14 ) );

			extraMenuActions = {[
				MenuAction.separator,
				MenuAction( "Add...", {
					SCRequestString( "", "please enter device name:", { |string|
						action.value( vw, this.constrain( string ) );
					});
				}),
				MenuAction( "Refresh", {
					this.class.refreshDevices;
					vw.inPu.items_([ "system default" ] ++ inDevices);
					vw.outPu.items_([ "system default" ] ++ outDevices);
					vw.setDevice([ vw.inDevice, vw.outDevice ]);
				})
			]};

			vw.inPu.items_([ "system default" ] ++ inDevices)
			.extraMenuActions_( extraMenuActions )
			.action_({ |pu|
				if( pu.value == 0 ) {
					vw.inDevice = nil;
					vw.outDevice = nil;
					action.value( vw, this.formatDevice( nil ) );
				} {
					vw.inDevice = pu.item;
					vw.outDevice = this.class.autoSelectOutDevice( vw.inDevice );
					action.value( vw, this.formatDevice([ vw.inDevice, vw.outDevice ]) );
				};
			});

			vw.outPu.items_([ "system default" ] ++ outDevices)
			.extraMenuActions_( extraMenuActions )
			.action_({ |pu|
				if( pu.value == 0 ) {
					vw.outDevice = nil;
					action.value( vw, this.formatDevice([ vw.inDevice, nil ]) );
				} {
					vw.outDevice = pu.item;
					action.value( vw, this.formatDevice([ vw.inDevice, vw.outDevice ]) );
					action.value( vw, this.formatDevice([ vw.inDevice, vw.outDevice ]) );
				};
			});

			vw.setDevice = { |vwx, device|
				device = this.unpackDevice( device );
				vw.inDevice = device[0];
				vw.outDevice = device[1];
				vw.inPu.item = vw.inDevice ? "system default";
				vw.outPu.item = vw.outDevice ? "system default";
				if( device.every(_.isNil) or: { this.class.checkDevice( device ) }) {
					vw.inPu.font = font; vw.outPu.font = font;
				} {
					vw.inPu.font = font.copy.italic_( true );
					vw.outPu.font = vw.inPu.font;
				};
			};

			vw.doAction = { action.value( vw, this.formatDevice([ vw.inDevice, vw.outDevice ]) ); };

			vw.atAll([\inPu, \outPu]).do({ |pu|
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