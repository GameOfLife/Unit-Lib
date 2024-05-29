MackieControl {
	classvar >global;
	var <dict, <selected = 0;
	var <>verbose = false;

	*new { |n = 24|
		^super.new.init( n );
	}

	makeGlobal { global = this; }

	*global { |n = 24|
		if( global.notNil ) {
			^global;
		} {
			^global = this.new( n );
		};
	}

	init { |inN = 24|
		dict = MultiLevelIdentityDictionary();
		inN.do({ |i|
			dict.putTree( i, [
				\slider, 0,
				\touch, 0,
				\knob, 0.5,
				\knobPush, 0,
				\knobHilited, false,
				\knobCentered, true,
				\solo, 0,
				\on, 0,
				\rec, 0,
				\sel, 0,
				\level, 0,
				\label, "sl%".format(i).asSymbol,
				\knobLabel, "kn%".format(i).asSymbol,
			]);
		});
		dict.putTree( 'global', [
			\master, 0,
			\masterKnob, 0.5,
			\bankLeft, 0,
			\bankRight, 0,
			\nudgeLeft, 0,
			\nudgeRight, 0,
		]);
		dict.putTree( 'transport', [
			\rewind, 0,
			\forward, 0,
			\stop, 0,
			\play, 0,
			\loop, 0,
			\record, 0,
			\r, 0,
			\w, 0
		]);
		this.changed( \init );
	}

	selected_ { |newSelection = 0|
		selected = newSelection;
		this.changed( \selected, selected );
		if( verbose ) { "MackieControl selected %\n".postf( selected ) };
	}

	at { |index = 0, type = \slider| ^dict.at( index, type ) }
	put { |index = 0, type = \slider, value = 0|
		dict.put( index, type, value );
		this.changed( index, type, value );
		if( verbose ) { "MackieControl % % %\n".postf( index, type, value ) };
	}
	toggle { |index = 0, type = \solo|
		this.put( index, type, 1 - this.at( index, type ) );
	}
	delta { |index = 0, type = \knob, add = 0, clipMode = \clip|
		this.put( index, type, (this.at( index, type ) + add).perform( clipMode, 0.0, 1.0 ) );
	}

	n { ^dict.size - 2 }

	actions {
		^this.dependants
		.select({ |item| item.isKindOf( MackieControlAction ) })
	}
}

MackieControlDependant {
	classvar <>all;

	var mackieControl;

	setMackieControl { |mc|
		if( mackieControl.notNil ) {
			mackieControl.removeDependant( this );
		};
		mackieControl = mc;
		if( mackieControl.notNil ) {
			mackieControl.addDependant( this );
		};
	}

	mackieControl {
		if( mackieControl.isNil ) {
			this.setMackieControl( MackieControl.global );
		};
		^mackieControl;
	}

	addToAll {
		if( (this.class.all !? _.includes( this ) ? false).not ) {
			this.class.all = this.class.all.add( this );
		};
	}

	remove {
		mackieControl !? _.removeDependant( this );
		this.class.all.remove( this );
	}

	free { this.remove }

}

MackieControlAction : MackieControlDependant {
	classvar <>all;

	var <>index = 0, <>key = \slider, <>action;
	var <>enabled = true, <>lastValue;

	*new { |index = 0, key = \slider, action, mackieControl|
		^super.new.init(
			mackieControl ?? { MackieControl.global },
			index ? 0,
			key ? \slider,
			action
		);
	}

	init { |mc, inIndex, inKey, inAction|
		index = inIndex;
		key = inKey;
		action = inAction;
		this.setMackieControl( mc );
		this.addToAll;
	}

	update { |mc, i, what, value|
		if( enabled && { index == i && { key === what } }) {
			action.value( this, value );
			lastValue = value;
		};
	}

	value { ^this.mackieControl[ index, key ] }

	value_ { |newVal = 0| this.setValue( newVal ) }

	setValue { |newVal = 0| // without calling action
		var wasEnabled;
		wasEnabled = enabled;
		enabled = false;
		this.mackieControl[ index, key ] = newVal;
		enabled = true;
	}

	valueAction_ { |newVal = 0|
		this.mackieControl[ index, key ] = newVal;
	}
}

