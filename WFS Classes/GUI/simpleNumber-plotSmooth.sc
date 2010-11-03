+ SimpleNumber {
	plotSmoothInput { |size, color, fromRect|
		WFSConfiguration.default.allSpeakers.wrapAt( this )
			.asWFSPoint.plotSmoothInput( size, color, fromRect )
		}
		
	plotSmooth {  |speakerConf = \default|
		if( speakerConf == \default )
			{ speakerConf = WFSConfiguration.default };
		speakerConf.allSpeakers.wrapAt( this )
			.asWFSPoint.plotSmooth( speakerConf );
		}
	}