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

MassEditU : U { // mimicks a real U, but in fact edits multiple instances of the same

	var <units, <>argSpecs;
	var <>autoUpdate = true;
	var <>defNameKey;
	var <>subDef;

	*new { |units| // all units have to be of the same Udef
		^super.newCopyArgs.init( units );
	}

	guiColor { ^units.detect(_.respondsTo(\guiColor)) !? _.guiColor ?? { Color.clear; } }

	init { |inUnits, active = true|
		var firstDef, defs;
		var dkey, dval;
		units = inUnits.asCollection;
		defs = inUnits.collect(_.def);
		firstDef = defs[0];
		if( defs.every({ |item| item == firstDef }) ) {
			def = firstDef;
			if( def.isKindOf( MultiUdef ).not or: {
				dkey = def.defNameKey;
				dval = units[0].get( dkey );
				units.every({ |unit|
					unit.get( dkey ) == dval
				});
			}) {
				argSpecs = def.argSpecs( inUnits[0] );
			} {
				argSpecs = [ def.getArgSpec( dkey, units[0] ) ];
			};

			if( def.isKindOf( MultiUdef ) ) {
				defNameKey = def.defNameKey;
				subDef = units[0].subDef;
				if( units[1..].any({ |item| item.subDef != subDef }) ) {
					subDef = nil;
				};
			} {
				subDef = units[0].subDef;
			};

			argSpecs = argSpecs.collect({ |argSpec|
				var values, massEditSpec, value;
				values = units.collect({ |unit|
					unit.get( argSpec.name );
				});

				if( values.any(_.isUMap) ) {
					massEditSpec = MassEditUMapSpec( MassEditUMap( values ) );
				} {
					massEditSpec = argSpec.spec.massEditSpec( values );
				};
				if( massEditSpec.notNil ) {
					ArgSpec( argSpec.name, massEditSpec.default, massEditSpec,
						argSpec.private, argSpec.mode );
				} {
					nil;
				};
			}).select(_.notNil);
			args = argSpecs.collect({ |item| [ item.name, item.default ] }).flatten(1);
			if( active == true ) { this.changed( \init ); };
		} {
			"MassEditU:init - not all units are of the same Udef".warn;
		};
	}

	units_ { |inUnits, active = true|
		this.disconnect;
		this.init( inUnits, active );
	}

	getArgSpec { |name|
		name = name.asSymbol;
		^argSpecs.detect({ |item| item.name == name });
	}

	guiCollapsed { ^units.select(_.isKindOf(U) ).any(_.guiCollapsed) }
	guiCollapsed_ { |bool|
		units.select(_.isKindOf(U) ).do(_.guiCollapsed_(bool));
		this.changed( \init );
	}

	connect {
		units.do(_.addDependant(this));
	}

	disconnect {
		units.do(_.removeDependant(this));
	}

	resetArg { |key| // doesn't change the units
		var spec, values;
		if( key.notNil ) {
			spec = this.getSpec( key );
			values = units.collect({ |unit| unit.get( key ) });
			if( spec.class != MassEditUMapSpec and: { values.any(_.isUMap).not }) {
				this.setArg( key, spec.massEditValue( values ) );
			};
		} {
			this.keys.do({ |key| this.resetArg( key ) });
		};
	}

	update { |obj, what ...args|
		if( autoUpdate ) {
			if( this.keys.includes( what ) ) {
				this.resetArg( what );
			};
		}
	}

	stop { units.do(_.stop) }

	insertUMap { |key, umapdef, args|
		var wasAutoUpdate;
		wasAutoUpdate = autoUpdate;
		autoUpdate = false;
		UMapSetChecker.stall = true;
		if( umapdef.isKindOf( UMap ) ) {
			units.do({ |unit|
				unit.insertUMap( key, umapdef.deepCopy, args );
			});
		} {
			units.do({ |unit|
				unit.insertUMap( key, umapdef, args );
			});
		};
		autoUpdate = wasAutoUpdate;
		UMapSetChecker.stall = false;
		this.changed( \init );
	}

	removeUMap { |key|
		var wasAutoUpdate;
		wasAutoUpdate = autoUpdate;
		autoUpdate = false;
		UMapSetChecker.stall = true;
		units.do({ |unit|
			unit.removeUMap( key )
		});
		autoUpdate = wasAutoUpdate;
		UMapSetChecker.stall = false;
		this.changed( \init );
	}

	set { |...args|
		var autoUpdateWas, defNameWas;

		if( defNameKey.notNil ) {
			defNameWas = units.first.get( defNameKey );
		};

		// disable auto updating to prevent loop
		autoUpdateWas = autoUpdate;
		autoUpdate = false;

		args.pairsDo({ |key, value|
			var values;
			this.setArg( key, value );
			values = this.getSpec( key ).massEdit( units.collect(_.get(key) ), value );
			units.do({ |unit, i|
				unit.set( key, values[i] );
			});
		});

		// re-enable auto updating
		autoUpdate = autoUpdateWas;

		if( defNameWas.notNil && { units.any( { |unit|
				unit.get( defNameKey ) != defNameWas } )
		} ) {
			this.changed( \init );
		};
	}

	getSpec { |name|
		^units[0].getSpec( name );
	}

	canUseUMap { |key, umapdef|
		^units.first.canUseUMap( key, umapdef );
	}

	defName { ^((this.def !? { this.def.name } ? "mixed").asString + "(% units)".format( units.size )).asSymbol }

	fullDefName {
		^((this.def !? { this.def.name } ? "mixed").asString ++
		if( defNameKey.notNil && subDef.notNil ) { " /" + units[0].subDefNames.join( " / " ) } { "" } ++
		" (% units)".format( units.size ));
	}

	def_ { |def|  units.do(_.def_( def ) ); this.init( units ); }

	checkDef { units.do(_.checkDef) }

	storeArgs { ^[ units ] }

}


