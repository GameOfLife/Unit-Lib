UMarker : UEvent {
	
	classvar <>defaultAction;
	classvar <>presetManager;
	
	var <name = "marker";
	var <>score; // set at playback from score
	var <action; 
	var <notes;
	
	*initClass {
		
		Class.initClassTree( PresetManager );
		
		presetManager = PresetManager( this, [ \default, { UMarker() } ] )
			.getFunc_({ |obj| obj.deepCopy })
			.applyFunc_({ |object, preset|
			 	object.fromObject( preset );
		 	});
		 	
		 presetManager.put( \pause, UMarker( 0,0, "pause", { |marker, score| 
	// pause the score
	score.pause; 
}) );
		 presetManager.put( \post, UMarker( 0,0, "post", { |marker, score| 
	// post the name of the current marker
	"passed marker '%' at %\n".postf( 
		marker.name, 
		score.pos.asSMPTEString(1000) 
	); 
}) );
		 presetManager.put( \jump_2s, UMarker( 0,0, "jump_2s", { |marker, score|
	// jump 2 seconds ahead 
	score.stop;
	score.pos = score.pos + 2;
	score.prepareAndStart( startPos: score.pos );
}) );

		presetManager.put( \jump_to_prev, UMarker( 0,0, "jump_to_prev", { |marker, score| 
	// jump to the previous marker and play (basic looping)
	score.stop;
	score.toPrevMarker;
	score.prepareAndStart( startPos: score.pos );
}) );

		defaultAction = { |marker, score| };
	}
	
	*new { |startTime = 0, track, name, action|
		^super.newCopyArgs
			.startTime_( startTime )
			.track_( track ? 0 )
			.name_( name ? "marker" )
			.action_( action ? defaultAction );
	}
	
	fromObject { |obj|
		this.name = obj.name;
		this.action = obj.action; // only copy the action from presets (perhaps more later)
	}
	
	*fromObject { |obj|
		^obj.value.deepCopy;
	}
	
	*fromPreset { |name| ^presetManager.apply( name ) }
	
	fromPreset { |name| ^presetManager.apply( name, this ); }
	
	start { |target, startPos = 0, latency| 
		if( startPos == 0 ) { action.value( this, this.score ); }
	}
	
	prepare { |target, startPos = 0, action| action.value( this ) }
	prepareAndStart{ |target, startPos = 0| this.start( target, startPos ); }
	waitTime { ^0 }
	prepareWaitAndStart { |target, startPos = 0| this.start( target, startPos ); }
	eventSustain{ ^inf }
	preparedServers {^[] }
	getAllUChains { ^[] }
	
	stop { }
	release { }
	
	duration { ^0 }
	duration_{ }
     dur_ { }
    
    	name_ { |x| name = x; this.changed(\name, name) }
    	action_ { |x| action = x; this.changed(\action, action) }
    	notes_ { |x| notes = x; this.changed(\notes, notes) }

	makeView{ |i,minWidth,maxWidth| ^UMarkerEventView(this,i,minWidth, maxWidth) }
	
	duplicate{
	    ^this.deepCopy;
	}
	
	storeArgs { ^[ startTime, track, name, if( action != defaultAction ) { action } ] }
	
	storeNotes { |stream|
		if( this.notes.notNil ) {
			stream << ".notes_(" <<< this.notes << ")";
		};
	}

	storeModifiersOn { |stream|
		this.storeNotes( stream );
		this.storeTags( stream );
		this.storeDisplayColor( stream );
	}
}