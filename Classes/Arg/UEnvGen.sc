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

	*kr{ |start, end, time, argName = \uxline|
		#start, end = argName.kr([start,end]);
		^UEnvGen.kr([0,1],[time],\lin).linexp(0.0,1.0,start,end)
	}

	*ar{ |start, end, time, argName = \uxline|
		#start, end = argName.kr([start,end]);
		^UEnvGen.ar([0,1],[time],\lin).linexp(0.0,1.0,start,end)
	}

}

ULine {

	*kr{ |start=0.0, end=1.0, time, argName = \uline|
		#start, end = argName.kr([start,end]);
		^UEnvGen.kr([0,1],[time],\lin).linlin(0.0,1.0,start,end)
	}

	*ar{ |start, end, time, argName = \uline|
		#start, end = argName.kr([start,end]);
		^UEnvGen.ar([0,1],[time],\lin).linlin(0.0,1.0,start,end)
	}

}

UEnvGenRel {

	*startDurationEnv{ |vals, timesRel, int|
		var timesRel2 = timesRel / timesRel.sum;
		var start = \u_startPos.kr(0.0);
		var duration = \u_dur.kr(1.0)+start;
		var env = Env(vals, timesRel2*duration, int);
		^[start, duration, env]
	}

	*ar{ |vals, timesRel, int = \lin|
		var start, duration, env, phasor;
		#start, duration, env = this.startDurationEnv(vals, timesRel, int);
		phasor = Line.ar(start, duration, (duration-start).max(0.0) );
		^IEnvGen.ar(env, phasor)
	}

	*kr{ |vals, timesRel, int = \lin|
		var start, duration, env, phasor;
		#start, duration, env = this.startDurationEnv(vals, timesRel, int);
		phasor = Line.kr(start, duration, (duration-start).max(0.0) );
		^IEnvGen.kr(env, phasor)
	}

}

UXLineRel {

	*kr{ |start, end, argName = \uxlineRel|
		#start, end = argName.kr([start,end]);
		^UEnvGenRel.kr([0,1],[1],\lin).linexp(0.0,1.0,start,end)
	}

	*ar{ |start, end, argName = \uxlineRel|
		#start, end = argName.kr([start,end]);
		^UEnvGenRel.ar([0,1],[1],\lin).linexp(0.0,1.0,start,end)
	}

}

ULineRel {

	*kr{ |start, end, argName = \ulineRel|
		#start, end = argName.kr([start,end]);
		^UEnvGenRel.kr([0,1],[1],\lin).linlin(0.0,1.0,start,end)
	}

	*ar{ |start, end, argName = \ulineRel|
		#start, end = argName.kr([start,end]);
		^UEnvGenRel.ar([0,1],[1],\lin).linlin(0.0,1.0,start,end)
	}

}

