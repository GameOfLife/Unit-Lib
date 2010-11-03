+ WFSPlane {

	width { ^WFSTools.fromRadians( radianWidth ) }
	
	width_ { |newWidth|
		radianWidth = WFSTools.asRadians( newWidth );
		}
		
	offset { ^WFSTools.fromRadians( radianOffset ) }
	
	offset_ { |newOffset|
		radianOffset = WFSTools.asRadians( newOffset );
		}
		
	
		
	}