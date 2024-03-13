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

MultiUMapDef : UMapDef {

	var <>udefs;
	var <>chooseFunc;
	var >defNameKey;
	var tempDef;
	var <defaultDefName;

	*defNameKey { ^\u_defName }
	defNameKey { ^defNameKey ? this.class.defNameKey }

	defType {
		var first;
		first = udefs.first.defType;
		^if( udefs.every({ |def| def.defType == first }) ) {
			first;
		} {
			\mixed
		};
	}

	guiColor { ^guiColor ?? {
			if( this.defType === \mixed ) {
				this.class.defaultGUIColor
			} {
				udefs.first.guiColor;
			}
		}
	}

	*defaultGUIColor { ^Color.white.alpha_(0.33) }

	*new { |name, udefs, category, setter, setterIsPrivate = true, addToAll = true| // first udef in list is default
		^super.basicNew( name, [
			ArgSpec( setter ? this.defNameKey,
				udefs[0].name, ListSpec( udefs.collect(_.name) ), setterIsPrivate, \nonsynth )
		], category, addToAll )
			.defNameKey_( setter )
			.useMappedArgs_( udefs.first.useMappedArgs )
			.udefs_( udefs );
	}

	defaultDefName_ { |name|
		defaultDefName = name;
		this.setDefault( defNameKey, defaultDefName );
	}

	useMappedArgs_ { |bool| useMappedArgs = bool }

	findUdef{ |name|
		^udefs.detect({ |item| item.name == name }) ? udefs[0];
	}

	dontStoreArgNames {
		^dontStoreArgNames ?? {
			if( this.getArgSpec( this.defNameKey ).private ) {
				[ this.defNameKey ]
			}
		};
	}

	createsSynth { ^udefs.any(_.createsSynth) }

	findUdefFor { |unit|
		^tempDef ?? { this.findUdef( unit.get( this.defNameKey ) ); };
	}

	asArgsArray { |argPairs, unit, constrain = true|
		var defName, argz, newDefName;
		argPairs = prepareArgsFunc.value( argPairs ) ? argPairs;
		defName = (argPairs ? []).detectIndex({ |item| item == this.defNameKey });
		if( defName.notNil ) {
			defName = argPairs[ defName + 1 ];
		} {
			defName = defaultDefName ?? { udefs[0].name; };
		};
		tempDef = this.findUdef( defName );
		argz = tempDef.asArgsArray( argPairs ? [], unit, constrain );
		if( chooseFunc.notNil ) {
			newDefName = chooseFunc.value( unit, argz );
			if( newDefName != defName ) { // second pass
				defName = newDefName;
				tempDef = this.findUdef( defName );
				argz = tempDef.asArgsArray( argPairs ? [], unit, constrain );
			};
		};
		tempDef = nil;
		^argz ++ [ this.defNameKey, defName ];
	}

	args { |unit|
		^(this.findUdefFor( unit ).args( unit ) ? []) ++
			argSpecs.collect({ |item| [ item.name, item.default ] }).flatten(1)
	}

	argNamesFor { |unit|
		^(this.findUdefFor( unit ).argNamesFor( unit ) ? []) ++ this.argNames;
	}

	synthDef { ^udefs.collect(_.synthDef).flat }

	createSynth { |unit, target, startPos|
		^this.findUdefFor( unit ).createSynth( unit, target, startPos );
	}

	prIOids { |mode = \in, rate = \audio, unit|
		^this.findUdefFor( unit ).prIOids( mode, rate, unit );
	}

	inputIsEndPoint { |unit|
		^this.findUdefFor( unit ).inputIsEndPoint( unit );
	}

	prepare { |servers, unit, action, startPos|
 		^this.findUdefFor( unit ).prepare( servers, unit, action, startPos )
 	}

 	doPrepareFunc { |servers, unit, action, startPos|
 		^this.findUdefFor( unit ).doPrepareFunc( servers, unit, action, startPos )
 	}

	canFreeSynth { |unit| ^this.findUdefFor( unit ).canFreeSynth( unit ) }

	getNext { |unit|
		^this.findUdefFor( unit ).getNext( unit );
	}

	getControlInput { |unit|
		^this.findUdefFor( unit ).getControlInput( unit );
	}

	chooseDef { |unit|
		var currentDefName, newDefName;
		if( chooseFunc.notNil ) {
			currentDefName = unit.get( this.defNameKey );
			newDefName = chooseFunc.value( unit, unit.args );
			if( currentDefName != newDefName ) {
				unit.setArg( this.defNameKey, newDefName );
				unit.init( unit.def, unit.args );
				unit.constrainArgs;
			};
		};
	}

	allowedModes { |unit|
		if( unit.notNil ) {
			^this.findUdefFor( unit ).allowedModes( unit )
		} {
			^allowedModes
		};
	}

	hasBus { |unit| ^this.findUdefFor( unit ).hasBus( unit ) }

	shouldPlayOn { |unit, server| // returns nil if no func
		^shouldPlayOnFunc !?
		{ shouldPlayOnFunc.value( unit, server ); } ??
		{ this.findUdefFor( unit ).shouldPlayOn( unit, server ) };
	}

	setSynth { |unit ...keyValuePairs|
		this.chooseDef( unit ); // change def based on chooseFunc if needed
		if( keyValuePairs.includes( this.defNameKey ) ) {
			unit.init( unit.def, unit.args );
			unit.constrainArgs;
		} {
			^this.findUdefFor( unit ).setSynth( unit, *keyValuePairs );
		};
	}

	makeSynth { |unit, target, startPos = 0, synthAction|
		^this.findUdefFor( unit ).makeSynth( unit, target, startPos, synthAction );
	}

	getArgSpec { |key, unit|
		if( key === this.defNameKey ) {
			^argSpecs[0];
		} {
			^this.findUdefFor( unit ).getArgSpec( key, unit );
		};
	}

	getSpec { |key, unit|
		if( key === this.defNameKey ) {
			^argSpecs[0].spec;
		} {
			^this.findUdefFor( unit ).getSpec( key, unit );
		};
	}

	getDefault { |name, unit|
		var asp;
		asp = this.getArgSpec(name, unit);
		if( asp.notNil ) { ^asp.default } { ^nil };
	}

	getValue { |unit|
		^this.findUdefFor( unit ).getValue( unit );
	}

	makeStream { |unit|
		unit.subDef.makeStream( unit );
	}

	needsStream { ^udefs.first.needsStream }

	useMappedArgs { ^udefs.first.useMappedArgs }

	findUdefsWithArgName { |key|
		if( key === this.defNameKey ) {
			^[ this ];
		} {
			^udefs.select({ |udef|
				udef.argNames.includes( key );
			});
		};
	}

	setSpec { |name, spec, mode, constrainDefault = true| // set the spec for all enclosed udefs
		this.findUdefsWithArgName( name ).do({ |item|
			item.setSpec( name, spec, mode, constrainDefault );
		});
	}

	setSpecMode { |...pairs|
		pairs.pairsDo({ |name, mode|
			this.findUdefsWithArgName( name ).do({ |item|
				item.setSpecMode( name, mode );
			});
		});
	}

	setArgSpec { |argSpec|
		argSpec = argSpec.asArgSpec;
		this.findUdefsWithArgName( argSpec.name ).do({ |item|
			item.setArgSpec( argSpec );
		});
	}

	argSpecs { |unit|
		^this.findUdefFor( unit ).argSpecs( unit ) ++ argSpecs;
	}

	performUpdate { |unit ...args|
		this.findUdefFor( unit ).performUpdate( unit, *args );
	}

}