/*
\select

Creates an UMap that can select one from a number values. An 'expand' UMap can be used on the 'values' parameter to use UMaps on values.

index: the index of the value (0-n)
interpolate: linear interpolation between steps on/off
vals: the values to select from
n: the total number of values (8-128)


The 'values' arg ranges are mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

var defs;

defs = [ 8,12,16,24,32,48,64,128 ].collect({ |n|
	HybridUMapDef( n, {
		var values, interpolate;
		var phase, step, resetPos, sig;
		UMapDef.useMappedArgs_( false );
		phase = \index.kr(0);
		interpolate = \interpolate.kr(0);
		values = \vals.kr( { |i| i/(n-1) }!n );
		phase = if( interpolate, phase, phase.floor );
		sig = SelectL.kr( phase, values );
		UMapOut.kr( sig );
	}, { |unit, index = 0, interpolate = 0, vals = #[0.5]|
		var out;
		out = vals.asCollection.wrapAt( index.asInteger );
		if( interpolate.booleanValue ) {
			out = out.blend( vals.asCollection.wrapAt( index.asInteger + 1 ), index.frac );
		};
		out;
	}, addToAll: false, extraPrefix: "select" ).category_( \choice )
		.setSpec( \index, [0,n-1].asSpec )
		.valueIsMapped_( false )
		.setSpec( \interpolate, BoolSpec(false) )
		.setSpec( \vals, ArrayControlSpec(0,1,\lin,default: 0.5!n ).size_(n) )
		.mappedArgs_( [ \vals ] );
});

MultiUMapDef( \select, defs, \selection, \n, false )
	.mappedArgs_([ \vals ])
	.allowedModes_( [ \init, \sync, \normal ] );