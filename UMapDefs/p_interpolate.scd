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

UPatDef( \p_interpolate, { |unit, input = 0, n = 1, type = \linear|
	{
		var i = 0, val, nn;
		var last, hist = [], ttype;
		var cubic = { |i, y0, y1, y2, y3|
			// this is used by many UGens as "cubic"
			var c0, c1, c2, c3;
			c0 = y1;
			c1 = 0.5 * (y2 - y0);
			c2 = y0 - (2.5 * y1) + (2.0 * y2) - (0.5 * y3);
			c3 = (0.5 * (y3 - y0)) + (1.5 * (y1 - y2));
			((c3 * i + c2) * i + c1) * i + c0;
		};

		if( unit.spec.respondsTo( \warp ) && { unit.spec.warp.asSpecifier != \lin }; ) {
			val = unit.spec.unmap( input.next( true ) );
			hist = [val,val,val];
			loop {
				if( i == 0 ) {
					//last = val;
					hist = hist[1..].add( val );
					val = unit.spec.unmap( input.next );
					nn = n.next;
				};
				unit.spec.map(
					switch( type.next,
						\step, { if( i/nn < 0.5 ) { hist.last; } { val } },
						\linear, { hist.last.blend( val, i/nn ) },
						\cubic, { cubic.( i/nn, *hist.add( val ) ) },
					)
				).yield;
				i = (i+1);
				if( i > (nn - 1) ) { i = 0; };
			};
		} {
			val = input.next( true );
			hist = [val,val,val];
			loop {
				if( i == 0 ) {
					//last = val;
					hist = hist[1..].add( val );
					val = input.next;
					nn = n.next;
				};
				switch( type.next,
					\step, { if( i/nn < 0.5 ) { hist.last; } { val } },
					\linear, { hist.last.blend( val, i/nn ) },
					\cubic, { cubic.( i/nn, *hist.add( val ) ) },
				).yield;
				i = (i+1);
				if( i > (nn - 1) ) { i = 0; };
			};
		};
	}.r
})
	.useMappedArgs_( false )
	.setSpec( \input, UAdaptSpec() )
	.setSpec( \n, IntegerSpec(1, 1,((2**31) - 1).asInteger) )
.setSpec( \type, ListSpec([ 'step', 'linear', 'cubic' ], 1 ) )
	.canUseUMapFunc_({ |unit, key, umapdef|
		unit.isKindOf( UPattern ).not && {
			[ Point, SimpleNumber, Array ].any({ |class|
				unit.getDefault( key ).isKindOf( class )
			}) && { UAdaptSpec().canAdapt( unit.getSpec( key ) ) }
		};
	})
	.mappedArgs_([ \input ]).category_( 'pattern_filter' );