/*
\auxOut

Send sound to one of the 32 internal 'aux' buses or to an audio output.

In 'internal' mode, yhe signal can be received by another UChain using 'auxIn' (also in 'internal' mode). On multiple-server systems (such as most WFSCollider WFS systems) you can make sure the signal is sent on all servers by enabling 'global' on the UChain. As an alternative to 'global', it is also possible to use an ugroup; In that case, make sure that the sending ('auxOut') unit is in the same ugroup as the receiving one ('auxIn'). All UChains started after this one will be able to receive the signal using 'auxIn'. If you want to make sure *every* UChain can receive it, set the UChain's addAction to 'addBefore', or that of the receiving chains to 'addToTail' or 'addAfter'. Note that auxOut also applies the UChain's fadeIn/fadeOut and gain settings, and can be used as a final stage in an UChain as well.

In 'external' mode, the signal will be sent to an audio output on your audio interface.

bus: the aux bus to send to (0-31)
amp: amplitude multiplier
useUChainGain: (true/false) if true the 'gain' parameter of the UChain will be used to attenuate the level, similar to "post fader send" on most DAW's and mixing desks. When false the UChain gain level is ignored ("pre fader"). Note that fadeIn and fadeOut will always be applied on the aux signal too.
type: \internal or \external

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

(
var defs, def;

defs = [
	Udef( \internal, { |bus = 0, amp = 1, useUChainGain = 1|
		var input;
		input = UIn.ar(0);
		amp = amp / if( useUChainGain, // quick workaround until UEnv supports useUChainGain
			1,
			UIn.getControl( \kr, UIn.getControlName( ), 'gain', 0, 0.5 ).dbamp.max(1.0e-12)
		);
		Out.ar( bus + FirstPrivateBus.ir + 32, input * amp * UEnv.kr( extraSilence: 0.2, useGlobalGain: 0 ) );
	}, addToAll: false, extraPrefix: \auxOut_ )
	.setSpec( \useUChainGain, BoolSpec(true) )
	.setSpec( \bus, PositiveIntegerSpec( 0, 0, 32 ) ),

	Udef( \external, { |bus = 0, amp = 1, useUChainGain = 1|
		var input;
		input = UIn.ar(0);
		amp = amp / if( useUChainGain, // quick workaround until UEnv supports useUChainGain
			1,
			UIn.getControl( \kr, UIn.getControlName( ), 'gain', 0, 0.5 ).dbamp.max(1.0e-12)
		);
		Out.ar( bus, input * amp * UEnv.kr( extraSilence: 0.2, useGlobalGain: 0 ) );
	}, addToAll: false, extraPrefix: \auxOut_ )
	.setSpec( \useUChainGain, BoolSpec(true) )
	.setSpec( \bus, HardwareBusSpec( \output, 1 ) ),
];

def = MultiUdef( \auxOut, defs, \output, \type, false );

def;
)