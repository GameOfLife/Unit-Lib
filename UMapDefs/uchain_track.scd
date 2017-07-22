/*
\uchain_track

Creates an UMap that passes on the track number of the current UChain. 

max: maximum track number (scales to range upper value)
range: range to which the track number will be scaled (track 0 = lower value)

The 'range' arg range is mapped to that of the parameter to which the UMap is connected
--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

FuncUMapDef( \uchain_track, { |unit, max = 16, range = #[0.0,1.0]|
	var chain, out;
	chain = UChain.nowPreparingChain;
	out = if( chain.notNil ) {
		chain.track;
	} {
		0;
	};
	out.linlin(0,max,*range);
})
	.category_( 'uchain_info' )
	.mappedArgs_( [ \range ] )
	.setSpec( \max, IntegerSpec(16,0, inf) )
	.setSpec( \value, private: true );