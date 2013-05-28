/*

Udef(\test,{
   UOut.ar( WhiteNoise.ar * SinOsc.ar( \lfo.ukr(10.0, 1.0, 15.0,\lin,0.0,lag:2) ) )
})

UChain(\test, \stereoOutput).gui

TODO:
intelligently sense the size of the default, and switch to ControlSpec, RangeSpec or ArrayControlSpec
*/
+ Symbol {

	ukr{ |default, minval=0.0, maxval=1.0, warp='lin', step=0.0, lag, fixedLag = false|
		Udef.addBuildSpec(ArgSpec(this, default, ControlSpec(minval, maxval, warp, step, default) ) );
		^this.kr(default, lag, fixedLag)
	}

	uir { |default, minval=0.0, maxval=1.0, warp='lin', step=0.0, lag, fixedLag = false|
		Udef.addBuildSpec(ArgSpec(this, default, ControlSpec(minval, maxval, warp, step, default) )
			.mode_(\init) );
		^this.kr(default, lag, fixedLag)
	}

	utr { |default|
		Udef.addBuildSpec(ArgSpec(this, default, TriggerSpec(this)) );
		^this.tr(default)
	}

}