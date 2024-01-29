/*
\trig_select

Creates an UMap that can select one from a number values via a trigger(or UMaps). When the end is reached, it wraps around to the first value.

trigger: switch to next value
reset: return to first value
step: step size
reverse: if true, step backwards instead of forward
range: min and max index to choose from
lag: lag time between steps (linear lag)
vals: the values of the steps (use 'expand' UMap if you want to use UMaps for these)
n: number of values

The 'vals' arg ranges are mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

var defs;

defs = [ 8,12,16,24,32,48,64,128 ].collect({ |n|
	UMapDef( n, {
		var trigger, reset;
		var step, range, lag;
		var index, values, sig;
		trigger = \trigger.tr;
		reset = \reset.tr;
		step = \step.kr(1) * \reverse.kr(0).linlin(0,1,1,-1);
		range =\range.kr([0,n-1]);
		lag = \lag.kr(0.0);
		index = Stepper.kr( trigger, reset, range[0], range[1], step );
		values = \vals.kr( { |i| i/(n-1) }!n ).linlin(0,1,-1,1);
		sig = SelectCF.kr( index, values, lag, 0 ).linlin(-1,1,0,1);
		UMapOut.kr( sig );
	}, addToAll: false, extraPrefix: "trig_select")
		.setSpec( \trigger, TriggerSpec() )
		.setSpec( \reset, TriggerSpec() )
		.setSpec( \step, [1,n,\lin,1,1].asSpec )
		.setSpec( \reverse, BoolSpec(false) )
		.setSpec( \range, [0,n-1,\lin,1].asSpec.asRangeSpec )
		.setSpec( \vals, ArrayControlSpec(0,1,\lin,default: { |i| i/(n-1) }!n ).size_(n) )
		.mappedArgs_([ \vals ]);
});

MultiUMapDef( \trig_select, defs, \selection, \n, false )
	.mappedArgs_([ \vals ]);