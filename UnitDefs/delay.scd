/*
\delay

time: the length of the delay.
maxTime: the maximum length you will use for your delay. This is necessary because the delay will have to allocate a buffer. maxTime is an 'init' parameter (recognized by the "(i)" in the UChain editor), which means changed values only become effective when the event is started the next time.
dry: level of the dry signal.
amp: level of the delayed signal.
lag: smoothing time for the changing of parameter settings.

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/


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
	