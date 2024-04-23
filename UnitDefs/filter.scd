/*
\filter

A collection of 6 filter types, basic high/low pass and shelving filters

freq: cutoff frequency (all filters)
rq: the reciprocal of Q. bandwidth / cutoffFreq (lowPass, highPass)
rs: the reciprocal of S. Shell boost/cut slope (lowShelf, highShelf)
db: boost/cut the center frequency (in dBs). (lowShelf, highShelf)
bw: the bandwidth in octaves between -3 dB frequencies (bandPass, bandStop)
makeUp: estimated gain compensation for bandPass (0-1)
type: \lowPass, \highPass, \bandPass, \bandStop, \lowShelf, \highShelf (default: \lowPass)

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

var defs;

defs = [
	Udef( \lowPass, { |freq = 440, rq = 1|
		var in;
		in = UIn.ar(0,1);
		in = BLowPass.ar( in, freq.clip(20,20000), rq );
		UOut.ar( 0, in );
	}, addToAll: false, extraPrefix: "filter" )
	.setSpec( \freq, \freq.asSpec )
	.setSpec( \rq, [ 0.01, 10, \exp, 0, 1 ].asSpec ),

	Udef( \highPass, { |freq = 440, rq = 1|
		var in;
		in = UIn.ar(0,1);
		in = BHiPass.ar( in, freq.clip(20,20000), rq );
		UOut.ar( 0, in );
	}, addToAll: false, extraPrefix: "filter" )
	.setSpec( \freq, \freq.asSpec )
	.setSpec( \rq, [ 0.01, 10, \exp, 0, 1 ].asSpec ),

	Udef( \bandPass, { |freq = 440, bw = 1, makeUp = 0.0|
		var in;
		in = UIn.ar(0,1);
		in = BBandPass.ar( in, freq.clip(20,20000), bw );
		in = in * DC.kr(1).blend( bw.explin(0.01,10,30,0,\none).dbamp, makeUp );
		UOut.ar( 0, in );
	}, addToAll: false, extraPrefix: "filter")
	.setSpec( \freq, \freq.asSpec )
	.setSpec( \bw, [ 0.001, 10, \exp, 0, 1 ].asSpec ),

	Udef( \bandStop, { |freq = 440, bw = 1|
		var in;
		in = UIn.ar(0,1);
		in = BBandStop.ar( in, freq.clip(20,20000), bw );
		UOut.ar( 0, in );
	}, addToAll: false, extraPrefix: "filter")
	.setSpec( \freq, \freq.asSpec )
	.setSpec( \bw, [ 0.001, 10, \exp, 0, 1 ].asSpec ),

	Udef( \lowShelf, { |freq = 440, rs = 1, db = 0|
		var in;
		in = UIn.ar(0,1);
		in = BLowShelf.ar( in, freq.clip(20,20000), rs, db );
		UOut.ar( 0, in );
	}, addToAll: false, extraPrefix: "filter")
	.setSpec( \freq, \freq.asSpec )
	.setSpec( \rs, [ 0.6, 10, \exp, 0, 1 ].asSpec )
	.setSpec( \db, [-24,24,\lin].asSpec ),

	Udef( \highShelf, { |freq = 440, rs = 1, db = 0|
		var in;
		in = UIn.ar(0,1);
		in = BHiShelf.ar( in, freq.clip(20,20000), rs, db );
		UOut.ar( 0, in );
	}, addToAll: false, extraPrefix: "filter" )
	.setSpec( \freq, \freq.asSpec )
	.setSpec( \rs, [ 0.6, 10, \exp, 0, 1 ].asSpec )
	.setSpec( \db, [-24,24,\lin].asSpec ),
];

MultiUdef( 'filter', defs, 'filter', 'type', false, true );
