ExpandUMapDef : UMapDef {

	var <>makeArgsFunc;

	*new { |name, args, category, addToAll = true|
		if( args.isKindOf( Function ) ) {
				^this.basicNew( name, [], addToAll ).makeArgsFunc_( args )
					.category_( category ? \default ).changed( \init );
		} {
			^this.basicNew( name, args ? [], addToAll )
				.category_( category ? \default ).changed( \init );
		};
	}

	*defaultGUIColor { ^Color.white.alpha_(0.33) }

	defType { ^\mixed }

	isMappedArg { |name|
		^false
	}

	argNeedsUnmappedInput { |key, unit|
		^unit.useSpec == false;
	}

	argSpecs { |unit|
		var specs;
		if( unit.spec.notNil ) {
			^makeArgsFunc.value( unit ).collect(_.asArgSpec) ? argSpecs
		} {
			^argSpecs;
		};
	}

	asArgsArray { |argPairs, unit, constrain = true|
		if( makeArgsFunc.isNil or: { unit.spec.notNil }) {
			^super.asArgsArray( argPairs, unit, constrain );
		} {
			^argPairs; // pass on unchanged
		};
	}

	activateUnit { |unit, parentUnit| // called at UMap:asUnitArg
		unit.init( this, unit.args );
		if( unit.synths.size == 0 && {
			parentUnit.notNil && { parentUnit.synths.size > 0 }
		}) {
				unit.unit_(parentUnit);
				unit.setUMapBus;
				unit.prepareAndStart( unit.unit.synthsForUMap );
			};
	}

	args { |unit|
		^this.argSpecs( unit ).collect({ |item| [ item.name, item.default ] }).flatten(1);
	}

	getArgSpec { |name, unit|
		name = name.asSymbol;
		^this.argSpecs( unit ).detect({ |item| item.name == name });
	}

	getSpec { |name, unit|
		var asp;
		asp = this.getArgSpec(name, unit);
		if( asp.notNil ) { ^asp.spec } { ^nil };
	}

	getValue { |unit| ^unit.values.collect(_.value) }

	getDefault { |name, unit|
		var asp;
		asp = this.getArgSpec(name, unit);
		if( asp.notNil ) { ^asp.default } { ^nil };
	}

	useMappedArgs_ { |bool| useMappedArgs = bool }

	asUnmappedArgsArray { |unit, argPairs|
		argPairs = argPairs ? #[];
		^unit.argSpecs.collect({ |item|
			var val;
			val = argPairs.pairsAt(item.name) ?? { item.default.copy };
			if( this.useMappedArgs && { this.isMappedArg( item.name ) } ) {
				val = item.spec.unmap( val.value );
			} {
				val = val.value;
			};
			[ item.name,  val ]
		}).flatten(1);
	}

	prepare { |servers, unit, action| action.value }

	needsPrepare { ^false }

	makeSynth { |unit, target, startPos = 0, synthAction|
		^target;
	}

	hasBus { ^false }

	value { |unit|
		if( unit.useSpec == false ) {
			^unit.values.collect({ |item|
				if( item.isUMap.not ) {
					unit.spec.asControlSpec.unmap( item );
				} {
					item
				};
			});
		} {
			^unit.values;
		};
	}

	setSynth { |unit ...keyValuePairs|
		unit.unitSet;
	}

	getNext { |unit|
		^this.value( unit ).do(_.next);
	}

	getControlInput { |unit|
		^this.value( unit ).collect(_.asControlInput( unit ));
	}

	canInsert { ^false }

	allowedModes { |unit|
		var modes = [ \init, \sync, \normal ]; // add \init when solution for setting problem is found
		if( unit.notNil ) {
			unit.args.do({ |item|
				if( item.isUMap ) {
					modes.copy.do({ |mode|
						if( item.allowedModes.includes( mode ).not ) {
							modes.remove( mode );
						};
					});
				};
			});
		};
		^modes;
	}

}