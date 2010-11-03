WFSExternalSyncCenter {
	
	classvar <>events, <>score;
	
	classvar <>postResults = false;
	
	classvar <>run = false;
	
	*initClass { events = []; }
	
	*start { |inScore|
		events = [];
		score = inScore;
		}
		
	*msgsAtTime { |time = 0|
		var currentEvents, startedEvents = [], stoppedEvents = [], msgs = [];
		if( score.notNil )
		{	
			currentEvents = score.eventsAtTime( time );
			
			events.do({ |event|
				if( currentEvents.includes( event ).not )
					{ stoppedEvents = stoppedEvents.add( event ); };
				});
			
			
			stoppedEvents.do({ |event|	
				var id;
				id = score.events.indexOf( event );
				//id = 0;
				msgs = msgs.add( event.extStopMsg( id ) );
				});
			
			
			events.removeAll( stoppedEvents );
			
			currentEvents.do({ |event|
				var isThere, id;
				isThere = events.includes( event );
				id = score.events.indexOf( event );
				//id = 0;
				if( isThere )
					{ msgs = msgs.add( event.extPosMsg( time, id ) ); }
					{ msgs = msgs.addAll( [ event.extStartMsg( id ), 
							event.extPosMsg( time, id, true ) ] );
					  startedEvents = startedEvents ++ [ event ]; 
					};
				});
				
			events = events.addAll( startedEvents );	
			
			^msgs.select(_.notNil);
			} { ^[]; }
		}
		
	*stop { |time| 
		var msgs;
		events.do({ |event|
			var id;
			id = score.events.indexOf( event );
			msgs = msgs.add( event.extStopMsg( id, time ) );
			});
		events = []; score = nil;
		^msgs; 
		}
		
	*sendMsgs { |time, addr|
		var msgs;
		if( run )
			{	msgs = this.msgsAtTime( time );
				addr = addr ?? { NetAddr( "192.168.2.13", 7004 ) };
				msgs.do({ |msg| addr.sendBundle( nil, msg ); });
				if( postResults ) { msgs.join(", ").postln; };
			};
		}
	
	}