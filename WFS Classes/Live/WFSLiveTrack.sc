WFSLiveTrack {

	var <in=0, <servers, <level = 1, <type = \point, <pos;
	var <buffers, <switchBuffers, <synths, <isRunning = true;

	*new { |in = 0, servers, level = 1, type = \point, pos = (0@0)|
		^super.newCopyArgs(in, servers, level, type, pos).init
	}

	init {
		buffers = [];
		switchBuffers = [];
	}

	loadBuffers {

		buffers = servers.collect({ |server| // buffers
			WFSPan2D.makeBuffer( server );
		});

		switchBuffers = servers.collect({ |server| // buffers
			WFSPan2D.makeBuffer( server );
		});
	}

	freeBuffers {
		buffers.do(_.free);
		switchBuffers.do(_.free);
		buffers = [];
		switchBuffers = [];
	}

	prCreateSynth {
		^servers.collect{ |server,i|

			Synth.perform(
				[\newPaused,\new][isRunning.binaryValue],
				"wfsLive_"++type.asString,
				([
				\i_bufD, buffers[i],
				\i_in, in,
				\x, pos.x,
				\y, pos.y,
				\level, level
				]
				++ WFSEQ.currentArgsDict.asArgsArray ++
				if(type == \point) { [\i_bufNumSw, switchBuffers[i] ] })
				, server
			)
		}
	}

	start {
		if( buffers.size > 0 ) {
			this.end;
			synths = this.prCreateSynth
		}
	}

	end {
		if( isRunning ) {
			synths.do(_.release)
		} {
			synths.do( _.free )
		};
		synths = [];
	}

	endAndFreeBuffers {
		this.end;
		this.freeBuffers;
	}

	// setting values
	level_ { |alevel = 1, changed = true|
		level = alevel;
		synths.do({ |synth|
			synth.set( \level, level );
		});
		this.changed(\level)

	}

	pos_ { |aPos = (0@0), changed = true|
		pos = aPos.asPoint;
		synths.do({ |synth|
			synth.set( \x, pos.x, \y, pos.y );
		});
		this.changed(\pos)
	}

	posPolar_ { |ang = 0, dist = 1, changed = true|
		var x,y;

		x = sin(ang*2pi/360)*dist;
		y = cos(ang*2pi/360)*dist;
		pos = x@y;
		synths.do({ |synth|
			synth.set(\x, x, \y, y);
		});
		this.changed(\pos)

	}

	type_ { |aType = \point, changed = true|
		type = aType;
		this.start;
		this.changed(\type)
	}

	setAll{ |atype = \point, aPos, aLevel, changed = true|
		if( atype != type ) {
			type = atype;
			pos = aPos;
			level = aLevel;
			this.start;
		} {
			this.pos_(aPos);
			this.level_(aLevel);
		};
		this.changed(\all)
	}

	isRunning_ { |bool = true, changed = true|
		isRunning = bool;
		synths.do{ |synth|
			synth.run(bool)
		};
		this.changed(\isRunning)
	}
}