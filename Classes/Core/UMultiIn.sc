UMultiMonoIn : UIn { // overwrites bus (ReplaceOut)

	*ar { |endPoint = false|
		^this.new1( \ar, 0, endPoint );
	}

	*kr { |id = 0, endPoint = false|
		^this.new1( \kr, 0, endPoint );
	}

	*getMultiControls { |type = 'i', selector = 'ar'|
		^32.collect({ |i|
			this.getControl( \kr, this.getControlName( type,  selector, i ), "bus", i);
		});
	}

	*getMultiIndex {
		^\u_index.ir(0);
	}

	*new1 { |selector = \ar, id = 0, endPoint = false|
		var res;
		var controls, index;
		controls = this.getMultiControls( 'i', 'ar' );
		index = this.getMultiIndex;
		id = Select.kr( index, controls );
		res = In.perform( selector, this.firstBusFor( selector ) + id, 1 );
		if( endPoint ) {
			ReplaceOut.perform( selector, this.firstBusFor( selector ) + id, Silent.ar );
			Udef.buildUdef.inputIsEndPoint = true;
		};
		^res;
	}
}

UMultiMonoOut : UMultiMonoIn { // overwrites bus (ReplaceOut)

	*ar { |input, offset = false|
		^this.new1( \ar, input, offset );
	}

	*kr { |input|
		^this.new1( \kr, input );
	}

	*new1 { |selector = \ar, input, offset = false|
		var controls, index, id;
		controls = this.getMultiControls( 'o', 'ar' );
		index = this.getMultiIndex;
		id = Select.kr( index, controls );
		if( offset ) {
			ReplaceOut.perform( selector, this.firstBusFor( selector ) + id, Silent.ar );
			^OffsetOut.perform( selector, this.firstBusFor( selector ) + id, input );
		} {
			^ReplaceOut.perform( selector, this.firstBusFor( selector ) + id, input );
		};
	}
}