(
Udef( \convolution, { |file, wet = 0.3, dry = 1|
	var sig, convolved;
	sig = UIn.ar(0,1);
	convolved = PartConv.ar( sig, 2048, file );
	UOut.ar( 0, (sig * dry) + (convolved * wet) );
})
	.category_( \effect )
	.setSpec( \file, PartConvBufferSpec() )
	.setSpec( \dry, \amp.asSpec )
	.setSpec( \wet, \amp.asSpec )
)

