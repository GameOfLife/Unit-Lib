/*
\mouse_axis

Creates an UMap for getting data from the computer mouse or trackpad cursor position. The position is projected on a line. The line can be the x axis (default) or y axis, but via the 'rotate' argument it can also be somewhere in between. The screen is scaled to a range from -1 to 1 on the x and y axes. 

which: the axis to project on
rotate: amount of rotation (to deviate from the axis)
fromRange: the range from which the output value will be calculated
toRange: the value range to which the range will be mapped
lag: amount of lag time in seconds, to dezipper cursor movement
clipMode: when the value is outside the fromRange, the clipMode decides what happens
	0 - 'clip' (default): clip inside the value range
	1 - 'fold': fold/mirror inside the value range
	2 - 'wrap': wrap around the value range
	3 - 'none': no clipping applied (values can go outside the value range)
clipSoftness: softens the edges of 'clip' and 'fold' modes (0-1).

The 'toRange' arg range is mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the WFSCollider Class Library default UMapDefs set
*/


UMapDef( \mouse_axis, { |
		which = 0, 
		rotate = 0,
		fromRange = #[-1, 1],
		toRange = #[0.0,1.0],
		lag = 0.2,
		clipMode = 0,
		clipSoftness = 0.0|
	var value, point;
	point = [ MouseX.kr(-1,1, lag: lag ), MouseY.kr(-1,1, lag: lag ) ];
	point = point.asPoint.rotate(rotate).asArray;
	value = Select.kr( which, point ++ point.neg );
	value = value.linlin( *fromRange ++ [-1,1, \none ] );
	value = Select.kr( clipMode, [
		value.softclip2(1, clipSoftness ),
		value.softfold2(1, clipSoftness ),
		value.wrap2(1, clipSoftness ),
		value
	]);
	value = value.linlin( -1, 1, *toRange ++ [\none] );
	UMapOut.kr( value );
})
	.mappedArgs_( [ \toRange ] )
	.setSpec( \which, ListSpec( (..3), 0, [ \x, \y, '-x', '-y' ] ) )
	.setSpec( \rotate, AngleSpec( -0.5pi, 0.5pi ) )
	.setSpec( \fromRange, [-1,1].asSpec.asRangeSpec )
	.setSpec( \lag, [0,1].asSpec )
	.setSpec( \clipMode, ListSpec( (..3), 0, [ \clip, \fold, \wrap, \none ] ) )
	.setSpec( \clipSoftness, [0,1,\lin,0,0.0].asSpec )
	.category_( 'control' )