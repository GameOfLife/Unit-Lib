UImage {

	var <>image;
	var <>filePath;

	*new { |filePath|
		^super.new.init( filePath );
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
		filePath = path;
	}

	soundFilePlot { |path, color, res, write = true|
		color = color ? Color.green(0.75);
		res = (res ? (64@100)).asPoint;
		path = path.getGPath;
		SoundFile.use( path, { |f|
			var arr, size = res.x, peaks;
			arr = FloatArray.newClear( f.numFrames );
			f.readData( arr );
			peaks = (arr.size / size).ceil.asInteger.collect({ |i|
				var chunk;
				i = i * size;
				chunk = arr[i..i+size-1].abs;
				[ chunk.maxItem, chunk.mean ];
			});
			image = Image.color( peaks.size, res.y, color.blend( Color.white, 0.75 ).alpha_(0.5) );
			peaks.do({ |prms,xx|
				prms.do({ |yy, ii|
					var size;
					size = yy.abs.linlin(0,1,0,res.y-1).asInteger;
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