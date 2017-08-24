/*
\p_crossfade

Creates an pattern UMap for linear crossfading between two values.

a: the first value
b: the second value
crossfade: (0-1) the crossfading position (a to b)

The 'a' and 'b' arg ranges are mapped to that of the parameter to which the UMap is connected

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UPatDef( \p_crossfade, { |unit, a = 0.0, b = 1.0, crossfade = 0.5|
	{
		inf.do { |i|
			var cf;
			cf = crossfade.next;
			switch( cf,
				0, { a.next.yield; },
				1, { b.next.yield; },
				{ a.next.blend( b.next, cf ).yield }
			);
		};
	}.r;
}).mappedArgs_([\a, \b])
.useMappedArgs_( false )
.setSpec( \a, UAdaptSpec() )
.setSpec( \b, UAdaptSpec() )
.canUseUMapFunc_({ |unit, key, umapdef|
	unit.isKindOf( UPattern ).not && {	
		[ Point, SimpleNumber, Array ].any({ |class|
			unit.getDefault( key ).isKindOf( class )
		});
	};
})
.category_( 'pattern_choice' );
