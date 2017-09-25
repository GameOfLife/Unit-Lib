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


+ RangeSpec {
	expandArgSpecs { 
		^[
			ArgSpec( \lo, this.default[0], this.asControlSpec ),
			ArgSpec( \hi, this.default[1], this.asControlSpec.default_( this.default[1] ) ),
		]
	}
	
	expandValues { |obj|
		^obj
	}
	
	objFromExpandValues { |values|
		^values;
	}
}

+ ArrayControlSpec {
	expandArgSpecs { 
		^this.default.collect({ |item, i|
			ArgSpec( ("value" ++ i).asSymbol, item, originalSpec ?? { this.asControlSpec });
		});
	}
	
	expandValues { |obj|
		^obj;
	}
	
	objFromExpandValues { |values|
		^values;
	}
}

+ PointSpec {
	expandArgSpecs {
		^[\x,\y].collect({ |item| ArgSpec( item, 0, this.asControlSpec ) });
	}
	
	expandValues { |obj|
		obj = obj.asPoint;
		^[obj.x, obj.y]
	}
	
	objFromExpandValues { |values|
		^values.asPoint;
	}
}

+ UEQSpec {
	expandArgSpecs {
		var df, specs;
		df = EQdef.fromKey( this.def );
		^df.names.collect({ |item, i|
			df.argNames[i].collect({ |argName, ii|
				var spec;
				spec = df.specs[i][ii];
				ArgSpec( (item ++ "_" ++ argName).asSymbol, spec.default, spec )
			});
		}).flatten(1);
	}
	
	expandValues { |obj|
		^obj.setting.flatten(1);
	}
	
	objFromExpandValues { |values|
		^this.default.copy.setting_( values );
	}
}
	