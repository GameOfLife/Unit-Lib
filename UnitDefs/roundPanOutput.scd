MultiChannelUdef( \roundPanOutput, { |bus = 0, point = #[0,0], rollOff = (-6), speakerRadius = 0.19, lag = 0.1|
	var sig, panned, pankr, polar, angle, amp;
	var mics, numChans;
	
	sig = UIn.ar( 0 );
	
	numChans = Udef.numChannels;
	
	mics = ({ |i| 
		Polar( speakerRadius.lag(lag), (i - 0.5).linlin( 0, numChans, 0, 2pi, \none ) ).asPoint
	} ! numChans );
	
	point = if( Line.kr(2,0,lag * 2).min( 1 ), point, LPF.kr( point, 1/lag ) ).asPoint;
	panned = VirtualMics.ar( mics, sig, point, rollOff );
	polar = point.asPolar;
	angle = polar.angle.linlin(0,pi,1,-1,\none);
	amp = (0.15 / polar.rho).min( 1 );
	
	pankr = PanAz.kr( numChans, 1, angle ).max(amp);

	Out.ar( bus, UGlobalEQ.ar( panned ) * pankr * UEnv.kr  );
}, channels: [2,3,4,5,6,7,8,10,12,16,24,32] )
	.category_( \io )
	.setSpec( \bus, PositiveIntegerSpec( 0 ) )
	.setSpec( \point, PointSpec( 200, step: 0.1 ) )
	.setSpec( \rollOff, [ -12, 0, \lin, 1 ].asSpec )
	.setSpec( \speakerRadius, [0,1,\lin,0,0.19].asSpec )
	.setSpec( \lag, [ 0, 10, 4, 0, 0.1 ].asSpec );