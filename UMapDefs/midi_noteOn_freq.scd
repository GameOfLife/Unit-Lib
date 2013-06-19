ValueUMapDef( \midi_noteOn_freq, { |unit, midifunc|
	var func;
	if(MIDIClient.initialized.not,{ MIDIIn.connectAll });
	midifunc.remove;
	func = { |value, range|
		var outSpec;
		outSpec = unit.getSpec( \value );
		value = value - 69;
		if( unit.get( \invert ).booleanValue ) {
			value = value.neg;
		};
		value = (value * unit.get( \scale )) + unit.get( \transpose );
		value = (value + 69).midicps * (unit.get(\a)/440);
		if( outSpec.isKindOf( FreqSpec ) ) {
			unit.set( \value, value.clip( outSpec.minval, outSpec.maxval ) );
		} {
			unit.mapSet( \value, \freq.asSpec.unmap(value) );
		};
	};
	MIDIFunc.noteOn({ |velo, value, chan, src|
		var range;
		if( unit.get( \learn ).booleanValue == true ) {
			unit.set( \channel, chan );
		};
		range = unit.get( \range );
		if( unit.get( \channel ) !? (_ == chan) ? true ) {
			if( value.inclusivelyBetween( *range ) ) {
				func.value( value, range );
			};
		};
	});
}, { |unit, midifunc|
	midifunc.free;
	nil;
}, [ 
	[ \channel, nil, ListSpec( [ nil ] ++ (..15), 0, [ "any" ] ++ (..15).collect(_.asString) ) ],
	[ \learn, false, BoolSpec( false ) ],
	[ \range, [0,127], RangeSpec(0,127,step:1) ],
	[ \a, 440, FreqSpec(400,480) ],
	[ \transpose, 0, [-36,36,\lin,0,0].asSpec ],
	[ \scale, 1, [0.25,4,\exp,0,1].asSpec ],
	[ \invert, false, BoolSpec(false) ],
]  )
	.category_(\control);