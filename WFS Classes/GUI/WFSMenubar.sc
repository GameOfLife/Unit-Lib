WFSMenubar {

	*add {
		
		var wfsMenu, scoreMenu, pathMenu, helpMenu, viewMenu, defaultMenu, addEvent, events;
		
		//score	
		scoreMenu = SCMenuGroup.new(nil, "Score",2);
		SCMenuItem.new(scoreMenu,  "New").action_({
			WFSScore.new.edit 
		});
		SCMenuItem.new(scoreMenu, "Open").action_({
			WFSScoreEditor.open
		});
		SCMenuItem.new(scoreMenu, "Save").action_({	
			var scoreEditor = WFSScoreEditor.current;
			if(scoreEditor.notNil){	WFSScoreEditor.current.save}
		});				
		SCMenuItem.new(scoreMenu, "Save as").action_({	
			var scoreEditor = WFSScoreEditor.current;
			if(scoreEditor.notNil){scoreEditor.saveAs}
		});
		SCMenuSeparator.new(scoreMenu);	
		SCMenuItem.new(scoreMenu, "Combine or append scores").action_({
			var scoreEditor = WFSScoreEditor.current;
			if(scoreEditor.notNil){	 WFSScoreEditor.current.combineAppend }
		});

		//events
		events = SCMenuGroup.new(nil, "Events",3);
		addEvent = SCMenuGroup.new(events, "Add");
		SCMenuItem.new(addEvent, "Audiofile").action_({
			var scoreEditor = WFSScoreEditor.current;
			if(scoreEditor.notNil){	 WFSScoreEditor.current.addAudioFiles}
		});
		SCMenuItem.new(addEvent, "Test Event").action_({
			var scoreEditor = WFSScoreEditor.current;
			if(scoreEditor.notNil){	 WFSScoreEditor.current.addTestEvent }
		});
		SCMenuItem.new(events, "duplicate").action_({
			var scoreEditor = WFSScoreEditor.current;
			if(scoreEditor.notNil){	 WFSScoreEditor.current.duplicateSelected }
		});	
		
		//paths			
		pathMenu = SCMenuGroup.new(nil, "Paths",4);
		
		SCMenuItem.new(pathMenu, "Generate New").action_(  
			{ WFSPathEditor.newEditor( 
				[ WFSPath( [ [0,0],[1,0] ], name: \temp ) ], "WFSPathEditor", true ); 
			});
		SCMenuItem.new(pathMenu, "Draw New").action_({  WFSDrawWindow.new; });
		SCMenuItem.new(pathMenu, "Open file").action_({   
								CocoaDialog.getPaths(
									{arg paths;
										//WFSPathEditor.close;
										WFSPathEditor( WFSPathArray.readWFSFile(paths.first) );
										}, { "cancelled".postln; });
								});
							
		SCMenuItem.new(pathMenu, "Import SVG file").action_({   
								CocoaDialog.getPaths(
									{arg paths;
										var file;
										file = SVGFile.read( paths[0] );
										if( file.hasCurves )
											{ SCAlert( "file '%'\nincludes curved segments. How to import?"
												.format( file.path.basename ),
												[ "cancel", "lines only", "curves" ],
												[ {},{  WFSPathEditor( WFSPathArray
														.fromSVGFile( file, useCurves: false) ) },
												  {  WFSPathEditor( WFSPathArray
												  		.fromSVGFile( file, useCurves: true) ) } ]  
												); }
											{ WFSPathEditor( WFSPathArray.fromSVGFile( file ) ) }
										//WFSPathEditor.close;
										}, { "cancelled".postln; });
								});
					
		defaultMenu = SCMenuGroup.new(pathMenu, "create default WFSPath");
		SCMenuItem.new(defaultMenu, "Circle").action_( {WFSPath.circle.edit; });
		SCMenuItem.new(defaultMenu, "Rand").action_( {WFSPath.rand.edit; });
		SCMenuItem.new(defaultMenu, "Spiral").action_( {WFSPath.spiral.edit; });
		SCMenuItem.new(defaultMenu, "Lissajous").action_( {WFSPath.lissajous.edit; });
		SCMenuItem.new(defaultMenu, "Line").action_( {WFSPath.line.edit; });
		SCMenuItem.new(defaultMenu, "Rect").action_( {WFSPath.rect.edit; });

		//view
		viewMenu = SCMenuGroup.new(nil, "View",5);
		SCMenuItem.new(viewMenu, "All").action_( {WFSEQ.new; WFSTransport.new; WFSLevelBus.makeWindow;});
		SCMenuItem.new(viewMenu, "EQ").action_( {WFSEQ.new; });
		SCMenuItem.new(viewMenu, "Transport").action_( {WFSTransport.new; });		SCMenuItem.new(viewMenu, "Level").action_( {WFSLevelBus.makeWindow; });
		if(WFSServers.default.isSingle){
			SCMenuItem.new(viewMenu, "Meter").action_({
				ServerMeter(WFSServers.default.masterServer,0,2);
			});
		};

	}
}

