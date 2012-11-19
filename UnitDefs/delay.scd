// If the delay is played on a different output bus, the original signal will remain untouched

Udef( \delay, { |time = 0.1, maxTime = 0.1, dry = 1, amp = 0.2, lag = 0|
	var in, delayed;
	in = UIn.ar( 0, 1 );
	time = LPFLag.kr( time, lag ).min( maxTime );
	delayed = DelayC.ar( in, maxTime, time, amp.lag(lag) );
	UOut.ar( 0, delayed + ( in * dry.lag(lag) ) )
} )
	.category_( \effect )
	.setSpec( \time, [ 0, 2, \lin, 0, 0.1 ] )
	.setSpec( \maxTime, [ 0, 2, \lin, 0, 0.1 ], \init )
	.setSpec( \dry, [0,1,\lin,0], \normal )
	.setSpec( \lag, [0,1,\lin].asSpec )
	.setSpecMode( \amp, \normal );
	