/*
\outputWithSubwoofer

Sends sounds from the previous units in the UChain to hardware output busses.
Send also the another output for subwoofer mix.

Note to WFSCollider users: This sends only to the outputs of the master server. To send to individual speakers on the WFS system, use wfsIndex instead. To send output "the official way" only to the outputs of the master audio interface, use the wfsMasterOut Udef instead (and set "toServers" to false if you really want the analog outputs of the master interface)

bus: the channels are sent out starting with this bus.
numChannels: number of channels (*).

(*) only the following numbers of channels are allowed:
1,2,3,4,5,6,7,8,10,12,16,24,32

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

MultiChannelUdef( \outputWithSubwoofer, { |bus, subwooferBus=2|
	var sig = UGlobalEQ.ar( UIn.ar( 0, Udef.numChannels ) ) * UEnv.kr;
	Out.ar( subwooferBus, if(sig.size>0){sig.sum}{sig} * UGlobalSubwooferGain.kr );
	Out.ar( bus, sig );
})
.category_( \private )
.setSpec( \bus, PositiveIntegerSpec( 0, 0, 192 ) )
.setSpec( \subwooferBus, PositiveIntegerSpec( 0, 0, 192 ) )
.shouldPlayOnFunc_({ |unit, target|
	target.asTarget.server.isLocal; // safety measure: no output on wfs servers
})