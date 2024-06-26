/*
\p_sequencer

Creates an UMap that can sequence a number of values in a UPattern. Each next event gets a new value from the list of values. To use UMaps as values, apply an 'expand' UMap to the 'values' parameter.

range: the range (0-n) of used steps.
reverse: if true, move backwards through the steps
vals: the values of the steps
n: the total number of values

The 'vals' arg ranges are mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

var defs;

defs = [2,3,4,5,6,7,8,9,10,11,12,14,16,18,20,24,28,32,48,64,96,128 ].collect({ |n|
	UPatDef( n, { |unit, range = #[0,7], reverse = false, loop = true, vals|
		{
			var current = 0, direction = 1;
			current = range.next[0];
			vals.at( current ).yield;
			inf.do { |i|
				direction = if( reverse.next.booleanValue == true ) { -1 } { 1 };
				if( loop.next.booleanValue == true ) {
					current = (current + direction).wrap( *range.next + [0,1] );
				} {
					current = (current + direction).clip( *range.next + [0,0] );
				};
				vals.at( current ).yield;
			};
		}.r
	}, addToAll: false )
		.category_( \pattern )
		.setSpec( \range, [0,n-1,\lin,1].asSpec.asRangeSpec.default_([0,n-1]) )
		.setDefault( \range, [0,n-1] )
		.setSpec( \reverse, BoolSpec(false) )
	    .setSpec( \loop, BoolSpec(true) )
		.setSpec( \vals, ArrayControlSpec(0,1,\lin,default: 0.5!n ).size_(n) )
		.mappedArgs_([ \vals ]);
});

MultiUMapDef( \p_sequencer, defs, \pattern_series, \n, false )
	.mappedArgs_([ \vals ])
	.defaultDefName_( 8 )
	.allowedModes_([ \init, \sync, \normal ])