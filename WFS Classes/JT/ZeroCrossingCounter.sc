ZeroCrossingCounter : UGen {
	*ar { arg in = 0.0;
		^this.multiNew('audio', in)
	}
	*kr { arg in = 0.0;
		^this.multiNew('control', in)
	}
 	checkInputs { ^this.checkSameRateAsFirstInput }
}


MultiUnPause : UGen {

	*ar { arg count = 0.0, range= 100;
		^this.multiNew('audio', count, range)
	}
// 	checkInputs { ^this.checkSameRateAsFirstInput }

}
