ValueUMapDef( \midi_cc, { |unit, midifunc|
	if(MIDIClient.initialized.not,{ MIDIIn.connectAll });
	midifunc.remove;
	MIDIFunc.cc({ |value, num, chan, src|
		var fromRange, toRange, curve;
		if( unit.get( \learn ).booleanValue == true ) {
			unit.set( \cc, num );
		};
		fromRange = unit.get( \fromRange );
		toRange = unit.mapGet( \toRange );
		curve = unit.get( \curve );
		if( num.asInt == unit.get( \cc ).asInt ) {
			if (abs(curve) < 0.001) {
				value = value.linlin( *fromRange ++ [ -1, 1, \none ] );
			} {
				value = value.lincurve( *fromRange ++ [ -1, 1, unit.get( \curve ), \none ] );
			};
			if( unit.get( \invert ).booleanValue ) {
				value = value.neg;
			};
			switch( unit.get( \clipMode ),
				\clip, { value = value.clip2(1) },
				\fold, { value = value.fold2(1) },
				\wrap, { value = value.wrap2(1) },
				\tanh, { value = value.tanh },
				\softclip, { value = value.softclip },
				\distort, { value = value.distort },
				\wrap_sine, { value = (value.wrap2(1) * pi).sin }
			);
			value = value.linlin(-1,1,*toRange ++ [ \none ]);
			unit.mapSet( \value, value );
		};
	});
}, { |unit, midifunc|
	midifunc.free;
	nil;
}, [ 
	[ \cc, 0, PositiveIntegerSpec(maxval:127) ],
	[ \learn, false, BoolSpec( false ) ],
	[ \fromRange, [0,127], RangeSpec(0,127,step:1) ],
	[ \toRange, [0,1], RangeSpec(0,1) ], 
	[ \invert, false, BoolSpec(false) ],
	[ \curve, 0, [-16,16,\lin,0,0].asSpec ],
	[ \clipMode, \clip, ListSpec( [ 
			\clip, \fold, \wrap, \tanh, \softclip, \distort, \wrap_sine, \none 
		] ) 
	]
]  )
	.mappedArgs_([\toRange])
	.category_(\control);