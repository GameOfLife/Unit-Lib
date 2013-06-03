UMapDef : Udef {
	classvar <>all, <>defsFolders, <>userDefsFolder;
	
	*initClass{
		defsFolders = [ 
			this.filenameSymbol.asString.dirname.dirname.dirname +/+ "UMapDefs"
		];
		userDefsFolder = Platform.userAppSupportDir ++ "/UMapDefs/";
	}
	
	*prefix { ^"umap_" } // synthdefs get another prefix to avoid overwriting
	
	*from { |item| ^item.asUDef( this ) }
	
	asUMapDef { ^this }
	
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
	
	var <>spec;
	
	*busOffset { ^1500 }
	
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
	
	hasBus { ^this.def.hasBus }
	
	setUMapBuses { } // this is done by the U for all (nested) UMaps
	
	u_waitTime { ^this.waitTime }
	
	dontStoreArgNames { ^[ 'u_dur', 'u_doneAction', 'u_mapbus', 'u_spec' ] }
	
	// UMap is intended to use as arg for a Unit (or another UMap)
	asUnitArg { |unit, key|
		this.unit = unit; 
		this.unitArgName = key;
		if( key.notNil ) {
			spec = unit.getSpec( key ).copy;
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
	
	unitArgName {  
		var array;
		^allUnits[ this ] !? { 
			allUnits[ this ][1] ?? {
				array = allUnits[ this ];
				array[1] = array[0].findKeyForValue( this );
				array[1];
			};
		}; 
	}
	
	unitArgName_ { |unitArgName|
		if( allUnits[ this ].notNil ) {
			allUnits[ this ][1] = unitArgName;
		} {
			"Warning: unitArgName_ - no unit specified for\n%\n"
				.postf( this.asCompileString )
		};
	}
	
	unitSet { // sets this object in the unit to enforce setting of the synths
		var unitArgName;
		if( this.unit.notNil ) {	
			unitArgName = this.unitArgName;
			if( unitArgName.notNil ) {
				this.unit.set( unitArgName, this );
			};
		};
	}
}