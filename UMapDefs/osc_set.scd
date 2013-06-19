ValueUMapDef( \osc_set, { |unit, oscfunc|
	var message, spec;
	oscfunc.free;
	spec = unit.getSpec( \value );
	message = unit.get( \message );
	if( message.size == 0 ) {
		message = unit.unitArgName;
		unit.set( \message, message.asString );
	};
	message = message.asSymbol;
	"setting up OSCFunc on port % for message '%', range %\n".postf( 
		NetAddr.langPort, 
		message,
		if( unit.get( \mapped ) ) {
			"0 - 1"
		} {
			"% - %".format( spec.minval, spec.maxval );
		};
	);
	OSCFunc({ |msg, time, addr, recvPort|
		if( unit.get( \mapped ) ) {
			unit.mapSet( \value, msg[1] );
		} {
			unit.set( \value, spec.constrain( msg[1] ) );
		};
	}, message)
}, { |unit, oscfunc|
	oscfunc.free;
	nil;
}, [
	[ \message, nil, StringSpec("") ],
	[ \mapped, false, BoolSpec(false) ]
], \control );