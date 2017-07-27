+ Symbol {
	
	uEnvirPut { |value, spec|
		currentEnvironment.put( this, value.value );
		if( spec.notNil ) {
			currentEnvironment[ \u_specs ] = currentEnvironment[ \u_specs ] ?? {()};
			currentEnvironment[ \u_specs ].put( this, spec );
		};
		currentEnvironment.changed( this );
	}
	
}