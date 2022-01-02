/*
\select_16

Creates an UMap that can select one from 16 values (or UMaps). If you need to select from <= 8 values, use \select_8 instead.

index: the index of the value (0-15)
smooth: smoothening parameter (0: hard steps, 1: linear interpolation)
value0 - value15: the values of the steps (can be UMaps)

The 'value[0-15]' arg ranges are mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UMapDef( \select_16, {
	var values, smooth;
	var phase, step, resetPos, sig;
	Udef.buildUdef.dontStoreSynthDef = true;
	phase = \index.kr(0);
	smooth = \smooth.kr(0.0);
	values = 16.collect({ |i|
		("value"++i).asSymbol.ukr(0.5);
	});
	phase = (phase.floor * (1-smooth)) + (phase * smooth);
	sig = LinSelectX.kr( phase, values );
	UMapOut.kr( sig );
}).category_( \private )
	.uchainInitFunc_({ |unit, chain|
		var values, smooth;
		values = 16.collect({ |i| unit.get( ("value"++i).asSymbol ) });
		smooth = unit.smooth;
		unit.def = \select;
		unit.interpolate = smooth.booleanValue;
		unit.n = values.size;
		if( values.any( _.isKindOf( UMap ) ) ) {
			unit.vals = [ \expand,
				16.collect({ |i| [ ("value"++i).asSymbol, values[i] ] }).flatten(1);
			];
		} {
			unit.vals = values;
		};
		unit
	})
	.setSpec( \index, [0,15].asSpec )
	.mappedArgs_( 16.collect({ |i| ("value"++i).asSymbol }) );