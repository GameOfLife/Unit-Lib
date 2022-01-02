MultiMonoUdef : Udef {

	classvar <>channelArgSpec;

	*initClass {
		channelArgSpec = ArgSpec( \numChannels, 1,
			ListSpec([1,2,3,4,5,6,7,8,10,12,16,24,32]), false, \nonsynth );
	}

	initArgs {
		argSpecs = argSpecs.collect({ |item|
			if( item.name.asString[..1].asSymbol == 'u_' ) {
				item.private = true;
			};
			if( item.spec.notNil ) {
				if( item.default.class != item.spec.default.class ) {
					item.default = item.spec.constrain( item.default );
				};
			};
			if( item.name != \u_index ) { item };
		}).select( _.notNil ) ++ [ channelArgSpec ]
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

	argSpecs { |unit, numChannels = 1|
		var ioArgNames;
		numChannels = unit !? { unit.get( \numChannels ) } ? numChannels;
		ioArgNames = this.class.getAllIOArgNames( numChannels );
		^argSpecs.select({ |item|
			if( this.class.isIOName( item.name ) ) {
				ioArgNames.includes( item.name );
			} {
				true
			};
		})
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
		if( keyValuePairs.includes( \numChannels ) ) {
			unit.init( this, unit.args );
		} {
			this.prSetSynth( unit.synths, *keyValuePairs );
		};
	}

	createSynth { |unit, target, startPos = 0| // create A single synth based on server
		var group, numChannels;
		target = target ? Server.default;
		numChannels =  unit.get( \numChannels );
		if ( numChannels > 1 ) {
			group = Group( target, \addToTail );
			(numChannels-1).asInteger.do({ |i|
				Synth( this.synthDefName, unit.getArgsFor( target, startPos ) ++ [ \u_index, i+1 ], group, \addToTail );
			});
			group.freeAction_({ |synth| unit.removeSynth( synth ); });
			unit.synths = unit.synths.add( group );
		};
		^Synth( this.synthDefName, unit.getArgsFor( target, startPos ), target, \addToTail );
	}
}