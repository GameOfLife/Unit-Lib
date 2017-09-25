RandIDAllocator {
	classvar <>dict;
	
	*value { |server|
		var count;
		server = server ? Server.default;
		dict = dict ?? { IdentityDictionary() };
		count = dict[ server ] ? -1;
		count = (count + 1).wrap(0, server.options.numRGens - 1);
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
	
	// double as Spec
	*new { ^this } // only use as class
	
	*asSpec { ^this }
	
	*constrain { ^this } // whatever comes in; UGlobalEQ comes out
	
	*default { ^this }
	
	*massEditSpec { ^nil }
	
	*findKey {
		^Spec.specs.findKeyForValue(this);
	}

}

URandSeed {
	var <>seed;
	
	*getRandID {
		var id = \u_randID.ir( 0 );
		Udef.addBuildSpec( ArgSpec( \u_randID, RandIDAllocator, RandIDAllocator, true, \init ) );
		RandID.ir( id );
	}
	
	*getSeed { |seed|
		seed = seed ? \seed;
		if( seed.isKindOf( Symbol ) ) {
			Udef.addBuildSpec( 
				ArgSpec( seed, URandSeed(), URandSeed, false, \init ) 
			);
			seed = seed.ir( 12345 );
		};
		^seed;
	}
	
	*ir { |seed|
		this.getRandID;
		RandSeed.ir( 1, this.getSeed( seed ) );
	}
	
	*kr { |trig = 1, seed|
		this.getRandID;
		RandSeed.kr( trig, this.getSeed( seed ) );
	}
	
	*new { ^super.new.next }
	
	*asControlSpec {
		^ControlSpec( 0, 2**24, \lin, 1, 12345 )
	}
	
	== { |obj| ^obj.class == this.class }
	
	value { ^seed }
	next { seed = 16777216.rand; this.changed( \seed ); }
	
	doesNotUnderstand { |selector ... args| // dirty maybe, but any reason not to do this?
		^seed.perform( selector, *args );
	}
	
	prepare { |target, startPos, action| 
		this.next;
		action.value( this );
	}
	
	asControlInput { ^this.value; }
	asControlInputFor { ^this.value; }
	
	*asSpec { ^this }
	*constrain { |value| 
		if( value.isNumber or: value.isKindOf( URandSeed ) ) {
			^value 
		} {  
			^URandSeed();
		} 
	}
	
	*uconstrain { |value| ^this.constrain( value ) }
	
	*default { ^URandSeed() }
	*map { |value| ^value }
	*unmap { |value| ^value }
	
	*massEditSpec { |inArray|
		^URandSeedMassEditSpec( inArray.size, inArray );
	}
	*massEditValue { |inArray|
		^inArray
	}
	*massEdit { |inArray, params|
		^params;
	}
	
	*findKey {
		^Spec.specs.findKeyForValue(this);
	}
	
}

URandSeedMassEditSpec : Spec {
	
	var size = 1, <>default;

	*new { |size, default|
		^super.newCopyArgs( size, default );
	}

	asSpec { ^this }
	constrain { |values| 
		^values.collect({ |value| URandSeed.constrain( value ) }); 
	}
	
}