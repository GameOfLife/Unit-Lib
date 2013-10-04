/*
\peak

Parametric equalizer

freq: cutoff frequency.
rq: the reciprocal of Q. bandwidth / cutoffFreq
lag: smoothing time for the freq parameter.
db: boost/cut the center frequency (in dBs).
--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

Udef( \peak, { |freq = 440, rq = 1, db = 0, lag = 0|
	var in;
	in = UIn.ar(0,1);
	in = BPeakEQ.ar( in, freq.lag(lag).clip(20,20000), rq, db );
	UOut.ar( 0, in );
})
.category_( \filter )
.setSpec( \freq, \freq.asSpec )
.setSpec( \rq, [ 0.01, 10, \exp, 0, 1 ].asSpec )
.setSpec( \lag, [0,1,\lin].asSpec )
.setSpec( \db, [-24,24,\lin].asSpec )
