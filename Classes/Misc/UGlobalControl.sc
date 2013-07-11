UGlobalControl : OEM {
	
	classvar <>current;
	
	*initClass {
		this.new( 
			*(..7).collect({ |x| [ ("global_" ++ x).asSymbol, 0.5 ] }).flatten(1) 
		).makeCurrent;
	}
	
	makeCurrent {
		current.removeDependant( this.class );
		this.addDependant( this.class );
		current = this;
		this.class.changed( \current );
	}
	
	removeCurrent {
		current.removeDependant( this.class );
		current = nil;
		this.class.changed( \current );
	}
	
	put { |key, value|
		super.put( key, value.asUnitArg(this,key) );
	}
	
	get { |key| 
		^this.at( key ).value;
	}
	
	set { |...keyValuePairs| 
		keyValuePairs.pairsDo({ |key, value|
			this.put( key, value );
		});
	}
	
	getSpec { ^[0,1].asSpec }
	getSpecMode { ^\init }
	
	canUseUMap { |key, umapdef|
		^umapdef.allowedModes.includes( this.getSpecMode ) && {
			umapdef.numChannels == 1
		};
	}
	
	prepare { |key, action|
		var act, val;
		if( key.isNil ) {
			act = MultiActionFunc({ action.value });
			this.keys.do({ |key| this.prepare( key, act.getAction ) });
		} {
			val = this[ key ];
			if( val.respondsTo( \unit_ ) ) {
				val.unit = this;
			};
			if( val.respondsTo( \prepare ) ) {
				val.prepare( ULib.servers, 0, action );
			} {
				action.value;
			};
		};
	}
	
	dispose { |key|
		this.values.do({ |value| 
			if( value.respondsTo( \dispose ) ) { 
				value.dispose;
			}
		});
	}
	
	valuesSetUnit {
		this.values.do({ |key, value| 
			if( value.respondsTo( \unit_ ) ) { 
				value.unit = this;
			}
		});
	}
	
	*update { |obj ...args| // redirect changed messages from current
		this.changed( *args );
	}

}