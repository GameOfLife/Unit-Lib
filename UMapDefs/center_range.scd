
UMapDef( \center_range, { |center = 0.5, range = 1.0|
	var lo, hi, halfRange;
	halfRange = range/2;
	center = center.linlin( 0, 1, halfRange, 1-halfRange );
	lo = center - halfRange;
	hi = center + halfRange;
	UMapOut.kr([lo,hi]);
})
	.mappedArgs_( [ \center ] )
	.category_( 'range' );
