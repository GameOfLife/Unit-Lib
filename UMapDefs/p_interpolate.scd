/*
\p_interpolate

Creates an pattern UMap for linear interpolation between values from other pattern UMaps. The output values of a connected (pattern) UMap is repeated n times until the next value is called.

input: value or UMap
n: number of interpolating steps to next value (1 = immediately to next)

The 'input' arg ranges is mapped to that of the parameter to which the UMap is connected

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UPatDef( \p_interpolate, { |unit, input = 0, n = 1|
	{
		var i = 0, val, nn;
		var last;
		if( unit.spec.respondsTo( \warp ) && { unit.spec.warp.asSpecifier != \lin }; ) { 
			val = unit.spec.unmap( input.next( true ) );
			loop {	
				if( i == 0 ) { 
					last = val;
					val = unit.spec.unmap( input.next );
					nn = n.next;
				};
				unit.spec.map( last.blend( val, i/nn ) ).yield;
				i = (i+1);
				if( i > (nn - 1) ) { i = 0; };
			};
		} {
			val = input.next( true );
			loop {
				if( i == 0 ) { 
					last = val;
					val = input.next;
					nn = n.next;
				};
				last.blend( val, i/nn ).yield;
				i = (i+1);
				if( i > (nn - 1) ) { i = 0; };
			};
		};
	}.r
})
	.useMappedArgs_( false )
	.setSpec( \input, UAdaptSpec() )
	.setSpec( \n, IntegerSpec(1, 1,((2**31) - 1).asInt) )
	.mappedArgs_([ \input ]).category_( 'pattern' );