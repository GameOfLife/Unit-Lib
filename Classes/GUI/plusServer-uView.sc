+ Server {
	
	uInfoString {
		^"CPU: %/%\tS/D: %/%"
			.format(
				this.avgCPU !? { |x| x.asInteger.asString ++"."++ (x.frac * 10).round(1).asInteger } ? "avg",  
				this.peakCPU !? { |x| x.asInteger.asString ++"."++ (x.frac * 10).round(1).asInteger } ? "peak", 
				this.numSynths ? "?", 
				this.numSynthDefs ? "?"
			);
	}
	
	uView { arg w, useRoundButton = true, onColor;
		var active, booter, killer, makeDefault, running, booting, bundling, stopped;
		var recorder, scoper;
		var countsViews, ctlr;
		var dumping=false;
		var infoString, oldOnClose;
		var font;
		var cpuMeter, composite;
		
		
		font = Font(Font.defaultSansFace, 10);
		onColor = onColor ? Color.new255(74, 120, 74);
		
		if (window.notNil, { ^window.front });
		
		if(w.isNil,{
			w = window = GUI.window.new(name.asString ++ " server", 
						Rect(10, named.values.indexOf(this) * 120 + 10, 320, 92));
			w.view.decorator = FlowLayout(w.view.bounds);
		});
		
		if(isLocal,{
			if( useRoundButton )
				{ booter = RoundButton(w, Rect(0,0, 18, 18)).canFocus_( false );
				  booter.states = [[ \power, Color.black, Color.clear],
						   		[ \power, Color.black, onColor]];
			 	}
				{ booter = Button( w, Rect(0,0,18,18));
				 booter.states = [[ "B"],[ "Q", onColor ]];
				 booter.font = font; };
						
			booter.action = { arg view; 
				if(view.value == 1, {
					booting.value;
					this.boot;
				});
				if(view.value == 0,{
					this.quit;
				});
			};
			booter.value = this.serverRunning.binaryValue;
		});
		
		active = StaticText(w, Rect(0,0, 78, 18));
		active.string = this.name.asString;
		active.align = \center;
		active.font = GUI.font.new("Helvetica-Bold", 12);
		active.background = Color.white;
		if(this.serverRunning,running,stopped);	
		
		/*
		w.view.keyDownAction = { arg ascii, char;
			var startDump, stopDump, stillRunning;
			
			case 
			{char === $n} { this.queryAllNodes }
			{char === $ } { if(this.serverRunning.not) { this.boot } }
			{char === $s and: {this.inProcess}} { this.scope }
			{char == $d} {
				if(this.isLocal or: { this.inProcess }) {
					stillRunning = {
						SystemClock.sched(0.2, { this.stopAliveThread });
					};
					startDump = { 
						this.dumpOSC(1);
						this.stopAliveThread;
						dumping = true;
						CmdPeriod.add(stillRunning);
					};
					stopDump = {
						this.dumpOSC(0);
						this.startAliveThread;
						dumping = false;
						CmdPeriod.remove(stillRunning);
					};
					if(dumping, stopDump, startDump)
				} {
					"cannot dump a remote server's messages".inform
				}
			
			};
		};
		*/
		
		if (isLocal, {
			
			running = {
				active.stringColor_( onColor );
				booter.value = 1;
			};
			stopped = {
				active.stringColor_(Color.grey(0.3));
				booter.value = 0;

			};
			booting = {
				active.stringColor_( Color.new255(255, 140, 0) );
			};
			
			bundling = {
				active.stringColor_(Color.new255(237, 157, 196));
				booter.value = 1;
			};
			
			oldOnClose = w.onClose.copy;
			w.onClose = {
				oldOnClose.value;
				window = nil;
				ctlr.remove;
			};
		},{	
			running = {
				active.background = onColor
			};
			stopped = {
				active.background = Color.white;

			};
			booting = {
				active.background = Color.yellow;
			};
			
			oldOnClose = w.onClose.copy;
			w.onClose = {
				// but do not remove other responders
				
				oldOnClose.value;
				this.stopAliveThread;
				ctlr.remove;
			};
		});
		if(this.serverRunning,running,stopped);
		
		composite = CompositeView( w, 200@18 );
		infoString = GUI.staticText.new(composite, Rect(50,0, 200, 18));
		infoString.string = this.uInfoString;
		infoString.font_( font );
		
		cpuMeter = LevelIndicator( composite, 46@18 )
			//.numTicks_( 9 ) // includes 0;
			//.numMajorTicks_( 5 )
			
			.drawsPeak_( true )
			.warning_( 0.8 )
			.critical_( 1 );
		
		w.view.decorator.nextLine;
		
		w.front;

		ctlr = SimpleController(this)
			.put(\serverRunning, {	if(this.serverRunning,running,stopped) })
			.put(\counts,{ 
				infoString.string = this.uInfoString;
				cpuMeter !? { cpuMeter.value = this.avgCPU / 100; cpuMeter.peakLevel = this.peakCPU / 100; };
			});
			
		this.startAliveThread;
	}
}