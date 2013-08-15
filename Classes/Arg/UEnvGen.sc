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

(
Udef(\test, {
	var env = UEnvGen.ar([200,400,100,200],  [2.0,2.0,2.0], \lin);
	var out = SinOsc.ar( env ) * 0.2;
	UOut.ar(0, out )
});
Udef(\test2, {
	var env = UXLine.ar(200, 400, 10, \freq);
	var out = SinOsc.ar( env ) * 0.2;
	UOut.ar(0, out )
});
Udef(\test1Rel, {
	var env = UEnvGenRel.ar([200,400,100,200],  [2.0,4.0,2.0], \lin);
	var out = SinOsc.ar( env ) * 0.2;
	UOut.ar(0, out )
});
Udef(\test2Rel, {
	var env = UXLineRel.ar(200,400, \freq);
	var out = SinOsc.ar( env ) * 0.2;
	UOut.ar(0, out )
});
Udef(\test3Rel, {
	var env = UXLineRel.ar(200,400, \freq, 0.5);
	var out = SinOsc.ar( env ) * 0.2;
	UOut.ar(0, out )
})
)

(
UScore(
UChain(3,1,10, \test2, \stereoOutput),
UChain(0,0,6, \test, \stereoOutput)
).gui
)


//every time different dur
(
UScore(
	UChain(0,0,5, \test1Rel, \stereoOutput),
	UChain(5,1,5, \test2Rel, \stereoOutput),
    UChain(15,2,5, \test3Rel, \stereoOutput)
).gui
)
*/

UEnvGen {

	*prepareEnv { |env, spec|
		env = env ?? { Env() };
		case { env.isKindOf(Env) or: env.isArray } {
			env = env.asArray;
		} { env.isKindOf(Symbol) } {
			// uncomment when EnvSpec is added
			Udef.addBuildSpec(
				ArgSpec(env, EnvM(), UEnvSpec( EnvM(), spec ), mode: \init)
			);
			env = env.ir( Env(0!32,0!31).asArray );
		};
		^[ 0, env[0], env[1], env[5,9..(env.size-1)*4].sum ] ++
			env[4..].clump(4).collect({ |item|
				[ item[1], item[2], item[3], item[0] ]
			}).flatten(1);
	}
	
	*getLineArgs { |env, timeScale = 1|
		var start, dur;
		start = \u_startPos.kr(0.0);
		timeScale = this.getTimeScale( timeScale );
		dur = env[3];
		^[ start / timeScale, dur, ((dur * timeScale)-start).max(0) ]
	}
	
	*getTimeScale { |timeScale = 1|
		if( timeScale.class == Symbol ) {
			Udef.addBuildSpec(
				ArgSpec(timeScale, 1, [0.25,4,\exp,0,1].asSpec, mode: \init)
			);
			^timeScale.ir( 1 );
		} {
			^timeScale;
		};
	}

	*ar { |env, spec, timeScale = 1|
		var phasor;
		if( env.isKindOf( Env ) && { spec.isNil } ) {
			spec = ControlSpec( env.levels.minItem, env.levels.maxItem );
			env.levels = env.levels.normalize;
		};
		env = this.prepareEnv(env, spec);
		spec = spec.asSpec;
		phasor = Line.ar( *this.getLineArgs(env, timeScale));
		^spec.map( IEnvGen.ar(env, phasor) )
	}

	*kr { |env, spec, timeScale = 1|
		var phasor;
		if( env.isKindOf( Env ) && { spec.isNil } ) {
			spec = ControlSpec( env.levels.minItem, env.levels.maxItem );
			env.levels = env.levels.normalize;
		};
		env = this.prepareEnv(env, spec);
		spec = spec.asSpec;
		phasor = Line.ar( *this.getLineArgs(env, timeScale));
		^spec.map( IEnvGen.kr(env, phasor) )
	}

}

UXLine {

	*makeControl {  |start, end, argName|
		^argName !? {
			Udef.addBuildSpec(ArgSpec(argName, [start,end], RangeSpec(start, end)));
			argName.kr([start,end])
		} ?? { [start, end] }
	}

	*kr{ |start=1.0, end=2.0, time, argName|
		#start, end = this.makeControl(start, end, argName);
		^UEnvGen.kr( Env([0,1],[time],\lin), [start, end, \exp] );
	}

	*ar{ |start=1.0, end=2.0, time, argName|
		#start, end = this.makeControl(start, end, argName);
		^UEnvGen.kr( Env([0,1],[time],\lin), [start, end, \exp] );
	}

}

ULine {

	*kr{ |start=0.0, end=1.0, time, argName|
		#start, end = UXLine.makeControl(start, end, argName);
		^UEnvGen.kr( Env([0,1],[time],\lin), [start, end, \lin] );
	}

	*ar{ |start=0.0, end=1.0, time, argName|
		#start, end = UXLine.makeControl(start, end, argName);
		^UEnvGen.ar( Env([0,1],[time],\lin), [start, end, \lin] );
	}

}

UEnvGenRel : UEnvGen {
	
	*getLineArgs { |env, timeScale = 1|
		var start, dur, envdur;
		start = \u_startPos.kr(0.0);
		dur = \u_dur.kr(1.0)+start;
		timeScale = this.getTimeScale( timeScale );
		envdur = env[3] / timeScale;
		^[ envdur * (start/dur), envdur, (dur-start).max(0) ]
	}
	
}

UXLineRel {

	*kr{ |start=1.0, end=2.0, argName, timeScale = 1.0|
		#start, end = UXLine.makeControl(start, end, argName);
		^UEnvGenRel.kr( Env([0,1],[1],\lin), [start, end, \exp], timeScale );
	}

	*ar{ |start=1.0, end=2.0, argName, timeScale = 1.0|
		#start, end = UXLine.makeControl(start, end, argName);
		^UEnvGenRel.ar( Env([0,1],[1],\lin), [start, end, \exp], timeScale );
	}

}

ULineRel {

	*kr{ |start=0.0, end=1.0, argName, timeScale = 1.0|
		#start, end = UXLine.makeControl(start, end, argName);
		^UEnvGenRel.ar( Env([0,1],[1],\lin), [start, end, \lin], timeScale );
	}

	*ar{ |start=0.0, end=1.0, argName, timeScale = 1.0|
		#start, end = UXLine.makeControl(start, end, argName);
		^UEnvGenRel.ar( Env([0,1],[1],\lin), [start, end, \lin], timeScale );
	}

}

