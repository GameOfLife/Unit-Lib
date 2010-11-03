WFSClickTrack {
	classvar <>synth, <>buffer, <>verbose = true;
	
	*loadSynthDef {
		SynthDef( "wfs_playClickTrack", { |bufnum = 0, out = 0, amp = 1|
					var sig;
					sig = DiskIn.ar( 1, bufnum ) * amp;
					Out.ar( out, sig.dup );
					Out.ar( 10, sig.dup ); // sp/dif
					}).load(  WFSServers.default.m );
		}
	*cmdPeriod { if( buffer.notNil ) { synth = nil; buffer.close; buffer.free; buffer = nil; 
				if( verbose ) { "WFSClickTrack: freed buffer at cmdPeriod".postln; } }  }
		
	*prepare { |path, time = 0| 
		if( path.notNil )
			{ buffer = Buffer.cueSoundFile( WFSServers.default.m, path, time * 44100, 1 ); 
			this.loadSynthDef;
			if( verbose ) { "WFSClickTrack: loaded buffer '%'\n".postf( path ); }; };
		}
		
	*start { if( buffer.notNil )
			{ synth = Synth( "wfs_playClickTrack", [\bufnum, buffer ], WFSServers.default.m ); 
			  CmdPeriod.doOnce( this );
			  if( verbose ) { "WFSClickTrack: started".postln; };
				}; }
	
	*stop { if( synth.notNil )
			{ synth.free; synth = nil; buffer.close; buffer.free; buffer = nil;
			 if( verbose ) { "WFSClickTrack: stopped".postln; }; } 
		 } 
 
	}