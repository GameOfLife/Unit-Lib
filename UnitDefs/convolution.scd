/*
\convolution

A realtime convolution udef, that uses partioned convolution, useful for creating reverbs. This udef requires soundfiles in a special format, usually with a .partconv extension. In the graphical interface, if you open a regular soundfile there will be a dialog providing options for conversion to .partconv. There is also a button for generating a random impulse response based on an algorithm by Dan Stowell.

On macOS if you have one of the following plugins/programs installed there will also be an 'import' menu for automatic import of the impulse response files that come with these programs: Apple Logic Pro, Ableton Live with Convolution package, MeldaProduction MConvolution plugin. Most of these are multichannel impulse responses and they will be converted to individual partconv files with name extensions as channel labels.

Convolution currently only supports mono impulse responses, but is quite simple to expand to multichannel via the '+' button in the gui or placing multiple instances consequently in the chain. The gui will provide easy management options for multi-channel .partconv file sets.

file: a PartConvBuffer object; points to a .partconv file on disk.
wet: wet (processed sound) level
dry: dry level

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

(
Udef( \convolution, { |file, wet = 0.3, dry = 1|
	var sig, convolved;
	sig = UIn.ar(0,1);
	convolved = PartConv.ar( sig, 2048, file );
	UOut.ar( 0, (sig * dry) + (convolved * wet) );
})
	.category_( \effect )
	.setSpec( \file, PartConvBufferSpec() )
	.setSpec( \dry, \amp.asSpec )
	.setSpec( \wet, \amp.asSpec )
)

