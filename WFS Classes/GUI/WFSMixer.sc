WFSMixer{
	
	*new{ |events,parentEvents,rect|
		var spec, maxTrack,count, color, cview,w,level,bounds;
		maxTrack = events.collect{ |event| event.track }.maxItem + 1;
		count = 0;
		spec = [-90,12,\db].asSpec;
		rect.postln;
		w = Window.new(
			"mix - level "++parentEvents.size,
			Rect(if(rect.notNil){rect.left}{100},if(rect.notNil){rect.top}{100},4+42*(events.size)+if(parentEvents.size>0){50}{0},310).postln
		).front;
		w.view.decorator = FlowLayout(w.view.bounds);
		if(parentEvents.size != 0){
			cview = CompositeView(w.view,40@300);
			SmoothButton(cview,32@20).states_([["back"]])
						.radius_(3)
						.action_({
							var eventsForParentMixer;
							w.close;
							eventsForParentMixer = parentEvents.pop;
							WFSMixer(eventsForParentMixer,parentEvents,w.bounds);
						});
				
		};
		maxTrack.do{ |j| 
			events.do{ |event,i|
				var cview,faders, eventsFromFolder;
				
				if(event.track == j){
				color = Color.rand;
				if(event.wfsSynth.class == WFSSynth){
					cview = CompositeView(w.view,40@300);
					cview.decorator = FlowLayout(cview.bounds);
					cview.background_(Color(0.58208955223881, 0.70149253731343, 0.83582089552239, 1.0););
					RoundNumberBox(cview, 30@20)
						.value_(if(event.wfsSynth.fadeTimes.notNil){event.wfsSynth.fadeTimes[0]}{1})
						.action({ |v| 
							event.wfsSynth.fadeTimes[0] = v.value 
						});
					cview.decorator.nextLine;
					RoundNumberBox(cview, 30@20)
							.value_(if(event.wfsSynth.fadeTimes.notNil){event.wfsSynth.fadeTimes[1]}{1})
						.action({ |v| 
							event.wfsSynth.fadeTimes[1] = v.value
						});
					cview.decorator.nextLine;
					EZSmoothSlider.new(cview, Rect(0,0,32,200), events.indexOf(event), spec, layout:\vert)
						.value_(event.wfsSynth.level.ampdb)
						.action_({ |v|				
								event.wfsSynth.level = v.value.dbamp;
						});			
				}{
					eventsFromFolder = event.wfsSynth.allEvents.collect{ |event| (\event: event,\oldLevel: event.wfsSynth.level) };
					cview = CompositeView(w.view,40@300);
					cview.decorator = FlowLayout(cview.bounds);
					cview.background_(Color(0.28208955223881, 0.50149253731343, 0.23582089552239, 1.0););
					SmoothButton(cview,32@20).states_([["open"]])
						.radius_(3)
						.action_({
							w.close;
							parentEvents.add(events);
							WFSMixer(event.wfsSynth.events,parentEvents,w.bounds)
						});
					EZSmoothSlider.new(cview, Rect(0,0,32,260), events.indexOf(event), spec, layout:\vert)
						.value_(0)
						.action_({ |v|
							eventsFromFolder.do{ |dict|
								dict[\event].wfsSynth.level = dict[\oldLevel]*v.value.dbamp;
							};
						});
					}
				}
			}
		}
	}
}