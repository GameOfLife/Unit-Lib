/*
\auxOut

Send sound to one of the 32 internal 'aux' buses. The signal can be received by another UChain using 'auxIn'. On multiple-server systems (such as most WFSCollider WFS systems) you can make sure the signal is sent on all servers by enabling 'global' on the UChain. As an alternative to 'global', it is also possible to use an ugroup; In that case, make sure that the sending ('auxOut') unit is in the same ugroup as the receiving one ('auxIn'). All UChains started after this one will be able to receive the signal using 'auxIn'. If you want to make sure *every* UChain can receive it, set the UChain's addAction to 'addBefore', or that of the receiving chains to 'addToTail' or 'addAfter'. Note that auxOut also applies the UChain's fadeIn/fadeOut and gain settings, and can be used as a final stage in an UChain as well.

bus: the aux bus to send to (0-31)
amp: amplitude multiplier

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

Udef( \auxOut, { |bus = 0, amp = 1, useUChainGain = 1|
	var input;
	input = UIn.ar(0);
	amp = amp / if( useUChainGain, // quick workaround until UEnv supports useUChainGain
		1,
		UIn.getControl( \kr, UIn.getControlName( ), 'gain', 0, 0.5 ).dbamp.max(1.0e-12)
	);
	Out.ar( bus + FirstPrivateBus.ir + 32, input * amp * UEnv.kr( extraSilence: 0.2, useGlobalGain: 0 ) );
})
	.category_( \io )
    .setSpec( \useUChainGain, BoolSpec(true) )
	.setSpec( \bus, PositiveIntegerSpec( 0, 0, 32 ) )