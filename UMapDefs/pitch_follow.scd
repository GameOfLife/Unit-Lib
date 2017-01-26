/*
\pitch_follow

Creates an UMap that listens to an audio bus from a previous unit in the chain, and follow it's pitch.

ampTreshold: an amplitude threshold above which pitch detection is performed
median: number of frames of built-in median filter
pitchRange: the range of pitches to detect (will never go out of range)
toRange: the range to which the pitchRange is scaled
invert: if true, the toRange is inverted (i.e. low values become high and v.v.)
curve: a curvature value for toRange. If curve == 0, the range is linear.
hasPitchID: shared value id for a signal indicating the 'clarity' of the pitch. This value can be obtained with a 'shared_in' UMap further in the chain, using the same id. The signal varies from 0 to 1, indicating the purity of the tone, where 0 is no tone and 1 is maximum tone.

The 'toRange' arg range is mapped to that of the parameter to which the UMap is connected. When the pitchRange and toRange are the same on a FreqSpec input, the tracked pitch will be scaled 1:1.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UMapDef( \pitch_follow, { |
	ampThreshold = 0.02, median = 7,
	pitchRange = #[60, 4000], toRange = #[0.0,1.0], 
	invert = 0, curve = 0|
	var sig, unranged;
	sig = UIn.ar( 0 );
	sig = Pitch.kr( sig, 440, pitchRange[0], pitchRange[1], 
		ampThreshold: ampThreshold, median: median, clar: 1
	);
	USharedValueOut.kr( \hasPitchID, sig[1] );
	sig = sig[0];
	sig = sig.explin(*pitchRange ++ [0,1,\none]);
	sig = if( invert, 1-sig, sig );
	sig = Select.kr( curve.inRange(-0.001,0.001), [
		sig.lincurve(0,1,0,1, curve ),
		sig
	]);
	sig = sig.linlin(0,1,*toRange ++ [ \none ]);
	UMapOut.kr(sig);
})
	.setSpec( \ampThreshold, \amp.asSpec )
	.setSpec( \median, [1,31,\lin,1,7].asSpec )
	.setSpec( \pitchRange, FreqSpec().asRangeSpec )
	.setSpec( \invert, BoolSpec(false) )
	.setSpec( \curve, [-16,16,\lin,0,0].asSpec )
	.prepareArgsFunc_( { |args| 
		var bus;
		bus = args.pairsAt( \bus );
		args ++ [ \u_i_ar_0_bus, bus ];
	 } )
	.mappedArgs_([ \toRange ])
	.category_(\input)

