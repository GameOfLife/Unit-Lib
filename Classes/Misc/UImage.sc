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

	soundFilePlot { |path, color, width = 5000, height = 100, write = true|
		color = color ? Color.green(0.75);
		path = path.getGPath;
		SoundFile.use( path, { |f|
			var arr, size, peaks;
			size = (f.numFrames / width).ceil.asInteger;
			arr = FloatArray.newClear( size * f.numChannels );
			peaks = (f.numFrames / size).ceil.asInteger.collect({ |i|
				var chunk;
				i = i * size;
				f.readData( arr );
				chunk = arr.abs;
				[ chunk.maxItem, chunk.mean ];
			});
			image = Image.color( peaks.size, height, color.blend( Color.white, 0.75 ).alpha_(0.5) );
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