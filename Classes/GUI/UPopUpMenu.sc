UPopUpMenu : StaticText {
	var <items, <index = 0, <>extraMenuActions;

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
		var actions, selected;

		actions = items.collect({ |item, i|
			if( item == '' or: { item == "" } ) {
				MenuAction.separator;
			} {
				MenuAction( item.asString, {
					this.value_( i, false );
					this.doAction;
					this.update;
				}).enabled_( index != i );
			};
		}) ? [];

		selected = actions[ index ];

		actions = actions.addAll( extraMenuActions );

		if( actions.size > 0 ) {
			^Menu( *actions ).front( QtGUI.cursorPosition - (20@0), action: selected );
		} {
			^nil;
		}
	}

	update {
		{
			this.string = " %".format( items !? _[ index ] ? "" );
		}.defer;
	}

	doAction { action.value( this ) }

}