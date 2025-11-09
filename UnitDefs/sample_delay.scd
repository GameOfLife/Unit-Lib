/*
\sample_delay

a clean and precise sample delay.

samples/ms/meters: the length of the delay.
lag: smoothing time applied to change of delay time, use to prevent clicks during change of delay time.
numChannels: number of channels
type: 'samples' / 'ms' / 'meters'; time mode, samples, milliseconds or meters

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/


var defs, func;

func = { |val, denom, step|
	var in, delayed;
	in = UIn.ar( 0, Udef.numChannels );
	delayed = if( val < step, in, DelayN.ar( in, 1, val / denom ) );
	UOut.ar( 0, delayed )
};

defs = [
	MultiChannelUdef( \samples, { |samples = 0, lag = 0|
		func.value( samples.lag3( lag ), SampleRate.ir, 0.1 );
	}, extraPrefix: \sample_delay )
	.setSpec( \lag, [0,1,\lin].asSpec )
	.setSpec( \samples, [ 0, 48000, \lin, 1, 0 ] ),

	MultiChannelUdef( \ms, { |ms = 0, lag = 0|
		func.value( ms.lag3( lag ), 1000, 0.1 );
	}, extraPrefix: \sample_delay )
	.setSpec( \lag, [0,1,\lin].asSpec )
	.setSpec( \ms, [ 0, 1000, \lin, 0.1, 0 ] ),

	MultiChannelUdef( \meters, { |meters = 0, lag = 0|
		func.value( meters.lag3( lag ), 344, 0.1 );
	}, extraPrefix: \sample_delay )
	.setSpec( \lag, [0,1,\lin].asSpec )
	.setSpec( \meters, [ 0, 344, \lin, 0.1, 0 ] ),
];

MultiUdef( 'sample_delay', defs, \utility, \type, false, true );