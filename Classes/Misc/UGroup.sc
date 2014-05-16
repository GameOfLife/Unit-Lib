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

UGroup {
	
	classvar <>all;
	var <>id;
	var <>groups;
	var <>children;
	var <>parent;
	var <>addAction = \addToHead;
	
	*new { |id = \default, parent| // only returns new object if doesn't exist
		^this.get(id) ?? { this.basicNew( id, parent ) };
	}
	
	*basicNew { |id = \default, parent|
		var check = if(parent == id){ Error("UGroup parent must be different from self").throw };
		^super.new.id_( id ).parent_(parent).addToAll;
	}
	
	*start { |id, targets, obj, parent|
		if( id.notNil ) {
			^this.new( id, parent ).start( targets, obj );
		} {
			^targets;
		};
	}
	
	*end { |obj|
		all.do({ |item|
			item.end( obj );
		});
	}
	
	*get { |id|
		^all.detect({ |item| item.id === id })
	}

	start { |targets, obj|
		var grps;
		grps = this.makeIfEmpty( targets );
		this.addChild( obj );
		^grps;
	}
	
	end { |obj|
		this.removeChild( obj );
		this.freeIfEmpty;
	}
	
	addToAll {
		all !? { all.removeAllSuchThat({ |item| item.id === this.id }); };
		all = all.asCollection.add( this );
		this.class.changed( \all, \add, this );
	}
	
	makeGroup { |target|
		//var d1 = "UGroup#makeGroup - id:% parent:%".format(id, target).postln;
		var group;
		group = Group(target, addAction);
		groups = groups.add( group );
		this.changed( \start );
		^group;
	}
	
	free { |target|
		if( groups.size > 0 ) {
			groups.do(_.free);
			//"freeing %".format(id).postln;
			groups = [];
			parent !? {
				//"removing % from %".format(id, parent).postln;
				UGroup.get(parent) !? { |x| x.end(this) } };
			this.changed( \end );
		};
	}
	
	addChild { |obj|
		children = children.add( obj );
		this.changed( \addChild, obj );
	}
	
	removeChild { |obj|
		var res;
		children !? { 
			res = children.remove( obj );
			if( res.notNil ) { this.changed( \removeChild, obj ); };
		};
	}
	
	makeIfEmpty { |targets|
		var grps;
		targets = targets.asCollection;
		if(parent.isNil){
		if( children.size == 0 ) {
			grps = targets.collect({ |item|
				this.makeGroup( item.asTarget );
				})
		} {
			grps = targets.collect({ |target|
				groups.detect({ |item|
					if( target.isKindOf( LoadBalancer ) ) {
						target.servers.includes( item.server );
					} {
						item.server == target.asTarget.server;
					};
				}) ?? {
					this.makeGroup( target.asTarget );
				};
			});
			}
		} {
			if( children.size == 0 ) {
				var parentUGroup = UGroup(parent);
				parentUGroup.start( targets, this );
				grps = parentUGroup.groups.collect{ |item|
					this.makeGroup( item.asTarget )
				}
			} {
				grps = groups
			}
		};
		^if( targets.size == 1 ) {
			grps[0];
		} {
			grps;
		};
	}
	
	freeIfEmpty {
		if( children.size == 0 ) {
			this.free;
		};
	}
}