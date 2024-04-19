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

+ Spec {

	adaptFromObject { ^this }

	viewNumLines { ^1 }

	makeEditWindow { |inView, values, label, action, spec|
		var evws = (), canBeControlSpec = false, hasOriginalSpec = false, scrollHeight;
		var specViewHeight, originalViewHeight;

		canBeControlSpec = this.respondsTo( \asControlSpec );
		hasOriginalSpec = this.respondsTo( \originalSpec );

		if( spec.notNil ) {
			evws[ \spec ] = spec;
		} {
			case { hasOriginalSpec && { this.originalSpec.notNil  } } {
				evws[ \spec ] = this.originalSpec;
			} { canBeControlSpec } {
				evws[ \spec ] = this.asControlSpec;
			} {
				"%:makeEditWindow : can't make window, no originalSpec specified\n".postf( this.class );
				^nil;
			};
		};

		specViewHeight = this.viewNumLines * 18;
		originalViewHeight = evws[ \spec ].viewNumLines * 18;

		RoundView.pushSkin( UChainGUI.skin );

		inView[ \editWin ] !? _.close;

		evws[ \close ] = { |evt| if( evt.w.isClosed.not ) { evt.w.close } };

		evws[ \values ] = values;
		evws[ \key ] = label;

		evws[ \w ] = Window("Edit: % (% items)".format( evws[ \key ], evws[ \values ].size ) ).front;
		evws[ \w ].addFlowLayout;

		inView[ \editWin ] = evws;

		evws[ \w ].onClose_({ inView[ \editWin ] = nil });

		evws[ \setValues ] = { |evt, values|
			evt[ \values ] = values;
			evt[ \updateViews ].value;
		};

		if( canBeControlSpec && {
			evws[ \spec ].unmap( evws[ \spec ].default ).isNumber;
		}) {
			evws[ \controlSpec ] = this.asControlSpec;
			scrollHeight = evws[ \w ].bounds.height - 200 - 6 - specViewHeight - 12;
			evws[ \multi ] = MultiSliderView( evws[ \w ], evws[ \w ].bounds.width - 8 @ 200 );
			evws[ \multi ].resize_(2);
			evws[ \multi ].elasticMode_(1);
			evws[ \multi ].isFilled_( true );
			evws[ \multi ].indexIsHorizontal = true;
			evws[ \multi ].showIndex = false;
			evws[ \multi ].action = { |sl|
				if( sl.readOnly.not ) {
					evws[ \values ] = sl.value.collect({ |x| evws[ \spec ].map( x ); });
					action.value( evws[ \values ] );
				};
			};
			evws[ \multi ].readOnly_( false );
		} {
			scrollHeight = evws[ \w ].bounds.height - 2 - specViewHeight - 12;
		};

		evws[ \updateViews ] = {
			{
				evws[ \multi ] !? _.value_( evws[ \values ].collect({ |x| evws[ \spec ].unmap( x ) }) );
				evws[ \multi ] !? _.reference_( evws[ \spec ].unmap( evws[ \spec ].default ) ! evws[ \values ].size );
			}.defer;
			evws[ \views ].do({ |vw, i|
				evws[ \spec ].setView( vw, evws[ \values ][ i ] )
			});
			this.setView( evws[ \massView ], evws[ \values ] );
		};

		evws[ \massView ] = this.makeView( evws[ \w ], (evws[ \w ].bounds.width - 10 ) @ (specViewHeight - 4), evws[ \key ], { |vws, val|
			evws[ \values ] = val;
			action.value( evws[ \values ] );
		}, 2, hasEdit: false);

		CompositeView(  evws[ \w ], (evws[ \w ].bounds.width - 8 ) @ 2 )
			.background_( Color.black.alpha_(0.25) )
			.resize_(2);

		evws[ \scroll ] = ScrollView( evws[ \w ], (evws[ \w ].bounds.width - 10) @ scrollHeight );
		evws[ \scroll ].resize_(5);
		evws[ \scroll ].addFlowLayout( 4@0, 4@4 );
		evws[ \scroll ].hasBorder_( false );

		evws[ \w ].view.minWidth_( 400 ).maxWidth_( 400 );

		RoundView.pushSkin( UChainGUI.skin ++ ( labelWidth: UChainGUI.skin.labelWidth - 4 ) );

		evws[ \views ] = evws[ \values ].size.collect({ |i|
			evws[ \spec ].makeView( evws[ \scroll ], (evws[ \w ].bounds.width - 58) @ (originalViewHeight - 4), "% [%]".format(evws[ \key ], i), { |vws, val|
				evws[ \values ][ i ] = val;
				action.value( evws[ \values ] );
			}, 2);
		});

		RoundView.popSkin;

		RoundView.popSkin;

		evws[ \updateViews ].value;

		^evws;
	}

	makeOperations { |currentVals, action|
		var operations;

		operations = OEM(
			\invert, { |values|
				values = this.unmap( values );
				values = values.linlin(
					values.minItem, values.maxItem, values.maxItem, values.minItem
				);
				this.map( values );
			},
			\reverse, { |values|
				values.reverse;
			},
			\sort, { |values|
				values.sort;
			},
			\scramble, { |values|
				values.scramble;
			},
			\random, { |values|
				var min, max;
				if( values.first.isKindOf( Number ) ) {
					#min, max = this.unmap( [values.minItem, values.maxItem] );
				} {
					#min, max = [0,1];
				};
				values = values.collect({ 0.0 rrand: 1 }).normalize(min, max);
				this.map( values );
			},
			\line, { |values|
				var min, max;
				if( values.first.isKindOf( Number ) ) {
					#min, max = this.unmap( [values.minItem, values.maxItem] );
				} {
					#min, max = [0,1];
				};
				values = (0..values.size-1).linlin(0,values.size-1, min, max );
				this.map( values );
			},
			'use first for all', { |values|
				Array.fill( values.size, { values.first.deepCopy });
			}
		);

		operations[ \rotate ] = (
			settings: [0],
			labels: ["rotate"],
			specs: { [ [ currentVals.size.neg, currentVals.size,\lin,1,0].asSpec ] },
			calculate: { |evt, values|
				values.rotate( evt[ \settings ][ 0 ].asInteger )
			},
		);

		operations[ 'move item' ] = (
			settings: [0,0,1],
			labels: ["from", "to", "range"],
			specs: { [
				[ 0, currentVals.size - 1, \lin, 1, 0 ].asSpec,
				[ 0, currentVals.size - 1, \lin, 1, 0 ].asSpec,
				[ 1, currentVals.size - 1, \lin, 1, 1 ].asSpec,
			] },
			calculate: { |evt, values|
				var take, from, to, range;
				#from, to, range = evt[ \settings ].collect(_.asInteger);
				if( from != to ) {
					values = values.copy;
					if( range > 1 ) {
						from = from.min( values.size - range );
						to = to.min( values.size - range );
						take = range.collect({ |i|
							values.removeAt( from );
						}).reverse;
						take.do({ |item|
							values = values.insert( to, item );
						});
					} {
						values.move( from, to )
					};
				};
				values;
			},
		);

		operations[ \resample ] = (
			settings: [1,0,'linear'],
			labels: ["ratio", "offset", "type"],
			specs: { [
				[0.125,8,\exp,0,1].asSpec,
				[-1,1,\lin,0,0].asSpec,
				ListSpec([ 'step', 'linear', 'spline', 'hermite', 'sine'], 1)
			] },
			calculate: { |evt, values|
				var ratio, offset, type;
				#ratio, offset, type = evt.settings;
				offset = offset * values.size;
				values.size.collect({ |i|
					values.intAt( i * ratio + offset, type );
				});
			},
		);

		operations[ \curve ] = (
			settings: [0, 0],
			labels: ["curve", "s-curve"],
			specs: { [ [-16,16,\lin,0,0].asSpec, [-16,16,\lin,0,0].asSpec ] },
			calculate: { |evt, values|
				var min, max, half, curve, scurve;
				min = values.minItem;
				max = values.maxItem;
				#curve, scurve = evt.settings;
				values = values.lincurve( min,max,min,max,curve );
				if( scurve != 0 ) {
					half = [ min, max ].mean;
					values.collect({ |val|
						if( val >= half ) {
							val.lincurve( half, max, half, max, scurve );
						} {
							val.lincurve( min, half, min, half, scurve.neg );
						};
					});
				} {
					values
				};
			},
		);

		operations[ \quantize ] = (
			settings: [0,0,0],
			labels: ["absolute", "relative", "division"],
			specs: { [
				[0, this.maxval, this.map(0.5).first.calcCurve(0, this.maxval),0,0].asSpec,
				[0, 1].asSpec,
				[0,32,\lin,1,0].asSpec
			] },
			calculate: { |evt, values|
				var out = values;
				if( evt.settings[0] != 0 ) {
					out = this.unmap( this.map( values ).round( evt.settings[0] ) );
				};
				if( evt.settings[1] != 0 ) {
					out = values.linlin( values.minItem, values.maxItem, 0,1 ).round( evt.settings[1] )
					.linlin( 0, 1, values.minItem, values.maxItem )
				};
				if( evt.settings[2] != 0 ) {
					out = values.linlin( values.minItem, values.maxItem, 0,1 ).round( 1/evt.settings[2] )
					.linlin( 0, 1, values.minItem, values.maxItem )
				};
				out;
			},
		);

		operations[ \smooth ] = (
			settings: [0,0.3],
			labels: ["smooth", "window"],
			specs: { [ [-1,1,\lin,0,0].asSpec, [0,1,\lin,0,0.3].asSpec ] },
			calculate: { |evt, values|
				var n, win, smoothed;
				n = (evt.settings[1] * values.size).max(3);
				win = ({ |i|
					i.linlin(0,(n-1).max(2),-0.5pi,1.5pi).sin.linlin(-1,1,0,1)
				}!n.max(2)).normalizeSum;
				smoothed = values.collect({ |item, i|
					(values.clipAt( (i + (n/ -2).ceil .. i + (n/2).ceil - 1) ) * win).sum;
				});
				values.blend( smoothed, evt.settings[0] );
			},
		);

		operations[ \flat ] = (
			settings: [0, 0],
			labels: ["blend", "reference"],
			specs: { [ [-1,1,\lin,0,0].asSpec, IntegerSpec(0, 0 , currentVals.size - 1) ] },
			calculate: { |evt, values|
				values.blend( values[ evt.settings[1].asInteger ], evt.settings[0] );
			},
		);

		operations[ \sine ] = (
			settings: [0,1,0],
			labels: ["blend", "periods", "phase"],
			specs: { [ [0,1].asSpec, [0.25,16,\exp,0,1].asSpec, AngleSpec() ] },
			calculate: { |evt, values|
				var min, max, size, blend, periods, phase;
				min = values.minItem;
				max = values.maxItem;
				size = values.size;
				#blend, periods, phase = evt.settings;
				values.collect({ |item,i|
					item.blend(
						i.linlin(0, size-1, phase, periods * 2pi + phase ).sin.linlin(-1,1,min,max),
						blend
					)
				});
			},
		);

		operations[ \square ] = (
			settings: [0,1,0.5,0],
			labels: ["blend", "periods", "width", "phase"],
			specs: { [ [0,1].asSpec, [1, (currentVals.size) / 2,\exp,0,1].asSpec, [0,1,\lin,0,0.5].asSpec, AngleSpec() ] },
			calculate: { |evt, values|
				var min, max, size, blend, periods, width, phase;
				min = values.minItem;
				max = values.maxItem;
				size = values.size;
				#blend, periods, width, phase = evt.settings;
				phase = phase / pi;
				values.collect({ |item,i|
					item.blend(
						(i.linlin(0, size, phase, periods + phase ).wrap(0,1) < width).binaryValue.linlin(0,1,min,max),
						blend
					)
				});
			},
		);

		operations[ \triangle ] = (
			settings: [0,1, 0.5,0],
			labels: ["blend", "periods", "width", "phase"],
			specs: { [
				[0,1].asSpec,
				[0.5, (currentVals.size) / 2,\exp,0,1].asSpec,
				[0,1,\lin,0,0.5].asSpec, AngleSpec()
			] },
			calculate: { |evt, values|
				var min, max, size, blend, periods, width, phase, out;
				min = values.minItem;
				max = values.maxItem;
				size = values.size;
				#blend, periods, width, phase = evt.settings;
				phase = phase / pi;
				out = values.collect({ |item,i|
					var val;
					val = i.linlin(0, size, phase, periods + phase ).wrap(0.0, 1.0);
					if( val < width ) {
						val = val.linlin( 0, width, 0, 1 );
					} {
						val = val.linlin( width, 1, 1, 0 );
					};
				});
				values.blend( out.normalize(0,1).linlin(0,1,min,max), blend );
			},
		);

		operations[ 'code...' ] =  { |values|
			CodeEditView( object: values ).action_({ |cew|
				var res;
				res = cew.object;
				if( res.isKindOf( Function ) ) {
					res = Array.fill( this.size, res );
				};
				res = this.constrain( res.asArray );
				res.postln;
				action.value( res );
			});
			nil;
		};

		operations[ \post ] = { |values| values.postcs; };

		^operations;
	}

	makeMenu { |hasEdit = true, inView, action, filter|
		var currentVals, operations;
		var makeItem;
		var menu;

		RoundView.pushSkin( UChainGUI.skin ++ ( labelWidth: 60 ));

		if( this.respondsTo( \unmap ) ) { currentVals = this.unmap( inView[ \val ] ); };

		operations = this.makeOperations( currentVals, action );

		makeItem = { |key = 'sine'|
			var operation;
			operation = operations[ key ];
			switch( operation.class,
				Event, {
					operation.comp = View().minWidth_(300).minHeight_(
						(operation.specs.collect(_.viewNumLines).sum * 16) + 6
					);
					operation.comp.addFlowLayout( 2@4, 2@2 );
					operation.specs.do({ |spec, i|
						var vw;
						vw = spec.makeView(
							operation.comp,
							294@(spec.viewNumLines * 14),
							operation.labels[ i ],
							{ |vws, val|
								operation.settings[ i ] = val;
								action.value( this.map( operation.calculate( currentVals ) ) );
							}
						);
						spec.setView( vw, operation.settings[ i ] );
					});
					Menu( CustomViewAction( operation.comp ) ).title_( key.asString )
				},
				Function, {
					MenuAction( key, {
						var res;
						res = operation.value( inView[ \val ] );
						if( res.notNil ) {
							action.value( res );
							if( this.respondsTo( \unmap ) ) { currentVals = this.unmap( inView[ \val ] ); };
						};
					});
				}
			)
		};

		menu = Menu(
			*operations.keys.select({ |key|
				if( filter.isNil ) { true } {
					filter.includes( key );
				};
			}).collect({ |key| makeItem.( key ) })
		).title_( "%".format( inView[ \label ] ) );

		if( thisProcess.platform.name == \osx ) { menu.tearOff_( true ); };

		if( hasEdit ) {
			menu.insertAction(0, MenuAction.separator( "Operations" ) );
			menu.insertAction(0, MenuAction( "Edit", {
				if( inView[ \editWin ].notNil && { inView[ \editWin ].w.isClosed.not } ) {
					inView[ \editWin ].front;
				} {
					this.makeEditWindow( inView, inView[ \val ].copy, inView.label, { |vals|
						this.setView( inView, vals, true );
					});
					menu.deepDestroy;
				};
			})
			);
		};

		RoundView.popSkin;

		^menu.uFront;
	}
}

+ Nil {
	viewNumLines { ^1 }
}

+ ControlSpec {

	makeView { |parent, bounds, label, action, resize|
		var view, vws, stp, labelWidth;
		vws = ();

		if( (minval != -inf) && { maxval != inf } ) {
			vws[ \valueView ] =
				EZSmoothSlider( parent, bounds, label !? { label.asString ++ " " },
					this, { |vw| action.value( vw, vw.value ) },
					labelWidth: (RoundView.skin ? ()).labelWidth ? 80,
				    numberWidth: (RoundView.skin ? ()).numberWidth ? 40,
				    gap: 2@2
			);

			vws[ \view ] = vws[ \valueView ].view;
			vws[ \sliderView ] = vws[ \valueView ].sliderView;
			vws[ \sliderView ].centered_( true ).centerPos_( this.unmap( default ) );

		} {
			view = EZCompositeView( parent, bounds );
			vws[ \view ] = view.view;
			bounds = view.view.bounds;
			stp = this.step;
			if( stp == 0 ) { stp = 1 };

			if( label.notNil ) {
				labelWidth = (RoundView.skin ? ()).labelWidth ? 80;
				vws[ \labelView ] = StaticText( view, labelWidth @ 14 )
					.string_( label.asString ++ " " )
					.align_( \right )
					.resize_( 4 )
					.applySkin( RoundView.skin );
			} {
				labelWidth = -4;
			};

			vws[ \valueView ] = SmoothNumberBox( view,
					Rect(labelWidth + 4,0,bounds.width-(labelWidth + 4),bounds.height)
				)
			    .action_({ |vw|
			        action.value( vw, vw.value );
			    } ).resize_(5)
				.step_( stp )
				.scroll_step_( stp )
				.clipLo_( this.minval )
				.clipHi_( this.maxval );
		};
		if( resize.notNil ) { vws.view.resize = resize };
		^vws;
	}

	setView { |vws, value, active = false|
		vws[ \valueView ].value = value;
		if( active ) { vws[ \valueView ].doAction };
	}

	mapSetView { |vws, value, active = false|
		vws[ \valueView ].value = this.map(value);
		if( active ) { vws[ \valueView ].doAction };
	}

	adaptFromObject { |object| // if object out of range; change range
		if( object.isArray ) {
			^this.asRangeSpec.adaptFromObject( object );
		} {
			if( object.inclusivelyBetween( minval, maxval ).not ) {
				^this.copy
					.minval_( minval.min( object ) )
					.maxval_( maxval.max( object ) )
			};
			^this;
		};
	}
}

