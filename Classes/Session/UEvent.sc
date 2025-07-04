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

UEvent : UArchivable {

	classvar >renderNumChannels = 2;
	classvar <>renderMaxTime = 60;
	classvar <>renderHeaderFormat = "WAV";
	classvar <>renderSampleFormat = "int24";
	classvar <>nrtMode = false;
	classvar <>nrtStartBundle;

    var <startTime=0;
    var <track=0;  //track number (horizontal segment) on the score editor
    var <duration = inf;
    var <>disabled = false;
    var <releaseSelf = true;
    var <oscSetter;
    var <displayColor;
    var <lockStartTime = false;
	var <>hideInGUI = false;
	var <allowPause = false;

    /*
    If 'releaseSelf' is set to false, then uchains will not free themselves automatically when the events stop playing.
    If 'releaseSelf' is set to true, then uchains will free themselves even if the score is paused;
	*/

    // event duration of non-inf will cause the event to release its chain
    // instead of the chain releasing itself

	waitTime { this.subclassResponsibility(thisMethod) }
	prepareTime { ^startTime - this.waitTime } // time to start preparing

	<= { |that|
		^case { this.prepareTime == that.prepareTime } {
			this.track <= that.track;
		} {
			this.prepareTime < that.prepareTime
		};
	} // sort support

	== { |that| // use === for identity
		^this.compareObject(that);
	}

	dur { ^this.duration }
	isFinite{ ^this.duration < inf}

    duration_{ this.subclassResponsibility(thisMethod) }
    isPausable_{ this.subclassResponsibility(thisMethod) }

    finiteDuration { |addInf = 10|
	    if( this.isFinite ) { ^this.duration } { ^addInf };
    }

    allowPause_ { |bool = false|
	    if( bool == true ) {
		    "allowPause_( true ) not (yet) supported for %\n".postf( this.class );
		};
    }

    resume { }

    renderNumChannels { ^renderNumChannels.value }

    track_ { |newTrack = 0| track = newTrack; this.changed( \track ) }

    startTime_ { |newTime|
	   if( lockStartTime.not ) { startTime = newTime; };
	   this.changed( \startTime )
    }

    lockStartTime_ { |bool = false|
	    lockStartTime = bool;
	    this.changed( \lockStartTime );
    }

    score { ^nil }
    score_ { }

    useNRT { |func|
	    if( nrtMode == true ) {
		    func.value
	    } {
		    if( SyncCenter.mode === \sample ) {
			    SyncCenter.mode = \nrt;
		    };
		    nrtMode = true;
		    func.value;
		    nrtMode = false;
		    if( SyncCenter.mode === \nrt ) {
			    SyncCenter.mode = \sample;
		    };
	    };
    }

    isFolder { ^false }

    endTime { ^startTime + this.duration; } // may be inf
    eventEndTime { ^startTime + this.eventSustain }

	disable { this.disabled_(true) }
	enable { this.disabled_(false) }
	toggleDisable { this.disabled_(disabled.not) }
	toggleLockStartTime { this.lockStartTime_( lockStartTime.not ) }

	displayColor_ { |color|
		if( color.isKindOf( Color ) ) { color = color.copy };
		displayColor = color;
		this.changed( \displayColor );
	}

	getTypeColor {
		^this.displayColor ?? {
			if(this.duration == inf) {
				Color(0.33, 0.33, 0.665)
			}{
				Color.white
			};
		};
	}

	connect { }
	disconnect { }

    /*
    *   server: Server or Array[Server]
    */
    play { |server| // plays a single event plus waittime
        ("preparing "++this).postln;
        this.prepare(server);
        fork{
            this.waitTime.wait;
            ("playing "++this).postln;
            this.start(server);
            if( duration != inf ) {
	           this.eventSustain.wait;
	           this.release;
            };
        }
    }

    asScore { |duration, timeOffset=0|

	    if( duration.isNil ) {
		    duration = this.finiteDuration;
	    };

	    ^Score(
			[ [ 0 ] ++ nrtStartBundle ] ++
			this.collectOSCBundles( ULib.allServers.first, timeOffset, duration  )
	    			++ [ [ duration, [ \c_set, 0,0 ] ] ]
	    	);
    }

    collectOSCBundles { ^[] }
    collectOSCBundleFuncs { ^[] }

    render { // standalone app friendly version
		arg path, maxTime=60, sampleRate = 44100,
			headerFormat, sampleFormat, options, inputFilePath, action;

		var file, oscFilePath, score, oldpgm;
		headerFormat = headerFormat ? this.class.renderHeaderFormat;
		sampleFormat = sampleFormat ? this.class.renderSampleFormat;
		oldpgm = Score.program;
		Score.program = Server.program;
		oscFilePath = Platform.defaultTempDir +/+ "temp_oscscore" ++ UniqueID.next;
		score = this.asScore(maxTime);
		score.recordNRT(
			oscFilePath, path, inputFilePath, sampleRate, headerFormat, sampleFormat,
			options, action: { |res|
				File.delete( oscFilePath );
				action.value(res, path);
			};
		);
		Score.program = oldpgm;
    }

    writeAudioFile { |path, maxTime, action, headerFormat, sampleFormat numChannels|
		var o;

		headerFormat = headerFormat ?  this.class.renderHeaderFormat;
		sampleFormat = sampleFormat ? this.class.renderSampleFormat;

		if( this.isFinite.not && { maxTime == nil } ) {
			maxTime = this.finiteDuration + 60;
		};

		numChannels = numChannels ? this.renderNumChannels ? 2;

		if( numChannels > 0 ) {
			o = ServerOptions.new
				.numOutputBusChannels_( numChannels ? this.renderNumChannels ? 2)
				.memSize_( 2**19 )
			    .numWireBufs_(2048)
				.maxSynthDefs_(4096);
			path = path.replaceExtension( headerFormat.toLower );
			this.render(
				path,
				maxTime, // explicit nil forces UScore to use score duration
				sampleRate: ULib.servers.first.sampleRate,
				headerFormat: headerFormat,
				sampleFormat: sampleFormat,
				options: o,
				action: action
			);
		} {
			"%:writeAudioFile - no audio file written; numChannels needs to be > 0\n"
				.postf( this.class );
			action.value;
		};
    }

    // tag system: for UScores and UChains
	setTag { |tag| UTagSystem.add( this, tag ); }

	removeTag { |tag| UTagSystem.remove( this, tag ); }

	clearTags { UTagSystem.removeObject( this ); }
	deepClearTags { UTagSystem.removeObject( this ); }

	tags { ^UTagSystem.getTags( this ); }

	tags_ { |tags|
		UTagSystem.removeObject( this );
		tags.asCollection.do({ |tag|
			UTagSystem.add( this, tag );
		});
	}

	storeTags { |stream|
		var tags;
		tags = this.tags;
		if( this.tags.size > 0 ) {
			stream << ".tags_(" <<<* tags.asArray.sort << ")";
		};
	}

	storeDisplayColor { |stream|
		if( this.displayColor.notNil ) {
			stream << ".displayColor_(" <<< this.displayColor << ")";
		};
	}

	storeDisabledStateOn { |stream|
		if( this.disabled == true ) {
			stream << ".disabled_(true)";
		};
	}

	storeModifiersOn { |stream|
		this.storeTags( stream );
		this.storeDisplayColor( stream );
		this.storeDisabledStateOn( stream );
	}

	makeView { |i=0, minWidth, maxWidth| ^UEventView( this, i, minWidth, maxWidth ) }

	selectUMaps { ^[] }

	setUMapsActive { |active = true, verbose = true|
		var umaps;

		umaps = this.selectUMaps({ |umap|
			umap.keys.any(_ == \active);
		});

		umaps.do( _.set( \active, active ) );

		if( verbose ) {
			"% UMaps set to 'active' = %\n".postf( umaps.size, active );
		};
	}

	deactivateUMaps { |verbose = true|
		var umaps;

		umaps = this.selectUMaps({ |umap|
			umap.keys.any(_ == \active) && { umap.active == true }
		});

		umaps.do( _.deactivate );

		if( verbose && { umaps.size > 0 } ) {
			"% UMap% deactivated for %\n".postf(
				umaps.size,
				if( umaps.size > 1 ) { "s" } { "" },
				this
			);
		}
	}

	//// UOSCsetter support
	oscSetter_ { |newOSCsetter, removeOld = true|
		if( removeOld ) {
			if( oscSetter.notNil && { (oscSetter === newOSCsetter).not } ) {
				oscSetter.remove;
			};
		};
		oscSetter = newOSCsetter;
		this.changed( \oscSetter );
	}

	enableOSC { |name, enableCurrent = true|
		this.oscSetter = UOSCsetter( this, name );
		UOSCSetterCurrent.enable;
	}

	disableOSC { |removeCurrent = true|
		this.oscSetter = nil;
		UOSCSetterCurrent.cleanup;
	}

	listOSCMessages {
	}

}