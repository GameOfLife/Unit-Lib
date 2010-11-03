// wfslib 2006  :  support for doing things from the lang

+ WFSEvent {

	performAll { |whatToPerform ... args|
		if( this.isFolder )
			{ ^wfsSynth.events.collect({ |item| item.perform( whatToPerform, *args ) }) }
			{ ^wfsSynth.perform( whatToPerform, *args ) ; }
		}
		
		
	position { ^this.performAll( \position ); }
	position_ { |newPos, changeDur = true| wfsSynth.position_( newPos, changeDur ); }
	
	filePath { ^this.performAll( \filePath ); }
	filePath_ { |newFilePath| wfsSynth.filePath = newFilePath; }
	
	pbRate { ^this.performAll( \pbRate ); }
	pbRate_ { |newPbRate| wfsSynth.pbRate = newPbRate; }
	
	startFrame { ^this.performAll( \startFrame ); }
	args { ^this.performAll( \args ); }
	level { ^this.performAll( \level ); }  
	
	events { if( this.isFolder ) { ^wfsSynth.events } { ^[] } }
	at { |index=0| if( this.isFolder ) { ^wfsSynth.at(index)  } { ^nil } }
	collect { |func|  if( this.isFolder ) {^wfsSynth.collect( func ); } { ^[] }  }
	do { |func| if( this.isFolder ) { wfsSynth.do( func ); } { func.value( this, 0 ) } }
	first { if( this.isFolder ) { ^wfsSynth.first } { ^this } }
	last { if( this.isFolder ) { ^wfsSynth.last } { ^this } }
	
	fadeInTime { ^this.performAll( \fadeInTime ) }
	fadeOutTime { ^this.performAll( \fadeOutTime ) }
	
	}
	
+ WFSSynth {
	position { ^wfsPath }
	position_ { |newPos, changeDur = true| this.wfsPath = newPos; }
	}
	