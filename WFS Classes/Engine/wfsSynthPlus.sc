// part of WFS Classes
// W. Snoei 2006

+ String {  // from defname ( "WFS_intType_audioType" )
	wfsIntType { ^this.split( $_ )[1].asSymbol; }
	wfsAudioType {  ^this.split( $_ )[2].asSymbol; } 
	
	wfsIntType_ { |type| var out;   // not in place: returns new string!!
		out = this.split( $_ )[1] = type;
		^out.join( $_ );
		}
		
	wfsAudioType_ { |type| var out;
		out = this.split( $_ )[2] = type;
		^out.join( $_ );
		} 
	
	asWFSSynthDefType { ^(this.wfsIntType ++ "_" ++ this.wfsAudioType).asSymbol }
	}

+ Symbol {
	wfsIntType { ^this.asString.wfsIntType; }
	wfsAudioType { ^this.asString.wfsAudioType; }
	asWFSSynthDefType { ^this.asString.asWFSSynthDefType; } 
	}