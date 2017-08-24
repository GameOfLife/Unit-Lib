/*
\p_post

Creates an pattern UMap for posting values in the post window. 

input: value or UMap to post

The 'input' arg ranges is mapped to that of the parameter to which the UMap is connected

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UPatDef( \p_post, { |unit, input = 0|
	{
		loop { input.next.postln.yield };
	}.r
})	
	.setSpec( \input, UAdaptSpec() )
	.canUseUMapFunc_({ |unit, key, umapdef| unit.isKindOf( UPattern ).not })
	.useMappedArgs_( false )
	.mappedArgs_([ \input ])
	.category_( 'pattern_utility' );