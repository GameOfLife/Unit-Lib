UMapDef : Udef {
	classvar <>all, <>defsFolders, <>userDefsFolder;
	classvar <>defaultCanUseUMapFunc;

	var <>mappedArgs;
	var <useMappedArgs = true;
	var <>outputIsMapped = true;
	var >canInsert;
	var >insertArgName;
	var <>allowedModes = #[ sync, normal ];
	var <>canUseUMapFunc;
	var <>apxCPU = 0;
	var >guiColor;
	var <>numChannelsForPlayBufFunc;

	*initClass{
		this.defsFolders = [
			this.filenameSymbol.asString.dirname.dirname.dirname +/+ "UMapDefs"
		];
		this.userDefsFolder = Platform.userAppSupportDir ++ "/UMapDefs/";
		defaultCanUseUMapFunc = { |unit, key, umapdef|
			unit.getSpec( key ).respondsTo( \asControlSpec ) && {
				unit.getDefault( key ).asControlInput.asCollection.size == umapdef.numChannels
			};
		};
	}

	defType { ^\dynamic }

	*prefix { ^"umap_" } // synthdefs get another prefix to avoid overwriting

	*from { |item| ^item.asUDef( this ) }

	*useMappedArgs {
		^this.buildUdef.useMappedArgs ? true;
	}

	*useMappedArgs_ { |bool = true|
		this.buildUdef.useMappedArgs = bool;
	}

	useMappedArgs_ { |bool = true|
		if( Udef.buildUdef == this ) {
			useMappedArgs = bool;
		} {
			"UMapDef:useMappedArgs can only be set from inside the synth function".warn;
		};
	}

	dontStoreValue { ^false }

	asArgsArray { |argPairs, unit, constrain = true|
		argPairs = prepareArgsFunc.value( argPairs ) ? argPairs ? #[];
		^this.argSpecs( unit ).collect({ |item|
			var val;
			val = argPairs.pairsAt(item.name) ?? { item.default.copy };
			val = val.deepCopy.asUnitArg( unit, item.name );
			if( constrain && this.isMappedArg( item.name ).not && { val.isKindOf( UMap ).not } ) {
				val = item.constrain( val )
			};
			[ item.name,  val ]
		}).flatten(1);
	}

	isMappedArg { |name|
		^this.mappedArgs.notNil && { this.mappedArgs.includes( name ) };
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

	args { |unit|
		^this.argSpecs( unit ).collect({ |item| [ item.name, item.default ] }).flatten(1);
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
			if( this.useMappedArgs && { this.isMappedArg( item[0] ) && { item[1].isUMap.not } } ) {
				[ item[0], this.getSpec( item[0], unit ) !? _.unmap( item[1] ) ? item[1] ];
			} {
				item
			};
		}).flatten(1);
		this.prSetSynth( unit.synths, *keyValuePairs );
	}

	activateUnit { |unit, parentUnit| // called at UMap:asUnitArg
		if( unit.synths.size == 0 && {
			parentUnit.notNil && { parentUnit.synths.size > 0 }
		}) {
				unit.unit_(parentUnit);
				unit.setUMapBus;
				unit.prepareAndStart( unit.unit.synthsForUMap );
			};
	}

	asUMapDef { ^this }

	isUdef { ^false }

	needsStream { ^false }

	getNext { }

	performUpdate { }

	getControlInput { |unit|
		if( this.hasBus ) {
			if( this.numChannels > 1 ) {
				^this.numChannels.collect({ |i|
					("c" ++ (this.getBus(unit) + i + unit.class.busOffset)).asSymbol;
				});
			} {
				^("c" ++ (this.getBus(unit) + unit.class.busOffset)).asSymbol;
			};
		} {
			^this.value( unit ).asControlInput;
		};
	}

	value { |unit|
		// subclass may put something different here
		^unit !? {|x| x.spec.default } ? 0;
	}

	getValue { |unit|
		 ^unit.get( \value ) ? unit
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

	unitCanUseUMap { |unit, key|
		^(canUseUMapFunc ? defaultCanUseUMapFunc).value( unit, key, this );
	}

	canInsert {
		^(canInsert != false) && { this.insertArgName.notNil; };
	}

	insertArgName {
		if( insertArgName.isNil ) {
			if( outputIsMapped ) {
				insertArgName = mappedArgs.asCollection.detect({ |item|
					this.getDefault( item ).asControlInput.asCollection.size == this.numChannels
				});
			} {
				insertArgName = argSpecs.select({ |item|
					item.private.not && { mappedArgs.asCollection.includes( item.name ).not }
				}).detect({ |item|
					item.default.asControlInput.asCollection.size == this.numChannels;
				}) !? _.name;
			};
		};
		^insertArgName;
	}

	argNeedsUnmappedInput { |key|
		^useMappedArgs && { this.isMappedArg( key ) }
	}

	*defaultGUIColor { ^Color.blue.blend( Color.white, 0.8 ).alpha_(0.3); }

	guiColor { ^guiColor ? (this.class.defaultGUIColor) }
}

