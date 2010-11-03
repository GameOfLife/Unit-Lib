+ WFSPan2D {
	*focusedSourceCrossFades { |location, speakerSpec, 
			radius = 1, angle = (0.5pi), fade = (0.03pi)|
		var dist;
		dist = location.dist(0).linlin(0, radius, 2pi, angle/2).clip(angle/2, 2pi);
		^(speakerSpec.radianAnglesFrom0 - location.radianAngleFrom( WFSPoint(0,0,0) ))
			.wrap(-pi, pi ).abs.linlin(dist, dist + fade, 1,0, nil ).clip(0,1);
		}
	}