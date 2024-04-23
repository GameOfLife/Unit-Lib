/*
\lowShelf

Low Shelf filter

freq: cutoff frequency.
rs: the reciprocal of S. Shell boost/cut slope
db: boost/cut the center frequency (in dBs).

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

Udef( \lowShelf, { |freq = 440, rs = 1, db = 0|
	var in;
	in = UIn.ar(0,1);
	in = BLowShelf.ar( in, freq.clip(20,20000), rs, db );
	UOut.ar( 0, in );
})
.category_( \private )
.setSpec( \freq, \freq.asSpec )
.setSpec( \rs, [ 0.6, 10, \exp, 0, 1 ].asSpec )
.setSpec( \db, [-24,24,\lin].asSpec )
.uchainInitFunc_({ |unit|
	var args;
	if( unit.def.category == \private ) {
		args = [ \rs, unit.get( \rs ), \db, unit.get( \db ) ];
		unit.defName = \filter;
		unit.set( \type, \lowShelf );
		unit.set( *args );
	};
});