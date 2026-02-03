UPopUpMenu {
	var <>view, <action;
	var <items, <index = 0, <extraMenuActions, <>title;
	var <>menu, <>menuActions, <>indexOffset = 0;
	var <>valueChangesString = true;

	*new { arg parent, bounds;
		^super.new.init( parent, bounds );
	}

	init { |parent, bounds|
		view = StaticText.new( parent, bounds );
		view.setProperty( \wordWrap, false);
		view.background_( Color.white.alpha_( 0.25 ) );
		view.applySkin( RoundView.skin );
		view.mouseDownAction_({ this.openMenu; });
		view.onClose_({ this.destroyMenu });
	}

	value { ^index }

	value_ { |newVal = 0, update = true|
		index = newVal;
		if( update ) { this.update; };
	}

	valueAction_ { arg val;
		this.value_(val, false);
		this.doAction;
		this.update;
	}

	clear {
		items = nil;
		this.update;
	}

	action_ { |inAction| action = inAction }

	items_ { |newItems|
		items = newItems;
		if( menu.notNil ) { this.destroyMenu; };
		this.update;
	}

	item { ^items !? _[ index ] }

	item_ { |inItem|
		var id;
		id = items.indexOfEqual( inItem );
		if( id.isNil ) {
			this.items = items ++ [ inItem ];
			id = items.indexOfEqual( inItem );
		};
		this.value = id;
	}

	extraMenuActions_ { |function|
		extraMenuActions = function;
		this.destroyMenu;
	}

	makeMenu {
		this.destroyMenu;

		menuActions = items.collect({ |item, i|
			case { item == '' or: { item == "" } } {
				MenuAction.separator;
			} { item.asString[..1] == "--" } {
				MenuAction.separator( item.asString[2..] );
			} {
				MenuAction( item.asString, {
					this.value_( i, false );
					this.doAction;
					this.update;
				});
			};
		}) ? [];

		menuActions = menuActions.addAll( extraMenuActions.value( this ) );

		if( menuActions.size > 0 ) {
			if( title.notNil ) {
				menuActions = [ MenuAction.separator( title.asString ) ] ++ menuActions;
				indexOffset = 1;
			};
			menu = Menu( *menuActions );
		} {
			menu = nil;
		}
	}

	openMenu {
		var selected;
		if( menu.isNil ) { this.makeMenu };
		if( menu.notNil ) {
			if( this.items.size > 0 ) {
				menuActions.do({ |item, i|
					if( i == (index + indexOffset) ) {
						item.enabled = false;
						selected = item;
					} {
						item.enabled = true;
					};
				});
			};
			^menu.uFront( QtGUI.cursorPosition - (20@0), action: selected );
		};
	}

	destroyMenu {
		if( menu.notNil ) {
			menu.deepDestroy;
			menuActions = nil;
			menu = nil;
		};
	}

	update {
		if( valueChangesString ) {
			{
				view.string = " % ".format( items !? { |item| item[ index ].asString } ? "" );
			}.defer;
		};
	}

	doAction { action.value( this ) }

	doesNotUnderstand { arg ... args;
		var result = view.perform( *args );
		^if( result === view, { this }, { result }); // be sure to replace view with base
	}

}