UMod : ObjectWithArgs {
	
	var <>key, def, defName;
	var <>environment;
	
	*new { |key, def, args|
		^super.new.key_(key).init( def, args );
	}
	
	*defClass { ^UModDef }
	
	asUModFor { |unit| ^this }
	
	get { |key|
		^this.getArg( key );
	}
	
	set { |...args|
		args.pairsDo({ |key, value|
			this.setArg( key, value );
		});
	}
	
	argSpecs { ^this.def.argSpecs( this ) }
	getSpec { |key| ^this.def.getSpec( key, this ); }
	
	init { |inDef, inArgs|
		if( inDef.isKindOf( this.class.defClass ) ) {
			def = inDef;
			defName = inDef.name;
			if( defName.notNil && { defName.asUModDef == def } ) {
				def = nil;
			};
		} {
			defName = inDef.asSymbol;
			def = nil;
		};
		if( this.def.notNil ) {
			args = this.def.asArgsArray( inArgs ? [] );
		} {
			args = inArgs;
			"UModDef '%' not found".format(inDef).warn;
		};
	}
	
	def {
        ^def ?? { defName.asUModDef }
    }

    defName {
        ^defName ?? { def.name }
    }

    def_ { |newDef, keepArgs = true|
        this.init( newDef, if( keepArgs ) { args } { [] }); // keep args
    }

    defName_ { |newDefName, keepArgs = true|
        this.init( newDefName, if( keepArgs ) { args } { [] }); // keep args
    }
	
	use { |func|
		if( this.def.notNil ) {
			if(	environment.notNil ) {
				environment.use({
					func.value;
				});
			} {
				func.value;
			};
		};
	}
	
	connect { |unit|
		this.use({ this.def.connect( this, unit, key ) });
	}
	
	disconnect { |unit|
		this.use({ this.def.disconnect( this, unit, key ) });
	}
	
	prepare { |unit, startPos = 0|
		this.use({ this.def.prepare( this, unit, key, startPos ) });
	}
	
	start { |unit, startPos = 0, latency = 0.2|
		this.use({ this.def.start( this, unit, key, startPos, latency ) });
	}
	
	stop { |unit|
		this.use({ this.def.stop( this, unit, key ) });
	}
	
	dispose {  |unit|
		this.use({ this.def.dispose( this, unit, key ) });
	}
	
	pause { |unit|
		this.use({ this.def.pause( this, unit, key ) });
	}
	
	unpause { |unit|
		this.use({ this.def.unpause( this, unit, key ) });
	}
	
	printOn { arg stream;
		stream << this.class.name << "( " <<* this.argsForPrint  <<" )"
	}
	
	dontStoreArgNames { ^[] }
	
	getInitArgs {
		var defArgs;
		defArgs = (this.def.args( this ) ? []).clump(2);
		^args.clump(2).select({ |item, i| 
			(item != defArgs[i]) && { this.dontStoreArgNames.includes( item[0] ).not };
		 }).flatten(1);
	}

	argsForPrint {
        var initArgs, initDef;
        initArgs = this.getInitArgs;
        initDef = this.def.name;
        if( initArgs.size > 0 ) {
            ^[ key, initDef, initArgs ];
        } {
            ^[ key, initDef ];
        };
    }
	
	storeArgs { 
		var initArgs, initDef;
		initArgs = this.getInitArgs;
		initDef = this.defName ?? { this.def };
		if( (initArgs.size > 0) ) {
			^[ key, initDef, initArgs ];
		} {
			^[ key, initDef ];
		};
	}
	
}

UModDef : GenericDef {
	
	classvar <>all;
	
	var <>funcDict;
	
	var <>category;
	
	*new { |name, funcDict, args, category, addToAll=true|
		if( name.isNil ) { addToAll = false };
		^super.new( name, args ? [], addToAll )
			.funcDict_( funcDict ).initNew.category_( category ? \default );
	}
	
	asUModDef { ^this }
	
	addToAll { |name|
		this.class.all ?? { this.class.all = IdentityDictionary() };
		this.class.all[ name ] = this; // name doesn't need to be a Symbol
	}
	
	initNew {
		if( funcDict.isFunction ) {
			funcDict = (\start -> funcDict);
		};
		if( funcDict.isArray ) {
			funcDict = Event.newFrom(funcDict)
		};
	}
	
	doFunc { |which, mod, unit, key ...args|
		funcDict[ which ].value( mod, unit, key, *args );
	}
	
	init { |mod, unit, key|
		this.doFunc( \init, mod, unit, key );
	}
	
	stop { |mod, unit, key|
		this.doFunc( \stop, mod, unit, key );
	}
	
	doesNotUnderstand { |selector ...args|
		if( selector.isSetter.not ) {
			^this.doFunc( selector, *args );
		} {
			^super.doesNotUnderstand( selector, *args );
		};
	}
	
}

+ Object {
	asUModDef { ^this }
	asUModFor { |unit| ^this } // accept any object as umod (for now)
}

+ Function {
	asUModDef { |name| ^UModDef( name, this ); }
}

+ Symbol { 
	asUModFor { |unit, key| ^UMod( key, this ) }
	asUModDef { ^UModDef.fromName( this ); }
}

+ Array {
	asUModFor { |unit| ^UMod( this[0], *this[1..] ) }
}