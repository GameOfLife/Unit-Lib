UPopUpMenu : StaticText {
	var <items, <index = 0, <>extraMenuActions, <>title;
	var <>menu, <>menuActions, <>indexOffset = 0;

	*new { arg parent, bounds;
		var obj = super.new( parent, bounds );
		obj.setProperty(\wordWrap, false);
		obj.applySkin( RoundView.skin );
		obj.background_( Color.white.alpha_( 0.25 ) );
		obj.mouseDownAction_({ obj.openMenu; });
		obj.onClose_({ obj.destroyMenu });
		^obj;
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
		if( menu.notNil ) { this.makeMenu; };
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

	makeMenu {
		this.destroyMenu;

		menuActions = items.collect({ |item, i|
			if( item == '' or: { item == "" } ) {
				MenuAction.separator;
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
			menuActions.do({ |item, i|
				if( i == (index + indexOffset) ) {
					item.enabled = false;
					selected = item;
				} {
					item.enabled = true;
				};
			});
			^menu.uFront( QtGUI.cursorPosition - (20@0), action: selected );
		};
	}

	destroyMenu {
		if( menu.notNil ) {
			menu.deepDestroy;
			menuActions = nil;
		};
	}

	update {
		{
			this.string = " % ".format( items !? _[ index ] ? "" );
		}.defer;
	}

	doAction { action.value( this ) }

}