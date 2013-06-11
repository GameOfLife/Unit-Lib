+ Spec {
	
	adaptToSpec { }
}

+ ControlSpec {
	
	adaptToSpec { |spec|
		var res = this, class = this.class;
		if ( spec.isKindOf( ControlSpec ) ) {
			if( spec.isMemberOf( FreqSpec ) && { class == ControlSpec } ) {
				class = FreqSpec;
			};
			res = this.class.newFrom( spec );
			res.default_( res.map( this.default ) );
		};
		^res;
	}
	
}

+ UEnvSpec {
	
	adaptToSpec { |spec|
		if( spec.notNil ) {
			^this.copy.spec_( spec );
		} {
			^this;
		};
	}
	
}