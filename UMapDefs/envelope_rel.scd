UMapDef( \envelope_rel, { |timeScale = 1.0|
	var sig;
	sig = UEnvGenRel.kr( \env, timeScale: timeScale );
	UMapOut.kr(sig);
}).mappedArgs_([ \env ])
.setSpec( \timeScale, [0.25,4,\exp,0,1].asSpec, \init )
.category_( 'automation' )