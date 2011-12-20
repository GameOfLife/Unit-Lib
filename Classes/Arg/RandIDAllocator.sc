RandIDAllocator {
	classvar <>dict;
	
	*value { |server|
		var count;
		server = server ? Server.default;
		dict = dict ?? { IdentityDictionary() };
		count = dict[ server ] ? -1;
		count = (count + 1).wrap(0, server.options.numRGens);
		dict[ server ] = count;
		^count;
	}
	
	*asControlInput { ^this.value }
	
	*asControlInputFor { |server| ^this.value( server ) }
	
	*reset { |server|
		if( server.notNil ) {
			dict[ server ] = nil;
		} {
			dict.clear; // remove all
		};
	}
}

RandIDAllocatorSpec : Spec {
	
	// fixed output: 
	*new { ^this } // only use as class
	
	*asSpec { ^this }
	
	*constrain { ^RandIDAllocator } // whatever comes in; UGlobalEQ comes out
	
	*default {  ^RandIDAllocator }
	
	*massEditSpec { ^nil }
	
	*findKey {
		^Spec.specs.findKeyForValue(this);
	}

}

URandSeed {
	
	*initClass {
		ControlSpec.specs = ControlSpec.specs.addAll([
			\u_randID -> RandIDAllocatorSpec
		]);
	}
	
	*ir { |seed = 12345|
		var id = \u_randID.ir( 0 );
		RandID.ir( id );
		RandSeed.ir( 1, seed );
	}
	
	*kr { |trig = 1, seed = 12345|
		var id = \u_randID.kr( 0 );
		RandID.kr( id );
		RandSeed.kr( trig, seed );
	}
}