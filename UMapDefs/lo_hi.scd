
UMapDef( \lo_hi, { |lo = 0.0, hi = 1.0|
	UMapOut.kr([lo,hi]);
})
	.mappedArgs_( [ \lo, \hi ] )
	.category_( 'range' );
