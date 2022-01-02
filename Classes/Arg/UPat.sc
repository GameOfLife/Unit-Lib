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

	defType { ^\pattern }

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

	getNext { |unit|
		if( unit.get( \u_prepared ) == false ) {
			this.doFunc( unit );
			unit.setArg( \u_prepared, true );
		};
	}

	getControlInput { |unit|
		var out;
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

	needsStream { ^true }
}

UPatArg {
	var <>unit, <>key, <>spec;

	*new { |unit, key, spec|
		^super.newCopyArgs( unit, key, spec );
	}

	prUnPrepare { |unit|
		if( unit.isKindOf( UMap ) ) {
			if( unit.subDef.isKindOf( FuncUMapDef ) ) {
				unit.u_prepared = false;
			};
			unit.values.do({ |val|
				this.prUnPrepare( val );
			});
		};
	}

	prNextValFuncUMapDef { |unit|
		if( unit.isUMap && {
			unit.subDef.isKindOf( FuncUMapDef ) or:
			{ unit.subDef.isKindOf( ExpandUMapDef ) }
		} ) {
			unit.values.do({ |item|
				this.prNextValFuncUMapDef( item );
			});
			unit.next;
		};
	}

	prNext { |value, unPrepare = false|
		var out;
		if( value.isUMap.not && { spec.notNil } ) {
			out = spec.unmap( value.next );
		} {
			this.prNextValFuncUMapDef( value );
			out = value.next;
		};
		if( unPrepare == true ) { this.prUnPrepare( value ) };
		^out;
	}

	prValue {
		^if( UPatDef.currentUnit.notNil && { unit !== UPatDef.currentUnit }) {
			UPatDef.currentUnit.get( key );
		} {
			unit.get( key );
		};
	}

	at { |index|
		var value;
		value = this.prValue;
		if( value.isKindOf( UMap ) && { value.subDef.isKindOf( ExpandUMapDef ) } ) {
			^this.prNext( value.values.at( index ) );
		} {
			^this.prNext( value ).at( index );
		};
	}

	next { |unPrepare = false|
		^this.prNext( this.prValue, unPrepare );
	}

	doesNotUnderstand { |selector ...args|
		^this.next.perform( selector, *args);
	}
}