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

	*checkEnv { |args|
		^if(args[0].isKindOf(Env)){
			args[0]
		} {
			Env(*args)
		}
	}

	*ar { |...args|
		var env = this.checkEnv(args);
		var start = \u_startPos.kr(0.0);
		var dur = env.times.sum;
		var phasor = Line.ar(start, dur, (dur-start).max(0.0) );
		^IEnvGen.ar(env, phasor)
	}

	*kr { |...args|
		var env = this.checkArgs(args);
		var start = \u_startPos.kr(0.0);
		var dur = env.times.sum;
		var phasor = Line.kr(start, dur, (dur-start).max(0.0) );
		^IEnvGen.kr(env, phasor)
	}

}

UXLine {

	*makeControl {  |start, end, argName|
		^argName !? {
			Udef.addBuildSpec(ArgSpec(argName, [start,end], RangeSpec(start, end)));
			argName.kr([start,end])
		} ?? { [start, end] }
	}

	*kr{ |start, end, time, argName|
		#start, end = this.makeControl(start, end, argName);
		^UEnvGen.kr([0,1],[time],\lin).linexp(0.0,1.0,start,end)
	}

	*ar{ |start, end, time, argName|
		#start, end = this.makeControl(start, end, argName);
		^UEnvGen.ar([0,1],[time],\lin).linexp(0.0,1.0,start,end)
	}

}

ULine {

	*kr{ |start=0.0, end=1.0, time, argName = \uline|
		#start, end = UXLine.makeControl(start, end, argName);
		^UEnvGen.kr([0,1],[time],\lin).linlin(0.0,1.0,start,end)
	}

	*ar{ |start, end, time, argName = \uline|
		#start, end = UXLine.makeControl(start, end, argName);
		^UEnvGen.ar([0,1],[time],\lin).linlin(0.0,1.0,start,end)
	}

}

UEnvGenRel {

	*startDurationEnv{ |vals, timesRel, int, scalingFactor = 1.0|
		var timesRel2 = timesRel / timesRel.sum;
		var start = \u_startPos.kr(0.0);
		var duration = \u_dur.kr(1.0)+start;
		var env = Env(vals, timesRel2*duration*scalingFactor, int);
		^[start, duration, env]
	}

	*ar{ |vals, timesRel, int = \lin, scalingFactor = 1.0|
		var start, duration, env, phasor;
		#start, duration, env = this.startDurationEnv(vals, timesRel, int, scalingFactor);
		phasor = Line.ar(start, duration, (duration-start).max(0.0) );
		^IEnvGen.ar(env, phasor)
	}

	*kr{ |vals, timesRel, int = \lin, scalingFactor = 1.0|
		var start, duration, env, phasor;
		#start, duration, env = this.startDurationEnv(vals, timesRel, int, scalingFactor);
		phasor = Line.kr(start, duration, (duration-start).max(0.0) );
		^IEnvGen.kr(env, phasor)
	}

}

UXLineRel {

	*kr{ |start, end, argName, scalingFactor = 1.0|
		#start, end = UXLine.makeControl(start, end, argName);
		^UEnvGenRel.kr([0,1],[1],\lin, scalingFactor).linexp(0.0,1.0,start,end)
	}

	*ar{ |start, end, argName, scalingFactor = 1.0|
		#start, end = UXLine.makeControl(start, end, argName);
		^UEnvGenRel.ar([0,1],[1],\lin, scalingFactor).linexp(0.0,1.0,start,end)
	}

}

ULineRel {

	*kr{ |start, end, argName, scalingFactor = 1.0|
		#start, end = UXLine.makeControl(start, end, argName);
		^UEnvGenRel.kr([0,1],[1],\lin, scalingFactor).linlin(0.0,1.0,start,end)
	}

	*ar{ |start, end, argName, scalingFactor = 1.0|
		#start, end = UXLine.makeControl(start, end, argName);
		^UEnvGenRel.ar([0,1],[1],\lin, scalingFactor).linlin(0.0,1.0,start,end)
	}

}

