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

/*
f = { |a=1, b=2| a+b };

x = ArgSpec.fromFunc( f ); // array of ArgSpecs

x.postln; // -> [ an ArgSpec(a, 1), an ArgSpec(b, 2) ]

*/


ArgSpec : Spec {
	var <>name, <>default, <>spec;
	var >private = false;
	var <>mode = \sync; // \sync, \normal, \init, \nonsynth
	var <>width = 1;
	var >label;

	classvar <>specs;

	*initClass {
		specs = IdentityDictionary[
			('verbosity' -> ControlSpec(0, 10, 'linear', 1, 0, "V")),
			('db' -> ControlSpec(-inf, 0.0, 'db', 0.0, -inf, "dB")),
			('widefreq' -> ControlSpec(0.1, 20000, 'exp', 0, 440, "Hz")),
			('midinote' -> ControlSpec(0, 127, 'linear', 0.0, 60, "")),
			('lofreq' -> ControlSpec(0.1, 100, 'exp', 0, 6, "Hz")),
			('controlbus' -> ControlSpec(0, 16383, 'linear', 1, 0, "")),
			('rate' -> ControlSpec(0.125, 8, 'exp', 0, 1, "")),
			('rq' -> ControlSpec(0.001, 2, 'exp', 0, 0.707, "")),
			('midfreq' -> ControlSpec(25, 4200, 'exp', 0, 440, "Hz")),
			('midivelocity' -> ControlSpec(1, 127, 'linear', 0.0, 64, "")),
			('detune' -> ControlSpec(-20, 20, 'linear', 0.0, 0, "Hz")),
			('beats' -> ControlSpec(0, 20, 'linear', 0.0, 0, "Hz")),
			('unipolar' -> ControlSpec(0, 1, 'linear', 0.0, 0, "")),
			('phase' -> ControlSpec(0, 6.2831853071796, 'linear', 0.0, 0, "")),
			('delay' -> ControlSpec(0.0001, 1, 'exp', 0, 0.3, "sec")),
			('amp' -> ControlSpec(0, 1, 'amp', 0, 0, "")),
			('pan' -> ControlSpec(-1, 1, 'linear', 0.0, 0, "")),
			('midi' -> ControlSpec(0, 127, 'linear', 0.0, 64, "")),
			('fadePx' -> ControlSpec(0, 100, 'amp', 0, 0.02, "")),
			('audiobus' -> ControlSpec(0, 1023, 'linear', 1, 0, "")),
			('freq' -> FreqSpec()), ('ampx4' -> ControlSpec(0, 4, 'amp', 0.0, 0, "")),
			('boostcut' -> ControlSpec(-20, 20, 'linear', 0.0, 0, "dB")),
			('bipolar' -> ControlSpec(-1, 1, 'linear', 0.0, 0, ""))
		];
	}

	*new { |name, default, spec, private, mode, width = 1|
		^super.newCopyArgs( name.asSymbol, default, spec, private ? false, mode ? \sync,
			width ? 1 ).init;
	}

	init {
		var specName;
		if( spec.class == Symbol ) {
			specName = this.class.specs.keys.detect({ |key|
				spec.matchOSCAddressPattern( key );
			});
			if( specName.notNil ) {
				spec = this.class.specs[ specName ];
			} {
				spec = nil;
			};
		};
		if( spec.isNil ) {
			specName = this.class.specs.keys.detect({ |key|
				name.matchOSCAddressPattern( key );
			});
			if( specName.notNil ) {
				spec = this.class.specs[ specName ];
			} {
				spec = Spec.forObject( default );
			};
		};
		if( spec.notNil ) { default = default ? spec.default };
		//default = this.constrain( default );
	}

	doWithSpec { |selector, value ...more|
		^spec.tryPerform( selector, value, *more ) ? value;
	}

	label { ^label ? name }

	private { ^private.value( this ) }

	constrain { |value|
		^this.doWithSpec( \uconstrain, value );
	}

	constrainDefault {
		this.default = this.constrain( default );
	}

	asSynthArg { |value|
		^[]
	}

	map { |value|
		^this.doWithSpec( \map, value );
	}

	unmap { |value|
		^this.doWithSpec( \unmap, value );
	}

	makeView { |parent, bounds, label, action, resize|
		var vws;
		if( private.not ) { // no view if private
			case { label.isNil } { label = this.label }
				{ label == false } { label = nil };
			vws = spec.asSpec.makeView( parent, bounds, label, action, resize );
			if( default.notNil ) { this.setView( vws, default, false ) };
			^vws;
		} {
			^nil;
		};
	}

	setView { |view, value, active = false|
		spec.asSpec.setView( view, value, active );
	}

	mapSetView { |view, value, active = false|
		spec.asSpec.mapSetView( view, value, active );
	}

	adaptToSpec { |spec|
		var asp;
		if( spec.notNil ) {
			asp = this.copy;
			asp.spec = this.spec.adaptToSpec( spec );
			asp.default = asp.spec.map( this.default );
			^asp;
		} {
			^this;
		};
	}

	asArgSpec { ^this }

	printOn { arg stream;
		stream << "an " << this.class.name << "(" <<* [name, default]  <<")"
	}

	storeOn { arg stream;
		var spc;
		spc = ( spec !? _.findKey) ? spec;
		if( spc.class == Symbol ) {
			spc = spc.asCompileString ++ ".asSpec";
		} {
			spc = spc.asCompileString;
		};
		stream << this.class.name << "(" <<* [
			name.asCompileString,
			default.asCompileString,
			spc,
			private,
			mode.asCompileString
		]  <<")"
	}

	*fromArgsArray { |args| // creates array of ArgSpecs

		if( args.notNil && { args[0].class == Symbol } ) { // assume synth-like arg pairs
			args = args.clump(2);
		};

		^args.collect({ |item| item.asArgSpec });
	}

	*fromFunc { |func, args| // creates array of ArgSpecs
		var argNames, values, inArgNames;

		args = this.fromArgsArray( args ); // these overwrite the ones found in the func
		inArgNames = args.collect(_.name);

		argNames = (func.argNames ? #[]);
		values = func.def.prototypeFrame ? #[];

		^argNames.collect({ |key, i|
			var inArgIndex;
			inArgIndex = (inArgNames ? []).indexOf( key );
			if( inArgIndex.isNil ) {
				ArgSpec( key, values[i] );
			} {
				args[ inArgIndex ];
			};
		});

	}

	*fromSynthDef { |synthDef, args| // creates array of ArgSpecs
		var argNames, values, inArgNames;
		var allControlNames;

		args = this.fromArgsArray( args ); // these overwrite the ones found in the func
		inArgNames = args.collect(_.name);

		allControlNames = synthDef.allControlNames;
		argNames = (allControlNames.collect(_.name) ? #[]);
		values = allControlNames.collect(_.defaultValue) ? #[];

		^argNames.collect({ |key, i|
			var inArgIndex;
			inArgIndex = (inArgNames ? []).indexOf( key );
			if( inArgIndex.isNil ) {
				ArgSpec( key, values[i] );
			} {
				args[ inArgIndex ];
			};
		});

	}


}

+ Array {
	asArgSpec { ^ArgSpec(*this) }
}

