/*
\add

Creates an UMap adding (+) two values together. 

value: the value to add to
add: the amount to add
factor: multiplier for add (negative value results in subtraction0

The 'value' and 'add' arg ranges are mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/


UMapDef( \add, { |value = 0.0, add = 0.0, factor = 1|
	var sig;
	UMapDef.useMappedArgs = false; // use raw values instead of mapped (0-1)
	sig = value + (add * factor);
	UMapOut.kr(sig);
})
	.mappedArgs_([ \value, \add ])
	.setSpec( \factor, [-1,1,\lin,0,0].asSpec )
	.category_( 'operator' )