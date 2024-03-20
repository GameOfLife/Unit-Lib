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

UMixer {

	 classvar <>sortToTracks = false;

     var <mainComposite, <mixerView, <scoreListView, font, <parent, <bounds;
     var <>scoreList;
     var <scoreController, <unitControllers;
     var <soloedTracks;

     *new{ |score, parent, bounds| ^super.new.init(score, parent,bounds) }

     init { |score, inParent, inBounds|

        scoreList = [score];
        soloedTracks  = [];
        parent = inParent ? Window("UMixer",Rect(100,100,800,372), scroll: true).front;
        if(parent.respondsTo(\onClose_)){ parent.onClose_({this.remove}) };
        bounds = inBounds ? Rect(0,0,800,372);

		parent.asView.minHeight = 372;
		parent.asView.maxHeight = 372;

		RoundView.pushSkin( UChainGUI.skin ?? {()} );
		font = RoundView.skin.font ?? { Font( Font.defaultSansFace, 11 ); };

        this.addCurrentScoreControllers;
        unitControllers = List.new;
        mainComposite = parent;
		SmoothButton( mainComposite, Rect( 4, 4, 80, 16 ) )
		.label_([ "sort to tracks", "sort to tracks" ])
		.value_( sortToTracks.binaryValue )
		.applySkin( RoundView.skin )
		.canFocus_( false )
		.action_({ |bt|
			sortToTracks = bt.value.booleanValue;
			this.remake;
		});
        this.makeMixerView;
		RoundView.popSkin;

     }

     addCurrentScoreControllers {

         if( scoreController.notNil ) {
	        scoreController.remove;
	    };
        scoreController = SimpleController( scoreList.last );

		scoreController.put(\numEventsChanged, {
		    this.update;
		});
	}

	remove {
        (unitControllers++[scoreController]).do(_.remove)
    }

	update {
	    this.remake;
	}

    currentScore {
        ^scoreList.last
    }

    isInnerScore {
        ^(scoreList.size > 1)
    }

    remake {

        if(scoreListView.notNil){
            scoreListView.remove;
            scoreListView = nil
        };
		RoundView.pushSkin( UChainGUI.skin ?? {()} );
        if(scoreList.size > 1) {
            this.makeScoreListView;
        };
        this.makeMixerView;
		RoundView.popSkin;
    }

    addtoScoreList{ |score|
        scoreList = scoreList.add(score);
        this.addCurrentScoreControllers;
        this.remake;
    }

    goToHigherScore{ |i|
        scoreList = scoreList[..i];
        this.addCurrentScoreControllers;
        fork{ { this.remake; }.defer }
    }

    makeScoreListView{
        var listSize = scoreList.size;
        scoreListView = CompositeView(mainComposite,Rect(88,0,4 + ((60+4)*(listSize-1)) + 20,24));
        scoreListView.addFlowLayout;
        scoreList[..(listSize-2)].do{ |score,i|
            SmoothButton(scoreListView,60@16)
                .states_([[(i+1).asString++": "++score.name, Color.black, Color.clear]])
                .font_( font )
                .border_(1).background_(Color.grey(0.8))
                .radius_(5)
                .canFocus_(false)
                .action_({
                    this.goToHigherScore(i);
                })
        };
        SmoothButton(scoreListView,16@16)
            .states_([[\up, Color.black, Color.clear]])
            .font_( font )
            .border_(1).background_(Color.grey(0.8))
            .radius_(5)
            .canFocus_(false)
            .action_({
                UMixer( this.currentScore )
            })

    }

     makeMixerView{
        var spec, maxTrack,count, cview,w,level,bounds, width,top,main,scroll, evs;
		var mixerEvents, setMuteButton;
		var score = this.currentScore;
		var events = score.events;
        var viewBounds, lastTrack;
        unitControllers.do(_.remove);
		evs = events.select({ |x| x.canFreeSynth && x.hideInGUI.not });
        maxTrack = evs.collect{ |event| event.track }.maxItem ? 0 + 1;
		count = 0;
		spec = [-90,12,\db].asSpec;

        if(mixerView.notNil) {
            mixerView.visible_(false);
            mixerView.focus(false);
            mixerView.remove;
        };

		mixerView = CompositeView(mainComposite, Rect(0,24,44*evs.size+4+(8*maxTrack),338));
        mixerView.addFlowLayout;

		if( sortToTracks ) {
			mixerEvents = [];
			maxTrack.do({ |j|
				evs.do({ |event, i|
					if( event.track == j ) {
						mixerEvents = mixerEvents.add( event );
					};
				});
			});
		} {
			mixerEvents = evs;
		};

		setMuteButton = { |event|
			if( score.softMuted.includes(event) ) { 1 } { event.muted.binaryValue * 2 };
		};

		lastTrack = 0;

		mixerEvents.do({ |event, i|
			var cview,faders, eventsFromFolder, ctl, sl, bt;
			if( sortToTracks && { event.track != lastTrack }) {
				mixerView.decorator.shift(8,0);
				lastTrack = event.track;
			};
			if(event.isFolder.not){
				cview = CompositeView(mixerView,40@330);
				cview.decorator = FlowLayout(cview.bounds);
				if( [ Color ].includes( event.getTypeColor.class ) ) {
					cview.background_( event.getTypeColor.copy.alpha_(0.5) );
				} {
					cview.background_(Color(0.58208955223881, 0.70149253731343, 0.83582089552239, 1.0));
				};
				cview.decorator.shift(0,20);
				sl = EZSmoothSlider.new(cview, Rect(0,0,32,240), events.indexOf(event), spec, layout:\vert)
				.value_(event.getGain)
				.action_({ |v|
					event.setGain(v.value);
				});
				sl.labelView.align_( \center );
				bt = SmoothButton(cview,32@20)
				.states_([
					[ "s", nil, nil ],
					[ "s", nil, Color.yellow.alpha_(0.75) ]
				] )
				.canFocus_(false)
				.value_(score.soloed.includes(event).binaryValue)
				.action_({ |v|
					score.solo(event, v.value.booleanValue)
				});
				bt = SmoothButton(cview,32@20)
				.states_([
					[ "m", nil, nil ],
					[ "m", nil, Color.red.alpha_(0.5) ],
					[ "m", nil, Color.red.alpha_(0.25) ],
				] )
				.canFocus_(false)
				.value_( setMuteButton.value( event ) )
				.action_({ |v|
					if( v.value == 2 ) { v.value = 0 };
					score.softMute(event, v.value.booleanValue)
				});
				ctl = SimpleController(event)
				.put(\gain,{ sl.value = event.getGain; })
				.put( \muted, { bt.value = setMuteButton.value( event ) });
				unitControllers.add(ctl);

			}{
				eventsFromFolder = event.allEvents.select(_.canFreeSynth)
				.collect{ |event| (\event: event,\oldLevel: event.getGain) };
				cview = CompositeView(mixerView,40@330);
				cview.decorator = FlowLayout(cview.bounds);
				if( [ Color ].includes( event.getTypeColor.class ) ) {
					cview.background_( event.getTypeColor.copy.alpha_(0.5) );
				} {
					cview.background_(Color(0.28208955223881, 0.50149253731343, 0.23582089552239, 1.0));
				};
				SmoothButton(cview,32@16).states_([["open"]])
				.radius_(3)
				.action_({
					{ this.addtoScoreList(event) }.defer(0.1)
				});
				EZSmoothSlider.new(cview, Rect(0,0,32,240), events.indexOf(event), spec, layout:\vert)
				.value_(0)
				.action_({ |v|
					eventsFromFolder.do{ |dict|
						dict[\event].setGain(dict[\oldLevel]+v.value);
					};
				})
				.labelView.align_( \center );
				bt = SmoothButton(cview,32@20)
				.states_([
					[ "s", nil, nil],
					[ "s",  nil, Color.yellow.alpha_(0.75) ]
				] )
				.canFocus_(false)
				.value_(score.soloed.includes(event).binaryValue)
				.action_({ |v|
					score.solo(event, v.value.booleanValue)
				});
				bt = SmoothButton(cview,32@20)
				.states_([
					[ "m", nil, nil ],
					[ "m", nil, Color.red.alpha_(0.5) ]
				] )
				.canFocus_(false)
				.value_(score.softMuted.includes(event).binaryValue)
				.action_({ |v|
					score.softMute(event, v.value.booleanValue)
				});
			};
		})


    }

     refresh{  }
}