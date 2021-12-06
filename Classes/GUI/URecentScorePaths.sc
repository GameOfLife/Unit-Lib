URecentScorePaths {
	classvar <>filePath;
	classvar <>dict;
	classvar <>maxSize = 30;
	classvar <>menu;

	*initClass {
		filePath = Platform.userAppSupportDir +/+ "recent_uscore_files.scd";
		dict = [];
		this.readPrefs;
	}

	*readPrefs {
		if( File.exists( filePath ) ) {
			File.use( filePath, "r", { |file|
				dict = file.readAllString.interpret;
			});
		}
	}

	*writePrefs {
		File.use( filePath, "w", { |file|
			file.write( dict.cs );
		});
	}

	*clear {
		dict = [];
		this.writePrefs;
	}

	*addPath { |path|
		var item;
		item = dict.detect({ |pth| pth[1] == path });
		if( item.notNil ) {
			item[0] = Date.localtime.stamp;
		} {
			dict = dict.add( [ Date.localtime.stamp, path ] );
		};
		dict = dict.sort({ |a,b| a[0] >= b[0] });
		if( dict.size > maxSize ) { dict = dict[..maxSize-1]; };
		this.writePrefs;
		{ this.fillMenu; }.defer(0.1);
	}

	*addPaths { |paths|
		paths.do({ |path|
			var item;
			item = dict.detect({ |pth| pth[1] == path });
			if( item.notNil ) {
				item[0] = Date.localtime.stamp;
			} {
				dict = dict.add( [ Date.localtime.stamp, path ] );
			};
		});
		dict = dict.sort({ |a,b| a[0] >= b[0] });
		if( dict.size > maxSize ) { dict = dict[..maxSize-1]; };
		this.writePrefs;
		{ this.fillMenu; }.defer(0.1);
	}

	*removePath { |path|
		var item;
		item = dict.detect({ |pth| pth[1] == path });
		if( item.notNil ) {
			dict.remove( item );
			this.writePrefs;
			{ this.fillMenu; }.defer(0.1);
		};
	}

	*pathList { |filterOpen = true|
		var nowOpen;
		if( filterOpen && { UScoreEditorGUI.all.notNil }) {
			nowOpen = UScoreEditorGUI.all.collect({ |gui| gui.score.filePath }).select(_.notNil);
			^dict.select({ |item| nowOpen.includesEqual( item[1] ).not }).collect(_[1]);
		} {
			^dict.collect(_[1]);
		}
	}

	*fillMenu { |menu|
		menu = menu ?? { this.menu; };
		if( menu.notNil ) {
			menu.clear;
			this.pathList.do({ |path|
				menu.addAction( MenuAction( path, { UScore.open( path, _.gui ) }) );
			});
			menu.addAction( MenuAction.separator );
			menu.addAction( MenuAction( "Clear", { this.clear; this.fillMenu( menu ) } ));
		}
	}
}