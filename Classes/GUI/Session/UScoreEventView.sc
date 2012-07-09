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

UScoreEventView : UEventView {

	getTypeColor {
        ^if(event.duration == inf){ Color(0.33, 0.33, 0.665) }{Color.white};
	}

	ifIsInsideRect{ |mousePos, yesAction, noAction|

	    if(rect.containsPoint(mousePos)) {
	        yesAction.value;
	    } {
	        noAction.value;
	    }

	}

	mouseDownEvent{ |mousePos,scaledUserView,shiftDown,mode|

		this.createRect(scaledUserView.viewRect.width);
        px5Scaled =  scaledUserView.doReverseScale(Point(5,0)).x;
		px10Scaled = scaledUserView.doReverseScale(Point(10,0)).x;

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
                originalTrack = event.track;
                //event.wfsSynth.checkSoundFile;
            }
        })

	}

	mouseMoveEvent{ |deltaTime, deltaTrack, overallState, snap, moveVert|

        if(overallState == \moving) {
            if( moveVert.not ) {
                event.startTime = (originalStartTime + deltaTime).round(snap)
            };
            event.track = originalTrack + deltaTrack;
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
				this.drawShape(innerRect);
				Pen.clip;
				
				// fill inside
				Pen.addRect( innerRect );
				this.getTypeColor.penFill(innerRect, lineAlpha * 0.75, nil, 10);
				
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

	}

}
