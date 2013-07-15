/*
\lag

An UMap that slows down value changes over time. It is implemented as a one-pole low-pass filter, with different settings for up- and downward movement.

value: the value upon which the lag is applied (this can be another UMap as well)
timeUp: the time it takes to move upwards to within 0.01% towards a new value
timeDown: the time it takes to move downwards to within 0.01% towards a new value

The value arg range is mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UMapDef( \lag, { |value = 0.0, timeUp = 0.0, timeDown = 0.0|
	UMapOut.kr(value.lag(timeUp, timeDown));
})
.setSpec( \timeUp, [0,10,\lin].asSpec )
.setSpec( \timeDown, [0,10,\lin].asSpec )
.mappedArgs_([ \value ])
.category_( 'filter' )