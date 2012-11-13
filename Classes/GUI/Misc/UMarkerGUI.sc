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

UMarkerGUI : UChainGUI {
	
	prMakeViews { |bounds|
		var margin = 0@0, gap = 4@4;
		var heights, units;
		var labelWidth, releaseTask;
		var controller;
		// var unitInitFunc;
		
		labelWidth = 80;
		
		if( RoundView.skin.notNil ) { labelWidth = RoundView.skin.labelWidth ? 80 };
		
		views = ();
		
		originalBounds = bounds.copy;
		
		bounds = bounds ?? { parent.asView.bounds.insetBy(4,4) };
		if( parent.asView.class.name == 'SCScrollTopView' ) {
			bounds.width = bounds.width - 12;
		};
				
		controller = SimpleController( chain );
		
		composite = CompositeView( parent, bounds ).resize_(5);
		composite.addFlowLayout( margin, gap );
		composite.onClose = { |vw|
			controller.remove; 
			if( composite == vw && { current == this } ) { current = nil } 
		};
		
		composite.decorator.shift( bounds.width - 14 - 80, 0 );
		
		views[ \singleWindow ] = SmoothButton( composite, 74@14 )
			.label_( [ "single window", "single window" ] )
			.border_( 1 )
			.hiliteColor_( Color.green )
			.value_( this.class.singleWindow.binaryValue )
			.resize_(3)
			.action_({ |bt|
				this.class.singleWindow = bt.value.booleanValue;
			});
		
		composite.decorator.nextLine;
		
		// name
		StaticText( composite, labelWidth@14 )
			.applySkin( RoundView.skin )
			.string_( "name" )
			.align_( \right );
			
		views[ \name ] = TextField( composite, 84@14 )
			.applySkin( RoundView.skin )
			.string_( chain.name )
			.action_({ |tf|
				chain.name_( tf.string );
			});
			
		composite.decorator.nextLine;
			
		// startTime
		StaticText( composite, labelWidth@14 )
			.applySkin( RoundView.skin )
			.string_( "startTime" )
			.align_( \right );
			
		views[ \startTime ] = SMPTEBox( composite, 84@14 )
			.applySmoothSkin
			.applySkin( RoundView.skin )
			.clipLo_(0)
			.value_( chain.startTime )
			.action_({ |nb|
				chain.startTime_( nb.value );
			});
		
		composite.decorator.nextLine;
		
		// action
		
		views[ \action ] = ObjectView( composite, (labelWidth + 84 + 80) @ 14, 
			chain, \action, CodeSpec({ |marker, score| }), controller
		);
		
		composite.decorator.nextLine;
		composite.decorator.top = composite.bounds.height - (PresetManagerGUI.getHeight + 8 );
		
		CompositeView( composite, (composite.bounds.width - (margin.x * 2)) @ 2 )
				.background_( Color.black.alpha_(0.25) )
				.resize_(8);
			
		presetView = PresetManagerGUI( 
			composite, 
			composite.bounds.width @ PresetManagerGUI.getHeight,
			UMarker.presetManager,
			chain
		).resize_(7);

		controller
			.put( \startTime, { views[ \startTime ].value = chain.startTime ? 0; })
			.put( \name, { { views[ \name ].value = chain.name; }.defer });		
		chain.changed( \startTime );
		chain.changed( \name );
	}
}

+ UMarker {
	gui { |parent, bounds, score| ^UMarkerGUI( parent, bounds, this, score ) }
}