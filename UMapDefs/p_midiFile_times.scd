/*
\p_midiFile_times

Creates an pattern UMap for extracting time data from Standard MIDI Files (.mid/.smf). There are currently two data types available; timeToNext and sustain, both of which extract timing of noteOn/noteOff events in the MIDI file. The notes are extracted in order of appearance, and can be filtered by channel. Typical use of this UMap would be on an UPattern with 'sustain_time', where both sustain and timeToNext receive an instance of this UMap, with corresponding 'type' setting. Tempo data found in the MIDI file is also used. The 'p_midiFile' UMap can then be used on other units in the UPattern to extract corresponding note or velocity values.

MIDI files (UMIDIFile) are loaded globally, and all currently loaded files will be shown in a popup menu in the GUI, next to the buttons for loading new files and refreshing the current file. Refreshing the file in one instance of the UMap will also refresh in all other instances using the same MIDI file.

midiFile: the midi file (an UMIDIFile object).
channel: the channel to listen to (can be nil / 'any')
type: type of data to extract;
	\timeToNext: time until the next note will play
	\sustain: duration of the current note
scale: applies a scaling factor (0.1-10) to the time.
add: can add an amount of extra time to the (scaled) value, in seconds

Note: the first note found in the midifile will always be played immediately; any rests before the first note are ignored.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UPatDef( \p_midiFile_times, { |unit, midifile, channel = nil, type = \timeToNext, scale = 1.0, add = 0|
	{
		var smf, nmf, notes, index = 0, chn, cmd, lastChn = -1, current = 69;
		var ffrange, crve, trange, value, ttype, lastType;
		var default;
		var startPos = UPattern.startPos;
		inf.do { |i|
			nmf = midifile.next.midiFile;
			if( nmf.notNil ) {
				if( smf != nmf ) {
					smf = nmf;
					lastChn = -1;
				};
			} {
				smf = nil;
			};
			chn = channel.next;
			ttype = type.next;
			if( chn != lastChn or: { ttype != lastType }) {
				if( smf.notNil ) {
					switch( ttype,
						\timeToNext, {
							notes = (smf.timeMode_( \seconds )
								.noteSustainEvents( chn )
								.sort({ |a,b| a[1] <= b[1] })
							    .select({ |item| item[1] >= startPos })
							.flop[ 1 ] - startPos).differentiate;
						},
						\sustain, {
							notes = [0] ++ (smf.timeMode_( \seconds )
								.noteSustainEvents( chn )
								.sort({ |a,b| a[1] <= b[1] })
								.select({ |item| item[1] >= startPos })
								.flop[ 6 ])
						}, {
							notes = [];
						}
					);
				} {
					notes = [];
				};
			};
			lastChn = chn;
			lastType = ttype;
			current = notes[ index ] ?? { ( \timeToNext: inf, \sustain: 0 )[ ttype ]; };
			index = index + 1;
			((current * scale.next) + add.next).yield;
		};
	}.r;
})
.useMappedArgs_( false )
.canUseUMapFunc_({ |unit, key, umapdef|
	([\time, \timeToNext, \sustain ].includes( key ) ) or: {
		unit.getSpec( key ).isKindOf( SMPTESpec ) or: {
			unit.getSpec( key ).isKindOf( UAdaptSpec )
	} }
})
.setSpec( \midifile, UMIDIFileSpec() )
.setDefault( \midifile, UMIDIFile() )
.setSpec( \channel, ListSpec( [ nil ] ++ (..15), 0, [ "any" ] ++ (..15).collect(_.asString) ) )
.setSpec( \type, ListSpec( [ \timeToNext, \sustain ] ) )
.setSpec( \scale, [0.1,10,\exp,0,1].asSpec )
.setSpec( \add, SMPTESpec() )
.setDefault( \channel, nil )
.category_( 'pattern_midiFile' );
