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
		curve = curve.clip2(0.9999).calcCurve( -1, 1 );
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

defs = defs.add(
	ControllerUMapDef( \cc, { |unit, midiDict ...args|
		var src, type, chan, num, value;
		#type, src, chan, num, value = args;
		if( type === \cc ) {
			if( UMIDIDict.matchEvent( unit.get( \filter ), [ src, type, chan, num ] ) ) {
				mapFunc.value( unit, value, unit.get( \fromRange ) );
			};
		};
	}, UMIDIDict, [
		[ \filter, [ '*/*', 'cc', 0, 0, nil ], UMIDIFilterSpec( 'cc' ) ],
	] ++ args, addToAll: false )
	.mappedArgs_([\toRange])
	.addStartFunc({ UMIDIDict.start( false ) })
);

defs = defs.add(
	ControllerUMapDef( \note, { |unit, midiDict ...args|
		var src, type, chan, num, value;
		#type, src, chan, num, value = args;
		if( type === \noteOn ) {
			if( UMIDIDict.matchEvent( unit.get( \filter ), [ src, type, chan ] ) ) {
				mapFunc.value( unit, num, unit.get( \fromRange ) );
			};
		};
	}, UMIDIDict, [
		[ \filter, [ '*/*', 'noteOn', 0, nil, nil ], UMIDIFilterSpec( 'noteOn', useNum: false ) ],
	] ++ args, addToAll: false )
	.mappedArgs_([\toRange])
	.addStartFunc({ UMIDIDict.start( false ) })
);

defs = defs.add(
	ControllerUMapDef( \velocity, { |unit, midiDict ...args|
		var src, type, chan, num, value;
		#type, src, chan, num, value = args;
		if( type === \note ) {
			if( UMIDIDict.matchEvent( unit.get( \filter ), [ src, type, chan, num ] ) ) {
				mapFunc.value( unit, value, unit.get( \fromRange ) );
			};
		};
	}, UMIDIDict, [
		[ \filter, [ '*/*', 'note', 0, 69, nil ], UMIDIFilterSpec( 'note' ) ],
	] ++ args, addToAll: false )
	.mappedArgs_([\toRange])
	.addStartFunc({ UMIDIDict.start( false ) })
);

defs = defs.add(
	ControllerUMapDef( \bend, { |unit, midiDict ...args|
		var src, type, chan, num, value;
		#type, src, chan, num, value = args;
		if( type === \bend ) {
			if( UMIDIDict.matchEvent( unit.get( \filter ), [ src, type, chan ] ) ) {
				mapFunc.value( unit, num, unit.get( \bendRange ) );
			};
		};
	}, UMIDIDict, [
		[ \filter, [ '*/*', 'bend', 0, nil, nil ], UMIDIFilterSpec( 'bend', useNum: false ) ],
		[ \bendRange, [0,16383], RangeSpec(0,16383,step:1) ],
	] ++ args[1..], addToAll: false )
	.mappedArgs_([\toRange])
	.addStartFunc({ UMIDIDict.start( false ) })
);

defs = defs.add(
	ControllerUMapDef( \touch, { |unit, midiDict ...args|
		var src, type, chan, num, value;
		#type, src, chan, num, value = args;
		if( type === \touch ) {
			if( UMIDIDict.matchEvent( unit.get( \filter ), [ src, type, chan ] ) ) {
				mapFunc.value( unit, num, unit.get( \fromRange ) );
			};
		};
	}, UMIDIDict, [
		[ \filter, [ '*/*', 'touch', 0, nil, nil ], UMIDIFilterSpec( 'touch', useNum: false ) ],
	] ++ args, addToAll: false )
	.mappedArgs_([\toRange])
	.addStartFunc({ UMIDIDict.start( false ) })
);

defs = defs.add(
	ControllerUMapDef( \polytouch, { |unit, midiDict ...args|
		var src, type, chan, num, value;
		#type, src, chan, num, value = args;
		if( type === \polytouch ) {
			if( UMIDIDict.matchEvent( unit.get( \filter ), [ src, type, chan, num ] ) ) {
				mapFunc.value( unit, value, unit.get( \fromRange ) );
			};
		};
	}, UMIDIDict, [
		[ \filter, [ '*/*', 'polytouch', 0, 0, nil ], UMIDIFilterSpec( 'polytouch' ) ],
	] ++ args, addToAll: false )
	.mappedArgs_([\toRange])
	.addStartFunc({ UMIDIDict.start( false ) })
);

defs = defs.add(
	ControllerUMapDef( \program, { |unit, midiDict ...args|
		var src, type, chan, num, value;
		#type, src, chan, num, value = args;
		if( type === \program ) {
			if( UMIDIDict.matchEvent( unit.get( \filter ), [ src, type, chan ] ) ) {
				mapFunc.value( unit, num, unit.get( \fromRange ) );
			};
		};
	}, UMIDIDict, [
		[ \filter, [ '*/*', 'program', 0, nil, nil ], UMIDIFilterSpec( 'program', useNum: false ) ],
	] ++ args, addToAll: false )
	.mappedArgs_([\toRange])
	.addStartFunc({ UMIDIDict.start( false ) })
);

MultiUMapDef( 'midi', defs, 'private', 'type', false ).mappedArgs_([ 'value', 'toRange' ]).canUseUMap_( false )