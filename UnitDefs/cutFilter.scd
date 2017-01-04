/*
\cutFilter

This is a combination of a low cut and a high cut filter. Both filters are cascaded Butterworth filters, and order is settable between 2nd and 8th.
freq: cutoff frequency for a low cut and a high cut filter.order: sets the order (steepness) of both filters. The orders are: 	0: off	1: 2nd order (12dB/octave)	2 (default): 4th order (24dB/octave)	3: 6th order (36dB/octave)	4: 8th order (48dB/octave)lag: smoothing time for the freq parameter.
mode: operating mode;
	low_high (default): combination of low and high cut filters
	low: only low cut
	high: only high cut

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

(
var defs, def;
defs = [ 
	\low_high, \freq.asSpec.asRangeSpec.default_( [20,20000] ),
	\low, FreqSpec(default: 20), 
	\high, FreqSpec(default: 20000),
].clump(2).collect({ |args|
	var type, spec;
	#type, spec = args;
	spec = spec.asSpec;
	Udef( type, {
		var in, lowFreq, highFreq;
		var order, lag;
		switch( type,
			\low_high,  { #lowFreq, highFreq = \freq.kr( spec.default ); },
			\low, { lowFreq = \freq.kr( spec.default ); },
			\high, { highFreq = \freq.kr( spec.default );},
		);
		order = \order.kr(2);
		lag = \lag.kr(0);
		in = UIn.ar(0,1);
		if( lowFreq.notNil ) { in = BLowCut.ar( in, lowFreq.lag(lag).clip(20,20000), order, 4 ); };
		if( highFreq.notNil ) { in = BHiCut.ar( in, highFreq.lag(lag).clip(20,20000), order, 4 ); };
		UOut.ar( 0, in );
	}, addToAll: false, extraPrefix: \cutFilter_ )
		.setSpec( \freq, spec )
		.setSpec( \order, [ 0, 4, \lin, 1, 2 ].asSpec )
		.setSpec( \lag, [0,1,\lin].asSpec );
});

MultiUdef( \cutFilter, defs, \filter, \mode, false );
)