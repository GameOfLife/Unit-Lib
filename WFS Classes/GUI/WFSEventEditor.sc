WFSEventEditor {

	classvar <>current;
	classvar <>editAction;
	
	var <event, <parent; // parent is an editor if available
	var <window, <views, <wfsPathBackup, <wfsPointBackup, <wfsPlaneBackup, <wfsIndexBackup;
	
	*initClass {
		editAction = { |event, what, value|
			//"currently edited event: changed % to %\n".postf( what, value );
			if( current.parent.notNil )
				{ current.parent.update  }
				{ if( WFSScoreEditor.current.notNil )
					{  WFSScoreEditor.current.update; }; };
			};
		}
	
	*new { arg event, leftTop, closeOldWindow = true, parent;
		^super.newCopyArgs( event.asWFSEvent, parent ).newWindow( closeOldWindow, leftTop );
		}
	
	update { 
		if( window.notNil && { window.dataptr.notNil } )
			{ 	views[ \startTime ].pos = event.startTime;
				views[ \duration ].value = event.dur;
				if( event.muted )
					{ window.view.background = Color.red(0.25).alpha_(0.33);  }
					{ window.view.background = nil;  };
				if( event.isFolder.not ) {
					views[ \buf ][ \dur ].value =  event.wfsSynth.soundFileDur;
					views[ \fadeIn ].value =  event.wfsSynth.fadeInTime;
					views[ \fadeOut ].value =  event.wfsSynth.fadeOutTime;
					views[ \prefServer ].value =  (event.wfsSynth.prefServer ? -1) + 1;
					views[ \intType ].value_(
						views[ \intType ].items.indexOf( event.wfsSynth.intType ) )
							.doAction;
					views[ \audioType ].value_(
						views[ \audioType ].items.indexOf( event.wfsSynth.audioType ) )
							.doAction;
					views[ \linear ][ \name ].string = { event.wfsSynth.wfsPath.name }.try;
					views[ \linear ][ \switch ].value = event.wfsSynth.useSwitch.binaryValue;
					views[ \cubic ][ \name ].string = { event.wfsSynth.wfsPath.name }.try;
					views[ \cubic ][ \switch ].value = event.wfsSynth.useSwitch.binaryValue;
					views[ \buf ][ \startFrameMode ].action.value( 
						views[ \buf ][ \startFrameMode ] );
					views[ \disk ][ \startFrameMode ].action.value( 
						views[ \disk ][ \startFrameMode ] );
					};
		
				
			};
		}
		
	newWindow { |closeOldWindow = true, leftTop|
		var windowBounds;
		var timeBoxes = [], typeSwitches, composite;
		var intTypeBounds = Rect(4, 102 + 24, 190, 54 );
		var audioTypeBounds = Rect(4, 160 + 24, 190, 170 );
		var intTypeViews = ();
		var audioTypeViews = ();
		
		views = ();
		
		leftTop = (leftTop ?? { (32 + 20.rand2)@(300 + 20.rand2) }).asPoint;
		windowBounds = Rect( leftTop.x, leftTop.y, 200, 364 );
		
		if( closeOldWindow )
			{ if( this.class.current.notNil )
				{  if(  this.class.current.window.notNil && 
						{ this.class.current.window.dataptr.notNil } )
					 {  	windowBounds = this.class.current.window.bounds;
					 	this.class.current.window.close };
				};
			};
			
		this.class.current = this;
		
		window = SCWindow( "WFSEventEditor" ++ ( ( parent !? 
			{ " (" ++ parent.id ++ ")" } ) ? ""), windowBounds, false ).front; 
			
		//window.view.decorator = FlowLayout( window.view.bounds );
								
		composite = SCCompositeView( window, Rect(4,4,190,94 + 24) )
			.background_( Color.white.alpha_(0.25) );
		
		composite.decorator = FlowLayout( composite.bounds );
		
		/*
		// startTime
		SCStaticText( composite, 60@20	).string_( "startTime" );
		views[ \startTime ] = ScrollingNBox( composite, 40@20 )
			.value_( event.startTime )
			.step_( 0.1 )
			.action_({ |box|	
					box.value = box.value.round( 10e-12 );
					if( box.value < 0 ) { box.value = 0 };
					event.startTime = box.value; 
					editAction.value( event, \startTime, box.value );
				});
		*/
		
		// startTime
		SCStaticText( composite, 80@20	).string_( "startTime" ).align_( \right );
		views[ \startTime ] =  SMPTEView( window, 94@11 )
			.background_( Color.white )
			.fontColor_( Color.black )
			.fontSize_( 13 )
			.fontName_( "Monaco" )
			.value_( event.startTime )
			.action_({ |box|	
					//box.value = box.value.round( 10e-12 );
					//if( box.value < 0 ) { box.value = 0 };
					event.startTime = box.value; 
					editAction.value( event, \startTime, box.value );
				});
		
		composite.decorator.nextLine;
		
		// duration
		SCStaticText( composite, 30@20	).string_( "dur" ).align_( \right );
		views[ \duration ] = ScrollingNBox( composite, 40@20 )
			.value_( event.dur )
			.step_( 0.1 )
			//.enabled_( false )
			.action_({ |box|
					
					if( box.value < 0 ) { box.value = 0 };
					event.dur = box.value; 
					editAction.value( event, \duration, box.value );
				});
				
				
		
		if( event.isFolder ) ////// is folder
			{ composite.decorator.nextLine;
				SCStaticText( composite, 30@20	).string_( "type" );
				SCStaticText( composite, 102@20 ).string_( "folder" )
					.font_( Font( 'Helvetica-Bold', 12 ) ).align_( \center ); 
				SCButton( composite, 40@20 ).states_( [["open"]] )
					.action_({ event.wfsSynth.edit( parent );  });
				views[ \duration ].enabled_( false );
				}
			{		/////// is no folder
		
		// level
		SCStaticText( composite, 60@20	).string_( "level (dB)" ).align_( \right );
		views[ \level ] = ScrollingNBox( composite, 40@20 )
			.value_( event.wfsSynth.level.ampdb )
			.step_( 1 )
			.action_({ |box|	
					//box.value = box.value.round( 10e-12 );
					//if( box.value < 0 ) { box.value = 0 };
					event.wfsSynth.level = box.value.dbamp; 
					editAction.value( event, \level, box.value.dbamp );
				});
		
		// fade in/out			
		composite.decorator.nextLine;
		
		SCStaticText( composite, 94@20	).string_( "fadetime in/out" ).align_( \right );
		views[ \fadeIn ] = ScrollingNBox( composite, 40@20 )
			.value_( 0 ).step_( 0.1 ).clipLo_( 0 )
			.action_({ |box|
				event.wfsSynth.fadeInTime = box.value;
				editAction.value( event, \fadeInTime, box.value.dbamp );
				});
	
		views[ \fadeOut ] = ScrollingNBox( composite, 40@20 )
			.value_( 0 ).step_( 0.1 ).clipLo_( 0 )
			.action_({ |box|
				event.wfsSynth.fadeOutTime = box.value;
				editAction.value( event, \fadeOutTime, box.value.dbamp );
				});

		composite.decorator.nextLine;
		
		// server
		
		SCStaticText( composite, 74@20	).string_( "play on" ).align_( \right );
		views[ \prefServer ] = SCPopUpMenu( composite, 104@20 )
			.items_( [ "both servers", "server 1", "server 2" ] )
			.value_( (event.wfsSynth.prefServer ? -1) + 1)
			.action_({ |box|
				event.wfsSynth.prefServer = [nil, 0, 1][ box.value ];
				editAction.value( event, \prefServer, [nil, 0, 1][ box.value ] );
				});

		
		// audioType
		SCStaticText( composite, 30@20 ).string_( "type" ).align_( \right );
		
		views[ \intType ] = SCPopUpMenu( composite, 90@20 )
				.items_( WFSSynthDef.validIntTypes.select({ |item| item != \switch }) )
				.value_( WFSSynthDef.validIntTypes.indexOf( 
							event.wfsSynth.wfsDefName.wfsIntType ) );
							
		views[ \audioType ] = SCPopUpMenu( composite, 54@20 )
				.items_( WFSSynthDef.validAudioTypes )
				.value_( WFSSynthDef.validAudioTypes.indexOf( 
							event.wfsSynth.wfsDefName.wfsAudioType ) )
				.action_({ |box| editAction.value( event, \audioType, box.items[ box.value ] );
					});			
		
		intTypeViews = (
				'linear': SCCompositeView( window, intTypeBounds )
					.background_( Color.black.alpha_(0.25) )
					.decorator_( FlowLayout( intTypeBounds ) )
					.visible_( false ),
				'cubic': SCCompositeView( window, intTypeBounds )
					.background_( Color.black.alpha_(0.25) )
					.decorator_( FlowLayout( intTypeBounds ) )
					.visible_( false ),
				'static': SCCompositeView( window, intTypeBounds )
					.background_( Color.black.alpha_(0.25) )
					.decorator_( FlowLayout( intTypeBounds ) )
					.visible_( false ),
				'plane': SCCompositeView( window, intTypeBounds )
					.background_( Color.black.alpha_(0.25) )
					.decorator_( FlowLayout( intTypeBounds ) )
					.visible_( false ),
				'index': SCCompositeView( window, intTypeBounds )
					.background_( Color.black.alpha_(0.25) )
					.decorator_( FlowLayout( intTypeBounds ) )
					.visible_( false )  );
		
		intTypeViews[ 'switch' ] = intTypeViews[ 'linear' ];
					
		// linear:
		SCStaticText( intTypeViews.linear, 30@20 ).string_( "path" );
		
		views[ \linear ] = ();
		
		views[ \linear ][ \name ] = SCStaticText( intTypeViews.linear, 135@20 )
			.string_( { event.wfsSynth.wfsPath.name; }.try )
			.font_( Font( "Monaco", 9 ) );
			
		SCButton( intTypeViews.linear, 30@20 )
			.states_( [ [ "edit", Color.black, Color.clear ] ] )
			.action_({ |button| 
				if( event.wfsSynth.wfsPath.notNil )
					{ event.wfsSynth.wfsPath.edit; }
					{ WFSPathEditor.new; };
				});
			
		SCButton( intTypeViews.linear, 68@20 )
			.states_( [ [ "from editor", Color.black, Color.clear ] ] )
			.action_({ |button| 
				event.wfsSynth.wfsPath = 
					WFSPathEditor.current ? event.wfsSynth.wfsPath;
				this.update;
				});
				
		SCButton( intTypeViews.linear, 30@20 )
			.states_( [ [ "plot", Color.black, Color.clear ] ] )
			.action_({ event.wfsSynth.wfsPath.currentTime_( 0 ).plotSmooth; });
		
		views[ \linear ][ \switch ] = RoundButton( intTypeViews.linear, 40@20 )
			.states_( [[ "switch", Color.black, Color.clear ], 
					[ "switch", Color.gray, Color.black ]] )
			.value_( event.wfsSynth.useSwitch.binaryValue )
			.radius_( 0 ).border_( 1 )
			.action_( { |bt|
				 views[ \cubic ][ \switch ].value = bt.value;
				 if( bt.value == 0 )
							{ event.wfsSynth.useSwitch = false }
							{ event.wfsSynth.useSwitch = true };
						});
			
					
		// cubic:
		SCStaticText( intTypeViews.cubic, 30@20 ).string_( "path" );
		
		views[ \cubic ] = ();
		
		views[ \cubic ][ \name ] = SCStaticText( intTypeViews.cubic, 135@20 )
			.string_( { event.wfsSynth.wfsPath.name; }.try )
			.font_( Font( "Monaco", 9 ) );
			
		SCButton( intTypeViews.cubic, 30@20 )
			.states_( [ [ "edit", Color.black, Color.clear ] ] )
			.action_({ |button| event.wfsSynth.wfsPath.edit; });
			
		SCButton( intTypeViews.cubic, 68@20 )
			.states_( [ [ "from editor", Color.black, Color.clear ] ] )
			.action_({ |button| 
				event.wfsSynth.wfsPath = 
					WFSPathEditor.current ? event.wfsSynth.wfsPath;
				this.update;
				});
				
		SCButton( intTypeViews.cubic, 30@20 )
			.states_( [ [ "plot", Color.black, Color.clear ] ] )
			.action_({ event.wfsSynth.wfsPath.currentTime_( 0 ).plotSmooth; });
			
		views[ \cubic ][ \switch ] = RoundButton( intTypeViews.cubic, 40@20 )
			.states_( [[ "switch", Color.black, Color.clear ], 
					[ "switch", Color.gray, Color.black ]] )
			.value_( event.wfsSynth.useSwitch.binaryValue )
			.radius_( 0 ).border_( 1 )
			.action_( { |bt| 
				 views[ \linear ][ \switch ].value = bt.value;
				if( bt.value == 0 )
							{ event.wfsSynth.useSwitch = false }
							{ event.wfsSynth.useSwitch = true };
						});
		
		// static:
		SCStaticText( intTypeViews.static, 80@20 ).string_( "position (x/y)" );
		
		views[ \static ] = ();
		
		views[ \static ][ \x ] = ScrollingNBox( intTypeViews.static, 30@20 )
			.value_( event.x ? 0 ).action_({ 
				/* if( event.wfsSynth.wfsPath.class == WFSPath )
					{ wfsPathBackup = event.wfsSynth.wfsPath }; */
				event.wfsSynth.wfsPath_( WFSPoint(
					views[ \static ][ \x ].value, views[ \static ][ \y ].value, 0 ),
					false );
				if( WFSPlotSmooth.isOpen ) 
					{ event.wfsSynth.wfsPath.plotSmooth( toFront: false ) };
			});
					
		views[ \static ][ \y ] = ScrollingNBox( intTypeViews.static, 30@20 )
			.value_( event.y ? 0 ).action_( views[ \static ][ \x ].action );	
		SCButton( intTypeViews.static, 30@20 )
			.states_( [ [ "plot", Color.black, Color.clear ] ] )
			.action_({ event.wfsSynth.wfsPath.plotSmooth; });
			
		intTypeViews.static.decorator.nextLine;
		
		RoundButton( intTypeViews.static, 60@16 ).states_( [[ "rotate 90" ]] )
			.radius_( 0 )
			.textOffset_( Point(6,0) )
			.action_({ 
				event.wfsSynth.wfsPath_(
				Point( event.x, event.y )
					.rotate( -0.5pi )
					.round( 0.01 )
					.asWFSPoint( event.wfsSynth.wfsPath.z ), false );
				//views[ \intType ].action.value( views[ \intType ] );
				views[ \static ][ \x ].value = event.x;
				views[ \static ][ \y ].value = event.y;
				if( WFSPlotSmooth.isOpen ) 
					{ event.wfsSynth.wfsPath.plotSmooth( toFront: false ) };
				});
				
		RoundButton( intTypeViews.static, 60@16 ).states_( [[ "random" ]] )
			.radius_( 0 )
			.textOffset_( Point(9,0) )
			.action_({ 
				event.wfsSynth.wfsPath_(
					WFSPoint( 15.0.rand2.round(0.1), 15.0.rand2.round(0.1), 
						event.wfsSynth.wfsPath.z ), false );
				//views[ \intType ].action.value( views[ \intType ] );
				views[ \static ][ \x ].value = event.x;
				views[ \static ][ \y ].value = event.y;
				if( WFSPlotSmooth.isOpen ) 
					{ event.wfsSynth.wfsPath.plotSmooth( toFront: false ) };
				});
			
		// plane:
		SCStaticText( intTypeViews.plane, 80@20 ).string_( "angle/distance" );
		
		views[ \plane ] = ();
		
		views[ \plane ][ \angle ] = ScrollingNBox( intTypeViews.plane, 30@20 )
			.value_( event.angle ? 0).action_({ 
				/* if( event.wfsSynth.wfsPath.class == WFSPath )
					{ wfsPathBackup = event.wfsSynth.wfsPath }; */
				event.wfsSynth.wfsPath_( WFSPlane(
					views[ \plane ][ \angle ].value, views[ \plane ][ \distance ].value, 0 ),
					false );
				if( WFSPlotSmooth.isOpen ) 
					{ event.wfsSynth.wfsPath.plotSmooth( toFront: false ) };
				});
					
		views[ \plane ][ \distance ] = ScrollingNBox( intTypeViews.plane, 30@20 )
			.value_( event.distance ? 0 ).clipLo_(0)
				.action_( views[ \plane ][ \angle ].action );	
		SCButton( intTypeViews.plane, 30@20 )
			.states_( [ [ "plot", Color.black, Color.clear ] ] )
			.action_({ event.wfsSynth.wfsPath.currentTime_( 0 ).plotSmooth; });
			
		intTypeViews.plane.decorator.nextLine;
		
		RoundButton( intTypeViews.plane, 60@16 ).states_( [[ "rotate 90" ]] )
			.radius_( 0 )
			.textOffset_( Point(6,0) )
			.action_({ 
				event.wfsSynth.wfsPath_(
					WFSPlane( (event.angle + 90) % 360, event.distance )
					, false );
				views[ \plane ][ \angle ].value = event.angle;
				if( WFSPlotSmooth.isOpen ) 
					{ event.wfsSynth.wfsPath.plotSmooth( toFront: false ) };
				//views[ \plane ][ \distance ].value = event.distance;
				});
				
		RoundButton( intTypeViews.plane, 60@16 ).states_( [[ "random" ]] )
			.radius_( 0 )
			.textOffset_( Point(9,0) )
			.action_({ 
				event.wfsSynth.wfsPath_(
					WFSPlane( 360.0.rand.round(1), 7.5 + ( 15.0.rand.round(0.1) ) ), false );
				
				views[ \plane ][ \angle ].value = event.angle;
				views[ \plane ][ \distance ].value = event.distance;
				if( WFSPlotSmooth.isOpen ) 
					{ event.wfsSynth.wfsPath.plotSmooth( toFront: false ) };
				});

			
		// index
		SCStaticText( intTypeViews.index, 80@20 ).string_( "speaker index" ).align_( \right );
		
		views[ \index ] = ();
		
		views[ \index ][ \index ] = ScrollingNBox( intTypeViews.index, 30@20 )
			.value_( if( event.wfsSynth.wfsPath.isNumber,
				 event.wfsSynth.wfsPath,  0 ) )
			.clipLo_( 0 ).action_({ 
				/* if( event.wfsSynth.wfsPath.class == WFSPath )
					{ wfsPathBackup = event.wfsSynth.wfsPath }; */
				event.wfsSynth.wfsPath_( views[ \index ][ \index ].value ,
					false );
			if( WFSPlotSmooth.isOpen ) 
					{WFSConfiguration.default.allSpeakers.wrapAt( 
					 views[ \index ][ \index ].value.round( 1 ) )
					.asWFSPoint.plotSmooth( toFront: false ) };
					});
					
		SCButton( intTypeViews.index, 40@20 )
			.states_( [ [ "plot", Color.black, Color.clear ] ] )
			.action_({ 
				WFSConfiguration.default.allSpeakers.wrapAt( 
					 views[ \index ][ \index ].value.round( 1 ) )
					.asWFSPoint.plotSmooth });				
				
		
		//intTypeViews.linear.visible = true;
		
		views[ \intType ].action_({ |box| 
					
					
					intTypeViews.values.do({ |item| item.visible = false });
					intTypeViews[ box.items[ box.value ] ].visible = true;

					case { [ \linear, \cubic ].includes( event.wfsSynth.intType ) }
						{ wfsPathBackup = event.wfsSynth.wfsPath; }
						{ [ \static ].includes( event.wfsSynth.intType ) }
						{ wfsPointBackup = event.wfsSynth.wfsPath; }
						{ [ \plane ].includes( event.wfsSynth.intType ) }
						{ wfsPlaneBackup = event.wfsSynth.wfsPath; }
						{ [ \index ].includes( event.wfsSynth.intType ) }
						{ wfsIndexBackup = event.wfsSynth.wfsPath; };
		
											
					event.wfsSynth.intType = box.items[ box.value ];
					
					if( [ \static, \plane, \index ].includes( event.wfsSynth.intType ) )
						{ views[ \duration ].enabled = true; }
						{ views[ \duration ].enabled = false };
						
					case { [ \linear, \cubic ].includes( event.wfsSynth.intType ) }
						{ event.wfsSynth.wfsPath = wfsPathBackup ??
							 { WFSPath.circle.length_(5); } }
						{ [ \static ].includes( event.wfsSynth.intType ) }
						{  event.wfsSynth.wfsPath = wfsPointBackup ? WFSPoint( 0,0,0 ); }
						{ [ \plane ].includes( event.wfsSynth.intType ) }
						{  event.wfsSynth.wfsPath = wfsPlaneBackup ? WFSPlane( 0, 6 ); }
						{ [ \index ].includes( event.wfsSynth.intType ) }
						{  event.wfsSynth.wfsPath = wfsIndexBackup ? 0 };
					
					editAction.value( event, \intType, box.items[ box.value ] );
					});
					
		// audioTypeViews
		audioTypeViews = (
				'blip': SCCompositeView( window, audioTypeBounds )
					.background_( Color.gray.alpha_(0.25) )
					.decorator_( FlowLayout( audioTypeBounds ) )
					.visible_( false ),
				'buf': SCCompositeView( window, audioTypeBounds )
					.background_(Color.gray.alpha_(0.25) )
					.decorator_( FlowLayout( audioTypeBounds ) )
					.visible_( false ),
				'disk': SCCompositeView( window, audioTypeBounds )
					.background_(Color.gray.alpha_(0.25) )
					.decorator_( FlowLayout( audioTypeBounds ) )
					.visible_( false ),
				'func': SCCompositeView( window, audioTypeBounds )
					.background_(Color.gray(0.1).alpha_(0.0))
					.decorator_( FlowLayout( intTypeBounds ) )
					.visible_( false ),
				'live': SCCompositeView( window, audioTypeBounds )
					.background_(Color.gray(0.1).alpha_(0.0))
					.decorator_( FlowLayout( intTypeBounds ) )
					.visible_( false ) );
					
		views[ \audioType ].action_({ |box| 
					audioTypeViews.values.do({ |item| item.visible = false });
					audioTypeViews[ box.items[ box.value ] ].visible = true;
					event.wfsSynth.audioType = box.items[ box.value ];
					
					views[ \buf ][ \startFrameMode ].action.value( 
						views[ \buf ][ \startFrameMode ] );
					views[ \disk ][ \startFrameMode ].action.value( 
						views[ \disk ][ \startFrameMode ] );

					editAction.value( event, \audioType, box.items[ box.value ] );
					});
					
					
		SCStaticText( audioTypeViews.func, 140@50	).string_( "not supported yet" );
		
		SCStaticText( audioTypeViews.live, 140@50	).string_( "not supported yet" );
					
					
		// blip
		views[ \blip ] = ();
		
		// freq
		SCStaticText( audioTypeViews.blip, 60@20	).string_( "freq (Hz)" );
		views[ \blip ][ \freq ] = ScrollingNBox( audioTypeViews.blip, 40@20 )
			.value_( (event.wfsSynth.args.asArgsDict ? ())[ \freq ] ? 100 )
			.step_( 1 )
			.action_({ |box|	
					if( box.value < 20 ) { box.value = 20 };
					if( box.value > 10000 ) { box.value = 10000 };
					event.wfsSynth.args = 
						( (event.wfsSynth.args.asArgsDict ? () )[\freq] = box.value ) 
							.asArgsArray;
					editAction.value( event, \freq, box.value );
				});
		
		// rate
		SCStaticText( audioTypeViews.blip, 30@20	).string_( "rate" ).align_( \right );
		views[ \blip ][ \rate ] = ScrollingNBox( audioTypeViews.blip, 40@20 )
			.value_( event.wfsSynth.pbRate )
			.step_( 0.1 )
			.action_({ |box|
					if( box.value < 0.1 ) { box.value = 0.1 };
					event.wfsSynth.pbRate = box.value; 
					editAction.value( event, \rate, box.value );
				});
				
		audioTypeViews.blip.decorator.nextLine;
			
		// noiselevel
		SCStaticText( audioTypeViews.blip, 60@20	).string_( "noiseLevel" );
		views[ \blip ][ \noiseLevel ] = ScrollingNBox( audioTypeViews.blip, 40@20 )
			.value_( (event.wfsSynth.args.asArgsDict ? ())[ \noiseLevel ] ? 0.125 )
			.step_( 0.125 )
			.action_({ |box|	
					if( box.value < 0 ) { box.value = 0 };
					if( box.value > 2 ) { box.value = 2 };
					event.wfsSynth.args = 
						( (event.wfsSynth.args.asArgsDict ? () )[\noiseLevel] = box.value ) 
							.asArgsArray;
					editAction.value( event, \noiseLevel, box.value.dbamp );
				});
				
		audioTypeViews.blip.decorator.nextLine;
				
		// blipLevel
		SCStaticText( audioTypeViews.blip, 60@20	).string_( "blipLevel" );
		views[ \blip ][ \blipLevel ] = ScrollingNBox( audioTypeViews.blip, 40@20 )
			.value_( (event.wfsSynth.args.asArgsDict ? ())[ \blipLevel ] ? 1.0 )
			.step_( 0.125 )
			.action_({ |box|	
					if( box.value < 0 ) { box.value = 0 };
					if( box.value > 2 ) { box.value = 2 };
					event.wfsSynth.args = 
						( (event.wfsSynth.args.asArgsDict ? () )[\blipLevel] = box.value ) 
							.asArgsArray;
					editAction.value( event, \blipLevel, box.value.dbamp );
				});
				
		// buf
		// file
		SCStaticText( audioTypeViews.buf, 40@29 ).string_( "file" );
		
		views[ \buf ] = ();
		
		views[ \buf ][ \fileName ] = SCStaticText( audioTypeViews.buf, 125@29 )
			.string_( event.wfsSynth.filePath.basename )
			.font_( Font( "Monaco", 9 ) );
			
		SCStaticText( audioTypeViews.buf, 40@29 ).string_( "folder" );
		
		views[ \buf ][ \dirName ] = SCStaticText( audioTypeViews.buf, 125@29 )
			.string_( event.wfsSynth.filePath.dirname.deStandardizePath )
			.font_( Font( "Monaco", 9 ) );
		
		// browse
		views[ \buf ][ \browse ] = SCButton( audioTypeViews.buf, 60@20 )
			.states_( [[ "browse", Color.black, Color.clear ]] )
			.action_({ 
				CocoaDialog.getPaths( { |paths|
					var newName;
					newName = paths[0];
					views[ \buf ][ \dirName ].string = newName.dirname.deStandardizePath;
					views[ \buf ][ \fileName ].string = newName.basename;
					views[ \disk ][ \dirName ].string = newName.dirname.deStandardizePath;
					views[ \disk ][ \fileName ].string = newName.basename;
					event.wfsSynth.filePath = newName.standardizePath; 
					if( event.wfsSynth.checkSoundFile(
						{  SCAlert( "numChannels != 1", actions: [ ] ); } ) )
							{ views[ \buf ][ \dur ].value =  event.wfsSynth.soundFileDur;  }
							{ SCAlert( "file could not be opened", actions: [ ]  );
								views[ \buf ][ \dur ].value =  nil;   };
						} );
				});
				
		// specify
				
		// verify
		views[ \buf ][ \verify ] = SCButton( audioTypeViews.buf, 56@20 )
			.states_( [[ "check", Color.black, Color.clear ]] )
			.action_({ 
				var soundFile, wndw, waitView;
				if( WFS.scVersion === \new )
				{
				if( event.wfsSynth.checkSoundFile(
					{ |sn, sf|  
						SCAlert( "The soundfile contains % channels.\nWhat do you want to do?"
							.format( sf.numChannels ),
						[ "split file", "extract..", "use first" ],
							actions: [ 
								{ 
								soundFile = SoundFile.openRead( event.wfsSynth.filePath );
								soundFile.close;
								/*
								wndw = SCWindow( "Split File", 
									Rect.aboutPoint( SCWindow.screenBounds.center, 
									150 , 50 ), false ).decorate.front;
								waitView = WaitView( wndw, 45@45 );
								waitView.start;
								*/
							
								wndw = SCAlert( "Please wait...",
									({ |i| (i+1).asString } ! sf.numChannels)
									 ++ [ "all" ] );
								wndw.disable;
								wndw.window.userCanClose_( false );
								wndw.color = Color.black.alpha_(0.75);
								wndw.iconName = "wait";
								
								waitView = Routine({
									var i = 0;
									loop {  wndw.iconName_( "wait_" ++ i ); 
										i = (i+(1/16)) % 1.0; 
										0.1.wait; }
									}).play( AppClock );
								soundFile.splitChannels( 
									server: WFSServers.default.m,
									doneAction: { |soundFiles|
											{ 
											waitView.stop;
											wndw.iconName = "warning";
											wndw.string = "Please select a channel to use";
											wndw.color_ ;
											wndw.enable;
											wndw.actions = soundFiles.collect({ |sf, i|
												   var newName;
												   newName = sf.path;
												   {
												   views[ \buf ][ \dirName ].string =
												    newName.dirname.deStandardizePath;
												   views[ \buf ][ \fileName ].string = 
												    newName.basename;
												   views[ \disk ][ \dirName ].string = 
												      newName.dirname.deStandardizePath;
												   views[ \disk ][ \fileName ].string =
												    newName.basename;
												   event.wfsSynth.filePath = 
												    newName.standardizePath;
												   views[ \buf ][ \dur ].value = 
												    event.wfsSynth.soundFileDur; 											       };
												  }) ++ [ { 
												    var newName;
												   newName = soundFiles[0].path;
												    views[ \buf ][ \dirName ].string =
												    newName.dirname.deStandardizePath;
												   views[ \buf ][ \fileName ].string = 
												    newName.basename;
												   views[ \disk ][ \dirName ].string = 
												      newName.dirname.deStandardizePath;
												   views[ \disk ][ \fileName ].string =
												    newName.basename;
												   event.wfsSynth.filePath = 
												    newName.standardizePath;
												   views[ \buf ][ \dur ].value = 
												    event.wfsSynth.soundFileDur; 
												   if( parent.notNil )
												       { soundFiles[1..].do({ |sf, i|
												         var newEvent;
												         newEvent =  event.duplicate.track_( event.track+1 );
												         newEvent.wfsSynth.filePath_( sf.path );
												          parent.score.events = 
												             parent.score.events.add( newEvent );
												           });
												         parent.score.cleanOverlaps;
												         parent.update;
												         };
							{ SCAlert( "no parent WFSScoreEditor found" ); }; } ] 
												   
												}.defer;
											});
								}, 
							{ soundFile = SoundFile.openRead( event.wfsSynth.filePath );
							  soundFile.close;
							  SCAlert( "Select channel to extract", {|i|i+1} ! soundFile.numChannels,
								 { |i| { soundFile.extractChannel(i, 
								 	server: WFSServers.default.m,
								 	doneAction: { |sf|
												   var newName;
												   newName = sf.path;
												   {
												   views[ \buf ][ \dirName ].string =
												    newName.dirname.deStandardizePath;
												   views[ \buf ][ \fileName ].string = 
												    newName.basename;
												   views[ \disk ][ \dirName ].string = 
												      newName.dirname.deStandardizePath;
												   views[ \disk ][ \fileName ].string =
												    newName.basename;
												   event.wfsSynth.filePath = 
												    newName.standardizePath;
												   views[ \buf ][ \dur ].value = 
												    event.wfsSynth.soundFileDur; 											       }.defer;
											}
								 	) } } ! soundFile.numChannels
									)
								}, { }] ); 
						} ) )
					{ views[ \buf ][ \dur ].value =  event.wfsSynth.soundFileDur;  }
					{ SCAlert( "Sorry, the file could not be opened", actions: [ ]  );
						views[ \buf ][ \dur ].value =  nil;   };
				}
				{  /// old version
				
				if( event.wfsSynth.checkSoundFile(
					{  SCAlert( "numChannels > 1\nfirst channel will be used",
						[ "split file", "use first" ],
							actions: [ 
								{ 
								soundFile = SoundFile.openRead( event.wfsSynth.filePath );
								//soundFile.close;
								/*
								wndw = SCWindow( "Split File", 
									Rect.aboutPoint( SCWindow.screenBounds.center, 
									150 , 50 ), false ).decorate.front;
								waitView = WaitView( wndw, 45@45 );
								waitView.start;
								SCStaticText( wndw, 80@45 ).string_( "please wait..." );
								*/
								soundFile.splitChannelsLang( 
									//server: WFSServers.default.m,
									doneAction: { |soundFiles|
											{ 
											//waitView.stop;
											//wndw.close;
											soundFile.close;
											SCAlert( "select channel to use",
												soundFiles.collect({ |sf, i|
												   (i+1).asString;
												   }) ++ [ "all" ],
												soundFiles.collect({ |sf, i|
												   var newName;
												   newName = sf.path;
												   {
												   views[ \buf ][ \dirName ].string =
												    newName.dirname.deStandardizePath;
												   views[ \buf ][ \fileName ].string = 
												    newName.basename;
												   views[ \disk ][ \dirName ].string = 
												      newName.dirname.deStandardizePath;
												   views[ \disk ][ \fileName ].string =
												    newName.basename;
												   event.wfsSynth.filePath = 
												    newName.standardizePath;
												   views[ \buf ][ \dur ].value = 
												    event.wfsSynth.soundFileDur; 											       };
												  }) ++ [ { 
												    var newName;
												   newName = soundFiles[0].path;
												    views[ \buf ][ \dirName ].string =
												    newName.dirname.deStandardizePath;
												   views[ \buf ][ \fileName ].string = 
												    newName.basename;
												   views[ \disk ][ \dirName ].string = 
												      newName.dirname.deStandardizePath;
												   views[ \disk ][ \fileName ].string =
												    newName.basename;
												   event.wfsSynth.filePath = 
												    newName.standardizePath;
												   views[ \buf ][ \dur ].value = 
												    event.wfsSynth.soundFileDur; 
												   if( parent.notNil )
												       { soundFiles[1..].do({ |sf, i|
												         var newEvent;
												         newEvent =  event.duplicate.track_( event.track+1 );
												         newEvent.wfsSynth.filePath_( sf.path );
												          parent.score.events = 
												             parent.score.events.add( newEvent );
												           });
												         parent.score.cleanOverlaps;
												         parent.update;
												         };
												     { SCAlert( "no parent WFSScoreEditor found" ); };
												   } ] 
												  ) 
												}.defer;
											});
								}, 
							{ soundFile = SoundFile.openRead( event.wfsSynth.filePath );
							  soundFile.close;
							  SCAlert( "select channel to extract", {|i|i+1} ! soundFile.numChannels,
								 { |i| { soundFile.extractChannel(i, 
								 	server: WFSServers.default.m,
								 	doneAction: { |sf|
												   var newName;
												   newName = sf.path;
												   {
												   views[ \buf ][ \dirName ].string =
												    newName.dirname.deStandardizePath;
												   views[ \buf ][ \fileName ].string = 
												    newName.basename;
												   views[ \disk ][ \dirName ].string = 
												      newName.dirname.deStandardizePath;
												   views[ \disk ][ \fileName ].string =
												    newName.basename;
												   event.wfsSynth.filePath = 
												    newName.standardizePath;
												   views[ \buf ][ \dur ].value = 
												    event.wfsSynth.soundFileDur; 											       }.defer;
											}
								 	) } } ! soundFile.numChannels
									)
								}, { }] ); 
						} ) )
					{ views[ \buf ][ \dur ].value =  event.wfsSynth.soundFileDur;  }
					{ SCAlert( "file could not be opened", actions: [ ]  );
						views[ \buf ][ \dur ].value =  nil;   };
				
				};
			});
				
		
		views[ \buf ][ \options ] = SCPopUpMenu( audioTypeViews.buf, 58@20 )
			.items_( [ "(options", /*)*/ 
				"-", "specify path..", "-", "copy to folder..", "save as..",     // 2, 4, 5
				"-", "play with Quicktime", "show in Finder" ] ) // 7, 8
			.action_({ |v|
				var instring;
				case { v.value == 2 } // specify
					{ instring = views[ \buf ][ \dirName ].string ++
							"/" ++ views[ \buf ][ \fileName ].string;
						if( instring.asSymbol === './.' )
							{ instring = "sounds/a11wlk01-44_1.aiff" };
						SCRequestString( 
							instring,
							"Please specify an audio file name:",
							{ |newName|
								views[ \buf ][ \dirName ].string =
									newName.dirname.deStandardizePath;
								views[ \buf ][ \fileName ].string = 
									newName.basename;
								views[ \disk ][ \dirName ].string = 
									newName.dirname.deStandardizePath;
								views[ \disk ][ \fileName ].string = newName.basename;
								event.wfsSynth.filePath = newName.standardizePath; } );
						 	}
					{ v.value == 4 } // copy to (use original name)
					{ 
					CocoaDialog.savePanel({ |path|
					
						event.wfsSynth.copySoundFileTo( path.dirname, 
							doneAction: { |newName|
								views[ \buf ][ \dirName ].string =
									newName.dirname.deStandardizePath;
								views[ \buf ][ \fileName ].string = 
									newName.basename;
								views[ \disk ][ \dirName ].string = 
									newName.dirname.deStandardizePath;
								views[ \disk ][ \fileName ].string = newName.basename;
								
								}
							  ) 
						
						});  
					}
					{ v.value == 5 } // save as (use specified name)
					{  
					CocoaDialog.savePanel({ |path|
					
						event.wfsSynth.copySoundFileTo( 
							path.dirname, path.basename,
							doneAction: { |newName|
								views[ \buf ][ \dirName ].string =
									newName.dirname.deStandardizePath;
								views[ \buf ][ \fileName ].string = 
									newName.basename;
								views[ \disk ][ \dirName ].string = 
									newName.dirname.deStandardizePath;
								views[ \disk ][ \fileName ].string = newName.basename;
								
								}
							  );
						});  
					}
					{ v.value == 7 } // play quicktime
					{ event.wfsSynth.filePath.openWith( "QuickTime Player.app" ) }
					{ v.value == 8 } // show in finder
					{ event.wfsSynth.filePath.showInFinder };
				
				v.value = 0;
				
				});
				
			
				
		
		audioTypeViews.buf.decorator.nextLine;
		
		// dur
		SCStaticText( audioTypeViews.buf, 60@20	).string_( "duration" ).align_( \right );
		views[ \buf ][ \dur ] = ScrollingNBox( audioTypeViews.buf, 40@20 )
			.value_( event.wfsSynth.soundFileDur )
			.enabled_( false );
			
		SCButton( audioTypeViews.buf, 40@20 )
			.states_( [["use"]] )
			.action_({ event.wfsSynth.useSoundFileDur( true, { this.update } ); });
			
		audioTypeViews.buf.decorator.nextLine;	
		
		// startFrame
		
		SCStaticText( audioTypeViews.buf, 60@20	).string_( "startOffset" ).align_( \right );
		views[ \buf ][ \startFrame ] = ScrollingNBox( audioTypeViews.buf, 73@20 )
			.value_( 0.0 )
			.clipLo_( 0 )
			.action_({ |box|
				case { views[ \buf ][ \startFrameMode ].value == 0 }
					{ event.wfsSynth.startTime_( box.value ); }
					{ views[ \buf ][ \startFrameMode ].value == 1 }
					{ event.wfsSynth.startTime_( box.value / 1000 ); }
					{ views[ \buf ][ \startFrameMode ].value == 2 }
					{ event.wfsSynth.startFrame_( box.value ); };
				editAction.value( event, \startFrame, box.value );
				});
	
		views[ \buf ][ \startFrameMode ] = SCPopUpMenu( audioTypeViews.buf, 40@20 )
			.items_( ["s", "ms","smp"] )
			.action_({ |popUp|
				case { views[ \buf ][ \startFrameMode ].value == 0 }
					{ views[ \buf ][ \startFrame ].value = event.wfsSynth.startTime
							.round( 0.00001 )  }
					{ views[ \buf ][ \startFrameMode ].value == 1 }
					{ views[ \buf ][ \startFrame ].value = 
						(event.wfsSynth.startTime * 1000).round( 0.01 )  }
					{ views[ \buf ][ \startFrameMode ].value == 2 }
					{ views[ \buf ][ \startFrame ].value = event.wfsSynth
						.startFrame.round(1); };
				//editAction.value( event, \startFrameMode, popUp.value );
				});
		
		audioTypeViews.buf.decorator.nextLine;	
		
		// rate
		SCStaticText( audioTypeViews.buf, 60@20	).string_( "rate" ).align_( \right );
		views[ \buf ][ \rate ] = ScrollingNBox( audioTypeViews.buf, 40@20 )
			.value_( event.wfsSynth.pbRate )
			.step_( 0.1 )
			.action_({ |box|
					if( box.value < 0.1 ) { box.value = 0.1 };
					event.wfsSynth.pbRate = box.value; 
					views[ \buf ][ \dur ].value =  event.wfsSynth.soundFileDur;
					views[ \buf ][ \startFrameMode ].action.value( 
						views[ \buf ][ \startFrameMode ] );
					/*
					views[ \disk ][ \startFrameMode ].action.value( 
						views[ \disk ][ \startFrameMode ] );
					*/

					editAction.value( event, \rate, box.value );
				});
				
		
				
		// loop
		SCStaticText( audioTypeViews.buf, 30@20	).string_( "loop" ).align_( \right );
		views[ \buf ][ \loop ] = SCPopUpMenu( audioTypeViews.buf, 40@20 )
			.items_( [ "off", "on" ] )
			.value_( event.wfsSynth.loop.asInt )
			.action_({ |box|
					event.wfsSynth.loop = box.value; 
					editAction.value( event, \loop, box.value );
				});
				
		
		
		// disk
		// file
		SCStaticText( audioTypeViews.disk, 40@29 ).string_( "file" );
		
		views[ \disk ] = ();
		
		views[ \disk ][ \fileName ] = SCStaticText( audioTypeViews.disk, 125@29 )
			.string_(  event.wfsSynth.filePath.basename )
			.font_( Font( "Monaco", 9 ) );
			
		SCStaticText( audioTypeViews.disk, 40@29 ).string_( "folder" );
		
		views[ \disk ][ \dirName ] = SCStaticText( audioTypeViews.disk, 125@29 )
			.string_( event.wfsSynth.filePath.dirname.deStandardizePath )
			.font_( Font( "Monaco", 9 ) );
		
		// browse
		views[ \disk ][ \browse ] = SCButton( audioTypeViews.disk, 60@20 )
			.states_( [[ "browse", Color.black, Color.clear ]] )
			.action_({ 
				CocoaDialog.getPaths( { |paths|
					var newName;
					newName = paths[0];
					views[ \disk ][ \dirName ].string = newName.dirname.deStandardizePath;
					views[ \disk ][ \fileName ].string = newName.basename;
					views[ \buf ][ \dirName ].string = newName.dirname.deStandardizePath;
					views[ \buf ][ \fileName ].string = newName.basename;
					event.wfsSynth.filePath = newName.standardizePath; 
					if( event.wfsSynth.checkSoundFile(
						{  SCAlert( "numChannels != 1", actions: [ ] ); },
						{ SCAlert( "sampleRate != 44100", actions: [ ] ); } ) )
							{ views[ \disk ][ \dur ].value = 
								event.wfsSynth.soundFileDur( false, false );  }
							{ SCAlert( "file could not be opened", actions: [ ]  );
								views[ \disk ][ \dur ].value =  nil;   };
					} );
				});
				
		// specify
		/*
		views[ \disk ][ \specify ] = SCButton( audioTypeViews.disk, 58@20 )
			.states_( [[ "specify", Color.black, Color.clear ]] )
			.action_({ |box|
				var instring;
				instring = views[ \disk ][ \dirName ].string ++
					"/" ++ views[ \disk ][ \fileName ].string;
				if( instring.asSymbol === './.' )
					{ instring = "sounds/a11wlk01-44_1.aiff" };
				SCRequestString( 
					instring,
					"Please specify an audio file name:",
					{ |newName|
						views[ \disk ][ \dirName ].string = newName.dirname.deStandardizePath;
						views[ \disk ][ \fileName ].string = newName.basename;
						views[ \buf ][ \dirName ].string = newName.dirname.deStandardizePath;
						views[ \buf ][ \fileName ].string = newName.basename;
						event.wfsSynth.filePath = newName.standardizePath; } );
				});
			*/
				
		// verify
		views[ \disk ][ \verify ] = SCButton( audioTypeViews.disk, 56@20 )
			.states_( [[ "check", Color.black, Color.clear ]] )
			.action_({if( event.wfsSynth.checkSoundFile(
						{  SCAlert( "numChannels != 1", actions: [ ] ); },
						{ SCAlert( "sampleRate should be 44100", 
								actions: [ ] ); } ) )
							{ views[ \disk ][ \dur ].value = 
								event.wfsSynth.soundFileDur( false, false );  }
							{ SCAlert( "file could not be opened", actions: [ ]  );
								views[ \disk ][ \dur ].value =  nil;   };
			});
			
		views[ \disk ][ \options ] = SCPopUpMenu( audioTypeViews.disk, 58@20 )
			.items_( [ "(options", /*)*/ 
				"-", "specify path..", "-", "copy to folder..", "save as..",     // 2, 4, 5
				"-", "play with Quicktime", "show in Finder" ] ) // 7, 8
			.action_({ |v|
				var instring;
				case { v.value == 2 } // specify
					{ instring = views[ \buf ][ \dirName ].string ++
							"/" ++ views[ \buf ][ \fileName ].string;
						if( instring.asSymbol === './.' )
							{ instring = "sounds/a11wlk01-44_1.aiff" };
						SCRequestString( 
							instring,
							"Please specify an audio file name:",
							{ |newName|
								views[ \buf ][ \dirName ].string =
									newName.dirname.deStandardizePath;
								views[ \buf ][ \fileName ].string = 
									newName.basename;
								views[ \disk ][ \dirName ].string = 
									newName.dirname.deStandardizePath;
								views[ \disk ][ \fileName ].string = newName.basename;
								event.wfsSynth.filePath = newName.standardizePath; } );
						 	}
					{ v.value == 4 } // copy to (use original name)
					{ 
					CocoaDialog.savePanel({ |path|
					
						event.wfsSynth.copySoundFileTo( path.dirname, 
							doneAction: { |newName|
								views[ \buf ][ \dirName ].string =
									newName.dirname.deStandardizePath;
								views[ \buf ][ \fileName ].string = 
									newName.basename;
								views[ \disk ][ \dirName ].string = 
									newName.dirname.deStandardizePath;
								views[ \disk ][ \fileName ].string = newName.basename;
								
								}
							  ) 
						
						});  
					}
					{ v.value == 5 } // save as (use specified name)
					{  
					CocoaDialog.savePanel({ |path|
					
						event.wfsSynth.copySoundFileTo( 
							path.dirname, path.basename,
							doneAction: { |newName|
								views[ \buf ][ \dirName ].string =
									newName.dirname.deStandardizePath;
								views[ \buf ][ \fileName ].string = 
									newName.basename;
								views[ \disk ][ \dirName ].string = 
									newName.dirname.deStandardizePath;
								views[ \disk ][ \fileName ].string = newName.basename;
								
								}
							  );
						
						});  
					}
					{ v.value == 7 } // play quicktime
					{ event.wfsSynth.filePath.openWithID( "com.apple.quicktime" ) }
					{ v.value == 8 } // show in finder
					{ event.wfsSynth.filePath.showInFinder };
				
				v.value = 0;
				
				});

			
		// dur
		SCStaticText( audioTypeViews.disk, 60@20	).string_( "duration" ).align_( \right );
		views[ \disk ][ \dur ] = ScrollingNBox( audioTypeViews.disk, 40@20 )
			.value_( event.wfsSynth.soundFileDur( false, false ) )
			.enabled_( false );
			
		SCButton( audioTypeViews.disk, 40@20 )
			.states_( [["use"]] )
			.action_({ event.wfsSynth.useSoundFileDur( true, { this.update }, false ); });
			
		audioTypeViews.buf.decorator.nextLine;	
			
		// startFrame
		
		SCStaticText( audioTypeViews.disk, 60@20	).string_( "startOffset" ).align_( \right );
		views[ \disk ][ \startFrame ] = ScrollingNBox( audioTypeViews.disk, 73@20 )
			.value_( 0.0 )
			.clipLo_( 0 )
			.action_({ |box|
				case { views[ \disk ][ \startFrameMode ].value == 0 }
					{ event.wfsSynth.startTime_( box.value, false, false  ); }
					{ views[ \disk ][ \startFrameMode ].value == 1 }
					{ event.wfsSynth.startTime_( box.value / 1000, false, false  ); }
					{ views[ \disk ][ \startFrameMode ].value == 2 }
					{ event.wfsSynth.startFrame_( box.value, false, false ); };
				editAction.value( event, \startFrame, box.value );
				});
	
		views[ \disk ][ \startFrameMode ] = SCPopUpMenu( audioTypeViews.disk, 40@20 )
			.items_( ["s", "ms","smp"] )
			.action_({ |popUp|
				case { views[ \disk ][ \startFrameMode ].value == 0 }
					{ views[ \disk ][ \startFrame ].value = event.wfsSynth
							.startTime( false, false )
							.round( 0.00001 )  }
					{ views[ \disk ][ \startFrameMode ].value == 1 }
					{ views[ \disk ][ \startFrame ].value = 
						(event.wfsSynth.startTime( false, false ) * 1000).round( 0.01 )  }
					{ views[ \disk ][ \startFrameMode ].value == 2 }
					{ views[ \disk ][ \startFrame ].value = event.wfsSynth
						.startFrame( false, false ).round(1); };
				//editAction.value( event, \startFrameMode, popUp.value );
				});
			
			

		};
				
		//audioTypeViews.blip.decorator.nextLine;
		//window.setFont( Font( "Monaco", 9 ) );
			
		this.update;
						
		}
	
	
	
	}
	