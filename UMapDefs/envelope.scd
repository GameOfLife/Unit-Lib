/*
\envelope

An envelope generator.

env: an Env or EnvM object, containing time and level values.
timeScale: a scale for the time/duration of the Env (can be modulated)
loop: loopmode (0: off, 1: loop, 2: alternate)
delay: delay time before starting the env (not affected by timeScale)
trigger: a trigger that restarts the env
timeMode: \absolute or \relative:
\absolute will use the duration specified in the Env object (a sum of the level values)
\relative will scale the duration of the Env to that of the UChain

The env arg range is mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

var defs;

defs = [
	UMapDef( \absolute, {
		var sig;
		sig = UEnvGen.kr( \env, nil, \timeScale, \loop, \delay, \trigger );
		UMapOut.kr(sig);
	}, addToAll: false, extraPrefix: "envelope" ).mappedArgs_([ \env ]),
	UMapDef( \relative, {
		var sig;
		sig = UEnvGenRel.kr( \env, nil, \timeScale, \loop, \delay, \trigger );
		UMapOut.kr(sig);
	}, addToAll: false, extraPrefix: "envelope" ).mappedArgs_([ \env ])
];

MultiUMapDef( \envelope, defs, \automation, \timeMode, false, true )
.mappedArgs_([ \env ])