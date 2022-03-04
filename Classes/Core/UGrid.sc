UGrid : UEvent {

	var <>events;
	var <>selected;
	classvar <>nCols = 8; // only used in case of creation with array

	*new { |events|
		^super.new.init( events );
	}

	init { |inEvents|
		case { inEvents.isArray } {
			events = Order();
			inEvents.do({ |evt, i|
				this.put( (i/nCols).asInteger, i.asInteger % nCols );
			});
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

	release { |row, col|
		this.eventsDo( _.release, row, col );
	}

	startInRow { |row = 0, col = 0, force = false| // start event in row, stop the rest
		var evtToStart;
		evtToStart = this[row,col];
		if( force or: (evtToStart.isNil) ) {
			this.release( row );
			evtToStart !? _.prepareAndStart;
		} {
			this.eventsDo({ |evt|
				if( evt == evtToStart ) {
					if( evt.isPlaying.not ) { evt.prepareAndStart };
				} {
					evt.release;
				};
			}, row)
		};
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

	readTextArchiveAction { }

	storeArgs { ^[ events ] }
}
	