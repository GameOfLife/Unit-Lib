/*
\magSmooth

An FFT-based (spectral) Udef that is able to smoothen the magnitude of an incoming audio signal over time. There is also the option to convolve the signal with noise, effectively randomizing the phase information.

fftSize: (512, 1024, 2048 or 4096) the window size of the FFT. Smaller windows result in faster response but more side effects
smooth: (0-1) amount of smoothing applied (0 means no smoothing, 1 means infinite freeze)
smear: (0-1) amount of smearing across bins applied (0 means no smear, 1 means maximum smear)
amp: amplitude of output signal
noise: the amount of convolved noise signal
seed: random seed of the noise

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

Udef( \magSmooth, { |fftSize = 4096, smooth = 0.0, smear = 0.0, amp = 0.1, noise = 0.0|
	var sig, fft1, fft2, conv, normal, delay;
	URandSeed.ir();
	sig = UIn.ar( 0, Udef.numChannels );
	fft1 = FFT( LocalBuf(fftSize).clear, sig, 0.5, 0);
	fft2 = FFT( LocalBuf(fftSize).clear, WhiteNoise.ar(0.05), 0.5, 0 );
	fft1 = PV_MagSmooth( fft1, smooth );
	fft1 = PV_MagSmear( fft1, smear *  fftSize );
	fft2 = PV_MagMul( fft2, fft1 );
	conv = IFFT.ar( fft2, 0 ) * 1;
	normal = IFFT.ar( fft1, 0 ) * 1;
	sig = XFade2.ar( normal, conv, noise.linlin(0,1,-1,1) );
	UOut.ar( 0, sig * amp );
})	
	.setSpec( \smooth, [0,1, 0.9.calcCurve].asSpec )
	.setSpec( \smear, [0,1, 0.05.calcCurve].asSpec )
	.setSpec( \fftSize, ListSpec([128,256,512,1024,2048,4096,8192]), \init)
	.category_( \effect );