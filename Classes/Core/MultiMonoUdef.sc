MultiMonoUdef : Udef {

	classvar <>channelArgSpec;

	*initClass {
		channelArgSpec = ArgSpec( \numChannels, 1,
			ListSpec([1,2,3,4,5,6,7,8,10,12,16,24,32]), false, \nonsynth );
	}

	initArgs {
		argSpecs.do({ |item|
			if( item.name.asString[..1].asSymbol == 'u_' ) {
				item.private = true;
			};
			if( item.spec.notNil ) {
				if( item.default.class != item.spec.default.class ) {
					item.default = item.spec.constrain( item.default );
				};
			};
		});
		argSpecs = argSpecs.add( channelArgSpec );
	}

	*getIOArgNames { |numChannels = 1, type = 'i', selector = 'ar'|
		^numChannels.collect({ |i|
			UIn.getControlName( type, selector, i, "bus" );
		})
	}

	*getAllIOArgNames { |numChannels = 1|
		^numChannels.collect({ |i|
			[
				UIn.getControlName( 'i', 'ar', i, "bus" ),
				UIn.getControlName( 'o', 'ar', i, "bus" ),
				UIn.getControlName( 'i', 'kr', i, "bus" ),
				UIn.getControlName( 'o', 'kr', i, "bus" ),
			]
		}).flatten(1);
	}

	*isIOName { |name| ^"u_[io]_(ar|kr)_\\d{1,}_bus".matchRegexp( name.asString ) }

	*getIOIndex { |name| ^name.asString.split( $_ )[3].interpret }

	*setIOIndex { |name, val = 0|
		name = name.asString.split( $_ );
		name[3] = val;
		^name.join($_).asSymbol;
	}

	addIOArgSpecs { |numChannels = 1|
		var synthIO;
		synthIO = argSpecs.select({ |item|
			this.class.isIOName( item.name );
		});
		if( numChannels > 1 ) {
			^argSpecs ++ (numChannels - 1).collect({ |i|
				synthIO.collect({ |argSpec|
					argSpec.copy
					.name_( this.class.setIOIndex( argSpec.name, i + 1 ) )
					.default_( i+1 );
				});
			}).flatten(1);
		} {
			^argSpecs;
		};
	}

	argSpecs { |unit, numChannels = 1|
		var ioArgNames;
		numChannels = unit !? { unit.get( \numChannels ) } ? numChannels ? 1;
		^this.addIOArgSpecs( numChannels );
	}

	asArgsArray { |argPairs, unit, constrain = true|
		var numChannelsIndex, numChannels = 1;
		argPairs = prepareArgsFunc.value( argPairs ) ? argPairs ? #[];
		numChannelsIndex = argPairs.indexOf( \numChannels );
		if( numChannelsIndex.notNil ) {
			numChannels = argPairs[ numChannelsIndex + 1 ];
		};
		^this.argSpecs( unit, numChannels ).collect({ |item|
			var val;
			val = argPairs.pairsAt(item.name) ?? { item.default.copy };
			val = val.deepCopy.asUnitArg( unit, item.name );
			if( constrain && { val.isKindOf( UMap ).not } ) { val = item.constrain( val ) };
			[ item.name,  val ]
		}).flatten(1);
	}

	args { |unit|
		^this.argSpecs( unit ).collect({ |item| [ item.name, item.default ] }).flatten(1);
	}

	argNamesFor { |unit|
		^this.argSpecs( unit ).collect(_.name);
	}

	getArgSpec { |name, unit|
		name = name.asSymbol;
		^this.argSpecs( unit ).detect({ |item| item.name == name });
	}

	getSpec { |name, unit|
		var asp;
		asp = this.getArgSpec(name, unit);
		if( asp.notNil ) { ^asp.spec } { ^nil };
	}

	getDefault { |name, unit|
		var asp;
		asp = this.getArgSpec(name, unit);
		if( asp.notNil ) { ^asp.default } { ^nil };
	}

	setSynth { |unit ...keyValuePairs|
		var numChannels, filteredKeyValuePairs;
		if( keyValuePairs.includes( \numChannels ) ) {
			unit.init( unit.def, unit.args );
		} {
			numChannels = unit.get( \numChannels );
			if( numChannels > 1 ) {
				keyValuePairs.pairsDo({ |key, value|
					var index, newKey;
					if( this.class.isIOName( key ) ) {
						index = this.class.getIOIndex( key );
						newKey = this.class.setIOIndex( key, 0 );
						this.prSetSynth( unit.synths.collect(_[index]), newKey, value );
					} {
						filteredKeyValuePairs = filteredKeyValuePairs.addAll( [ key, value ] );
					}
				});
				if( filteredKeyValuePairs.notNil ) {
					this.prSetSynth( unit.synths, *keyValuePairs );
				};
			} {
				this.prSetSynth( unit.synths, *keyValuePairs );
			}
		};
	}

	createSynthArgsArray { |inArgs, n = 1|
		var ioArgs, otherArgs;

		inArgs.pairsDo({ |key, value|
			if( this.class.isIOName( key ) ) {
				ioArgs = ioArgs.add( [ key, value ] );
			} {
				otherArgs = otherArgs.addAll( [ key, value ] );
			};
		});

		^n.collect({ |i|
			otherArgs ++
			ioArgs.select({ |item|
				this.class.getIOIndex( item[0] ) == i
			}).collect({ |item|
				[ this.class.setIOIndex( item[0], 0 ), item[1] ]
			}).flatten(1)
		});
	}

	createSynth { |unit, target, startPos = 0| // create A single synth based on server
		var group, numChannels, argsArray;
		target = target ? Server.default;
		numChannels =  unit.get( \numChannels );
		if ( numChannels > 1 ) {
			group = GroupWithChildren( target, \addToTail );
			argsArray = this.createSynthArgsArray( unit.getArgsFor( target, startPos ), numChannels );
			numChannels.asInteger.do({ |i|
				group.addChild(
					Synth( this.synthDefName, argsArray[i], group, \addToHead )
				);
			});
			^group;
		} {
			^Synth( this.synthDefName, unit.getArgsFor( target, startPos ), target, \addToTail );
		}
	}
}