UPatDef : FuncUMapDef {
	
	classvar <>defaultCanUseUMapFunc;
	classvar <>currentUnit;
	classvar <>valueIsPrivate = true;
	
	*defaultGUIColor { ^Color.green.blend( Color.white, 0.8 ).alpha_(0.4); }
	
	*initClass{
		defaultCanUseUMapFunc = { |unit, key, upatdef|
			unit.getSpec( key ).respondsTo( \asControlSpec ) && {
				unit.getDefault( key ).asControlInput.asCollection.size == upatdef.numChannels
			};
		};
	}
	
	doFunc { |unit|
		var res, was;
		if( unit.stream.isNil ) { this.makeStream( unit ) };
		was = this.class.currentUnit;
		this.class.currentUnit = unit;
		res = unit.stream.next;
		this.class.currentUnit = was;
		if( this.useMappedArgs && valueIsMapped ) {
			unit.setArg( \value, unit.getSpec( \value ).map( res ) );
		} {
			unit.setArg( \value, res );
		};
	}
	
	activateUnit { |unit| // called at UMap:asUnitArg
		unit.makeStreamID;
		if( unit.unit.notNil && { unit.unit.synths.size > 0 } ) {
			unit.prepare;
		};
	}
	
	makeStream { |unit|
		unit.stream = func.value( unit, *this.getStreamArgs( unit ) );
	}
	
	getStreamArgs { |unit|
		^unit.argSpecs.collect({ |item| 
			if( useMappedArgs && { this.isMappedArg( item.name ) } ) { 
				UPatArg( unit, item.name, item.spec ); 
			} {
				UPatArg( unit, item.name );
			};
		});
	}
	
	useMappedArgs_ { |bool = true| useMappedArgs = bool }
	
	getControlInput { |unit|
		var out;
		if( unit.get( \u_prepared ) == false ) {
			this.doFunc( unit );
			unit.setArg( \u_prepared, true );
		};
		out = unit.get( \value );
		if( unit.get( \u_useSpec ) == false ) {
			out = unit.getSpec( \value ).unmap( out );
		};
		if( out.isUMap ) {
			out = out.asControlInput( unit );
		};
		^out;
	}
	
	setSynth { |unit ...keyValuePairs|
		keyValuePairs.clump(2).do({ |item|
			if( [ \u_spec, \u_prepared ].includes( item[0] ).not ) {
				unit.unitSet;
			};
		});
	}

	canInsert {
		^(canInsert != false) && { this.insertArgName.notNil; };
	}
}

UPatArg {
	var <>unit, <>key, <>spec;
	
	*new { |unit, key, spec|
		^super.newCopyArgs( unit, key, spec );	
	}
	
	next {
		var value;
		if( UPatDef.currentUnit.notNil && { unit !== UPatDef.currentUnit }) {
			value = UPatDef.currentUnit.get( key );
		} {
			value = unit.get( key );
		};
		if( value.isUMap.not && { spec.notNil } ) {
			^spec.unmap( value.next );
		} {
			^value.next;
		};
	}
	
	doesNotUnderstand { |selector ...args|
		^this.next.perform( selector, *args);
	}
}