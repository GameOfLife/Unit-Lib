(
MultiChannelUdef( \output, { |bus|
	Out.ar( bus, UGlobalEQ.ar( UIn.ar( 0, Udef.numChannels ) ) * UEnv.kr );
})	
	.category_( \io )
	.setSpec( \bus, PositiveIntegerSpec( 0 ) )
	.shouldPlayOnFunc_({ |unit, target|
		target.asTarget.server.isLocal; // safety measure: no output on wfs servers
	})
)