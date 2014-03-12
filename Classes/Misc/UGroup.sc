UGroup {
	
	classvar <>all;
	var <>id;
	var <>groups;
	var <>children;
	var <>parent;
	var <>addAction = \addToHead;
	
	*new { |id = \default, parent| // only returns new object if doesn't exist
		var result;
		result = all.detect({ |item| item.id === id });
		^this.get(id) ?? { this.basicNew( id, parent ) };
	}
	
	*basicNew { |id = \default, parent|
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
		parent !? { UGroup.get(parent) !? { |x| x.end(this) } };
	}
	
	addToAll {
		all !? { all.removeAllSuchThat({ |item| item.id === this.id }); };
		all = all.asCollection.add( this );
		this.class.changed( \all, \add, this );
	}
	
	makeGroup { |target|
		var group;
		group = Group(target, addAction);
		groups = groups.add( group );
		this.changed( \start );
		^group;
	}
	
	free { |target|
		if( groups.size > 0 ) {
			groups.do(_.free);
			groups = [];
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