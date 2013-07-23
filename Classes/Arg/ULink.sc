ULink {
	var <>url;
	var <unit, <chain, <>spec, <>unitArgName;
	var key;
	
	*new { |url|
		^super.newCopyArgs(url);
	}
	
	asControlInput { ^this.asControlInputFor( Server.default ) }
	
	asControlInputFor { |server, startPos|
		var out, split;
		if( chain.notNil && { url.size > 0 } ) {
			split = url.asString.split( $/ ).select(_.size > 0);
			if( split[0].couldBeNumber ) {
				split = [ split[0].interpret ] ++ split[1..].collect(_.asSymbol);
				if( chain.units.indexOf( unit ) == split[0] ) {
					"%:asControlInput - can only link to units before self"
						.postf( this.class );
					^this.fallBack( server, startPos )
				};
				out = chain;
				split.do({ |item|
					key = item;
					if( out.respondsTo( \at ) ) {
						out = out[ item ];
						if( out.isNil ) {
							"%:asControlInput - '%' not found\n".postf( this.class, item );
							^this.fallBack( server, startPos )
						};
					} {
						"%:asControlInput - % doesn't respond to .at('%')\n"
							.postf(  this.class, out, item );
						^this.fallBack(  server, startPos )
					};
				});
				^out.asControlInputFor( server, startPos );
			} {
				"%:asControlInput - url should start with a number\n".postf( this.class );
				^this.fallBack( server, startPos )
			};
		} {
			^this.fallBack( server, startPos )
		};
	}
	
	fallBack { |server, startPos| 
		^(spec !? _.default ? 0).asControlInputFor( server, startPos ); 
	}
		
	asUnitArg { |unit, key|
		spec = unit.getSpec( key );
		this.unitArgName = key; 
	}
	
	unit_ { |aUnit|
		if( aUnit.notNil ) {
			unit = aUnit;
			chain = UChain.nowPreparingChain ? chain;
		} {
			unit = nil;
			chain = nil;
		};
	}
	
	disposeFor {
		this.unit = nil;
	}
	
	dispose {
		this.unit = nil;
	}
}

ULinkSpec : Spec {
	var <>default;
	
	*new { |def, default|
		default = default ?? { ULink() };
		^super.newCopyArgs.default_( default );
	}
	
	map { |val| ^val }
	unmap { |val| ^val }
	
	constrain { |value|
		^value
	}
}

