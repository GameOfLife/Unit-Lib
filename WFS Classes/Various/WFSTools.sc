WFSTools {

	*makeWFSPointsArray { arg inArray; // for WFSPath
		if(inArray.size < 2)
			{ "A WFSPath should contain at least 2 positions".warn; };
		^inArray.collect({ |item| item.asWFSPoint });
	}
	
	*asRadians { |in| ^(in / WFSPath.azMax) * 2pi; }
	
	*fromRadians { |in| ^(in / 2pi) * WFSPath.azMax; }
	
	*singleAZToXY { arg az, d, center = 0, azMax; // with contol/audio rate support
		var x, y;
		
		azMax = azMax ? WFSPath.azMax;
		if(azMax != 2pi, {az = (az / azMax) * 2pi});
		
		case { az.rate == 'control' }
			{ ^([SinOsc.kr(0, az), SinOsc.kr(0, az - (0.5pi))] * d ) + center; }
			{ az.rate == 'audio' }
			{ ^([SinOsc.ar(0, az), SinOsc.ar(0, az - (0.5pi))] * d ) + center; }
			{ true }
			{ ^([az.sin, az.cos] * d) + center; };
			
		}
	
	*azToXY { arg inArray, center = 0, round = 1.0e-16, azMax; //2D
		azMax = azMax ? WFSPath.azMax;
		^inArray.collect({ |item|
		var azimuth, distance;
		# azimuth, distance = item;
		if(azMax != 2pi, {azimuth = (azimuth / azMax) * 2pi});
		([azimuth.sin.round(round), azimuth.cos.round(round)] * distance) + center;		});
		}
	
	*xyToAZ { arg inArray, center = 0, azMax; //2D
		azMax = azMax ? WFSPath.azMax;
		if(center.class == WFSPoint)	
			{ center = [center.x, center.y]; };
		^inArray.collect({ |item|
				var azimuth, distance, xx, yy;
				#xx, yy = item - center;
				azimuth = atan2(xx,yy) % 2pi; //theta
				distance = hypot(xx,yy); //rho
				if(azMax != 2pi, {azimuth = (azimuth / 2pi) * azMax});
				[azimuth, distance];
			})
		}
	
	*validateTimeLine { |timeLine|
		var lastTime = 0;
		^timeLine.collect({ |item|
			var out;
			if(item < lastTime)
				{ out = item + lastTime; "validateTimeLine: corrected 1 timeLine item".postln; }
				{ out = item;};
			lastTime = out;
			out; });
		}
		
}