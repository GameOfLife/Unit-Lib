PresetBank {
	
	var <>name;
	var <>getFunc, <>setFunc;
	var <>presets;
	
	/*
	getFunc:
	{ |object| object.get } // -> returns settings to store
	
	setFunc:
	{ |object, settings| object.set( settings ) } // -> set settings, return object
	*/
		
	*new { |name, getFunc, setFunc|
		^super.newCopyArgs(name, setFunc, getFunc).init;
	}
	
	init {
		presets = [];
		getFunc = getFunc ?? { { |object| object.copy } }; 
		setFunc = setFunc ?? { { |object, setting| ^setting.copy } };
		this.changed( \init );
	}
	
	put { |key, object, index=0|
		var id;
		if( (id = this.prIndexOf( key )).isNil ) {
			index = (index * 2).min(presets.size);
			presets = presets
				.insert( index, getFunc.value( object ) )
				.insert( index, key );
		} {
			presets[id] = getFunc.value( object ); 
		};
		this.changed( \put );
	}
	
	prIndexOf { |key|
		var i=0, max;
		max = presets.size-1;
		while { i < max } {
			if( presets[i] === key ) {
				^i+1
			};
			i=i+2;
		};
		^nil;
	}
	
	at { |key|
		var index;
		index = this.prIndexOf( key );
		if( index.notNil ) {
			^presets[index];
		} {
			^nil;
		};
	}
	
	get { |object|
		^getFunc.value( object );
	}
	
	set { |key, object|
		var preset;
		if( this.at( key ).notNil ) {
			^setFunc.value( object, this.at( key ) );
		} {
			"%:set - key % not found for bank '%'\n".postf( 
				this.class, 
				key.asCompileString, 
				name.asCompileString
			);
			^object;
		};
	}
	
	findKey { |object|
		var i=1, max, res;
		max = presets.size-2;
		res = getFunc.value( object );
		while { i < max } {
			if( presets[i] == res ) {
				^presets[i-1];
			};
			i=i+2;
		};
		^nil;
	}
	
	
	
}