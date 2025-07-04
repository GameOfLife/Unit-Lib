UAuxIn {

	classvar <>offset = 64;
	classvar <>specs;

	*initClass {
		Class.initClassTree(Spec);
		specs = IdentityDictionary();
		specs.put( \bus, PositiveIntegerSpec(0,0,64) );
	}

	*ar { |bus = 0, numChannels = 1|
		 ^In.ar( bus + FirstPrivateBus.ir + this.offset, numChannels );
	}

}

UAuxOut : UAuxIn {

	*ar { |bus = 0, channelsArray|
		 ^Out.ar( bus + FirstPrivateBus.ir + this.offset, channelsArray );
	}

}

UPrivateIn : UAuxIn {

	classvar <>offset = 128;

}

UPrivateOut : UAuxOut {

	classvar <>offset = 128;

}