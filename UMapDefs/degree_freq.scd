/*
\degree_freq

Creates an UMap for converting note values to a frequency using specific scales and tunings.

degree: note index (-n - n, depending on numsteps), 0 means first note
offset: and offset value, added to the degree, useful for transposition (-n - n)
base: frequency of the base note (A)
key: key note of the scale
octave: octavve transposition (-4 - 4)
scale: scale name (possible names depending on numSteps)
tuning: tuning setting name (possible names depending on numSteps)
numSteps: number of steps in the scale
type: number of steps per octave of the tuning

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

var allScales, defDefs;

allScales = Order();

if( \ScaleInfo.asClass.notNil ) {
	\ScaleInfo.asClass.scales.keysValuesDo({ |key, value|
		allScales[ value.pitchesPerOctave ] = allScales[ value.pitchesPerOctave ].add( key );
	});
} {
	Scale.all.parent.keysValuesDo({ |key, value|
		allScales[ value.pitchesPerOctave ] = allScales[ value.pitchesPerOctave ].add( key );
	});
};

defDefs = allScales.collectAs({	|scales, ii|
	var tunings, subScales, defs;
	var default;

	default = ( 12: \et12, 24: \et24, 43: \partch )[ ii ];

	if( \TuningInfo.asClass.notNil ) {
		tunings = \TuningInfo.asClass.tunings.select({ |item| item.tuning.size == ii }).keys.asArray.sort;
	} {
		tunings = Tuning.all.parent.select({ |item| item.tuning.size == ii }).keys.asArray.sort;
	};
	scales = scales.sort;

	if( ii == 24 ) {
		tunings.remove( \harmonic );
		tunings = tunings.reverse;
	 };

	subScales = Order();

	scales.do({ |item|
		var sc;
		sc = Scale.newFromKey( item ).degrees.size;
		subScales[ sc ] = subScales[ sc ].add( item );
	});

	defs = subScales.collectAs({ |item, i|
		FuncUMapDef( i, { |unit, degree = 0, offset = 0, base = 440, transpose = 0, tune = 0, key = 0, octave = 0, scale = \chromatic, tuning = 'et12'|

			scale = Scale.newFromKey( scale, tuning );
			if( key != 0 && { i != ii }) {
				scale = Scale(
					(scale.degrees + key).wrap(0,ii).sort,
					scale.pitchesPerOctave,
					scale.tuning
				);
			};
			scale.degreeToFreq( degree + offset, base * (transpose + (tune/100)).midiratio, octave );
		}, addToAll: false )
			.category_( 'convert' )
			.useMappedArgs_( false )
			.canUseUMapFunc_({ |unit, key, umapdef|
				var spec;
				spec = unit.getSpec( key );
				spec.isKindOf( FreqSpec );
			})
			.setSpec( \key, [0,ii-1,\lin,1,0].asSpec, private: i == ii )
			.setSpec( \degree, [i * -2,i * 2,\lin,1,0].asSpec )
			.setSpec( \octave, [ -4, 4,\lin,1,0].asSpec )
			.setSpec( \base, [68.midicps.floor, 70.midicps.ceil,\lin,0,440].asSpec )
		    .setSpec( \transpose, [-12,12,\lin,1,0].asSpec )
		    .setSpec( \tune, [-50,50,\lin,0,0].asSpec )
			.setSpec( \offset, [ i * -1, i, \lin, 1, 0 ].asSpec )
			.setSpec( \scale, ListSpec( item, 0 ) )
			.setSpec( \tuning, ListSpec( tunings, tunings.indexOf( default ) ) )
			.setDefault( \tuning, default );
	}, Array ).reverse;

	MultiUMapDef( ii, defs, \convert, \numSteps, false, addToAll: false );
}, Array);

MultiUMapDef( \degree_freq, defDefs, \convert, \type, false, addToAll: true );


/*

FuncUMapDef( \degree_freq, { |unit, degree = 0, base = 440, octave = 0, scale = \chromatic, tuning = 'et12'|
	Scale.newFromKey( scale, tuning ).degreeToFreq( degree, base, octave );

})
	.category_( 'convert' )
	.useMappedArgs_( false )
	.canUseUMapFunc_({ |unit, key, umapdef|
		var spec;
		spec = unit.getSpec( key );
		spec.isKindOf( FreqSpec );
	})
	.setSpec( \degree, [-24,24,\lin,1,0].asSpec )
	.setSpec( \octave, [ -4, 4,\lin,1,0].asSpec )
	.setSpec( \base, [68.midicps, 70.midicps,\lin,0,440].asSpec )
	.setSpec( \scale, ListSpec( scales, scales.indexOf( 'chromatic' ) ) )
	.setSpec( \tuning, ListSpec( tunings, tunings.indexOf( 'et12' ) ) );

*/
/*
t = Tuning.newFromKey( \et12 );

Tuning.harmonic.octaveRatio

Scale( (..t.tuning.size-1), t.tuning.size, t ).degrees

Scale.chromatic.pitchesPerOctave
0.midiname

Scale.newFromKey( \major )

ScaleInfo.scales.select({ |item| item.pitchesPerOctave != 12 }).keys.asArray.sort;

69 + 60

BoolSpec:massEditSpec

Scale.directory

ScaleInfo.scales.select({ |item|
	item.tuning == Tuning.et12 && {
		item.degrees.size.postln; true
	}
}).dopostln;

ScaleInfo.scales.do({ |item, key|
	key.post; ": ".post; item.tuning.postln;
})

Tuning.et12.asControlInput

Tuning.partch.tuning.plot

Tuning.et12.tuning.size

Scale.chromatic( \just ).degreeToFreq( 0, 440, 0 ).cpsmidi

Scale.zhi

Scale.scriabin.degrees
Scale.scriabin.tuning

TuningInfo.tunings.select({ |item| item.tuning.size != 12 }).dopostln;

kirnberger: Tuning.kirnberger
wcSJ: Tuning.wcSJ
sept2: Tuning.sept2
sept1: Tuning.sept1
vallotti: Tuning.vallotti
reinhard: Tuning.reinhard
young: Tuning.young
mean6: Tuning.mean6
wcHarm: Tuning.wcHarm
mean4: Tuning.mean4
pythagorean: Tuning.pythagorean
werckmeister: Tuning.werckmeister
just: Tuning.just
mean5: Tuning.mean5
et12: Tuning.et12

wcGamma: Tuning.wcGamma
partch: Tuning.partch
et53: Tuning.et53
chalmers: Tuning.chalmers
wcAlpha: Tuning.wcAlpha
johnston: Tuning.johnston
harrison: Tuning.harrison
sruti: Tuning.sruti
et22: Tuning.et22
et24: Tuning.et24
harmonic: Tuning.harmonic
et19: Tuning.et19
catler: Tuning.catler
et31: Tuning.et31
wcBeta: Tuning.wcBeta
bp: Tuning.bp
et41: Tuning.et41


*/
