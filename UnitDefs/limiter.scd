Udef( \limiter, { |limit = 0, lookAhead = 0.01|
	var in;
	in = UIn.ar(0,1);
	in = Limiter.ar( in, limit.dbamp, lookAhead );
	UOut.ar( 0, in );
})
	.category_( \dynamics )
	.setSpec( \limit, [ -60, 0, \lin, 1, 0].asSpec )
	.setSpec( \lookAhead, [ 0.001, 0.1, \exp, 0, 0.01].asSpec, \init );