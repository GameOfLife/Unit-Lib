+ WFSScore {
	allEvents {
		var list = [];
		
		this.events.do({ |item|
				if( item.wfsSynth.respondsTo( \allEvents ) )
					{ list = list.addAll( item.wfsSynth.allEvents ); }
					{ list = list.add( item ); }
				});
				
		^list;
		
		}
	}
