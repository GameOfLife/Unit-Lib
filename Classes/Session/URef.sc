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

// a relative pointer to an object inside a U-object
/*

(
~session = USession(
	UScore(
		UChain(0, 0, 10, 'sine', [ 'output', [ 'u_fadeIn', 1.0, 'u_fadeOut', 1.0 ] ])
	)
);
);

~session.gui;

r = URef( 0, 0 );

r.value( ~session ) == ~session[0][0];
*/

URef {
	var <>path; // an array of indices / keys pointing to the object
	var <>object;
	
	*new { |...path|
		^this.newCopyArgs( path );
	}
	
	*fromObject { |object, parent|
		var path;
		path = parent.indexOf( object );
		if( path.notNil ) {
			^this.new( *path ).object_( object );
		} {
			^nil;
		};
	}
	
	getObject { |parent|
		var obj;
		if( path[0].isNil ) { ^parent };
		obj = parent;
		path.do({ |item|
			obj = obj[ item ];
			if( obj.isNil ) { ^nil };
		});
		^obj;
	}
	
	value { |parent| ^this.getObject( parent ) } // always fetch the object from the parent
	
	check { |parent|
		if( object.isNil ) {
			^this.getObject( parent ).notNil;
		} {
			^this.getObject( parent ) == object;
		};
	}
	
	storeArgs { ^path }

}