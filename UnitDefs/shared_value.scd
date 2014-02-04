/*
\shared_value

** this Udef should be used in conjunction with 'shared_in' UMaps **

The 'shared_value' Udef creates a unit that is able to share a value or UMap with other units used further in the chain. The 'shared_value' unit would always need to be first in the order of the chain; it sends the value to a bus to the other units. To retreive the value in an other unit use the 'shared_in' UMapDef, and make sure the 'id' setting is the same on both. The value can be set in 5 different formats: value (0-1), freq (2-20000), amp (0-1, curved for amplitude use), trigger (a trigger signal) and boolean (true/false). The 'shared_in' UMapDef will automatically map the value to the range of the parameter it is connected to, which means it is possible to use for example the 'freq' value of this unit to influence the 'amp' value of another unit. The shared_value itself does not output any sound, it only sends the value. All this will only work _within_ a single chain.

value: the value to share (can be UMap)
range: the range of the value to be shared (not available for 'trigger' and 'boolean' types)
id: the id (0-99) by which the value can be retreived by a 'shared_in' UMap
type: the type of parameter range (\value, \freq, \amp, \trigger, \boolean)

--
this is an Udef definition file
part of the Unit lib default UDefs set
*/

(
var defs, def;
defs = [ 
	\value, [ 0, 1 ], 
	\freq, FreqSpec(2,20000), 
	\amp, \amp,
	\trigger, TriggerSpec(),
	\boolean, BoolSpec(),
].clump(2).collect({ |args|
	var type, spec;
	#type, spec = args;
	spec = spec.asSpec;
	Udef( type, {
		var sig, range, id;
		switch( type,
			\trigger,  { sig = \value.tr( spec.default ); },
			\boolean, { sig = \value.kr( 1.0 ); }, 
			{ 
				sig = \value.kr( spec.default ); 
				sig = spec.unmap( sig );
				range = spec.unmap( \range.kr([spec.minval, spec.maxval]) );
				sig = sig.linlin(*range ++ [0,1,\none]);
			}
		);
		id = \id.kr(0);
		ReplaceOut.kr(id + 1400, sig);
	}, addToAll: false)
		.setSpec( \value, spec )
		.setSpec( \range, spec.asControlSpec.asRangeSpec )
		.setSpec( \id, IntegerSpec( 0, 0, 99 ) );
});

def = MultiUdef( \shared_value, defs, \utility, \type, false );

def
)