+ ListSpec {

	makeView { |parent, bounds, label, action, resize|
		var multipleActions = action.size > 0;
		var vw, view, menu;
		var lbls, viewWidth, labelWidth;
		if( modern ) {
			view = EZCompositeView( parent, bounds, gap: 2@2 );
			view.asView.resize_( resize ? 5 );
			bounds = view.asView.bounds;

			if( label.notNil ) {
				labelWidth = RoundView.skin.labelWidth ? 100;
				StaticText( view, labelWidth @ (bounds.height) )
				.applySkin( RoundView.skin )
				.align_( \right )
				.string_( "% ".format( label ) );
				viewWidth = bounds.width - labelWidth - 2;
			} {
				viewWidth = bounds.width;
			};

			menu = UPopUpMenu( view, viewWidth @ (bounds.height) )
			.resize_( resize ? 2 )
			.items_( labels !? { |x| x.asCollection.collect(_.value); } ? list );

			if( multipleActions ) {
				menu.action_({ |pu| action[ pu.value ].value( vw, list[ pu.value ] ) })
			} {
				menu.action_({ |pu| action.value( vw, list[ pu.value ] ) })
			};

			if( label.notNil ) { menu.title_( label ) };

			^menu
		} {
			lbls = labels.asCollection.collect(_.value);
			vw = EZPopUpMenu( parent, bounds, label !? { label.asString ++ " " },
				if( multipleActions ) {
					list.collect({ |item, i|
						(lbls[i] ? item.asSymbol) -> { |vw| action[i].value( vw, list[i] ) };
					});
				} { list.collect({ |item, i|
					(lbls[i] ? item.asSymbol) -> nil
				})
				},
				initVal: defaultIndex
			);
			if( multipleActions.not ) {
				vw.globalAction = { |vw| action.value( vw, list[vw.value] ) };
			};
			vw.labelWidth = 80; // same as EZSlider
			vw.applySkin( RoundView.skin ); // compat with smooth views
			vw.labelView.applySkin( RoundView.skin );
			vw.menu.applySkin( RoundView.skin );
			if( resize.notNil ) { vw.view.resize = resize };
			^vw
		}
	}

	setView { |view, value, active = false|
		{  // can call from fork
			view.value = this.unmap( value );
			if( active ) { view.doAction };
		}.defer;
	}

	mapSetView { |view, value, active = false|
		{
			view.value = value;
			if( active ) { view.doAction };
		}.defer;
	}

	adaptFromObject { |object|
		if( list.any({ |item| item == object }).not ) {
			^this.copy.add( object )
		} {
			^this
		};
	}


}

+ ArrayControlSpec {

	makeView { |parent, bounds, label, action, resize, hasEdit = true|
		var vws, view, labelWidth, width;
		var localStep;
		var modeFunc;
		var font;
		var editAction;
		var tempVal;
		var optionsWidth = 33, operationsOffset = 1, editWidth = 40;
		var isMassEdit;
		var hiliteColor;
		var menu;
		vws = ();

		isMassEdit = UGUI.nowBuildingUnit.isKindOf( MassEditU );

		font =  (RoundView.skin ? ()).font ?? { Font( Font.defaultSansFace, 10 ); };

		hiliteColor = RoundView.skin[ \SmoothSlider ] !? _.hiliteColor ?? { Color(0,0,0,0.33); };

		bounds.isNil.if{bounds= 350@20};

		view = EZCompositeView( parent, bounds, gap: 4@4 );
		view.asView.resize_( resize ? 5 );
		bounds = view.asView.bounds;
		width = bounds.width;

		vws[ \view ] = view;
		vws[ \label ] = label;
		vws[ \val ] = default.asCollection;
		vws[ \range ] = [ vws[ \val ] .minItem, vws[ \val ].maxItem ];
		vws[ \doAction ] = { action.value( vws, vws[ \val ] ) };

		if( GUI.id === \qt ) {
			if( isMassEdit ) {
				optionsWidth = 40; operationsOffset = 0;
			} {
				optionsWidth = 40; operationsOffset = 0;
			};
		} {
			if( isMassEdit ) {
				optionsWidth = editWidth = 40;
			} {
				optionsWidth = editWidth = 33;
			};
		};

		if( label.notNil ) {
			labelWidth = (RoundView.skin ? ()).labelWidth ? 80;
			vws[ \labelView ] = StaticText( vws[ \view ], labelWidth @ bounds.height )
			.string_( label.asString ++ " " )
			.align_( \right )
			.resize_( 4 )
			.applySkin( RoundView.skin );
			width = width - labelWidth - 2;
		} {
			labelWidth = 0;
		};

		vws[ \rangeSlider ] = EZSmoothRanger( view, (width - optionsWidth - 6) @ (bounds.height),
			nil, this.asControlSpec, { |sl|
				var values, min, max;
				values = this.unmap( vws[ \val ] );
				vws[ \range ] = sl.value;
				#min, max = this.unmap( vws[ \range ] );
				if( min == max ) { max = max + 1.0e-11 };
				values = values.linlin( values.minItem, values.maxItem, min, max );
				if( values.every(_==min) ) {
					values = Array.series( values.size, min, ((max - min)/(values.size-1)) );
				};
				vws[ \val ] = this.map( values );
				vws[ \setPlotter ].value;
				vws[ \setMeanSlider ].value;
				action.value( vws, vws[ \val ] );
			}, numberWidth: (RoundView.skin ? ()).numberWidth ? 40, gap: 2@2
		);

		if( warp.isKindOf( ExponentialWarp ) ) {
			[ vws[ \rangeSlider ].loBox, vws[ \rangeSlider ].hiBox ].do({ |box|
				box.allowedChars = "+-.AaBbCcDdEeFfGgMmTtpi#*/()%";
				box.interpretFunc = { |string, val|
					var cents = 0, splits;
					string = string.format( val );
					case { "AaBbCcDdEeFfGg".includes(string.first) } {
						if( string.indexOf( $+ ).notNil ) {
							cents = string.split( $+ ).last.interpret;
						} {
							splits = string.split($-);
							if( splits.size > 1 ) {
								if( splits[ splits.size-2 ].last.isDecDigit ) {
									cents = splits.last.interpret.neg;
								};
							};
						};
						string.namecps * (cents / 100).midiratio;
					} { "Mm".includes(string.first) } {
						string[1..].interpret.midicps;
					} { "Tt".includes(string.first) } {
						("0" ++ string[1..]).interpret.midiratio * val;
					} { string.find( "pi" ).notNil } {
						string.interpret;
					} {
						string.interpret;
					};
				};
			})
		};

		vws[ \setRangeSlider ] = {
			var min, max;
			min = vws[ \val ].minItem;
			max = vws[ \val ].maxItem;
			vws[ \rangeSlider ].value_( [ min, max ] );
		};

		vws[ \setRangeSlider ].value;

		vws[ \meanSlider ] = SmoothSlider(
			vws[ \rangeSlider ].rangeSlider.parent,
			vws[ \rangeSlider ].rangeSlider.bounds.insetAll(0,0,0,
				vws[ \rangeSlider ].rangeSlider.bounds.height * 0.6 )
		)
		.hiliteColor_( nil )
		.background_( Color.white.alpha_(0.125) )
		.knobSize_(0.6)
		.mode_( \move )
		.action_({ |sl|
			var values, min, max, mean;
			values = this.unmap( vws[ \val ] );
			min = values.minItem;
			max = values.maxItem;
			mean = [ min, max ].mean;
			values = values.normalize( *(([ min, max ] - mean) + sl.value).clip(0,1) );
			vws[ \val ] = this.map( values );
			vws[ \setPlotter ].value;
			vws[ \setRangeSlider ].value;
			action.value( vws, vws[ \val ] );
		});

		vws[ \meanSlider ].mouseDownAction = { |sl, x,y,mod, xx, clickCount|
			if( clickCount == 2 ) {
				vws[ \val ] = this.map( sl.value ! vws[ \val ].size );
				vws[ \setRangeSlider ].value;
				vws[ \setPlotter ].value;
				action.value( vws, vws[ \val ] );
			};
		};

		vws[ \setMeanSlider ] = {
			var min, max;
			min = vws[ \val ].minItem;
			max = vws[ \val ].maxItem;
			vws[ \meanSlider ].value_( this.unmap( [ min, max ] ).mean );
		};

		vws[ \setMeanSlider ].value;

		vws[ \options ] = UserView( view, optionsWidth @  (bounds.height) )
		.background_( Color.white.alpha_( 0.25 ) )
		.drawFunc_({ |vw|
			var bounds, vals, size, def;
			Pen.color = hiliteColor;
			bounds = vw.bounds.moveTo(0,0);
			vals = this.unmap( vws[ \val ] ).linlin(0,1,bounds.height,0);
			size = vals.size;
			def = this.unmap( this.originalSpec !? _.default ? 0 ).linlin(0,1,bounds.height,0);
			Pen.moveTo( bounds.left @ def );
			vals.do({ |val, i|
				Pen.lineTo( i.linlin(0,size,0,bounds.width) @ val );
				Pen.lineTo( (i + 1).linlin(0,size,0,bounds.width) @ val );
			});
			Pen.lineTo( bounds.width @ def );
			Pen.lineTo( bounds.left @ def );
			Pen.fill;
		}).mouseDownAction_({
			menu = this.makeMenu( hasEdit, vws, { |values|
				vws[ \val ] = values;
				vws[ \update ].value;
				action.value( vws, vws[ \val ] );
			});
		});

		if( GUI.id != \qt ) {
			vws[ \edit ] = SmoothButton( view, editWidth @ (bounds.height) )
			.label_( "edit" )
			.radius_( 2 )
			.font_( font )
			.action_({
				vws[ \operations ][ \edit ].value;
			});
			vws[ \edit ].resize_(3);
		};

		if( isMassEdit.not ) {
			vws[ \expand ] = SmoothButton( view, 12 @ 12 )
			.label_( '+' )
			.action_({
				action.value( vws, UMap( \expand ) );
			})
			.resize_(3);
		};

		vws[ \update ] = {
			vws[ \setRangeSlider ].value;
			vws[ \setMeanSlider ].value;
			vws[ \editWin ] !? _.setValues( vws[ \val ] );
			{ vws[ \options ].refresh; }.defer;
		};

		vws[ \rangeSlider ].view.resize_(2);
		vws[ \meanSlider ].resize_(2);
		vws[ \options ] !? _.resize_(3);

		view.view.onClose_({
			vws[ \editWin ] !? _.close;
			menu !? _.deepDestroy;
		});

		^vws;
	}

	setView { |vws, value, active = false|
		vws[ \val ] = value.asCollection;
		vws[ \update ].value;
		if( active ) { vws[ \doAction ].value };
	}

	mapSetView { |vws, value, active = false|
		this.setView( vws, this.map(value), active );
	}

}

+ GenericMassEditSpec {

	viewNumLines { ^this.originalSpec.viewNumLines }

	makeView { |parent, bounds, label, action, resize, hasEdit = true|
		var vws, view, labelWidth, width;
		var localStep;
		var modeFunc;
		var font;
		var editAction;
		var tempVal;
		var optionsWidth = 40;
		var isMassEdit = true;
		var hiliteColor;
		var menu, compWidth;
		var canMap = false;
		var hasDefault;

		vws = ();

		font = (RoundView.skin ? ()).font ?? { Font( Font.defaultSansFace, 10 ); };

		hiliteColor = RoundView.skin[ \SmoothSlider ] !? _.hiliteColor ?? { Color(0,0,0,0.33); };

		hasDefault = this.originalSpec.respondsTo( \default );

		bounds.isNil.if{bounds= 350@20};

		view = EZCompositeView( parent, bounds, gap: 4@4 );
		view.asView.resize_( resize ? 5 );
		bounds = view.asView.bounds;
		width = bounds.width;

		vws[ \view ] = view;
		vws[ \label ] = label;
		if( hasDefault ) { vws[ \val ] = [ this.originalSpec.default ]; } { vws[ \val ] = [ nil ] };
		vws[ \doAction ] = { action.value( vws, vws[ \val ] ) };

		if( originalSpec.respondsTo( \unmap ) && {
			if( hasDefault ) {
				originalSpec.unmap( originalSpec.default ).isNumber
			} { false }
		}) { canMap = true };

		if( label.notNil ) {
			labelWidth = (RoundView.skin ? ()).labelWidth ? 80;
			vws[ \labelView ] = StaticText( vws[ \view ], labelWidth @ 14 )
			.string_( label.asString ++ " " )
			.align_( \right )
			.resize_( 4 )
			.applySkin( RoundView.skin );
			width = width - labelWidth - 2;
		} {
			labelWidth = 0;
		};

		compWidth = bounds.width - labelWidth - 8 - optionsWidth;

		vws[ \specComp ] = CompositeView( view, compWidth @  (bounds.height) );

		vws[ \specView ] = this.originalSpec.makeView(
			vws[ \specComp ],
			vws[ \specComp ].bounds.moveTo(0,0),
			nil, { |vw, val|
				vws[ \val ] = vws[ \val ].collect({ val.deepCopy });
				vws[ \update ].value;
				action.value( vws, vws[ \val ] );
		}, 5
		);

		view.asView.decorator.shift( compWidth.neg - 4, 0 );

		vws[ \mixedComp ] = View( view, compWidth @  (bounds.height) );

		vws[ \mixedView ] = StaticText( vws[ \mixedComp ], vws[ \mixedComp ].bounds.moveTo(0,0).height_(14) )
		.string_( " mixed" )
		.applySkin( RoundView.skin );

		vws[ \mixedView ].setProperty(\wordWrap, false);


		vws[ \options ] = UserView( view, optionsWidth @ 14 )
		.background_( Color.white.alpha_( 0.25 ) )
		.mouseDownAction_({
			menu = this.makeMenu( hasEdit, vws, { |values|
				vws[ \val ] = values;
				vws[ \update ].value;
				action.value( vws, vws[ \val ] );
			}, operations ?? { [ \reverse, \scramble, 'use first for all', \rotate, 'move item', 'code...', \post ] });
		});

		if( canMap ) {
			vws[ \options ].drawFunc_({ |vw|
				var bounds, vals, size, def;
				Pen.color = hiliteColor;
				bounds = vw.bounds.moveTo(0,0);
				vals = vws[ \val ].collect({ |val|
					this.originalSpec.unmap( val )
				});
				if( vals.any(_ > 1) ) {
					vals = vals.normalize(0,1);
				};
				vals = vals.linlin(0,1,bounds.height,0);
				size = vals.size;
				if( hasDefault ) {
					def = this.originalSpec.unmap( this.originalSpec.default ).linlin(0,1,bounds.height,0);
				} {
					def = vals.mean.linlin( 0,1, bounds.height,0 );
				};
				Pen.moveTo( bounds.left @ def );
				vals.do({ |val, i|
					Pen.lineTo( i.linlin(0,size,0,bounds.width) @ val );
					Pen.lineTo( (i + 1).linlin(0,size,0,bounds.width) @ val );
				});
				Pen.lineTo( bounds.width @ def );
				Pen.lineTo( bounds.left @ def );
				Pen.fill;
			});
		};

		vws[ \update ] = {
			var isSingle, string;
			vws[ \editWin ] !? _.setValues( vws[ \val ] );
			isSingle = vws[ \val ].every({ |item| item == vws[ \val ][ 0 ] });
			if( isSingle && { vws[ \val ][0].notNil } ) {
				this.originalSpec.setView( vws[ \specView ], vws[ \val ][0], false );
			};
			{
				if( isSingle ) {
					vws[ \specComp ].visible = true;
					vws[ \mixedComp ].visible = false;
				} {
					vws[ \specComp ].visible = false;
					vws[ \mixedComp ].visible = true;
					string =  " " ++ vws[ \val ].cs;
					if( string.bounds( font ).width <= vws[ \mixedView ].bounds.width ) {
						vws[ \mixedView ].string = string;
					} {
						vws[ \mixedView ].string = " mixed (% items)".format( vws[ \val ].size );
					};
				};
				vws[ \options ].refresh;
			}.defer;
		};

		vws[ \options ] !? _.resize_(3);

		view.view.onClose_({
			vws[ \editWin ] !? _.close;
			menu !? _.deepDestroy;
		});

		^vws;
	}

	setView { |vws, value, active = false|
		vws[ \val ] = value.asCollection;
		vws[ \update ].value;
		if( active ) { vws[ \doAction ].value };
	}
}


+ MultiRangeSpec {

	viewNumLines { ^1 }

	originalSpec {
		^originalSpec ?? { this.asRangeSpec };
	}

	makeView { |parent, bounds, label, action, resize, hasEdit = true|
		var vws, view, labelWidth, width;
		var localStep;
		var modeFunc;
		var font;
		var editAction;
		var tempVal;
		var optionsWidth = 40;
		var isMassEdit = true;
		var hiliteColor;
		var menu, compWidth;
		var canMap = false;
		var hasDefault;

		vws = ();

		font = (RoundView.skin ? ()).font ?? { Font( Font.defaultSansFace, 10 ); };

		hiliteColor = RoundView.skin[ \SmoothSlider ] !? _.hiliteColor ?? { Color(0,0,0,0.33); };

		hasDefault = this.originalSpec.respondsTo( \default );

		bounds.isNil.if{bounds= 350@20};

		view = EZCompositeView( parent, bounds, gap: 4@4 );
		view.asView.resize_( resize );
		bounds = view.asView.bounds;
		width = bounds.width;

		vws[ \view ] = view;
		vws[ \label ] = label;
		vws[ \val ] = this.default;
		vws[ \setRange ] = {
			vws[ \range ] = [ vws[ \val ].flat.minItem, vws[ \val ].flat.maxItem ];
		};
		vws[ \setRange ].value;
		vws[ \doAction ] = { action.value( vws, vws[ \val ] ) };

		canMap = true;

		if( label.notNil ) {
			labelWidth = (RoundView.skin ? ()).labelWidth ? 80;
			vws[ \labelView ] = StaticText( vws[ \view ], labelWidth @ 14 )
			.string_( label.asString ++ " " )
			.align_( \right )
			.resize_( 4 )
			.applySkin( RoundView.skin );
			width = width - labelWidth - 2;
		} {
			labelWidth = 0;
		};

		compWidth = bounds.width - labelWidth - 8 - optionsWidth;

		vws[ \specComp ] = CompositeView( view, compWidth @  (bounds.height) );

		vws[ \specView ] = this.originalSpec.makeView(
			vws[ \specComp ],
			vws[ \specComp ].bounds.moveTo(0,0),
			nil, { |vw, val|
				var rrange, unmapped;
				rrange = this.unmap( vws[ \range ] ++ val );
				unmapped = this.unmap( vws[ \val ] );
				if( vws[ \range ][ 0 ] == vws[ \range ][ 1 ] ) {
					unmapped = unmapped.collect({ |item|
						item + [0, 1.0e-12];
					});
				};
				vws[ \val ] = this.map(	unmapped.linlin( *rrange ) );
				vws[ \update ].value;
				action.value( vws, vws[ \val ] );
		}, 5
		);

		vws[ \options ] = UserView( view, optionsWidth @ 14 )
		.background_( Color.white.alpha_( 0.25 ) )
		.mouseDownAction_({
			menu = this.makeMenu( hasEdit, vws, { |values|
				vws[ \val ] = values;
				vws[ \update ].value;
				action.value( vws, vws[ \val ] );
			}, [ \reverse, \scramble, 'use first for all', \rotate, 'move item', 'code...', \post ]);
		});

		if( canMap ) {
			vws[ \options ].drawFunc_({ |vw|
				var bounds, vals, size, def;
				Pen.color = hiliteColor;
				bounds = vw.bounds.moveTo(0,0);
				vals = vws[ \val ].collect({ |val|
					this.originalSpec.unmap( val )
				});
				vals = vals.linlin(0,1,bounds.height,0);
				size = vals.size;
				Pen.moveTo( bounds.left @ vals[0][0] );
				vals.do({ |val, i|
					Pen.lineTo( i.linlin(0,size,0,bounds.width) @ val[0] );
					Pen.lineTo( (i + 1).linlin(0,size,0,bounds.width) @ val[0] );
				});
				vals.reverseDo({ |val, i|
					Pen.lineTo( i.linlin(0,size,bounds.width,0) @ val[1] );
					Pen.lineTo( (i + 1).linlin(0,size,bounds.width,0) @ val[1] );
				});
				Pen.moveTo( bounds.left @ vals[0][0] );
				Pen.fill;
			});
		};

		vws[ \update ] = {
			vws[ \editWin ] !? _.setValues( vws[ \val ] );
			vws[ \setRange ].value;
			this.originalSpec.setView( vws[ \specView ], vws[ \range ], false );
			{ vws[ \options ].refresh; }.defer;
		};

		vws[ \options ] !? _.resize_(3);

		view.view.onClose_({
			vws[ \editWin ] !? _.close;
			menu !? _.deepDestroy;
		});

		^vws;
	}

	setView { |vws, value, active = false|
		vws[ \val ] = value.asCollection;
		vws[ \update ].value;
		if( active ) { vws[ \doAction ].value };
	}
}


