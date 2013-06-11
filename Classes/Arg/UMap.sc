UMapDef : Udef {
	classvar <>all, <>defsFolders, <>userDefsFolder;
	
	var <>mappedArgs;
	
	*initClass{
		this.defsFolders = [ 
			this.filenameSymbol.asString.dirname.dirname.dirname +/+ "UMapDefs"
		];
		this.userDefsFolder = Platform.userAppSupportDir ++ "/UMapDefs/";
	}
	
	*prefix { ^"umap_" } // synthdefs get another prefix to avoid overwriting
	
	*from { |item| ^item.asUDef( this ) }
	
	asArgsArray { |argPairs, constrain = true|
		argPairs = argPairs ? #[];
		^argSpecs.collect({ |item| 
			var val;
			val = argPairs.pairsAt(item.name) ?? { item.default.copy };
			if( constrain && this.isMappedArg( item.name ).not && { val.isKindOf( UMap ).not } ) { 
				val = item.constrain( val ) 
			};
			[ item.name,  val ] 
		}).flatten(1);
	}
	
	isMappedArg { |name|
		^mappedArgs.notNil && { mappedArgs.includes( name ) };
	}
	
	argSpecs { |unit|
		^argSpecs.collect({ |asp|
			if( this.isMappedArg( asp.name ) ) {
				asp.adaptToSpec( unit !? _.spec );
			} {
				asp
			};
		});
	}
		
	getArgSpec { |name, unit|
		var asp;
		asp = argSpecs.detect({ |item| item.name == name });
		if( this.isMappedArg( name ) ) {
			^asp !? _.adaptToSpec( unit !? _.spec )
		} {
			^asp
		};
	}
	
	getSpec { |name, unit|
		var asp;
		asp = argSpecs.detect({ |item| item.name == name });
		if( this.isMappedArg( name ) ) {
			^asp.spec.adaptToSpec( unit.spec );
		} {
			^asp.spec
		};
	}
	
	getDefault { |name, unit|
		var asp;
		asp = this.getArgSpec(name, unit);
		if( asp.notNil ) { ^asp.default; } { ^nil };
	}
	
	setSynth { |unit ...keyValuePairs|
		keyValuePairs = keyValuePairs.clump(2).collect({ |item|
			if( this.isMappedArg( item[0] ) ) {
				[ item[0], this.getSpec( item[0], unit ) !? _.unmap( item[1] ) ? item[1] ];
			} {
				item
			};
		}).flatten(1);
		this.prSetSynth( unit.synths, *keyValuePairs );
	}
	
	asUMapDef { ^this }
	
	isUdef { ^false }
	
	getControlInput { |unit|
		if( this.hasBus ) {
			^("c" ++ (this.getBus(unit) + unit.class.busOffset)).asSymbol;
		} {
			^this.value( unit );
		};
	}
	
	value { |unit|  
		// subclass may put something different here
		^unit !? {|x| x.spec.default } ? 0;
	}
	
	getBus { |unit|
		^unit.get(\u_mapbus) ? 0
	}
	
	setBus { |bus = 0, unit|
		unit.set(\u_mapbus, bus );
	}
	
	hasBus { ^this.argNames.includes( \u_mapbus ); }
	
	createSynth { |umap, target, startPos = 0| // create A single synth based on server
		target = target ? Server.default;
		^Synth( this.synthDefName, umap.getArgsFor( target, startPos ), target, \addBefore );
	}
}

