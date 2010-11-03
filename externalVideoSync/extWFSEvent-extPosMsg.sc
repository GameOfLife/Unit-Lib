+ WFSEvent {

	extStartMsg {  |id=0|
		^[ "/wfs", "/start", startTime, "/type", wfsSynth.intType, "/id", id ];
		}
		
	extStopMsg {  |id=0, time|
		^[ "/wfs", "/stop", time ? this.endTime, "/id", id  ];
		}
		
	extPosMsg {  |time=0, id=0, first = false|
		var pos;
		if( [\static, \plane, \index ].includes( wfsSynth.intType ) && { first == false })
			{ ^nil }
			{    
			case { wfsSynth.wfsPath.class == WFSPath; }
				{ pos = wfsSynth.wfsPath.atTime2( time - startTime, 'hermite', false );
					^[ "/wfs","/point", pos.x, pos.y, time, "/id", id  ]; }
				
				{ wfsSynth.wfsPath.class == WFSPoint; }
				{^[ "/wfs", "/point", wfsSynth.wfsPath.x, wfsSynth.wfsPath.y, time, "/id", id]; }
				
				{ wfsSynth.wfsPath.class == WFSPlane; }
				{^[ "/wfs", "/plane", wfsSynth.wfsPath.angle, wfsSynth.wfsPath.distance,
					 time, "/id", id  ]; }
				
				{ wfsSynth.wfsPath.isNumber; }
				{^[ "/wfs", "/index", wfsSynth.wfsPath, time, "/id", id  ]; }
					
				{ true }
				{ ^nil }; 
			};
		}
	
	}
	
+ WFSScore {
	eventsAtTime { |time = 0|
		^events.select({ |event| time.inclusivelyBetween( event.startTime, event.endTime ); });
		}
	}