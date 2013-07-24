+ Number {
	softfold2 { |aNumber = 1.0, range|
		var out;
		range = range ?? { aNumber / 2 };
		out = this.fold2( aNumber );
		range = range.clip(0,aNumber);
		^out.clip2( aNumber-range ) + ( 
			out.excess( aNumber-range )
				.linlin( range.neg, range, -0.5pi, 0.5pi  )
				.sin * 0.64028001980323 * range
			);
	}
	
	softclip2 { |aNumber = 1.0, range|
		range = range ?? { aNumber / 2 };
		range = range.clip(1.0e-12,aNumber);
		^this.clip2( aNumber-range ) + ( 
			(this.excess( aNumber-range ) / range).distort * range
		);
	}
}

+ UGen {
	softfold2 { |aNumber = 1.0, range|
		var out;
		range = range ?? { aNumber / 2 };
		out = this.fold2( aNumber );
		range = range.clip(1.0e-12, aNumber);
		^out.clip2( aNumber-range ) + ( 
			out.excess( aNumber-range )
				.linlin( range.neg, range, -0.5pi, 0.5pi  )
				.sin * 0.64028001980323 * range
			);
	}
	
	softclip2 { |aNumber = 1.0, range|
		range = range ?? { aNumber / 2 };
		range = range.clip(1.0e-12, aNumber);
		^this.clip2( aNumber-range ) + ( 
			(this.excess( aNumber-range ) / range).distort * range
		);
	}
}

+ SequenceableCollection {
	softfold2 { arg aNumber = 1.0, range; ^this.collect(_.softfold2(aNumber,range)); }
	softclip2 { arg aNumber = 1.0, range; ^this.collect(_.softclip2(aNumber,range)); }
}

+ Point {
	softfold2 { arg aPoint = 1.0, range; 
		aPoint = aPoint.asPoint;
		range = (range ?? { aPoint * 0.5 }).asPoint;
		^Point( x.softfold2( aPoint.x, range.x ), y.softfold2( aPoint.y, range.y ) );
	}
	
	softclip2 { arg aPoint = 1.0, range; 
		aPoint = aPoint.asPoint;
		range = (range ?? { aPoint * 0.5 }).asPoint;
		^Point( x.softclip2( aPoint.x, range.x ), y.softclip2( aPoint.y, range.y ) );
	}
}
