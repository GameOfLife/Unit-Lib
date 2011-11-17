
EQdef( \low, BLowShelf, \mid, BPeakEQ, \hi, BHiShelf )
	.defaultSetting_([ 
		[ 250, 1, 0 ],
		[ 1000, 1, 0 ], 
		[ 6000, 1, 0 ]
	])
	.name_( \simpleEQ );

Udef( \simpleEQ, { 
	var in;
	in = UIn.ar( 0, 1 );
	UOut.ar(0, UEQ.ar( in, \lowMidHi, \simpleEQ ) );
})	
	.category_( 'filter' )
	.setSpec( \lowMidHi, UEQSpec( \simpleEQ ) );
	