+ Symbol {

	uEnvirPut { |value, spec|
		var default;
		value = value.value;
		if( value.isArray && { spec.notNil } ) {
			spec = spec.massEditSpec( value ).default_( { spec.default }!(value.size) );
		};
		currentEnvironment.put( this, value );
		if( spec.notNil ) {
			currentEnvironment[ \u_specs ] = currentEnvironment[ \u_specs ] ?? {()};
			currentEnvironment[ \u_specs ].put( this, spec );
		};
		currentEnvironment.changed( this );
	}

}