MassEditUPattern : MassEditU {

	init { |inUnits, active = true|
		var firstDef, defs;
		var dkey, dval;
		units = inUnits.asCollection;
		if( units.every({ |item| item.isKindOf( UPattern ) }) ) {
			def = units[0];
			argSpecs = def.argSpecs( inUnits[0] );

			argSpecs = argSpecs.collect({ |argSpec|
				var values, massEditSpec, value;
				values = units.collect({ |unit|
					unit.get( argSpec.name );
				});

				if( values.any(_.isUMap) ) {
					massEditSpec = MassEditUMapSpec( MassEditUMap( values ) );
				} {
					if( argSpec.name != \fadeTimes ) {
						massEditSpec = argSpec.spec.massEditSpec( values );
					};
				};
				if( massEditSpec.notNil ) {
					ArgSpec( argSpec.name, massEditSpec.default, massEditSpec,
						argSpec.private, argSpec.mode );
				} {
					nil;
				};
			}).select(_.notNil);

			args = argSpecs.collect({ |item| [ item.name, item.default ] }).flatten(1);
			if( active == true ) { this.changed( \init ); };
		} {
			"MassEditUPattern:init - not all units are of the same Udef".warn;
		};
	}

	defName { ^\UPattern }
}