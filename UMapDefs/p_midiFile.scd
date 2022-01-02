/*
\p_midiFile

Creates an pattern UMap for extracting data from Standard MIDI Files (.mid/.smf). There are currently three data types available; note (note number), velocity and cc (control). The values are extracted in order of appearance, and can be filtered by channel, and, in case of control, controller number. Use this UMap in note/velocity mode in combination with the p_midiFile_times UMap if you want to playback a sequence from a MIDI file. Beware that MIDI channel numbers start counting at zero (0).

MIDI files (UMIDIFile) are loaded globally, and all currently loaded files will be shown in a popup menu in the GUI, next to the buttons for loading new files and refreshing the current file. Refreshing the file in one instance of the UMap will also refresh in all other instances using the same MIDI file.

midiFile: the midi file (an UMIDIFile object).
channel: the channel to listen to (can be nil / 'any')
cc: (only for \cc type) the controller number to listen to
fromRange: the range of values to use
toRange: the value range to convert to
invert: invert the mapping range (high input values become low output values and v.v.)
clipMode: sets the way values out of 'fromRange' are used
	\ignore: values outside the range are not used; the UMap will output the last known value
	\clip: limits the values to the outer bounds of toRange
	\fold: mirrors values that are out of toRange back in
	\wrap: wraps values around toRange
	\none: no clipping; values outside of fromRange will be extrapolated outside of toRange
clipSoftness: amount of softening of the edges in case of \clip and \fold modes
type: \note, \velocity or \cc

The 'toRange' arg range is mapped to that of the parameter to which the UMap is connected

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

var defs, scaleFunc;

scaleFunc = { |value, fromRange = #[0,127], invert = false, curve = 0, clipMode = \ignore, clipSoftness = 0|
	value = if (abs(curve) < 0.001) {
		value = value.linlin( *fromRange ++ [ -1, 1, \none ] );
	} {
		value = value.lincurve( *fromRange ++ [ -1, 1, curve, \none ] );
	};
	if( invert.booleanValue ) {
		value = value.neg;
	};
	switch( clipMode,
		\clip, { value = value.softclip2(1, clipSoftness ) },
		\fold, { value = value.softfold2(1, clipSoftness ) },
		\wrap, { value = value.wrap2(1) }
	);
	value;
};

defs = [
	UPatDef( \note, { |unit, midiFile, channel = nil, fromRange = #[0,127], toRange = #[0,1],
			invert = false, curve = 0, clipMode = \ignore, clipSoftness = 0|
		{
			var smf, nmf, notes = [], index = 0, chn, cmd, lastChn = -1, current = 69;
			var ffrange, crve, trange, value, ttype, lastType;
			inf.do { |i|
				nmf = midiFile.next.midiFile;
				if( nmf.notNil ) {
					if( smf != nmf ) {
						smf = nmf;
						lastChn = -1;
					};
				} {
					smf = nil;
				};

				chn = channel.next;
				if( chn != lastChn ) {
					if( smf.notNil ) {
						notes = smf.realNoteOnEvents( chn )
							.sort({ |a,b| a[1] <= b[1] })
							.flop[ 4 ] ? [];
					} {
						notes = [];
					};
				};
				lastChn = chn;

				cmd = clipMode.next;
				ffrange = fromRange.next;

				if( cmd == \ignore ) {
					if( notes[ index ].notNil && { notes[ index ].inclusivelyBetween( *ffrange ) } ) {
						current = notes[ index ];
					}
				} {
					current = notes[ index ] ? current;
				};

				index = (index + 1).wrap( 0, notes.size-1 );
				value = scaleFunc.( current, ffrange, invert.next, curve.next, cmd, clipSoftness.next );
				value.linlin(-1,1,*toRange.next ++ [ \none ]).yield;
			};
		}.r;
	}, addToAll: false ).mappedArgs_([\toRange])
		.setSpec( \midiFile, UMIDIFileSpec() )
		.setDefault( \midiFile, UMIDIFile() )
		.setSpec( \channel, ListSpec( [ nil ] ++ (..15), 0, [ "any" ] ++ (..15).collect(_.asString) ) )
		.setSpec( \fromRange, RangeSpec(0,127,step:1) )
		.setSpec( \toRange, RangeSpec(0,1) )
		.setSpec( \invert, BoolSpec(false) )
		.setSpec( \curve, [-16,16,\lin,0,0].asSpec )
		.setSpec( \clipMode, ListSpec( [ \ignore, \clip, \fold, \wrap, \none ] ) )
		.setSpec( \clipSoftness, [0,1].asSpec )
		.setDefault( \channel, nil )
		.category_( 'pattern' ),

	UPatDef( \velocity, { |unit, midiFile, channel = nil, fromRange = #[1,127], toRange = #[0,1],
			invert = false, curve = 0, clipMode = \ignore, clipSoftness = 0|
		{
			var smf, nmf, notes = [], index = 0, chn, cmd, lastChn = -1, current = 69;
			var ffrange, crve, trange, value, ttype, lastType;
			inf.do { |i|
				nmf = midiFile.next.midiFile;
				if( nmf.notNil ) {
					if( smf != nmf ) {
						smf = nmf;
						lastChn = -1;
					};
				} {
					smf = nil;
				};

				chn = channel.next;
				if( chn != lastChn ) {
					if( smf.notNil ) {
						notes = smf.realNoteOnEvents( chn )
							.sort({ |a,b| a[1] <= b[1] })
							.flop[ 5 ] ? [];
					} {
						notes = [];
					};
				};
				lastChn = chn;

				cmd = clipMode.next;
				ffrange = fromRange.next;

				if( cmd == \ignore ) {
					if( notes[ index ].notNil && { notes[ index ].inclusivelyBetween( *ffrange ) } ) {
						current = notes[ index ];
					}
				} {
					current = notes[ index ] ? current;
				};

				index = (index + 1).wrap( 0, notes.size-1 );
				value = scaleFunc.( current, ffrange, invert.next, curve.next, cmd, clipSoftness.next );
				value.linlin(-1,1,*toRange.next ++ [ \none ]).yield;			};
		}.r;
	}, addToAll: false ).mappedArgs_([\toRange])
		.setSpec( \midiFile, UMIDIFileSpec() )
		.setDefault( \midiFile, UMIDIFile() )
		.setSpec( \channel, ListSpec( [ nil ] ++ (..15), 0, [ "any" ] ++ (..15).collect(_.asString) ) )
		.setSpec( \fromRange, RangeSpec(1,127,step:1) )
		.setSpec( \toRange, RangeSpec(0,1) )
		.setSpec( \invert, BoolSpec(false) )
		.setSpec( \curve, [-16,16,\lin,0,0].asSpec )
		.setSpec( \clipMode, ListSpec( [ \ignore, \clip, \fold, \wrap, \none ] ) )
		.setSpec( \clipSoftness, [0,1].asSpec )
		.setDefault( \channel, nil )
		.category_( 'pattern' ),

	UPatDef( \cc, { |unit, midiFile, cc = 0, channel = nil, fromRange = #[1,127], toRange = #[0,1],
			invert = false, curve = 0, clipMode = \ignore, clipSoftness = 0|
		{
			var smf, nmf, notes = [], index = 0, ccx, lastCc, chn, cmd, lastChn = -1, current = 69;
			var ffrange, crve, trange, value, ttype, lastType;
			inf.do { |i|
				nmf = midiFile.next.midiFile;
				if( nmf.notNil ) {
					if( smf != nmf ) {
						smf = nmf;
						lastChn = -1;
					};
				} {
					smf = nil;
				};
				chn = channel.next;
				ccx = cc.next;
				if( chn != lastChn or: { ccx != lastCc } ) {
					if( smf.notNil ) {
						notes = smf.controllerEvents(ccx, chn )
							.sort({ |a,b| a[1] <= b[1] })
							.flop[ 5 ] ? [];
					} {
						notes = [];
					};
				};
				lastChn = chn;
				lastCc = ccx;
				cmd = clipMode.next;
				ffrange = fromRange.next;
				if( cmd == \ignore ) {
					if( notes[ index ].notNil && { notes[ index ].inclusivelyBetween( *ffrange ) } ) {
						current = notes[ index ];
					}
				} {
					current = notes[ index ] ? current;
				};

				value = scaleFunc.( current, ffrange, invert.next, curve.next, cmd, clipSoftness.next );
				index = (index + 1).wrap( 0, notes.size-1 );
				value.linlin(-1,1,*toRange.next ++ [ \none ]).yield;
			};
		}.r;
	}, addToAll: false ).mappedArgs_([\toRange])
		.setSpec( \midiFile, UMIDIFileSpec() )
		.setDefault( \midiFile, UMIDIFile() )
		.setSpec( \cc, [0,127,\lin,1,0].asSpec )
		.setSpec( \channel, ListSpec( [ nil ] ++ (..15), 0, [ "any" ] ++ (..15).collect(_.asString) ) )
		.setSpec( \fromRange, RangeSpec(0,127,step:1) )
		.setSpec( \toRange, RangeSpec(0,1) )
		.setSpec( \invert, BoolSpec(false) )
		.setSpec( \curve, [-16,16,\lin,0,0].asSpec )
		.setSpec( \clipMode, ListSpec( [ \ignore, \clip, \fold, \wrap, \none ] ) )
		.setSpec( \clipSoftness, [0,1].asSpec )
		.setDefault( \channel, nil )
		.category_( 'pattern' ),

	];

MultiUMapDef( \p_midiFile, defs, \pattern_midiFile, \type, false )
	.mappedArgs_([ \toRange ])
	.defaultDefName_( \note )
	.allowedModes_([ \init, \sync, \normal ])