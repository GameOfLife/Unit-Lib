+ String {

	// bypass cents and alt methods, causes crashes on post 3.14.0-dev
	uNamemidi { arg cents;
		// format "[notename][bb/b/#/x][octave(-9/9)]"
		// examples: "C#-2" , "Gbb7"
		// min notenr = -84 (C-9)
		// max notenr = 145 (Bx9)
		var notename, addition, octave = 0;
		cents = cents ? 0;
		^(this.notesDict.at(this.getNote) + 24
			+ this.uGetAlt + (this.getOctave * 12) + (cents * 0.01) );
	}

	uNamecps { arg cents;
		^this.uNamemidi( cents ).midicps;
	}

	uGetAlt {
		var addition = 0;
		this.do({ |item|
			if (item == $#) {addition = addition + 1};
			if (item == $b) {addition = addition - 1};
			if (item == $x) {addition = addition + 2};
		});
		^addition
	}
}