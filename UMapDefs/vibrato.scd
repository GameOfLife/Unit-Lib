/*
\vibrato

Creates an UMap for vibrato modulation. The typical use would be on a 'freq' parameter, but it will work for any type.

value: the fundamental value around which the vibrato takes place
freq: the frequency (rate) of the vibrato
depth: size of vibrato deviation around the fundamental, as a proportion of the fundamental. 0.02 = 2% of the fundamental.
delay: delay before vibrato is established in seconds (a singer tends to attack a note and then stabilise with vibrato, for instance)
onset: transition time in seconds from no vibrato to full vibrato after the initial delay time
rateVar: noise on the freq, expressed as a proportion of the rate; can change once per cycle of vibrato
depthVar: noise on the depth of modulation, expressed as a proportion of the depth; can change once per cycle of vibrato. The noise affects independently the up and the down part of vibrato shape within a cycle
lag: a lag time for the fundamental value
seed: random seed (positive whole number). The same seed will always result in exactly the same random variation on any computer.

The 'value' arg range is mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UMapDef( \vibrato, { |value = 0.58560567020555, freq = 6, depth = 0.02, delay = 0, onset = 0
		rateVar = 0.04, depthVar = 0.1, lag = 0.1|
	URandSeed.ir();
	UMapDef.useMappedArgs = false;
	value = Vibrato.kr( value, freq, depth, delay, onset, rateVar, depthVar );
	UMapOut.kr(value);
})
	.setSpec( \freq, FreqSpec(1,300, default: 6) )
	.setSpec( \depth, [0,1,4,0,0.02].asSpec )
	.setSpec( \delay, SMPTESpec(), \init )
	.setSpec( \onset, SMPTESpec(), \init )
	.setSpec( \rateVar, [0,1,4,0,0.04].asSpec )
	.setSpec( \depthVar, [0,1,4,0,0.1].asSpec )
	.mappedArgs_( [ \value ] )
	.category_( 'modulation' )
