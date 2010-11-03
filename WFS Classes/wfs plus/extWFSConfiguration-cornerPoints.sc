+ WFSConfiguration {
	cornerPoints { // need to be added manually for half configurations
		var points;
		^cornerPoints ?? {
		points = Array.with(*speakerLines.collect({ |sl, i|
			var a,b, nxt, pt;
			nxt = speakerLines.wrapAt(i+1);
			
			a = Polar( sl.shortestDistFromBasic( WFSPoint(0,0,0) ),
				((((sl[0].angle -180) / 360) * (-2pi)) + 0.5pi).wrap(0,2pi) );
			b = Polar( nxt.shortestDistFromBasic( WFSPoint(0,0,0) ),
				((((nxt[0].angle -180) / 360) * (-2pi)) + 0.5pi).wrap(0,2pi) );
				
			pt = (a.magnitude@
						((a.magnitude*(((b.angle - 0.5pi)-a.angle).tan))
						+ (b.magnitude/(((b.angle - 0.5pi)-a.angle).cos)))
					).rotate( a.angle );
			
			WFSSpeaker(pt.x, pt.y, 0, sl.angle );
			
			});
		 	);
		 	
		cornerPoints = speakerLines.collect({ |sl, i|
			var out;
			out = WFSSpeakerArray.with( points.wrapAt(i-1).copy, points.at(i) );
			out[0].angle = out[0].angle + 90;
			out;
			});
		};
		
		}
	}
	
+ WFSPan2D {
	*cornerPointCrossFades { |location, speakerSpec|
		if( speakerSpec.useCrossFades &&
				{ speakerSpec.speakerLines.size > 1 } ) 
					// dirty trick for dual line setup, change later
			{ ^speakerSpec.speakerLines.collect({ |spl, index|
				var out;
				out = ((((speakerSpec.cornerPoints[index]
		 			.collect( _.radianAngleFrom( location ) )
						- spl[0].radianAngle) * [-1,1])
					.fold( 0.75pi, 1.75pi ).clip(pi,1.5pi) - pi).sin).product;
				out.dup( spl.size );
				}).flat;
			}
			{ ^1  };
		}
	}