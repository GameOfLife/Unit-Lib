/*
\decodeB2

Decodes a two dimensional ambisonic B-format signal (FuMa first order) to a set of speaker locations in a regular polygon. The outputs will be in clockwise order. The position of the first speaker location is either center or left of center.

orientation: Should be zero if the front is a vertex of the polygon. The first speaker location will be directly in front. Should be 0.5 if the front bisects a side of the polygon. Then the first speaker will be the one left of center.
numChannels: number of input channels (*).

(*) only the following numbers of channels are allowed:
2,3,4,5,6,7,8,10,12,16,24,32

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

MultiChannelUdef( \decodeB2, {
	var w,x,y, orientation, rotate, mode;
	orientation = \orientation.ukr( 0, 0, 0.5, \lin );
	rotate = \rotate.ukr( 0, AngleSpec() ) / pi;
	#w,x,y = UIn.ar( 0, 3, true );
	#x,y = Rotate2.ar( x, y, rotate );
	UOut.ar( 0, DecodeB2.ar( Udef.numChannels, w, x, y, orientation ) );
}, channels: [2,3,4,5,6,7,8,10,12,16,24,32])
	.category_( \utility )