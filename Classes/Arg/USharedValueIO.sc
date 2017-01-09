USharedValueIn {
	
	classvar <>busOffset = 1400;
	
	*spec { |default| ^SharedValueIDSpec( default ) }
	
	*makeInput { |id, default = 0|
		if( id.isNil ) { id = \id };
		if( id.isKindOf( Symbol ) ) {
			Udef.addBuildSpec(
				ArgSpec(id, default, this.spec( default ) ) 
			);
			id = id.kr( default );
		};
		^id
	}
	
	*kr { |id, default = 0|
		^In.kr( this.makeInput( id, default ) + this.busOffset );
	}
}

USharedValueOut : USharedValueIn {

	*kr { |id, channelsArray, default = 0|
		if( id.isArray ) {
			channelsArray = channelsArray.asArray;
			default = default.asArray;
			^id.collect({ |id, i|
				this.single( id, channelsArray.wrapAt(i), default.wrapAt(i) );
			});
		} {
			^this.single( id, channelsArray, default );
		}
	}
	
	*single { |id, channel, default = 0|
		^ReplaceOut.kr( this.makeInput( id, default ) + this.busOffset, channel );
	}
}