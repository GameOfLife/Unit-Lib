/*
\p_envir_get

Creates an pattern UMap for getting environment variables. Use in conjunction with the 'envir_set' Udef. Environment values are also accessible via regular SuperCollider code, by adding a '~' in front of the name.

key: the name of the variable (default: 'a')

The value will automatically be scaled to the value range of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UPatDef( \p_envir_get, { |unit, key = 'a'|
	{
		var kkey, val, spec, fromSpec;
		spec = unit.spec ?? { [0,1].asSpec; };
		loop { 
			kkey = key.next.asSymbol;
			if( kkey.notNil ) {
				val = currentEnvironment[ kkey ] ? (spec.default);
				fromSpec = currentEnvironment[ \u_specs ] !? _[ kkey ] ? spec;
				if( fromSpec.isKindOf( FreqSpec ) && { spec.isKindOf( FreqSpec ) } ) {
					fromSpec = spec;
				};
				if( fromSpec.isKindOf( PointSpec ) ) {
					fromSpec = fromSpec.asControlSpec;
					if( val.isKindOf( Point ) ) { val = val.x };
				};
				val = fromSpec.unmap( val );
			} {
				val = spec.unmap( spec.default );
			};
			val.yield;
		};
	}.r
})	
	.setSpec( \key, SymbolSpec() )
	.mappedArgs_([])
	.category_( 'pattern' );