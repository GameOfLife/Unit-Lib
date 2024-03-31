UPopUpMenu : StaticText {
	var <items, <index = 0, <>extraMenuActions, <>title;

	*new { arg parent, bounds;
		var obj = super.new( parent, bounds );
		obj.setProperty(\wordWrap, false);
		obj.applySkin( RoundView.skin );
		obj.background_( Color.white.alpha_( 0.25 ) );
		obj.mouseDownAction_({ obj.makeMenu; });
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
		this.update;
	}

	item { ^items !? _[ index ] }

	makeMenu {
		var actions, selected, menu;

		actions = items.collect({ |item, i|
			if( item == '' or: { item == "" } ) {
				MenuAction.separator;
			} {
				MenuAction( item.asString, {
					this.value_( i, false );
					this.doAction;
					this.update;
					menu.destroy;
				}).enabled_( index != i );
			};
		}) ? [];

		selected = actions[ index ];

		actions = actions.addAll( extraMenuActions.value( this ) );

		if( actions.size > 0 ) {
			if( title.notNil ) {
				actions = [ MenuAction.separator( title.asString ) ] ++ actions;
			};
			menu = Menu( *actions );
			^menu.front( QtGUI.cursorPosition - (20@0), action: selected );
		} {
			^nil;
		}
	}

	update {
		{
			this.string = " % ".format( items !? _[ index ] ? "" );
		}.defer;
	}

	doAction { action.value( this ) }

}