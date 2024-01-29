/*
\min

This UMap outputs the lowest (minumum) out of two input values

a: the first value
b: the second value

a and b arg ranges are mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

HybridUMapDef( \min, { |a = 0.5, b = 0.5|
	UMapOut.kr( a.min(b) );
}, { |unit, a = 0.5, b = 0.5|
	a.min(b);
})
.mappedArgs_([ \a, \b ])
.setSpec( \a, UAdaptSpec() )
.setSpec( \b, UAdaptSpec() )
.category_( 'math' )