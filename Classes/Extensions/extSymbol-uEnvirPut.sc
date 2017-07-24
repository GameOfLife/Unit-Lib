+ Symbol {
	
	uEnvirPut { |value, spec|
		currentEnvironment.put( this, value.value );
		currentEnvironment[ \u_specs ] = currentEnvironment[ \u_specs ] ?? {()};
		currentEnvironment[ \u_specs ].put( this, spec );
		currentEnvironment.changed( this );
	}
	
}