/*

Udef(\test,{
   UOut.ar( WhiteNoise.ar * SinOsc.ar( \lfo.krSpec(1.0,15.0,\lin,0.0,10,lag:2) ) )
})

UChain(\test, \stereoOutput).gui

*/
+ Symbol {

	krSpec{ |minval=0.0, maxval=1.0, warp='lin', step=0.0, default, lag, fixedLag = false|
		Udef.addBuildSpec(ArgSpec(this,default, ControlSpec(minval, maxval, warp, step, default) ) )
		^this.kr(default, lag, fixedLag)
	}

}