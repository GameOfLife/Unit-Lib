+ WFSEvent {
	addToStartTime { |add = 0| startTime = startTime + add; }
	 
	flat { |startTimeOffset = 0, excludeMuted = false|
		if( this.isFolder )
			{ ^this.wfsSynth.flat( startTimeOffset + this.startTime, excludeMuted ); }
			{ ^[ this.copyNew.wfsSynth_( this.wfsSynth ).addToStartTime( startTimeOffset ) ]; };
		}
	
	}
	
+ WFSScore {

	flat { |startTimeOffset = 0, excludeMuted = false|
		if( excludeMuted )
			{ ^events.collect({ |event| 
				if( event.muted == false )
					{ event.flat( startTimeOffset ); }
					{ nil } }).flatten(1).select( _.notNil );  }
			{ ^events.collect({ |event| event.flat( startTimeOffset ); }).flatten(1); };

		}
	
	asFlatScore { |excludeMuted = false| 
		^this.class.new( *this.flat( 0, excludeMuted ) ).sort.name_( name )
			.clickTrackPath_( clickTrackPath ); }
	
	}