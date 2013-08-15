/*
\envelope

An envelope generator. 

env: an Env or EnvM object, containing time and level values. 

The env arg range is mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UMapDef( \envelope, {
	var sig;
	sig = UEnvGen.kr( \env, timeScale: \timeScale );
	UMapOut.kr(sig);
}).mappedArgs_([ \env ])
.category_( 'automation' )