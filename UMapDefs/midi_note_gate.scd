ValueUMapDef( \midi_note_gate, { |unit, midifuncs|
	var func;
	if(MIDIClient.initialized.not,{ MIDIIn.connectAll });
	midifuncs.do(_.free);
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
	[
	MIDIFunc.noteOn({ |value, nn, chan, src|
		var fromRange;
		if( unit.get( \learn ).booleanValue == true ) {
			unit.set( \nn, nn );
			unit.set( \channel, chan );
		};
		fromRange = unit.get( \fromRange );
		if( unit.get( \channel ) !? (_ == chan) ? true && {
			 unit.get( \nn ) == nn;
		}) {
			if( unit.get( \clipMode ) === \ignore ) {
				if( value.inclusivelyBetween( *fromRange ) ) {
					func.value( value, fromRange );
				};
			} {
				func.value( value, fromRange );
			};
		};
	}),
	MIDIFunc.noteOff({ |velo, nn, chan, src|
		var fromRange, value = 0;
		if( unit.get( \learn ).booleanValue == true ) {
			unit.set( \nn, nn );
			unit.set( \channel, chan );
		};
		fromRange = unit.get( \fromRange );
		if( unit.get( \channel ) !? (_ == chan) ? true && {
			 unit.get( \nn ) == nn;
		}) {
			if( unit.get( \clipMode ) === \ignore ) {
				if( value.inclusivelyBetween( *fromRange ) ) {
					func.value( value, fromRange );
				};
			} {
				func.value( value, fromRange );
			};
		};
	})
	];
}, { |unit, midifuncs|
	midifuncs.do(_.free);
	nil;
}, [ 
	[ \nn, [0,127], [0,127,\lin].asSpec ],
	[ \channel, nil, ListSpec( [ nil ] ++ (..15), 0, [ "any" ] ++ (..15).collect(_.asString) ) ],
	[ \learn, false, BoolSpec( false ) ],
	[ \fromRange, [0,127], RangeSpec(0,127,step:1) ],
	[ \toRange, [0,1], RangeSpec(0,1) ], 
	[ \invert, false, BoolSpec(false) ],
	[ \curve, 0, [-16,16,\lin,0,0].asSpec ],
	[ \clipMode, \clip, ListSpec( [ 
			\ignore, \clip, \fold, \wrap, \tanh, \softclip, \distort, \wrap_sine, \none 
		] ) 
	]
]  )
	.mappedArgs_([\toRange])
	.category_(\control);