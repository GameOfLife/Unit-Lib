EnvM : Env {
	// a mapped Env. level values are entered raw, but stored internally unmapped
	var <spec;
	
	unmappedLevels { ^levels }
	unmappedLevels_ { |newLevels| levels = newLevels; array = nil; }
	
	levels { ^if( spec.notNil ) { spec.map( levels ) } { levels } }
	levels_ { |newLevels|
		if( spec.notNil ) { 
			levels = spec.unmap( newLevels ) 
		} { 
			levels = newLevels;
		};
		array = nil; 
	}
	
	storeArgs { ^[this.levels, times, curves, releaseNode, loopNode] }
	
	spec_ { |newSpec|
		if( newSpec.notNil ) {
			if( spec.notNil ) {
				levels = spec.map( levels );
			};
			spec = newSpec.asSpec;
			levels = spec.unmap( levels );
		} {
			if( spec.notNil ) {
				levels = spec.map( levels );
			};
			spec = nil;
		};
		this.changed( \levels );
	}
	
	asUnitArg { |unit, key|
		if( key.notNil ) {
			if( unit.isUMap && { unit.def.isMappedArg( key ) } ) {
				if( unit.spec.notNil ) {
					this.spec = unit.getSpec( key ).asControlSpec.copy;
				};
			} {
				this.spec = unit.getSpec( key ).asControlSpec.copy;
			};
		};
		^this;
	}
	
}

+ Env {
	unmappedLevels { ^levels }
	unmappedLevels_ { |newLevels| this.levels = newLevels; }
}