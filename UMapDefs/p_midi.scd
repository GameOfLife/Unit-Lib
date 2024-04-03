/*
\p_midi

Creates an UMap that listens to MIDI  messages, but specifically in UPatterns. It currently has two modes; 'cc' and 'note'. More modes may be added later. 'cc' mode will set each pattern event to the latest recorded control message. 'note' is a bit more complex, it detects active notes and chooses between them based on settings.

filter: an Array of Symbols and values to filter the incoming midi messages. The array is formatted as follows:
    [ <device/port>, type, chan, num, val ]
    <device/port>: a Symbol with the names of device and port as given by the system, divided by a '/'. The names may contain wildcards (*) which will be resolved to accept any device/port that matches the pattern. The default is * / *, meaning any device/port
   type: a Symbol for event type. This is ignored by the UMap as the type is set elsewhere in the args
   chan, num, val: MIDI channel, num and val to filter. Nil means any.
mode: ('note' type only) sets the note choice mode. Possible modes are:
   'highest': (default) use the highest currently playing note
   'lowest': use the lowest currently playing note
   'loudest': use the note with the highest velocity value
   'softest': use the note with the lowest velocity value
   'random': randomly pick a note from the currently playing notes
   'arp_up': index through the currently playing notes from low to high
   'arp_down': indext through the currently playing notes from high to low
in all modes, when no note is played the UMap will return the last chosen note
fromRange: the used range of the midi message
toRange: the output range of the UMap
invert: if true, the input range is inverted (i.e. low values become high and v.v.)
curve: a curvature value for the mapped range. If curve == 0, the range is linear.
clipMode: can be one of the following modes (Symbol):
'clip' (default): clip values outside fromRange
'fold': fold values outside fromRange
'wrap': wrap around values outside fromRange
'excess': map values outside the fromRange to min and max and return center value when inside fromRange
'none': no clipping applied (values can go outside toRange)
clipSoftness: softens the edges of 'clip' and 'fold' modes.

The 'toRange' arg range is mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

var defs = [];
var mapFunc, args;

mapFunc = { |value, fromRange, toRange, invert, curve, clipMode, clipSoftness|
	var scaleValue;
	if( invert == true ) {
		toRange = toRange.reverse;
	};
	curve = curve.clip2( 0.99932929973907 ).calcCurve( -1, 1 );
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
				value = scaleValue.value( excessed,
					[0 + (diff / 2) - mean, 127 - (diff/2) - mean],
					curve, \minmax
				)
				.blend( scaleValue.value( value, [0,127] ), clipSoftness );

			} {
				value = scaleValue.value( mean.blend( value, clipSoftness ), [0,127], \minmax );
			};
		},
		{
			value = scaleValue.value( value, fromRange );
		}
	);
	value.linlin(-1,1,*toRange ++ [ \none ]);
};

args = [
	[ \fromRange, [0,127], RangeSpec(0,127,step:1) ],
	[ \toRange, [0,1], RangeSpec(0,1) ],
	[ \invert, false, BoolSpec(false) ],
	[ \curve, 0, [-16,16,\lin,0,0].asSpec ],
	[ \clipMode, \clip, ListSpec( [ \clip, \fold, \wrap, \excess, \none ] ) ],
	[ \clipSoftness, 0, [0,1].asSpec ],
];

defs = defs.add(
	UPatDef( \cc, { |unit, filter = #[ '*/*', 'cc', 0, 0, nil ],
		fromRange = #[0,1], toRange = #[0,1], invert = false,
		curve = 0, clipMode = \clip, clipSoftness = 0|
		{
			var val = 0;
			UMIDIDict.start( false );
			loop {
				val = UMIDIDict.getEvent( *filter.next[[0,1,2,3]] ) ? val;
				mapFunc.value( val ? 0,
					*[ fromRange, toRange, invert, curve, clipMode, clipSoftness ].collect(_.next)
				).yield;
			};
		}.r;
	}, [
		[ \filter,  [ '*/*', 'cc', 0, 0, nil ], UMIDIFilterSpec('cc') ]
	] ++ args, addToAll: false )
	.mappedArgs_([\toRange])
);

defs = defs.add(
	UPatDef( \note, { |unit, filter = #[ '*/*', 'note', 0, nil, nil ],
		mode = 'highest',
		fromRange = #[0,1], toRange = #[0,1], invert = false,
		curve = 0, clipMode = \clip, clipSoftness = 0|
		{
			var val = 0, notes, index = 0;
			UMIDIDict.start( false );
			loop {
				notes = UMIDIDict.getEvent( *filter.next[[0,1,2]] );
				switch( mode.next,
					\highest, {
						val = notes.asPairs.clump(2).select({ |x| x[1] > 0 })
						.sort({ |a,b| a[0] <= b[0]; })
						.last !? _[0] ? val;
					},
					\lowest, {
						val = notes.asPairs.clump(2).select({ |x| x[1] > 0 })
						.sort({ |a,b| a[0] <= b[0]; })
						.first !? _[0] ? val;
					},
					\loudest, {
						val = notes.asPairs.clump(2).select({ |x| x[1] > 0 })
						.sort({ |a,b| a[1] <= b[1]; })
						.last !? _[0] ? val;
					},
					\softest, {
						val = notes.asPairs.clump(2).select({ |x| x[1] > 0 })
						.sort({ |a,b| a[1] <= b[1]; })
						.first !? _[0] ? val;
					},
					\random, {
						val = notes.asPairs.clump(2).select({ |x| x[1] > 0 })
						.choose !? _[0] ? val;
					},
					\arp_up, {
						val = notes.asPairs.clump(2).select({ |x| x[1] > 0 })
						.sort({ |a,b| a[0] <= b[0]; })
						.wrapAt( index ) !? _[0] ? val;
						index = index + 1;
					},
					\arp_down, {
						val = notes.asPairs.clump(2).select({ |x| x[1] > 0 })
						.sort({ |a,b| a[0] <= b[0]; })
						.wrapAt( index ) !? _[0] ? val;
						index = index - 1;
					},
				);
				mapFunc.value( val ? 0,
					*[ fromRange, toRange, invert, curve, clipMode, clipSoftness ].collect(_.next)
				).yield;
			};
		}.r;
	}, [
		[ \filter, [ '*/*', 'note', 0, nil, nil ], UMIDIFilterSpec( 'note', useNum: false ) ],
		[ \mode, 'highest',
			ListSpec(['highest', 'lowest', 'loudest', 'softest', 'random', 'arp_up', 'arp_down'])
		],
	] ++ args, addToAll: false )
	.mappedArgs_([\toRange])
);

MultiUMapDef( 'p_midi', defs, \private, 'type', false )
.allowedModes_(['init', 'sync', 'normal' ])
.mappedArgs_([ 'value', 'toRange' ]);