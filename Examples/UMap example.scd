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

(
UMapDef( \mapkr, { |bus = 0, range = #[0.0,1.0]|
	var sig;
	sig = In.kr(bus + UIn.firstControlBus);
	sig = sig.linlin(0,1,*range);
	UMapOut.kr(sig);
});

Udef( \lfo, { |freq = 2, amp = 1|
	var sig;
	sig = SinOsc.kr(freq, 0, amp).range(0,1);
	UOut.kr(0,sig);
}).setSpec( \freq, FreqSpec(0.01,100) );
)

y = UChain( \lfo, \sine, \output );
y[1].freq = UMap( \mapkr, [ \range, [0.5,0.7]]);

y.prepareAndStart;
y.release;