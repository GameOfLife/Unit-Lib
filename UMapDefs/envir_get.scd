/*
\envir_get

Creates an pattern UMap for getting environment variables. Use in conjunction with the 'envir_value' Udef. Environment values are also accessible via regular SuperCollider code, by adding a '~' in front of the name.

key: the name of the variable (default: 'a')

The value will automatically be scaled to the value range of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

ValueUMapDef( \envir_get, { |unit, ctrl|
	var kkey, val, spec, fromSpec;
	ctrl.remove;
	kkey = unit.get( \key );
	spec = unit.getSpec( \value );
	ctrl = SimpleController( currentEnvironment )
		.put( kkey, {
			spec = unit.getSpec( \value );
			val = currentEnvironment[ kkey ] ? (spec.default);
			fromSpec = currentEnvironment[ \u_specs ] !? _[ kkey ] ? spec;
			if( fromSpec.isKindOf( FreqSpec ) && { spec.isKindOf( FreqSpec ) } ) {
				fromSpec = spec;
			};
			if( fromSpec.isKindOf( PointSpec ) ) {
				fromSpec = fromSpec.asControlSpec;
				if( val.isKindOf( Point ) ) { val = val.x };
			};
			unit.mapSet( \value, fromSpec.unmap( val ) );
		});
	currentEnvironment.changed( kkey );
	ctrl
},  { |unit, ctrl|
	ctrl.remove;
	nil;
},[
	[ \key, \a, SymbolSpec("") ]
], \control )
	.category_( 'control' );