+ StringSpec {

	makeView { |parent, bounds, label, action, resize|
		var vws, view, labelWidth, stringBackground;
		vws = ();

		// this is basically an EZButton

		bounds.isNil.if{bounds= 320@20};

		#view, bounds = EZGui().prMakeMarginGap.prMakeView( parent, bounds );
		 vws[ \view ] = view;

		if( label.notNil ) {
			labelWidth = (RoundView.skin ? ()).labelWidth ? 80;
			vws[ \labelView ] = StaticText( vws[ \view ], labelWidth @ bounds.height )
				.string_( label.asString ++ " " )
				.align_( \right )
				.resize_( 4 )
				.applySkin( RoundView.skin );
		} {
			labelWidth = 0;
		};

		vws[ \setString ] = { |vws, string = ""|
			vws[ \string ] = string;
			{
				vws[ \stringView ].value = vws[ \string ];
				vws[ \stringView ].background = stringBackground;
			}.defer;
		};

		vws[ \stringView ] = TextField( view,
			Rect( labelWidth + 2, 0, bounds.width-(labelWidth+2), bounds.height )
		)	.resize_(2)
			.applySkin( RoundView.skin ? () )
			.action_({ |tf|
				vws[ \task ].stop;
				vws.setString( this.constrain( tf.value ) );
				action.value( vws, vws[ \string ] );
			})
			.mouseDownAction_({ |view|
				vws[ \task ].stop;
				vws[ \task ] = {
					block { |break|
						loop {
							0.1.wait;
							if( view.isClosed ) { break.value; };
							if( view.hasFocus.not ) { break.value; };
							if( view.value != vws[ \string ] ) {
								view.background = Color.red.blend( stringBackground, 0.5 );
							} {
								view.background = stringBackground
							};
						};
					};
				}.fork( AppClock );
			});

		stringBackground = vws[ \stringView ].background;

		if( resize.notNil ) { vws[ \view ].resize = resize };
		^vws;
	}

	setView { |view, value, active = false|
		view.setString( this.constrain( value ).asString );
		{
			view.setString( this.constrain( value ).asString );
			view[ \stringView ].value = this.constrain( value ).asString;

		}.defer;
		if( active ) { view[ \string ].doAction };
	}

}

+ IPSpec {

	makeView { |parent, bounds, label, action, resize|
		var vws, view, labelWidth, textViewWidth, stringBackground;
		vws = ();

		// this is basically an EZButton

		bounds.isNil.if{bounds= 320@20};

		#view, bounds = EZGui().prMakeMarginGap.prMakeView( parent, bounds );
		 vws[ \view ] = view;

		if( label.notNil ) {
			labelWidth = (RoundView.skin ? ()).labelWidth ? 80;
			vws[ \labelView ] = StaticText( vws[ \view ], labelWidth @ bounds.height )
				.string_( label.asString ++ " " )
				.align_( \right )
				.resize_( 4 )
				.applySkin( RoundView.skin );
		} {
			labelWidth = 0;
		};

		vws[ \setString ] = { |vws, string = ""|
			vws[ \string ] = string;
			{
				vws[ \stringView ].value = vws[ \string ];
				vws[ \stringView ].background = stringBackground;
			}.defer;
		};

		textViewWidth = bounds.width-(labelWidth+4+(bounds.height));

		vws[ \stringView ] = TextField( view,
			Rect( labelWidth + 2, 0, textViewWidth, bounds.height )
		)	.resize_(2)
			.applySkin( RoundView.skin )
			.action_({ |tf|
				vws[ \task ].stop;
				vws.setString( this.constrain( tf.value ) );
				action.value( vws, vws[ \string ] );
			})
			.mouseDownAction_({ |view|
				vws[ \task ].stop;
				vws[ \task ] = {
					block { |break|
						loop {
							0.1.wait;
							if( view.isClosed ) { break.value; };
							if( view.hasFocus.not ) { break.value; };
							if( view.value != vws[ \string ] ) {
								view.background = Color.red.blend( stringBackground, 0.5 );
							} {
								view.background = stringBackground;
							};
						};
					};
				}.fork( AppClock );
			});

		stringBackground = vws[ \stringView ].background;

		vws[ \local ] = SmoothButton( view, Rect( labelWidth + 4 + textViewWidth, 0, bounds.height, bounds.height ) )
		.states_([["L"]])
		.radius_( 2 )
		.resize_(3)
		.canFocus_( false )
		.action_({
			vws[ \task ].stop;
			vws.setString( "127.0.0.1" );
			action.value( vws, vws[ \string ] );
		});

		if( resize.notNil ) { vws[ \view ].resize = resize };
		^vws;
	}

	setView { |view, value, active = false|
		view.setString( this.constrain( value ).asString );
		{
			view.setString( this.constrain( value ).asString );
			view[ \stringView ].value = this.constrain( value ).asString;

		}.defer;
		if( active ) { view[ \string ].doAction };
	}

}

+ EnvirSpec {

	makeView { |parent, bounds, label, action, resize|
		var vws, view, labelWidth;
		var ctrl, strWidth;
		vws = ();

		// this is basically an EZButton

		bounds.isNil.if{bounds= 320@20};

		#view, bounds = EZGui().prMakeMarginGap.prMakeView( parent, bounds );
		 vws[ \view ] = view;

		if( label.notNil ) {
			labelWidth = (RoundView.skin ? ()).labelWidth ? 80;
			vws[ \labelView ] = StaticText( vws[ \view ], labelWidth @ bounds.height )
				.string_( label.asString ++ " " )
				.align_( \right )
				.resize_( 4 )
				.applySkin( RoundView.skin );
		} {
			labelWidth = 0;
		};

		StaticText( view, Rect( labelWidth + 2, 0, 10, bounds.height ) )
			.applySkin( RoundView.skin ? () )
			.string_( "~" )
			.align_( \right );

		strWidth = bounds.width-(labelWidth+2+12+62);

		vws[ \string ] = TextField( view,
			Rect( labelWidth + 2 + 12, 0, strWidth, bounds.height )
		)	.resize_(2)
			.applySkin( RoundView.skin ? () )
			.action_({ |tf|
				if( tf.value != "" ) {
					action.value( vws, this.constrain( tf.value ) );
					vws[ \menu ].value = vws[ \menu ].items.indexOfEqual( "~" ++ (tf.value) ) ? 0;
					vws[ \setColor ].value;
				};
			});

		vws[ \setColor ] = {
			var hash;
			hash = vws[ \string ].value.hash;

			vws[ \string ].background = Color.new255(
				(hash & 16711680) / 65536,
				(hash & 65280) / 256,
				hash & 255,
				128
			).blend( Color.white, 2/3 );
		};

		vws[ \menu ] = UPopUpMenu( view,
			Rect( labelWidth + 2 + 12 + strWidth + 2, 0, 60, bounds.height )
		)	.resize_(3)
			.items_( [ "" ] )
			.action_({ |pu|
				var item;
				if( pu.value > 0 ) {
					item = pu.item.asString[1..];
					vws[ \string ].string = item;
					action.value( vws, this.constrain( item ) );
				} {
					vws[ \menu ].value = vws[ \menu ].items.indexOfEqual( "~" ++ (vws[ \string ].value) ) ? 0;
				};
			});

		vws[ \menu ].extraMenuActions = {[
				MenuAction.separator,
				MenuAction("Open Environment Window", { ULib.envirWindow; })
		]};

		ctrl = {
			var currentKeys;
			currentKeys = [ "" ] ++ (currentEnvironment[ \u_specs ] !? _.keys).asArray.sort.collect({ |item| "~" ++ item });
			{
				if( vws[ \menu ].items != currentKeys ) {
					vws[ \menu ].items = currentKeys;
					vws[ \menu ].value = vws[ \menu ].items.indexOfEqual( "~" ++ (vws[ \string ].value) ) ? 0;
				};
			}.defer;
		};

		currentEnvironment.addDependant( ctrl );

		ctrl.value;

		vws[ \menu ].onClose_( { currentEnvironment.removeDependant( ctrl ); } );

		if( resize.notNil ) { vws[ \view ].resize = resize };
		^vws;
	}

	setView { |view, value, active = false|
		value = this.constrain( value );
		{
			view[ \string ].value = value.asString;
			view[ \setColor ].value;
			view[ \menu ].value = view[ \menu ].items.indexOfEqual( "~" ++ value ) ? 0;
		}.defer;
		if( active ) { view[ \string ].doAction };
	}

}

+ SMPTESpec {

	makeView { |parent, bounds, label, action, resize|
		var vws, view, labelWidth;

		bounds.isNil.if{bounds= 160 @ 18 };

		vws = ();
		#view, bounds = EZGui().prMakeMarginGap.prMakeView( parent, bounds );
		vws[ \view ] = view;

		if( label.notNil ) {
			labelWidth = (RoundView.skin ? ()).labelWidth ? 80;
			vws[ \labelView ] = StaticText( vws[ \view ], labelWidth @ 14 )
				.string_( label.asString ++ " " )
				.align_( \right )
				.resize_( 4 )
				.applySkin( RoundView.skin );
		} {
			labelWidth = -2;
		};

		vws[ \box ] = SMPTEBox( vws[ \view ],
				Rect(labelWidth + 2,0,bounds.width-(labelWidth + 2),bounds.height)
			)
			.applySmoothSkin
		    .applySkin( RoundView.skin )
		    .action_({ |vw|
		        action.value( vw, vw.value );
		    } ).resize_(5)
		    .fps_( fps )
			.clipLo_( minval )
			.clipHi_( maxval );

		if( resize.notNil ) { vws[ \view ].resize = resize };
		^vws;
	}

	setView { |view, value, active = false|
		view[ \box ].value = value;
		if( active ) { view.doAction };
	}

}

+ TriggerSpec {

	makeView { |parent, bounds, label, action, resize|
		var vws, view, labelWidth;
		vws = ();

		// this is basically an EZButton

		bounds.isNil.if{bounds= 350@20};

		#view, bounds = EZGui().prMakeMarginGap.prMakeView( parent, bounds );
		 vws[ \view ] = view;
		 vws[ \val ] = this.default;

		if( label.notNil ) {
			labelWidth = (RoundView.skin ? ()).labelWidth ? 80;
			vws[ \labelView ] = StaticText( vws[ \view ], labelWidth @ bounds.height )
				.string_( label.asString ++ " " )
				.align_( \right )
				.resize_( 4 )
				.applySkin( RoundView.skin );
		} {
			labelWidth = 0;
		};

		vws[ \buttonView ] = SmoothButton( vws[ \view ],
				Rect( labelWidth + 2, 0, 60, bounds.height ) )
			.label_( this.label ? "set" );

		if( spec.notNil ) {
			vws[ \valueView ] = spec.asSpec.makeView( view,
				Rect( labelWidth + 64, 0, bounds.width-(labelWidth+64), bounds.height ),
				nil, { |vw, val| vws[ \val ] = val }
			);
			spec.setView( vws[ \valueView ], vws[ \val ] );
		};

		vws[ \buttonView ]
				.radius_( bounds.height / 8 )
				.mouseDownAction_({
					if( spec.notNil ) {
						action.value( vws, vws[ \val ] );
					} {
						action.value( vws, 1 );
					}
				})
				.resize_( 1 );

		vws[ \normalBackground ] = vws[ \buttonView ].background;

		vws[ \task ] = Task({
			0.1.wait;
			vws[ \buttonView ].background = vws[ \normalBackground ];
		}).start;

		if( resize.notNil ) { vws[ \view ].resize = resize };
		^vws;
	}

	setView { |view, value, active = false|
		if( view[ \task ].isPlaying ) {
			view[ \task ].stop;
		} {
			view[ \buttonView ].background = Color.red(0.75);
		};
		view[ \task ] = Task({
			0.1.wait;
			if( view[ \buttonView ].isClosed.not ) {
				view[ \buttonView ].background = view[ \normalBackground ];
			};
		}).start;
		view[ \val ] = value;
		if( view[ \valueView ].notNil ) {
			spec.setView( view[ \valueView ], view[ \val ] );
		};
		if( active ) { view[ \buttonView ].doAction };
	}

	mapSetView { |view, value, active = false|
		this.setView( view, value, active );
	}
}

+ BoolSpec {

	makeView { |parent, bounds, label, action, resize|
		var vws, view, labelWidth;
		vws = ();

		// this is basically an EZButton

		bounds.isNil.if{bounds= 160@20};

		#view, bounds = EZGui().prMakeMarginGap.prMakeView( parent, bounds );
		 vws[ \view ] = view;

		if( label.notNil ) {
			labelWidth = (RoundView.skin ? ()).labelWidth ? 80;
			vws[ \labelView ] = StaticText( vws[ \view ], labelWidth @ bounds.height )
				.string_( label.asString ++ " " )
				.align_( \right )
				.resize_( 4 )
				.applySkin( RoundView.skin );
		} {
			labelWidth = 0;
		};

		if( trueLabel.isNil && falseLabel.isNil ) {
			vws[ \buttonView ] = SmoothButton( vws[ \view ],
					Rect( labelWidth + 2, 0, bounds.height, bounds.height ) )
				.label_( [ "", 'x' ] )
			    .hiliteColor_( RoundView.skin.hiliteColor ?? { Color.black.alpha_(0.33) } )
		} {
			vws[ \buttonView ] = SmoothButton( vws[ \view ],
					Rect( labelWidth + 2, 0, bounds.width-(labelWidth+2), bounds.height ) )
				.label_( [ falseLabel ? "off", trueLabel ? "on" ] );
		};

		vws[ \buttonView ]
				.radius_( bounds.height / 8 )
				.value_( this.unmap( this.constrain( default ) ) )
				.action_({ |bt| action.value( vws, this.map( bt.value ) ) })
				.resize_( 1 );

		if( resize.notNil ) { vws[ \view ].resize = resize };
		^vws;
	}

	setView { |view, value, active = false|
		view[ \buttonView ].value = this.unmap( this.constrain( value ) );
		if( active ) { view[ \buttonView ].doAction };
	}

	mapSetView { |view, value, active = false|
		view[ \buttonView ].value = this.map(  value );
		if( active ) { view[ \buttonView ].doAction };
	}
}

