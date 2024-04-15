UImage {

	classvar <>all;
	classvar <>imageDict;

	var image;
	var <>filePath;

	*initClass {
		imageDict = IdentityDictionary();
	}

	*new { |filePath|
		^super.new.init( filePath );
	}

	addToAll {
		all = all.add( this );
	}

	init { |inFilePath|
		if( inFilePath.notNil ) {
			filePath = inFilePath.getGPath;
			if( imageDict[ filePath.asSymbol ].isNil ) {
				imageDict[ filePath.asSymbol ] = Image.open( filePath );
			};
		};
	}

	image { ^image ? imageDict[ filePath.asSymbol ] }
	image_ { |newImage| image = newImage; }

	write { |path|
		path = (path ? filePath).getGPath;
		path = path.replaceExtension( "png" );
		image.write( path );
		"writing %\n".postf( path );
		filePath = path;
		if( imageDict[ filePath.asSymbol ] !== image ) {
			imageDict[ filePath.asSymbol ].free;
			imageDict[ filePath.asSymbol ] = image;
		};
		image = nil;
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
			image = Image( peaks.size, height);
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
		this.image !? { |img| img.drawInRect( rect ); };
	}

	storeArgs { ^[ filePath.formatGPath ] }

}

USoundFileOverview : UImage {
	classvar <>defaultColor;
	var <>dur = 1, <>color;

	*initClass {
		StartUp.defer({
			defaultColor = Color.green(0.75);
		});
	}

	*new { |filePath, duration = 1, color|
		^super.new.init( filePath ).dur_( duration ? 1 ).color_( color );
	}

	getColor { ^color ? defaultColor }

	fromUChain { |chain, path, action, setDisplayColor = true|
		var bouncePath, imagePath;
		if( path.isNil ) {
			ULib.savePanel({ |pth|
				this.fromUChain( chain, pth, action );
			});
		} {
			switch( chain.getTypeColor.class,
				Color, { this.color = chain.getTypeColor; },
				this.class, { this.color = chain.getTypeColor.color; }
			);
			path = path.getGPath.replaceExtension( "wav" );
			chain.bounce(nil, path, {
				{ this.prApplyOverview( chain, path, action, setDisplayColor ); }.defer;
			}, false, false );
		};
	}

	prApplyOverview { |chain, path, action, setDisplayColor = true|
		this.dur = chain.dur;
		this.soundFilePlot( path, this.getColor, duration: chain.dur );
		File.delete( path.standardizePath );
		if( setDisplayColor ) { chain.displayColor = this; };
		action.value( this );
	}

	storeArgs { ^[ filePath.formatGPath, dur, color ] }

	penFill { |rect, alpha, fromRect| // fromRect contains duration
		var toRect;
		toRect = rect.copy;
		fromRect = fromRect ?? { rect.copy.width_( dur ); };
		toRect.width = toRect.width * ( dur / fromRect.width );
		this.getColor.blend( Color.white, 0.75 ).alpha_(0.5).penFill( rect, alpha, fromRect );
		this.image !? _.drawInRect( toRect );
	}

}