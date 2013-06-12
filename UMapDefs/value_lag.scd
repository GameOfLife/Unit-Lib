UMapDef( \value_lag, { |value = 0.0, lag = 0.0|
	UMapOut.kr(value.lag(lag));
}).mappedArgs_([ \env ])
.mappedArgs_([ \value ])
.category_( 'filter' )