MackieControlGUI : MackieControlDependant {
	classvar <>all;
	classvar <>viewWidth = 50, <>viewHeight = 300;

	var <>window, <>views;

	*new { |name, width, mackieControl|
		^super.new.init( name, width, mackieControl );
	}

	init { |name, width, mc|
		var size, comp;
		this.setMackieControl( mc );
		this.addToAll;
		if( window.notNil && { window.isClosed.not } ) {
			window.onClose.value;
			window.close;
		};
		size = width ?? { this.mackieControl.n };
		window = Window(
			name ? "MackieControl",
			Rect( 200, 20, 9 * (viewWidth + 12) + 8, viewHeight + 50 )
		).front;
		window.onClose_({ this.remove });
		window.addFlowLayout(0@0,4@4);
		window.view.minHeight_( viewHeight + 50 ).maxHeight_( viewHeight + 50 );
		views = ();
		RoundView.pushSkin( UChainGUI.skin );
		this.makeViews( window, size );
		this.makeMasterViews( window );
		this.makeTransportViews( window );
		RoundView.popSkin;
	}

	makeViews { |parent, n = 24|
		var scrollView, comp;
		scrollView = ScrollView( parent,
			Rect(0,0, 8 * (viewWidth + 12) + 8, (viewHeight + 24))
		).resize_(2);
		comp = CompositeView( scrollView,
			Rect(0,0, n * (viewWidth + 12) + 8, (viewHeight + 8))
		);
		comp.addFlowLayout;
		n.do({ |i|
			this.makeStripView( comp, i, viewWidth, viewHeight );
		});
		this.updateAll;
	}

	makeStripView { |parent, index = 0, width = 40, height = 300|
		var composite, comp2, lvWidth = 20, vws;
		composite = CompositeView( parent, (width + 8) @ height );
		composite.addFlowLayout;
		vws = ();
		vws[ \label ] = TextField( composite, width @ 14 )
		.applySkin( RoundView.skin )
		.action_({ |tv|
			this.mackieControl[ index, \label ] = tv.string;
		});
		vws[ \knobLabel ] = TextField( composite, width @ 14 )
		.applySkin( RoundView.skin )
		.action_({ |tv|
			this.mackieControl[ index, \knobLabel ] = tv.string;
		});
		vws[ \knob ] = SmoothSlider( composite, width @ 14 )
		.centered_( true )
		.action_({ |sl|
			this.mackieControl[ index, \knob ] = sl.value;
		});
		vws[ \slider ] = SmoothSlider( composite,
			(width - lvWidth - 4) @ (height - 14 - 14 - 14 - 16 )
		)
		.action_({ |sl|
			this.mackieControl[ index, \slider ] = sl.value;
		});
		comp2 = CompositeView( composite, lvWidth @ (height - 8) );
		comp2.addFlowLayout( 0@0, 4@4 );
		vws[ \level ] = LevelIndicator( comp2, lvWidth @
			(height - 14 - 14 - 14 - 16 - 14 - 14 - 14 - 14 - 16 )
		);
		vws[ \solo ] = SmoothButton( comp2, lvWidth@14 )
		.label_([ "s", "s" ])
		.radius_(2)
		.hiliteColor_( Color.yellow )
		.action_({ |bt|
			this.mackieControl[ index, \solo ] = bt.value;
		});
		vws[ \on ] = SmoothButton( comp2, lvWidth@14 )
		.label_([ "on", "on" ])
		.radius_(2)
		.hiliteColor_( Color.green )
		.action_({ |bt|
			this.mackieControl[ index, \on ] = bt.value;
		});
		vws[ \rec ] = SmoothButton( comp2, lvWidth@14 )
		.label_([ "r", "r" ])
		.radius_(2)
		.hiliteColor_( Color.red )
		.action_({ |bt|
			this.mackieControl[ index, \rec ] = bt.value;
		});
		vws[ \sel ] = SmoothButton( comp2, lvWidth@14 )
		.label_([ "sel", "sel" ])
		.radius_(2)
		.hiliteColor_( Color.yellow )
		.action_({ |bt|
			this.mackieControl[ index, \sel ] = bt.value;
		});

		views[ index ] = vws;
	}

	makeMasterViews {
	}

	makeTransportViews {
	}

	update { |mc, i, what, value|
		var func = { views[ i ][ what ].value = value };
		switch( what,
			\label, {
				{ views[ i ][ \label ].string = value }.defer;
			},
			\knobLabel, {
				{ views[ i ][ \knobLabel ].string = value }.defer;
			},
			\slider, func,
			\knob, func,
			\knobCentered, { views[ i ][ \knob ].centered = value },
			\solo, func,
			\on, func,
			\rec, func,
			\sel, func,
			\level, func,
		);
	}

	updateAll {
		[
			\label, \knobLabel, \slider, \knob, \knobCentered,
			\solo, \on, \rec, \sel, \level
		].do({ |item|
			this.mackieControl.n.do({ |i|
				this.update( this.mackieControl, i, item, this.mackieControl[ i, item ] );
			});
		})
	}
}

