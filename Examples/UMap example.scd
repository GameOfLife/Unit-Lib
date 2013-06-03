(
UMapDef( \line, { |range = #[0.0,1.0], duration = 10|
	var sig;
	sig = Line.kr( range[0], range[1], duration );
	UMapOut.kr(sig);
});

UMapDef( \env, {
	var sig;
	sig = UEnvGen.kr( \env );
	UMapOut.kr(sig);
});
)

x = UChain( \sine, \output );

x[0].freq = UMap( \env, [ \env, Env([0.5,0.75,0.25],[2,3]) ] );

x[0].freq = UMap( \line, [ \range, [0.5,0.7] ] );

x.prepareAndStart;
x.release;
