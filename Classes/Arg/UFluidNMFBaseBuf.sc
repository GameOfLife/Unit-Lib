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

	classvar <>extension = "ufbases%";

	var <path;
	//var <>numChannels;

	*new{ |path|
		^super.new().path_( path, true ); // update from path
	}

	*newBasic { |path, numChannels, numFrames, sampleRate|
		^super.new().path_( path, false )
		.numChannels_( numChannels )
		.numFrames_( numFrames )
		.sampleRate_( sampleRate )
		; // don't update from path
	}

	shallowCopy{
        ^this.class.new(path, numChannels );
	}

	numChannels_ { |newNumChannels| numChannels = newNumChannels; this.changed( \numChannels ) }

	numFrames_ { |newNumFrames| numFrames = newNumFrames; this.changed( \numFrames ) }

	sampleRate_ { |newSampleRate| sampleRate = newSampleRate; this.changed( \sampleRate ) }

	fromFile { |soundfile|
		if( this.prReadFromFile( soundfile ).not ) {
			"%:initFromFile - could not open file '%'\n".postf( this.class, path.basename )
		}
	}

	prReadFromFile { |soundfile|
		var test = true;
		//path.postln;
		if( soundfile.isNil or: { soundfile.isOpen.not and: { path.notNil } } ) {
			soundfile = soundfile ?? { SoundFile.new };
			if( path.notNil ) {
				test = soundfile.openRead( path.getGPath.asPathFromServer );
				soundfile.close; // close if it wasn't open
			} {
				test = false;
			};
		};
		if( test ) {
			this.numFrames = soundfile.numFrames;
			this.numChannels = soundfile.numChannels;
			this.sampleRate = soundfile.sampleRate;
			^true;
		} {
			^false
		};
	}

	asSoundFile { // convert to normal soundfile
		^SoundFile( path.getGPath.asPathFromServer )
			//.numFrames_( numFrames ? 0 )
		    .instVarPut( \numFrames,  numFrames ? 0 )
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
		var server;
		server = ULib.allServers.first;
		fluidBufNMF = 'FluidBufNMF'.asClass;
		outPath = outPath ?? { inPath.replaceExtension( extension.format( numComponents ) ); };
		bases = Buffer( server );
		buf = Buffer.read( server, inPath, action: {
			fluidBufNMF.processBlocking( server, buf,
				bases: bases,
				resynthMode: 0,
				components: numComponents,
				action: {
					OSCFunc({  |msg, time, addr, recvPort|
						action.value( outPath );
					}, '/done', server.addr, nil, ['/b_write', bases.bufnum ] ).oneShot;
					bases.write( outPath, "aiff", "float", completionMessage: { bases.freeMsg } );
					buf.free;
					//action.value( outPath );

				}
			);
		});
	}

	// mvc aware settersc

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
			path.formatGPath !? _.quote, numChannels, numFrames, sampleRate
		]  <<")"
	}
}

UFluidNMFBaseBufferSpec : RichBufferSpec {

	*new {
		^super.newCopyArgs().init;
	}

	*testObject { |obj|
		^obj.isKindOf( UFluidNMFBaseBuffer );
	}

	constrain { |value|
		^value.asUFluidNMFBaseBuffer;
	}

	default {
		^nil.asUFluidNMFBaseBuffer;
	}

	*newFromObject { |obj|
		^this.new( obj.numChannels );
	}

	viewNumLines { ^UFluidNMFBaseBufferView.viewNumLines }

	viewClass { ^UFluidNMFBaseBufferView }

	makeView { |parent, bounds, label, action, resize|
		var vws, view, labelWidth;

		vws = ();

		bounds.isNil.if{bounds= 350 @ (this.viewNumLines * 18) };

		#view, bounds = EZGui().prMakeMarginGap.prMakeView( parent, bounds );
		 vws[ \view ] = view;
		 view.addFlowLayout(0@0, 4@4);
		if( label.notNil ) {
			labelWidth = (RoundView.skin ? ()).labelWidth ? 80;
			vws[ \labelView ] = StaticText( vws[ \view ], labelWidth @ 14 )
				.string_( label.asString ++ " " )
				.align_( \right )
				.resize_( 4 )
				.applySkin( RoundView.skin );
		} {
			labelWidth = -4;
		};

		if( resize.notNil ) { vws[ \view ].resize = resize };

		vws[ \bufferView ] = this.viewClass.new( vws[ \view ],
			( bounds.width - (labelWidth+4) ) @ bounds.height, { |vw|
				action.value( vw, vw.value )
			} )

		^vws;
	}

	setView { |view, value, active = false|
		view[ \bufferView ].value = value;
		if( active ) { view.doAction };
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