MassEditUChain {

	var <uchains;
	var <umarkers;
	var <units;
	var <>prepareTasks;

	*new { |uchains, umarkers|
		^super.newCopyArgs( uchains, umarkers ).init;
	}

	lockStartTime { ^uchains.any(_.lockStartTime) }
	lockStartTime_ { |bool| ^uchains.do(_.lockStartTime_(bool)) }

	init {
		var allDefNames = [], allUnits = Order();
		var multiUdefs = Set();

		uchains.do({ |uchain|
			uchain.units.select({|x| x.def.class != LocalUdef}).do({ |unit|
				var defName, index;
				defName = unit.defName;
				if( unit.def.isKindOf( MultiUdef ) ) {
					multiUdefs.add( defName );
				};
				if( allDefNames.includes( defName ).not ) {
					allDefNames = allDefNames.add( defName );
				};
				index = allDefNames.indexOf( defName );
				allUnits.put( index, allUnits[ index ].add( unit ) );
			});
		});

		units = allUnits.asArray.collect({ |item, i|
			var variants = ();
			if( allDefNames[i].notNil ) {
				if( item.size == 1 ) {
					[ item[0] ]
				} {
					if( multiUdefs.includes( allDefNames[i] ) ) {
						item.do({ |unit|
							var val;
							val = unit.subDef;
							variants[ val ] = variants[ val ].add( unit );
						});
						variants.keys.as(Array).sort({ |a,b| a.name <= b.name }).collect({ |key|
							if( variants[ key ].size == 1 ) {
								variants[ key ][ 0 ];
							} {
								MassEditU( variants[ key ] )
							};
						});
					} {
						[ MassEditU( item ) ]
					};
				};
			} {
				nil
			};
		}).select(_.notNil).flatten(1);

		this.changed( \init );
	}

	addDependantToChains { |dependant|
		uchains.do(_.addDependant(dependant));
	}

	removeDependantFromChains { |dependant|
		uchains.do(_.removeDependant(dependant));
	}

	connect {
		uchains.do(_.addDependant(this));
	}

	disconnect {
		uchains.do(_.removeDependant(this));
	}

	update { |obj, what ...args|
		this.changed( what, *args );
	}

	groups { ^uchains.collect(_.groups).flatten(1); } // don't know any groups

	releaseSelf { ^uchains.collect(_.releaseSelf).every(_==true); }
	releaseSelf_ { |bool|
		uchains.do(_.releaseSelf_(bool));
	}

	global { ^uchains.collect(_.global).every(_==true); }
	global_ { |bool|
		uchains.do(_.global_(bool));
	}

	addAction {
		var actions = uchains.collect(_.addAction);
		var first = actions.first;
		if( actions.every(_ === first) ) {
			^first;
		} {
			^\mixed;
		};
	}
	addAction_ { |symbol|
		if( symbol != \mixed ) { uchains.do(_.addAction_(symbol)); };
	}

	ugroup {
		var first;
		first = uchains.first.ugroup;
		if( uchains[1..].any({ |item| item.ugroup != first }) ) { ^\mixed } { ^first };
	}

	ugroup_ { |ugroup|
		if( ugroup !== \mixed ) {
			uchains.do(_.ugroup_( ugroup ));
		};
	}

	uchainsOrUMarkers { if( uchains.size > 0 ) { ^uchains } { ^umarkers } }

	canSetColorIndices {
		^this.uchainsOrUMarkers.collect({ |item|
			var res = item.getTypeColor;
			if( res.respondsTo( \asColor ) ) { res.asColor } { nil }
		}).selectIndices( _.isKindOf( Color ) );
	}

	getTypeColor {
		var allColors;
		allColors = this.uchainsOrUMarkers[ this.canSetColorIndices ].collect({ |x|
			x.getTypeColor.asColor;
		});
		if( allColors.size == 0 ) {
			^Color.gray;
		} {
			^Color( *allColors.collect(_.asArray).mean );
		};
	}

	displayColor {
		^if( this.uchainsOrUMarkers.any({ |item| item.displayColor != nil }) ) {
			this.getTypeColor
		};
	}

	displayColor_ { |color|
		this.uchainsOrUMarkers.do({ |item| item.displayColor = color });
		this.changed( \displayColor, color );
	}

	getTypeColors {
		^this.uchainsOrUMarkers[ this.canSetColorIndices ].collect({ |x|
			x.getTypeColor.asColor;
		});
	}

	setDisplayColors { |colors|
		this.uchainsOrUMarkers[ this.canSetColorIndices ]
		.do({ |item, i|
			if( item.displayColor.isKindOf( USoundFileOverview ) ) {
				item.displayColor = item.displayColor.color_( colors.wrapAt(i) );
			} {
				item.displayColor = colors.wrapAt(i);
			}
		});
	}

	fadeIn_ { |fadeIn = 0|
		var add = fadeIn - this.fadeInTime;

		uchains.do({ |item|
			item.fadeIn_( item.fadeInTime + add );
		});
	}

	fadeOut_ { |fadeOut = 0|
		var add = fadeOut - this.fadeOutTime;

		uchains.do({ |item|
			item.fadeOut_( item.fadeOutTime + add );
		});
	}

	fadeOut {
		^uchains.collect({ |item| item.fadeOutTime }).maxItem ? 0;
	}

	fadeIn {
		^uchains.collect({ |item| item.fadeInTime }).maxItem ? 0;
	}

	fadeOutTime {
		^uchains.collect({ |item| item.fadeOutTime }).maxItem ? 0;
	}

	fadeInTime {
		^uchains.collect({ |item| item.fadeInTime }).maxItem ? 0;
	}

	fadeInCurve_ { |curve = 0|
		uchains.do({ |item|
			item.fadeInCurve_( curve );
		});
	}

	fadeOutCurve_ { |curve = 0|
		uchains.do({ |item|
			item.fadeOutCurve_( curve );
		});
	}

	fadeOutCurve {
		^uchains.collect({ |item| item.fadeOutCurve }).maxItem ? 0;
	}

	fadeInCurve {
		^uchains.collect({ |item| item.fadeInCurve }).maxItem ? 0;
	}

	useSndFileDur { // look for SndFiles in all units, use the longest duration found
		var durs;
		uchains.do(_.useSndFileDur);
	}

	getMaxDurChain { // get unit with longest non-inf duration
		var dur, out;
		uchains.do({ |uchain|
			var u_dur;
			u_dur = uchain.dur;
			if( (u_dur > (dur ? 0)) && { u_dur != inf } ) {
				dur = u_dur;
				out = uchain;
			};
		});
		^out;
	}

	dur { // get longest duration
		var uchain;
		uchain = this.getMaxDurChain;
		if( uchain.isNil ) {
			^inf
		} {
			^uchain.dur;
		};
	}

    /*
	* sets same duration for all units
	* clipFadeIn = true clips fadeIn
	* clipFadeIn = false clips fadeOut
	*/
	dur_ { |dur = inf, clipFadeIn = true|
		var currentDur, mul;
		currentDur = this.dur;
		if( (currentDur != inf) && { dur != inf } ) {
			mul = dur.max(1.0e-11) / currentDur.max(1.0e-11);
			uchains.do({ |uchain|
				if( uchain.dur != inf ) {
					uchain.dur_( uchain.dur * mul, clipFadeIn );
				};
			});
		} {
		    uchains.do(_.dur_( dur ))
		};
		this.changed(\dur)
	}

	duration { ^this.dur }
	duration_ { |x| this.dur_(x)}

	muted { ^uchains.collect({ |ch| ch.muted.binaryValue }).mean > 0.5 }
	muted_ { |bool|
		uchains.do({ |ch| ch.muted = bool });
		this.changed( \muted );
	}

	startTime {
		^(uchains.collect({ |ch| ch.startTime ? 0 }) ++
		umarkers.collect({ |ch| ch.startTime ? 0 })).minItem;
	}

	startTime_ { |newTime|
		var oldStartTime, delta;
		oldStartTime = this.startTime;
		if( newTime.notNil ) {
			delta = newTime - oldStartTime;
		} {
			delta = 0;
		};
		if( delta != 0 ) {
			uchains.do({ |ch|
				ch.startTime = (ch.startTime ? 0) + delta;
			});
			umarkers.do({ |ch|
				ch.startTime = (ch.startTime ? 0) + delta;
			});
		};
	}

	setGain { |gain = 0| // set the average gain of all units that have a u_gain arg
		var mean, add;
		mean = this.getGain;
		add = gain - mean;
		uchains.do({ |uchain|
			 uchain.setGain( uchain.getGain + add );
		});
		this.changed( \gain );
	}

	getGain {
		var gains;
		gains = this.uchains.collect(_.getGain);
		if( gains.size > 0 ) { ^gains.mean } { ^0 };
	}

	autoPause { ^umarkers.collect(_.autoPause) }
	autoPause_ { |newAutoPause|
		newAutoPause = newAutoPause.asCollection.wrapExtend( umarkers.size );
		newAutoPause.do({ |bool, i|
			umarkers[i].autoPause = bool;
		});
		this.changed( \autoPause );
	}


	start { |target, latency|
		^uchains.collect( _.start( target, latency ) );
	}

	free { uchains.do(_.free); }
	stop { uchains.do(_.stop); }

	release { |time|
		uchains.do( _.release( time ) );
	}

	prepare { |target, startPos = 0, action|
		var firstAction;
		action = MultiActionFunc( action );
		firstAction = action.getAction;
	     uchains.do( _.prepare(target, startPos, action.getAction ) );
	     firstAction.value; // fire action at least once
	     ^target; // return array of actually prepared servers
	}

	prepareAndStart{ |target, startPos = 0|
		var task, cond;
		cond = Condition(false);
		task = fork {
			var action;
			action = { cond.test = true; cond.signal };
			target = this.prepare( target, startPos, action );
			cond.wait;
	       	this.start(target);
		};
	}

	waitTime { ^this.units.collect(_.waitTime).sum }

	prepareWaitAndStart { |target, startPos = 0|
		var task;
		task = fork {
			this.prepare( target, startPos );
			this.waitTime.wait; // doesn't care if prepare is done
	       	this.start(target);
	       	prepareTasks.remove(task);
		};
	}

	dispose { uchains.do( _.dispose ) }

	// indexing / access

	at { |index| ^units[ index ] }

	last { ^units.last }
	first { ^units.first }

	unitMatch { |index = 0, unit|
		^if( units[ index ].isKindOf( MassEditU ) ) {
			units[ index ].units.includes( unit );
		} {
			units[ index ] === unit;
		};
	}

	unitSize { |index = 0|
		^if(  units[ index ].isKindOf( MassEditU ) ) {
			units[ index ].units.size;
		} {
			1
		};
	}

	findInsertChains { |index = 0|
		var chains;
		if( index == 0 or: { index >= (units.size) }) {
			^nil
		};
		if( this.unitSize( index ) != this.unitSize( index - 1 ) ) {
			^nil;
		};
		uchains.do({ |chain|
			var i1, i2;
			i1 = chain.units.detectIndex({ |unit|
				this.unitMatch( index-1, unit );
			});
			if( i1.notNil ) {
				i2 = chain.units.detectIndex({ |unit|
					this.unitMatch( index, unit );
				});
				if( (i1 + 1) == i2) {
					chains = chains.add( [ chain, i2 ] );
				};
			};
		});
		if( chains.size == this.unitSize( index ) ) {
			^chains; // array of chains and indices to insert
		} {
			^nil;
		};
	}

	canInsertAt { |index = 0|
		^this.findInsertChains( index ).notNil;
	}

	insert { |index = 0, what, args|
		this.findInsertChains( index ).do({ |item|
			item[0].insert( item[1], what.asUnit( args ), false );
		});
		this.changed( \units );
	}

	findRemoveChains { |index = 0|
		var chains, sizes;
		if( index == 0 or: { index >= (units.size-1) }) {
			^nil
		};
		sizes = [-1,0,1].collect({ |item|
			this.unitSize( index + item )
		});
		if( sizes.any( _ != sizes[0] ) ) {
			^nil;
		};
		uchains.do({ |chain|
			var i1, i2, i3;
			#i1, i2, i3 = [-1,0,1].collect({ |item|
				chain.units.detectIndex({ |unit|
					this.unitMatch( index+item, unit );
				});
			});
			if( i2.notNil && { i2 + 1 == i3 && { i2 - 1 == i1 } }) {
				chains = chains.add( [ chain, i2 ] );
			};
		});
		if( chains.size == sizes[1]) {
			^chains; // array of chains and indices to remove
		} {
			^nil;
		};
	}

	canRemoveAt { |index = 0|
		^this.findRemoveChains( index ).notNil;
	}

	removeAt { |index = 0|
		this.findRemoveChains( index ).do({ |item|
			item[0].removeAt( item[1], false );
		});
		this.changed( \units );
	}

	printOn { arg stream;
		stream << "a " << this.class.name << "(" <<* units.collect(_.defName)  <<")"
	}

	gui { |parent, bounds, score| ^UChainGUI( parent, bounds, this, score ) }

}