+ BoolArraySpec {

	 originalSpec { ^BoolSpec( false, trueLabel, falseLabel ) }

	 makeView { |parent, bounds, label, action, resize, hasEdit = true|
		var vws, view, labelWidth, width;
		var localStep;
		var modeFunc;
		var font;
		var editAction;
		var tempVal;
		var hiliteColor;
		var menu;
		vws = ();

		font =  (RoundView.skin ? ()).font ?? { Font( Font.defaultSansFace, 10 ); };

		hiliteColor = RoundView.skin[ \SmoothSlider ] !? _.hiliteColor ?? { Color(0,0,0,0.33); };

		bounds.isNil.if{bounds= 350@20};

		view = EZCompositeView( parent, bounds, gap: 2@2 );
		bounds = view.asView.bounds;
		width = bounds.width;

		vws[ \view ] = view;
		vws[ \val ] = default.asCollection;
		vws[ \label ] = label;
		vws[ \doAction ] = { action.value( vws, vws[ \val ] ) };

		if( label.notNil ) {
			labelWidth = (RoundView.skin ? ()).labelWidth ? 80;
			vws[ \labelView ] = StaticText( vws[ \view ], labelWidth @ bounds.height )
				.string_( label.asString ++ " " )
				.align_( \right )
				.resize_( 4 )
				.applySkin( RoundView.skin );
			width = width - labelWidth - 2;
		} {
			labelWidth = 0;
		};

		if( trueLabel.isNil && falseLabel.isNil ) {
			vws[ \state ] = SmoothButton( view, (bounds.height)@(bounds.height) )
				.states_([
					[ "" ],
					[ 'x' ],
					[ '-', nil, Color.gray(0.2,0.25) ]
			    ]).hiliteColor_( hiliteColor )
		} {
			vws[ \state ] = SmoothButton( view, (bounds.width - 46 - labelWidth )@(bounds.height) )
			    .resize_(2)
				.states_([
					[ falseLabel ? "off" ],
					[ trueLabel ? "on" ],
					[ "mixed" , nil, Color.gray(0.2,0.25) ]
				])
		};

		vws[ \state ]
				.radius_( 2 )
				.font_( font )
				.action_({ |bt|
					switch( bt.value.asInteger,
						2, { vws[ \val ] = vws[ \val ].collect( false ); },
						1, { vws[ \val ] = vws[ \val ].collect( true ); },
						0, { vws[ \val ] = vws[ \val ].collect( false ); }
					);
					vws[ \update ].value;
					action.value( vws, vws[ \val ] );
				});

		view.decorator.left_( bounds.width - 40 );

		vws[ \options ] = UserView( view, 40 @  (bounds.height) )
		.background_( Color.white.alpha_( 0.25 ) )
		.drawFunc_({ |vw|
			var bounds, vals, size, def;
			Pen.color = hiliteColor;
			bounds = vw.bounds.moveTo(0,0);
			vals = this.unmap( vws[ \val ] ).linlin(0,1,bounds.height,0);
			size = vals.size;
			//def = this.unmap( this.originalSpec !? _.default ? 0 ).linlin(0,1,bounds.height,0);
			def = bounds.height/2;
			Pen.moveTo( bounds.left @ def );
			vals.do({ |val, i|
				Pen.lineTo( i.linlin(0,size,0,bounds.width) @ val );
				Pen.lineTo( (i + 1).linlin(0,size,0,bounds.width) @ val );
			});
			Pen.lineTo( bounds.width @ def );
			Pen.lineTo( bounds.left @ def );
			Pen.fill;
		}).mouseDownAction_({
			menu = this.makeMenu( hasEdit, vws, { |values|
				vws[ \val ] = values;
				vws[ \update ].value;
				action.value( vws, vws[ \val ] );
			}, [ \invert, \reverse, \scramble, \random, \line, \rotate, 'move item', \flat, 'code...', \post ]);
		});

		vws[ \update ] = {
			case { vws[ \val ].every(_ == true) } {
				vws[ \state ].value = 1;
			} { vws[ \val ].every(_ == false) } {
				vws[ \state ].value = 0;
			} { vws[ \state ].value = 2; };
			vws[ \editWin ] !? _.setValues( vws[ \val ] );
			{ vws[ \options ].refresh }.defer;
		};

		view.view.onClose_({
			vws[ \editWin ] !? _.close;
			menu !? _.deepDestroy;
		});

		^vws;
	 }

	 setView { |vws, value, active = false|
		vws[ \val ] = value.asCollection.collect(_.booleanValue);
		vws[ \update ].value;
		if( active ) { vws[ \doAction ].value };
	}

	mapSetView { |vws, value, active = false|
		this.setView( vws, this.map(value), active );
	}

}

+ PointSpec {

	makeView { |parent, bounds, label, action, resize|
		var vws, view, labelWidth;
		var localStep;
		var startVal;
		vws = ();

		vws[ \val ] = 0@0;

		localStep = step.copy;
		if( step.x == 0 ) { localStep.x = 1 };
		if( step.y == 0 ) { localStep.y = 1 };

		bounds.isNil.if{bounds= 160@20};

		#view, bounds = EZGui().prMakeMarginGap.prMakeView( parent, bounds );
		 vws[ \view ] = view;

		if( label.notNil ) {
			labelWidth = (RoundView.skin ? ()).labelWidth ? 80;
			vws[ \labelView ] = StaticText( vws[ \view ], labelWidth @ bounds.height )
				.string_( label.asString ++ " " )
				.align_( \right )
				.resize_( 4 )
				.applySkin( RoundView.skin );
		} {
			labelWidth = 0;
		};

		vws[ \x ] = SmoothNumberBox( vws[ \view ], Rect( labelWidth + 2, 0, 40, bounds.height ) )
			.action_({ |nb|
				vws[ \val ] = nb.value @ vws[ \y ].value;
				action.value( vws, vws[ \val ]);
			})
			//.step_( localStep.x )
			.scroll_step_( localStep.x )
			.clipLo_( rect.left )
			.clipHi_( rect.right )
			.value_(0);

		vws[ \xy ] = XYView( vws[ \view ],
			Rect( labelWidth + 2 + 42, 0, bounds.height, bounds.height ) )
			.action_({ |xy|
				startVal = startVal ?? { vws[ \val ].copy; };
				vws[ \x ].value = (startVal.x + (xy.x * localStep.x))
					.clip( rect.left, rect.right );
				vws[ \y ].value = (startVal.y + (xy.y * localStep.y.neg))
					.clip( rect.top, rect.bottom );
				action.value( vws, vws[ \x ].value @ vws[ \y ].value );
			})
			.mouseUpAction_({
				vws[ \val ] = vws[ \x ].value @ vws[ \y ].value;
				startVal = nil;
			});

		vws[ \y ] = SmoothNumberBox( vws[ \view ],
				Rect( labelWidth + 2 + 42 + bounds.height + 2, 0, 40, bounds.height ) )
			.action_({ |nb|
				vws[ \val ] = vws[ \x ].value @ nb.value;
				action.value( vws,  vws[ \val ] );
			})
			//.step_( localStep.y )
			.scroll_step_( localStep.y )
			.clipLo_( rect.top )
			.clipHi_( rect.bottom )
			.value_(0);

		^vws;
	}

	setView { |view, value, active = false|
		var constrained;
		constrained = this.constrain( value );
		view[ \x ].value = constrained.x;
		view[ \y ].value = constrained.y;
		view[ \val ] = constrained;
		if( active ) { view[ \x ].doAction };
	}

	mapSetView { |view, value, active = false|
		var mapped;
		mapped = this.map( value );
		view[ \x ].value = mapped.x;
		view[ \y ].value = mapped.y;
		view[ \val ] = mapped;
		if( active ) { view[ \x ].doAction };
	}

}

+ CodeSpec {

	makeView { |parent, bounds, label, action, resize|
		var vws, view, labelWidth;
		var localStep;
		var modeFunc;
		var font;
		var editAction;
		var tempVal;
		vws = ();

		font =  (RoundView.skin ? ()).font ?? { Font( Font.defaultSansFace, 10 ); };

		bounds.isNil.if{bounds= 160@20};

		view = EZCompositeView( parent, bounds, gap: 2@2 );
		bounds = view.asView.bounds;

		vws[ \view ] = view;
		vws[ \val ] = default;

		if( label.notNil ) {
			labelWidth = (RoundView.skin ? ()).labelWidth ? 80;
			vws[ \labelView ] = StaticText( vws[ \view ], labelWidth @ bounds.height )
				.string_( label.asString ++ " " )
				.align_( \right )
				.resize_( 4 )
				.applySkin( RoundView.skin );
		} {
			labelWidth = 0;
		};

		vws[ \objectLabel ] = StaticText( view,
			(bounds.width-(labelWidth + 2) - 42) @ (bounds.height)
	).applySkin( RoundView.skin ).background_( Color.white.alpha_(0.25) );

		vws[ \setLabel ] = {
			{ vws[ \objectLabel ].string = " " ++ vws[ \val ].asString; }.defer;
		};

		vws[ \setLabel ].value;

		vws[ \edit ] = SmoothButton( view, 40 @ (bounds.height) )
			.label_( "edit" )
			.radius_( 2 )
			.font_( font )
			.action_({
				var editor;
				if( vws[ \editor ].isNil or: { vws[ \editor ].view.isClosed } ) {
					editor = CodeEditView( bounds: 400 @ 200, object: vws[ \val ] )
						.action_({ |vw|
							var obj;
							obj = vw.object;
							if( obj.class == Function ) {
								if( obj.def.sourceCode
									.select({ |item|
										[ $ , $\n, $\r, $\t ].includes(item).not
									})
									.size > 2
								) {
									vws[ \val ] = obj;
									vws[ \setLabel ].value;
									action.value( vws,  vws[ \val ] );
								} {
									vws[ \val ] = nil;
									vw.object = {};
									vw.setCode( vw.object );
									vws[ \setLabel ].value;
									action.value( vws,  vws[ \val ] );
								};
							} {
								vws[ \val ] = obj;
								vws[ \setLabel ].value;
								action.value( vws,  vws[ \val ] );
							};
						})
						.failAction_({ |vw|
							vws[ \val ] = nil;
							vw.textView.string.postln;
							vw.object = {};
							vw.setCode( vw.object );
							vws[ \setLabel ].value;
							action.value( vws,  vws[ \val ] );
						});
					editor.view.onClose_({
						if( vws[ \editor ] == editor ) {
							vws[ \editor ] = nil;
						};
					});
					vws[ \editor ] = editor;
				} {
					vws[ \editor ].view.getParents.last.findWindow.front;
				};
			});

		view.view.onClose_({
			if( vws[ \editor ].notNil ) {
				vws[ \editor ].view.getParents.last.findWindow.close
			};
		});

		^vws;
	}

	setView { |view, value, active = false|
		view[ \val ] = value;
		view[ \setLabel ].value;
		if( view[ \editor ].notNil ) {
			view[ \editor ].object = view[ \val ] ?? {{}};
			view[ \editor ].setCode( view[ \editor ].object );
		};
	}

}

+ UEnvSpec {

	makeView { |parent, bounds, label, action, resize|
		var vws, view, labelWidth;
		var localStep;
		var modeFunc;
		var font;
		var editAction;
		var tempVal;
		var skin;
		vws = ();

		skin = RoundView.skin;
		font = skin.font ?? { Font( Font.defaultSansFace, 10 ); };

		bounds.isNil.if{bounds= 160@20};

		view = EZCompositeView( parent, bounds, gap: 4@4 );
		bounds = view.asView.bounds;

		vws[ \view ] = view;
		vws[ \val ] = Env();

		if( label.notNil ) {
			labelWidth = (RoundView.skin ? ()).labelWidth ? 80;
			vws[ \labelView ] = StaticText( vws[ \view ], labelWidth @ bounds.height )
				.string_( label.asString ++ " " )
				.align_( \right )
				.resize_( 4 )
				.applySkin( RoundView.skin );
		} {
			labelWidth = 0;
		};

		vws[ \edit ] = SmoothButton( view, 60 @ (bounds.height) )
		.label_( [ { |vw, bounds|
			var n = bounds.width.ceil.asInteger - 1, env = vws[ \val ];
			var dur = env.duration;

			Pen.color = Color.blue(1.0).alpha_(0.5);
			Pen.width = 1;

			Pen.moveTo( 1 @ (env[0].linlin(0,1,bounds.height - 1,1)) );
			(n-1).do({ |i|
				Pen.lineTo( (i+2) @ ( env[i.linlin(0,n-2,0,dur)].linlin(0,1,bounds.height - 1,1) ) );
			});
			Pen.stroke;
		} ] )
			.border_( 0 )
			.radius_( 0 )
			.background_( Color.white.alpha_(0.25) )
			.font_( font )
			.action_({
				var editor;
				if( vws[ \editor ].isNil or: { vws[ \editor ].isClosed } ) {
					RoundView.pushSkin( skin );
					editor = EnvView( "Envelope editor - "++label, env: vws[ \val ], spec: spec )
						.onClose_({
							if( vws[ \editor ] == editor ) {
								vws[ \editor ] = nil;
							};
						}).action_({ |vw|
					vws[ \val ] = vw.env;
					vws[ \update ].value;
					action.value( vws, vws[ \val ] );
				});
					RoundView.popSkin;
					vws[ \editor ] = editor;
				} {
					vws[ \editor ].front;
				};

			});

		vws[ \update ] = {
			vws[ \edit ].refresh;
			if( vws[ \editor ].notNil && {
				vws[ \editor ].env != vws[ \val ];
			}) {
				vws[ \editor ].env = vws[ \val ];
			};
		};

		view.view.onClose_({
			if( vws[ \editor ].notNil ) {
				vws[ \editor ].close;
			};
		});

		^vws;
	}

	setView { |view, value, active = false|
		view[ \val ] = value;
		view[ \update ].value;
	}

}

+ RealVector3DSpec {

    makeView { |parent, bounds, label, action, resize|
		var vws, view, labelWidth;
		var localStep;
		var startVal;
		vws = ();

		vws[ \val ] = RealVector3D[0,0,0];

		localStep = step.collect{ |x| if(x == 0 ){ 1 }{ x } };

		bounds.isNil.if{bounds= 160@20};

		#view, bounds = EZGui().prMakeMarginGap.prMakeView( parent, bounds );
		 vws[ \view ] = view;

		if( label.notNil ) {
			labelWidth = (RoundView.skin ? ()).labelWidth ? 80;
			vws[ \labelView ] = StaticText( vws[ \view ], labelWidth @ bounds.height )
				.string_( label.asString ++ " " )
				.align_( \right )
				.resize_( 4 )
				.applySkin( RoundView.skin );
		} {
			labelWidth = 0;
		};

		vws[ \coord ] = 3.collect{ |i|

            SmoothNumberBox( vws[ \view ], Rect( labelWidth + 2 + (i*42), 0, 40, bounds.height ) )
                .action_({ |nb|
                    vws[ \val ] = vws[ \coord ].collect(_.value).as(RealVector3D);
                    action.value( vws, vws[ \val ]);
                })
                //.step_( localStep[i] )
                .scroll_step_( localStep[i] )
                .clipLo_( nrect.sortedConstraints[i][0] )
                .clipHi_( nrect.sortedConstraints[i][1] )
                .value_(0);
         };

		^vws;
	}

	setView { |view, value, active = false|
		var constrained;
		constrained = this.constrain( value );
		[view[ \coord ], constrained].flopWith(_.value(_));
		view[ \val ] = constrained;
		if( active ) { view[ \x ].doAction };
	}

	mapSetView { |view, value, active = false|
		var mapped;
		mapped = this.map( value );
		[view[ \coord ], mapped].flopWith(_.value(_));
        view[ \val ] = mapped;
		if( active ) { view[ \x ].doAction };
	}

}

+ RectSpec {

	makeView { |parent, bounds, label, action, resize|
		var vws, view, labelWidth, val = 0@0, wh = 0@0;
		var localStep, setCenter, setWH;
		var font;
		vws = ();

		font = Font( Font.defaultSansFace, 10 );

		localStep = 0.01@0.01;

		vws[ \rect ] = this.default;

		setCenter = { |center|
			vws[ \rect ] = vws[ \rect ].center_( center );
		};

		setWH = { |whx|
			vws[ \rect ].centeredExtent_( whx );
		};

		bounds.isNil.if{bounds= 320@20};

		#view, bounds = EZGui().prMakeMarginGap.prMakeView( parent, bounds );
		 vws[ \view ] = view;

		view.addFlowLayout( 0@0, 2@2 );

		if( label.notNil ) {
			labelWidth = (RoundView.skin ? ()).labelWidth ? 80;
			vws[ \labelView ] = StaticText( vws[ \view ], labelWidth @ bounds.height )
				.string_( label.asString ++ " " )
				.align_( \right )
				.resize_( 4 )
				.applySkin( RoundView.skin );
		} {
			labelWidth = 0;
		};

		vws[ \centerLabel ] = StaticText( vws[ \view ], 12 @ bounds.height )
			.string_( "c" )
			.align_( \right )
			.font_( font )
			.applySkin( RoundView.skin );

		vws[ \x ] = SmoothNumberBox( vws[ \view ], 40 @ bounds.height )
			.action_({ |nb|
				setCenter.value( nb.value @ vws[ \y ].value );
				val = vws[ \rect ].center;
				action.value( vws, vws[ \rect ]);
			})
			//.step_( localStep.x )
			.scroll_step_( localStep.x )
			.clipLo_( rect.left )
			.clipHi_( rect.right )
			.value_(0);

		vws[ \xy ] = XYView( vws[ \view ],  bounds.height @ bounds.height )
			.action_({ |xy|
				vws[ \x ].value = (val.x + (xy.x * localStep.x))
					.clip( rect.left, rect.right );
				vws[ \y ].value = (val.y + (xy.y * localStep.y.neg))
					.clip( rect.top, rect.bottom );
				setCenter.value( val + (xy.value * localStep * (1 @ -1) ) );
				action.value( vws, vws[ \rect ] );
			})
			.mouseUpAction_({
				val = vws[ \x ].value @ vws[ \y ].value;
			});

		vws[ \y ] = SmoothNumberBox( vws[ \view ], 40 @ bounds.height )
			.action_({ |nb|
				setCenter.value( vws[ \x ].value @ nb.value );
				val = vws[ \rect ].center;
				action.value( vws, vws[ \rect ] );
			})
			//.step_( localStep.y )
			.scroll_step_( localStep.y )
			.clipLo_( rect.top )
			.clipHi_( rect.bottom )
			.value_(0);

		vws[ \whLabel ] = StaticText( vws[ \view ], 20 @ bounds.height )
			.string_( "w/h" )
			.align_( \right )
			.font_( font )
			.applySkin( RoundView.skin );

		vws[ \width ] =
			SmoothNumberBox( vws[ \view ], 40 @ bounds.height )
			.action_({ |nb|
				setWH.value( nb.value @ vws[ \height ].value );
				wh = vws[ \rect ].extent;
				action.value( vws, vws[ \rect ]);
			})
			//.step_( localStep.x )
			.scroll_step_( localStep.x )
			.clipLo_( 0)
			.clipHi_( rect.right )
			.value_(0);

		vws[ \wh ] = XYView( vws[ \view ], bounds.height @ bounds.height )
			.action_({ |xy|
				vws[ \width ].value = (wh.x + (xy.x * localStep.x))
					.clip( 0, rect.width );
				vws[ \height ].value = (wh.y + (xy.y * localStep.y.neg))
					.clip( 0, rect.height );
				setWH.value( wh + (xy.value * localStep * (1 @ -1) ) );
				action.value( vws, vws[ \rect ] );
			})
			.mouseUpAction_({
				wh = vws[ \width ].value @ vws[ \height ].value;
			});

		vws[ \height ] =
			SmoothNumberBox( vws[ \view ], Rect( labelWidth + 2, 0, 40, bounds.height ) )
			.action_({ |nb|
				setWH.value( vws[ \width ].value @ nb.value );
				wh = vws[ \rect ].extent;
				action.value( vws, vws[ \rect ]);
			})
			//.step_( localStep.x )
			.scroll_step_( localStep.x )
			.clipLo_( 0 )
			.clipHi_( rect.right )
			.value_(0);

		^vws;
	}

	setView { |view, value, active = false|
		var constrained;
		constrained = this.constrain( value );
		view[ \rect ] = constrained.copy;
		view[ \x ].value = constrained.center.x;
		view[ \y ].value = constrained.center.y;
		view[ \width ].value = constrained.width;
		view[ \height ].value = constrained.height;
		if( active ) { view[ \x ].doAction };
	}

	mapSetView { |view, value, active = false|
		var mapped;
		mapped = this.map( value );
		view[ \rect ] = mapped.copy;
		view[ \x ].value = mapped.center.x;
		view[ \y ].value = mapped.center.y;
		view[ \width ].value = mapped.width;
		view[ \height ].value = mapped.height;
		if( active ) { view[ \x ].doAction };
	}


}

