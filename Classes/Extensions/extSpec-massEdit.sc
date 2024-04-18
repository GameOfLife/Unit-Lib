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
		^AngleArraySpec.newFrom( this ).size_( inArray.size ).originalSpec_( this ).default_( inArray );
	}

	massEditValue { |inArray|
		^inArray
	}

	massEdit { |inArray, params|
		^params;
	}

}

+ SMPTESpec {

	massEditSpec { |inArray|
		^GenericMassEditSpec()
		.default_( inArray )
		.size_( inArray.size )
		.originalSpec_( this )
		.operations_( [ \invert, \reverse, \sort, \scramble, \random, \line, 'use first for all', \rotate, 'move item', \resample, \curve, \smooth, \flat, \sine, \square, \triangle,  'code...', \post ] )
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

+ RangeSpec {

	massEditSpec { |inArray|
		^MultiRangeSpec( minval, maxval, minRange, maxRange, warp, step, inArray, units )
		.originalSpec_( this )
		.size_( inArray.size )
	}

	massEditValue { |inArray|
		^inArray
	}

	massEdit { |inArray, params|
		^params;
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
	