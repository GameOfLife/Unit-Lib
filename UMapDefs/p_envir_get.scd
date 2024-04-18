/*
\p_envir_get

Creates an pattern UMap for getting environment variables. Environment values are also accessible via regular SuperCollider code, by adding a '~' in front of the name. \p_envir_get also features an 'index' parameter indicating which item to return in case the envir variable is an array.

index: index in an array (0-1024). If the array is smaller than the index the value will be wrapped around.
key: the name of the variable (default: 'a')

The value will automatically be scaled to the value range of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UPatDef( \p_envir_get, { |unit, key = 'a', index = 0|
	{
		var kkey, val, spec, fromSpec, ii = 0;
		spec = unit.spec ?? { [0,1].asSpec; };
		loop {
			kkey = key.next.asSymbol;
			if( kkey.notNil ) {
				val = currentEnvironment[ kkey ] ? (spec.default);
				fromSpec = currentEnvironment[ \u_specs ] !? _[ kkey ] ? spec;
				if( fromSpec.respondsTo( \originalSpec ) && { fromSpec.originalSpec.notNil } ) { fromSpec = fromSpec.originalSpec };

				if( val.isCollection ) {
					val = val.wrapAt( index.next.asInteger );
				};

				case { fromSpec.isKindOf( FreqSpec ) && { spec.isKindOf( FreqSpec ) } } {
					fromSpec = spec; // map correctly within range (can be 2-20000 or 20-20000
				} { fromSpec.isKindOf( PointSpec ) } {
					fromSpec = fromSpec.asControlSpec;
					if( val.isKindOf( Point ) ) { val = val.x }; // use x value
				} { fromSpec.isKindOf( IntegerSpec ) } {
					fromSpec = spec.asControlSpec;
				};
				val = fromSpec.unmap( val );
			} {
				val = spec.unmap( spec.default );
			};
			val.yield;
		};
	}.r
})
.setSpec( \key, EnvirSpec() )
.setSpec( \index, IntegerSpec(0,0,1024) )
.setSpec( \value, UAdaptSpec() )
.mappedArgs_([])
.category_( 'pattern_envir' );