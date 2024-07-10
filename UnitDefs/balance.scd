/*
\balance

A panning balancer for multiple channels (minimum: 2).

pan: pan position
width: width of panning (0: 2 outputs, 1: all outputs)
amount: amount of panning applied (0-1)
level: output level
numChannels: number of channels (2,3,4,5,6,7,8,10,12,16,24,32)

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

MultiChannelUdef( \balance, { |pan = 0, width = 0.0, amount = 1.0, level = 1.0 |
	var in, pansig;
	in = UIn.ar(0, Udef.numChannels );
	if( Udef.numChannels == 2 ) {
		pan = pan * 0.5;
	};
	pansig = PanAz.kr(
		Udef.numChannels, DC.kr(1), pan, 1,
		width.linlin(0,1,2,Udef.numChannels), 0.5
	);
	pansig = DC.kr(1).blend( pansig, amount );
	in = in * pansig * level;
	UOut.ar( 0, in );
}, channels: [2,3,4,5,6,7,8,10,12,16,24,32])
.category_( \utility )
.setSpec( \level, \amp.asSpec, \normal )
.setSpec( \pan, [-1,1,\lin,0,0], \normal )


/*
{ PanAz.ar( 2, DC.ar(1), Line.ar(-0.5,0.5,0.1), 1, 2, 0 ) }.plot(0.1)
*/