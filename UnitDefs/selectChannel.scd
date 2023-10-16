/*
\selectChannel

select a single channel to multiple inputs, with optional crossfade time.

numChannels: number of output channels (*).

(*) only the following numbers of channels are allowed:
2,3,4,5,6,7,8

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

MultiChannelUdef( \selectChannel, {
	var index;
	var fadeTime;
	var in;
	index = \index.ukr( 0, 0, Udef.numChannels - 1, \lin, 1 );
	fadeTime = \fadeTime.ukr( 0, 0, 1, \lin );
	in = UIn.ar( 0, Udef.numChannels, true );
	UOut.ar( 0, SelectCF.ar( index, in, fadeTime ) );
}, channels: [ 2,3,4,5,6,7,8 ])
	.category_( \utility )
