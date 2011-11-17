Udef( \fullEQ, { 
	var in;
	in = UIn.ar( 0, 1 );
	UOut.ar(0, UEQ.ar( in, \eq, \default ) );
})	
	.category_( 'filter' )
	.setSpec( \eq, UEQSpec( \default ) );