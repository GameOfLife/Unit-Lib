/*
\trig_select_8

Creates an UMap that can select one from 8 values via a trigger(or UMaps). When the end is reached, it wraps around to the first value. If you need to select from > 8 values, use \trig_select_16 instead.

trigger: switch to next value
reset: return to first value
step: step size
reverse: if true, step backwards instead of forward
range: min and max index to choose from
lag: lag time between steps (linear lag)
value0 - value7: the values of the steps (can be UMaps)

The 'value[0-7]' arg ranges are mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UMapDef( \trig_select_8, {
	var trigger, reset;
	var step, range, lag;
	var index, values, sig;
	Udef.buildUdef.dontStoreSynthDef = true;
	trigger = \trigger.tr;
	reset = \reset.tr;
	step = \step.kr(1) * \reverse.kr(0).linlin(0,1,1,-1);
	range =\range.kr([0,7]);
	lag = \lag.kr(0.0);
	index = Stepper.kr( trigger, reset, range[0], range[1], step );
	values = 8.collect({ |i|
		("value"++i).asSymbol.ukr(i/7).linlin(0,1,-1,1);
	});
	sig = SelectCF.kr( index, values, lag, 0 ).linlin(-1,1,0,1);
	UMapOut.kr( sig );
}).category_( \private )
	.uchainInitFunc_({ |unit, chain|
		var values, range;
		values = 8.collect({ |i| unit.get( ("value"++i).asSymbol ) });
		range = unit.range;
		unit.def = \trig_select;
		unit.n = values.size;
		unit.range = range;
		if( values.any( _.isKindOf( UMap ) ) ) {
			unit.vals = [ \expand,
				8.collect({ |i| [ ("value"++i).asSymbol, values[i] ] }).flatten(1);
			];
		} {
			unit.vals = values;
		};
		unit
	})
	.setSpec( \trigger, TriggerSpec() )
	.setSpec( \reset, TriggerSpec() )
	.setSpec( \step, [1,8,\lin,1,1].asSpec )
	.setSpec( \range, [0,7,\lin,1].asSpec.asRangeSpec )
	.setSpec( \reverse, BoolSpec(false) )
	.mappedArgs_( 8.collect({ |i| ("value"++i).asSymbol }) );