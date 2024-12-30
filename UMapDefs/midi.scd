/*
\midi

Creates an UMap that listens to MIDI  messages.

value: the actual output value (this will change when a midi message is received, but can also be changed manually)
active: if true, the UMap will listen to the specified MIDI message. Hitting cmd-. will make the UMap stop listening, but whenever the Unit is started, it will re-activate. The MIDI client will be inited when the first midi_xxx UMap is made 'active'.
filter: an Array of Symbols and values to filter the incoming midi messages. The array is formatted as follows:
    [ <device/port>, type, chan, num, val ]
    <device/port>: a Symbol with the names of device and port as given by the system, divided by a '/'. The names may contain wildcards (*) which will be resolved to accept any device/port that matches the pattern. The default is * / *, meaning any device/port
   type: a Symbol for event type. This is ignored by the UMap as the type is set elsewhere in the args
   chan, num, val: MIDI channel, num and val to filter. Nil means any.
The graphical interface of UMIDIDeviceSpec contains a 'learn' button, which waits for an incoming MIDI event and fills the filter in when receiving it.
fromRange / fromBendRange: the used range of the midi message
toRange: the output range of the UMap (can be inverted)
curve: a curvature value for the mapped range. If curve == 0, the range is linear.
clipMode: can be one of the following modes (Symbol):
	'ignore': ignore values outside fromRange
	'clip' (default): clip values outside fromRange
	'fold': fold values outside fromRange
	'wrap': wrap around values outside fromRange
	'none': no clipping applied (values can go outside toRange)
clipSoftness: softens the edges of 'clip' and 'fold' modes.
type: the type of MIDI message to listen to; 'cc', 'note', 'velocity', 'bend', 'touch', 'polytouch' or 'program'

The 'value' and 'toRange' arg ranges are mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

var defs = [];
var mapFunc, args;
var switchFunc, switchArgs;
var makeDefsFunc;

mapFunc = { |unit, value, fromRange|
	var toRange, invert, curve, clipMode, clipSoftness;
	var scaleValue;
	clipMode = unit.get( \clipMode );
	if( (clipMode !== \ignore) or: {
		value.inclusivelyBetween( *fromRange )
	} ) {
		toRange = unit.mapGet( \toRange );
		invert = unit.get( \invert );
		if( invert == true ) {
			toRange = toRange.reverse;
		};
		curve = unit.get( \curve );
		curve = curve.clip2( 0.99932929973907 ).calcCurve( -1, 1 );
		clipSoftness = unit.get( \clipSoftness );
		scaleValue = { |val, fr, mode = \none|
			if ( abs(curve) < 0.001 ) {
				val.linlin( *fr ++ [ -1, 1, mode ] );
			} {
				val.lincurve( *fr ++ [ -1, 1, curve, mode ] );
			};
		};
		switch( clipMode,
			\clip, { value = scaleValue.value( value, fromRange ).softclip2(1, clipSoftness ) },
			\fold, { value = scaleValue.value( value, fromRange ).softfold2(1, clipSoftness ) },
			\wrap, {
				value = if( clipSoftness > 0 ) {
					scaleValue.value( value, fromRange ).softwrap2(1, clipSoftness )
				} {
					scaleValue.value( value, fromRange ).wrap2(1)
				}
			},
			\excess, {
				var diff, mean, excessed;
				diff = fromRange[1] - fromRange[0];
				mean = fromRange.mean;
				if ( diff < 127 ) {
					excessed = (value - mean).excess( diff / 2);
					value = scaleValue.value( excessed,  [0 + (diff / 2) - mean, 127 - (diff/2) - mean], \minmax )
					.blend( scaleValue.value( value, [0,127] ), clipSoftness );

				} {
					value = scaleValue.value( mean.blend( value, clipSoftness ), [0,127], \minmax );
				};
			},
			{
				value = scaleValue.value( value, fromRange );
			}
		);
		value = value.linlin(-1,1,*toRange ++ [ \none ]);
		unit.mapSet( \value, value );
	};
};

args = [
	[ \fromRange, [0,127], RangeSpec(0,127,step:1) ],
	[ \toRange, [0,1], RangeSpec(0,1) ],
	[ \invert, false, BoolSpec(false) ],
	[ \curve, 0, [-1,1,\lin,0,0].asSpec ],
	[ \clipMode, \clip, ListSpec( [ \ignore, \clip, \fold, \wrap, \excess, \none ] ) ],
	[ \clipSoftness, 0, [0,1].asSpec ],
];

switchFunc = { |unit, value, fromRange|
	var numStates, clickType, clickDur, timeOut, triggerOn, toRange, invert;
	var dict, lastIn, lastTime, lastState, clickCount, now;
	var matched, newState;

	#numStates, clickType, clickDur, timeOut, triggerOn, invert = #[
		numStates, clickType, clickDur, timeOut, triggerOn, invert
	].collect({ |item| unit.get( item ) });

	toRange = unit.mapGet( \toRange );

	dict = unit.get( \dict );

	if( dict.isNil ) {
		dict = ();
		unit.set( \dict, dict );
	};

	lastIn = dict[ \lastIn ] ? 0;
	dict[ \lastIn ] = value;

	switch( triggerOn,
		\match, { // match specific values, allows repetition
			matched = switch( value,
				fromRange[0], false,
				fromRange[1], true
			);
		},
		\nearest, {
			matched = fromRange.absdif(value).minIndex.booleanValue;
		},
		\up_down, { // test for up/down, ignores repetitions
			if( value >= fromRange[0] && { value <= fromRange[1] }) {
				case { value > lastIn } {
					if( dict[ \lastMatched ] != true ) {
						matched = true;
						dict[ \lastMatched ] = true;
					};
				} { value < lastIn } {
					if( dict[ \lastMatched ] != false ) {
						matched = false;
						dict[ \lastMatched ] = false;
					};
				};
			};
		},
		\schmitt, { // switch at passing upper and only switch back at passing lower
			case {
				value >= fromRange[1] && { lastIn < fromRange[1] }
			} {
				matched = true;
			} {
				value <= fromRange[0] && { lastIn > fromRange[0] }
			} {
				matched = false;
			};
		},
		\in_range, {
			if( value < fromRange[1] and: { value > fromRange[0] }) {
				if( lastIn >= fromRange[1] or: { lastIn <= fromRange[0] } ) {
					matched = true;
				};
			} {
				if( lastIn <= fromRange[1] and: { lastIn >= fromRange[0] } ) {
					matched = false;
				};
			};
		},
	);

	if( matched.notNil ) {
		now = Process.elapsedTime;
		clickCount = dict.clickCount ? 0;

		if( (dict.lastTime ? 0) >= (now - timeOut) ) {
			if( matched ) {
				dict[ \clickCount ] = clickCount = clickCount + 1;
			} {
				if( clickDur === \long ) { matched = nil };
			};
		} {
			if( matched ) {
				dict[ \clickCount ] = clickCount = 0;
			} {
				if( clickDur === \short ) { matched = nil };
			};
		};

		dict.lastTime = now;

		switch( clickType,
			\single, { if( clickCount != 0 ) { matched = nil }; },
			\double, { if( clickCount != 1 ) { matched = nil }; },
			\triple, { if( clickCount != 2 ) { matched = nil }; },
		);

		if( matched.notNil ) {
			if( invert == true ) { toRange = toRange.reverse };
			if( numStates > 1 ) {
				if( clickDur !== \any ) { matched = matched.not }; // react on up
				if( matched ) {
					newState = ((dict[ \lastState ] ? 0) + 1).wrap(0, numStates-1);
					value = newState.linlin(0, numStates-1, toRange[0], toRange[1] );
					unit.mapSet( \value, value );
					dict[ \lastState ] = newState;
				};
			} {
				value = toRange[ matched.binaryValue ];
				dict[ \lastState ] = matched.binaryValue;
				unit.mapSet( \value, value );
			};
		}

	};
};

switchArgs = [
	[ \fromRange, [0,127], RangeSpec(0,127,step:1) ],
	[ \toRange, [0,1], RangeSpec(0,1) ],
	[ \invert, false, BoolSpec(false) ],
	[ \numStates, 2, IntegerSpec(1,1,99) ],
	[ \clickType, \any, ListSpec([ 'any', 'single', 'double', 'triple' ]) ],
	[ \clickDur, \any, ListSpec([ 'any', 'short', 'long' ]) ],
	[ \timeOut, 0.2, ControlSpec(0.1,1,\exp,0.0,0.2) ],
	[ \triggerOn, \match, ListSpec([ 'match', 'nearest', 'up_down', 'schmitt', 'in_range' ]) ],
	[ \dict, nil, AnythingSpec(), true ],
];

makeDefsFunc = { |name, matchType, matchFunc, argSpec, replaceToRangeArg|
	var def, subdefs;
	subdefs = [ \map, \switch ].collect({ |item|
		var func, extraArgs;
		switch( item,
			\map, { func = mapFunc; extraArgs = args; },
			\switch, { func = switchFunc; extraArgs = switchArgs; }
		);

		if( replaceToRangeArg.notNil ) { extraArgs[0] = replaceToRangeArg; };

		ControllerUMapDef( item, { |unit, midiDict ...args|
			if( args[0] === matchType ) { matchFunc.value( unit, func, *args ); };
		}, UMIDIDict, [ argSpec ] ++ extraArgs, addToAll: false )
		.mappedArgs_([\value, \toRange])
		.addStartFunc({ UMIDIDict.start( false ) });
	});

	MultiUMapDef( name, subdefs, 'midi', 'mode', false, false )
	.mappedArgs_([\toRange])
	.allowedModes_( [ \init, \sync, \normal ] )
	.canUseUMap_( false );
};

defs = defs.add(
	makeDefsFunc.( \cc, \cc, { |unit, func, type, src, chan, num, value|
		if( UMIDIDict.matchEvent( unit.get( \filter ), [ src, type, chan, num ] ) ) {
			func.value( unit, value, unit.get( \fromRange ) );
		};
	}, [ \filter, [ '*/*', 'cc', 0, 0, nil ], UMIDIFilterSpec( 'cc' ) ])
);

