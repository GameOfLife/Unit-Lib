/*
\index_of_nearest

Creates an UMap that can compare a number of input values against a single given value. The UMap will output the index of the value nearest to the 'nearestTo' value.

vals: the values to test
nearestTo: the value to compare to
range: the output range as mapped to the range of the receiver.


The 'range' arg is mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

var defs;

defs = [ 2,3,4,5,6,7,8 ].collect({ |n|
	HybridUMapDef( n, {
		var array, nearestTo, range, out;
		var phase, step, resetPos, sig;
		array = \vals.kr( { |i| i/(n-1) }!n );
		nearestTo = \nearestTo.kr( 0.5 );
		range = \range.kr( [0.0,1.0] );
		array = array.collect(_.absdif( nearestTo ));
		out = [ array[0], 0 ];
		array[1..].do({ |item,i|
			out = if( item < out[0], [ item, i+1 ], out );
		});
		out = out[1].linlin(0, n-1, *range ++ [ \none ]);
		UMapOut.kr( out );
	}, { |unit, vals = #[0.5], nearestTo = 0.5, range = #[0.0,1.0] |
		vals.collect(_.absdif( nearestTo ))
		.minIndex
		.linlin(0, n-1, *range ++ [ \none ] )
	}, addToAll: false, extraPrefix: "index_of_nearest" ).category_( \operator )
	    .mappedArgs_( [ \range ] )
		.setSpec( \vals, ArrayControlSpec(0,1,\lin,default: 0.5!n ).size_(n) )
	    .setSpec( \nearestTo, ControlSpec(0,1,\lin,default: 0.5 ) )
	    .setSpec( \range, ControlSpec(0.0,1.0,\lin).asRangeSpec )
});

MultiUMapDef( \index_of_nearest, defs, \operator, \n, false )
	.allowedModes_( [ \init, \sync, \normal ] )
    .mappedArgs_( [ \range ] );