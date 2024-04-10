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

+ AbstractMenuAction {

	deepDestroy {
		this.menu !? _.deepDestroy;
		this.destroy;
	}

}

+ MainMenu {

	*registerNoUpdate {
		|action, menu, group=\none|
		var menuList, existingIndex;

		menu = menu.asSymbol;
		group = group.asSymbol;

		menuList = this.prGetMenuGroup(menu, group);
		existingIndex = menuList.detectIndex({ |existing| existing.string == action.string });
		if (existingIndex.notNil) {
			"Menu item '%' replaced an existing menu".format(action.string).warn;
			menuList[existingIndex] = action;
		} {
			menuList.add(action);
		};

		//this.prUpdate();
	}
}