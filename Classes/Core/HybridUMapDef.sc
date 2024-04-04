/*
    Unit Library
    The Game Of Life Foundation. http://gameoflife.nl
    Copyright 2006-2017 Miguel Negrao, Wouter Snoei.

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

HybridUMapDef : MultiUMapDef {

	*new { |name, ugenFunc, func, category, addToAll = true, extraPrefix|
		^super.basicNew( name, [
			ArgSpec( this.defNameKey, \dynamic, ListSpec( [ \dynamic, \value ]), true, \nonsynth )
		], category, addToAll )
			.udefs_( this.makeUdefs( name, ugenFunc, func, extraPrefix ) )
			.allowedModes_( [ \init, \sync, \normal ] )
			.chooseFunc_({ |unit, args|
				if( unit.unitArgMode != \init ) {
					\dynamic;
				} {
					\value;
				};
			});
	}

	*makeUdefs { |name, ugenFunc, func, extraPrefix|
		var dynamic;
		if( extraPrefix.notNil ) { name = extraPrefix ++ "_" ++ (name ? "") };
		dynamic = UMapDef( \dynamic, ugenFunc, extraPrefix: name ++ "_", addToAll: false );
		^[
			dynamic,
			FuncUMapDef( \value, func, addToAll: false )
				.useMappedArgs_( dynamic.useMappedArgs )
		]
	}

	numChannels { ^udefs[0].numChannels }
	numChannels_ { |newNumChannels| udefs.do(_.numChannels_( newNumChannels ) ) }

	valueIsMapped { ^udefs[1].valueIsMapped }
	valueIsMapped_ { |bool| udefs[1].valueIsMapped_( bool ) }

	mappedArgs { ^udefs[0].mappedArgs }
	mappedArgs_ { |array| udefs.do(_.mappedArgs_(array)) }

	canInsert { ^udefs[0].canInsert }
	canInsert_ { |bool| udefs.do(_.canInsert_(bool)) }

	insertArgName { ^udefs[0].insertArgName }
	insertArgName_ { |name| udefs.do(_.insertArgName_(name)) }
}