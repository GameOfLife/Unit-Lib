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

UFluidNMFBaseBuffer : AbstractRichBuffer {

	var <path;
	var <>numChannels;

	*new{ |path|
		^super.new().path_( path, true ); // update from path
	}

	*newBasic { |path, numChannels|
		^super.new().path_( path ).numChannels_( numChannels ); // don't update from path
	}

	shallowCopy{
        ^this.class.new(path, numChannels );
	}

	fromFile { |soundfile|
		if( this.prReadFromFile( soundfile ).not ) {
			"%:initFromFile - could not open file '%'\n".postf( this.class, path.basename )
		}
	}

	prReadFromFile { |soundfile|
		var test = true;
		if( soundfile.isNil or: { soundfile.isOpen.not } ) {
			soundfile = soundfile ?? { SoundFile.new };
			test = soundfile.openRead( path.getGPath.asPathFromServer );
			soundfile.close; // close if it wasn't open
		};
		if( test ) {
			this.numChannels = soundfile.numChannels;
			^true;
		} {
			^false
		};
	}

	asSoundFile { // convert to normal soundfile
		^SoundFile( path.getGPath.asPathFromServer )
			//.numFrames_( numFrames ? 0 )
			.numChannels_( numChannels ? 1 )
			.sampleRate_( sampleRate ? 44100 );
	}

	makeBuffer { |server, startPos = 0, action, bufnum|
		var buf;
		if( path.notNil ) {
			buf = Buffer.read( server, path.getGPath, 0, -1, action, bufnum );
			this.addBuffer( buf );
			^buf;
		} {
			action.value;
			^nil;
		};
	}

	*generateBases { |numComponents = 2, inPath, outPath, action| // write to local file if outPath.notNil
		var bases, buf;
		var fluidBufNMF;
		fluidBufNMF = 'FluidBufNMF'.asClass;
		outPath = outPath ?? { inPath.replaceExtension( "ufbases%".format( numComponents ) ); };
		bases = Buffer( ULib.allServers.first );
		buf = Buffer.read( ULib.allServers.first, inPath, action: {
			fluidBufNMF.processBlocking( ULib.allServers.first, buf,
				bases: bases,
				resynthMode: 0,
				components: numComponents,
				action: {
					bases.write( outPath, "aiff", "float", completionMessage: { bases.freeMsg } );
					buf.free;
					action.value( outPath );

				}
			);
		});
	}

	// mvc aware setters

	path_ { |new, update = false|
		path = (new ? path).formatGPath;
		this.changed( \path, path );
		if( update == true ) { this.prReadFromFile; };
	}

	basename { ^path !? { path.basename } }
	basename_ { |basename|
		if( path.isNil ) {
			this.path = basename;
		} {
			this.path = path.dirname +/+ basename;
		};
	}

	dirname {  ^path !? { path.dirname } }
	dirname_ { |dirname|
		if( path.isNil ) {
			this.path = dirname;
		} {
			this.path = dirname +/+ path.basename;
		};
	}

	// utilities

	plot { this.asSoundFile.plot; } // plots the raw data (a sequence of fft frames)

	checkDo { |action|
		var test = true;
		if( numChannels.isNil ) {
			test = this.prReadFromFile; // get numFrames etc.
		};
		if( test ) {
			^action.value
		} {
			"%: file % not found".format( this.class, path.quote ).warn;
			^false;
		};
	}

	asUFluidNMFBaseBuffer { ^this }

    printOn { arg stream;
		stream << this.class.name << "(" <<* [
		    	path, numChannels
		]  <<")"
	}

    storeOn { arg stream;
		stream << this.class.name << ".newBasic(" <<* [ // use newBasic to prevent file reading
		    path.formatGPath !? _.quote, numChannels
		]  <<")"
	}
}


+ Object {

	asUFluidNMFBaseBuffer {
		^UFluidNMFBaseBuffer.newBasic(nil, 2)
	}
}

+ String {

	asUFluidNMFBaseBuffer {
		^UFluidNMFBaseBuffer.new( this )
	}
}