+ DualValueSpec {

	makeView { |parent, bounds, label, action, resize|
		var vws = (), view, labelWidth, numWidth = 40, sliderWidth;
		var cspec, numberStep, round = 0.001, background, hiliteColor;

		cspec = this.asControlSpec;

		bounds.isNil.if{bounds= 320@20};

		#view, bounds = EZGui().prMakeMarginGap.prMakeView( parent, bounds );
		 vws[ \view ] = view;

		vws[ \val ] = [0,1];

		view.addFlowLayout( 0@0, 2@2 );

		numWidth = RoundView.skin.numberWidth ? 40;
		hiliteColor = RoundView.skin.hiliteColor ?? { Color.blue.alpha_(0.5) };

		if( label.notNil ) {
			labelWidth = (RoundView.skin ? ()).labelWidth ? 80;
			vws[ \labelView ] = StaticText( vws[ \view ], labelWidth @ bounds.height )
				.string_( label.asString ++ " " )
				.align_( \right )
				.resize_( 4 )
				.applySkin( RoundView.skin );
		} {
			labelWidth = 0;
		};

		sliderWidth = bounds.width - labelWidth - ((numWidth + 2) * 3) - 2;

		numberStep = this.guessNumberStep;

		vws[ \num1 ] = SmoothNumberBox( view, numWidth @ bounds.height )
		.step_( numberStep ).scroll_step_( numberStep )
		.action_({ |nb|
			vws[ \val ][0] = nb.value;
			vws[ \val ] = this.constrain( vws[ \val ] );
			vws.setVal;
			action.value( vws, vws[ \val ] );
		});
		vws[ \slider1 ] = SmoothSlider( view, sliderWidth @ (bounds.height / 2) )
		.centered_( true )
		.hiliteColor_( hiliteColor )
		.knobSize_(0.5)
		.action_({ |sl|
			vws[ \val ][0] = cspec.map( sl.value );
			vws.setVal;
			vws.doAction;
		});
		view.decorator.shift( sliderWidth.neg - 2, bounds.height / 2 );
		vws[ \slider2 ] = SmoothSlider( view, sliderWidth @ (bounds.height / 2) )
		.centered_( true )
		.knobSize_(0.5)
		.hiliteColor_( hiliteColor )
		.action_({ |sl|
			vws[ \val ][1] = cspec.map( sl.value );
			vws.setVal;
			vws.doAction;
		});
		view.decorator.shift( 0, bounds.height / -2 );
		vws[ \num2 ] = SmoothNumberBox( view, numWidth @ bounds.height )
		.step_( numberStep ).scroll_step_( numberStep )
		.action_({ |nb|
			vws[ \val ][1] = nb.value;
			vws[ \val ] = this.constrain( vws[ \val ] );
			vws.setVal;
			vws.doAction;
		});
		SmoothButton( view, numWidth @ bounds.height )
		.label_( "invert" )
		.radius_(2)
		.action_({
			vws[ \val ] = vws[ \val ].reverse;
			vws.setVal;
			vws.doAction;
		});

		vws[ \setVal ] = { |evt|
			evt[ \num1 ].value = evt.val[0].round( round );
			evt[ \num2 ].value = evt.val[1].round( round );
			evt[ \slider1 ].value = cspec.unmap( evt.val[0] );
			evt[ \slider2 ].value = cspec.unmap( evt.val[1] );
			evt[ \slider1 ].centerPos = evt[ \slider2 ].value;
			evt[ \slider2 ].centerPos = evt[ \slider1 ].value;
			if( evt.val[0] <= evt.val[1] ) {
				evt[ \slider1 ].hiliteColor = hiliteColor;
				evt[ \slider2 ].hiliteColor = hiliteColor;
			} {
				evt[ \slider1 ].hiliteColor = hiliteColor.complementary;
				evt[ \slider2 ].hiliteColor = hiliteColor.complementary;
			};
		};

		vws[ \doAction ] = { action.value( vws, vws[ \val ] ); }

		^vws;

	}

	setView { |vws, value, active = false|
		vws[ \val ] = value;
		vws.setVal;
		if( active ) { vws.doAction };
	}

	mapSetView { |vws, value, active = false|
		vws[ \val ] = this.map(value);
		vws.setVal;
		if( active ) { vws.doAction };
	}

	adaptFromObject { |object|
		if( object.isArray.not ) {
			^this.asControlSpec.adaptFromObject( object );
		} {
			if(  (object.minItem < minval) or: (object.maxItem > maxval) ) {
				^this.copy
					.minval_( minval.min( object.minItem ) )
					.maxval_( maxval.max( object.maxItem ) )
			};
			^this;
		};
	}

}

+ RangeSpec {

	makeView { |parent, bounds, label, action, resize|
		var vw = EZSmoothRanger( parent, bounds, label !? { label.asString ++ " " },
			this.asControlSpec,
			{ |sl| sl.value = this.constrain( sl.value ); action.value(sl, sl.value) },
			labelWidth: (RoundView.skin ? ()).labelWidth ? 80,
			numberWidth: RoundView.skin.numberWidth ? 40,
			).value_( this.default );
		if( warp.isKindOf( ExponentialWarp ) ) {
			[ vw.loBox, vw.hiBox ].do({ |box|
				box.allowedChars = "+-.AaBbCcDdEeFfGgMm#*/()%";
				box.interpretFunc = { |string|
					var cents = 0, splits;
					case { "AaBbCcDdEeFfGg".includes(string.first) } {
						if( string.indexOf( $+ ).notNil ) {
							cents = string.split( $+ ).last.interpret;
						} {
							splits = string.split($-);
							if( splits.size > 1 ) {
								if( splits[ splits.size-2 ].last.isDecDigit ) {
									cents = splits.last.interpret.neg;
								};
							};
						};
						string.namecps * (cents / 100).midiratio;
					} { "Mm".includes(string.first) } {
						string[1..].interpret.midicps;
					} {
						string.interpret;
					};
				};
			})
		};
		// later incorporate rangeSpec into EZSmoothRanger
		if( resize.notNil ) { vw.view.resize = resize };
		^vw;
	}

	setView { |vws, value, active = false|
		vws.value = value;
		if( active ) { vws.doAction };
	}

	mapSetView { |vws, value, active = false|
		vws.value = this.map(value);
		if( active ) { vws.doAction };
	}

	adaptFromObject { |object|
		if( object.isArray.not ) {
			^this.asControlSpec.adaptFromObject( object );
		} {
			if(  (object.minItem < minval) or: (object.maxItem > maxval) ) {
				^this.copy
					.minval_( minval.min( object.minItem ) )
					.maxval_( maxval.max( object.maxItem ) )
			};
			^this;
		};
	}

}

+ ColorSpec {

	viewNumLines { ^9 }

	makeView { |parent, bounds, label, action, resize|
		var vws, view, labelWidth;
		var localStep;
		var modeFunc;
		var font;
		var editAction;
		var tempVal;
		var viewHeight, viewWidth;
		vws = ();

		font =  (RoundView.skin ? ()).font ?? { Font( Font.defaultSansFace, 10 ); };

		bounds.isNil.if{bounds= 372@200};

		view = EZCompositeView( parent, bounds, gap: 4@4 );
		bounds = view.asView.bounds;
		view.asView.resize_( resize ? 2 );

		vws[ \view ] = view;
		vws[ \val ] = this.default;

		if( label.notNil ) {
			labelWidth = (RoundView.skin ? ()).labelWidth ? 80;
			vws[ \labelView ] = StaticText( vws[ \view ], labelWidth @ bounds.height )
				.string_( label.asString ++ " " )
				.align_( \right )
				.resize_( 4 )
				.applySkin( RoundView.skin );
		} {
			labelWidth = 0;
		};

		viewHeight = (bounds.height / 9) - 4;
		viewWidth = bounds.width - (labelWidth + 8);

		vws[ \colorView ] = UserView( view, viewWidth @ viewHeight )
			.drawFunc_({ |vw|
				var rect;
				rect = vw.drawBounds;
				Pen.color = Color.black;

				Pen.line( (rect.right * 2/5) @ (rect.top), rect.rightBottom );
				Pen.lineTo( rect.rightTop );
				Pen.lineTo( (rect.right * 2/5) @ (rect.top) );

				Pen.fill;
				Pen.color = Color.white;
				Pen.line( rect.leftTop, (rect.right * 3/5) @ (rect.bottom));
				Pen.lineTo( rect.leftBottom );
				Pen.lineTo( rect.leftTop );
				Pen.fill;

				Pen.color = vws[ \val ];
				Pen.fillRect( vw.drawBounds );
			})
			.canReceiveDragHandler_({
				var obj;
				obj = View.currentDrag;
				if( obj.class == String ) {
					obj = { obj.interpret }.try;
				};
				obj.respondsTo( \asColor );
			})
			.receiveDragHandler_({
				var obj;
				if( View.currentDrag.class == String ) {
					obj = View.currentDrag.interpret.asColor;
				} {
					obj = View.currentDrag.asColor;
				};
				if( obj.notNil ) { vws[ \val ] = obj };
				vws[ \updateViews ].value;
				action.value( vws, vws[ \val ] );
			})
			.beginDragAction_({ vws[ \val ] })
			.resize_(2);

		vws[ \h ] = EZSmoothSlider(view, viewWidth @ viewHeight, "hue" ).value_(0);
		vws[ \s ] = EZSmoothSlider(view, viewWidth @ viewHeight, "saturation" ).value_(0);
		vws[ \v ] = EZSmoothSlider(view, viewWidth @ viewHeight, "value" ).value_(0.5);
		vws[ \r ] = EZSmoothSlider(view, viewWidth @ viewHeight, "red" ).value_(0.5);
		vws[ \g ] = EZSmoothSlider(view, viewWidth @ viewHeight, "green" ).value_(0.5);
		vws[ \b ] = EZSmoothSlider(view, viewWidth @ viewHeight, "blue" ).value_(0.5);
		vws[ \a ] = EZSmoothSlider(view, viewWidth @ viewHeight, "alpha" ).value_(1);

		[\h,\s,\v,\r,\g,\b,\a].collect(vws[_]).do({ |item|
			item.sliderView.hiliteColor = nil;
			item.view.resize_(2);
		});

		vws[ \h ].sliderView.background = { |bounds|
			var left, right, bottom, height, sat, val, res = 1;
			left = bounds.left;
			right = bounds.right;
			bottom = bounds.bottom;
			height = bounds.height;
			sat = vws[ \val ].sat;
			val = vws[ \val ].val;
			Pen.width = res;
			((height/res).ceil+1).do({ |i|
				i = i*res;
				Pen.color = Color.hsv( (i / height).min( 0.9999999999999999 ), sat, val );
				Pen.line( left @ (bottom - i), right @ (bottom - i) ).stroke;
			});
		};

		vws[ \updateViews ] = {

			vws[ \s ].sliderView.background = Gradient(
					vws[ \val ].copy.sat_(1).alpha_(1),
					vws[ \val ].copy.sat_(0).alpha_(1), \v
				);
			vws[ \v ].sliderView.background = Gradient(
					vws[ \val ].copy.val_(1).alpha_(1),
					vws[ \val ].copy.val_(0).alpha_(1), \v
				);
			vws[ \r ].sliderView.background = Gradient(
					vws[ \val ].copy.red_(1).alpha_(1),
					vws[ \val ].copy.red_(0).alpha_(1), \v
				);
			vws[ \g ].sliderView.background = Gradient(
					vws[ \val ].copy.green_(1).alpha_(1),
					vws[ \val ].copy.green_(0).alpha_(1), \v
				);
			vws[ \b ].sliderView.background = Gradient(
				vws[ \val ].copy.blue_(1).alpha_(1),
				vws[ \val ].copy.blue_(0).alpha_(1), \v
				);
			vws[ \a ].sliderView.background = Gradient(
				vws[ \val ].copy.alpha_(1), vws[ \val ].copy.alpha_(0), \v
			);

			vws[ \h ].value = vws[ \val ].hue;
			vws[ \s ].value = vws[ \val ].sat;
			vws[ \v ].value = vws[ \val ].val;
			vws[ \r ].value = vws[ \val ].red;
			vws[ \g ].value = vws[ \val ].green;
			vws[ \b ].value = vws[ \val ].blue;
			vws[ \a ].value = vws[ \val ].alpha;
			{ vws[ \colorView ].refresh }.defer;
		};

		vws[ \updateViews ].value;

		editAction = { |perform = \red_ |
			{ |sl|
				vws[ \val ].perform( perform, sl.value );
				vws[ \updateViews ].value;
				action.value( vws, vws[ \val ] );
			};
		};

		// vws[ \h ].action = editAction.( \hue_ );
		vws[ \s ].action = editAction.( \sat_ );
		vws[ \v ].action = editAction.( \val_ );
		vws[ \r ].action = editAction.( \red_ );
		vws[ \g ].action = editAction.( \green_ );
		vws[ \b ].action = editAction.( \blue_ );
		vws[ \a ].action = editAction.( \alpha_ );

		vws[ \h ].action = { |sl|
			vws[ \val ].hue_( sl.value.min( 0.9999999999999999 ) );
			vws[ \updateViews ].value;
			action.value( vws, vws[ \val ] );
		};

		vws[ \presetManager ] = PresetManagerGUI(
			view, viewWidth @ viewHeight, presetManager, vws[ \val ]
		).action_({ |pm|
			vws[ \val ] = pm.object;
			vws[ \updateViews ].value;
			action.value( vws, vws[ \val ] );
		})
		.resize_( 2 );

		^vws;
	}

	setView { |view, value, active = false|
		view[ \val ] = value;
		view[ \updateViews ].value;
	}


}

+ ColorArraySpec {

	viewNumLines { ^9 }

	makeView { |parent, bounds, label, action, resize|
		var vws, view, labelWidth;
		var localStep;
		var modeFunc;
		var font;
		var editAction;
		var tempVal;
		var viewHeight, viewWidth;
		vws = ();

		font =  (RoundView.skin ? ()).font ?? { Font( Font.defaultSansFace, 10 ); };

		bounds.isNil.if{bounds= 372@200};

		view = EZCompositeView( parent, bounds, gap: 4@4 );
		bounds = view.asView.bounds;
		view.asView.resize_(resize ? 5);

		vws[ \view ] = view;
		vws[ \val ] = this.default;

		if( label.notNil ) {
			labelWidth = (RoundView.skin ? ()).labelWidth ? 80;
			vws[ \labelView ] = StaticText( vws[ \view ], labelWidth @ bounds.height )
				.string_( label.asString ++ " " )
				.align_( \right )
				.resize_( 4 )
				.applySkin( RoundView.skin );
		} {
			labelWidth = 0;
		};

		viewHeight = (bounds.height / 9) - 4;
		viewWidth = bounds.width - (labelWidth + 8);

		vws[ \colorView ] = UserView( view, viewWidth @ viewHeight )
		.drawFunc_({ |vw|
			var rect, smallRect;
			rect = vw.drawBounds;
			Pen.color = Color.black;

			Pen.line( (rect.right * 2/5) @ (rect.top), rect.rightBottom );
			Pen.lineTo( rect.rightTop );
			Pen.lineTo( (rect.right * 2/5) @ (rect.top) );

			Pen.fill;
			Pen.color = Color.white;
			Pen.line( rect.leftTop, (rect.right * 3/5) @ (rect.bottom));
			Pen.lineTo( rect.leftBottom );
			Pen.lineTo( rect.leftTop );
			Pen.fill;

			smallRect = rect.copy;
			smallRect.width = (rect.width / this.size) + 1;

			vws[ \val ].do({ |color, i|
				Pen.color = color;
				Pen.fillRect( smallRect.moveBy( i * (smallRect.width - 1), 0 )  );
			});
		})
		.resize_(2);

		vws[ \specs ] = ();

		editAction = { |perform = \red_ |
			{ |vw, val|
				vws[ \val ].do({ |item, i|
					item.perform( perform, val[i] );
				});
				vws[ \updateViews ].value;
				action.value( vws, vws[ \val ] );
			};
		};

		vws[ \actions ] = ();
		vws[ \actions ][ \s ] = editAction.( \sat_ );
		vws[ \actions ][ \v ]= editAction.( \val_ );
		vws[ \actions ][ \r ] = editAction.( \red_ );
		vws[ \actions ][ \g ] = editAction.( \green_ );
		vws[ \actions ][ \b ] = editAction.( \blue_ );
		vws[ \actions ][ \a ] = editAction.( \alpha_ );
		vws[ \actions ][ \h ] = { |vw, val|
			vws[ \val ].do({ |item, i|
				item.hue_( val[i].min( 0.9999999999999999 ) );
			});
			vws[ \updateViews ].value;
			action.value( vws, vws[ \val ] );
		};

		[
			[ \h, 0, "hue"],
			[ \s, 0, "saturation" ],
			[ \v, 0, "value" ],
			[ \r, 0, "red" ],
			[ \g, 0, "green" ],
			[ \b, 0, "blue" ],
			[ \a, 1, "alpha" ]
		].collect({ |item,i|
			vws[ \specs ][ item[0] ] = ArrayControlSpec( 0, 1, \lin, 0, item[1] ! (this.size) );
			vws[ item[0] ] = vws[ \specs ][ item[0] ]
			.makeView( view, viewWidth @ viewHeight, item[2], vws[ \actions ][ item[0] ], 2 );
		});

		vws[ \updateViews ] = {
			var setView;

			setView = { |which = \h, val|
				vws[ \specs ][ which ].setView( vws[ which ], val );
			};

			setView.value( \h, vws[ \val ].collect(_.hue) );
			setView.value( \s, vws[ \val ].collect(_.sat) );
			setView.value( \v, vws[ \val ].collect(_.val) );
			setView.value( \r, vws[ \val ].collect(_.red) );
			setView.value( \g, vws[ \val ].collect(_.green) );
			setView.value( \b, vws[ \val ].collect(_.blue) );
			setView.value( \a, vws[ \val ].collect(_.alpha) );

			{ vws[ \colorView ].refresh }.defer;
		};

		vws[ \updateViews ].value;

		^vws;
	}

	setView { |view, value, active = false|
		view[ \val ] = value;
		view[ \updateViews ].value;
	}


}

