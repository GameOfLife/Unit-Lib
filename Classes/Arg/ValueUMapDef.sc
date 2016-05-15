ValueUMapDef : UMapDef {
	
	classvar <>activeUnits;
	
	var <>valueIsMapped = true;
	var <>startFunc, <>endFunc;
	
	*initClass {
		activeUnits = IdentityDictionary();
		CmdPeriod.add( this );
	}
	
	*cmdPeriod {
		activeUnits.keys.asArray.do(_.set( \active, false ));
	}
	
	*new { |name, startFunc, endFunc, args, category, addToAll=true|
		^this.basicNew( name, args ? [], addToAll )
			.initFunc( startFunc, endFunc ).category_( category ? \default ); 
	}
	
	initFunc { |instartFunc, inendFunc|
		startFunc = instartFunc;
		endFunc = inendFunc;
		argSpecs = ([
			[ \value, 0, ControlSpec(0,1) ], 
			[ \active, false, BoolSpec(false) ],
			[ \u_spec, [0,1].asSpec, ControlSpecSpec(), true ],
		] ++ argSpecs).collect(_.asArgSpec);
		argSpecs.do(_.mode_( \init ));
		this.setSpecMode( \value, \nonsynth );
		mappedArgs = [ \value ];
		allowedModes = [ \init, \sync, \normal ];
		this.canUseUMap = false;
		this.changed( \init );
	}
	
	makeSynth { ^nil }
	
	prepare { |servers, unit, action|
		this.activateUnit( unit ); // make sure it is running
		action.value;
	}
	
	needsPrepare { ^true }
	
	stop { |unit|
		unit.set( \active, false );
	}
	
	activateUnit { |unit|
		if( unit.get( \active ).booleanValue == true && { 
			activeUnits.keys.includes( unit ).not 
		}) {
			unit.set( \active, true );
		};
	}
	
	hasBus { ^false }
	
	isMappedArg { |name|
		if( name == \value ) {
			^valueIsMapped;
		} {
			^mappedArgs.notNil && { mappedArgs.includes( name ) };
		};
	}
	
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
			switch( item[0],
				 \value, { unit.unitSet; },
				 \active, { 
					 if( item[1].booleanValue ) {
						 activeUnits.put( unit, startFunc.value( unit, activeUnits[ unit ] ) );
					 } {
						 activeUnits.put( unit, endFunc.value( unit, activeUnits[ unit ] ) );
					 };
				 }
			)
		});
	}

}