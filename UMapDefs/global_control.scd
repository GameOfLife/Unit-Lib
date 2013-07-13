ValueUMapDef( \global_control, { |unit, ctrl|
	var key, spec;
	UGlobalControl.removeDependant( ctrl );
	spec = unit.getSpec( \value );
	key = unit.get( \controlKey );
	if( key.isNil ) {
		key = 'global_0';
		unit.set( \controlKey, key );
	};
	key = key.asSymbol;
	"listening to UGlobalControl '%'\n".postf( key );
	ctrl = { |obj, key|
		var val;
		val = UGlobalControl.current.get( key );
		if( val.notNil ) {
			unit.mapSet( \value, val );
		};
	};
	UGlobalControl.addDependant( ctrl );
	UGlobalControl.current.put( key, UGlobalControl.current.at( key ) );
	ctrl;
}, { |unit, ctrl|
	UGlobalControl.removeDependant( ctrl );
	nil;
}, [
	[ \controlKey, nil, UGlobalControlKeySpec() ]
], \control );