UMIDIFile {

	classvar <>all;

	var <path;

	*initClass {
		all = IdentityDictionary();
	}

	*new { |path, reload = false|
		^super.newCopyArgs( path ).init( reload );
	}

	init { |reload = false|
		var rawPath, file;
		if( path.notNil ) {
			path = path.formatGPath;
			rawPath = path.getGPath;
			if( this.exists ) {
				if( reload or: { all[ rawPath.asSymbol ].isNil }) {
					file = SimpleMIDIFile.read( rawPath );
					if( file.midiEvents.size > 0 ) {
				 		all[ rawPath.asSymbol ] = SimpleMIDIFile.read( rawPath );
				 		UMIDIFile.changed( \added );
					} {
						"% : File appears to be empty (probably not a MIDI File)\n\t%"
							.format( this.class, rawPath ).warn;
					};
				};
			} {
				"% : File not found:\n\t%".format( this.class, rawPath ).warn;
			};
		};
	}

	midiFile {
		^if( path.notNil ) {
			all[ this.key ] ?? { this.init; all[ this.key ] };
		};
	}

	path_ { |newPath| path = newPath; this.init }

	key { ^path.getGPath.asSymbol }

	reload { this.init( true ) }

	exists { ^if( path.notNil ) { File.exists( path.getGPath ) } { false } }

	storeArgs { ^if( path.notNil ) { [ path.formatGPath ] } { [] } }

	asUMIDIFile { ^this }
}

+ Object {
	asUMIDIFile { ^UMIDIFile() }
}

+ String {
	asUMIDIFile { ^UMIDIFile(this) }
}