defs = defs.add(
	makeDefsFunc.( \note, \noteOn, { |unit, func, type, src, chan, num, value|
		if( UMIDIDict.matchEvent( unit.get( \filter ), [ src, type, chan ] ) ) {
			func.value( unit, num, unit.get( \fromRange ) );
		};
	}, [ \filter, [ '*/*', 'noteOn', 0, nil, nil ], UMIDIFilterSpec( 'noteOn', useNum: false ) ])
);

defs = defs.add(
	makeDefsFunc.( \velocity, \note, { |unit, func, type, src, chan, num, value|
		if( UMIDIDict.matchEvent( unit.get( \filter ), [ src, type, chan, num ] ) ) {
			func.value( unit, value, unit.get( \fromRange ) );
		};
	}, [ \filter, [ '*/*', 'note', 0, 69, nil ], UMIDIFilterSpec( 'note' ) ])
);

defs = defs.add(
	makeDefsFunc.( \bend, \bend, { |unit, func, type, src, chan, num, value|
		if( UMIDIDict.matchEvent( unit.get( \filter ), [ src, type, chan ] ) ) {
			mapFunc.value( unit, num, unit.get( \bendRange ) );
		};
	},
	[ \filter, [ '*/*', 'bend', 0, nil, nil ], UMIDIFilterSpec( 'bend', useNum: false ) ],
	[ \bendRange, [0,16383], RangeSpec(0,16383,step:1) ])
);

