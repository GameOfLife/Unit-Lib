UImage {

	classvar <>all;

	var <>image;
	var <>filePath;

	*new { |filePath|
		^super.new.init( filePath ).addToAll;
	}

	addToAll {
		all = all.add( this );
	}

	init { |inFilePath|
		if( inFilePath.notNil ) {
			filePath = inFilePath.getGPath;
			image = Image.open( filePath );
		};
	}

	write { |path|
		path = (path ? filePath).getGPath;
		path = path.replaceExtension( "png" );
		image.write( path );
		"writing %\n".postf( path );
		filePath = path;
	}

	soundFilePlot { |path, color, width = 5000, height = 100, duration, write = true|
		color = color ? Color.green(0.75);
		path = path.getGPath;
		SoundFile.use( path, { |f|
			var arr, size, peaks, numFrames;
			if( duration.isNil ) {
				numFrames = f.numFrames;
			} {
				numFrames = (duration * f.sampleRate).ceil.asInteger;
			};
			size = (numFrames / width).ceil.asInteger;
			arr = FloatArray.newClear( size * f.numChannels );
			peaks = (numFrames / size).ceil.asInteger.collect({ |i|
				var chunk;
				i = i * size;
				if( i < (f.numFrames * f.numChannels ) ) {
					f.readData( arr );
					chunk = arr.abs;
					if( arr.size == 0 ) { arr = [0] };
					[ chunk.maxItem, chunk.mean ];
				} {
					[ 0, 0 ];
				};
			});
			image = Image.color( peaks.size, height, Color.clear );
			peaks.do({ |prms,xx|
				prms.do({ |yy, ii|
					var size;
					size = yy.abs.linlin(0,1,0,height-1).asInteger;
					size.do({ |i|
						image.setColor( color.alpha_(ii*0.5+0.5), xx, 99-i );
					});
				})
			});
			if( write ) {
				this.write( path );
			};
		});
	}

	penFill { |rect|
		image !? _.drawInRect( rect );
	}

	storeArgs { ^[ filePath.formatGPath ] }

}

USoundFileOverview : UImage {
	classvar <>defaultColor;
	var <>duration = 1, <>color;

	*initClass {
		StartUp.defer({
			defaultColor = Color.green(0.75).blend( Color.white, 0.75 ).alpha_(0.5);
		});
	}

	*new { |filePath, duration = 1, color|
		^super.new.init( filePath ).duration_( duration ? 1 ).color_( color );
	}

	storeArgs { ^[ filePath.formatGPath, duration, color ] }

	penFill { |rect, alpha, fromRect| // fromRect contains duration
		var toRect;
		toRect = rect.copy;
		fromRect = fromRect ?? { rect.copy.width_( duration ); };
		toRect.width = toRect.width * ( duration / fromRect.width );
		(color ? defaultColor).penFill( rect, alpha, fromRect );
		image !? _.drawInRect( toRect );
	}

}