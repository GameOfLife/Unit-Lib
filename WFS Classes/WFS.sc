WFS {
	
	//classvar <>debugMode = false; -> Meta_WFSPan2D:silent
	
	classvar <>graphicsMode = \fast;
	classvar <>scVersion = \new;
	classvar <>debugMode = false;
	
	classvar <>debugSMPTE;
	
	classvar <>syncWrap = 16777216; // == 2**24 == max resolution 32 bits float
	
	*initClass { debugSMPTE = SMPTE(0, 1000); }
	
	*debug { |string ... argsArray |
		if( debugMode )
			{ (string.asString ++ "\n").postf( *argsArray ); };
		}
		
	*secsToTimeCode { |secs = 0|
		^debugSMPTE.initSeconds( secs ).toString;
		}
	
	*startup { |mode = 'single', hostName|
		var server, serverCounter = 0;
		
		if( Buffer.respondsTo( \readChannel ).not )
			{ scVersion = \old };
		
		Server.default.options
			.numAudioBusChannels_(256)
			.numOutputBusChannels_(96)
			.numInputBusChannels_(20)
			.numWireBufs_(2048)
			.memSize_(2**19) // 256MB
			.hardwareBufferSize_(512)
			.blockSize_(128)
			.sampleRate_( 44100 )
			.blockAllocClass_( ContiguousBlockAllocator ); // better allocation?
		
		case { mode == 'single' }
			  { 	server = WFSServers.single.makeDefault; }
			{ mode == 'gameoflife' }
			  {	Server.default.options.device_( "MOTU 828mk2" );
			  	server = WFSServers( "192.168.2.11", "192.168.2.12"  ).makeDefault;
				server.hostNames_( "Game Of Life 1", "Game Of Life 2" );
				server.wfsConfigurations = 
					[ WFSConfiguration.halfRect2_1, WFSConfiguration.halfRect2_2 ];
				
				// live eq (todo: incorporate in software)
				WFSEQ.action = { |eq, label, val, desc|
						WFSServers.default.allServers
							.do({ |sv| sv.sendMsg( "/n_set", 1, label, val ); });
					 };
				// end life eq
				 } 
			{ mode == 'client' }
			  { 	Server.default.options.device_( "PCI-424" );
			     server = WFSServers.client.makeDefault;
			  	server.hostNames_( hostName );
			  	server.wfsConfigurations = [ WFSConfiguration.default ]; };
		
		//WFSConfiguration.default = WFSConfiguration.rect2;
		
		//server.singleWFSConfiguration = WFSConfiguration.stereo( 1.3, -90 ).useSwitch_( false );
		
		server.singleWFSConfiguration = WFSConfiguration.stereoLine;
		
		server.makeWindow;
		
		/*
		clientBootAction = {
				var i = 0;
				{  
				if( i == 3 )
					{ "\n\tWelcome to the WFS System".postln;
			    			// lifesign
							Server.internal.options.device = "Built-in Audio";
							Server.internal.waitForBoot({
								"\nplaying lifesign on internal server\n".postln;
								SystemClock.sched(0.5, { 
									{ SinOsc.ar( XLine.ar( 20, 5000, 0.5, 
											doneAction:2 ), 0, 1 ) }
										.play( Server.internal ); });
								SystemClock.sched( 3.5, { Server.internal.quit } );
							});
							};
					WFSSynthDef
						.allTypes(server.wfsConfigurations[0])
						.do({ |def| 
								if( i == 0 )
									{ def.writeDefFile( subServer ) }
									{ def.send( subServer ) };
									 });
							
					}
			};
		*/
		
		
		case { server.isSingle }
			{ server.m.waitForBoot({ 
					server.loadAllSync; 
					WFSSynthDef
						.allTypes( server.singleWFSConfiguration)
						.do({ |def| def.load( server.m ) });
					"\n\tWelcome to the WFS System".postln	
					}); }
			{ server.isMaster }
			{	server.m.waitForBoot({ 
					server.loadAllSync;
					"\n\tWelcome to the WFS System".postln; 
					}) }
			{ server.isClient }
			{	
					
				server.boot;
			     server.multiServers[0].servers.do({
			     	|subServer, i|
			     		subServer.waitForBoot({	
			     			var allTypes;
			     			
			     			if( serverCounter > 2 )									{ "\n\tWelcome to the WFS System".postln;
			     			
			     			allTypes = WFSSynthDef.allTypes(server.wfsConfigurations[0]);
			     			allTypes.do({ |def| def.def.writeDefFile });
			     			server.multiServers[0].servers.do({ |srv|
			     				allTypes.do({ |ddef|
			     					 srv.loadSynthDef( ddef.def.name );
			     					 });
			     				});
			     				
			    			// lifesign
							Server.internal.options.device = "Built-in Audio";
							Server.internal.waitForBoot({
								"\nplaying lifesign on internal server\n".postln;
								SystemClock.sched(0.5, { 
									{ SinOsc.ar( XLine.ar( 20, 5000, 0.5, 
											doneAction:2 ), 0, 1 ) }
										.play( Server.internal ); });
								SystemClock.sched( 3.5, { Server.internal.quit } );
							});
							} { serverCounter = serverCounter + 1 }
						/*
						WFSSynthDef
							.allTypes(server.wfsConfigurations[0])
							.do({ |def| 
								if( i == 0 )
									{ def.load( subServer ) }
									{ def.send( subServer ) };
									 });
						*/
							});
						});
					};

		
		^server; // returns an instance of WFSServers for assignment
		// best to be assigned to var 'm' in the intepreter
		}
	
	*startupFull { ^this.startup( 'gameoflife' ) }
	*startupClient { |hostName = "Game Of Life 1"| ^this.startup( 'client', hostName ) }
	
	*makeArchive { |version = "0.5b"| 
		WFS.filenameSymbol.asString.dirname.dirname
			.targz( "~/Desktop/WFS_Classes_v" ++ version );
		}

 }

/*

// marije baalman sketch WFS Class

WFS { 
 classvar <>soundVel = 340;
 var <>arrayPosList, <>refPoint;
 var <>rsp;
 
 *new { arg startPoint, deltaxy, numSpk=8,ref;
	
	 ^super.new.init(startPoint, deltaxy,numSpk, ref);
 }

init { arg start, delta, n, ref;
	refPoint = ref ? (0 @ 0).asComplex;
	start = start ? (-1.4375 @ 3).asComplex;
	delta = delta ? (0.125 @ 0).asComplex;
	arrayPosList = Array.fill(n, { arg i; delta*i+ start; } );
	rsp = (arrayPosList-refPoint).magnitude;
}
   
ar { arg in, xpos=0.1, ypos=0.1;
	 var delay,vol,r,rc,xypos,sign,cosphi,pr;

	xypos = Complex(xpos,ypos); // position as complex
	r = max(0.01,(arrayPosList-xypos).magnitude); // distance from speaker
	sign = (rc - rsp).sign; // front or back <- wrong
	rc = xypos.magnitude * sign; // positive or negative distance
	cosphi = -1*(  ypos - refPoint.imag )/r; // this should be dependent on normal to array
	delay = sign*r/soundVel + 0.01;
	pr = ( rc - r )/rc;   // positive distance
	vol = cosphi.abs* (pr.sqrt) / r.sqrt;
	^Out.ar(0, LPF.ar(DelayL.ar(in, 0.2, delay , vol/2), min((100000 / rc.abs), 20000)));
}

}
*/



