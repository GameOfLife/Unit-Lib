/*
    Unit Library
    The Game Of Life Foundation. http://gameoflife.nl
    Copyright 2006-2011 Miguel Negrao, Wouter Snoei.

    GameOfLife Unit Library: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    GameOfLife Unit Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with GameOfLife Unit Library.  If not, see <http://www.gnu.org/licenses/>.
*/

USessionGUI : UAbstractWindow {

	classvar <>topBarHeigth = 30;

	var <session;
    var <sessionView, bounds;
    var <sessionController, objGuis;
    var <selectedObject;
	var compTop = 0;

    *new { |session, bounds|
        ^super.new.init( session)
			.addToAll
			.makeGui(bounds)
	}

	init { |inSession|
	    session = inSession;
	    sessionController = SimpleController(session);
	    sessionController.put(\objectsChanged,{
	        { this.makeSessionView }.defer(0.05);
	    });
	    sessionController.put(\name,{
            window.name = this.windowTitle
        });

	}

    windowTitle {
        ^("Session Editor : "++this.session.name)
    }

    remove {
        sessionController.remove;
        objGuis.do(_.remove)
    }

	makeGui { |bounds|
        var topBarView;
		var font = Font( Font.defaultSansFace, 11 );
        var size = 16;
        var margin = 4;
        var gap = 2;
		var menuView, menuH = 22;
		var plusButtonTask;
        bounds = bounds ? Rect(100,100,700,400);
        this.newWindow(bounds, "USession - "++session.name, {

            if(USessionGUI.current == this) {
                USessionGUI.current = nil
            };
            this.remove;
            {
                if( (this.session.objects.size != 0) && (this.session.isDirty) ) {
                    SCAlert( "Do you want to save your session? (" ++ this.session.name ++ ")" ,
                        [ [ "Don't save" ], [ "Cancel" ], [ "Save" ],[ "Save as"] ],
                        [ 	nil,
                            { USessionGUI(session) },
                            { this.session.save(nil, {USessionGUI(session)} ) },
                            { this.session.saveAs(nil,nil, {USessionGUI(session)} ) }
                        ] );
                };
            }.defer(0.1)
        }, UChainGUI.skin.scoreEditorWindow, margin:0, gap:0);

		RoundView.pushSkin( UChainGUI.skin );

		if( UMenuBarIDE.hasMenus ) {
			menuView = UMenuBarIDE.createMenuStrip( view, bounds.width @ menuH, [0,0,0,0] );
			compTop = menuH;
		};

        topBarView =  CompositeView(view, Rect(0,compTop,bounds.width,topBarHeigth)).resize_(2);
        topBarView.addFlowLayout;

        SmoothButton( topBarView, 40@size  )
			.states_( [
			    [ \play, nil, Color.clear ]] )
			.canFocus_(false)
			.font_( font )
			.action_({
			    session.prepareAndStart
			});

		SmoothButton( topBarView, 40@size  )
			.states_( [
			    [ \stop, nil, Color.clear ]] )
			.canFocus_(false)
			.font_( font )
			.action_({
			    session.release
			});

		topBarView.decorator.shift(10);

        SmoothButton( topBarView, size@size )
            .states_( [[ '+' ]] )
            .canFocus_(false)
		    .toolTip_( "Add Event" )
		.mouseDownAction_({
			plusButtonTask.stop;
			plusButtonTask = {
				0.5.wait;
				UMenuBarIDE.allMenus[ 'File' ].actions.detect({ |act|
					act.string == "Add to Session"
				}).menu.front;
				plusButtonTask = nil;
			}.fork( AppClock );
		})
		.action_({
			plusButtonTask !? _.stop;
			plusButtonTask = nil;
			session.add( UScore().name_(
				"Untitled %".format( session.objects.size + 1 )
			) )
		});

		topBarView.decorator.nextLine;

		CompositeView( topBarView, Rect( 0, 14, (topBarView.bounds.width - (margin * 2)), 2 ) )
        	.background_( Color.black.alpha_(0.25) )
        	.resize_(2);

		this.makeSessionView;

		RoundView.popSkin;

    }

    removeObject { |object|
        if(object.class != UScore){
            session.remove(object);
        } {
            if( (object.events.size != 0) && (object.isDirty) ) {
                SCAlert( "Do you want to save your score? (" ++ object.name ++ ")" ,
                [ [ "Don't save" ], [ "Cancel" ], [ "Save" ],[ "Save as"] ],
                [ 	{session.remove(object)},
                    nil,
                    { object.save({session.remove(object)}) },
                    { object.saveAs(nil,{session.remove(object)}) }
                ] );

            } {
				session.remove(object);
			}
        }
    }

    makeSessionView {
        var margin = 4;
        var gap = 2;
        var sessionViewsHeight = 16;
        var font = Font( Font.defaultSansFace, 11 );
        var bounds = view.bounds.moveTo(0,0);
		var boxColor, alpha;

        //first remove old view and controllers;
        if(sessionView.notNil) {
            sessionView.remove;
        };
        objGuis.do(_.remove);

		RoundView.pushSkin( UChainGUI.skin );

		boxColor =  Color.gray(0.6,0.0);

		//alpha = boxColor.alpha;

		//boxColor = UChainGUI.skin.scoreEditorWindow.blend( boxColor.alpha_(1), alpha );

		sessionView = CompositeView(view, Rect(0,topBarHeigth + compTop,bounds.width,bounds.height - topBarHeigth)).resize_(5);
        sessionView.addFlowLayout(margin@margin,margin@margin);

        objGuis = session.objects.collect { |object,i|
            var releaseTask, but, ctl, comp, gui;
			var titleMenu;

            comp = CompositeView( sessionView, (sessionView.bounds.width - (margin*2))@(sessionViewsHeight + (margin*2)) )
            		.resize_(2)
			.background_( boxColor.blend( object.getTypeColor, 0.5 ) );
            comp.addFlowLayout;

            titleMenu = UPopUpMenu(comp,200@16)
            .string_( " "++object.name)
			.extraMenuActions = {
				[
					MenuAction( "Open...", {
						var gui;
						gui = object.gui;
						if( gui.isKindOf( UScoreEditorGUI ) ) { gui.askForSave = false };
					}),

					if( object.isKindOf( UScore ) ) {
						MenuAction( "Edit...", {
							MassEditUChain(
								object.getAllUChains,
								object.getAllUMarkers
							).gui( score: object );
						})
					},

					MenuAction.separator,

					MenuAction( "Move to top", {
						session.insert( 0, session.objects.remove( object ) );
					}).enabled_( i > 0 ),
					MenuAction( "Move up", {
						session.insert( i-1, session.objects.removeAt( i ) );
					}).enabled_( i > 0 ),
					MenuAction( "Move down", {
						session.insert( i+1, session.objects.removeAt( i ) );
					}).enabled_( i < (session.objects.size-1) ),
					MenuAction( "Move to bottom", {
						session.add( session.objects.remove( object ) );
					}).enabled_( i < (session.objects.size-1) ),

					MenuAction.separator,

					MenuAction( "Duplicate", {
						var copy;
						copy = object.deepCopy;
						if( copy.isKindOf( UScore ) ) {
							copy.name = copy.name ++ "(copy)";
						};
						session.insert( i+1, copy );
					})
			].select(_.notNil) };

            SmoothButton(comp,25@16)
                .states_([[\up,nil,Color.clear]])
                .font_( font )
			    .radius_(5)
			    .canFocus_(false)
			    .action_({
				    var gui;
				    gui = object.gui;
				    if( gui.isKindOf( UScoreEditorGUI ) ) { gui.askForSave = false };
			    });

			comp.decorator.shift(22,0);
			gui = object.sessionGUI(comp);

			comp.decorator.left_( comp.bounds.width - 20 );

			SmoothButton( comp, 16@16 )
            .states_( [[ '-' ]] )
			.resize_( 3 )
            .canFocus_(false)
            .action_({
                this.removeObject( object );
            });
			sessionView.decorator.nextLine;

			ctl = SimpleController( object )
			.put( \name, {
				{
					titleMenu.string_(" "++object.name)
				}.defer;
			})
			.put( \displayColor, {
				{
					comp.background_( boxColor.blend( object.getTypeColor, 0.5 ) );
				}.defer;
			});

			comp.onClose_({ ctl.remove });

			gui
        };

        window.refresh;

		RoundView.popSkin;
    }

}

