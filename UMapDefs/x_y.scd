
UMapDef( \x_y, { |x = 0.5, y = 0.5|
	UMapOut.kr([x,y]);
})
	.mappedArgs_( [ \x, \y ] )
	.category_( 'point' );
