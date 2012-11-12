UMarker : UEvent {
	
	classvar <>defaultAction;
	
	var <name = "marker";
	var <>score; // set at playback from score
	var <action; 
	
	*initClass {
		defaultAction = { |marker, score| };
	}
	
	*new { |startTime = 0, track, name, action|
		^super.newCopyArgs
			.startTime_( startTime )
			.track_( track ? 0 )
			.name_( name ? "marker" )
			.action_( action ? defaultAction );
	}
	
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

	makeView{ |i,minWidth,maxWidth| ^UMarkerEventView(this,i,minWidth, maxWidth) }
	
	duplicate{
	    ^this.deepCopy;
	}
	
	storeArgs { ^[ startTime, track, name, if( action != defaultAction ) { action } ] }
}