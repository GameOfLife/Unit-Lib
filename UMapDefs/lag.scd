/*
\lag

An UMap that slows down value changes over time, useful for smoothing control inputs (such as MIDI values). In 'normal' mode the lag filter uses one-pole low pass filter, in 'up_down' mode the same filter is used but separate times for upwards and downwards lag can be set. The 'linear' mode creates straight lines between the old and new values. The 'lpf' mode uses a second order low pass filter, causing a slight resonance after the value reaches its new position.

value: the value upon which the lag is applied (this can be another UMap as well)
time: the time it takes to move to within 0.01% towards a new value
order: (1-4) order of the lag, a higher order produces a smoother lag curve. The UMap cascades four lag filters, and the order parameter sets how many of them are actually used.
timeUp: the time it takes to move upwards to within 0.01% towards a new value (only in 'up_down' mode)
timeDown: the time it takes to move downwards to within 0.01% towards a new value (only in 'up_down' mode)
curve: the curvature of a linear lag (only in 'linear' mode)
type: the mode of the filter ('normal', 'up_down', 'linear', 'lpf')

The value arg range is mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

var defs;

defs = [

	UMapDef( \normal, { |value = 0.5, time = 0.1, order = 1|
		4.do({ |i|
			var amt;
			amt = (order - i).clip(0,1) * (order ** -0.5 );
			value = value.lag( time * amt );
		});
		UMapOut.kr(value);
	}, addToAll: false, extraPrefix: "lag_" )
		.setSpec( \time, [0,10, 1.calcCurve(0,10) ].asSpec )
		.setSpec( \order, [1,4,\lin,0,1].asSpec )
		.mappedArgs_([ \value ]),

	UMapDef( \up_down, { |value = 0.5, timeUp = 0.1, timeDown = 0.1, order = 1|
		4.do({ |i|
			var amt;
			amt = (order - i).clip(0,1) * (order ** -0.5 );
			value = value.lag( timeUp * amt, timeDown * amt );
		});
		UMapOut.kr(value);
	}, addToAll: false, extraPrefix: "lag_" )
		.setSpec( \timeUp, [0,10, 1.calcCurve(0,10) ].asSpec )
		.setSpec( \timeDown, [0,10, 1.calcCurve(0,10) ].asSpec )
		.setSpec( \order, [1,4,\lin,0,1].asSpec )
		.mappedArgs_([ \value ]),

	UMapDef( \linear, { |value = 0.5, time = 0.1, curve = 0.0|
		UMapOut.kr(value.varlag(time, curve));
	}, addToAll: false, extraPrefix: "lag_" )
		.setSpec( \time, [0,10, 1.calcCurve(0,10) ].asSpec )
		.setSpec( \curve, [-40,40,\lin].asSpec )
		.mappedArgs_([ \value ]),

	UMapDef( \lpf, { |value = 0.5, time = 0.0|
		UMapOut.kr( LPFLag.kr( value, time).clip(0,1) );
	}, addToAll: false, extraPrefix: "lag_" )
		.setSpec( \time, [0,10, 1.calcCurve(0,10) ].asSpec )
		.mappedArgs_([ \value ])
];

MultiUMapDef( \lag, defs, \filter, \type, false )
	.prepareArgsFunc_({ |args|
		var timeUp, timeDown;
		if( args.pairsAt( \type ).isNil && { args.pairsAt( \time ).isNil}) {
			timeUp = args.pairsAt( \timeUp );
			timeDown = args.pairsAt( \timeDown );
			if( timeUp == timeDown ) {
				if( timeUp.notNil ) {
					args = args ++ [ \time, timeUp ];
				};
			} { args = args ++ [ \type, \up_down ]; };
		};
		args;
	})
	.mappedArgs_([ \value ]);
