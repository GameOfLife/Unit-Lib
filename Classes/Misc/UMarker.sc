UMarker : UEvent {
	
	var <name = "marker";
	var <>score; // set at playback from score
	var <action; 
	
	*new { |startTime = 0, name, action|
		^super.newCopyArgs
			.startTime_( startTime )
			.name_( name ? "marker" )
			.action_( action );
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
	
	stop { }
	release { }
	
	duration { ^0 }
	duration_{ }
     dur_ { }
    
    	name_ { |x| name = x; this.changed(\name) }
    	action_ { |x| action = x; this.changed(\action) }

	makeView{ |i,minWidth,maxWidth| ^UMarkerEventView(this,i,minWidth, maxWidth) }
	
	duplicate{
	    ^this.deepCopy;
	}
}