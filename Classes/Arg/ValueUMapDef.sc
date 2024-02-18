ValueUMapDef : UMapDef {

	classvar <>activeUnits;

	var <>valueIsMapped = true;
	var <>startFunc, <>endFunc;

	*defaultGUIColor { ^Color.yellow.blend( Color.white, 0.8 ).alpha_(0.3); }

	defType { ^\control }

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
			[ \u_useSpec, true, BoolSpec(true), true ],
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
		var out, spec;
		out = unit.get( \value );
		if( out.isUMap.not && { this.useMappedArgs && valueIsMapped }) {
			if( unit.get( \u_useSpec ) == false ) {
				spec = [0,1].asSpec;
			} {
				spec = unit.get( \u_spec ) ?? { [0,1].asSpec };
			};
			^spec.map(
				unit.getSpec( \value ).unmap( out )
			);
		} {
			^out;
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