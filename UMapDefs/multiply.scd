/*
\multiply

Creates an UMap for multiplication (*) of two values.
 
value: the value
multiply: the multiplier
negative: multiply with negative value
factor: 


The value arg range is mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/


UMapDef( \multiply, { |value = 0.0, multiply = 1.0, negative = 0, factor = 0|
	var sig;
	UMapDef.useMappedArgs = false; // use raw values instead of mapped (0-1)
	sig = value * (multiply * negative.linlin(0,1,1,-1) * (10**factor));
	UMapOut.kr(sig);
})
	.mappedArgs_([ \value ])
	.setSpec( \multiply, [0,2,\lin,0,1].asSpec )
	.setSpec( \negative, BoolSpec(false) )
	.setSpec( \factor, ListSpec( [-2,-1,0,1,2,3,4,5], 2, 
		[-2,-1,0,1,2,3,4,5].collect({ |x| (10**x).asString })
	 ) )
	.category_( 'utility' )