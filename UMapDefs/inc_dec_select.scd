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

defs = [ 2,3,4,5,6,7,8,10,12,16,24,32,48,64,128 ].collect({ |n|
	UMapDef( n, {
		var inc, dec, reset, resetIndex;
		var wrap, lag;
		var index, values, sig;
		inc = Trig1.kr( \inc.tr, ControlDur.ir );
		dec = Trig1.kr( \dec.tr, ControlDur.ir );
		reset = Trig1.kr( \reset.tr(1), ControlDur.ir );
		resetIndex = \resetIndex.kr(0);
		wrap = \wrap.kr(0);
		lag = \lag.kr(0.0);
		index = LocalIn.kr( 1, resetIndex );
		index = (index + inc - dec).round(1);
		index = Select.kr( (wrap + (reset * 2)).clip(0,2), [
			index.clip(0,n-1), 
			index.wrap(0,n), 
			resetIndex,
		]);
		LocalOut.kr(index);
		//index = Stepper.kr( trigger, reset, range[0], range[1], step );
		values = \vals.kr( { |i| i/(n-1) }!n ).linlin(0,1,-1,1);
		sig = SelectCF.kr( index, values, lag, 0 ).linlin(-1,1,0,1);
		UMapOut.kr( sig );
	}, addToAll: false, extraPrefix: "inc_dec_select")
		.setSpec( \inc, TriggerSpec() )
		.setSpec( \dec, TriggerSpec() )
		.setSpec( \reset, TriggerSpec() )
		.setSpec( \resetIndex, IntegerSpec(0, 0, n-1) )
		.setSpec( \wrap, BoolSpec(false) )
		.setSpec( \vals, ArrayControlSpec(0,1,\lin,default: { |i| i/(n-1) }!n ).size_(n) )
		.mappedArgs_([ \vals ]);
});	
	
MultiUMapDef( \inc_dec_select, defs, \trigger_to_value, \n, false )
	.defaultDefName_( 8 )
	.mappedArgs_([ \vals ]);