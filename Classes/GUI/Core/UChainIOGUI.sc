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

UChainIOGUI : UChainGUI {

	var <analyzers;
	var <unitColors;
	var <>max = 7;
	var <>allBuses;
	var <>usedBuses;
	var <>setMaxFunc;

	makeViews { |bounds|

		allBuses = [];
		usedBuses = [];
		setMaxFunc = nil;
		RoundView.useWithSkin( skin ++ (RoundView.skin ? ()), {
			this.prMakeViews( bounds );
		});
		this.updateMax;
	}

	updateMax {
		var oldMax;
		max = ((((allBuses.maxItem+1)/ 8).ceil.max(1) * 8)-1).clip(7,31);
		if( oldMax != max ) {
			setMaxFunc.value;
		};
	}

	getControlsForUnit { |unit|
		var ins, outs, mix;
		ins = unit.audioIns;
		outs = unit.audioOuts;
		mix = outs.select({ |item|
			unit.keys.includes( unit.getIOKey( \out, \audio, item, "lvl" ) )
		});
		^[ ins, outs, mix ];
	}

	getHeight { |units, margin, gap|
		^units.collect({ |unit, i|
			((14 + gap.y) * (
				this.getControlsForUnit( unit ).flatten(1).size
			)) +
				14 + gap.y + gap.y;
		}).sum + (4 * (14 + gap.y));
	}

	getUnits {
		var units = [];
		chain.units.do({ |unit|
			unit.getAllUMaps.do({ |umap|
				if ( (umap.audioIns.size > 0) or: (umap.audioOuts.size > 0) ) {
					units = units.add( umap );
				};
			});
			units = units.add( unit );
		});
		^units;
	}

	makeUnitHeader { |units, margin, gap|
		var comp, header, code, io, defs;
		var audio, control;

		comp = CompositeView( composite, (composite.bounds.width - (margin.x * 2))@16 )
			.resize_(2);

		header = StaticText( comp, comp.bounds.moveTo(0,0) )
				.applySkin( RoundView.skin )
				.string_( " units" )
				.align_( \left )
				.resize_(2);

		io = SmoothButton( comp, Rect( comp.bounds.right - 40, 1, 40, 12 ) )
                .label_( "i/o" )
                .radius_( 2 )
                .background_( RoundView.skin[ 'SmoothButton' ] !? _.hiliteColor ? Color.green )
                .action_({
	                UChainGUI(
	                	this.window.name, originalBounds,
	                	chain, replaceCurrent: true
	                );
                }).resize_(3);

         code = SmoothButton( comp,
                    Rect( comp.bounds.right - (40 + 4 + 40), 1, 40, 12 ) )
                .label_( "code" )
                .radius_( 2 )
                .action_({
	                UChainCodeGUI(
	                	this.window.name, originalBounds,
	                	chain, replaceCurrent: true
	                );
                }).resize_(3);

		defs = SmoothButton( comp,
				Rect( comp.bounds.right - (
					2 + 40 + (4 + 40 + 4 + 40)
					), 1, 42, 12
				)
			)
			.label_( "udefs" )
		    .toolTip_( "open Udefs window.\n\nYou can drag Udefs to the chain from there or open" +
			    "their corresponding code files."
		    )
			.radius_( 2 )
			.action_({
				UdefsGUI();
			}).resize_(3);

		CompositeView( comp, Rect( 0, 14, (composite.bounds.width - (margin.x * 2)), 2 ) )
			.background_( Color.black.alpha_(0.25) )
			.resize_(2)

	}

	makeUnitSubViews { |scrollView, units, margin, gap|
		var unitInitFunc;
		var labelWidth;
		var width;

		width = scrollView.bounds.width - 12 - (margin.x * 2);

		labelWidth = 80;

		if( RoundView.skin.notNil ) { labelWidth = RoundView.skin.labelWidth ? 80 };

		unitInitFunc = { |unit, what ...args|
			if( what === \init ) { // close all views and create new
				chain.changed( \units );
			};
		};

		scrollView.decorator.gap = 4@0;

		^units.collect({ |unit, i|
			var header, comp, views, params;
			var index;

			index = chain.deepIndexOf( unit );
			if( index.size > 1 ) {
				index[ 0 ] = "%: %".format(  index[0], chain.units[ index[0] ].fullDefName );
			};

			comp = CompositeView( scrollView, width@16 )
				.resize_(2);

			header = StaticText( comp, width @ 14 )
				.applySkin( RoundView.skin )
				.string_(
				 	" " ++ index.asCollection.join( "." ) ++ ": " ++
					if( unit.def.class == LocalUdef ) { "[Local] " } { "" } ++
					unit.fullDefName
				)
				.background_( if( unit.isUMap ) { unit.guiColor } {
				    RoundView.skin.headerColor ?? { Color.gray(0.9) }
			    } )
				.resize_(2)
				.font_(
					(RoundView.skin.tryPerform( \at, \font ) ??
						{ Font( Font.defaultSansFace, 12) }).boldVariant
				);

			views = this.makeUnitView( scrollView, unit, i, labelWidth, width );

			unit.addDependant( unitInitFunc );

			header.onClose_({
				unit.removeDependant( unitInitFunc );
				views[ \ctrl ].remove;
			});

			views;

		});

	}

	prScanForBus { |array, bus = 0|
		var neg;
		neg = (bus+1).neg;
		array.reverseDo({ |item|
			switch( item,
				neg, { ^false; },
				bus, { ^true; }
			);
		});
		^false;
	}

	makeUnitView { |scrollView, unit, i, labelWidth, width|
		var ctrl;
		var views;
		var controls;
		var func;

		views = ();

		controls = this.getControlsForUnit( unit );

		ctrl = SimpleController( unit );

		func = { |item, mode = \in, mix = 0|
			var nb, sl;
			var key, val;
			var spec, allBusesIndex, usedBusesIndex;
			var endPoint;
			key = unit.getIOKey( mode, \audio, item, "bus" );
			val = unit.get( key );
			allBuses = allBuses.add( val.asInteger );
			allBusesIndex = allBuses.size-1;
			endPoint = (mode === \in) && { unit.inputIsEndPoint };
			if( mode == \out ) {
				usedBuses = usedBuses.add( val.asInteger );
			} {
				if( endPoint ) {
					usedBuses = usedBuses.add( (val +1).neg.asInteger );
				};
			};
			usedBusesIndex = usedBuses.size-1;
			StaticText( scrollView, labelWidth @ 14 )
				.applySkin( RoundView.skin )
				.align_( \right )
				.background_(
					switch( mode,
						\out, { Color.red(0.75).alpha_(0.125) },
						\in, { Color.green(0.65).alpha_(0.125) },
						{ nil }
					)
				)
				.string_( "% % ".format( mode, item )  );

			sl = SmoothSlider( scrollView, (width - labelWidth - (45 + 4 + 4))@16 )
				.hiliteColor_( nil )
				.knobSize_( 0 )
				.thumbSize_( 16 )
				.step_( 1/max )
				.focusColor_( Color.clear )
				.action_( { |nb|
					unit.set( key, (nb.value * max).round(1) );				} );

			sl.background_({ |rect ...args|
				var size, bounds;
				var which;
				which = usedBuses[..usedBusesIndex];
				which = (..max.asInteger).select({ |item|
					this.prScanForBus( which, item );
				});
				size = (1 / sl.step);
				bounds = sl.sliderBounds;
				(size.asInteger ..0).do({ |i, index|
					var y, alpha;
					if( (sl.value * max).round(1) != index ) {
						if( which.includes( index.asInteger ) ) {
							alpha = 0.75;
						} {
							alpha = 0.1;
						};
					} {
						alpha = mix.value * 0.75;
					};
					if( alpha > 0 ) {
						Pen.width = 5;
						Pen.color = Color.gray(0.4).alpha_(alpha);
						y = bounds.top + ((i / size) * bounds.height);
						Pen.line( bounds.left @ y, bounds.right @ y );
						Pen.stroke;
					};
				});
			});

			sl.knobColor_(
				switch( mode,
					\in, { { |rect|
						var center, used, alpha;
						if( endPoint ) {
							used = this.prScanForBus( usedBuses[..usedBusesIndex-1], allBuses[ allBusesIndex ] );
						} {
							used = this.prScanForBus( usedBuses[..usedBusesIndex], allBuses[ allBusesIndex ] );
						};
						if( used ) { alpha = 0.75 } { alpha = 0.1 };
						Pen.width = 5;
						center = rect.center;
						Pen.color	 = Color.gray(0.4).alpha_( alpha );
						if( unit.def.inputIsEndPoint ) {
							Pen.line( rect.left @ center.y, center.x @ center.y ).stroke;
						} {
							Pen.line( rect.left @ center.y, rect.right @ center.y ).stroke;
						};

						if( used ) {
							Pen.color = Color.green(0.65);
						} {
							Pen.color = Color.gray(0.65);
						};
						Pen.fillOval( rect.insetAll(3,3,3,3) )
					} },
					\out, { { |rect|
						var center;

						Pen.width = 5;
						Pen.color = Color.gray(0.4).alpha_(0.75);
						center = rect.center;
						Pen.line( rect.right @ center.y, center ).stroke;

						Pen.color = Color.red(0.75);
						Pen.fillOval( rect.insetAll(3,3,3,3) );
					} }
				)
			);

			nb = SmoothNumberBox( scrollView, 45@14 )
				.clipLo_( 0 )
				.clipHi_( 31 )
				.step_( 1 )
				.scroll_step_( 1 )
				.value_( val )
				.action_( { |nb|
					unit.set( key, nb.value.round(1) );
				} );

			setMaxFunc = setMaxFunc.addFunc({
				sl.step = 1/max;
				sl.value = allBuses[ allBusesIndex ]/max;
			});

			ctrl.put( key, {
				var val;
				val = unit.get( key );
				nb.value = val;
				sl.value = val/max;
				allBuses.put( allBusesIndex, val.asInteger );
				case { mode == \out } {
					usedBuses.put( usedBusesIndex, val.asInteger );
				} { endPoint } {
					usedBuses.put( usedBusesIndex, (val+1).neg.asInteger );
				};
				this.updateMax;
			});

			views[ mode ] = views[ mode ].add( [ nb, sl ] );

			[ nb, sl ];
		};

		controls[0].do({ |item|
			func.( item, \in );
		});

		controls[1].do({ |item| // out
			var key, val, mx;

			if( controls[2].includes( item ) ) {
				key = unit.getIOKey( \out, \audio, item, "lvl" );
				val = unit.get( key );

				mx = EZSmoothSlider(  scrollView, width@14,
					"mix % ".format( item ),
					\amp.asSpec,
					{ |vw|
						unit.set( key, vw.value );
					},
					labelWidth: labelWidth
				);
				mx.labelWidth_( labelWidth );

				mx.value = val;
				mx.view.resize = 2;
				mx.sliderView.focusColor = Color.gray(0.2).alpha_(0.2);

				ctrl.put( key, {
					mx.value = unit.get( key );
				});

				views[ \mix ] = views[ \mix ].add( mx );
			};

			func.( item, \out, mx ? 0 );
		});

		if( views.size == 0 ) {
			ctrl.remove;
		} {
			views[ \ctrl ] = ctrl;
		};
		^views;
	}

}