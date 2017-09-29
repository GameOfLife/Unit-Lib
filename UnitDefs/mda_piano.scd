/*
\mda_piano

A piano synthesiser (originally a VST plugin by Paul Kellett, ported to SC by Dan Stowell). Plays a single piano note at any desired frequency. The Udef provides a 2-channel output.

freq: pitch of the piano note.
vel: velocity
decay: decay time (0-1)
hard: hardness of attack (0-1)
muffle: amount of muffling (0-1)
stereo: amount of stereo width (0-1)
sustain: if on, the piano sustain pedal is emulated
amp: amplitude (0-1)
trigger: retriggers the note at the current freq.

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

Udef(\mda_piano,{ |freq = 440, vel = 0.8, decay = 0.8, hard = 0.8, muffle = 0.8, stereo = 0.2, sustain = 0, amp = 0.1|
	var sig, detune, midi, trigger;
	trigger = \trigger.tr(0);
	midi = freq.clip(20,20000).cpsmidi;
	detune = ((midi - midi.round(1))) + 0.5;
	freq = midi.round(1).midicps;
	sig = MdaPiano.ar( freq, 1 - trigger, vel * 127, decay, 0.8, hard, 0.8, muffle, 0.8, 0.8, stereo, detune, 0.1, 0.1, sustain ) * amp;
	UMixOut.ar(0, sig, 0, true );
}).category_( \synthesis )
.setSpec( \trigger, TriggerSpec() )
.setSpec( \gate, BoolSpec(true) )
.setSpec( \sustain, BoolSpec(false) );