UMap : U {

	/*
	example:
	x = UChain([ 'sine', [ 'freq', UMap() ] ], 'output');
	x.prepareAndStart;
	x.stop;
	*/

	classvar <>allUnits;
	classvar <>currentBus = 0, <>maxBus = 499;
	classvar >guiColor;
	classvar <>allStreams;
	classvar <>currentStreamID = 0;

	var <spec;
	var <>useSpec;
	var <>unitArgName;
	var <>unitArgMode;
	var <>unmappedKeys;
	var <>streamID;
	var <>deactivateOnEnd = false;

	*busOffset { ^1500 }

	*guiColor { ^guiColor ?? { guiColor = Color.blue.blend( Color.white, 0.8 ).alpha_(0.3) }; }
	guiColor { ^this.subDef !? _.guiColor ? this.class.guiColor }

	init { |in, inArgs, inMod|
		super.init( in, inArgs ? [], inMod );
		this.setunmappedKeys( inArgs );
		this.mapUnmappedArgs;
		if( this.subDef.isKindOf( UPatDef ) && { spec.notNil } ) { this.makeStream };
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
	    allStreams = Order();
	}

	*defClass { ^UMapDef }

	update { |...args|
		this.def !? _.performUpdate( this, *args );
	}

	asControlInput {
		^this.def.getControlInput(this);
	}

	asOSCArgEmbeddedArray { | array| ^this.asControlInput.asCollection.asOSCArgEmbeddedArray(array) }

	getBus {
		^this.def.getBus( this );
	}

	setBus { |bus = 0|
		this.def.setBus( bus, this );
	}

	nextBus {
		var res, nextBus, n;
		n = this.def.numChannels;
		nextBus = currentBus + n;
		if( nextBus > (maxBus + 1) ) {
			nextBus = 0 + n;
			res = 0;
		} {
			res = currentBus;
		};
		currentBus = nextBus;
		^res;
	}

	setUMapBus {
		if( this.hasBus ) {
			this.setBus( this.nextBus );
		};
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

	hasBus { ^this.def.hasBus( this ) }

	setUMapBuses { } // this is done by the U for all (nested) UMaps

	u_waitTime { ^this.waitTime }

	dontStoreArgNames { ^[ 'u_dur', 'u_doneAction', 'u_mapbus', 'u_spec', 'u_store', 'u_prepared', 'u_originalSpec', 'u_useSpec' ] ++ if( this.def.dontStoreValue ) { [ \value ] } { [] } }

	spec_ { |newSpec|
		if( spec.isNil ) {
			if( newSpec.notNil ) {
				spec = newSpec;
				this.mapUnmappedArgs;
			};
		} {
			if( newSpec != spec ) {
				this.def.mappedArgs.do({ |key|
					var val;
					val = this.get( key );
					if( val.isUMap.not ) {
						this.set( key, this.getSpec( key ).unmap( this.get( key ) ) );
					} {
						val.spec = nil;
					};
				});
				spec = newSpec;
				unmappedKeys = this.def.mappedArgs.copy;
				this.mapUnmappedArgs;
			}
		}
	}

	mapUnmappedArgs {
		if( spec.notNil ) {
			unmappedKeys.copy.do({ |key|
				var val;
				val = this.get( key );
				if( val.isUMap.not ) {
					this.set( key, this.getSpec( key ).map( val ) );
				} {
					val.spec = this.getSpec( key );
				};
			});
		};
	}

	canUseUMap { |key, umapdef|
		^this.def.canUseUMap == true &&
		{ umapdef.allowedModes.includes( this.getSpecMode( key ) ) && {
			if( spec.notNil ) {
				this.getSpec( key ).isKindOf( UAdaptSpec ) or:
				{ umapdef.unitCanUseUMap( this, key ); }
			} {
				true // allow any umap at creation time
			}
		};
		}
	}

	// UMap is intended to use as arg for a Unit (or another UMap)
	asUnitArg { |unit, key|
		var notMappedArg = true;
		if( unit.canUseUMap( key, this.def ) ) {
			this.unitArgName = key;
			if( key.notNil ) {
				this.unitArgMode = unit.getSpecMode( key );
				if( unit.argNeedsUnmappedInput( key ) ) {
					if( unit.spec.notNil ) {
						this.spec = unit.getSpec( key ).copy;
						this.useSpec = false;
						this.set( \u_spec, this.spec );
						this.set( \u_useSpec, false );
					};
				} {

					if( unit.isKindOf( UMap ) && { unit.def.isMappedArg( key ) } ) {
						notMappedArg = false;
					};

					if( ( notMappedArg == true ) or: { unit.spec.notNil } ) {						this.spec = unit.getSpec( key ).copy;
						this.useSpec = true;
						this.set( \u_spec, this.spec );
						this.set( \u_useSpec, true );
					};
				};
				this.subDef.activateUnit( this, unit );
				this.valuesAsUnitArg
			};
			^this;
		} {
			^unit.getDefault( key );
		};
	}

	argNeedsUnmappedInput { |key|
		^this.subDef.argNeedsUnmappedInput( key, this );
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
				this.unit.prSet( this.unitArgName, this );
			};
		};
	}

	getSynthArgs {
		var nonsynthKeys;
		nonsynthKeys = this.argSpecs.select({ |item| item.mode == \nonsynth }).collect(_.name);
		^this.args.clump(2).select({ |item| nonsynthKeys.includes( item[0] ).not })
			.collect({ |item|
				if( this.def.useMappedArgs && {this.def.isMappedArg( item[0] ) && { item[1].isUMap.not }}) {
					[ item[0], this.getSpec( item[0] ) !? _.unmap( item[1] ) ? item[1] ];
				} {
					item
				};
			})
			.flatten(1);
	}

	value { ^this.def.getValue( this ) }

	numChannelsForPlayBuf {
		^this.def.numChannelsForPlayBufFunc.value( this ) ? 1
	}

	/// UPat

	stream {
		^allStreams[ streamID ? -1 ];
	}

	stream_ { |stream|
		if( stream.isNil ) {
			if( streamID.notNil ) {
				allStreams.removeAt( streamID );
			};
		} {
			this.makeStreamID;
			allStreams[ streamID ] = stream;
		};
	}

	*nextStreamID {
		^currentStreamID = currentStreamID + 1;
	}

	makeStreamID { |replaceCurrent = false|
		if( replaceCurrent or: { streamID.isNil }) {
			streamID = this.class.nextStreamID;
		};
	}

	makeStream {
		this.def.makeStream( this );
	}

	resetStream {
		if( this.def.needsStream ) {
			this.stream = nil;
			this.makeStreamID( true );
		};
	}

	resetStreams {
		this.resetStream;
		this.args.pairsDo({ |key, item|
			if( item.isKindOf( UMap ) ) {
				item.resetStreams;
			};
		});
	}

	next {
		this.def.getNext( this );
		^this.asControlInput;
	}

	disposeFor { |...args|
		this.def.disposeFor( this, *args );
		if( this.unit.notNil && { this.unit.synths.select(_.isKindOf( Synth ) ).size == 0 }) {
			this.unit = nil;
			if( this.subDef.isKindOf( TaskUMapDef ) ) { this.free; };
		};
		if( this.subDef.isKindOf( FuncUMapDef ) or: this.subDef.isKindOf( TaskUMapDef ) ) {
			this.values.do{ |val|
	       	 if(val.respondsTo(\disposeFor)) {
		            val.disposeFor( *args );
		        }
		    };
		};
	}

	dispose {
	    this.free;
		this.def.dispose( this );
	    this.values.do{ |val|
	        if(val.respondsTo(\dispose)) {
	            val.dispose
	        }
	    };
	    this.modPerform( \dispose );
	    this.preparedServers = nil;
	    this.unit = nil;
	}

	allowedModes { ^this.def.allowedModes( this ) }
}

