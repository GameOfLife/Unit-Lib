/*
\line_rel

An line generator of which the duration is automatically scaled to that of the event.

range: [ low, high ] the range defines the start and end points of the line
up: a boolean that decides wether the line goes from low to high (true) or from high to low (false)
curve: a curve value for the line. A 0 (zero) curve creates a linear line, a positive curve value makes the line tend towards the low value, and v.v.
timeScale: if timeScale is 1, the duration of the line will become exactly that of the event, lower and higher timeScales result in a shorter and longer envelope durations.


The 'range' arg range is mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UMapDef( \line_rel, { |range = #[0.0,1.0], up = 1, curve = 0, timeScale = 1.0|
	var sig, lineRange = [0,1];
	lineRange = if( up, lineRange, lineRange.reverse );
	sig = ULineRel.kr( lineRange[0], lineRange[1], nil, timeScale );
	sig = Select.kr( curve.inRange(-0.001,0.001), [
		sig.lincurve(0,1, range[0], range[1], curve ),
		sig.linlin(0,1, range[0], range[1] )
	]);
	UMapOut.kr(sig);
})
	.setSpec( \up, BoolSpec(true) )
	.setSpec( \curve, [-16,16,\lin,0,0].asSpec )
	.setSpec( \timeScale, [0.25,4,\exp,0,1].asSpec, \init )
	.category_( 'automation' )
	.mappedArgs_( [ \range ] );