MackieControl {

	var <dict;

	*new { |n = 24, port|
		^super.new.init( n, port );
	}

	init { |inN = 24, port|
		dict = MultiLevelIdentityDictionary();
		inN.do({ |i|
			dict.putTree( i, [
				\slider, 0,
				\touch, 0,
				\knob, 0.5,
				\knobPush, 0,
				\knobHilited, false,
				\knobCentered, false,
				\solo, 0,
				\on, 0,
				\rec, 0,
				\sel, 0,
				\level, 0,
				\label, i.asSymbol,
				\knobLabel, '',
			]);
		});
		dict.putTree( 'global', [
			\master, 0,
			\masterKnob, 0,
			\bankLeft, 0,
			\bankRight, 0,
			\nudgeLeft, 0,
			\nudgeRight, 0
		]);
	}

	at { |index = 0, type = \slider| ^dict.at( index, type ) }
	put { |index = 0, type = \slider, value = 0|
		dict.put( index, type, value );
		this.changed( index, type, value );
	}
}

MackieControlMIDI {
	var <mackieControl;
	var <>offset = 0;
	var <>midiIn, <>midiOut;
	var <>currentFaderVals;
	var <>sofSetTime = 0.9, <>hwSoftSetTasks;
	var <>knobsobRes = 0.0078125; // resolution for knobs (default 1/128)
	var <>knobsobVelo = 1.25; // velocity dependence (1: none, >1: inc/dec more at higher velocity)
	var <>masterKnobRes = 0.1;
	var <>useSysex = true;

	*new { |mackieControl, midiIn, midiOut, offset = 0|
		^super.new.init( mackieControl, midiIn, midiOut, offset );
	}

	init { |inMC, inMIn, inMOut, inOffset|
		midiIn = inMIn ?? 0; // spoof midi for now
		midiOut = inMOut ?? {()};
		offset = inOffset ? 0;
		this.setMackieControl( inMC );
		currentFaderVals = 0!9;
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

	update { |mc, i, what, value|
		this.hwSet( i, what, value, mc );
	}

	setOffset { |newOffset = 0, softSet = true, mc|
		offset = newOffset ? 0;
		this.hwSetAll( softSet, mc );
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
		time = time ? this.sofSetTime;
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