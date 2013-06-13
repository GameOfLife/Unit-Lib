FuncUMapDef( \mapdirect_control, { |unit, bus = 0|
	("c" ++ (bus + UIn.firstControlBus)).asSymbol
}, valueIsPrivate: true )
	.valueIsMapped_( false )
	.setSpec( \value, MapSpec() )
	.setSpec( \bus, PositiveIntegerSpec(0,0,31) )
	.category_( 'input' )