+ BufSndFileSpec {

	viewNumLines { ^BufSndFileView.viewNumLines }

	viewClass { ^BufSndFileView }

	makeView { |parent, bounds, label, action, resize|
		var vws, view, labelWidth;

		vws = ();

		bounds.isNil.if{bounds= 350 @ (this.viewNumLines * 18) };

		#view, bounds = EZGui().prMakeMarginGap.prMakeView( parent, bounds );
		 vws[ \view ] = view;
		 view.addFlowLayout(0@0, 2@2);
		if( label.notNil ) {
			labelWidth = (RoundView.skin ? ()).labelWidth ? 80;
			vws[ \labelView ] = StaticText( vws[ \view ], labelWidth @ 14 )
				.string_( label.asString ++ " " )
				.align_( \right )
				.resize_( 4 )
				.applySkin( RoundView.skin );
		} {
			labelWidth = -4;
		};

		if( resize.notNil ) { vws[ \view ].resize = resize };

		vws[ \sndFileView ] = this.viewClass.new( vws[ \view ],
			( bounds.width - (labelWidth+2) ) @ bounds.height, { |vw|
				action.value( vw, vw.value )
			} )

		^vws;
	}

	setView { |view, value, active = false|
		view[ \sndFileView ].value = value;
		if( active ) { view.doAction };
	}
}

+ DiskSndFileSpec {
	viewClass { ^DiskSndFileView }
}

+ RichBufferSpec {

	viewNumLines { ^if( editMode.notNil ) { 1 } { 0 } }

	makeView { |parent, bounds, label, action, resize|
		var vws, view, labelWidth;

		bounds.isNil.if{bounds= 160 @ 18 };

		switch( editMode,
			\duration, {
				vws = ();

				#view, bounds = EZGui().prMakeMarginGap.prMakeView( parent, bounds );
				vws[ \view ] = view;
				vws[ \val ] = this.default.copy;

				if( label.notNil ) {
					labelWidth = (RoundView.skin ? ()).labelWidth ? 80;
					vws[ \labelView ] = StaticText( vws[ \view ], labelWidth @ 14 )
						.string_( label.asString ++ " " )
						.align_( \right )
						.resize_( 4 )
						.applySkin( RoundView.skin );
				} {
					labelWidth = -2;
				};

				vws[ \box ] = SMPTEBox( vws[ \view ],
						Rect(labelWidth + 2,0,bounds.width-(labelWidth + 2),bounds.height)
					)
					.applySmoothSkin
				    .action_({ |vw|
					    var sampleRate;
					    if( useServerSampleRate ) {
						    sampleRate = ULib.servers.first !? _.sampleRate ? 44100;
					    } {
						    sampleRate = vws[ \val ].sampleRate;
					    };
					    vws[ \val ].numFrames = (vw.value * sampleRate).asInteger;
				        action.value( vw, vws[ \val ] );
				    } ).resize_(5)
				    .fps_( 1000 )
					.clipLo_( 128 / 44100 )
					.clipHi_( 60 * 60 );

				vws[ \updateViews ] = {
					 var sampleRate;
					 if( useServerSampleRate ) {
						 sampleRate = ULib.servers.first !? _.sampleRate ? 44100;
					 } {
						 sampleRate = vws[ \val ].sampleRate;
					 };
					vws[ \box ].value = vws[ \val ].numFrames / sampleRate;
				};

				vws[ \updateViews ].value;
				if( resize.notNil ) { vws[ \view ].resize = resize };

			},
		);
		^vws;
	}

	setView { |view, value, active = false|
		view[ \val ] = value;
		view[ \updateViews ].value;
		if( active ) { view[ \box ].doAction };
	}
}

+ MultiSndFileSpec {

	viewNumLines { ^4 }

	makeView { |parent, bounds, label, action, resize, hasEdit = true|
		var vws, view, labelWidth;
		var localStep;
		var font;
		var editAction;
		var loopSpec, rateSpec;
		var viewHeight;
		vws = ();

		font =  (RoundView.skin ? ()).font ?? { Font( Font.defaultSansFace, 10 ); };

		bounds.isNil.if{bounds= 350 @ (this.viewNumLines * 18) };

		viewHeight = 14;

		view = EZCompositeView( parent, bounds, gap: 4@4 );
		bounds = view.asView.bounds;

		vws[ \view ] = view;

		vws[ \val ] = this.default ? [];

		loopSpec = BoolSpec(true).trueLabel_("loop").falseLabel_("loop")
		.massEditSpec( vws[ \val ].collect(_.loop) );
		rateSpec = [-96,96].asSpec.massEditSpec( vws[ \val ].collect({|x| x.rate.ratiomidi }) );

		if( label.notNil ) {
			labelWidth = (RoundView.skin ? ()).labelWidth ? 80;
			vws[ \labelView ] = StaticText( vws[ \view ], labelWidth @ viewHeight )
				.string_( label.asString ++ " " )
				.align_( \right )
				.resize_( 4 )
				.applySkin( RoundView.skin );
		} {
			labelWidth = 0;
		};

		vws[ \path ] = MultiFilePathView( view, (view.bounds.width - labelWidth - 4) @ viewHeight );
		vws[ \path ].fixedSize = fixedAmount;

		if( fixedAmount ) {
			vws[ \path ].action = { |vw|
				vws[ \val ].do({ |item, i|
					item.path = vw.value[i];
					item.fromFile;
			    });
			};
		} {
			vws[ \path ].action = { |vw|
				if( vws[ \val ].size != vw.value.size ) {
					action.value( vws, vw.value.collect({ |path| sndFileClass.new( path ) }) );
				} {
					vws[ \val ].do({ |item, i|
						item.path = vw.value[i];
						item.fromFile;
					});
				};
			};
		};
		view.view.decorator.nextLine;
		view.view.decorator.shift( labelWidth, 0 );

		vws[ \amount ] = StaticText( view, 60 @ viewHeight )
			.applySkin( RoundView.skin )
			.font_( font );

		if( fixedAmount ) {
			vws[ \amount ].string = " % files".format( default.size );
		} {
			vws[ \setAmount ] = { |vws, value|
				{ vws[ \amount ].string = " % files".format( value.size ); }.defer;
			};
		};

		if( sndFileClass != DiskSndFileSpec ) {
			view.view.decorator.left = view.bounds.width - 84;

			vws[ \global ] = SmoothButton( view, 40 @ viewHeight )
				.label_( ["global", "global" ] )
				.radius_( 2 )
				.font_( font )
				.action_({ |bt|
					switch( bt.value,
						1, { vws[ \val ].do(_.loadGlobal) },
						0, { vws[ \val ].do(_.disposeGlobal) },
					);
				});

			vws[ \setGlobal ] = { |evt, value|
				if( value.every(_.hasGlobal) ) {
					vws[ \global ].value = 1;
				} {
					vws[ \global ].value = 0;
				};
			};
		};

		if( hasEdit ) {
			view.view.decorator.left = view.bounds.width - 40;

			vws[ \edit ] = SmoothButton( view, 40 @ viewHeight )
			.label_( "edit" )
			.radius_( 2 )
			.action_({
				this.makeEditWindow( vws, vws[ \val ], label, { |vals|
					this.setView( vws, vals, true );
				}, BufSndFileSpec(nil) )
			});
		};

		view.view.decorator.nextLine;
		view.view.decorator.shift( labelWidth, 0 );

		RoundView.pushSkin( RoundView.skin ++ (labelWidth: 30) );

		vws[ \rate ] = rateSpec.makeView( view, (view.bounds.width - labelWidth) @ viewHeight,
			" rate", { |vw, val|
				var size;
				vws[ \updateRate ] = false;
				size = val.size - 1;
				val.do({ |item, i|
					if( i == size ) { vws[ \updateRate ] = true };
					vws[ \val ][ i ].rate = item.midiratio;
				})
			}, 2 );

		vws[ \rate ].labelView.align_( \left );

		vws[ \setRate ] = { |evt, value|
			if( evt.updateRate != false ) {
				rateSpec.setView( evt[ \rate ], value.collect({|x| x.rate.ratiomidi }) );
			};
		};

		view.view.decorator.nextLine;
		view.view.decorator.shift( labelWidth, 0 );

		vws[ \loop ] = loopSpec.makeView( view, (view.bounds.width - labelWidth ) @ viewHeight,
			" loop", { |vw, val|
				var size;
				vws[ \updateLoop ] = false;
				size = val.size - 1;
				val.do({ |item, i|
					if( i == size ) { vws[ \updateLoop ] = true };
					vws[ \val ][ i ].loop = item;
				});
			}, 2 );

		vws[ \loop ].labelView.align_( \left );

		vws[ \setLoop ] = { |evt, value|
			if( evt.updateLoop != false ) {
				loopSpec.setView( evt[ \loop ], value.collect(_.loop) );
			};
		};

		view.view.onClose_({
			vws[ \editWin ] !? _.close;
		});

		RoundView.popSkin;

		^vws;
	}

	setView { |view, value, active = false|
		view[ \val ] = value;
		view[ \path ].value = value.collect( _.path );
		view.setLoop( value );
		view.setRate( value );
		view.setGlobal( value );
		view.setAmount( value );
	}
}

+ PartConvBufferSpec {

	viewNumLines { ^PartConvBufferView.viewNumLines }

	viewClass { ^PartConvBufferView }

	makeView { |parent, bounds, label, action, resize|
		var vws, view, labelWidth;

		vws = ();

		bounds.isNil.if{bounds= 350 @ (this.viewNumLines * 18) };

		#view, bounds = EZGui().prMakeMarginGap.prMakeView( parent, bounds );
		 vws[ \view ] = view;
		 view.addFlowLayout(0@0, 4@4);
		if( label.notNil ) {
			labelWidth = (RoundView.skin ? ()).labelWidth ? 80;
			vws[ \labelView ] = StaticText( vws[ \view ], labelWidth @ 14 )
				.string_( label.asString ++ " " )
				.align_( \right )
				.resize_( 4 )
				.applySkin( RoundView.skin );
		} {
			labelWidth = -4;
		};

		if( resize.notNil ) { vws[ \view ].resize = resize };

		vws[ \bufferView ] = this.viewClass.new( vws[ \view ],
			( bounds.width - (labelWidth+4) ) @ bounds.height, { |vw|
				action.value( vw, vw.value )
			} )

		^vws;
	}

	setView { |view, value, active = false|
		view[ \bufferView ].value = value;
		if( active ) { view.doAction };
	}
}


+ MultiPartConvBufferSpec {

	viewNumLines { ^2 }

	makeView { |parent, bounds, label, action, resize|
		var vws, view, labelWidth;
		var localStep;
		var font;
		var editAction;
		var viewHeight;
		var currentSkin;
		vws = ();

		font =  (RoundView.skin ? ()).font ?? { Font( Font.defaultSansFace, 10 ); };

		currentSkin = RoundView.skin;

		bounds.isNil.if{bounds= 350 @ (this.viewNumLines * 18) };

		viewHeight = (bounds.height / this.viewNumLines).floor - 2;

		view = EZCompositeView( parent, bounds, gap: 4@4 );
		bounds = view.asView.bounds;

		vws[ \view ] = view;

		vws[ \val ] = this.default ? [];

		if( label.notNil ) {
			labelWidth = (RoundView.skin ? ()).labelWidth ? 80;
			vws[ \labelView ] = StaticText( vws[ \view ], labelWidth @ viewHeight )
				.string_( label.asString ++ " " )
				.align_( \right )
				.resize_( 4 )
				.applySkin( RoundView.skin );
		} {
			labelWidth = 0;
		};


		editAction = { |vw|
			vws[ \val ] = vw.object;
			action.value( vws, vws[ \val ] );
		};

		vws[ \path ] = MultiFilePathView( view, (view.bounds.width - labelWidth - 4) @ viewHeight );
		vws[ \path ].fixedSize = fixedAmount;

		vws[ \path ].preProcessPathsFunc = { |fv, paths, action|
			if( paths.notNil && { paths.any({ |pth| pth.extension.toLower != "partconv"  }) }) {
				SCAlert( "One or move files appear not to be a .partconv file\ndo you want to convert them?",
					[ "use anyway", "convert" ],
					[{ action.value( paths ) }, {
						{
							var cond = Condition( false );
							var res;
							paths.do({ |pth|
								if( pth.extension.toLower != "partconv" ) {
									cond.test = false;
									PartConvBuffer.convertIRFileMulti( pth,
										server: ULib.servers,
										action: { |paths|
											res = res.addAll( paths );
											cond.test = true;
											cond.signal;
										}
									);
									cond.wait;
								} {
									res = res.add( pth );
								};
							});
							action.value( res );
						}.fork;
					}]
				);
			} {
				action.value( paths )
			};
		};

		if( fixedAmount ) {
			vws[ \path ].action = { |vw|
				vws[ \val ].do({ |item, i|
					item.path = vw.value[i];
					item.fromFile;
			    });
			};
		} {
			vws[ \path ].action = { |vw|
				if( vws[ \val ].size != vw.value.size ) {
					action.value( vws, vw.value.collect({ |path| PartConvBuffer.new( path ) }) );
				} {
					vws[ \val ].do({ |item, i|
						item.path = vw.value[i];
						item.fromFile;
					});
				};
			};
		};
		view.view.decorator.nextLine;
		view.view.decorator.shift( labelWidth, 0 );

		vws[ \amount ] = StaticText( view, (bounds.width - 44 - 64 - 64 - labelWidth) @ viewHeight )
			.applySkin( RoundView.skin )
			.font_( font );

		if( fixedAmount ) {
			vws[ \amount ].string = " % files".format( default.size );
		} {
			vws[ \setAmount ] = { |vws, value|
				{ vws[ \amount ].string = " % files".format( value.size ); }.defer;
			};
		};

		vws[ \import ] = StaticText( view, 60 @ viewHeight );

		if( PartConvBufferView.canImport ) {
			vws[ \import ]
			.applySkin( RoundView.skin )
			.string_( "import" )
			.align_( \center )
			.background_( Color.white.alpha_(0.25) )
			.mouseDownAction_({
				vws[ \importMenu ] !? _.deepDestroy;
				vws[ \importMenu ] = PartConvBufferView.makeImportMenu({ |path|
					var savePath;
					savePath = (ULib.lastPath ? "~/").standardizePath.withoutTrailingSlash
					+/+ path.basename.replaceExtension( "partconv" );
					ULib.savePanel({ |pth|
						PartConvBuffer.convertIRFileMulti( path, pth.replaceExtension( "partconv" ),
							server: ULib.servers,
							action: { |paths|
								//views[ \path ].value = paths[0]; views[ \path ].doAction
								vws[ \val ] = vws[ \val ].collect({ |item, i|
									if( paths[i].notNil ) {
										PartConvBuffer.new( paths[i] )
									} {
										item
									}
								});
								action.value( vws, vws[ \val ] );
							}
						);
					}, path: savePath )
				})
			})
			.onClose_({ vws[ \importMenu ] !? _.deepDestroy; })
		};


		vws[ \danStowel ] = SmoothButton( view, 60 @ viewHeight )
		.radius_( 2 )
		.resize_( 3 )
		.label_( "generate" )
		.action_({
			var closeFunc;
			// generate danstowell
			if( vws[ \genWindow ].isNil or: { vws[ \genWindow ].isClosed } ) {
				vws[ \genWindow ] = Window( "danstowell (%)".format( vws[\val].size ), Rect(592, 534, 294, 102) ).front;
				vws[ \genWindow ].addFlowLayout;

				RoundView.pushSkin( currentSkin );

				StaticText( vws[ \genWindow ], 50@18 ).string_( "duration" ).applySkin( RoundView.skin );
				vws[ \genDur ] = SMPTEBox( vws[ \genWindow ], 80@18 )
				.value_(1.3)
				.applySmoothSkin;
				SmoothButton( vws[ \genWindow ], 80@18 )
				.extrude_(false)
				.label_( [ "generate" ] )
				.action_({
					Dialog.savePanel({ |path|
						{ vws[ \genWindow ].name = "danstowel (generating)"; }.defer;
						path = path.removeExtension;
						PartConvBuffer.convertIRFileMulti(
							vws[ \val ].size.collect({
								PartConvBuffer.generateDanStowelIR( vws[ \genDur ].value )
							}),
							vws[ \val ].size.collect({ |i|
								path ++ "_%.partconv".format(i);
							}),
							ULib.servers,
							{ |paths|
								vws[ \val ] = paths.collect({ |pth| PartConvBuffer.new( pth ) });
								action.value( vws, vws[ \val ] );
								{ closeFunc.value; }.defer;
							}
						);
					});
				});

				RoundView.popSkin;

				closeFunc = { vws[ \genWindow ] !? (_.close); };

				vws[ \danStowel ].onClose = vws[ \danStowel ].onClose.addFunc( closeFunc );

				vws[ \genWindow ].onClose = {
					vws[ \danStowel ].onClose.removeFunc( closeFunc );
					vws[ \genWindow ] = nil;
				};
			} {
				vws[ \genWindow ].front;
			};

		});


		^vws;
	}

	setView { |view, value, active = false|
		view[ \val ] = value;
		view[ \path ].value = value.collect( _.path );
		view.setAmount( value );
	}
}

+ IntegerSpec {

	makeView { |parent, bounds, label, action, resize|
		var vws, view, labelWidth;

		bounds.isNil.if{bounds= 160 @ 18 };

		vws = ();
		#view, bounds = EZGui().prMakeMarginGap.prMakeView( parent, bounds );
		vws[ \view ] = view;

		if( label.notNil ) {
			labelWidth = (RoundView.skin ? ()).labelWidth ? 80;
			vws[ \labelView ] = StaticText( vws[ \view ], labelWidth @ 14 )
				.string_( label.asString ++ " " )
				.align_( \right )
				.resize_( 4 )
				.applySkin( RoundView.skin );
		} {
			labelWidth = -4;
		};

		vws[ \box ] = SmoothNumberBox( vws[ \view ],
				Rect(labelWidth + 2,0,bounds.width-(labelWidth + 2),bounds.height)
			)
		    .action_({ |vw|
		        action.value( vw, vw.value.asInteger );
		    } ).resize_(5)
		    .allowedChars_( "-" )
			.step_( step )
			.scroll_step_( step )
			.alt_scale_( alt_step / step )
			.clipLo_( this.minval )
			.clipHi_( this.maxval );

		if( resize.notNil ) { vws[ \view ].resize = resize };
		^vws;
	}

	setView { |view, value, active = false|
		view[ \box ].value = value;
		if( active ) { view.doAction };
	}

}

