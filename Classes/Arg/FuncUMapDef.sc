FuncUMapDef : UMapDef {
	
	// example function for random value
	/*
	{ |unit, range = #[0.0,1.0]|
		range[0] rrand: range[1]; // result sets \value arg
	} 
	*/
	
	var <>valueIsMapped = true;
	
	*new { |name, func, args, valueIsPrivate = false, category, addToAll=true|
		^this.basicNew( name, args ? [], addToAll )
			.initFunc( func, valueIsPrivate ).category_( category ? \default ); 
	}
	
	initFunc { |inFunc, valueIsPrivate|
		func = inFunc;
		argSpecs = ArgSpec.fromFunc( func, argSpecs )[1..];
		argSpecs = argSpecs ++ [
			[ \value, 0, DisplayControlSpec(0,1), valueIsPrivate ], 
			[ \u_spec, [0,1].asSpec, ControlSpecSpec(), true ],
		].collect(_.asArgSpec);
		argSpecs.do(_.mode_( \nonsynth ));
		mappedArgs = [ \value ];
		this.canUseUMap = false;
		this.changed( \init );
	}
		
	isMappedArg { |name|
		if( name == \value ) {
			^valueIsMapped;
		} {
			^mappedArgs.notNil && { mappedArgs.includes( name ) };
		};
	}
	
	asUnmappedArgsArray { |unit, argPairs|
		argPairs = argPairs ? #[];
		^unit.argSpecs.collect({ |item| 
			var val;
			val = argPairs.pairsAt(item.name) ?? { item.default.copy };
			if( this.isMappedArg( item.name ) && { val.isKindOf( UMap ).not } ) { 
				val = item.spec.unmap( val ); 
			};
			[ item.name,  val ] 
		}).flatten(1);
	}
	
	doFunc { |unit|
		var res;
		res = func.value( unit, 
			*this.asUnmappedArgsArray( unit, unit.args ).clump(2).flop[1]
		);
		if( valueIsMapped ) {
			unit.setArg( \value, unit.getSpec( \value ).map( res ) );
		} {
			unit.setArg( \value, res );
		};
	}
	
	prepare { |servers, unit, action|
		this.doFunc( unit );
		action.value;
	}
	
	activateUnit { |unit| // called at UMap:asUnitArg
		if( unit.unit.notNil && { unit.unit.synths.size > 0 } ) {
			unit.prepare;
		};
	}
	
	makeSynth { ^nil }
	
	hasBus { ^false }
	
	value { |unit|
		if( valueIsMapped ) {
			^(unit.get( \u_spec ) ?? { [0,1].asSpec }).map( 
				unit.getSpec( \value ).unmap( unit.value )
			);
		} {
			^unit.value
		};
	}
	
	setSynth { |unit ...keyValuePairs|
		keyValuePairs.clump(2).do({ |item|
			if( [ \u_spec ].includes( item[0] ).not ) {
				this.doFunc( unit );
				unit.unitSet;
			};
		});
	}

}