UChainSessionView {
    var object;
    var ctl;

    *new { |object,view|
        ^super.newCopyArgs(object).init(view)
    }

    remove {
        ctl.remove;
    }

    init { |view|
        var button;
        var font = Font( Font.defaultSansFace, 11 );
        button = SmoothButton( view, 40@16  )
            .label_( ['power','power'] )
            .hiliteColor_( Color.green.alpha_(0.5) )
            .canFocus_(false)
            .font_( font )
            .action_( [ {
                object.prepareAndStart;
            }, {
                object.release
            } ]
            );

        if( object.groups.size > 0 ) {
            button.value = 1;
        };

        ctl = SimpleController(object)
            .put( \start, { button.value = 1 } )
            .put( \end, {
                if( object.units.every({ |unit| unit.synths.size == 0 }) ) {
                    button.value = 0;
                };
            } )
    }

}

/*DragCompositeView : SCCompositeView {
    var <object;

    *new{ |parent,bounds,object|
        ^super.new(parent,bounds).initDrag(object)
    }

    initDrag { |inObject|
        object = inObject
    }

    defaultGetDrag {
        ^object
    }

}*/

UChainGroupSessionView {
    var object;

    *new { |object,view|
        ^super.newCopyArgs(object).init(view)
    }

    init { |view|
        var button;
        var font = Font( Font.defaultSansFace, 11 );
        button = SmoothButton( view, 40@16  )
            .label_( ['power','power'] )
            .hiliteColor_( Color.green.alpha_(0.5) )
            .canFocus_(false)
            .font_( font )
            .action_( [ {
                object.prepareAndStart;
            }, {
                object.release
            } ]
            );

        if( object.groups.size > 0 ) {
            button.value = 1;
        };
    }

    remove{ }

}

+ UScore {
    sessionGUI { |view|
        ^UTransportView(this, view, 16)
    }
}

+ UChain {
    sessionGUI { |view|
        ^UChainSessionView(this,view)
    }
}

+ UChainGroup {
    sessionGUI { |view|
        ^UChainGroupSessionView(this,view)
    }
}

+ UScoreList {
    sessionGUI { |view|
        ^UTransportView(this.metaScore, view, 16);
    }
}