+ HardwareBusSpec {

	makeMenu { |labels, list, action|
		var menu, menuActions = [];
		labels = labels ?? { this.getDeviceLabels };
		list = list ?? { this.class.makeDeviceLabelsList( labels ) };

		labels.do({ |dev|
			var ma;
			case { dev.isKindOf( Symbol ) } {
				menuActions = menuActions.add(
					MenuAction( dev.asString, {
						action.value( list.indexOf( dev ) );
					})
				);
			} { dev[1].size < 3 } {
				dev[1].do({ |id|
					menuActions = menuActions.add(
						MenuAction( dev[0].asString + id, {
							action.value( list.indexOf( (dev[0].asString + id).asSymbol ) );
						})
					);
				});
			} { dev[1].size < 32 } {
				ma = Menu().title_( dev[0].asString );
				dev[1].do({ |id|
					ma.addAction(
						MenuAction( dev[0].asString + id, {
							action.value( list.indexOf( (dev[0].asString + id).asSymbol ) );
						})
					);
				});
				menuActions = menuActions.add( ma );
			} {
				ma = Menu().title_( dev[0].asString );
				dev[1].clump(16).do({ |ids, i|
					var mai = Menu().title_( dev[0].asString + "%-%".format( ids.first, ids.last ) );
					ids.do({ |id|
						mai.addAction(
							MenuAction( dev[0].asString + id, {
								action.value( list.indexOf( (dev[0].asString + id).asSymbol ) );
							})
						);
					});
					ma.addAction( mai );
				});
				menuActions = menuActions.add( ma );
			};
		});
		if( menuActions.size == 1 && { menuActions[0].isKindOf( Menu ) }) {
			^menuActions[0];
		} {
			^Menu( *menuActions );
		};
	}

	makeView { |parent, bounds, label, action, resize|
		var vws, view, labelWidth;
		var boxWidth, setColor;

		bounds.isNil.if{bounds= 160 @ 18 };

		vws = ();
		#view, bounds = EZGui().prMakeMarginGap.prMakeView( parent, bounds );
		vws[ \view ] = view;
		vws[ \labels ] = this.getDeviceLabels;
		vws[ \list ] = this.class.makeDeviceLabelsList( vws[ \labels ] );

		vws[ \doAction ] = {
			action.value( vws, vws[ \box ].value.asInteger );
		};

		vws[ \setLabel ] = {
			var index, labels, lastName;
			index = vws[ \box ].value;
			if( numChannels == 1 ) {
				vws[ \label ].string = " " ++ vws[ \list ][ index.asInteger ] ? "";
			} {
				labels = [ vws[ \list ][ index.asInteger ],  vws[ \list ][ index.asInteger + (numChannels-1) ] ]
				.collect(_.asString);
				if( labels[1].find( labels[0].split($ )[0] ).notNil ) {
					vws[ \label ].string = " " ++ labels[0] ++ " - " ++ labels[1].split($ ).last;
				} {
					vws[ \label ].string = " " ++ labels.join( "-" );
				};
			};
		};

		if( label.notNil ) {
			labelWidth = (RoundView.skin ? ()).labelWidth ? 80;
			vws[ \labelView ] = StaticText( vws[ \view ], labelWidth @ 14 )
				.string_( label.asString ++ " " )
				.align_( \right )
				.resize_( 4 )
				.applySkin( RoundView.skin );
		} {
			labelWidth = -4;
		};

		//boxWidth = bounds.width-(labelWidth + 2 + 40 + 2);
		boxWidth = 45;

		vws[ \box ] = SmoothNumberBox( vws[ \view ],
				Rect(labelWidth + 2, 0, boxWidth, bounds.height)
			)
		    .action_({ |vw|
		        vws.setLabel;
			    vws.doAction;
		    } )
		    .allowedChars_( "" )
			.step_( step )
			.scroll_step_( step )
			.alt_scale_( alt_step / step )
			.clipLo_( this.minval )
			.clipHi_( this.maxval );

		vws[ \label ] = StaticText( vws[ \view ],
				Rect( labelWidth + boxWidth + 4, 0, bounds.width - 2 - boxWidth - 2 - labelWidth, bounds.height)
		).applySkin( RoundView.skin )
		.background_( Color.white.alpha_(0.25) )
		.resize_( 5 )
		.mouseDownAction_({
			if( vws[ \menu ].isNil ) {
				vws[ \menu ] = this.makeMenu( vws[ \labels ], vws[ \list ], { |val|
					vws[ \box ].value = val ? 0;
					vws.setLabel;
					vws.doAction;
				});
			};
			vws[ \menu ] !? _.uFront;
		})
		.onClose_({
			vws[ \menu ] !? _.deepDestroy;
		});

		if( resize.notNil ) { vws[ \view ].resize = resize };
		vws.setLabel;
		^vws;
	}

	setView { |view, value, active = false|
		view[ \box ].value = value;
		{ view.setLabel; }.defer;
		if( active ) { view[ \box ].doAction };
	}
}

+ SharedValueIDSpec {

	makeView { |parent, bounds, label, action, resize|
		var vws, view, labelWidth;
		var boxWidth, setColor;

		bounds.isNil.if{bounds= 160 @ 18 };

		vws = ();
		#view, bounds = EZGui().prMakeMarginGap.prMakeView( parent, bounds );
		vws[ \view ] = view;

		vws[ \setColor ] = {
			var hash;
			hash = (vws[ \box ].value + this.class.umap_name.hash).hash;

			vws[ \drag ].background = Color.new255(
				(hash & 16711680) / 65536,
				(hash & 65280) / 256,
				hash & 255,
				128
			);
		};

		if( label.notNil ) {
			labelWidth = (RoundView.skin ? ()).labelWidth ? 80;
			vws[ \labelView ] = StaticText( vws[ \view ], labelWidth @ 14 )
				.string_( label.asString ++ " " )
				.align_( \right )
				.resize_( 4 )
				.applySkin( RoundView.skin );
		} {
			labelWidth = -4;
		};

		boxWidth = bounds.width-(labelWidth + 2 + 40 + 2);

		vws[ \box ] = SmoothNumberBox( vws[ \view ],
				Rect(labelWidth + 2, 0, boxWidth, bounds.height)
			)
		    .action_({ |vw|
		        action.value( vw, vw.value.asInteger );
		        vws.setColor;
		    } ).resize_(5)
		    .allowedChars_( "" )
			.step_( step )
			.scroll_step_( step )
			.alt_scale_( alt_step / step )
			.clipLo_( this.minval )
			.clipHi_( this.maxval );

		vws[ \drag ] = UDragBoth( vws[ \view ],
				Rect( labelWidth + boxWidth + 2, 0, 40,bounds.height)
			)
			.beginDragAction_({
				{ UChainGUI.current.view.refresh }.defer(0.1);
				UMap( this.class.umap_name, [ \id, vws[ \box ].value ] );
			})
			.canReceiveDragHandler_({
				View.currentDrag.isKindOf( UMap ) && {
					View.currentDrag.defName === (this.class.umap_name)
				};
			})
			.receiveDragHandler_({
				vws[ \box ].valueAction = View.currentDrag.id;
			});

		if( resize.notNil ) { vws[ \view ].resize = resize };
		vws.setColor;
		^vws;
	}

	setView { |view, value, active = false|
		view[ \box ].value = value;
		view.setColor;
		if( active ) { view.doAction };
	}

}

+ FreqSpec {

	makeView { |parent, bounds, label, action, resize|
		var vws, view, labelWidth;
		var font;
		var tempVal;
		var makeMenu, menu;

		vws = ();

		font =  (RoundView.skin ? ()).font ?? { Font( Font.defaultSansFace, 10 ); };

		bounds.isNil.if{bounds= 320@20};

		view = EZCompositeView( parent, bounds, gap: 2@2 );
		bounds = view.asView.bounds;

		makeMenu = {
			var mv0, mv1, mv2, sls, menus = [], currentValue, transpFunc;

			currentValue = vws[ \val ];

			RoundView.pushSkin( UChainGUI.skin ++ (labelWidth: 80) );

			menus = menus.add(
				Menu( *[ "C", "D", "E", "F", "G", "A", "B" ].collect({ |letter|
					Menu( *[ "bb", "b", "", "#", "##" ].collect({ |add|
						Menu( *(((this.minval.cpsmidi.round(1)/12).floor-2).asInteger..
							((this.maxval.cpsmidi.round(1)/12).floor-2).asInteger).collect({ |octave|
							MenuAction( letter ++ add ++ octave, {
								vws.setVal( (letter ++ add ++ octave).namecps );
								vws.doAction;
							})
						})
						).title_( letter ++ add )
					})
					).title_( letter )
				})
				).title_( "named note" )
			);

			mv0 = 1.collect({ View().minWidth_(300).minHeight_(20); });

			EZSmoothSlider(mv0[0], Rect( 0, 2, 300, 16 ), "midinote", [
				this.minval.cpsmidi.ceil,
				this.maxval.cpsmidi.floor,
				\lin, 1,
				this.default.cpsmidi.round(1)
			].asSpec )
			.value_( currentValue.cpsmidi.round(1) )
			.action_({ |sl|
				vws.setVal( sl.value.midicps );
				vws.doAction;
			}).sliderView.centered_( true );

			menus = menus.add(
				Menu( *mv0.collect({ |v| CustomViewAction( v ) }) ).title_( "MIDI note" )
			);

			if( this.maxval <= 300 ) {
				mv1 = 2.collect({ View().minWidth_(300).minHeight_(20); });
				EZSmoothSlider(mv1[0], Rect( 0, 2, 300, 16 ), "period", [1/this.maxval,1/this.minval,\exp,0,0].asSpec )
				.value_( 1/currentValue )
				.action_({ |sl|
					vws.setVal( 1/sl.value );
					vws.doAction;
				}).sliderView.centered_( true );
				EZSmoothSlider(mv1[1], Rect( 0, 2, 300, 16 ), "bpm", [this.minval*60,this.maxval*60,\exp,0.1,0].asSpec )
				.value_( currentValue * 60 )
				.action_({ |sl|
					vws.setVal( sl.value / 60 );
					vws.doAction;
				}).sliderView.centered_( true );

				menus = menus.add(
					Menu( *mv1.collect({ |v| CustomViewAction( v ) }) ).title_( "time" )
				);
			};

			mv2 = 3.collect({ View().minWidth_(300).minHeight_(20); });
			transpFunc = {
				vws.setVal(
						this.constrain(
							currentValue *
							((sls[0].value * 12) + (sls[1].value) + (sls[2].value/100)).midiratio
						)
					);
					vws.doAction;
			};
			sls = [
				EZSmoothSlider(mv2[0], Rect( 0, 2, 300, 16 ), "octaves", [-6,6,\lin,1,0].asSpec )
				.action_(transpFunc),
				EZSmoothSlider(mv2[1], Rect( 0, 2, 300, 16 ), "semitones", [-36,36,\lin,1,0].asSpec )
				.action_(transpFunc),
				EZSmoothSlider(mv2[2], Rect( 0, 2, 300, 16 ), "cents", [-100,100,\lin,1,0].asSpec )
				.action_(transpFunc);
			];
			sls.do({ |sl| sl.sliderView.centered = true });

			menus = menus.add(
				Menu( *mv2.collect({ |v| CustomViewAction( v ) }) ).title_( "transpose" )
			);

			RoundView.popSkin;

			menus = menus.add(
				Menu(
					*[ 0.001, 0.01, 0.1, 1, 10 ].collect({ |item|
						var res = currentValue.round( item );
						MenuAction( "%Hz (%)".format( item, res ), {
							vws.setVal( this.constrain( res ) );
							vws.doAction;
						});
					}) ++
					[ 1, 12 ].collect({ |item, i|
						var res = currentValue.cpsmidi.round( item ).midicps;
						MenuAction( "1 % (%)".format( ["semitone", "octave"][i], res.round(0.001) ), {
							vws.setVal( this.constrain( res ) );
							vws.doAction;
						});
					})
				).title_( "round" );
			);

			menus = menus.add(
				Menu(
					*(2..16).collect({ |item|
						var res = (currentValue / item);
						MenuAction( "/ % (%)".format( item, res.round(0.001) ), {
							vws.setVal( this.constrain( res ) );
							vws.doAction;
						});
					})
				).title_( "divide" );
			);

			menus = menus.add(
				Menu(
					*(2..16).collect({ |item|
						var res = (currentValue * item).interpret;
						MenuAction( "* % (%)".format( item, res.round(0.001) ), {
							vws.setVal( this.constrain( res ) );
							vws.doAction;
						});
					})
				).title_( "multiply" );
			);

			menus = menus.add(
				Menu( *(1..16).collect({ |item| (1..16).collect({ |x| [item,x] }) }).collect({ |item|
					Menu(
						*item.collect({ |item|
							var interp, joined;
							joined = item.join("/");
							interp = joined.interpret.asFraction;
							if( interp[1] == 1 ) { interp = [ interp.first ] };
							interp = interp.join( "/" );
							if( interp == joined ) {
								MenuAction( joined, {
									vws.setVal( this.constrain( currentValue * joined.interpret ) );
									vws.doAction;
								});
							} {
								MenuAction( "% (%)".format( joined, interp ), {
									vws.setVal( this.constrain( currentValue * interp.interpret ) );
									vws.doAction;
								});
							};
						})
					).title_( item.first.join( "/" ) + "-" + item.last.join("/") );
				}) ).title_( "fraction" )
			);

			menus = menus.add(
				MenuAction( "default (%)".format( this.default ), { vws.setVal( this.default ); vws.doAction });
			);

			menu = Menu( *menus ).uFront;
		};

		vws[ \view ] = view;

		vws[ \val ] = this.default;

		vws[ \setVal ] = { |vwx, val|
			vws[ \val ] = val;
			vws[ \hz ].value = val;
			vws.setName;
		};

		vws[ \doAction ] = {
			action.value( vws, vws[ \val ] );
		};

		if( label.notNil ) {
			labelWidth = (RoundView.skin ? ()).labelWidth ? 80;
			vws[ \labelView ] = StaticText( vws[ \view ], labelWidth @ bounds.height )
				.string_( label.asString ++ " " )
				.align_( \right )
				.resize_( 4 )
				.applySkin( RoundView.skin );
		} {
			labelWidth = 0;
		};

		// hz mode
		vws[ \hz ] = EZSmoothSlider( vws[ \view ],
			(bounds.width - (labelWidth + 45)) @ (bounds.height),
			nil,  this, { |vw|
				vws[ \val ] = vw.value;
				vws.setName;
				vws.doAction;
			}, numberWidth: RoundView.skin.numberWidth ? 40
		);

		vws[ \hz ].sliderView.centered_( true ).centerPos_( this.unmap( default ) );

		vws[ \hz ].numberView.allowedChars = "+-.AaBbCcDdEeFfGgMmTt#*/()%";
		vws[ \hz ].numberView.interpretFunc = { |string, val|
			var cents = 0, splits;
			string = string.format( val );
			case { "AaBbCcDdEeFfGg".includes(string.first) } {
				if( string.indexOf( $+ ).notNil ) {
					cents = string.split( $+ ).last.interpret;
				} {
					splits = string.split($-);
					if( splits.size > 1 ) {
						if( splits[ splits.size-2 ].last.isDecDigit ) {
							cents = splits.last.interpret.neg;
						};
					};
				};
				string.namecps * (cents / 100).midiratio;
			} { "Mm".includes(string.first) } {
				string[1..].interpret.midicps;
			} { "Tt".includes(string.first) } {
				("0" ++ string[1..]).interpret.midiratio * val;
			} {
				string.interpret;
			};
		};

		vws[ \name ] = StaticText( view, 40 @ (bounds.height) )
			.font_( font )
			.applySkin( RoundView.skin ? () )
		    .mouseDownAction_({ makeMenu.value })
		    .string_( " " ++ this.default.cpsname )
		    .background_( Color.white.alpha_(0.25) );

		vws[ \name ].setProperty(\wordWrap, false);

		vws[ \setName ] = {
			var name;
			name = vws[ \val ].cpsname;
			if( name.cents.abs >= 1 ) {
				vws[ \name ].string = " % %%".format(
					name, if( name.cents.isPositive ) { "+" } { "" },
					name.cents.round(1).asInteger
				)
			} {
				vws[ \name ].string = " " ++ name;
			};
		};

		vws[ \view ].onClose_({ menu !? _.deepDestroy });

		^vws;
	}

	setView { |view, value, active = false|
		view[ \val ] = value;
		view[ \hz ].value = value;
		{
			view.setName;
		}.defer;
		if( active ) { view.doAction };
	}

	mapSetView { |view, value, active = false|
		this.setView( view, this.map( value ), active );
	}
}

