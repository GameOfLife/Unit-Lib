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

// a LoadBalancer holds a set of servers and their approximate cpu loads

LoadBalancer {

	classvar <>all;
	classvar <>bootDelay = 5;

	var <servers;
	var <>name;
	var <>loads;
	var <>verbose = false;
	var <>beforeBootAction, <>afterBootAction;

	*initClass { all = [] }

	*fill { |n = 8, name = "ulib", addr, options|
		addr = addr ? NetAddr( "127.0.0.1", 58000 );
		options = options ?? { Server.defaultOptionsClass.new };
		^this.new( *{ |i|
			Server( name ++ "_" ++ (i+1), NetAddr( addr.hostname, addr.port + i ), options );
		}!n );
	}

	*new { |...servers|
		^super.newCopyArgs( servers ).init.addToAll;
	}

	at { |index = 0| ^servers.at( index ) }

	options { ^servers[0].options }

	options_ { |options| servers.do({ |srv| srv.options = options }) }

	isLocal { ^servers[0].isLocal }

	boot {
		if( this.isLocal ) {
			beforeBootAction.value( this );
			servers[..servers.size-2].collect({ |srv, i|
				srv.doWhenBooted({ servers[i+1].boot });
			});
			servers.last.doWhenBooted({ afterBootAction.value( this ); });
			servers.first.boot;
		} {
			beforeBootAction.value( this );
			afterBootAction.value( this );
		};
	}

	quit { servers.do(_.quit) }

	init {
		loads = 0!servers.size;
	}

	indexOf { |server|
		^servers.indexOf( server );
	}

	addServer { |server|
		if( servers.includes( server ).not ) {
			servers = servers.add( server );
			loads = loads.add(0);
		};
	}

	removeServer { |server|
		var index;
		index = this.indexOf( server );
		if( index.notNil ) {
			servers.removeAt( index );
			loads.removeAt( index );
		} {
			^nil
		};
	}

	addToAll {
		if( all.includes( this ).not ) {
			all = all.add( this );
		};
	}

	removeFromAll {
		all.remove( this );
	}

	addLoad { |server, load = 0|
		var index;
		index = this.indexOf( server );
		if( verbose ) {
			if( load != 0 && index.notNil ) {
				"LoadBalancer-addLoad: adding % for % (%)\n"
					.postf( load, server, index );
			};
		};
		if( index.notNil ) {
			loads[ index ] = loads[ index ] + load;
		};
	}

	setLoad { |server, load = 0|
		var index;
		index = this.indexOf( server );
		if( verbose ) {
			"LoadBalancer-setLoad: setting % for % (%)\n".postf( load, server, index );
		};
		if( index.notNil ) {
			loads[ index ] = load;
		};
	}

	reset { this.init }

	lowest {
		^servers[ loads.minIndex ];
	}

	asTarget { |addLoad = 0|
		var server;
		server = this.lowest;
		this.addLoad( server, addLoad );
		/*
		if( verbose ) {
			if( addLoad != 0 ) {
				"LoadBalancer-asTarget: using %, adding %\n"
					.postf( server, addLoad );
			} {
				"LoadBalancer-asTarget: using %\n".postf( server );
			};
		};
		*/
		^server.asTarget;
	}

	latency { ^servers[0].latency }

	latency_ { |latency| servers.do(_.latency_(latency)); }

	doesNotUnderstand { |selector ...args|
		var res;
		servers[0] !? {
			res = servers[0].perform( selector, *args );
		};
		if( res == servers[0] ) { ^this } { ^res };
	}

	asArray { ^servers }
}

+ Server {

	loadBalancerAddLoad { |load = 0|
		LoadBalancer.all.do({ |lb|
			lb.addLoad( this, load );
		});
	}

	loadBalancerSetLoad { |load = 0|
		LoadBalancer.all.do({ |lb|
			lb.setLoad( this, load );
		});
	}

}