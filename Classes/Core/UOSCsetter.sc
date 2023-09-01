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


UOSCsetter {

	classvar <>all;

	var <>uobject;
	var <name;
	var <oscfunc;

	*initClass {
		all = Set();
	}

	*new { |uobject, name|
		^super.newCopyArgs( uobject, name ).init;
	}

	init {

		if( name.isNil && { uobject.respondsTo( \name ) } ) {
			name = uobject.name;
		} {
			"%:init - missing name: did not create OSCFunc\n".postf( this.class );
			^this;
		};

		oscfunc = OSCFunc({ |msg|
			this.set( *msg );
		}, this.oscPath, dispatcher: OSCMethodPatternDispatcher.new );

		oscfunc.permanent = true;
		oscfunc.enable;
		"started UOSCsetter for %\n - messages should start with '/%/'\n - port: %\n".postf( uobject, name, NetAddr.langPort );
	}

	addToAll { all.add( this ) }

	oscPath { ^"/" ++ name ++ "/*" }

	set { |pth ...inArgs|
		var obj;
		var path, args;
		var setObj, setter;
		path = pth.asString[this.oscPath.size-1..];
		path = path.split($/).collect({ |item|
			item = item.asNumberIfPossible;
			if( item.isNumber ) {
				item;
			} {
				item.asSymbol;
			}
		}).select(_.notNil);
		inArgs = inArgs.collect({ |item|
			if( [ "nil", 'nil', "", '' ].includesEqual( item ) ) {
				nil
			} {
				item
			};
		});
		this.uobject.setOrPerform( path, *inArgs );
	}

	enable { oscfunc.enable }

	disable { oscfunc.disable }

	remove {
		oscfunc.free;
		all.remove( this );
	}

	*disable {
		all.do(_.disable);
	}

	*enable {
		all.do(_.enable);
	}

}

UOSCSetterCurrent : UOSCsetter {

	classvar <>default;

	*new { |recvPort, makeDefault = true|
		^super.newCopyArgs().init( recvPort, makeDefault );
	}

	uobject { ^UScore.current }

	remove {
		oscfunc.free;
		if( default === this ) {
			default = nil;
		};
	}

	init { |recvPort, makeDefault = true|

		name = "current";

		oscfunc = OSCFunc({ |msg|
			if( this.uobject.notNil && {
				this.uobject.oscSetter.notNil // only if it has an OSCSetter
			} ) { this.set( *msg ); };
		}, this.oscPath, recvPort: recvPort, dispatcher: OSCMethodPatternDispatcher.new );

		oscfunc.permanent = true;

		if( makeDefault ) {
			if( default.notNil ) {
				default.remove;
			};
			default = this;
		};

		oscfunc.enable;
		"started UOSCsetter for current score\n - messages should start with '/%/'\n - port: %\n".postf( name, recvPort ?? { NetAddr.langPort });
	}
}