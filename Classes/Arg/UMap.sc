UMap {
	
	// This class is under development. For now it plays a line between min and max.
	// it can only be used for args that have a single value ControlSpec
	// gui doesn't work yet
	
	/* 
	example:
	x = UChain([ 'sine', [ 'freq', UMap(0.5,0.75,5) ] ], 'output');
	x.prepareAndStart;
	x.stop;
	*/
	
	classvar <>allUnits;
	
	var <min = 0.5, <max = 1, <time = 5;
	var <>spec;
	var <>synths;
	var <>bus = 0;
	
	*busOffset { ^1000 }
	
	*initClass { 
	    allUnits = IdentityDictionary();
	}
	
	*new { |min = 0.5, max = 1, time = 5|
		^super.newCopyArgs( min, max, time );
	}
	
	asControlInput {
		"called %.asControlInput\n".postf( this.cs );
		//^spec.asSpec.map( value.value );
		^("c" ++ (bus + this.class.busOffset)).asSymbol;
	}
	
	u_waitTime { ^1 }
	
	synthDef {
		^SynthDef( "umap_line", {
			ReplaceOut.kr( \bus.kr(bus + this.class.busOffset), spec.asSpec.map( 
				Line.kr( \min.kr(min),\max.kr(max), \time.kr(time) )
				)
			);
		});
	}
	
	prepare { |servers, startPos = 0, action|
		action = MultiActionFunc( action );
		servers.do({ |server|
			var innerAction = action.getAction;
			this.synthDef.send(server);
			OSCresponderNode( server.addr, '/done', { |time, resp, msg, addr|
				if( msg == [ '/done', '/d_recv' ]  ) {
					resp.remove;
					innerAction.value;
				};
			}).add;
	     });
		"called %.prepare( % )\n".postf( this.cs, [servers, startPos, action ]
			.join( ", " ) 
		);
	}
	
	min_ { |new = 0|
		min = new;
		this.changed( \min, min );
		this.unitSet;
	}
	
	max_ { |new = 0|
		max = new;
		this.changed( \max, max );
		this.unitSet;
	}
	
	time_ { |new = 0|
		time = new;
		this.changed( \time, time );
		this.unitSet;
	}
	
	start { |targets, startPos, latency|
		var bundles;
		bundles = this.unit.synths.collect({ |synth|
			synth.server.makeBundle( false, {
				Synth.before( synth, "umap_line", [
					\min, min.blend(max,startPos/time), 
					\max, max, 
					\time, time - startPos, 
					\bus, bus + this.class.busOffset 
				] );
			});
		});
		this.unit.synths.do({ |synth, i|
			synth.server.sendSyncedBundle( latency, nil, *bundles[i] );
		});
		"called %.start( %, %, % )\n".postf( this.cs, targets, startPos, latency);
	}
	
	dispose {
		"called %.dispose\n".postf( this.cs );
	}
	
	disposeFor { |server|
		"called %.disposeFor(%)\n".postf( this.cs, server );
	}
	
	asUnitArg { |unit, key|
		this.unit = unit; 
		this.unitArgName = key;
		if( key.notNil ) {
			spec = unit.getSpec( key ).copy;
		};
		^this;
	}
	
	unit_ { |aUnit|
		if( aUnit.notNil ) {
			case { this.unit == aUnit } {
				// do nothing
			} { allUnits[ this ].isNil } {
				allUnits[ this ] = [ aUnit, nil ];
			} {
				"Warning: unit_ \n%\nis already being used by\n%\n".postf(
					this.class,
					this.asCompileString, 
					this.unit 
				);
			};
		} {
			allUnits[ this ] = nil; // forget unit
		};
	}
	
	unit { ^allUnits[ this ] !? { allUnits[ this ][0] }; }
	
	unitArgName {  
		var array;
		^allUnits[ this ] !? { 
			allUnits[ this ][1] ?? {
				array = allUnits[ this ];
				array[1] = array[0].findKeyForValue( this );
				array[1];
			};
		}; 
	}
	
	unitArgName_ { |unitArgName|
		if( allUnits[ this ].notNil ) {
			allUnits[ this ][1] = unitArgName;
		} {
			"Warning: unitArgName_ - no unit specified for\n%\n"
				.postf( this.asCompileString )
		};
	}
	
	unitSet { // sets this object in the unit to enforce setting of the synths
		var unitArgName;
		if( this.unit.notNil ) {	
			unitArgName = this.unitArgName;
			if( unitArgName.notNil ) {
				this.unit.set( unitArgName, this );
			};
		};
	}
	
	storeArgs {
		^[ min, max, time ]
	}
}