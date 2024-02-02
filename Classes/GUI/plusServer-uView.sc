+ Server {

	uInfoString {
		^"%\\% %\\%%s%d"
		.format(
			this.avgCPU !? { |x| x.asInteger.asString.padLeft(3) ++"."++ (x.frac * 10).floor.asInteger } ? "   avg",
			this.peakCPU !? { |x| x.asInteger.asString.padLeft(3) ++"."++ (x.frac * 10).floor.asInteger } ? "  peak",
			(this.numSynths ? "?").asString.padLeft(6),
			(this.numSynthDefs ? "?").asString.padLeft(6)
		);
	}

	uView { arg w, width = 386, useRoundButton = true, onColor;
		var active, booter, killer, makeDefault, running, booting, bundling, stopped;
		var recorder, scoper;
		var countsViews, ctlr;
		var dumping=false;
		var infoString, oldOnClose;
		var font;
		var cpuMeter, composite;

		width = width - 26;

		font = Font(Font.defaultSansFace, 11);
		onColor = onColor ? Color.green(0.5);

		if (window.notNil, { ^window.front });

		if(w.isNil,{
			w = window = GUI.window.new(name.asString ++ " server",
				Rect(10, named.values.indexOf(this) * 120 + 10, 390, 92));
			w.view.decorator = FlowLayout(w.view.bounds);
		});

		composite = CompositeView( w, (width + 22) @ 18 );
		composite.background = Color.gray(0.9);

		if(isLocal,{
			if( useRoundButton ) {
				booter = SmoothButton(composite, Rect(0,0, 18, 18))
				.canFocus_( false )
				.radius_(2);
				booter.states = [
					[ \power, Color.gray(0.2), Color.clear],
					[ \power, Color.gray(0.2), onColor.copy.alpha_(0.5) ]
				];
			} { booter = Button( composite, Rect(0,0,18,18));
				booter.states = [[ "B"],[ "Q", onColor ]];
				booter.font = font;
			};

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

		cpuMeter = LevelIndicator( composite, Rect( 22, 0, width, 18 ) )
		//.numTicks_( 9 ) // includes 0;
		//.numMajorTicks_( 5 )
		.meterColor_( onColor.copy.alpha_(0.25) )
		.criticalColor_( Color( 1,0.2,alpha: 0.4 ) )
		.warningColor_( Color.yellow( 1,1, alpha: 0.4 ) )
		.background_( Color.clear )
		.drawsPeak_( true )
		.warning_( 0.8 )
		.critical_( 1 );

		active = StaticText(composite, Rect(22,2, width, 16));
		active.string = " " ++ this.name.asString + this.uInfoString;
		active.align = \left;
		active.font = font;

		if(this.serverRunning,running,stopped);

		running = {
			{ active.stringColor_( onColor ); }.defer;
			if( isLocal ) { booter.value = 1; }
		};
		stopped = {
			{ active.stringColor_( Color.grey(0.2) ); }.defer;
			if( isLocal ) { booter.value = 0; }

		};
		booting = {
			{ active.stringColor_( Color(1.0, 0.5) ); }.defer;
		};

		bundling = {
			{ active.stringColor_(Color.new255(237, 157, 196)); }.defer;
			if( isLocal ) { booter.value = 1; }
		};

		active.onClose = {
			window = nil;
			ctlr.remove;
			if( isLocal.not ) {
				this.stopAliveThread;
			};
		};

		if(this.serverRunning,running,stopped);

		w.front;

		ctlr = SimpleController(this)
		.put(\serverRunning, {	if(this.serverRunning,running,stopped) })
		.put(\counts,{
			active.string = " " ++ this.name.asString + this.uInfoString;
			//infoString.string = this.uInfoString;
			cpuMeter !? {
				cpuMeter.value = this.avgCPU / 100;
				cpuMeter.peakLevel = this.peakCPU / 100;
			};
		});

		this.startAliveThread;
	}

	uViewHeight { ^22 }
}

+ LoadBalancer {

	addr { ^servers[0].addr }

	uView { arg w, width = 386, useRoundButton = true, onColor;
		var splitpos, srvs;
		splitpos = (servers.size / 2).ceil.asInteger;
		srvs = [ servers[..splitpos - 1], servers[ splitpos.. ] ].lace;
		if( servers.size > 1 && { servers.size.asInteger.odd }) {
			srvs = srvs.add( servers[ (servers.size/2).asInteger ] );
		};
		srvs.do({ |srv, i|
			srv.uView( w, (width / 2), useRoundButton, onColor );
			if( i.odd ) { w.asView.decorator.nextLine; }
		});
	}

	uViewHeight { ^((servers.size/2).ceil) * 22 }
}