/*
example:

x = UChain([ 'sine', [ 'freq', UMap('envir_get', [ 'active', true, 'key', "freq" ]) ] ], 'output');
x.prepareAndStart;

~freq = 330;
~freq = 440;

x.stop;
*/

ValueUMapDef( \envir_get, { |unit, routine|
	var key, spec;
	var update, lastVal;
	routine.stop;
	spec = unit.getSpec( \value );
	key = unit.get( \key );
	if( key.size == 0 ) {
		key = unit.unitArgName;
		unit.set( \key, key.asString );
	};
	key = key.asSymbol;
	"listening to ~%, range %\n".postf( 
		key,
		if( unit.get( \mapped ) ) {
			"0 - 1"
		} {
			"% - %".format( spec.minval, spec.maxval );
		};
	);
	update = {
		var key = unit.get( \key ).asSymbol;
		var val = key.envirGet;
		if( val != lastVal && { spec.class.testObject( val ) }) {
			if( unit.get( \mapped ) ) {
				unit.mapSet( \value, val );
			} {
				unit.set( \value, spec.constrain( val ) );
			};
		};
		lastVal = val;
	};
	update.value; // this seems to make it crash sometimes..
	{
		loop {
			(1/unit.get( \speed )).max(0.01).wait;
			update.value;
		};
	}.fork;
}, { |unit, routine|
	routine.stop;
	nil;
}, [
	[ \key, nil, StringSpec("") ],
	[ \speed, 10, [1,100,\exp,0,10].asSpec ],
	[ \mapped, false, BoolSpec(false) ]
], \control );