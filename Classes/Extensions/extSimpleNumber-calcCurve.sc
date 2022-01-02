+ SimpleNumber {

	// calculate a curve value for a specific center point
	// to be used as curve value for a ControlSpec or lincurve
	calcCurve { |min = 0, max = 1|
		^(this.linlin(min,max,0,1).max(1e-154).reciprocal-1).squared.log;
	}

}