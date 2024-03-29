/*
\tiltEQ

Combined High Shelf and Low Shelf filter. Default settings mimic the Tonelux TILT eq response.

tilt: amount of tilt (in dBs). Positive numbers increase high frequencies.
center: center frequency (default 650)
rs: the reciprocal of S. Shell boost/cut slope for both shelving filters (default 2)

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

MultiChannelUdef( \tiltEQ, { |tilt = 0, center = 650, rs = 2, ampComp = 0|
	var in;
	in = UIn.ar( 0, Udef.numChannels );
	center = center.clip(20,20000);
	in = BHiShelf.ar( in, center, rs, tilt );
	in = BLowShelf.ar( in, center, rs, tilt.neg );
	UOut.ar( 0, in * (tilt.abs * ampComp).dbamp );
})
.category_( \eq )
.setSpec( \tilt, [-24,24,\lin,0,0].asSpec )
.setSpec( \ampComp, [-1,1,\lin,0,0].asSpec )
.setSpec( \center, \freq.asSpec.copy.default_(650) )
.setSpec( \rs, [ 0.6, 10, \exp, 0, 1 ].asSpec )
