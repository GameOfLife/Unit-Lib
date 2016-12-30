/*
use like this:

(
SynthDef( "test_map", {
	var sig, freq;
	freq =  \freq.asSpecMapKr( Line.kr(0,1,10,doneAction: 2) );
	Out.ar( 0, SinOsc.ar( freq, 0, 0.1) );
}).add;
)

x = Synth( "test_map", [ \freq, [220,440,\exp].asSpec ] );
*/

+ ControlSpec {
	
	asControlInput {
		var curveval, curvenum;
		curvenum = warp.asSpecifier;
		if( curvenum.isNumber ) {
			curveval = curvenum;
			curvenum = 5;
		} {
			curvenum = ( 
				\lin: 1, 
				\linear: 1, 
				\exp: 2, 
				\exponential: 2, 
				\cos: 3, 
				\sin: 4, 
				\amp: 6, 
				\db: 7
			)[curvenum ];
		};
		^[ minval, maxval, curvenum, curveval ? -2, step ];
	}
	
	asOSCArgEmbeddedArray { | array| ^this.asControlInput.asOSCArgEmbeddedArray(array) }
}

+ Spec {

	asControlInput {
		^this.asControlSpec.asControlInput;
	}
	
	asOSCArgEmbeddedArray { | array| ^this.asControlInput.asOSCArgEmbeddedArray(array) }
}

+ Array {
	
	asSpecMapKr { |value = 0|
		var minval, maxval, curvenum, curve, step;
		var range, dbrange, ratio, grow, a, b, value_squared, minval_dbamp;
		var lin;
		#minval, maxval, curvenum, curve, step = [0,1,1,-2,0].overWrite( this );
		value = value.clip(0,1);
		range = maxval - minval;
		ratio = maxval / minval.max(1e-12);
		curve = if( InRange.kr(curve, -0.001, 0.001 ), 0.001, curve );
		grow = exp(curve);
		a = range / (1.0 - grow);
		b = minval + a;
		value_squared = value.squared;
		minval_dbamp = minval.dbamp;
		dbrange = maxval.dbamp - minval_dbamp;
		^Select.kr( curvenum, [
			value.round(1) * range + minval, // step
			value * range + minval, // lin
			(ratio ** value) * minval, // exp
			(0.5 - (cos(pi * value) * 0.5)) * range + minval, // cos
			sin(0.5pi * value) * range + minval, // sin
			b - (a * pow(grow, value)), // curve
			if(range >= 0,  // amp
				value_squared * range + minval, 
				(1 - (1-value).squared) * range + minval 
			),
			if(dbrange >= 0, // db
				(value_squared * dbrange + minval_dbamp).ampdb, 
				((1 - (1-value).squared) * dbrange + minval_dbamp).ampdb 
			)
		]).round(step);
	}
	
	asSpecUnmapKr { |value = 0|
		var minval, maxval, curvenum, curve, step;
		var range, dbrange, ratio, grow, a, b, minval_dbamp, maxval_dbamp, value_dbamp;
		var lin;
		#minval, maxval, curvenum, curve, step = [0,1,1,-2,0].overWrite( this );
		value = value.round(step);
		range = maxval - minval;
		ratio = maxval / minval.max(1e-12);
		curve = if( InRange.kr(curve, -0.001, 0.001 ), 0.001, curve );
		grow = exp(curve);
		a = range / (1.0 - grow);
		b = minval + a;
		lin = (value - minval) / range;
		minval_dbamp = minval.dbamp;
		dbrange = maxval.dbamp - minval_dbamp;
		value_dbamp = value.dbamp;
		^Select.kr( curvenum, [
			lin.round(1), // step
			lin, // lin
			log(value/minval) / log(ratio), // exp
			acos(1.0 - (lin * 2)) / pi, // cos
			asin(lin) / 0.5pi, // sin
			log((b - value) / a) / curve, // curve
			if(range >= 0,  // amp
				((value - minval) / range).sqrt,
				1 - sqrt(1 - ((value - minval) / range))
			),
			if(dbrange >= 0, // db
				((value_dbamp - minval_dbamp) / dbrange).sqrt, 
				1 - sqrt(1 - ((value_dbamp - minval_dbamp) / dbrange))
			)
		]);
	}
}

+ Symbol {
	asSpecMapKr { |value = 0| ^this.kr([0,1,1,-2,0]).asSpecMapKr(value); }
	asSpecUnmapKr { |value = 0| ^this.kr([0,1,1,-2,0]).asSpecUnmapKr(value); }
}