defs = defs.add(
	makeDefsFunc.( \touch, \touch, { |unit, func, type, src, chan, num, value|
		if( UMIDIDict.matchEvent( unit.get( \filter ), [ src, type, chan ] ) ) {
			mapFunc.value( unit, num, unit.get( \fromRange ) );
		};
	}, [ \filter, [ '*/*', 'touch', 0, nil, nil ], UMIDIFilterSpec( 'touch', useNum: false ) ])
);

defs = defs.add(
	makeDefsFunc.( \polytouch, \polytouch, { |unit, func, type, src, chan, num, value|
		if( UMIDIDict.matchEvent( unit.get( \filter ), [ src, type, chan, num ] ) ) {
			mapFunc.value( unit, value, unit.get( \fromRange ) );
		};
	}, [ \filter, [ '*/*', 'polytouch', 0, 0, nil ], UMIDIFilterSpec( 'polytouch' ) ])
);

defs = defs.add(
	makeDefsFunc.( \program, \program, { |unit, func, type, src, chan, num, value|
		if( UMIDIDict.matchEvent( unit.get( \filter ), [ src, type, chan ] ) ) {
			mapFunc.value( unit, num, unit.get( \fromRange ) );
		};
	}, [ \filter, [ '*/*', 'program', 0, nil, nil ], UMIDIFilterSpec( 'program', useNum: false ) ])
);

MultiUMapDef( 'midi', defs, 'midi', 'type', false )
.allowedModes_( [ \init, \sync, \normal ] )
.mappedArgs_([ 'value', 'toRange' ])
.canUseUMap_( false )