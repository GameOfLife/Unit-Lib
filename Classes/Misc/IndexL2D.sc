IndexL2D {

	/*
	accepts a buffer of any number of channels/frames and lets you treat it as a 2-dimensional table
	*/

	*ar { arg bufnum, frame = 0, channel = 0, autoScale = false;
		var numChannels;
		numChannels = BufChannels.kr(bufnum);
		if( autoScale.booleanValue == true ) {
			frame = frame.linlin(0, 1, 0, BufFrames.kr( bufnum ) - 1, \minmax );
			channel = channel.linlin(0, 1, 0, numChannels - 1, \minmax );
		};
		^LinXFade2.ar(
			IndexL.ar( bufnum, (frame.round(2) * numChannels) + channel ),
			IndexL.ar( bufnum, ((frame.trunc(2) + 1) * numChannels) + channel ),
			(frame * 2 - 1).fold2(1)
		);
	}

	*kr { arg bufnum, frame = 0, channel = 0, autoScale = false;
		var numChannels;
		numChannels = BufChannels.kr(bufnum);
		if( autoScale.booleanValue == true ) {
			frame = frame.linlin(0, 1, 0, BufFrames.kr( bufnum ) - 1, \minmax );
			channel = channel.linlin(0, 1, 0, numChannels - 1, \minmax );
		};
		^LinXFade2.kr(
			IndexL.kr( bufnum, (frame.round(2) * BufChannels.ir(bufnum)) + channel ),
			IndexL.kr( bufnum, ((frame.trunc(2) + 1) * BufChannels.ir(bufnum)) + channel ),
			(frame * 2 - 1).fold2(1)
		);
	}

}