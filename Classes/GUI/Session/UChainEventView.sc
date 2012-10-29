/*
    Unit Library
    The Game Of Life Foundation. http://gameoflife.nl
    Copyright 2006-2011 Miguel Negrao, Wouter Snoei.

    GameOfLife Unit Library: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    GameOfLife Unit Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with GameOfLife Unit Library.  If not, see <http://www.gnu.org/licenses/>.
*/

UChainEventView : UEventView {

    var <fadeAreaHeight;

	getTypeColor {
        ^case {
	        event.displayColor.notNil;
        } {
	        event.displayColor;
        } { 
	        event.duration == inf 
	   } {
	        Color(0.4, 0.4, 0.8)
        } {
	        event.releaseSelf == true;
        } {
	        Color(0.768, 0.3,0.768);
        } {
	        Color(0.4, 0.6, 0.6);
        };
	}

	ifIsInsideRect{ |mousePos, yesAction, noAction|

	    if(rect.containsPoint(mousePos)) {
	        yesAction.value;
	    } {
	        noAction.value;
	    }

	}

	ifIsInResizeFrontArea{ |mousePos, fadeInAreaBiggerThen10Pixels, yesAction,noAction|
		var resizeFrontDetectArea;

        if(fadeInAreaBiggerThen10Pixels) {
            resizeFrontDetectArea = rect.copy.width_(px5Scaled);
        } {
            resizeFrontDetectArea = rect.copy.width_(px5Scaled).height_(rect.height-fadeAreaHeight).top_(rect.top+fadeAreaHeight);
        };
        //resize front
        if(resizeFrontDetectArea.containsPoint( mousePos )) {
            yesAction.value
        }{
            noAction.value
        }
	}

	ifIsInResizeBackArea{ |mousePos, fadeOutAreaBiggerThen10Pixels, yesAction,noAction|
		var resizeBackDetectArea =

        if(fadeOutAreaBiggerThen10Pixels) {
            resizeBackDetectArea = rect.copy.width_(px5Scaled).left_((rect.left + rect.width - px5Scaled))
        } {
            resizeBackDetectArea = rect.copy.width_(px5Scaled).left_((rect.left + rect.width - px5Scaled))
            .height_(rect.height-fadeAreaHeight).top_(rect.top+fadeAreaHeight)
        };

        //resize back
        if(resizeBackDetectArea.containsPoint( mousePos )) {
            yesAction.value
        }{
            noAction.value
        }
	}

	ifIsInFadeInArea{ |mousePos,fadeInAreaBiggerThen10Pixels, yesAction,noAction |
	     var fadeInDetectArea;

	     if(fadeInAreaBiggerThen10Pixels) {
            fadeInDetectArea = rect.copy.width_(px10Scaled).left_(rect.left + event.fadeIn - px5Scaled);
        } {
            fadeInDetectArea = rect.copy.width_(px5Scaled).height_(fadeAreaHeight);
        };

        if( fadeInDetectArea.contains( mousePos ) ) {
            yesAction.value
        }{
            noAction.value
        }
	}

	ifIsInFadeOutArea{ |mousePos, fadeOutAreaBiggerThen10Pixels,yesAction,noAction|
	    var fadeOutDetectArea;
	    if(fadeOutAreaBiggerThen10Pixels) {
            fadeOutDetectArea = rect.copy.width_(px10Scaled).left_((rect.right - event.fadeOut - px5Scaled))
        } {
            fadeOutDetectArea = rect.copy.width_(px5Scaled).left_((rect.right - px5Scaled)).height_(fadeAreaHeight);
        };
        //fade out
        if(fadeOutDetectArea.contains( mousePos ) ) {
            yesAction.value
        }{
            noAction.value
        }

	}

	// four modes of operation:
	// all -  all editing is activated
	// move - only moving events activated
	// resize - only resizing events activated
	// fades - only changing fade times activated
	mouseDownAll{ |mousePos,scaledUserView,shiftDown|

		this.createRect(scaledUserView.viewRect.width);
        px5Scaled =  scaledUserView.doReverseScale(Point(5,0)).x;
		px10Scaled = scaledUserView.doReverseScale(Point(10,0)).x;
		fadeAreaHeight = (rect.height*0.3);

        this.ifIsInsideRect( mousePos, {

            var fadeInAreaBiggerThen10Pixels = event.fadeIn > px10Scaled;
            //resize front
            this.ifIsInResizeFrontArea( mousePos, fadeInAreaBiggerThen10Pixels, {
                state = \resizingFront;
                originalStartTime = event.startTime;
                //event.wfsSynth.checkSoundFile;
            },{
                var fadeOutAreaBiggerThen10Pixels = event.fadeOut > px10Scaled;
                //resize back
                this.ifIsInResizeBackArea( mousePos, fadeOutAreaBiggerThen10Pixels, {
                    state = \resizingBack;
                    originalEndTime = event.endTime;
                    //event.wfsSynth.checkSoundFile;
                }, {//fade in
                    this.ifIsInFadeInArea( mousePos,fadeInAreaBiggerThen10Pixels, {
                        state = \fadeIn;
                        originalFades = event.fadeTimes.copy;
                    },{ //fade out
                        this.ifIsInFadeOutArea( mousePos, fadeOutAreaBiggerThen10Pixels, {
                            "fade out";
                            state = \fadeOut;
                            originalFades = event.fadeTimes.copy;
                        }, {//moving
                            state = \moving;
                            originalTrack = event.track;
                            originalStartTime = event.startTime;
                            originalEndTime = event.endTime;
                        })
                    })
                })
            })
        }, {
            if(selected) {
                originalStartTime = event.startTime;
                originalEndTime = event.endTime;
                originalFades = event.fadeTimes.copy;
                originalTrack = event.track;
                //event.wfsSynth.checkSoundFile;
            }
        })
	}

	mouseDownMove{ |mousePos,scaledUserView,shiftDown|

        this.createRect(scaledUserView.viewRect.width);
        px5Scaled =  scaledUserView.doReverseScale(Point(5,0)).x;
		px10Scaled = scaledUserView.doReverseScale(Point(10,0)).x;
		fadeAreaHeight = (rect.height*0.3);

        this.ifIsInsideRect( mousePos, {
            //moving
            state = \moving;
            originalTrack = event.track;
            originalStartTime = event.startTime;
            originalEndTime = event.endTime;
        }, {
            if(selected) {
                originalStartTime = event.startTime;
                originalEndTime = event.endTime;
                originalFades = event.fadeTimes.copy;
                originalTrack = event.track;
                //event.wfsSynth.checkSoundFile;
            }
        })
	}

	mouseDownResize{ |mousePos,scaledUserView,shiftDown|

		this.createRect(scaledUserView.viewRect.width);
        px5Scaled =  scaledUserView.doReverseScale(Point(5,0)).x;
		px10Scaled = scaledUserView.doReverseScale(Point(10,0)).x;
		fadeAreaHeight = (rect.height*0.3);

        this.ifIsInsideRect( mousePos, {

            var fadeInAreaBiggerThen10Pixels = event.fadeIn > px10Scaled;
            //resize front
            this.ifIsInResizeFrontArea( mousePos, fadeInAreaBiggerThen10Pixels, {
                state = \resizingFront;
                originalStartTime = event.startTime;
                //event.wfsSynth.checkSoundFile;
            },{
                var fadeOutAreaBiggerThen10Pixels = event.fadeOut > px10Scaled;
                //resize back
                this.ifIsInResizeBackArea( mousePos, fadeOutAreaBiggerThen10Pixels, {
                    state = \resizingBack;
                    originalEndTime = event.endTime;
                    //event.wfsSynth.checkSoundFile;
                })
            })
        }, {
            if(selected) {
                originalStartTime = event.startTime;
                originalEndTime = event.endTime;
                //event.wfsSynth.checkSoundFile;
            }
        })
	}

	mouseDownFades{ |mousePos,scaledUserView,shiftDown|

        this.createRect(scaledUserView.viewRect.width);
        px5Scaled =  scaledUserView.doReverseScale(Point(5,0)).x;
		px10Scaled = scaledUserView.doReverseScale(Point(10,0)).x;
		fadeAreaHeight = (rect.height*0.3);

        this.ifIsInsideRect( mousePos, {

            var fadeInAreaBiggerThen10Pixels = event.fadeIn > px10Scaled;
            var fadeOutAreaBiggerThen10Pixels = event.fadeOut > px10Scaled;
            //fade in
            this.ifIsInFadeInArea( mousePos,fadeInAreaBiggerThen10Pixels, {
                state = \fadeIn;
                originalFades = event.fadeTimes.copy;
            },{ //fade out
                this.ifIsInFadeOutArea( mousePos, fadeOutAreaBiggerThen10Pixels, {
                    "fade out";
                    state = \fadeOut;
                    originalFades = event.fadeTimes.copy;
                })
            })
        }, {
            if(selected) {
                originalFades = event.fadeTimes.copy;
                //event.wfsSynth.checkSoundFile;
            }
        })

	}

	mouseDownEvent{ |mousePos,scaledUserView,shiftDown,mode|

		switch(mode)
		{\all}{ this.mouseDownAll(mousePos,scaledUserView,shiftDown) }
		{\move}{ this.mouseDownMove(mousePos,scaledUserView,shiftDown) }
		{\resize}{ this.mouseDownResize(mousePos,scaledUserView,shiftDown) }
		{\fades}{ this.mouseDownFades(mousePos,scaledUserView,shiftDown) }

	}

	mouseMoveEvent{ |deltaTime, deltaTrack, overallState, snap, moveVert|

			switch(overallState)
			{\resizingFront}
			{
				event.trimStart( originalStartTime + deltaTime )
			}
			{\resizingBack}
			{
				//"resizing back";
				event.trimEnd( originalEndTime + deltaTime )
			}
			{\moving}
			{
				if( moveVert.not ) {
					event.startTime = originalStartTime + deltaTime.round(snap)
				};
				event.track = originalTrack + deltaTrack;
			}
			{\fadeIn}
			{
				event.fadeIn = originalFades[0] + deltaTime;
			}
			{\fadeOut}
			{
				event.fadeOut = originalFades[1] - deltaTime;
			}

	}

	draw { |scaledUserView, maxWidth|
		var lineAlpha =  if( event.disabled ) { 0.5  } { 1.0  };
		var scaledRect, innerRect;

		this.createRect(maxWidth);
		
		scaledRect = scaledUserView.translateScale(rect);
		
		if( scaledUserView.view.drawBounds.intersects( scaledRect.insetBy(-2,-2) ) ) {
			
			innerRect = scaledRect.insetBy(0.5,0.5);
	
			//selected outline
			if( selected ) {
				Pen.width = 2;
				Pen.color = Color.grey(0.2);
				this.drawShape(scaledRect);
				Pen.stroke;
			};
	
	        Pen.use({
	            var fadeinScaled, fadeoutScaled, fades;
	            var heightPoint;
	
	            this.drawShape(innerRect);
	            Pen.clip;
	            
	            // fill inside
	            Pen.addRect( innerRect );
	            this.getTypeColor.penFill(innerRect, lineAlpha, nil, 10);
	            
	            //draw fades
	            fades = event.fadeTimes;
	            
	            if( fades.any(_!=0) ) { // draw only if needed
		            
		            Pen.color = Color.gray(0.75, 0.75);
		            
		            fadeinScaled = innerRect.leftBottom + scaledUserView.doScale(Point(fades[0],0));
		            fadeoutScaled = innerRect.rightBottom - scaledUserView.doScale(Point(fades[1],0));
		            
		            heightPoint = Point(0,innerRect.height);
		            	            
		            // fade in
		            Pen.moveTo(innerRect.leftBottom);
		            Pen.lineTo(fadeinScaled - heightPoint);
		            Pen.lineTo(innerRect.leftTop);
		            Pen.lineTo(innerRect.leftBottom);
		           
		            // fade out
		            Pen.lineTo(innerRect.rightBottom);
		            Pen.lineTo(fadeoutScaled - heightPoint);
		            Pen.lineTo(innerRect.rightTop);
		            Pen.lineTo(innerRect.rightBottom);
		
		            Pen.fill;
		            
		            //fade lines
		            Pen.width = 1;
		            Pen.color = Color.grey(0.3, 0.5);
		            Pen.moveTo(fadeoutScaled - heightPoint);
		            Pen.lineTo(fadeoutScaled);
		            Pen.moveTo(fadeinScaled - heightPoint);
		            Pen.lineTo(fadeinScaled);
		            
		            Pen.stroke;	 
		                       
		       };
		       
		       //draw name
		       if( scaledRect.height > 4 ) {
			       Pen.color = Color.black.alpha_( lineAlpha  );
			       Pen.stringAtPoint( 
			       	" " ++ this.getName, 
			       	scaledRect.leftTop.max( 0 @ -inf ) + (2 @ 1) 
			       );
		       };
	        });
	    };

        //draw name
		/*
		Pen.color = Color.black.alpha_( lineAlpha  );

		if( scaledRect.height > 4 ) {

			textrect = scaledRect.sect( scaledUserView.view.drawBounds.moveTo(0,0).insetBy(-3,0) );
			Pen.use({
				Pen.addRect( textrect ).clip;
				Pen.stringInRect(
					" " ++ this.getName,
					textrect );
			});

			//show region bounds
			/*if(event.isFolder.not) {
				if(scaledRect.height > 35){
				Pen.use({
					var rect = scaledRect.copy.width_(30).height_(scaledRect.height/2).moveTo(scaledRect.left,scaledRect.top+(scaledRect.height/2));
					var seconds = event.wfsSynth.startFrame/44100;
					var string = SMPTE.global.initSeconds(seconds,25).format("mm:ss");
					Pen.addRect(rect).clip;
					Pen.stringLeftJustIn(string,rect);
				});
				Pen.use({
					var rect = scaledRect.copy.width_(30).height_(scaledRect.height/2).moveTo(scaledRect.right-30,scaledRect.top+(scaledRect.height/2));
					var seconds = (event.wfsSynth.startFrame/44100)+event.dur;
					var string = SMPTE.global.initSeconds(seconds,25).format("mm:ss");
					Pen.addRect(rect).clip;
					Pen.stringRightJustIn(string,rect);
				});
				}
			} */
		};
		*/

		/*
		// DEBUG resize area
		Pen.color = Color.red;
		Pen.addRect(scaledRect.copy.width_(5).height_(scaledRect.height-6).top_(scaledRect.top+6));
		Pen.fill;
		Pen.color = Color.red;
		Pen.addRect(scaledRect.copy.width_(5).left_(scaledRect.left+scaledRect.width-5).height_(scaledRect.height-6).top_(scaledRect.top+6));
		Pen.fill;
		*/


		// DEBUG fade area
		/*Pen.color = Color.blue;
		Pen.addRect(scaledRect.copy.width_(10).left_(scaledRect.left + scaledUserView.translateScale(Rect(0,0,event.fadeIn,0)).width - 5));
		Pen.fill;
		Pen.color = Color.blue;
		Pen.addRect(scaledRect.copy.width_(10).left_((scaledRect.right - scaledUserView.translateScale(Rect(0,0,event.fadeOut,0)).width - 5)));
		Pen.fill;
        */

	}

}
