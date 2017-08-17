/*
\expand

Creates an UMap that expands certain multi-value args into single values. This UMap patches it stored values and UMaps directly to the correct inputs. Use it for example to control the x and y values of a Point separately, or to make EQ's change over time.

Argnames depend on the type of Spec. The following Specs have dedicated argnames:

RangeSpec:
lo: low / first value
hi: high / second value

PointSpec:
x: x value (default range -200m to 200m)
y: y value (default range -200m to 200m)

EQSpec:
<band_name>_<param_name>: an arg with the correct spec for the correct parameter
... etc

ArraySpec:
value<n>: the value at index <n>

all arg ranges are mapped to that of the parameter to which the UMap is connected.
This UMapDef replaces the former 'lo_hi' and 'x_y' UMapDefs, which are now automatically re-routed to 'expand'.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

ExpandUMapDef( \expand, { |unit|
	var specs;
	specs = unit.spec.expandArgSpecs;
	if( unit.unitArgMode.notNil ) {
		specs.do({ |spec| spec.mode = unit.unitArgMode })
	};
	specs;
}, \map )
	.guiColor_( Color.white.alpha_(0.5) )
	.canUseUMapFunc_({ |unit, key, umapdef|
		unit.getSpec( key ).respondsTo( \expandArgSpecs );
	});