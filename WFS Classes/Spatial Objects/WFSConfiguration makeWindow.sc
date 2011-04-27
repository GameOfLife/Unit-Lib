+ WFSConfiguration {
	
	*makeWindow{
		var window, views, conf;
		var array, path;
		
		path = "/Library/Application Support/WFSCollider/WFSCollider_configuration.txt";
		
		{
		var file, dict;
		
		if( File.exists( path )  )
			{
			file = File( path, "r" );
			dict = file.readAllString.interpret;
			if( dict[ \speakConf ].notNil ) {
				array = dict[ \speakConf ];
			} {  
				array = [40, 56, 8.1, 10.95 ]; 
			};
			
			file.close;
			default = 
				WFSConfiguration.rect4( array[0], array[1], 0.165, array[2], array[3] );
			} { array = [40, 56, 8.1, 10.95 ] };
		}.value;
		
		window = Window( "WFSConfiguration", Rect(490, 551, 298, 100), false ).front;
		
		views = ();
		window.decorate;
		
		StaticText( window, 70@18 ).string_( "speakers H:" );
		views[ 'h' ] = SmoothNumberBox( window, 40@18 ).clipLo_( 0 ).clipHi_( 12 * 8 ).step_(8).scroll_step_(8).value_( array[0]);
		StaticText( window, 30@18 ).string_( " V:" );
		views[ 'v' ] =SmoothNumberBox( window, 40@18 ).step_(8).scroll_step_(8).clipLo_( 0 ).clipHi_( 12 * 8 ).value_( array[1] );
		
		views[ 'h' ].action_({ |vw| 
			views[ 'v' ].value = (views[ 'total' ].value / 2)  - views[ 'h' ].value;  
				views[ 'plot' ].value;  
			});
		
		views[ 'v' ].action_({ |vw| 
			views[ 'h' ].value = (views[ 'total' ].value / 2 )- views[ 'v' ].value; 
				views[ 'plot' ].value;   
			});
			
		StaticText( window, 40@18 ).string_( " total:" );
		views[ 'total' ] =SmoothNumberBox( window, 40@18 )
			.step_(16).clipLo_( 16 * 8 ).clipHi_( 24 * 8 ).value_( (array[0] + array[1]) * 2 );
		
		views[ 'total' ].action_({ |vw| 
			views[ 'h' ].value = (views[ 'total' ].value / 2) - views[ 'v' ].value;
			views[ 'plot' ].value;  
			});	
		
		window.view.decorator.nextLine;
		
		StaticText( window, 35@18 ).string_( "width:" );
		views[ 'x' ] = SmoothNumberBox( window, 40@18 ).step_(0.05).value_( array[2] )
			.action_({ 	views[ 'plot' ].value;  });
		StaticText( window, 65@18 ).string_( "m     height:" );
		views[ 'y' ] =SmoothNumberBox( window, 40@18 ).step_(0.05).value_( array[3] )
			.action_({ 	views[ 'plot' ].value;  });
		StaticText( window, 20@18 ).string_( "m" );
		
		window.view.decorator.nextLine;
		
		views[ 'gap' ] = StaticText( window, 260@18 ).string_( "corner gap width: %m"
			.format( "" ) );
			
		views[ 'wait' ] = WaitView( window, 18@18 );
		
		window.view.decorator.nextLine;
		
		views[ 'plot' ] = { 
			var conf;
			
			conf = WFSConfiguration.rect4( views['h'].value, views['v'].value, 0.165, 
					views['x'].value, views['y'].value );
			
			views[ 'gap' ].string =  "corner gap width: %m".format( 
				conf.speakerLines[0].last.dist( conf.speakerLines[1].first ).round(0.01) );
		
			if( views[ 'plotbutton' ].value == 1 ) { conf.plotSmooth; window.front; };
			conf;
			};
		
		SmoothButton( window, 80@18).states_([
			[ "cancel" ]])
			.action_({ |bt| window.close; });
		
		StaticText( window, 10@18 );
			
		views[ 'plotbutton' ] = SmoothButton( window, 80@18).states_([
			[ "plot" ], [ "plot", Color.gray(0.7), Color.black]])
			.value_( 0 ).action_({ |bt| views[ 'plot' ].value; });
		
		views[ 'usebutton' ] = SmoothButton( window, 100@25).states_([
			[ "use", Color.black, Color.blue( 0.5 ).alpha_(0.25) ]])
			.value_( 0 ).action_({ |bt| 
				var writeFunc, doneFunc;
				var done1 = false, done2 = false;
				
				writeFunc = {
					var localDict, remoteDicts, speakConf;
					
					
					localDict = (
						ips: ["192.168.2.11", "192.168.2.12"] , 
						hostnames: ["Game Of Life 1","Game Of Life 2"]
					);
					
					remoteDicts = [ ( 
						remoteWritten: true,
						hostname: "Game Of Life 1", 
						serverNumber: 0, 
						numberOfServers: 2,
						startPort:58000,
						soundCard: "JackRouter",
						scsynthsPerSystem:8 
					), (
						remoteWritten: true,
						hostname: "Game Of Life 2", 
						serverNumber: 1, 
						numberOfServers: 2,
						startPort:58000,
						soundCard: "JackRouter",
						scsynthsPerSystem:8 
					) ];
					
					speakConf = ( speakConf: [  views['h'].value, views['v'].value,
						   views['x'].value, views['y'].value ] );
						   
					
					File.checkDo( path, (localDict ++ speakConf).asCompileString, true, false, "w");
					
					(
						"ssh gameoflife@192.168.2.11 '" ++
						"echo % > %".format( 
							( remoteDicts[0] ++ speakConf ).asCompileString.escapeChar( $\" ).quote,
							path.escapeChar( $  ) ) ++
						"'"
					).unixCmd( { done1 = true; doneFunc.value; } );
					
					(
						"ssh gameoflife@192.168.2.12 '" ++
						"echo % > %".format( 
							( remoteDicts[1] ++ speakConf ).asCompileString.escapeChar( $\" ).quote,
							path.escapeChar( $  ) ) ++
						"'"
					).unixCmd( { done2 = true; doneFunc.value; } );
					   
					WFSConfiguration.default = views[ 'plot' ].value;
					views[ 'wait' ].start;
			    };
			    
			    doneFunc = {
				    if( done1 && done2 ) {	
					    views[ 'wait' ].stop;
						window.close;
						SCAlert(  "now reboot sc on servers", ["ok"], [nil]);
					};
			    };
				
				bt.enabled = false;
				
				writeFunc.value;		
				
		});
		
		views['plot'].value;
		
		
		window
		
	}
}