/*
\p_midi_cc

Creates an UMap that listens to MIDI control messages, but specifically in UPatterns. Use this instead of the normal 'midi_cc' UMapDef when using MIDI in a pattern, otherwise Unit Lib may crash! 'p_midi_cc' polls values recorded by UMIDIDict, which (after activation) keeps track of all incoming MIDI data.

Tip: to 'learn' a controller, first select a 'midi_cc' UMapDef, learn the controller number etc. there, and then switch to a 'p_midi_cc'. The settings will remain the same.

cc: the control number (0-127) to listen to
channel: the MIDI channel to listen to. 0-15 or nil (any channel).
learn: if learn is true, the 'channel' and 'cc' values will be changed according to any incoming MIDI control message, as long as 'learn' is true.
device: device name, intelligently looks at device names that contain this string.
device: port name, intelligently looks at device names that contain this string.
-- if device and port are both "" (default), 'p_midi_cc' will listen to any device
fromRange: the used range of the midi message
toRange: the output range of the UMap
invert: if true, the input range is inverted (i.e. low values become high and v.v.)
curve: a curvature value for the mapped range. If curve == 0, the range is linear.
clipMode: can be one of the following modes (Symbol):
'ignore': ignore values outside fromRange
'clip' (default): clip values outside fromRange
'fold': fold values outside fromRange
'wrap': wrap around values outside fromRange
'none': no clipping applied (values can go outside toRange)
clipSoftness: softens the edges of 'clip' and 'fold' modes.

The 'toRange' arg range is mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UPatDef( \p_midi_cc, { |unit,
	cc=0,
	channel = 0,
	device = "",
	port = "",
	fromRange = #[0,1],
	toRange = #[0,1],
	invert = false,
	curve = 0,
	clipMode = \clip,
	clipSoftness = 0|

	var scaleFunc, findUIDs, val = 0;

	scaleFunc = { |value|
		var ccurve;
		ccurve = curve.next;
		if (abs(ccurve) < 0.001) {
			value = value.linlin( *fromRange.next ++ [ -1, 1, \none ] );
		} {
			value = value.lincurve( *fromRange.next ++ [ -1, 1, ccurve, \none ] );
		};
		if( invert.next.booleanValue ) {
			value = value.neg;
		};
		switch( clipMode.next,
			\clip, { value = value.softclip2(1, clipSoftness.next ) },
			\fold, { value = value.softfold2(1, clipSoftness.next ) },
			\wrap, { value = value.wrap2(1) }
		);
		value.linlin(-1,1,*toRange.next ++ [ \none ]);
	};

	{
		var src, val = 0, cchan, nnum;
		UMIDIDict.start( false );
		loop {
			src = (device.next ++ "*/" ++ port.next ++ "*").asSymbol;
			cchan = channel.next;
			nnum = cc.next;
			val = UMIDIDict.getEvent( src, \cc, cchan, nnum ) ? val;
			if( val.size > 0 ) { val = val.first };
			scaleFunc.value( val ? 0 ).yield;
		};
	}.r;
}, [
	[ \cc, 0, PositiveIntegerSpec(maxval:127) ],
	[ \channel, nil, ListSpec( [ nil ] ++ (..15), 0, [ "any" ] ++ (..15).collect(_.asString) ) ],
	[ \device, "", StringSpec() ],
	[ \port, "", StringSpec() ],
	[ \fromRange, [0,127], RangeSpec(0,127,step:1) ],
	[ \toRange, [0,1], RangeSpec(0,1) ],
	[ \invert, false, BoolSpec(false) ],
	[ \curve, 0, [-16,16,\lin,0,0].asSpec ],
	[ \clipMode, \clip, ListSpec( [ \clip, \fold, \wrap, \none ] ) ],
	[ \clipSoftness, 0, [0,1].asSpec ],
]  )
.mappedArgs_([\toRange])
.uchainInitFunc_({ |umap, unit|
	var filter, invert, active, curve;
	if( umap.def.category == \private ) {
		filter = [
			UMIDIFilterSpec.formatDeviceString( umap.get( \device ) ++ "*/" ++ umap.get( \port ) ++ "*" ).asSymbol,
			'cc',
			umap.get( \channel ),
			umap.get( \cc ),
			nil
		];
		curve = umap.get( \curve );
		curve = 0.linlin(-1,1,-1,1,curve);
		umap.defName = \p_midi;
		umap.set( \curve, curve );
		umap.set( \filter, filter );
	}
})
.category_(\private);