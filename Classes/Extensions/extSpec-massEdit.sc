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

+ Spec {

	massEditSpec { |inArray|
		^GenericMassEditSpec()
		.default_( inArray )
		.size_( inArray.size )
		.originalSpec_( this )
	}

	massEditValue { |inArray|
		^inArray
	}

	massEdit { |inArray, params|
		^params;
	}

}

+ Nil {
	massEditSpec { ^nil }
}

+ ControlSpec {

	massEditSpec { |inArray|
		^this.asArrayControlSpec.size_( inArray.size ).default_( inArray );
	}

	massEditValue { |inArray|
		^inArray
	}

	massEdit { |inArray, params|
		^params;
	}

}

+ AngleSpec {

	massEditSpec { |inArray|
		^AngleArraySpec.newFrom( this ).size_( inArray.size ).default_( inArray );
	}

	massEditValue { |inArray|
		^inArray
	}

	massEdit { |inArray, params|
		^params;
	}

}

+ TriggerSpec {

	massEditSpec { |inArray|
		^this.copy;
	}

	massEditValue { |inArray|
		^inArray[0];
	}

	massEdit { |inArray, params|
		^inArray.collect({ |item| params });
 	}
}

+ BoolSpec {

	massEditSpec { |inArray|
		^BoolArraySpec( inArray, trueLabel, falseLabel );
	}

	massEditValue { |inArray|
		^inArray
	}

	massEdit { |inArray, params|
		^params;
	}

}

+ DualValueSpec {

	massEditSpec { |inArray|
		var minmax;
		minmax = this.massEditValue( inArray );
		^RangeSpec( minval, maxval, 1.0e-11, inf, warp, step, minmax, units );
	}

	massEditValue { |inArray|
		if( inArray.notNil ) {
			^[ inArray.flat.minItem, inArray.flat.maxItem ];
		} {
			^[minval, maxval];
		};
	}

	massEdit { |inArray, params|
		var linlinArgs;
		linlinArgs = this.unmap( this.massEditValue( inArray ) ) ++ this.unmap( params );
		^inArray.collect({ |item|
			this.map( this.unmap( item ).linlin( *linlinArgs ) );
		});
 	}
}

+ RangeSpec {

	massEditSpec { |inArray|
		var minmax;
		minmax = this.massEditValue( inArray );
		^RangeSpec( minval, maxval, 1.0e-11, inf, warp, step, minmax, units );
	}

	massEditValue { |inArray|
		if( inArray.notNil ) {
			^[ inArray.flat.minItem, inArray.flat.maxItem ];
		} {
			^[minval, maxval];
		};
	}

	massEdit { |inArray, params|
		var linlinArgs;
		linlinArgs = this.unmap( this.massEditValue( inArray ) ) ++ this.unmap( params );
		^inArray.collect({ |item|
			this.map( this.unmap( item ).linlin( *linlinArgs ) );
		});
 	}
}

+ IntegerSpec {

	massEditSpec { |inArray|
		^ArrayControlSpec( ).adaptToSpec( this ).default_( inArray );
	}

	massEditValue { |inArray|
		^inArray
	}

	massEdit { |inArray, params|
		^params;
	}
}

+ BufSndFileSpec {

	massEditSpec { |inArray|
		^MultiSndFileSpec(inArray, true, this.class );
	}

	massEditValue { |inArray|
		^inArray
	}

	massEdit { |inArray, params|
		^params;
	}

}

+ PartConvBufferSpec {

	massEditSpec { |inArray|
		^MultiPartConvBufferSpec(inArray, true );
	}

	massEditValue { |inArray|
		^inArray
	}

	massEdit { |inArray, params|
		^params;
	}

}


+ DiskSndFileSpec {

	massEditSpec { |inArray|
		^MultiSndFileSpec(inArray, true, this.class );
	}

	massEditValue { |inArray|
		^inArray
	}

	massEdit { |inArray, params|
		^params;
	}

}

+ ArrayControlSpec {

	massEditSpec { |inArray|
		^GenericMassEditSpec()
		.default_( inArray )
		.size_( inArray.size )
		.originalSpec_( this )
	}

	massEditValue { |inArray|
		^inArray
	}

	massEdit { |inArray, params|
		^params;
	}

}
	