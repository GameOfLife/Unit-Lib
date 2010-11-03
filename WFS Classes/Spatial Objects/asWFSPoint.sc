+ Object {
	isWFSPoint { ^false }
	asWFSPoint { ^nil }
	asWFSSpeaker { ^nil }
	isWFSSpeaker { ^false }
	isWFSPath { ^false }
	asWFSPath { ^nil }
	couldBeWFSPoint { ^false }
	
	}

+ Collection {
	asWFSPoint { ^WFSPoint(*this) }
	asWFSSpeaker { ^WFSSpeaker(*this) }
	asWFSPath { ^WFSPath( this.collect( _.asWFSPoint ) ) }
	asWFSPointArray { ^WFSPointArray.with( *this ) }
	asWFSPathArray { ^WFSPathArray.with(*this) }
	asWFSSpeakerArray { ^WFSSpeakerArray.with(*this) }
	asWFSSpeakerLine { ^WFSSpeakerLine.with(*this) }
	
	couldBeWFSPoint { ^(this.size < 4) && { this.every( { |item|
											item.size == 0 } ) }; 
				}
			
		
	writeWFSFile { |path = "~/scwork/wfsPathsOut.xml", name="example"|
		var array;
		array = this.asWFSPathArray;
		if( array.isValid )
			{ ^array.writeWFSFile(path, name) }
			{ "this is not an Array containing WFSPaths".error };
	}
	
	writeSVGFile { |path =  "~/scwork/wfsPathsOut.svg", name="example"|
		var array;
		array = this.asWFSPathArray;
		if( array.isValid )
			{ ^array.writeSVGFile(path, name) }
			{ "this is not an Array containing WFSPaths".error };
	}
}
+ String {
	asWFSPoint { ^nil }
	asWFSSpeaker {^nil }
	asWFSPath { ^nil }
}

+ Number { 
	asWFSPoint { |amount = 3| // fill in the number at all slots or only 1 or 2
		^WFSPoint(*this.dup(amount)) }
		
	asWFSSpeaker { |angle = 0| ^this.asWFSPoint.asWFSSpeaker(angle) }
	
	couldBeWFSPoint { ^true }
	}

+ Point {
	asWFSPoint { |z = 0| ^WFSPoint(x,y,z); }
	asWFSSpeaker { |z=0, angle=0| ^WFSSpeaker(x,y,z,angle) }
	
	couldBeWFSPoint { ^true }
	}

