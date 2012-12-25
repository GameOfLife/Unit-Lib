+ ScaledUserView {
	
	drawTimeGrid { // assumes that 1px (unscaled) = 1s
		var viewRect, l, n, l60, r, lr, bnds, scaleAmt;
		var top, bottom;
		
		viewRect = this.viewRect;
		top = viewRect.top;
		bottom = viewRect.bottom;
		l = viewRect.left.ceil;
		n = viewRect.width.ceil;
		r = (n / 5).max(1);
		r = r.nearestInList([1,5,10,30,60,300,600]);
		lr = l.round(r);
		l60 = l.round(60);
		bnds = "00:00".bounds( Font( Font.defaultSansFace, 9 ) );
		bnds.width = bnds.width + 4;
		scaleAmt = 1/this.scaleAmt.asArray;
		
		Pen.width = this.pixelScale.x / 2;
		Pen.color = Color.gray.alpha_(0.25);
		
		if( viewRect.width < (this.view.bounds.width/2) ) {			n.do({ |i|
				Pen.line( (i + l) @ top, (i + l) @ bottom );
			});
			Pen.stroke;
		};
		
		Pen.color = Color.white.alpha_(0.75);
		(n / 60).ceil.do({ |i|
			i = (i * 60) + l60;
			Pen.line( i @ top, i @ bottom );
		});
		Pen.stroke;
		
		(n/r).ceil.do({ |i|
			Pen.use({
				i = i * r;
				Pen.translate( (i + lr), bottom );
				Pen.scale( *scaleAmt );
				Pen.font = Font( Font.defaultSansFace, 9 );
				Pen.color = Color.gray.alpha_(0.25);
				Pen.addRect( bnds.moveBy( 0, bnds.height.neg - 1 ) ).fill;
				Pen.color = Color.white.alpha_(0.5);
				Pen.stringAtPoint(
					SMPTE.global.initSeconds( i+lr ).asMinSec
						.collect({ |item| item.asInt.asStringToBase(10,2); })
						.join($:),
					2@(bnds.height.neg - 1) 
				);
			});
		});
	}
}
