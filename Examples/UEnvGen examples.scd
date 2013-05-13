(
Udef(\test, {
	var env = UEnvGen.ar([200,400,100,200],  [2.0,2.0,2.0], \lin);
	var out = SinOsc.ar( env ) * 0.2;
	UOut.ar(0, out )
});
Udef(\test2, {
	var env = ULine.ar(200,400, 10, \freq);
	var out = SinOsc.ar( env ) * 0.2;
	UOut.ar(0, out )
})
.setSpec(\freq, RangeSpec(20,20000));
Udef(\test1Rel, {
	var env = UEnvGenRel.ar([200,400,100,200],  [2.0,4.0,2.0], \lin);
	var out = SinOsc.ar( env ) * 0.2;
	UOut.ar(0, out )
});
Udef(\test2Rel, {
	var env = UXLineRel.ar(200, 400);
	var out = SinOsc.ar( env ) * 0.2;
	UOut.ar(0, out )
})
.setSpec(\freq, RangeSpec(20,20000) )
)

(
UScore(
	UChain(0,0,6, \test, \stereoOutput),
	UChain(3,1,10, \test2, \stereoOutput)
).gui
)


(
//change dur, run again
var dur = 4;
UScore(
	UChain(0,0,dur, \test1Rel, \stereoOutput),
	UChain(0,1,dur, \test2Rel, \stereoOutput)
).gui
)
