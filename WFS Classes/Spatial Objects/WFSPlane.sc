WFSPlane {  /// plane wave
	
	// a plane wave is always directed towards the center of the system
	// angle and distance are both relative from the center (0@0)
		
	var <>radianAngle, <>distance;
	
	var <>radianWidth = 2pi, <>radianOffset = 0, <>curve = 10; // experimental, not finished
	
	// angle is stored internally as radian for optimization reasons
	
	*new { |angle = 0, distance = 0|
		^super.newCopyArgs( WFSTools.asRadians( angle ), distance );
		}
		
	*fromWFSPoint { |wfsPoint|
		wfsPoint = wfsPoint.asWFSPoint;
		^this.new( wfsPoint.angleTo( 0 ), wfsPoint.distance )
		}
		
	angle { ^WFSTools.fromRadians( radianAngle ) }
	
	angle_ { |newAngle|
		radianAngle = WFSTools.asRadians( newAngle );
		}
	
	angleFromCenter { 
		^WFSTools.fromRadians( radianAngle + pi )
		}
		
	dist { arg aWFSPoint;
		aWFSPoint = aWFSPoint.asWFSPoint;
		^this.basicDist( aWFSPoint.radianAngleFrom(0),  aWFSPoint.rho );
		//^distance - ( ( radianAngle - aWFSPoint.radianAngleFrom(0) ).cos * aWFSPoint.rho );
		}
		
	basicDist { arg inAngleFrom0, inRho;
		^distance - ( ( radianAngle - inAngleFrom0 ).cos * inRho )
		}
		
	asWFSPoint { ^WFSPoint.newAZ( this.angle, distance );  }
	asPoint { ^this.asWFSPoint.asPoint; }
	
	x { ^this.asWFSPoint.x }
	y { ^this.asWFSPoint.y }
	z { ^0 }
	
	asRect { |otherCorner|
		otherCorner = (otherCorner ? this).asWFSPoint.asPoint;
		^Rect.fromPoints( this.asPoint, otherCorner.asPoint )
	 	}
		
	
		
	printOn { arg stream;
		stream << "WFSPlane( " << this.angle << ", " << distance << " )";
		}
		
	plotSmoothInput { |size, color, fromRect| // x/y
		var path, backScaledRect;
		size = size ? 400;
		color = color ? Color.white.alpha_(0.75);
		path = (
			WFSPointArray[ 
				WFSPoint( 0,0,0 ).moveAZ( this.angle, distance * 0.9 ), 
				WFSPoint( 0,0,0 ).moveAZ( this.angle, distance * 1.1 ),
				this.asWFSPoint.moveAZ( this.angle + 90, 100 ),
				this.asWFSPoint.moveAZ( this.angle - 90, 100 )
			]).asEnvViewInput(true, fromRect);
		
		path = [path[0], 1 - path[1]];
		path = ( path * size ).flop
				.collect({ |item| item[0]@item[1] });
		// points
		color.set;
		Pen.arrow( path[1], path[0] );
		Pen.stroke;
		
		color.alpha_(0.33).set;
		Pen.moveTo( path[2] );
		Pen.lineTo( path[3] );
		Pen.stroke;
		^path;
		}
		
	plotSmooth { |speakerConf = \default, toFront = true|
		
		var window;
		var point, point2;
		var fromRect;
		
		/*
		window = SCWindow("WFSPlane", Rect(128, 64, 400, 400)).front;
		window.view.background_(Color.black );
		*/
		
		window = WFSPlotSmooth( "WFSPoint", toFront: toFront );
		
		if( speakerConf == \default )
			{ speakerConf = WFSConfiguration.default };
			
		point = this.asWFSPoint.asPoint;
		point2 = WFSPoint( 0,0,0 ).moveAZ( this.angle, distance * 1.1 ) .asPoint;
		
		fromRect = this.asRect( 0 ); // include center
		
		fromRect = fromRect.union( speakerConf.asRect );
		
		window.drawHook = { var tempPath, tempPath2, firstPoint, bounds;
			bounds = [window.view.bounds.width, window.view.bounds.height]; 
			bounds = bounds.minItem;
						
			if( speakerConf.notNil )
				{ speakerConf.plotSmoothInput( bounds, fromRect: fromRect ) };

			
			this.plotSmoothInput( bounds, fromRect: fromRect );
			
			};
			
		window.refresh;
		}
	
	storeArgs { ^[this.angle, distance] }
		
	}