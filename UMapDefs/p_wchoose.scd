/*
\p_wchoose

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

defs = [ 8,12,16,24,32,48,64,128 ].collect({ |n|
	UPatDef( n, { |unit, vals, weights, seed|
		{	
			thisThread.randSeed = seed.next.asControlInput;
			loop {
				vals.at( (..n-1).wchoose( weights.next.normalizeSum ) ).yield;
			};
		}.r
	}, addToAll: false )
		.category_( \pattern )
		.setSpec( \seed, URandSeed )
		.setDefault( \seed, URandSeed() )
		.setSpec( \vals, ArrayControlSpec(0,1,\lin,default: 0.5!n ).size_(n) )
		.setSpec( \weights, ArrayControlSpec(0,1,\lin,default: 0.5!n ).size_(n) )
		.mappedArgs_([ \vals ]);
});	
	
MultiUMapDef( \p_wchoose, defs, \pattern_random, \n, false )
	.mappedArgs_([ \vals ])
	.allowedModes_([ \init, \sync, \normal ])