/*
\p_choose

Creates an pattern UMap that chooses between multiple values, with an optional exclusion history. To use UMaps as values, apply an 'expand' UMap to the 'vals' parameter.


vals: values to choose from
histSize: size of history buffer; the amount of steps before a value can repeat itself.
seed: random seed.
n: total number of values.

The 'vals' arg ranges are mapped to that of the parameter to which the UMap is connected

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

var defs;

defs = [ 3,4,5,6,7,8,10,12,16,24,32,48,64,128 ].collect({ |n|
	UPatDef( n, { |unit, vals, histSize = 0, seed|
		{
			var index, size, arr, nn;
			thisThread.randSeed = seed.next.asControlInput;
			arr = (..n-1).scramble;
			loop {
				index = arr.removeAt( histSize.next.asInteger rrand: (n-1).asInteger );
				arr = arr.addFirst( index );
				vals.at( index ).yield;
			};
		}.r
	}, addToAll: false )
		.category_( \pattern )
		.setSpec( \histSize, IntegerSpec(0,0,n-1) )
		.setSpec( \seed, URandSeed )
		.setDefault( \seed, URandSeed() )
		.setSpec( \vals, ArrayControlSpec(0,1,\lin,default: 0.5!n ).size_(n) )
		.mappedArgs_([ \vals ]);
});

MultiUMapDef( \p_choose, defs, \pattern_random, \n, false )
	.mappedArgs_([ \vals ])
	.defaultDefName_( 8 )
	.allowedModes_([ \init, \sync, \normal ])