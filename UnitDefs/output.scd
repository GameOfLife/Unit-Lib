/*
\output

Sends sounds from the previous units in the UChain to hardware output busses.
WFSCollider users: This sends only to the outputs of the master server. To send to individual speakers on the WFS system, use wfsIndex instead. To send output "the official way" only to the outputs of the master audio interface, use the wfsMasterOut Udef instead (and set "toServers" to false if you really want the analog outputs of the master interface)

bus: the channels are sent out starting with this bus.
numChannels: number of channels (*).

(*) only the following numbers of channels are allowed:
1,2,3,4,5,6,7,8,10,12,16,24,32

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

var def = MultiChannelUdef( \output, { |bus, useGlobalGain = 1|
	Out.ar( bus, UGlobalEQ.ar( UIn.ar( 0, Udef.numChannels, endPoint: true ) ) * UEnv.kr(
		extraSilence: 0.2,
		useGlobalGain: useGlobalGain
	) );
})
	.category_( \output )
    .setSpec( \useGlobalGain, BoolSpec(true) )
	.shouldPlayOnFunc_({ |unit, target|
		target.asTarget.server.isLocal; // safety measure: no output on wfs servers
	});

def.udefs.do({ |def|
	def.setSpec( \bus, HardwareBusSpec(\output, def.name ) );
});

def