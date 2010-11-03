+ UGen {
	sinOpt {
		case { this.rate == 'audio' }
		  	{ ^SinOsc.ar( 0, this ) }
		  	{ this.rate == 'control' }
		  	{ ^SinOsc.kr( 0, this ) }
		  	{ true }
		  	{ ^this.sin }
		}
	}
	
+ Object {
	sinOpt {
		^this.sin
		}
	}
	
+ Collection {
	sinOpt {
		^this.collect( _.sinOpt );
		}
	}
	

	