MackieControlMIDI {
	classvar <>all;

	var mackieControl, <>midiFuncs;
	var <>offset = 0;
	var <midiIn, <midiOut;
	var <>currentFaderVals;
	var <>softSetTime = 0.9, <>hwSoftSetTasks;
	var <>knobRes = 0.0078125; // resolution for knobs (default 1/128)
	var <>knobVelo = 1.25; // velocity dependence (1: none, >1: inc/dec more at higher velocity)
	var <>knobPushResets = true;
	var <>masterKnobRes = 0.1;
	var <>useSysex = true;

	*new { |midiIn, midiOut, offset = 0, mackieControl|
		^super.new.init( mackieControl ?? { MackieControl.global }, midiIn, midiOut, offset );
	}

	init { |inMC, inMIn, inMOut, inOffset|
		midiIn = this.class.getMIDIInSrc( inMIn );
		if( all !? _.any({ |mcm|
			mcm.midiIn == midiIn;
		}) ? false ) {
			"MackieControlMIDI: a MackieControlMIDI with midiIn % already exists\n...using the existing one instead\n"
			.postf( inMIn );
			^all.detect({ |mcm| mcm.midiIn == midiIn });
		} {
			midiOut = this.class.getMIDIOut( inMOut );
			offset = inOffset ? 0;
			this.setMackieControl( inMC );
			currentFaderVals = 0!9;
			this.startMIDIFuncs;
			this.addToAll;
		}
	}

	addToAll {
		if( (all !? _.includes( this ) ? false).not ) {
			all = all.add( this );
		};
	}

	*getMIDIInSrc { |mIn|
		var port;
		if( MIDIClient.initialized.not ) { MIDIIn.connectAll; };
		case { mIn.isNumber } {
			port = MIDIClient.sources[ mIn ] ?? {
				MIDIClient.sources.detect({ |item| item.uid == mIn })
			} !? _.uid;
			if( port.isNil ) { "MackieControlMIDI: MIDI source % not found\n".postf( mIn ) };
			^port ? mIn;
		} { mIn.isKindOf( MIDIEndPoint ) } {
			^mIn.uid;
		} { [ String, Symbol ].includes( mIn.class ) } {
			mIn = UMIDIFilterSpec.formatDeviceString( mIn.asString ).asSymbol;
			port = MIDIClient.sources.detect({ |item|
				[ item.device, item.name ].join( "/" ).asSymbol.matchOSCAddressPattern( mIn );
			}) !? _.uid;
			if( port.isNil ) { "MackieControlMIDI: MIDI source % not found\n".postf( mIn ) };
			^port ? 0;
		} {
			^0
		};
	}

	*getMIDIOut { |mOut|
		var port;
		if( MIDIClient.initialized.not ) { MIDIIn.connectAll; };
		case { mOut.isKindOf( MIDIOut ) } {
			^mOut;
		} { mOut.isNumber } {
			port = MIDIClient.destinations[ mOut ];
			if( port.isNil ) {
				port = MIDIClient.destinations.detectIndex({ |item| item.uid == mOut });
			} {
				port = mOut;
			};
			if( port.isNil ) {
				"MackieControlMIDI: MIDI destination % not found\n".postf( mOut );
				^(); // spoof port
			} {
				^MIDIOut( port ).latency_(0);
			};
		} { mOut.isKindOf( MIDIEndPoint ) } {
			^MIDIOut( MIDIClient.destinations.detectIndex( mOut ) ).latency_(0);
		} { [ String, Symbol ].includes( mOut.class ) } {
			mOut = UMIDIFilterSpec.formatDeviceString( mOut.asString ).asSymbol;
			port = MIDIClient.destinations.detectIndex({ |item|
				[ item.device, item.name ].join( "/" ).asSymbol.matchOSCAddressPattern( mOut );
			});
			if( port.isNil ) {
				"MackieControlMIDI: MIDI destination % not found\n".postf( mOut );
				^(); // spoof port
			} {
				^MIDIOut( port ).latency_(0);
			}
		} {
			^(); // spoof port
		};
	}

	setMackieControl { |mc|
		if( mackieControl.notNil ) {
			mackieControl.removeDependant( this );
		};
		mackieControl = mc;
		if( mackieControl.notNil ) {
			mackieControl.addDependant( this );
		};
	}

	remove {
		mackieControl !? _.removeDependant( this );
		midiFuncs.do(_.free); midiFuncs = nil;
		all.remove( this );
	}

	free { this.remove }

	mackieControl {
		if( mackieControl.isNil ) {
			this.setMackieControl( MackieControl.global );
		};
		^mackieControl;
	}

	update { |mc, i, what, value|
		this.hwSet( i, what, value, mc );
	}

	getMaxOffset { |mc|
		^((mc ?? { this.mackieControl }).dict.dictionary.keys
			.select(_.isNumber).maxItem ? 0).roundUp( 8 ) - 8;
	}

	setOffset { |newOffset = 0, softSet = true, mc|
		offset = (newOffset ? 0).clip(0, this.getMaxOffset( mc ) );
		this.hwSetAll( softSet, mc );
	}

	startMIDIFuncs {
		midiFuncs.do(_.free);
		midiFuncs = [
			MIDIFunc.bend({ |val, ch, src|
				if( ch < 8 ) {
					this.mackieControl[ch + offset, \slider ] = (val / 16) / 1023;
				} {
					this.mackieControl[\global, \master ] =  (val / 16) / 1023;
				};
			}, srcID: this.midiIn ),
			MIDIFunc.cc({ |val, cc, ch, src|
				case { cc.inclusivelyBetween(16,23) } {
					if( val >= 65 ) { val = (64-val) };
				    val = (val.abs ** this.knobVelo) * ( if( val.isNegative ) { -1 } { 1 } );
					this.mackieControl.delta( cc + this.offset - 16, \knob, val * this.knobRes );
				} { cc == 60 } {
					if( this.mackieControl.selected.notNil ) {
						if( val >= 65 ) { val = (64-val) };
						val = (val.abs ** this.knobVelo) * ( if( val.isNegative ) { -1 } { 1 } );
						this.mackieControl.delta( this.mackieControl.selected, \slider, val * this.knobRes * 0.1 );
					};
				};
			}, srcID: this.midiIn ),
			MIDIFunc.noteOn({ |val, nn, ch, src|
				var index;
				switch( nn >> 3,
					0, { this.mackieControl.toggle( nn + this.offset, \rec ); },
					1, { this.mackieControl.toggle( nn + this.offset - 8, \solo ); },
					2, { this.mackieControl.toggle( nn + this.offset - 16, \on ); },
					3, { // select
						index = nn + this.offset - 24;
						this.mackieControl.dict.dictionary.keys.do({ |key|
							this.mackieControl[ key, \sel ] = (key == index).binaryValue;
						});
						this.mackieControl.selected = index;
					},
					4, { // knobPush
						index = nn + this.offset - 32;
						if( knobPushResets ) {
							this.mackieControl[ index, \knob ] = [ 0, 0.5 ][
								this.mackieControl[ index, \knobCentered ].binaryValue
							];
						};
						this.mackieControl[ index, \knobPush ] = 1;
					},
					13, { this.mackieControl[ nn + this.offset - 104, \touch ] = 1;	},
					{
						switch( nn,
							46, { //bank back
								this.setOffset( this.offset - 8 );
								this.mackieControl[ \global, \bankLeft ] = 1;
							},
							47, { //bank forward
								this.setOffset( this.offset + 8 );
								this.mackieControl[ \global, \bankRight ] = 1;
							},
							48, {  //nudge back
								this.setOffset( this.offset - 1 );
								this.mackieControl[ \global, \nudgeRight ] = 1;
							},
							49, { //nudge forward
								this.setOffset( this.offset + 1 );
								this.mackieControl[ \global, \nudgeLeft ] = 1;
							},
							97, { this.mackieControl.delta( \global, \masterKnob, masterKnobRes ) },
							96, { this.mackieControl.delta( \global, \masterKnob, masterKnobRes.neg ) }
						);
					}
				)
			}, srcID: this.midiIn ),
			MIDIFunc.noteOff({ |val, ch, nn, src|
				switch( nn >> 3,
					4, { this.mackieControl[  nn + this.offset - 32, \knobPush ] = 0; },
					13, { this.mackieControl[ nn + this.offset - 104, \touch ] = 0; },
					{
						switch( nn,
							46, { this.mackieControl[ \global, \bankLeft ] = 0; },
							47, { this.mackieControl[ \global, \bankRight ] = 0; },
							48, { this.mackieControl[ \global, \nudgeLeft ] = 0; },
							49, { this.mackieControl[ \global, \nudgeRight ] = 0; }
						);
					}
				)
			}, srcID: this.midiIn ),
		];
	}

	hwSetStrip { |j = 0, softSet = true, mc|
		var i;
		i = j + offset;
		[
			if( softSet ) { \softSetSlider } { \slider },
			\knob,
			\level,
			\on,
			\solo,
			\rec,
			\sel,
			\label,
			\knobLabel
		].do({ |item|
			this.hwSet( i, item, nil, mc );
		});
	}

	hwSetAll { |softSet = true, mc|
		(..7).do({ |j| this.hwSetStrip( j, softSet, mc ) });
		this.hwSet( \global, \master, nil, mc );
	}

	hwSet { |i = 0, what = \slider, value, mc|
		var j, knob;
		mc = mc ?? { this.mackieControl; };
		if( i.isNumber ) {
			j = i - offset;
			if( j.inclusivelyBetween(0,7) ) {
				switch( what,
					\slider, { this.hwSetSlider( j, value ?? { mc[ i, \slider ] ? 0 } ) },
					\softSetSlider, {  this.hwSoftSetSlider( j, value ?? { mc[ i, \slider ] ? 0 } ) },
					\knob, {
						this.hwSetKnob( j,
							value ?? { mc[ i, \slider ] ? 0 },
							mc[ i, \knobHilited ] ? false,
							mc[ i, \knobCentered ] ? false
						);
					},
					\level, { this.hwSetLevel( j, value ?? { mc[ i, \level ] ? 0 } ) },
					\on, { this.hwSetButton( j, value ?? { mc[ i, \on ] ? 0 }, \on ) },
					\solo, { this.hwSetButton( j, value ?? { mc[ i, \solo ] ? 0 }, \solo ) },
					\rec, { this.hwSetButton( j, value ?? { mc[ i, \rec ] ? 0 }, \rec ) },
					\sel, { this.hwSetButton( j, value ?? { mc[ i, \sel ] ? 0 }, \sel ) },
					\label, { this.hwSetText( j, value ?? { mc[ i, \label ] ? "" } , \label ) },
					\knobLabel, { this.hwSetText( j, value ?? { mc[ i, \knobLabel ] ? "" } , \knob ) }
				);
			};
		} {
			if( i == \global ) {
				switch( what,
					\master, { this.hwSetSlider( 8, value ?? { mc[ \global, \master ] ? 0 } ) },
				);
			};
		}
	}

	hwSetSlider { |i, value|
		this.midiOut.bend( i, (value * 1023).asInteger * 16 );
		this.currentFaderVals[i] = value; // store value
	}

	hwSoftSetSlider { |i, value, time|
		var startVal, interval = 0.025, nSteps;
		time = time ? this.softSetTime;
		if( time > 0 ) {
			this.hwSoftSetTasks = this.hwSoftSetTasks ?? { Order( this.currentFaderVals.size ) };
			this.hwSoftSetTasks[i].stop;
			startVal = this.currentFaderVals[i];
			nSteps = ((time * (1/interval)) * ((startVal - value).abs.pow(1/8))).round(1);
			if( nSteps > 0 ) {
				this.hwSoftSetTasks[ i ] = Task({
					var lastRawVal;
					nSteps.do({ |ii|
						var val, rawVal;
						interval.wait;
						val = ii.linlin(0, nSteps-1, 0,0.5pi).sin.linlin(0,1, startVal, value );
						rawVal = (val * 127).asInteger * 128;
						if( lastRawVal != rawVal ) // only update if actually changed (prevent midi overload)
						{ this.midiOut.bend( i, rawVal ); };
						this.currentFaderVals[i] = val; // store value
						lastRawVal = rawVal;
					});
				}).start;
			};
		} {
			this.hwSetSlider( i, value );
		};
	}

	hwSetKnob { |i, value, hilite = true, centered = true|
		this.midiOut.control( 0, 16+32+i, (
			if( hilite ) {
				if ( centered ) { 16 } { 32 };
			} { 0 }
		) + value.linlin(0,1,1,11).asInteger );
	}

	hwSetButton { |i, value, type = \on|
		this.midiOut.noteOn( 0, i + (solo: 8, on: 16, rec: 0, sel: 24)[type], value * 127 );
	}

	hwSetLevel { |i, value|
		this.midiOut.touch( i, value * 10 );
	}

	hwSetText { |i, string = "", type = \label| // \label or \knob
		var array;
		if( useSysex ) {
			string = string.asString;
			array = Int8Array[ 16rf0, 0, 0, 16r66, 16r14, 16r12 ] ++
			Int8Array[ (i*7) + ( label: 0, knob: (8*7) )[ type ] ] ++
			string.extend(7,$ ).ascii.as( Int8Array ) ++
			Int8Array[ 16rf7 ];
			this.midiOut.sysex( array );
		};
	}
}