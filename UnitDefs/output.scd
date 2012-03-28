(
var udefs;

udefs = [1,2,3,4,5,6,7,8,10,12,16,24,32].collect({ |numChannels|
	HiddenUdef( "%".format( numChannels ).asSymbol, { |bus|
		Out.ar( bus, UGlobalEQ.ar( UIn.ar( 0, numChannels ) ) * UEnv.kr );
	}).setSpec( \bus, PositiveIntegerSpec( 0 ) )
	  .extraPrefix_( "output" )
});

MultiUdef( \output, udefs, \io, \numChannels, false )
	.shouldPlayOnFunc_({ |unit, target|
		target.asTarget.server.isLocal; // safety measure: no output on wfs servers
	})
)