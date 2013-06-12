FuncUMapDef : UMapDef {
	
	// example function for random value
	/*
	{ |unit, range = #[0.0,1.0]|
		range[0] rrand: range[1]; // result sets \value arg
	} 
	*/
	
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
		this.changed( \init );
	}
	
	mappedArgs_ { |args|
		args = args ? [];
		if( args.includes( \value ).not ) {
			mappedArgs = args.add( \value );
		} {
			mappedArgs = args;
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
		unit.setArg( \value, unit.getSpec( \value ).map(	 		func.value( unit, 
					*this.asUnmappedArgsArray( unit, unit.args ).clump(2).flop[1]
				)
			) 
		);
	}
	
	prepare { |servers, unit, action|
		this.doFunc( unit );
		action.value;
	}
	
	makeSynth { ^nil }
	
	hasBus { ^false }
	
	value { |unit|
		^(unit.get( \u_spec ) ?? { [0,1].asSpec }).map( 
			unit.getSpec( \value ).unmap( unit.value )
		);
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
