UMod : ObjectWithArgs {
	
	var <>key, <>def, <>args;
	var <>environment;
	
	*new { |key, def, args|
		^super.new.initNew( key, def, args );
	}
	
	asUMod { ^this }
	
	initNew { |inKey, inDef, inArgs|
		key = inKey;
		def = inDef.asUModDef;
		args = inArgs;
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
	
	init { |unit|
		this.use({ def.init( this, unit, key ) });
	}
	
	connect { |unit|
		this.use({ def.connect( this, unit, key ) });
	}
	
	disconnect { |unit|
		this.use({ def.disconnect( this, unit, key ) });
	}
	
	prepare { |unit, startPos = 0|
		this.use({ def.prepare( this, unit, key, startPos ) });
	}
	
	start { |unit, startPos = 0, latency = 0.2|
		this.use({ def.start( this, unit, key, startPos, latency ) });
	}
	
	stop { |unit|
		this.use({ def.stop( this, unit, key ) });
	}
	
	dispose {  |unit|
		this.use({ def.dispose( this, unit, key ) });
	}
	
	pause { |unit|
		this.use({ def.pause( this, unit, key ) });
	}
	
	unpause { |unit|
		this.use({ def.unpause( this, unit, key ) });
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
	
	doFunc { |which, mod, unit, key|
		funcDict[ which ].value( mod, unit, key );
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
	asUMod { ^this } // accept any object as umod (for now)
}

+ Function {
	asUModDef { |name| ^UModDef( name, this ); }
}

+ Symbol { 
	asUMod { |args| ^UMod( this, args ) }
	asUModDef { ^UModDef.fromName( this ); }
}

+ Array {
	asUMod { ^UMod( this[0], *this[1..] ) }
}