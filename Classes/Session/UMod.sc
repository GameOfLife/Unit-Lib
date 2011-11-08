/*
    Unit Library
    The Game Of Life Foundation. http://gameoflife.nl
    Copyright 2006-2011 Miguel Negrao, Wouter Snoei.

    GameOfLife Unit Library: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    GameOfLife Unit Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with GameOfLife Unit Library.  If not, see <http://www.gnu.org/licenses/>.
*/

/*
simple modulator. Can set values for a number of U or UChain objects. Example:


z = UScore(
	UChain(0, 0, 10, 'sine', [ 'output', [ 'u_fadeIn', 1.0, 'u_fadeOut', 1.0 ] ]), 
	UChain(1.0, 2.0, 10, 'sine', [ 'output', [ 'u_fadeIn', 1.0, 'u_fadeOut', 1.0 ] ]), 
	UChain(2.25, 4.0, 10, 'sine', [ 'output', [ 'u_fadeIn', 1.0, 'u_fadeOut', 1.0 ] ])
);

z.gui;

x = UMod( \freq1, [ z[0][0], z[1][0], z[2][0] ], \freq );

x.set( 600 ); // set all freqs to 600

x.get; // get the values

x.destinations; // show the registered destinations
*/

UMod {
	
	classvar <all;
	
	var <>destinations;
	var <>spec;
	
	*initClass {
		all = IdentityDictionary();
	}
	
	*new { |name, objects, keys, specs|
		^super.new.init( objects, keys, specs ).addToAll( name );
	}
	
	init { | objects, keys, specs |
		destinations = this.class.formatDesinations( objects, keys, specs );
	}
	
	addToAll { |name|
		all[ name ] = this;
	}
	
	*formatDesinations { | objects, keys, specs |
		^[ objects, keys, specs ].flop.collect({ |item|
			if( item[0].notNil and: { item[1].notNil } ) {
				UModDest( *item )
			} {
				nil
			};
		}).select(_.notNil);
	}
	
	add { |object, key, spec| // can be multiple
		destinations = destinations ++ this.class.formatDesinations( object, key, spec );
	}
	
	remove { |object, key|
		if( key.isNil ) {
			^destinations.removeAllSuchThat({ |item| item.obj == object });
		} {
			if( object.isNil ) {
				^destinations.removeAllSuchThat({ |item| item.key == key });
			} {
				^destinations.removeAllSuchThat({ |item| 
					(item.object == object) && { item.key == key };
				});
			};
		};
	}
	
	clear {
		destinations = [];
	}
	
	set { |value|
		if( spec.notNil ) {
			value = spec.unmap( value );
		};
		destinations.do(_.set(value));
	}
	
	get {
		if( spec.notNil ) {
			^spec.map( destinations.collect(_.get) );
		} {
			^destinations.collect(_.get)
		};
	}
	
}

UModDest {
	
	// a simple association to an object and a key
	
	var <obj, <key, <>spec;
	
	*new { |obj, key, spec|
		^super.newCopyArgs(  obj, key, spec );
	}
		
	set { |value|
		if( spec.notNil ) {
			value = spec.map( value );
		};
		obj.set( key, value );
	}
	
	get { 
		if( spec.notNil ) {
			^spec.unmap( obj.get( key ) );
		} {
			^obj.get( key );
		};
	}
	
	printOn { arg stream; stream << this.class.name << "(" <<* [obj, key] <<")" }
	
	storeArgs {
		^[ obj, key, spec ]
	}
		
}