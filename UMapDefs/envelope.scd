UMapDef( \envelope, {
	var sig;
	sig = UEnvGen.kr( \env );
	UMapOut.kr(sig);
}).mappedArgs_([ \env ])