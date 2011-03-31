WFSLiveOSCResp {
	classvar <>verbose = false, <responders;

	*init {

		this.free;

		responders =
		[
		//SOURCE
		OSCresponderNode(nil,'/wfscollider/source/set',{ |t, r, msg|
			var index, type, x, y, level;
			if (this.verbose, { ("WFSLive: "++msg).postln });
			#index, type, x, y, level = msg[1..];
			WFSLive.tracks[index].setAll(type, x@y, level);

		}).add,
		OSCresponderNode(nil,'/wfscollider/source/mute',{ |t, r, msg|
			var index, bool;
			if (this.verbose, { ("WFSLive: "++msg).postln });
			#index, bool = msg[1..];
			WFSLive.tracks[index].running_( bool.booleanValue.not );
		}).add,
		OSCresponderNode(nil,'/wfscollider/source/position',{ |t, r, msg|
			var index, x, y;
			if (this.verbose, { ("WFSLive: "++msg).postln });
			#index, x, y = msg[1..];
			WFSLive.tracks[index].pos_(x@y);
		}).add,
		OSCresponderNode(nil,'/wfscollider/source/position_polar',{ |t, r, msg|
			var index, ang, dist;
			if (this.verbose, { ("WFSLive: "++msg).postln });
			#index, ang, dist = msg[1..];
			WFSLive.tracks[index].posPolar_( ang, dist );
		}).add,
		OSCresponderNode(nil,'/wfscollider/source/type',{ |t, r, msg|
			var index, type;
			if (this.verbose, { ("WFSLive: "++msg).postln });
			#index, type = msg[1..];
			WFSLive.tracks[index].type_( type.asSymbol );
		}).add,
		OSCresponderNode(nil,'/wfscollider/source/level',{ |t, r, msg|
			var index, level;
			if (this.verbose, { ("WFSLive: "++msg).postln });
			#index, level = msg[1..];
			WFSLive.tracks[index].level_( level.clip(0.0,1.0) ); //we don't want to blow up the speakers...
		}).add,
		//GLOBAL
		OSCresponderNode(nil,'/wfscollider/all',{ |t, r, msg|

			var numOfParemetersPerSource = 4;
			if (this.verbose, { ("WFSLive: "++msg).postln });
			if( msg[1..].size == (8*numOfParemetersPerSource) ) {
				msg[1..].clump(4).do{ |args,index|
					var type, x, y, level;
					#type, x, y, level = args;
					WFSLive.tracks[index].setAll(type, x@y, level);
				};
			}
		}).add,
		OSCresponderNode(nil,'/wfscollider/level',{ |t, r, msg|
			if (this.verbose, { ("WFSLive: "++msg).postln });
			WFSLevelBus.setRaw(WFSLevelBus.ampToRaw(msg[1].clip(0.0,1.0)),updateWindow:true);
		}).add,
		OSCresponderNode(nil,'/wfscollider/start',{ |t, r, msg|
			if (this.verbose, { ("WFSLive: "++msg).postln });
			WFSLive.start;
			WFSLive.changed(\start)
		}).add,
		OSCresponderNode(nil,'/wfscollider/stop',{ |t, r, msg|
			if (this.verbose, { ("WFSLive: "++msg).postln });
			WFSLive.stop;
			WFSLive.changed(\end)
		}).add,
		OSCresponderNode(nil,'/wfscollider/eq/low/freq',{ |t, r, msg|
			if (this.verbose, { ("WFSLive: "++msg).postln });
			WFSEQ.set(\eqLowFr,msg[1].clip(50,1000));
		}).add,
		OSCresponderNode(nil,'/wfscollider/eq/low/gain',{ |t, r, msg|
			if (this.verbose, { ("WFSLive: "++msg).postln });
			WFSEQ.set(\eqLowGain,msg[1].linlin(-1.0,1.0,-24.0,24.0));
		}).add,
		OSCresponderNode(nil,'/wfscollider/eq/mid/freq',{ |t, r, msg|
			if (this.verbose, { ("WFSLive: "++msg).postln });
			WFSEQ.set(\eqMidFr,msg[1].clip(167,6000));
		}).add,
		OSCresponderNode(nil,'/wfscollider/eq/mid/q',{ |t, r, msg|
			if (this.verbose, { ("WFSLive: "++msg).postln });
			WFSEQ.set(\eqMidRQ,msg[1].clip(0.1,10));
		}).add,
		OSCresponderNode(nil,'/wfscollider/eq/mid/gain',{ |t, r, msg|
			if (this.verbose, { ("WFSLive: "++msg).postln });
			WFSEQ.set(\eqMidGain,msg[1].linlin(-1.0,1.0,-24.0,24.0));
		}).add,
		OSCresponderNode(nil,'/wfscollider/eq/high/freq',{ |t, r, msg|
			if (this.verbose, { ("WFSLive: "++msg).postln });
			WFSEQ.set(\eqHighFr,msg[1].clip(1000,10289));
		}).add,
		OSCresponderNode(nil,'/wfscollider/eq/high/gain',{ |t, r, msg|
			if (this.verbose, { ("WFSLive: "++msg).postln });
			WFSEQ.set(\eqHighGain,msg[1].linlin(-1.0,1.0,-24.0,24.0));
		}).add,

		//INFO
		OSCresponderNode(nil,'/wfscollider/info/source',{ |t, r, msg, addr|
			var pos;
			if (this.verbose, { ("WFSLive: "++msg).postln });
			if( msg.size == 2) {
				pos = WFSLive.positions[msg[1]];
				addr.sendMsg('/wfscollider/info/source',
					WFSLive.tracks[msg[1]].type,
					pos.x,
					pos.y,
					WFSLive.tracks[msg[1]].level
				)
			}

		}).add,
		OSCresponderNode(nil,'/wfscollider/info/all',{ |t, r, msg, addr|
			var pos, data;
			if (this.verbose, { ("WFSLive: "++msg).postln });
			if( msg.size == 1) {
				pos = WFSLive.positions;
				data = [WFSLive.tracks.collect(_.type), pos.collect(_.x), pos.collect(_.y), WFSLive.tracks.collect(_.level) ];
				addr.sendMsg(*(['/wfscollider/info/source']++data.flop.flat))
			}
		}).add
		]

	}

	*free {
		if(responders.notNil){
			responders.do(_.remove);
			responders = nil;
		}
	}
}