MassEditUMap : MassEditU {

	var <>mixed = false;

	init { |inUnits|
		var defs, sd;
		var dkey, dval;
		units = inUnits.asCollection;
		if( units.every(_.isUMap) ) {
			defs = inUnits.collect(_.def);
			sd = inUnits.collect(_.subDef);
			if( sd.every({ |item| item == sd[0] }) ) {
				def = defs[0];

				if( def.isKindOf( MultiUMapDef ).not or: {
					dkey = def.defNameKey;
					dval = units[0].get( dkey );
					units.every({ |unit|
						unit.get( dkey ) == dval
					});
				}) {
					argSpecs = def.argSpecs( inUnits[0] );
				} {
					argSpecs = [ def.getArgSpec( dkey, units[0] ) ];
				};

				if( def.isKindOf( MultiUMapDef ) ) {
					defNameKey = def.defNameKey;
					subDef = units[0].subDef;
					if( units[1..].any({ |item| item.subDef != subDef }) ) {
						subDef = nil;
					};
				} {
					subDef = units[0].subDef;
				};


				argSpecs = units[0].argSpecs.collect({ |argSpec|
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
				argSpecs = units[0].argSpecs.select({ |argSpec|
					units.every({ |item|
						item.getArgSpec( argSpec.name ) == argSpec
					});
				}).collect({ |argSpec|
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
			^("mixed" + "(% umap%%)".format( numUMaps, if(numUMaps != 1) { "s" } { "" }, if( numValues > 0 ) {
				", % value%".format( numValues, if(numValues != 1) { "s" } { "" } )
			} { "" }
			)).asSymbol
		} {
			^((this.def !? { units[0].fullDefName }).asString +
				"(% umaps)".format( units.size )).asSymbol
		};
	}

	fullDefName {
		^this.defName;
	}

	def {
        ^if( mixed ) { ^nil } { def ?? { defName.asUdef( this.class.defClass ) } };
    }

	def_ { |def|
		units.do({ |item|
			if( item.isUMap ) { item.def = def };
		});
		this.init( units );
	}

	getInitArgs {
		if( mixed ) { ^nil } { ^super.getInitArgs };
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