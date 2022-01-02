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

UPatternEventView : UChainEventView {

	// four modes of operation:
	// all -  all editing is activated
	// move - only moving events activated
	// resize - only resizing events activated
	// fades - only changing fade times activated
	mouseDownAll{ |mousePos,scaledUserView,shiftDown|

       	px5Scaled =  scaledUserView.pixelScale.x * 5;
		px10Scaled = scaledUserView.pixelScale.x * 10;
		this.createRect(px10Scaled, scaledUserView.fromBounds.width);

		//fadeAreaHeight = (rect.height*0.3);

        this.ifIsInsideRect( mousePos, {

           // var fadeInAreaBiggerThen10Pixels = event.fadeInTime > px10Scaled;
            //resize front
            this.ifIsInResizeFrontArea( mousePos, true, {
                state = \resizingFront;
                originalStartTime = event.startTime;
                //event.wfsSynth.checkSoundFile;
            },{
               // var fadeOutAreaBiggerThen10Pixels = event.fadeOutTime > px10Scaled;
               //resize back
                this.ifIsInResizeBackArea( mousePos, true, {
                    state = \resizingBack;
                    originalEndTime = event.endTime;
                    //event.wfsSynth.checkSoundFile;
                }, {//moving
	               state = \moving;
	               originalTrack = event.track;
	               originalStartTime = event.startTime;
	               originalEndTime = event.endTime;
                })
            })
        }, {
            if(selected) {
                originalStartTime = event.startTime;
                originalEndTime = event.endTime;
                // originalFades = event.fadeTimeValues.copy;
                originalTrack = event.track;
                //event.wfsSynth.checkSoundFile;
            }
        })
	}

	mouseDownMove{ |mousePos,scaledUserView,shiftDown|

		px5Scaled =  scaledUserView.pixelScale.x * 5;
		px10Scaled = scaledUserView.pixelScale.x * 10;
		this.createRect(px10Scaled, scaledUserView.fromBounds.width);

		// fadeAreaHeight = (rect.height*0.3);

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
                //originalFades = event.fadeTimeValues.copy;
                originalTrack = event.track;
                //event.wfsSynth.checkSoundFile;
            }
        })
	}

	mouseDownResize{ |mousePos,scaledUserView,shiftDown|

        	px5Scaled =  scaledUserView.pixelScale.x * 5;
		px10Scaled = scaledUserView.pixelScale.x * 10;
		this.createRect(px10Scaled, scaledUserView.fromBounds.width);

		//fadeAreaHeight = (rect.height*0.3);

        this.ifIsInsideRect( mousePos, {

            // var fadeInAreaBiggerThen10Pixels = event.fadeInTime > px10Scaled;
            //resize front
            this.ifIsInResizeFrontArea( mousePos, true, {
                state = \resizingFront;
                originalStartTime = event.startTime;
                //event.wfsSynth.checkSoundFile;
            },{
                //var fadeOutAreaBiggerThen10Pixels = event.fadeOutTime > px10Scaled;
                //resize back
                this.ifIsInResizeBackArea( mousePos, true, {
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

	draw { |scaledUserView, maxWidth|
		var lineAlpha =  if( event.disabled ) { 0.5  } { 0.875  };
		var scaledRect, innerRect;
		var px5sq;
		var pixelScale;

		pixelScale = scaledUserView.pixelScale;

		this.createRect( pixelScale.x * 10, maxWidth);

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

			//event is playing
			if( event.isPlaying ) {
				Pen.width = 3;
				Pen.color = Color.grey(0.9);
				this.drawShape(scaledRect);
				Pen.stroke;
			};

		   if( UScoreView.hideUChains.not ) {

	        Pen.use({
	            //var fadeinScaled, fadeoutScaled, fades;
	            var textLeft = 2;

	            this.drawShape(innerRect);
	            Pen.clip;

	            // fill inside
	            Pen.addRect( innerRect );
	            event.getTypeColor.penFill(innerRect, lineAlpha, nil, 10);

	            // resize handles
	            if( event.duration != inf ) {
		            px5sq = scaledRect.width.linlin( 5, 5 * 3, 0, 5, \minmax );
		            Pen.addRect( scaledRect.copy.width_(px5sq) );
		            Pen.addRect( scaledRect.copy.left_(scaledRect.right - px5sq).width_(px5sq) );
		            Pen.color =  Color.gray(0.2, if( selected ) { 0.25 } { 0.125 });
		            Pen.fill;
		        };

	            //draw fades
	            /*
	            fades = event.fadeTimeValues;

	            if( fades.any(_!=0) ) { // draw only if needed

		            Pen.color = Color.gray(0.75, 0.75);

		            fadeinScaled = innerRect.left + (fades[0] / pixelScale.x);
		            fadeoutScaled = innerRect.right - (fades[1] / pixelScale.x);

		            // fade in
		            Pen.moveTo(innerRect.leftBottom);
		            if( UScoreView.showFadeCurves && { event.fadeInCurve != 0 }) {
			            ((..7)/8).do({	 |item|
				            Pen.lineTo(
				            	innerRect.left.blend( fadeinScaled, item ) @
				            	(innerRect.bottom.blend( innerRect.top, item.lincurve(0,1,0,1, event.fadeInCurve) ))
				            );
			            });
		            };
		            Pen.lineTo(fadeinScaled @ (innerRect.top) );
		            Pen.lineTo(innerRect.leftTop);
		            Pen.lineTo(innerRect.leftBottom);

		            // fade out
		            Pen.lineTo(innerRect.rightBottom);
		            if( UScoreView.showFadeCurves && { event.fadeOutCurve != 0 }) {
			            ((..7)/8).do({	 |item|
				            Pen.lineTo(
				            	innerRect.right.blend( fadeoutScaled, item ) @
				            	(innerRect.bottom.blend( innerRect.top, item.lincurve(0,1,0,1, event.fadeOutCurve.neg) ))
				            );
			            });
		            };
		            Pen.lineTo(fadeoutScaled @ (innerRect.top));
		            Pen.lineTo(innerRect.rightTop);
		            Pen.lineTo(innerRect.rightBottom);

		            Pen.fill;

		            //fade lines
		            Pen.width = 1;
		            Pen.color = Color.grey(0.3, 0.5);
		            Pen.moveTo(fadeoutScaled @ (innerRect.top));
		            Pen.lineTo(fadeoutScaled @ (innerRect.bottom));
		            Pen.moveTo(fadeinScaled @ (innerRect.top));
		            Pen.lineTo(fadeinScaled @ (innerRect.bottom));

		            Pen.stroke;

		       };
		       */

		       //draw name
		       if( UScoreView.showUChainNames && {scaledRect.height > 4} ) {
			       Pen.color = Color.black.alpha_( lineAlpha  );
			       if( event.lockStartTime ) {
				       DrawIcon( \lock, Rect( scaledRect.left + 2, scaledRect.top, 14, 14 ) );
				       textLeft = textLeft + 12;
			       };
			       Pen.stringAtPoint(
			       	" " ++ this.getName,
			       	scaledRect.leftTop.max( 0 @ -inf ) + (textLeft @ 1)
			       );
		       };
	        });

		   };
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
