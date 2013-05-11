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

	*kr{ |start, end, time|
		^UEnvGen.kr([start,end],[time],\exp)
	}

	*ar{ |start, end, time|
		^UEnvGen.ar([start,end],[time],\exp)
	}

}

ULine {

	*kr{ |start, end, time|
		^UEnvGen.kr([start,end],[time],\lin)
	}

	*ar{ |start, end, time|
		^UEnvGen.ar([start,end],[time],\lin)
	}

}

UEnvGenRel {

	*startDurationEnv{ |vals, timesRel, int|
		var timesRel2 = timesRel / timesRel.sum;
		var start = \u_startPos.kr(0.0);
		var duration = \u_dur.kr(1.0)+start;
		var x1 = \u_doneAction.kr(0);
		var x2 = \u_fadeIn.kr(0);
		var x3 = \u_fadeOut.kr(0);
		var x4 = \u_gain.kr(0);
		var x5 = \u_gate.kr(0);
		var x6 = \u_mute.kr(0);
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

	*kr{ |start, end|
		^UEnvGenRel.kr([start,end],[1],\exp)
	}

	*ar{ |start, end|
		^UEnvGenRel.ar([start,end],[1],\exp)
	}

}

ULineRel {

	*kr{ |start, end|
		^UEnvGenRel.kr([start,end],[1],\lin)
	}

	*ar{ |start, end|
		^UEnvGenRel.ar([start,end],[1],\lin)
	}

}

