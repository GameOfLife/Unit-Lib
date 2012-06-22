Udef( \tanhDistort, { |inGain = 0, outGain = 0|
	UOut.ar( 0, (UIn.ar( 0 ) * inGain.dbamp.lag(0.1)).tanh * outGain.dbamp.lag(0.1) )
} )
	.category_( \effect )
	.setSpec( \inGain, [0, 60].asSpec )
	.setSpec( \outGain, [-60, 0].asSpec );