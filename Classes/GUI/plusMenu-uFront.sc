+ Menu {
	uFront {
		|point, action|
		var tempAction;
		point = point ?? QtGUI.cursorPosition;
		action = action ?? { tempAction = MenuAction(); };
		this.invokeMethod(\popup, [point, action]);
		tempAction !? _.destroy;
	}

	deepDestroy {
		this.actions.do({ |act|
			if( act.menu.notNil ) {
				act.menu.deepDestroy;
			};
			act.destroy;
		});
		this.destroy;
	}
}