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


UMenuWindow {
	
	classvar <>window, <font;
	classvar <>sessionMenu, <>scoreMenu, <>viewMenu;
	classvar <>sessionDict, <>scoreDict, <>viewDict;
	
	*initClass {
		sessionDict = MultiLevelIdentityDictionary();
		scoreDict = MultiLevelIdentityDictionary();
		viewDict = MultiLevelIdentityDictionary();
		
	}
	
	*new { 
		if( window.notNil && { window.isClosed.not } ) {
			window.front;
		} {
			this.makeWindow;
		};
	}
	
	*makeWindow {
		
		if( font.class != Font.implClass ) { font = nil };
		
		font = font ?? { Font( Font.defaultSansFace, 12 ); };
		
		window = Window( "UMenuWindow", Rect(0, Window.screenBounds.height - 30, 400, 30) ).front;
		window.addFlowLayout;
		
		sessionMenu = PopUpTreeMenu(window, 100@20 )
			.font_( font )
			.tree_(
				(
					'  Session': (),
					' New': (),
					' Open...': (),
					' Save': (),
					' Save as...': (),
					'Add': (
						'New': ( 
							'UChain': (),
							'UChainGroup': (),
							'UScore': (),
							'UScoreList': ()
						),
						'Current score': (),
						'Current score duplicated': (),
						'Selected events': (
							'all': (),
							'flattened': (),
							'into a UChainGroup': (),
							'into a UScore': ()
						)
					)
				)
			)
			.value_( [ '  Session' ] )
			.action_({ |vw, value|
				vw.value = [ '  Session' ];
				sessionDict.at( *value ).value;
			});	
		
		scoreMenu = PopUpTreeMenu(window, 100@20 )
			.font_( font )
			.tree_(
				(
					'  Scores': (),
					' New': (),
					' Open...': (),
					' Save': (),
					' Save as...': (),
					'Add': (
						'New': ( 
							'UChain': (),
							'UChainGroup': (),
							'UScore': (),
							'UScoreList': ()
						),
						'Current score': (),
						'Current score duplicated': (),
						'Selected events': (
							'all': (),
							'flattened': (),
							'into a UChainGroup': (),
							'into a UScore': ()
						)
					)
				)
			)
			.value_( [ '  Scores' ] )
			.action_({ |vw, value|
				vw.value = [ '  Scores' ];
				scoreDict.at( *value ).value;
			});	
		
		viewMenu = PopUpTreeMenu(window, 100@20 )
			.font_( font )
			.tree_(
				(
					' View': (),
					'EQ': (),
					'Level': (),
					'Udefs': (),
					'Level meters': ()
				)
			)
			.value_( [ ' View' ] )
			.action_({ |vw, value|
				vw.value = [ ' View' ];
				scoreDict.at( *value ).value;
			});	
	}
	
}