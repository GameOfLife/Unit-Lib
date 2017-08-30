/*
\sequencer

Creates an UMap that can sequence a number of values over time. To use UMaps as values, apply an 'expand' UMap to the 'values' parameter.

speed: number of steps per second
reverse: if true, move backwards through the steps
range: the range (0-n) of used steps.
interpolate: linear interpolation between steps on/off
vals: the values of the steps
n: the total number of values

The 'vals' arg ranges are mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

var defs;

defs = [ 8,12,16,24,32,48,64,128 ].collect({ |n|
	UMapDef( n, {
		var values, speed, reverse, range, smooth;
		var phase, step, resetPos, sig;
		speed = \speed.kr(10);
		reverse = \reverse.kr(0);
		range =\range.kr([0,31]);
		smooth = \interpolate.kr(0.0);
		values = \vals.kr( { |i| i/(n-1) }!n );
		step = ((ControlDur.ir + SampleDur.ir) * speed) * reverse.linlin(0,1,1,-1);
		range = range + [0,1];
		resetPos = (range[0] - step) + 
			((\u_startPos.ir(0.0) * ControlRate.ir) * step).wrap( 0, range[1] - range[0] );
		phase = Phasor.kr( Impulse.kr(0), step, range[0], range[1], resetPos );
		phase = (phase.floor * (1-smooth)) + (phase * smooth);
		sig = LinSelectX.kr( phase, values );
		UMapOut.kr( sig );
	}, addToAll: false, extraPrefix: "sequencer")
		.category_( \modulation )
		.setSpec( \speed, [0,300, 4,0,10].asSpec )
		.setSpec( \interpolate, BoolSpec(false) )
		.setSpec( \reverse, BoolSpec(false) )
		.setSpec( \range, [0,n-1,\lin,1].asSpec.asRangeSpec )
		.setSpec( \vals, ArrayControlSpec(0,1,\lin,default: 0.5!n ).size_(n) )
		.mappedArgs_([ \vals ]);
});	
	
MultiUMapDef( \sequencer, defs, \modulation, \n, false )
	.mappedArgs_([ \vals ]);