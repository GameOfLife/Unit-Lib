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
		if( path.notNil ) {
			^all[ path.getGPath.asSymbol ] ?? { this.init; all[ path.getGPath ] };
		};
	}
	
	path_ { |newPath| path = newPath; this.init }
	
	reload { this.init( true ) }
	
	exists { ^if( path.notNil ) { File.exists( path.getGPath ) } { false } }
}