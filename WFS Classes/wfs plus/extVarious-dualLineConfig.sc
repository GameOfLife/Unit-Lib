// WS 17/02/2009
// additions for dual line config support
// Also changed: extWFSConfiguration-cornerPoints.sc


+ WFSConfiguration {
	
	*rect4 { |xSpeakers = 40, ySpeakers = 56, dist = 0.165, xSize = 7.5, ySize = 11.0 |
	
		// if xSpeakers or ySpeakers == 0 no speakerLine is created
		// dual line : .rect4( 0, 12*8, 0.165, 5, 0 ); -- 5 = width
		
		^WFSConfiguration( 
			*( { |i|
				if([xSpeakers, ySpeakers].wrapAt(i) > 0)
					{	WFSSpeakerLine.newFromCenter( 
							[	[ 0, ySize / 2, 0, 180],
								[ xSize / 2, 0, 0, 270],
								[0, ySize.neg / 2, 0, 0 ],
								[xSize.neg / 2, 0, 0, 90] ][i],
							[xSpeakers, ySpeakers].wrapAt(i),
							dist)
					};
			 } ! 4 ).select(_.notNil)
				 
			).name_( ("rect4_" ++ xSpeakers ++ "x_" ++ ySpeakers ++ "y").asSymbol  );

		}
		
	partial { |index = 0, max = 2|
	
		// split a conf up into multiple parts, per speakerLine 
		// might not always be the best option, but works in regular cases
		
		// Q: DO I NEED TO COPY MORE? CHECK STARTUP FILES !!
		
		var nSplPerIndex, startIndex, endIndex;
		nSplPerIndex = speakerLines.size/max;  
		startIndex = (nSplPerIndex * index).asInt;  
		endIndex = ((nSplPerIndex * (index+1)) - 1).asInt; 
		^WFSConfiguration( *( speakerLines[startIndex..endIndex] ) )
			.cornerPoints_( this.cornerPoints[startIndex..endIndex] )
			.name_( name ++ "_i" ++ (index+1) );	
		}
	
	}