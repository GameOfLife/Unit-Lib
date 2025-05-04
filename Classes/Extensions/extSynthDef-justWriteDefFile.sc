+ SynthDef {
	justWriteDefFile {	arg dir, overwrite = (true);
		super.writeDefFile(name, dir, overwrite);
	}

	uWriteDefFile {	arg dir, overwrite = (true), makeSynthDesc = (false);
		if( makeSynthDesc ) {
			this.writeDefFile(dir, overwrite);
		} {
			super.writeDefFile(name, dir, overwrite);
		};
	}

	uLoad {	arg server, completionMsg, dir(synthDefDir), makeSynthDesc = (false);
		server = server ? Server.default;
		dir = dir ? synthDefDir;
		this.uWriteDefFile( dir, makeSynthDesc: makeSynthDesc );
		server.asCollection.do({ |srv|
			srv.sendMsg("/d_load", dir ++ name ++ ".scsyndef", completionMsg)
		});
	}

}

	