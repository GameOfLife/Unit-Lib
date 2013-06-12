+ Spec {
	
	adaptToSpec { }
}

+ ControlSpec {
	
	adaptToSpec { |spec|
		var res = this, class = this.class;
		if ( spec.respondsTo(\asControlSpec) ) {
			if( spec.isMemberOf( FreqSpec ) && { class == ControlSpec } ) {
				class = FreqSpec;
			};
			spec = spec.asControlSpec;
			res = class.newFrom( spec );
			res.minval = res.minval.max( (2**24).neg );
			res.maxval = res.maxval.min( 2**24 );
			res.default_( res.map( this.default ) );
		};
		^res;
	}
	
}

+ UEnvSpec {
	
	adaptToSpec { |spec|
		if( spec.notNil && spec.respondsTo(\asControlSpec) ) {
			^this.copy.spec_( spec.asControlSpec );
		} {
			^this;
		};
	}
	
}