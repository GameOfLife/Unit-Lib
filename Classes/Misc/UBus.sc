/*
    Unit Library
    The Game Of Life Foundation. http://gameoflife.nl
    Copyright 2006-2014 Miguel Negrao, Wouter Snoei.

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
(
Udef.loadOnInit = true;
Udef(\test, { Out.ar(\bus.kr(0), SinOsc.ar([440,880])*0.1) }).setSpec(\bus, PositiveIntegerSpec() );
Udef(\out2, { Out.ar(0, In.ar(\bus.kr(0),2)) }).setSpec(\bus, PositiveIntegerSpec() );
)

(
x = UScore( UChain( [\test, [\bus, UBus(\a,2) ]] ).ugroup_('default'), UChain( [\out2, [\bus, UBus(\a,2)]]).ugroup_('default').addAction_(\addToTail) );
x.prepareAndStart
)

(
x = UScore(
	UChain( [\test, [\bus, UBus(\a,2) ]] ).ugroup_('a'),
	UChain( [\out2, [\bus, UBus(\a,2)]]).ugroup_('a').addAction_(\addToTail),
	UChain( [\test, [\bus, UBus(\b,2) ]] ).ugroup_('b'),
	UChain( [\out2, [\bus, UBus(\b,2)]]).ugroup_('b').addAction_(\addToTail)
);
x.prepareAndStart
)


x.stop
x.dispose
*/


UBus {

	classvar <>all;
	var <>id;
	var <>buses;
	var <children;
	var <>numChannels = 1;

	*new { |id = \default, numChannels = 1| // only returns new object if doesn't exist
		var x = this.get(id) ?? { this.basicNew( id, numChannels ) };
		//"new id: % hash: %".format(id, x.hash).postln;
		^x
	}

	*basicNew { |id = \default, numChannels = 1|
		//"basicNew".postln;
		^super.new.id_( id ).numChannels_(numChannels).addToAll;
	}

	*get { |id|
		//"get id: % returns %".format(id, all.detect({ |item| item.id === id }) ).postln;
		^all.detect({ |item| item.id === id })
	}


	*clear {
		all = nil
	}

	addChild { |obj|
		//"id: % hash: % adding child: % to children: of class %".format(this.id, this.hash, obj, children, children.class).postln;
		this.children_( children.add( obj ) );
		//"children is now %".format(children).postln;
		this.changed( \addChild, obj );
	}

	removeChild { |obj|
		var res;
		//"id: % hash: %".format(this.id, this.hash).postln;
		children !? {
			res = children.remove( obj );
			//"removed: %".format(res).postln;
			if( res.notNil ) { this.changed( \removeChild, obj ); };
		};
	}

	children_{ |obj|
		//"setting children in id: % hash: % to %".format(id, this.hash, obj).postln;
		children = obj
	}

	addToAll {
		all !? { all.removeAllSuchThat({ |item| item.id === this.id }); };
		all = all.asCollection.add( this );
		this.class.changed( \all, \add, this );
	}

	prepare { |servers, startPos = 0, action, unit|
		//"prepare: id: % hash: % servers: % this: % unit: %".format(this.id, this.hash, servers, this, unit).postln;
		this.makeIfEmpty( servers );
		this.addChild( unit );
		action.value;
	}

	disposeFor { |server, unit|
		//"dispose: id: % hash: % server: % unit:%".format(this.id, this.hash, server, unit).postln;
		this.removeChild( unit );
		this.freeIfEmpty;
	}

    asControlInputFor { |server, startPos = 0|
		^buses.detect({ |item|
			item.server == server
		})
	}

	makeBus { |server|
		//var d1 = "UBus#makeBus - id:%".format(id).postln;
		var bus;
		bus = Bus.audio(server, numChannels);
		buses = buses.add( bus );
		//"buses :%".format(buses).postln;
		this.changed( \start );
		^bus;
	}

	free { |target|
		if( buses.size > 0 ) {
			buses.do(_.free);
			//"freeing %".format(id).postln;
			buses = [];
			this.changed( \end );
		};
	}


	makeIfEmpty { |servers|
		servers = servers.asCollection;
		if( children.size == 0 ) {
			servers.collect({ |s|
				this.makeBus( s );
			})
		} {
			servers.collect({ |s|
				buses.detect({ |item|
					item.server == s
				}) ?? {
					this.makeBus( s );
				};
			});
		}
	}

	freeIfEmpty {
		if( children.size == 0 ) {
			this.free;
		};
	}

	deepCopy {
		//"I'm being deep copied id:% hash%".format(id, this.hash).postln;
		^this
	}
}