+ AngleSpec {

	makeView {  |parent, bounds, label, action, resize|
		var vws, view, labelWidth;
		var localStep;
		var modeFunc;
		var font;
		var editAction;
		var tempVal;
		var degMul = 180 / pi;
		var ctrl;
		vws = ();

		font =  (RoundView.skin ? ()).font ?? { Font( Font.defaultSansFace, 10 ); };

		localStep = step.copy;
		if( step == 0 ) { localStep = 1 };
		bounds.isNil.if{bounds= 320@20};

		view = EZCompositeView( parent, bounds, gap: 2@2 );
		bounds = view.asView.bounds;

		vws[ \view ] = view;

		if( label.notNil ) {
			labelWidth = (RoundView.skin ? ()).labelWidth ? 80;
			vws[ \labelView ] = StaticText( vws[ \view ], labelWidth @ bounds.height )
				.string_( label.asString ++ " " )
				.align_( \right )
				.resize_( 4 )
				.applySkin( RoundView.skin );
		} {
			labelWidth = 0;
		};

		vws[ \comp ] = CompositeView( view, (bounds.width - (labelWidth + 44)) @ (bounds.height) );

		vws[ \mode ] = StaticText( view, 40 @ (bounds.height) )
		.font_( font )
		.string_( " pi" )
		.applySkin( RoundView.skin ? () )
		.background_( Color.white.alpha_( 0.25 ) )
		.mouseDownAction_({ |pu|
			var actions;
			actions = [
				MenuAction( "radians (-pi - pi)", {
					this.class.mode = \rad;
				}).enabled_( mode != \rad ),
				MenuAction( "degrees (-180° - 180°)", {
					this.class.mode = \deg;
				}).enabled_( mode != \deg ),
			];
			Menu( *actions ).uFront( action: actions[ [\rad, \deg].indexOf( mode ) ] );
		});

		// rad mode
		vws[ \rad ] = EZSmoothSlider( vws[ \comp ],
			vws[ \comp ].bounds.width @ (bounds.height),
			nil,
			[ this.minval / pi, this.maxval / pi, \lin, step / pi, this.default / pi ].asSpec,
			{ |vw| action.value( vw, vw.value * pi ) },
			numberWidth: RoundView.skin.numberWidth ? 40
		).visible_( false );

		vws[ \rad ].sliderView
			.centered_( true )
			.centerPos_( this.unmap( default ) )
			.clipMode_( \wrap );

		// deg mode
		vws[ \deg ] = EZSmoothSlider( vws[ \comp ],
			vws[ \comp ].bounds.width @ (bounds.height),
			nil,
			[ this.minval * degMul, this.maxval * degMul, \lin, step * degMul,
				this.default * degMul ].asSpec,
			{ |vw| action.value( vw, vw.value / degMul ) },
			numberWidth: RoundView.skin.numberWidth ? 40
		).visible_( false );

		vws[ \deg ].sliderView
			.centered_( true )
			.centerPos_( this.unmap( default ) )
			.clipMode_( \wrap );

		this.setMode( vws, mode );

		ctrl = SimpleController( this.class )
		.put( \mode, { { this.setMode( vws, mode ); }.defer });

		vws[ \comp ].onClose_({ ctrl.remove });

		^vws;
	}

	setMode { |view, newMode|
		[ \rad, \deg ].do({ |item|
			view[ item ].visible = (item == newMode)
		});
		view[ \mode ].string = ( \rad: " pi", \deg: " °" )[ newMode ];
	}

	setView { |view, value, active = false|
		view[ \rad ].value = value / pi;
		view[ \deg ].value = value * 180 / pi;
		{
			this.setMode( view, mode );
			// view[ \mode ].value = view[ \mode ].items.indexOf( mode ) ? 0;
		}.defer;
		if( active ) { view[ \rad ].doAction };
	}

	mapSetView { |view, value, active = false|
		this.setView( view, this.map( value ), active );
	}

}

+ AngleArraySpec {

	/*
	makeView { |parent, bounds, label, action, resize|
		var mode, vws, act, spec, degMul;
		mode = AngleSpec.mode;
		switch( mode,
			\rad, {
				act = { |vws, value|
					action.value( vws, value * pi )
				};
				spec = ArrayControlSpec( minval / pi, maxval / pi, \linear, step, default / pi );
				spec.originalSpec = this.originalSpec;
				vws = spec.makeView( parent, bounds, label, act, resize );
				vws[ \mode ] = \rad;
				vws[ \spec ] = spec;
			},
			\deg, {
				degMul = 180 / pi;
				act = { |vws, value|
					action.value( vws, value / degMul );
				};
				spec = ArrayControlSpec( minval * degMul, maxval * degMul, \linear, step,
					default * degMul );
				spec.originalSpec = this.originalSpec;
				vws = spec.makeView( parent, bounds, label, act, resize );
				vws[ \mode ] = \deg;
				vws[ \spec ] = spec;
			}
		);
		^vws;
	}

	setView { |view, value, active = false|
		switch( view[ \mode ],
			\rad, { value = value / pi },
			\deg, { value = value * 180 / pi }
		);
		view[ \spec ].setView( view, value, active )
	}

	mapSetView { |view, value, active = false|
		this.setView( view, this.map( value ), active );
	}
	*/

}

+ FactorSpec {

	makeView { |parent, bounds, label, action, resize|
		var vws, view, labelWidth;

		bounds.isNil.if{bounds= 160 @ 18 };

		vws = ();
		#view, bounds = EZGui().prMakeMarginGap.prMakeView( parent, bounds );
		vws[ \view ] = view;

		if( label.notNil ) {
			labelWidth = (RoundView.skin ? ()).labelWidth ? 80;
			vws[ \labelView ] = StaticText( vws[ \view ], labelWidth @ 14 )
				.string_( label.asString ++ " " )
				.align_( \right )
				.resize_( 4 )
				.applySkin( RoundView.skin );
		} {
			labelWidth = -4;
		};

		vws[ \box1 ] = SmoothNumberBox( vws[ \view ],
				Rect(labelWidth + 2,0,40,bounds.height)
			)
		    .action_({ |vw|
		        action.value( vw, vws[ \box1 ].value / vws[ \box2 ].value );
		    } ).resize_(5)
		    .allowedChars_( "" )
			.step_( 1 )
			.scroll_step_( 1 )
			.alt_scale_( 0.1 )
			.clipLo_( 1 )
			.clipHi_( 32 );

		StaticText( vws[ \view ], Rect(labelWidth + 2 + 40,0,60,bounds.height) )
			.string_( " /" )
			.applySkin( RoundView.skin );

		vws[ \box2 ] = SmoothNumberBox( vws[ \view ],
				Rect(labelWidth + 2 + 40 + 2 + 20,0,60,bounds.height)
			)
		    .action_({ |vw|
		        action.value( vw,  vws[ \box1 ].value / vws[ \box2 ].value );
		    } ).resize_(5)
		    .allowedChars_( "" )
			.step_( 1 )
			.scroll_step_( 1 )
			.alt_scale_( 0.1 )
			.clipLo_( 1 )
			.clipHi_( 32 );

		if( resize.notNil ) { vws[ \view ].resize = resize };
		^vws;
	}

	setView { |view, value, active = false|
		if( value > 1 ) {
			view[ \box1 ].value = value;
			view[ \box2 ].value = 1;
		} {
			view[ \box1 ].value = 1;
			view[ \box2 ].value = 1/value;
		};
		if( active ) { view.doAction };
	}

}

+ URandSeed { // is actually a Spec too

	*viewNumLines { ^1 }

	*makeView { |parent, bounds, label, action, resize|
		var vws, view, labelWidth, boxWidth;

		bounds.isNil.if{bounds= 160 @ 18 };

		vws = ();
		#view, bounds = EZGui().prMakeMarginGap.prMakeView( parent, bounds );
		vws[ \view ] = view;

		vws[ \setValue ] = { |vwx, value|
			if( vws[ \val ] != value ) {
				vws[ \ctrl ].remove;
				vws[ \val ] = value;
				if( vws[ \val ].isKindOf( URandSeed ) ) {
					vws[ \auto ].value = 1;
					vws[ \box ].value = vws[ \val ].value;
					vws[ \box ].enabled = false;
					vws[ \ctrl ] = SimpleController( vws[ \val ] )
						.put( \seed, {
							vws[ \box ].value = vws[ \val ].value;
						});
				} {
					vws[ \auto ].value = 0;
					vws[ \box ].value = vws[ \val ];
					vws[ \box ].enabled = true;
				};
			};
		};

		vws[ \view ].onClose_({ vws[ \ctrl ].remove });

		if( label.notNil ) {
			labelWidth = (RoundView.skin ? ()).labelWidth ? 80;
			vws[ \labelView ] = StaticText( vws[ \view ], labelWidth @ 14 )
				.string_( label.asString ++ " " )
				.align_( \right )
				.resize_( 4 )
				.applySkin( RoundView.skin );
		} {
			labelWidth = -4;
		};

		boxWidth = bounds.width-(42 + labelWidth + 2);

		vws[ \box ] = SmoothNumberBox( vws[ \view ],
				Rect(labelWidth + 2,0,boxWidth,bounds.height)
			)
		    .action_({ |vw|
		        action.value( vw, vw.value.asInteger );
		    } ).resize_(5)
		    .allowedChars_( "" )
			.step_( 1 )
			.scroll_step_( 1)
			.alt_scale_( 10 )
			.clipLo_( 0 )
			.clipHi_( 16777216 );

		vws[ \auto ] = SmoothButton( vws[ \view ],
			Rect(labelWidth + 2 + boxWidth + 2,0,40,bounds.height)
		)
			.radius_(2)
			.resize_(3)
			.action_( { |bt|
				switch( bt.value,
					0, { action.value( vws, vws[ \box ].value ) },
					1, { action.value( vws, URandSeed() ) }
				);
			})
			.label_( [ "auto", "auto" ] );

		if( resize.notNil ) { vws[ \view ].resize = resize };
		^vws;
	}

	*setView { |view, value, active = false|
		view.setValue( value );
		if( active ) { view[ \auto ].doAction };
	}

}


+ URandSeedMassEditSpec { // is actually a Spec too

	viewNumLines { ^1 }

	makeView { |parent, bounds, label, action, resize|
		var vws, view, labelWidth, boxWidth;

		bounds.isNil.if{bounds= 160 @ 18 };

		vws = ();
		#view, bounds = EZGui().prMakeMarginGap.prMakeView( parent, bounds );
		vws[ \view ] = view;

		vws[ \setValue ] = { |vwx, value|
			var min, max;
			if( vws[ \val ] != value ) {
				vws[ \val ] = value;
				case {
					vws[ \val ].every( _.isKindOf( URandSeed ) )
				} {
					vws[ \auto ].value = 1;
					vws[ \box ].value = 'auto';
					vws[ \box ].enabled = false;
				} {
					 vws[ \val ].any( _.isKindOf( URandSeed ) )
				} {
					vws[ \auto ].value = 2;
					min = vws[ \val ].select( _.isNumber ).minItem;
					max = vws[ \val ].select( _.isNumber ).maxItem;
					if( min == max ) {
						vws[ \box ].value = "mixed (%, auto)".format( min ).asSymbol;
					} {
						vws[ \box ].value = "mixed (% - %, auto)".format( min,max ).asSymbol;
					};
					vws[ \box ].enabled = true;
				} {
					vws[ \val ].every({ |item| item == vws[ \val ].first })
				} {
					vws[ \auto ].value = 0;
					vws[ \box ].value = vws[ \val ].first;
					vws[ \box ].enabled = true;
				} {
					vws[ \auto ].value = 0;
					min = vws[ \val ].select( _.isNumber ).minItem;
					max = vws[ \val ].select( _.isNumber ).maxItem;
					vws[ \box ].value = "mixed (% - %)".format( min,max ).asSymbol;
					vws[ \box ].enabled = true;
				};
			}
		};

		if( label.notNil ) {
			labelWidth = (RoundView.skin ? ()).labelWidth ? 80;
			vws[ \labelView ] = StaticText( vws[ \view ], labelWidth @ 14 )
				.string_( label.asString ++ " " )
				.align_( \right )
				.resize_( 4 )
				.applySkin( RoundView.skin );
		} {
			labelWidth = -4;
		};

		boxWidth = bounds.width-(42 + labelWidth + 2);

		vws[ \box ] = SmoothNumberBox( vws[ \view ],
				Rect(labelWidth + 2,0,boxWidth,bounds.height)
			)
		    .action_({ |vw|
			    if( vw.value.isNumber ) {
				     action.value( vw, vw.value ! size );
			    };
		    } ).resize_(5)
		    .allowedChars_( "" )
			.step_( 1 )
			.scroll_step_( 1)
			.alt_scale_( 10 )
			.clipLo_( 0 )
			.clipHi_( 16777216 );

		vws[ \auto ] = SmoothButton( vws[ \view ],
			Rect(labelWidth + 2 + boxWidth + 2,0,40,bounds.height)
		)
			.radius_(2)
			.resize_(3)
			.action_( { |bt|
				switch( bt.value,
					0, { action.value( vws, vws[ \val ].collect(_.value) ) },
					1, { action.value( vws, { URandSeed() }!size ) },
					2, { bt.valueAction_(0) }
				);
			})
			.states_( [ ["auto"], ["auto"], ["auto", nil, Color(0.0, 0.0, 0.0, 0.33/2)] ] );

		if( resize.notNil ) { vws[ \view ].resize = resize };
		^vws;
	}

	setView { |view, value, active = false|
		view.setValue( value );
		if( active ) { view[ \auto ].doAction };
	}
}

+ DisplaySpec {

	makeView {
		 |parent, bounds, label, action, resize|
		var vws, view, labelWidth, font;
		vws = ();

		font =  (RoundView.skin ? ()).font ?? { Font( Font.defaultSansFace, 10 ); };
		bounds.isNil.if{bounds= 320@20};

		view = EZCompositeView( parent, bounds, gap: 2@2 );
		bounds = view.asView.bounds;

		vws[ \view ] = view;

		if( label.notNil ) {
			labelWidth = (RoundView.skin ? ()).labelWidth ? 80;
			vws[ \labelView ] = StaticText( vws[ \view ], labelWidth @ bounds.height )
				.string_( label.asString ++ " " )
				.align_( \right )
				.resize_( 4 )
				.applySkin( RoundView.skin );
		} {
			labelWidth = 0;
		};

		vws[ \display ] = StaticText( view, (bounds.width - (labelWidth + 4)) @ (bounds.height) )
			.font_( font );

		^vws;
	}

	setView { |view, value, active = false|
		{
			view[ \display ].string = formatFunc.value( value, spec );		}.defer;
	}

	mapSetView { |view, value, active = false|
		this.setView( view, this.map( value ), active );
	}

}

+ UMIDIFileSpec {

	viewNumLines { ^2 }

	makeView { |parent, bounds, label, action, resize|
		var vws, view, labelWidth;
		var ctrl, strWidth, viewHeight;
		vws = (
			menuPaths: [ nil ],
			doAction: { |evt|
				action.value( vws, vws[ \obj ] )
			}
		);

		// this is basically an EZButton

		bounds.isNil.if{bounds= 320@40};

		#view, bounds = EZGui().prMakeMarginGap.prMakeView( parent, bounds );
		 vws[ \view ] = view;

		viewHeight = (bounds.height / 2) - 1;

		if( label.notNil ) {
			labelWidth = (RoundView.skin ? ()).labelWidth ? 80;
			vws[ \labelView ] = StaticText( vws[ \view ], labelWidth @ viewHeight )
				.string_( label.asString ++ " " )
				.align_( \right )
				.resize_( 4 )
				.applySkin( RoundView.skin );
		} {
			labelWidth = 0;
		};

		vws[ \menu ] = UPopUpMenu( view,
			Rect( labelWidth + 2, 0, bounds.width - (40 + labelWidth + 2), viewHeight )
		)	.resize_(3)
			.items_( [ "" ] )
			.action_({ |pu|
				if( vws[ \menuPaths ].size > 1 ) {
					if( pu.value > 0 ) {
						vws[ \obj ] !? _.path_( vws[ \menuPaths ][ pu.value ].asString )
						?? { vws[ \obj ] = UMIDIFile( vws[ \menuPaths ][ pu.value ].asString ) };
					} {
						vws[ \obj ] !? _.path_( nil ) ?? { vws[ \obj ] = UMIDIFile() };
					};
					vws.doAction;
				};
			});

		ctrl = {
			var menuItems;
			vws[ \menuPaths ] = [ nil ] ++ UMIDIFile.all.keys.asArray.sort;
			menuItems = [ "" ] ++ vws[ \menuPaths ][1..].collect({ |item| item.asString.basename });
			{
				if( vws[ \menu ].items != menuItems ) {
					vws[ \menu ].items = menuItems;
				};
				vws[ \menu ].value = vws[ \menuPaths ].indexOf( vws[\obj] !? { |x| x.key } ) ? 0;
			}.defer;
		};

		UMIDIFile.addDependant( ctrl );

		ctrl.value;

		vws[ \browse ] = SmoothButton( view, Rect( bounds.width - 36, 0, 16, viewHeight ) )
			.radius_( 0 )
			.resize_( 3 )
			.label_( 'folder' )
			.action_({
				ULib.openPanel( { |path|
				  vws[ \obj ] !? { |x| x.path_( path ); x.reload; }
				  ?? { vws[ \obj ] = UMIDIFile( path, true ) };
				  vws.doAction;
				});
			});

		vws[ \refresh ] = SmoothButton( view, Rect( bounds.width - 16, 0, 16, viewHeight ) )
			.radius_( 0 )
			.resize_( 3 )
			.label_( 'roundArrow' )
			.action_({
				vws[ \obj ] !? _.reload;
			});

		vws[ \info ] = StaticText( view,
			Rect( labelWidth + 2, viewHeight + 2, bounds.width - (labelWidth + 2) - 36, viewHeight )
		).applySkin( RoundView.skin )
		.string_( "--" );

		vws[ \plot ] = SmoothButton( view, Rect( bounds.width - 36, viewHeight + 2, 36, viewHeight ) ).radius_( 0 )
			.resize_( 3 )
		    .label_( "plot" )
		.action_({
			if( vws[ \obj ].notNil ) {
				vws[ \obj ].midiFile !? _.plot;
			};
		});


		vws[ \menu ].onClose_( { UMIDIFile.removeDependant( ctrl ); } );

		if( resize.notNil ) { vws[ \view ].resize = resize };
		^vws;
	}

	setView { |view, value, active = false|
		value = this.constrain( value );
		view[ \obj ] = value;
		{
			view[ \menu ].value = view[ \menuPaths ].indexOf( view[ \obj ] !? { |x| x.key } ) ? 0;
			if( view[ \obj ].notNil && { view[ \obj ].midiFile.notNil }) {
				view[ \info ].string = "%, % notes, % cc".format(
					view[ \obj ].midiFile.timeMode_(\seconds).length.asSMPTEString(1000),
					view[ \obj ].midiFile.realNoteOnEvents.size,
					view[ \obj ].midiFile.controllerEvents.size
				)
			} {
				view[ \info ].string = "--";
			};
		}.defer;
		if( active ) { view.doAction };
	}

}


+ EZPopUpMenu {

	labelWidth { ^labelView !? { labelView.bounds.width } ? 0 }

	labelWidth_ { |width = 80|
		var delta;
		if( layout === \horz && { labelView.notNil } ) { // only for horizontal sliders
			delta = labelView.bounds.width - width;
			labelView.bounds = labelView.bounds.width_( width );
			widget.bounds = widget.bounds
				.width_( widget.bounds.width + delta )
				.left_( widget.bounds.left - delta );
		};
	}
}