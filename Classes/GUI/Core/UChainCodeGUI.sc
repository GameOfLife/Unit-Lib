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

UChainCodeGUI : UChainGUI {

	makeViews { |bounds|

		RoundView.useWithSkin( skin ++ (RoundView.skin ? ()), {
			this.prMakeViews( bounds );
		});

	}

	getUnits { ^chain.units }

    makeUnitHeader { |units, margin, gap|
        var comp, header, io, code, defs;

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
                .action_({
	                UChainIOGUI(
	                	this.window.name, originalBounds,
	                	chain, replaceCurrent: true
	                );
                }).resize_(3);

        code = SmoothButton( comp,
                    Rect( comp.bounds.right - (40 + 4 + 40), 1, 40, 12 ) )
                .label_( "code" )
                .radius_( 2 )
		        .background_( RoundView.skin[ 'SmoothButton' ] !? _.hiliteColor ? Color.green )
                .action_({
	                UChainGUI(
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

		labelWidth = 80;
	    width = scrollView.bounds.width - 12 - (margin.x * 2);

		if( RoundView.skin.notNil ) { labelWidth = RoundView.skin.labelWidth ? 80 };

		unitInitFunc = { |unit, what ...args|
			if( what === \init ) { // close all views and create new
				chain.changed( \units );
			};
		};

		DragSource(scrollView, (width - 104)@16 )
		.align_(\center)
		.object_( chain.asCompileString )
		.applySkin( RoundView.skin )
		.string_( "drag me for the UChain's code" );

		SmoothButton(scrollView, 100@16 )
		.label_( "post uchain code" )
		.action_({
			chain.postcs;
		});

		if( chain.isKindOf( UPattern ) && { chain.pattern.isKindOf( UMap ) } ) {
			units = [ chain.pattern ] ++ units;
		};

		^units.collect({ |unit, i|
			var header, comp, views, params;

			if( unit.isKindOf( UMap ).not ) {

				comp = CompositeView( scrollView, width@14 )
				.resize_(2);

				header = StaticText( comp, comp.bounds.moveTo(0,0) )
				.applySkin( RoundView.skin )
				.string_( " " ++ i ++ ": " ++ if(unit.def.class == LocalUdef){"[Local] "}{""} ++ unit.defName )
				.background_( RoundView.skin.headerColor ?? { Color.white.alpha_(0.5) } )
				.resize_(2)
				.font_(
					(RoundView.skin.tryPerform( \at, \font ) ??
						{ Font( Font.defaultSansFace, 12) }).boldVariant
				);
				if(unit.def.class == LocalUdef) {
					SmoothButton( comp, Rect( comp.bounds.right - (80+2+80), 1, 80, 12 ) )
					.label_( "revert" )
					.radius_( 2 )
					.action_({
						unit.def = unit.def.asOriginalUdef;
					}).resize_(3);

					SmoothButton( comp, Rect( comp.bounds.right - (80), 1, 80, 12 ) )
					.label_( "save as Udef" )
					.radius_( 2 )
					.action_({
						unit.def.saveAsUdef({ |x|
							unit.def_(x);
							ULib.servers.do{ |s| unit.def.sendSynthDef(s) }
						})
					}).resize_(3);
				} {
					SmoothButton( comp, Rect( comp.bounds.right - (100), 1, 100, 12 ) )
					.label_( "open Udef file" )
					.radius_( 2 )
					.action_({
						unit.def.openDefFile;
					}).resize_(3);
				};
				views = this.makeUnitView( unit, scrollView, i, labelWidth, width );

				unit.addDependant( unitInitFunc );

				header.onClose_({
					unit.removeDependant( unitInitFunc );
					views[ \ctrl ].remove;
				});

			};

			(
				if( unit.isKindOf( UMap ) ) {
					[ unit ] ++ (unit.getAllUMaps ? [])
				} {
					unit.getAllUMaps
				}
			).do({ |umap|
				var comp, header;
				comp = CompositeView( scrollView, width@14 )
				.resize_(2);

				header = StaticText( comp, comp.bounds.moveTo(0,0) )
				.applySkin( RoundView.skin )
				.string_( "     ." ++
					(unit.getUMapPath( umap ) ? ["pattern"]).join(".") ++
					": " ++ umap.defName )
				.background_( umap.guiColor )
				.resize_(2)
				.font_(
					(RoundView.skin.tryPerform( \at, \font ) ??
						{ Font( Font.defaultSansFace, 12) }).boldVariant
				);

				SmoothButton( comp, Rect( comp.bounds.right - (100), 1, 100, 12 ) )
				.label_( "open UMapDef file" )
				.radius_( 2 )
				.action_({
					umap.def.openDefFile;
				}).resize_(3);
			});

			views;

		});

	}

	makeUnitView { |unit, comp, i, labelWidth, width|
		var ctrl;
		var views;
		var font = RoundView.skin.font ?? { Font( Font.defaultSansFace, 11); };

		views = ();

		//ctrl = SimpleController( unit );
        if( (U.uneditableCategories.includes(unit.def.category) || [FreeUdef, MultiUdef, MultiChannelUdef].includes(unit.def.class)).not ) {
            StaticText(comp, 200@17).font_(font).string_("Function:");
            comp.decorator.nextLine;
            views[\func] = CodeEditView.new(comp, width@100, unit.def.func)
                .action_({ |f|
                    var def = unit.def;
                    unit.def_(LocalUdef(def.name,f.object,def.argSpecs,def.category).shouldPlayOnFunc_(def.shouldPlayOnFunc))
                });
            comp.decorator.nextLine;
            StaticText(comp, 200@17).font_(font).string_("Argument Specs:");
            comp.decorator.nextLine;
            views[\specs] = CodeEditView.new(comp, width@100, unit.def.argSpecs.select({ |x| x.name.asString[..1] != "u_"}))
                .action_({ |f|
                    var def = unit.def;
                    unit.def_(LocalUdef(def.name,def.func,
                        f.object++def.argSpecs.select({ |x| x.name.asString[..1] == "u_"}),def.category))
                });
            comp.decorator.nextLine;
            StaticText(comp, 200@17).font_(font).string_("Category:");
            comp.decorator.nextLine;
            views[\category] = CodeEditView.new(comp, width@40, unit.def.category)
               .action_({ |f|
                    unit.def_(LocalUdef(unit.def.name,unit.def.func,unit.def.argSpecs,f.object))
               });
        } {
            StaticText(comp, 200@17).font_(font).string_("Unit is not editable")
        };
		if( views.size == 0 ) {
			//ctrl.remove;
		} {
			//views[ \ctrl ] = ctrl;
		};
		^views;

	}

}