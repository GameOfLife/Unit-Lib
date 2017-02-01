/*
\round

Creates an UMap for rounding off (quantizing) values. The output value will be the nearest multiple of the 'round' value.

value: the value to be rounded
round: the value to round to

The 'value' and 'round' arg ranges are mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

d = UMapDef( \round, { |value = 0.5, round = 0|
	UMapDef.useMappedArgs = false;
	value = value.round( round );
	UMapOut.kr(value);
})
	.setSpec( \round, UAdaptSpec( { |spec| 
		var center;
		spec = spec.copy;
		if( spec.isKindOf( FreqSpec ) ) {
			spec = ControlSpec.newFrom( spec );
		};
		if( spec.respondsTo( \warp ) ) {
			if( spec.warp.asSpecifier == \exp ) {
				center = spec.map( 0.5 );
				center = center / spec.maxval;
				spec.warp = (center.reciprocal-1).squared.log;
			};
		};
		spec.minval = 0;
		spec.warp = spec.warp.asSpecifier;
		spec;
	} ) )
	.mappedArgs_( [ \value, \round ] )
	.category_( 'operator' )