UMap : U {
	
	// This class is under development. For now it plays a line between min and max.
	// it can only be used for args that have a single value ControlSpec
	// gui doesn't work yet
	
	/* 
	example:
	x = UChain([ 'sine', [ 'freq', UMap() ] ], 'output');
	x.prepareAndStart;
	x.stop;
	*/
	
	classvar <>allUnits;
	
	var <spec;
	var <>unitArgName;
	var <>unmappedKeys;
	
	*busOffset { ^1500 }
	
	*new { |def, args, mod|
		^super.new( def, args, mod ).setunmappedKeys( args );
	}
	
	setunmappedKeys { |args|
		args = (args ? []).clump(2).flop[0];
		this.def.mappedArgs.do({ |item|
			if( args.includes( item ).not ) {
				unmappedKeys = unmappedKeys.add( item );
			};
		});
	}
	
	*initClass { 
	    allUnits = IdentityDictionary();
	}
	
	*defClass { ^UMapDef }
	
	asControlInput {
		^this.def.getControlInput(this);
	}
	
	getBus { 
		^this.def.getBus( this );
	}
	
	setBus { |bus = 0|
		this.def.setBus( bus, this );
	}
	
	set { |...args|
		var keys;
		if( unmappedKeys.size > 0 ) {	
			keys = (args ? []).clump(2).flop[0];
			keys.do({ |item|
				if( unmappedKeys.includes( item ) ) {
					unmappedKeys.remove(item);
				};
			});
		};
		^super.set( *args );
	}
	
	isUMap { ^true }
	
	hasBus { ^this.def.hasBus }
	
	setUMapBuses { } // this is done by the U for all (nested) UMaps
	
	u_waitTime { ^this.waitTime }
	
	dontStoreArgNames { ^[ 'u_dur', 'u_doneAction', 'u_mapbus', 'u_spec' ] }
	
	spec_ { |newSpec|
		if( spec.isNil ) {
			if( newSpec.notNil ) {
				spec = newSpec;
				unmappedKeys.do({ |item|
					this.set( item, this.getSpec( item ).map( this.get( item ) ) );
				});
				unmappedKeys = nil;
			};
		} {
			this.def.mappedArgs.do({ |item|
				this.set( item, this.getSpec( item ).unmap( this.get( item ) ) );
			});
			spec = newSpec;
			if( spec.notNil ) {
				this.def.mappedArgs.do({ |item|
					this.set( item, this.getSpec( item ).map( this.get( item ) ) );
				});
			} {
				unmappedKeys = this.def.mappedArgs.copy;
			};
		} 
	}
	
	// UMap is intended to use as arg for a Unit (or another UMap)
	asUnitArg { |unit, key|
		this.unitArgName = key;
		if( key.notNil ) {
			this.spec = unit.getSpec( key ).copy;
			this.set( \u_spec, spec );
		};
		^this;
	}
	
	unit_ { |aUnit|
		if( aUnit.notNil ) {
			case { this.unit == aUnit } {
				// do nothing
			} { allUnits[ this ].isNil } {
				allUnits[ this ] = [ aUnit, nil ];
			} {
				"Warning: unit_ \n%\nis already being used by\n%\n".postf(
					this.class,
					this.asCompileString, 
					this.unit 
				);
			};
		} {
			allUnits[ this ] = nil; // forget unit
		};
	}
	
	unit { ^allUnits[ this ] !? { allUnits[ this ][0] }; }
	
	unitSet { // sets this object in the unit to enforce setting of the synths
		if( this.unit.notNil ) {	
			if( this.unitArgName.notNil ) {
				this.unit.set( this.unitArgName, this );
			};
		};
	}
	
	getSynthArgs {
		var nonsynthKeys;
		nonsynthKeys = this.argSpecs.select({ |item| item.mode == \nonsynth }).collect(_.name);
		^this.args.clump(2).select({ |item| nonsynthKeys.includes( item[0] ).not })
			.collect({ |item|
				if( this.def.isMappedArg( item[0] ) ) {
					[ item[0], this.getSpec( item[0] ) !? _.unmap( item[1] ) ? item[1] ];
				} {
					item
				};
			})
			.flatten(1);
	}
	
	disposeFor {
		if( this.unit.notNil && { this.unit.synths.size == 0 }) {
			this.unit = nil;
		};
	}
	
	dispose {
	    this.free;
	    this.values.do{ |val|
	        if(val.respondsTo(\dispose)) {
	            val.dispose
	        }
	    };
	    this.modPerform( \dispose );
	    preparedServers = [];
	    this.unit = nil;
	}

}

MassEditUMap : MassEditU {
	
	var <>mixed = false;
	
	init { |inUnits|
		var firstDef, defs;
		units = inUnits.asCollection;
		if( units.every(_.isUMap) ) {	
			defs = inUnits.collect(_.def);
			firstDef = defs[0];
			if( defs.every({ |item| item == firstDef }) ) {
				def = firstDef;
				argSpecs = def.argSpecs.collect({ |argSpec|
					var values, massEditSpec;
					values = units.collect({ |unit|
						unit.get( argSpec.name );
					});
					if( values.any(_.isUMap) ) {
						massEditSpec = MassEditUMapSpec( MassEditUMap( values ) );
					} {	
						massEditSpec = argSpec.spec.massEditSpec( values );
					};
					if( massEditSpec.notNil ) {
						ArgSpec( argSpec.name, massEditSpec.default, massEditSpec, argSpec.private, argSpec.mode ); 
					} {
						nil;
					};
				}).select(_.notNil);
				args = argSpecs.collect({ |item| [ item.name, item.default ] }).flatten(1);
				this.changed( \init );
			} {
				mixed = true;
			};
		} {
			mixed = true;
		};
	}
	
	unitArgName { ^units.detect(_.isUMap).unitArgName }
	
	asUnitArg { }
	
	isUMap { ^true }
	
	defName { 
		var numUMaps, numValues;
		if( mixed ) {
			numUMaps = units.count(_.isUMap);
			numValues = units.size - numUMaps;
			^("mixed" + "(% umaps%)".format( numUMaps, if( numValues > 0 ) { 
				", % values".format( numValues ) 
			} { "" }
			)).asSymbol
		} {
			^((this.def !? { this.def.name }).asString + 
				"(% umaps)".format( units.size )).asSymbol
		};
	}
	
	def_ { |def| 
		units.do({ |item|
			if( item.isUMap ) { item.def = def };
		});
		this.init( units ); 
	}	
	
	remove {
		units.do({ |item|
			if( item.isUMap ) { item.remove };
		});
		this.init( units ); 
	}
}

MassEditUMapSpec : Spec {
	
	var <>default;
	// placeholder for mass edit MassEditUMap
	
	*new { |default|
		^super.newCopyArgs( default );
	}
	
	viewNumLines { 
		if( default.mixed ) { 
			^1.1 
		} {
			^UMapGUI.viewNumLines( default );
		};
	}
	
	constrain { |value| ^value }
	
	map { |value| ^value }
	unmap { |value| ^value }
	
}

+ Object {
	isUMap { ^false }
}