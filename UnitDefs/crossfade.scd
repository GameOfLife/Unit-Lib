/*
\crossfade

Equal power crossfade between two sources.

crossfade: amount of left/right
numChannels: number of channels

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

var defs, def;
defs = [ 
	\equal, 
	\linear, 
	\overlap, 
].collect({ |type|
	MultiChannelUdef( type, { |crossfade = 0.5, level = 1.0|
		var in1, in2, out;
		in1 = UIn.ar(0, Udef.numChannels, endPoint: true );
		in2 = UIn.ar( Udef.numChannels, Udef.numChannels, endPoint: true );
		switch( type,
			\equal,  { 
				out = XFade2.ar( in1, in2, crossfade.linlin(0,1,-1,1), level);
			},
			\linear, { 
				out = LinXFade2.ar( in1, in2, crossfade.linlin(0,1,-1,1), level);
			},
			\overlap, { 
				out = (in1 * crossfade.linlin(0.5,1,1,0)) + 
				(in2 * crossfade.linlin(0,0.5,0,1));
				out = out * level;
			},
		);
		UOut.ar( 0, out );
	}, addToAll: false, extraPrefix: \crossfade_ )
		.setSpec( \level, \amp.asSpec, \normal )
});

MultiUdef( \crossfade, defs, \utility, \mode, false );

/*
MultiChannelUdef( \crossfade, { |crossfade = 0.5, level = 1.0|
	var in1, in2, out;
	in1 = UIn.ar(0, Udef.numChannels, endPoint: true );
	in2 = UIn.ar( Udef.numChannels, Udef.numChannels, endPoint: true );
	out = XFade2.ar( in1, in2, crossfade.linlin(0,1,-1,1), level);
	UOut.ar( 0, out );
}, channels: [1,2,3,4,5,6,7,8,10,12,16])
	.setSpec( \level, \amp.asSpec, \normal )
	.category_( \utility )
*/