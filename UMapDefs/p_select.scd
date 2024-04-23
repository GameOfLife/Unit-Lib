/*
\p_select

Creates an UMap that can select a value from an Array of values in a UPattern.

index: the index (0-n-1)
vals: the Array of values
n: the total number of values

The 'vals' arg ranges are mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

var defs;

defs = [2,3,4,5,6,7,8,9,10,11,12,14,16,18,20,24,28,32,48,64,96,128 ].collect({ |n|
	UPatDef( n, { |unit, index = 0, vals|
		{
			inf.do({
				vals.next.wrapAt( index.next ).yield;
			});
		}.r
	}, addToAll: false )
	.category_( \pattern )
	.setSpec( \index, IntegerSpec(0,0,n-1) )
	.setSpec( \vals, ArrayControlSpec(0,1,\lin,default: 0.5!n ).size_(n) )
	.mappedArgs_([ \vals ]);
});

MultiUMapDef( \p_select, defs, \pattern_selection, \n, false )
.mappedArgs_([ \vals ])
.defaultDefName_( 8 )
.allowedModes_([ \init, \sync, \normal ])