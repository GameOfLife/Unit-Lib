/*
\midi_touch

Creates an UMap that listens to MIDI aftertouch (channel pressure) messages. Works similar to all 'midi_xxx' UMapDefs.

value: the actual output value (this will change when a midi message is received, but can also be changed manually)
active: if true, the UMap will listen to the specified MIDI message. Hitting cmd-. will make the UMap stop listening, but whenever the Unit is started, it will re-activate. The MIDI client will be inited when the first midi_xxx UMap is made 'active'.
channel: the MIDI channel to listen to. 0-15 or nil (any channel).
learn: if learn is true, the 'channel' value will be changed according to any incoming MIDI aftertouch message.
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

The 'value' arg range is mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

ValueUMapDef( \midi_touch, { |unit, midifunc|
	var func;
	if(MIDIClient.initialized.not,{ MIDIIn.connectAll });
	midifunc.free;
	func = { |value, fromRange|
		var toRange, curve;
		toRange = unit.mapGet( \toRange );
		curve = unit.get( \curve );
		if (abs(curve) < 0.001) {
				value = value.linlin( *fromRange ++ [ -1, 1, \none ] );
		} {
			value = value.lincurve( *fromRange ++ [ -1, 1, unit.get( \curve ), \none ] );
		};
		if( unit.get( \invert ).booleanValue ) {
			value = value.neg;
		};
		switch( unit.get( \clipMode ),
			\clip, { value = value.softclip2(1, unit.get( \clipSoftness ) ) },
			\fold, { value = value.softfold2(1, unit.get( \clipSoftness )) },
			\wrap, { value = value.wrap2(1) }
		);
		value = value.linlin(-1,1,*toRange ++ [ \none ]);
		unit.mapSet( \value, value );
	};
	MIDIFunc.touch({ |value, chan, src|
		var fromRange, endPoint;
		endPoint = MIDIClient.sources.detect({ |ep| ep.uid == src });
		if( unit.get( \learn ).booleanValue == true ) {
			unit.set( \channel, chan );
			unit.set( \device, endPoint !? _.device ? "" );
			unit.set( \port, endPoint !? _.name ? "" );
			unit.set( \learn, false );
		};
		fromRange = unit.get( \fromRange );
		if( endPoint.isNil or: { unit.get( \device ).size == 0 or: {
				endPoint.device.find( unit.get( \device ) ).notNil;
			}
		}) { if( endPoint.isNil or: { unit.get( \port ).size == 0 or: {
					endPoint.name.find( unit.get( \port ) ).notNil;
				}
			}) { if( unit.get( \channel ) !? (_ == chan) ? true ) {
					if( unit.get( \clipMode ) === \ignore ) {
						if( value.inclusivelyBetween( *fromRange ) ) {
							func.value( value, fromRange );
						};
					} {
						func.value( value, fromRange );
					};
				};
			};
		};
	}).permanent_( true );
}, { |unit, midifunc|
	midifunc.free;
	nil;
}, [
	[ \channel, nil, ListSpec( [ nil ] ++ (..15), 0, [ "any" ] ++ (..15).collect(_.asString) ) ],
	[ \device, "", StringSpec() ],
	[ \port, "", StringSpec() ],
	[ \learn, false, BoolSpec( false ) ],
	[ \fromRange, [0,127], RangeSpec(0,127,step:1) ],
	[ \toRange, [0,1], RangeSpec(0,1) ],
	[ \invert, false, BoolSpec(false) ],
	[ \curve, 0, [-16,16,\lin,0,0].asSpec ],
	[ \clipMode, \clip, ListSpec( [ \ignore, \clip, \fold, \wrap, \none ] ) ],
	[ \clipSoftness, 0, [0,1].asSpec ],
]  )
.uchainInitFunc_({ |umap, unit|
	var filter, invert, active, curve;
	if( umap.def.category == \private ) {
		filter = [
			UMIDIFilterSpec.formatDeviceString( umap.get( \device ) ++ "*/" ++ umap.get( \port ) ++ "*" ).asSymbol,
			'touch',
			umap.get( \channel ),
			nil,
			nil
		];
		curve = umap.get( \curve );
		curve = 0.linlin(-1,1,-1,1,curve);
		active = umap.get( \active ) == true;
		if( active ) { umap.set( \active, false ) };
		umap.defName = \midi;
		umap.set( \curve, curve );
		umap.set( \filter, filter );
		if( active ) { umap.set( \active, true ) };
	}
})
.mappedArgs_([\toRange])
.category_(\private);