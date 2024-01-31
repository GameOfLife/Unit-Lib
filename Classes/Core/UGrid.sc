UGrid : UEvent {

	var <>events;
	var <>selected;
	var <>exclusiveMode; // \h, \v, \all, nil
	classvar <>defaultNCols = 8; // only used in case of creation with array

	*new { |events, nCols|
		^super.new.init( events, nCols );
	}

	init { |inEvents, nCols|
		nCols = nCols ? defaultNCols;
		case { inEvents.isArray } {
			events = Order();
			if( inEvents[0].isArray ) {
				inEvents.do({ |arr, row|
					arr.do({ |evt, col|
						this.put( row, col, evt );
					});
				});
			} {
				inEvents.do({ |evt, i|
					this.put( (i/nCols).asInteger, i.asInteger % nCols, evt );
				});
			};
		} { inEvents.isKindOf( UEvent ) } {
			events = Order();
			this.put( 0, 0, inEvents );
		} {
			events = inEvents;
		};
		this.changed( \init );
	}

	at { |row = 0, col|
		if( col.isNil ) {
			^events !? { events[row] };
		} {
			^events !? { events[row] !? { events[row][col] } };
		};
	}

	put { |row = 0, col, event|
		if( col.isNumber ) {
			this.putEvent( row, col, event );
		} {
			this.putRow( row, event );
		};
	}

	nRows {
		^(events !? { |x| x.indices.maxItem; } ? -1) + 1;
	}

	nCols { |row|
		^(events !? { |x|
			if( row.notNil ) {
				x[row] !? { |y| y.indices.maxItem; } ? -1;
			} {
				x.collect({ |row|
					row.indices.maxItem
				}).maxItem ? -1;
			};
		} ? -1) + 1
	}

	rowAsArray { |row = 0|
		var nCols;
		nCols = this.nCols( row );
		^if( nCols > 0 ) {
			Array.fill( nCols, { |i| this[row,i] });
		} {
			[]
		};
	}

	colAsArray { |col = 0|
		var nRows;
		nRows = this.nRows;
		^if( nRows > 0 ) {
			Array.fill( nRows, { |i| this[i,col] });
		} {
			[]
		};
	}

	eventsInRow { |row = 0|
		^this.rowAsArray(row).select(_.isKindOf( UEvent ) );
	}

	eventsInCol { |col = 0|
		^this.colAsArray(col).select(_.isKindOf( UEvent ) );
	}

	allEvents {
		^events.collectAs({ |item|
			item.selectAs(_.isKindOf( UEvent ), Array)
		}, Array).flatten(1);
	}

	asArray {
		^this.nRows.collect({ |row|
			(events[ row ].indices.maxItem + 1).collect({ |col|
				events[ row ][ col ];
			});
		});
	}

	eventsDo { |func, row, col|
		if( row.isNil ) {
			if( col.isNil ) {
				this.allEvents.do(func);
			} {
				this.eventsInCol(col).do(func);
			};
		} {
			if( col.isNil ) {
				this.eventsInRow(row).do(func);
			} {
				this[ row,col ] !? func;
			};
		};
	}

	prepareAndStart { |target, startPos, row = 0, col = 0|
		this.eventsDo( _.prepareAndStart( target, startPos ), row, col );
	}

	stop { |row, col|
		this.eventsDo( _.stop, row, col );
	}

	release { |row, col, exclude|
		this.eventsDo( _.release, row, col );
	}

	doAt { |row = 0, col = 0, func, exclusive|  // exclusive \h, \v, \all, \none, nil (default)
		var evtToApply;
		evtToApply = this[row,col];
		exclusive = exclusive ? exclusiveMode;
		this.eventsDo({ |evt|
			if( evt === evtToApply ) {
				func.value( evtToApply );
			} {
				evt.release; if( evt.isKindOf( UScore ) ) { evt.pos = 0 };
			};
		}, *switch( exclusive,
			\h, [row,nil],
			\v, [nil,col],
			\all, [nil,nil],
			[row,col] )
		);
	}

	startAt { |row = 0, col = 0, exclusive, force = false|
		this.doAt( row, col, { |evt|
			if( evt.isPlaying.not ) {
				evt.prepareAndStart
			} {
				if( force ) {
					evt.stop;
					evt.prepareAndStart;
				};
			}
		}, exclusive )
	}

	toggleAt { |row = 0, col = 0, exclusive|
		this.doAt( row, col, { |evt|
			if( evt.isPlaying.not ) {
				evt.prepareAndStart
			} {
				evt.release;
			}
		}, exclusive )
	}

	toggleOrNextAt { |row = 0, col = 0, exclusive|
		this.doAt( row, col, { |evt|
			if( evt.isKindOf( UScore ) && {
				evt.events.select(_.isKindOf(UMarker)).any({ |um|
					um.autoPause == true && { um.disabled.not }
				})
			} ) {
				evt.playAnyway;
			} {
				if( evt.isPlaying.not ) {
					evt.prepareAndStart
				} {
					evt.release;
				};
			}
		}, exclusive )
	}

	startInRow { |row = 0, col = 0, force = false| // start event in row, stop the rest
		this.startAt( row, col, \h, force );
	}

	doInRow { |row = 0, col = 0, func, othersFunc|
		var selectedEvent;
		selectedEvent = this[row,col];
		othersFunc = othersFunc ?? { _.release };
		this.eventsDo({ |evt|
			if( evt == selectedEvent ) {
				func.( evt );
			} {
				othersFunc.( evt );
			};
		}, row)
	}

	startCol { |col = 0, force = false|
		var nRows;
		nRows = this.nRows;
		nRows.do({ |row|
			this.startInRow( row, col, force );
		});
	}

	putEvent { |row = 0, column = 0, event, update = true|
		events = events ?? { Order(); };
		if( event.notNil ) {
			if( events[row].isNil ) {
				events[row] = Order();
			};
			events[row][column] = event;
		} {
			if( events[row].notNil ) {
				events[row].removeAt( column );
				if( events[row].size == 0 ) {
					events.removeAt( row );
				};
			};
			if( events.size == 0 ) { events = nil };
		};
		if( update ) { this.changed( \events ); };
	}

	putRow { |row = 0, inEvents, update = true|
		case { inEvents.isKindOf( Order ) } {
			events = events ?? { Order() };
			events[ row ] = inEvents;
		} { inEvents = nil } {
			if( events.notNil ) {
				events.removeAt( row );
				if( events.size == 0 ) { events = nil };
			};
		} { inEvents.isArray } {
			inEvents.do({ |item, i|
				this.putEvent( row, i, item, false );
			});
		} { inEvents.isKindOf( UEvent ) } {
			this.putEvent( row, 0, inEvents, false );
		};
		if( update ) { this.changed( \events ); };
	}

	selectUMaps { |selectFunc|
		^this.allEvents.collect({ |evt|
			evt.selectUMaps( selectFunc );
		}).flatten(1);
	}

	onSaveAction { }

	textArchiveFileExtension { ^"ugrid" }

	readTextArchiveAction { }

	storeArgs { ^[ events ] }
}
	