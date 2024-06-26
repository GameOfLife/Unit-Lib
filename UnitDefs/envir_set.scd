/*
\envir_set

** this Udef can be used in conjunction with 'envir_get' and 'p_envir_get' UMaps **

The 'envir_set' Udef creates a unit that is able to globally share a value with other units used further in the chain, or in other chains. To use the value within the same chain, an 'envir_set' unit should always be first in the order of the chain. It makes the value available as an Environment value, which is globally accessible. To retreive the value in an other unit use the 'p_envir_get' or 'envir_get' UMapDef, and make sure the 'key' setting is the same on both. The value is can also be accessed via SuperCollider code, by adding a '~' in front of the name. The value can be set in 5 different formats: value (0-1), freq (2-20000), amp (0-1, curved for amplitude use), boolean (true/false) and time (0-inf). The 'p_envir_get' UMapDef will automatically map the value to the range of the parameter it is connected to, which means it is possible to use for example the 'freq' value of this unit to influence the 'amp' value of another unit. The 'envir_set' unit itself does not output any sound, it only makes the value available.

value: the value to share (can be UMap, but only from a function, control, midi or pattern category)
key: the name of the variable (default: 'a')
n: number of values. If n > 1 the value will be an Array
type: the type of parameter (\value, \freq, \amp, \boolean, \time)

--
this is an Udef definition file
part of the Unit lib default UDefs set
*/

(
var defs, def, sizeList = [1,2,3,4,5,6,7,8,9,10,12,16,20,24,28,32,48,64,96,128];
defs = [
	\value, [ 0, 1 ],
	\freq, FreqSpec(2,20000),
	\amp, \amp,
	\integer, IntegerSpec(),
	\boolean, BoolSpec(),
	\time, SMPTESpec(),
].clump(2).collect({ |args|
	var type, spec, subdefs, makeUdef;
	#type, spec = args;
	spec = spec.asSpec;

	makeUdef = { |name, argspec, default|
		argspec = argspec ? spec;
		FreeUdef( name, [
			[ \value, default, argspec, false, \init ],
			[ \key, \a, EnvirSpec() ],
		], addToAll: false )
		.apxCPU_( 0 )
		.prepareFunc_({ |server, unit, action, startPos|
			var value;
			value = unit.get( \value );
			unit.get( \key ).asSymbol.uEnvirPut( value.value, spec );
			if( value.isKindOf( UMap ) ) {
				value.start
			};
			action.value;
		})
		.createsSynth_( false )
		.setSynthFunc_({ |unit ...keyValuePairs|
			unit.get( \key ).asSymbol.uEnvirPut( unit.get( \value ).value, spec );
		});
	};

	subdefs = [ makeUdef.value( sizeList[0], spec, 0 ) ];

	subdefs = subdefs ++ sizeList[1..].collect({ |size|
		makeUdef.value( size,
			spec.massEditSpec( { spec.default } ! size ),
			0 ! size
		);
	});

	 MultiUdef( type, subdefs, \envir, \n, false, false )
});

def = MultiUdef( \envir_set, defs, \envir, \type, false )
.apxCPU_( 0 );

def
)
