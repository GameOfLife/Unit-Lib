FuncUMapDef( \random_value, { |unit, range = #[0.0,1.0]|
	range[0] rrand: range[1]; // result sets \value arg
}).mappedArgs_([\range]).category_( 'function' )