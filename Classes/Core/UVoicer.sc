UVoicer : UEvent {

	classvar <>currentNote, <>currentAmp;

	var <chain;
	var <>events;
	var <>device = '*/*';
	var <>channel = 0;
	var <>noteRange = #[0,127];

	*new { |chain, startTime = 0, track = 0, duration = inf, releaseSelf = false|
		chain = chain ?? { UChain.default };
		^super.newCopyArgs
			.chain_( chain )
			.startTime_( startTime )
			.track_( track ? 0 )
			.releaseSelf_( releaseSelf )
			.duration_( duration )
			.init
	}

	init {
		events = Order();
		this.changed( \init );
	}

	releaseSelf_ { |bool|
		if(releaseSelf != bool) {
	        releaseSelf = bool;
	        this.changed( \releaseSelf );
        };
    }

	update { |obj, what, src, chan, num, val|
		if( obj == UMIDIDict && {
			what === \note
		} && {
			UMIDIDict.matchDevice( device, src );
		} && {
			num.inclusivelyBetween( *noteRange );
		}) {
			if( val > 0 ) {
				this.startEvent( num, val / 127 );
			} {
				this.endEvent( num );
			};
		};
	}

    chain_ { |aChain|
	    chain = aChain;
	    this.changed( \chain );
    }

    duration_{ |dur|
        duration = dur;
        this.changed( \dur );
    }

    prPrepareUnit { |unit|
		var prepareThese;
		prepareThese = unit.valuesToPrepare.select({ |item|
			item.isKindOf( UMap );
		});
		prepareThese.do({ |umap|
			if( umap.subDef.isKindOf( UPatDef ).not ) {
				this.prPrepareUnit( umap );
			};
		});
		prepareThese.do({ |umap|
			 if( umap.subDef.isKindOf( FuncUMapDef ) ) {
				umap.next;
			 };
		});
	}

	prPatternsToValues { |unit|
		unit.args.pairsDo({ |key, item|
			if( item.isKindOf( UMap ) ) {
				if( [ FuncUMapDef, ValueUMapDef ].any({ |def| item.subDef.isKindOf( def ) }) ) {
					if( item.subDef.isKindOf( ControllerUMapDef ) ) {
						item.deactivateOnEnd = true;
					} {
						unit.set( key, item.value );
						item.deactivate;
					}
				} {
					this.prPatternsToValues( item );
				};
			};
		});
	}

	nextChain { |startTime = 0, note = 69, value = 0.5|
		var next, was, ctrl;
		next = chain.deepCopy;
		next.startTime = startTime;
		next.voicerNote = note;
		next.voicerValue = value;
		next.releaseSelf_( true );
		next.parent = this;
		if( next.class == UChain ) {
			was = UChain.nowPreparingChain;
			UChain.nowPreparingChain = next;
			next.units.do({ |unit|
				this.prPrepareUnit( unit );
				this.prPatternsToValues( unit );
			});
			UChain.nowPreparingChain = was;
		};
		ctrl = SimpleController( next )
			.put( \end, {
				if( next.isKindOf( UPattern ) or: {
					next.units.every({ |unit| unit.synths.size == 0 })
				} ) {
					events[ note ].remove( next );
					ctrl.remove;
				};
			});
		^next;
	}

	startEvent { |note = 69, value = 0.5|
		events[ note ] = events[ note ].add(
			this.nextChain( this.startTime, note, value ).prepareAndStart
		);
	}

	endEvent { |note = 69, releaseTime|
		events[ note ].do(_.release( releaseTime ));
	}

	endAll { |releaseTime|
		events.do({ |evts| evts.do(_.release( releaseTime )); });
	}

	allEvents { ^events.flatten(1) }
}