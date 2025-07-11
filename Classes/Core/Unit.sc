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

/*


Udef -> *new { |name, func, args, category|
    name: name of the Udef and corresponding unit
    func: ugen graph function
    args:  array with argName/default pairs
    category: ?

     -> defines a synthdef, and specs for the argumetns of the synthdef
     -> Associates the unitDef with a name in a dictionary.

U -> *new { |defName, args|
	Makes new Unit based on the defName.
	Retrieves the corresponding Udef from a dictionary
	sets the current args


// example

//using builtin Udefs
//looks for the file in the Udefs folder
x  = U(\sine)
x.def.loadSynthDef
x.start
(
x = Udef( \sine, { |freq = 440, amp = 0.1|
	Out.ar( 0, SinOsc.ar( freq, 0, amp ) )
} );
)

y = U( \sine, [ \freq, 880 ] );
y.gui;

y.def.loadSynthDef;

y.start;
y.stop;

y.set( \freq, 700 );

(
// a styled gui in user-defined window
w = Window( y.defName, Rect( 300,25,200,200 ) ).front;
w.addFlowLayout;
RoundView.useWithSkin( (
	labelWidth: 40,
	font: Font( Font.defaultSansFace, 10 ),
	hiliteColor: Color.gray(0.25)
), {
	SmoothButton( w, 16@16 )
		.label_( ['power', 'power'] )
		.hiliteColor_( Color.green.alpha_(0.5) )
		.action_( [ { y.start }, { y.stop } ] )
		.value_( (y.synths.size > 0).binaryValue );
	y.gui( w );
});
)

*/

Udef : GenericDef {

	classvar <>all, <>defsFolders, <>userDefsFolder;
	classvar <>buildUdef; // the Udef currently under construction
	classvar <>buildArgSpecs; //The specs under construction
	classvar <>loadOnInit = true;
	classvar <>synthDefDir;

	var <>func, <>category;
	var <>synthDef;
	var <>shouldPlayOnFunc;
	var <>nameFunc;
	var <>apxCPU = 1; // indicator for the amount of cpu this unit uses (for load balancing)
	var <>extraPrefix;
	var <>numChannels = 1;
	var <>ioNames;
	var <>canUseUMap = true;
	var <>showOnCollapse = #[ value ];
	var <>prepareArgsFunc;
	var <>prepareFunc;
	var <>uchainInitFunc;
	var <>inputIsEndPoint = false;
	var <>dontStoreArgNames;
	var <>dontStoreSynthDef = false;
	var <>infoString;
	var <>disposeFunc, <>disposeForFunc;
	var <>makeSynthDesc = false, <>writeOnce = false;
	var <>synthDefName;

	*initClass{
		defsFolders = [
			this.filenameSymbol.asString.dirname.dirname.dirname +/+ "UnitDefs"
		];
		userDefsFolder = Platform.userAppSupportDir ++ "/UnitDefs/";
	}

	*basicNew { |name, args, category, addToAll=true, extraPrefix|
		^super.new( name, args, addToAll )
			.extraPrefix_( extraPrefix ).category_( category ? \default );
	}

	*new { |name, func, args, category, addToAll=true, extraPrefix, makeSynthDesc=false, writeOnce = false|
		^super.new( name, args, addToAll )
		.extraPrefix_( extraPrefix )
		.makeSynthDesc_( makeSynthDesc )
		.writeOnce_( writeOnce )
		.init( func )
		.category_( category ? \default );
	}

	*prefix { ^"u_" }

	*callByName { ^true }

	*numChannels { ^buildUdef !? { buildUdef.numChannels } ? 1 }

	*addBuildSpec { |argSpec|
		this.buildArgSpecs !? {
			this.buildArgSpecs = this.buildArgSpecs ++ [argSpec];
		}
	}

	//Udef.find("pass")
	*find { |string|
		string = string.asString;
		^Udef.all.select{ |x| x.name.asString.find(string, true).notNil }.asArray
	}

	*open { |symbol|
		Udef.all.at(symbol).openDefFile
	}

	*clearArgSpecData {
		"rm -f %/*.uargspecs".format( synthDefDir.escapeChar($ )).systemCmd;
	}

    prGenerateSynthDefName {
       ^this.class.prefix ++ (extraPrefix ? "") ++ this.name.asString
    }

	init { |inFunc|
		var fileHash, umtaData;

		func = inFunc;

		synthDefName = this.prGenerateSynthDefName;

		if( this.filePath.notNil ) {
			File.use( this.filePath, "r", { |f| fileHash = f.readAllString.hash });
		};

		if( loadOnInit.not && { this.writeOnce && {
			File.exists( synthDefDir +/+ synthDefName ++ ".uargspecs" )
		}}) {
			umtaData = (synthDefDir +/+ synthDefName ++ ".uargspecs").load;
			if( umtaData[ \fileHash ].notNil && { umtaData[ \fileHash ] == fileHash }) {
				argSpecs = umtaData[ \argSpecs ];
				this.initArgs;
			} {
				this.initSynthDef( fileHash );
			};
		} {
			this.initSynthDef( fileHash );
		};

		this.changed( \init );
	}

	initSynthDef { |fileHash|

		this.class.buildUdef = this;
		this.class.buildArgSpecs = [];
		this.synthDef = SynthDef( synthDefName, func );
		this.class.buildUdef = nil;

		argSpecs = ArgSpec.fromSynthDef( this.synthDef, argSpecs ++ this.class.buildArgSpecs );

		this.class.buildArgSpecs = nil;

		this.initArgs;

		if( this.writeOnce ) {
			File.use( synthDefDir +/+ synthDefName ++ ".uargspecs", "w", { |f|
				f.write( ( \argSpecs: argSpecs, \fileHash: fileHash ).cs );
			});
		};

		if( this.dontStoreSynthDef ) { this.synthDef = nil };
		if( loadOnInit ) { this.loadSynthDef } { this.writeDefFile };
	}

	initArgs {
		argSpecs.do({ |item|
			if( item.name.asString[..1].asSymbol == 'u_' ) {
				item.private = true;
			};
			if( item.spec.notNil ) {
				if( item.default.class != item.spec.default.class ) {
					item.default = item.spec.constrain( item.default );
				};
			};
		});
	}

	asArgsArray { |argPairs, unit, constrain = true|
		argPairs = prepareArgsFunc.value( argPairs ) ? argPairs ? #[];
		^this.argSpecs( unit ).collect({ |item|
			var val;
			val = argPairs.pairsAt(item.name) ?? { item.default.copy };
			val = val.deepCopy.asUnitArg( unit, item.name );
			if( constrain && { val.isKindOf( UMap ).not } ) { val = item.constrain( val ) };
			[ item.name,  val ]
		}).flatten(1);
	}

	argNamesFor { |unit| ^this.argNames }

	createsSynth { ^true }

	// this may change
	// temp override to send instead of load (remote servers can't load!!)
	loadSynthDef { |server|
		var defs;
		server = server ? ULib.servers ? Server.default;
		defs = this.synthDef.asCollection;
		server.asCollection.do{ |s|
			if( s.class == LoadBalancer ) {
				if( s.servers[0].isLocal ) {
					defs.do{ |def|
						def.uLoad( s.servers, dir: synthDefDir, makeSynthDesc: makeSynthDesc );
					}
					} {
					s.servers.do{ |s|
						defs.do(_.send(s))
					};
				}

			} {
				if( s.isLocal ) {
					defs.do(_.uLoad(s, dir: synthDefDir ? SynthDef.synthDefDir, makeSynthDesc: makeSynthDesc ));
				} {
					defs.do(_.send(s));
				};
			};
		}
	}

	sendSynthDef { |server|
		var defs;
		server = server ? ULib.servers ? Server.default;
		defs = this.synthDef.asCollection;
		server.asCollection.do{ |s|
			if( s.class == LoadBalancer ) {
				s.servers.do({ |s|
					defs.do(_.send(s));
				});
			} {
				defs.do(_.send(s));
			};
		}
	}

	writeDefFile {
		this.synthDef.asCollection.do( _.uWriteDefFile( dir: synthDefDir, makeSynthDesc: makeSynthDesc ) )
	}

	prepare { |servers, unit, action, startPos|
		if( prepareFunc.notNil ) {
			prepareFunc.value( servers, unit, action, startPos );
		} {
			action.value
		};
	}

	doPrepareFunc { |servers, unit, action, startPos|
		if( prepareFunc.notNil ) {
			prepareFunc.value( servers, unit, action, startPos );
		} {
			action.value
		};
	}

	needsPrepare { ^false }

	dispose { |unit|
		disposeFunc.value( unit );
	}

	disposeFor { |unit ...args|
		disposeForFunc.value( unit, *args );
	}

	stop { |unit|
		unit.synths.do(_.free);
	}

	isUdef { ^true }

	load { |server| this.loadSynthDef( server ) }
	send { |server| this.sendSynthDef( server ) }

	makeSynth { |unit, target, startPos = 0, synthAction|
	    var synth;
	    var started = false;
	    if( unit.shouldPlayOn( target ) != false ) {
		    /* // maybe we don't need this, or only at verbose level
		    if( unit.preparedServers.includes( target.asTarget.server ).not ) {
				"U:makeSynth - server % may not (yet) be prepared for unit %"
					.format( target.asTarget.server, this.name )
					.warn;
			};
			*/
			synth = this.createSynth( unit, target, startPos );
			if( synth.notNil ) {
				synth.startAction_({ |synth|
					unit.changed( \go, synth );
					started = true;
				});
				synth.freeAction_({ |synth|
					if( started == false ) { synth.changed( \n_go ) };
					unit.removeSynth( synth );
					synth.server.loadBalancerAddLoad( this.apxCPU.neg );
					unit.changed( \end, synth );
					if(unit.disposeOnFree) {
						unit.disposeArgsFor(synth.server)
					}
				});
				unit.changed( \start, synth );
				synthAction.value( synth );
				unit.addSynth(synth);
			};
		};
		^synth;
	}

	shouldPlayOn { |unit, server| // returns nil if no func
		^shouldPlayOnFunc !? { shouldPlayOnFunc.value( unit, server ); }
	}

	// I/O

	prGetIOKey { |mode = \in, rate = \audio ... extra|
		^([
			"u",
			switch( mode, \in, "i", \out, "o" ),
			switch( rate, \audio, "ar", \control, "kr" )
		] ++ extra).join( "_" );
	}

	prIOspecs { |mode = \in, rate = \audio, key, unit|
		key = key ?? { this.prGetIOKey( mode, rate ); };
		^this.argSpecs( unit ).select({ |item|
			var name;
			name = item.name.asString;
			name[..key.size-1] == key &&
			 	{ name[ name.size - 3 .. ] == "bus" };
		});
	}

	prIOids { |mode = \in, rate = \audio, unit|
		var key;
		key = this.prGetIOKey( mode, rate );
		^this.prIOspecs( mode, rate, key, unit ).collect({ |item|
			item.name.asString[key.size+1..].split( $_ )[0].interpret;
		});
	}

	audioIns { |unit| ^this.prIOids( \in, \audio, unit ); }
	controlIns { |unit| ^this.prIOids( \in, \control, unit ); }
	audioOuts { |unit| ^this.prIOids( \out, \audio, unit ); }
	controlOuts { |unit| ^this.prIOids( \out, \control, unit ); }

	prGetIOName { |mode = \in, rate = \audio, index = 0|
		if( this.ioNames.notNil ) {
			^this.ioNames[ mode, rate, index.asInteger ];
		} {
			^nil
		};
	}

	prSetIOName { |mode = \in, rate = \audio, index = 0, name|
		if( this.ioNames.isNil ) {
			this.ioNames = MultiLevelIdentityDictionary();
		};
		this.ioNames.put( mode, rate, index.asInteger, name );
	}

	audioInName { |index=0| ^this.prGetIOName( \in, \audio, index ); }
	controlInName { |index=0| ^this.prGetIOName( \in, \control, index ); }
	audioOutName { |index=0| ^this.prGetIOName( \out, \audio, index ); }
	controlOutName { |index=0| ^this.prGetIOName( \out, \control, index ); }

	prSetMultiIOName { |mode, rate, index, name|
		if( name.isString ) { name = [ name ] };
		name = name.asCollection.collect(_.asSymbol);
		index.asCollection.do({ |index, i|
			this.prSetIOName( mode, rate, index, name[i] );
		});
	}

	setAudioInName { |index, name| this.prSetMultiIOName( \in, \audio, index, name ); }
	setControlInName { |index, name| this.prSetMultiIOName( \in, \control, index, name ); }
	setAudioOutName { |index, name| this.prSetMultiIOName( \out, \audio, index, name ); }
	setControlOutName { |index, name| this.prSetMultiIOName( \out, \control, index, name ); }

	ioType { |unit|
		var hasIn, hasOut;
		hasIn = this.audioIns( unit ).size > 0;
		hasOut = this.audioOuts( unit ).size > 0;
		^case { hasIn && hasOut } { \modifier } { hasOut } { \generator } { hasIn } { \endpoint } { \other };
	}

	canFreeSynth { |unit| ^this.keys.includes( \u_doneAction ) }
		// assumes the Udef contains a UEnv

	// these may differ in subclasses of Udef
	createSynth { |unit, target, startPos = 0| // create A single synth based on server
		target = target ? Server.default;
		^Synth( this.synthDefName, unit.getArgsFor( target, startPos ), target, \addToTail );
	}

	setSpec { |name, spec, mode, constrainDefault = true, private|
		var asp;
		asp = this.getArgSpec(name);
		if( asp.notNil ) {
			if( spec.notNil ) { asp.spec = spec.asSpec; };
			if( mode.notNil ) { asp.mode = mode; };
			if( constrainDefault ) { asp.constrainDefault };
			if( private.notNil ) { asp.private = private };
		};
	}

	setSpecMode { |...pairs|
		pairs.pairsDo({ |name, mode|
			var asp;
			asp = this.getArgSpec( name );
			if( asp.notNil ) { asp.mode = mode };
		});
	}

	argSpecModes { // collect all modes into a dict
		var dict = IdentityDictionary();
		argSpecs.do({ |item| dict[ item.name ] = item.mode; });
		^dict;
	}
	setSynth { |unit ...keyValuePairs|
		this.prSetSynth( unit.synths, *keyValuePairs );
	}

	prSetSynth { |synths ...keyValuePairs|
		var syncIndices, normalIndices;
		var modes;
		syncIndices = Array(keyValuePairs.size);
		normalIndices = Array(keyValuePairs.size);
		modes = this.argSpecModes;
		keyValuePairs.pairsDo({ |key, value, index|
			var mode;
			mode = modes[key] ? \sync;
			switch( mode,
				\sync, { syncIndices.addAll( [ index, index + 1 ] ) },
				\normal, { normalIndices.addAll( [ index, index + 1 ] ) }
				// \init mode: ignore (only set at start of synth)
				// \nonsynth mode: ignore
			);
		});
		if( syncIndices.size > 0 ) {
			synths.asCollection.do({ |synth|
				this.prSetSynthSync( synth, *keyValuePairs[ syncIndices ] );
			});
		};
		if( normalIndices.size > 0 ) {
			synths.asCollection.do({ |synth|
				this.prSetSynthNormal( synth, *keyValuePairs[ normalIndices ] );
			});
		};
	}

	prSetSynthSync { |synth ...keyValuePairs|
		var server;
		server = synth.server;
		server.sendSyncedBundle( Server.default.latency, nil,
			*server.makeBundle( false, {
				this.prSetSynthNormal( synth, *keyValuePairs )			})
		);
	}

	prSetSynthNormal { |synth ...keyValuePairs|
		synth.set(*keyValuePairs.clump(2).collect{ |arr|
				[arr[0],arr[1].asControlInputFor(synth.server)]
			}.flatten
		);
	}

	synthsForUMap { |unit|
		// return only one synth per server
		var servers, synths;
		servers = Set();
		synths = [];
		unit.synths.reverseDo({ |item|
			if( servers.includes( item.server ).not ) {
				synths = synths.add( item );
				servers.add( item.server );
			};
		});
		^synths;
	}

	extraNames { ^nil }

	getInfoString {
		var str;
		if( infoString.isNil ) {
			if( this.filePath.notNil ) {
				File.use( this.filePath, "r", { |f|
					str = f.readAllString;
				});
				if( str.find( "/*" ).notNil ) {
					infoString = str[ str.find( "/*" ) + 3 .. str.find( "*/" ) - 1 ];
					^infoString
				} {
					^nil
				}
			} {
				^nil
			}
		} {
			^infoString;
		}
	}

	printOn { arg stream;
		stream << "a " << this.class.name << "(" <<* [this.name]  <<")"
	}

	storeOn { arg stream;
		stream << this.class.name << "(" <<* [
			this.name.asCompileString,
			func.asCompileString,
			argSpecs.asCompileString,
			category.asCompileString
		]  <<")"
	}

	asUdef { ^this }

	asUnit { |args| ^U( this, args ) }

}

U : ObjectWithArgs {

	classvar <>loadDef = false;
	classvar <>synthDict;
	classvar <>uneditableCategories;
	classvar <>preparedServersDict;

	var def, defName;
	//var <>synths;
	var <>disposeOnFree = true;
	//var <>preparedServers;
	var >waitTime; // use only to override waittime from args
	var <guiCollapsed = false;
	var <>parentChain;

	*initClass {
	    synthDict = IdentityDictionary( );
		preparedServersDict = IdentityDictionary( );
	    uneditableCategories = [];
	}

	*addUneditableCategory { |category|
	    uneditableCategories = uneditableCategories !? ( _.add(category) ) ? [category]
	}

	*new { |def, args|
		^super.new.init( def, args ? [] )
	}

	*defClass { ^Udef }

	*clear {
		synthDict.do({ |synths|
			synths.do({	 |synth| if( synth.isPlaying ) {
					synth.free
				} {
					synth.changed( \n_end );
				};
			});
		});
		synthDict = IdentityDictionary();
	}

	init { |in, inArgs|
		if( in.isKindOf( this.class.defClass ) ) {
			def = in;
			defName = in.name;
			if( defName.notNil && { defName.asUdef( this.class.defClass ) == def } ) {
				def = nil;
			};
		} {
			defName = in.asSymbol;
			def = nil;
		};
		if( this.def.notNil ) {
			args = this.def.asArgsArray( inArgs ? [], this );
		} {
			args = inArgs;
			"def '%' not found".format(in).warn;
		};
		this.preparedServers = nil;
		this.changed( \init );
	}
	allKeys { ^this.keys }
	allValues { ^this.values }

	uchainInit { |...args|
		this.def !? { |d| d.uchainInitFunc.value( this, *args ) };
		this.getUMaps.do(_.uchainInit(this, *args));
	}

	guiCollapsed_ { |bool = false|
		if( guiCollapsed != bool ) {
			guiCollapsed = bool;
			this.changed( \init );
		};
	}

    def {
        ^def ?? { defName.asUdef( this.class.defClass ) }
    }

    defName {
        ^defName ?? { def.name }
    }

    def_ { |newDef, keepArgs = true|
		this.deactivate;
        this.init( newDef, if( keepArgs ) { args } { [] }); // keep args
    }

    defName_ { |newDefName, keepArgs = true|
		this.deactivate;
        this.init( newDefName, if( keepArgs ) { args } { [] }); // keep args
    }

    subDef {
	    var df;
	    df = this.def;
	    while { df.respondsTo( \findUdefFor ) } {
		    df = df.findUdefFor( this );
	    };
	    ^df;
    }

    subDefNames {
	    var df, res;
	    df = this.def;
	    while { df.respondsTo( \findUdefFor ) } {
		    df = df.findUdefFor( this );
		    res = res.add( df.name );
	    };
	    ^res;
    }

    fullDefName {
	    ^([ this.defName ] ++ this.subDefNames ++ this.extraNames ).join( " / " );
    }

	extraNames {
		^this.def.extraNames( this );
	}

    checkDef {
	    if( this.def.notNil && { this.def.argNamesFor( this ) != this.argNames } ) {
		    this.init( this.def, args );
	    };
    }

    constrainArgs { |exclude|
	    	this.argSpecs.do({ |argSpec|
		    	var val;
		    	if( argSpec.private.not ) {
			    	val = this.get( argSpec.name );
			    	if( val.isUMap.not ) {
				    	this.setArg( argSpec.name, argSpec.spec.uconstrain( val ) );
			    	};
		    	}
		});
    }

    name {
	    if( this.def.respondsTo( \nameFunc ) && { this.def.nameFunc.notNil } ) {
		    ^this.def.nameFunc.value( this );
	    } {
		    ^this.defName;
	    };
    }

	set { |...args|
		var synthArgs;
		args.pairsDo({ |key, value|
			var ext, extid;
			ext = key.asString;
			extid = ext.find( "." );
			if( extid.notNil ) {
				key = ext[..extid-1].asSymbol;
				ext = ext[extid+1..];
				if( ext[0].isDecDigit ) {
					value = this.get( key ).put( ext.asInteger, value );
				} {
					value = this.get( key ).perform( ext.asSymbol.asSetter, value );
				};
			} {
				value = value.asUnitArg( this, key );
			};
			this.setArg( key, value );
			synthArgs = synthArgs.addAll( [ key, value ] );
		});
		this.def.setSynth( this, *synthArgs );
	}

	prSet { |...args| // without changing the arg
		this.def.setSynth( this, *args );
	}

	setConstrain { |...args|
		this.set(
			*args.clump(2).collect({ |item|
				var key, value;
				if( key.asString.includes($.) ) { // don't constrain in this case
					item
				} {
					#key, value = item;
					if( value.isUMap.not ) {
						[ key, this.getSpec( key ).uconstrain( value ) ]
					} {
						item;
					};
				}
			}).flatten(1)
		);
	}

	setSynth { |key, value|
		this.def.setSynth( this, [ key, value ?? { this.get( key ) } ] );
	}

	insertUMap { |key, umapdef, args|
		var item, umap;
		if( umapdef.isKindOf( UMap ) ) {
			umap = umapdef;
			umapdef = umap.def;
		} {
			umapdef = umapdef.asUdef( UMapDef );
			if( umapdef.notNil ) {
				umap = UMap( umapdef,  args );
			};
		};
		if( umap.notNil ) {
			if( umap.def.canInsert ) {
				item = this.get( key );
				if( item.isUMap ) {
					this.set( key, umap );
					this.get( key ).set( umapdef.insertArgName, item );
				} {
					item = this.mapGet( key );
					this.set( key, umap );
					this.get( key ).mapSet( umapdef.insertArgName, item );
				};
			} {
				this.set( key, umap );
			};
		};
	}

	removeUMap { |key|
		var umap, insertArg;
		umap = this.get( key );
		if( umap.isKindOf( UMap ) ) {
			umap.deactivate;
			if( umap.def.canInsert ) {
				insertArg = umap.get( umap.def.insertArgName );
				if( insertArg.isUMap ) {
					this.set( key, insertArg );
				} {
					this.mapSet( key, umap.mapGet( umap.def.insertArgName ) );
				};
			} {
				this.set( key, this.getDefault( key ).copy );
			};
		};
	}

	argNeedsUnmappedInput { ^false }

	get { |key|
		^this.getArg( key );
	}

	mapSet { |...args|
        var argsWithSpecs = args.clump(2).collect{ |arr|
            var key, value, spec;
            #key, value = arr;
            spec = this.getSpec(key);
            if( spec.notNil ) {
                [key, spec.map(value) ]
            } {
                [key, value ]
            }
        };
        this.set( * argsWithSpecs.flatten )
	}

	mapGet { |key|
		var spec = this.getSpec(key);
		^if( spec.notNil ) {
		    spec.unmap( this.get(key) )
		} {
		    this.get(key)
		}
	}

	setDur { |dur = inf|
		if( this.keys.includes( \u_dur ) ) {
			this.set( \u_dur, dur );
		};
		this.getUMaps.do(_.setDur(dur));
	}

	getDur {
		^this.get( \u_dur );
	}

	argSpecsForDisplay { |forceUMaps = true|
		var out, showOnCollapse;
		if( this.guiCollapsed ) {
			showOnCollapse = this.def !? _.showOnCollapse ? [];
			this.keys.do({ |key|
				var argSpec;
				if( showOnCollapse.includes( key ) or: {
					this.at( key ).isKindOf( UMap ) or: { this.at( key ).isKindOf( MassEditUMap ) };
				}) {
					argSpec = this.getArgSpec( key );
					if( argSpec.private.not ) {
						out = out.add( argSpec );
					};
				};
			});
			^out;
		} {
			^this.argSpecs.select({ |item| item.private.not })
		};
	}

	umapPerform { |what ...args|
		this.getUMaps.do({ |item|
			item.perform( what, *args );
		});
	}

	getUMaps {
		var res = [];
		this.values.do({ |item|
			if( item.isUMap && { res.includes( item ).not } ) {
				res = res.add( item );
			};
		});
		^res;
	}

	getAllUMaps {
		var umaps;
		this.getUMaps.do({ |item|
			umaps = umaps.add( item );
			umaps = umaps.addAll( item.getAllUMaps );
		});
		^umaps;
	}

	getUMapPath { |umap, inPath|
		var res = [];
		inPath = inPath ? [];
		this.args.pairsDo({ |key, value|
			if( value.class == umap.class ) {
				if( value === umap ) {
					^inPath ++ [ key ];
				};
			};
		});
		this.args.pairsDo({ |key, value|
			if( value.class == umap.class ) {
				res = res ++ [ value.getUMapPath( umap, inPath ++ [ key ] ) ];
			};
		});
		res = res.select(_.notNil);
		^res.first ? nil;
	}

	setUMapBuses {
		this.getAllUMaps.do({ |item|
			item.setUMapBus;
		});
	}

	getUMapBusNumChannels {
		^(this.getAllUMaps.select(_.hasBus).collect({ |item| item.def.numChannels }) ? []).sum;
	}

	canUseUMap { |key, umapdef|
		^this.def.canUseUMap == true &&
		{ umapdef.allowedModes.includes( this.getSpecMode( key ) ) && {
			this.getSpec( key ).isKindOf( UAdaptSpec ) or:
			{ umapdef.unitCanUseUMap( this, key ); }
			};
		}
	}

	resetStreams {
		this.args.pairsDo({ |key, item|
			if( item.isKindOf( UMap ) ) {
				item.resetStreams;
			};
		});
	}

	release { |releaseTime, doneAction| // only works if def.canFreeSynth == true
		var args;
		if( doneAction.notNil && { this.get( \u_doneAction ) != doneAction } ) {			args = [ \u_doneAction, doneAction ];
		};
		if( releaseTime.notNil ) {
			args = [ \u_fadeOut, releaseTime ] ++ args;
		};

		this.prSet( *args );
		this.set( \u_gate, 0 );
	}

	getArgsFor { |server, startPos = 0|
		server = server.asTarget.server;
		^this.class.formatArgs( this.getSynthArgs, server, startPos );
	}

	getSynthArgs {
		var nonsynthKeys;
		nonsynthKeys = this.argSpecs.select({ |item| item.mode == \nonsynth }).collect(_.name);
		^this.args.clump(2).select({ |item| nonsynthKeys.includes( item[0] ).not }).flatten(1);
	}

	getArgs { |selection|
		if( selection.notNil ) {
			selection = selection.asArray;
			^this.args.clump(2).select({ |item| selection.includes( item[0] ) }).flatten(1);
		} {
			^this.args;
		};
	}

	*formatArgs { |inArgs, server, startPos = 0|
		^inArgs.clump(2).collect({ |item, i|
			[ item[0], switch( item[0],
				\u_startPos, { startPos },
				\u_dur, { item[1] - startPos },
				\u_fadeIn, {
					if( startPos > 0 ) {
						(item[1] - startPos).max(0.025)
					} {
						item[1]
					};
				},
				{ item[1].asControlInputFor( server, startPos ) }
			) ];
		}).flatten(1);
	}

	getIOKey { |mode = \in, rate = \audio, id = 0, what = "bus"|
		^this.def.prGetIOKey( mode, rate, id, what ).asSymbol;
	}

	setAudioIn { |id = 0, bus = 0|
		this.set( this.getIOKey( \in, \audio, id ), bus );
	}
	setControlIn { |id = 0, bus = 0|
		this.set( this.getIOKey( \in, \control, id ), bus );
	}
	setAudioOut { |id = 0, bus = 0|
		this.set( this.getIOKey( \out, \audio, id ), bus );
	}
	setControlOut { |id = 0, bus = 0|
		this.set( this.getIOKey( \out, \control, id ), bus );
	}
	setAudioMixOutLevel { |id = 0, level = 0|
		^this.set( this.getIOKey( \out, \audio, id, "lvl" ), level );
	}
	setControlMixOutLevel { |id = 0, level = 0|
		^this.set( this.getIOKey( \out, \control, id, "lvl" ), level );
	}


	getAudioIn { |id = 0|
		^this.get( this.getIOKey( \in, \audio, id ) );
	}
	getControlIn { |id = 0|
		^this.get( this.getIOKey( \in, \control, id ) );
	}
	getAudioOut { |id = 0|
		^this.get( this.getIOKey( \out, \audio, id ) );
	}
	getControlOut { |id = 0|
		^this.get( this.getIOKey( \out, \control, id ) );
	}
	getAudioMixOutLevel { |id = 0|
		^this.get( this.getIOKey( \out, \audio, id, "lvl" ) );
	}
	getControlMixOutLevel { |id = 0|
		^this.get( this.getIOKey( \out, \control, id, "lvl" ) );
	}

	audioIns { ^this.def.audioIns( this ); }
	controlIns { ^this.def.controlIns( this ); }
	audioOuts { ^this.def.audioOuts( this ); }
	controlOuts { |unit| ^this.def.controlOuts( this ); }

	inputIsEndPoint { ^this.def.inputIsEndPoint( this ); }

	increaseIOs { |amt = 1|
		var audioIns = this.audioIns;
		var controlIns = this.controlIns;
		var audioOuts = this.audioOuts;
		var controlOuts = this.controlOuts;
		audioIns.do({ |item|
			this.setAudioIn(item, this.getAudioIn(item) + (audioIns.size * amt));
		});
		controlIns.do({ |item|
			this.setControlIn(item, this.getControlIn(item) + (controlIns.size * amt));
		});
		audioOuts.do({ |item|
			this.setAudioOut(item, this.getAudioOut(item) + (audioOuts.size * amt));
		});
		controlOuts.do({ |item|
			this.setControlOut(item, this.getControlOut(item) + (controlOuts.size * amt));
		});
	}

	canFreeSynth { ^this.def.canFreeSynth( this ) }

	shouldPlayOn { |target| // this may prevent a unit or chain to play on a specific server
		^this.def.shouldPlayOn( this, target );
	}

	doesNotUnderstand { |selector ...args|
		// bypasses errors; warning only if arg not found
		if( selector.isSetter ) {
			this.set( selector.asGetter, *args )
		} {
			^this.get( selector );
		};
	}

	// override methods from Object to support args with names 'loop' and 'rate'
	rate { ^this.get( \rate ) }
	rate_ { |new| this.set( \rate, new ) }
	loop { ^this.get( \loop ) }
	loop_ { |new| this.set( \loop, new ) }
	numChannels { ^this.get( \numChannels ) }
	numChannels_ { |new| this.set( \numChannels, new ) }
	value { ^this.get( \value ) }
	value_ { |new| this.set( \value, new ) }
	reset { ^this.get( \reset ) }

	cutStart { |amount = 0|
		this.values.do({ |value|
			if( value.respondsTo( \cutStart ) ) {
				value.cutStart( amount );
			};
		});
	}

	synths { ^synthDict[ this ] ? [] }

	synthsForUMap {
		^this.def.synthsForUMap( this );
	}

	synths_ { |synths| synthDict.put( this, synths ); }

	addSynth { |synth|
		 synthDict.put( this, synthDict.at( this ).add( synth ) );
	}

	removeSynth { |synth|
		var synths;
		synths = this.synths;
		synths.remove( synth );
		if( synths.size == 0 ) {
			 synthDict.put( this, nil );
		} {
			 synthDict.put( this, synths );
		};
	}

	makeSynth { |target, startPos = 0, synthAction|
		var synth;
		synth = this.def.makeSynth( this, target, startPos, synthAction );
		if( synth.notNil ) {
			this.umapPerform( \makeSynth, synth, startPos );
		};
	}

	makeBundle { |targets, startPos = 0, synthAction|
		^targets.asCollection.collect({ |target|
			target.asTarget.server.makeBundle( false, {
			    this.makeSynth(target, startPos, synthAction)
			});
		})
	}

	preparedServers {
		^preparedServersDict[ this ];
	}

	preparedServers_ { |preparedServers|
		if( preparedServers == [] ) { preparedServers = nil };
		preparedServersDict[ this ] = preparedServers;
	}

	start { |target, startPos = 0, latency|
		var targets, bundles;
		target = target ? this.preparedServers ? Server.default;
		targets = target.asCollection;
		latency = latency ? 0.2;
		bundles = this.makeBundle( targets, startPos );
		targets.do({ |target, i|
			if( bundles[i].size > 0 ) {
				target.asTarget.server.sendSyncedBundle( latency, nil, *bundles[i] );
			};
		});
		if( target.size == 0 ) {
			^this.synths[0]
		} {
			^this.synths;
		};
	}

	free {
		this.def.stop( this );
		this.umapPerform( \free );
	}
	stop { this.free }

	resetSynths { this.synths = nil; } // after unexpected server quit
	resetArgs {
		this.values = this.def.values.deepCopy;
		this.def.setSynth( this, *args );
	}

	argSpecs { ^this.def.argSpecs( this ) }
	getSpec { |key| ^this.def.getSpec( key, this ); }
	getArgSpec { |key| ^this.def.getArgSpec( key, this ) }
	getSpecMode { |key| ^this.def.getArgSpec( key, this ) !? _.mode }
	getDefault { |key| ^this.def.getDefault( key, this ); }

	getAllArgsForSpecClasses { |specs, searchUMaps = true|
		var out = [];
		if( specs.isArray.not ) { specs = [ specs ] };
		this.args.pairsDo({ |key, val|
			if( searchUMaps ) {
				if( val.isUMap ) {
					out = out.addAll( val.getAllArgsForSpecClasses( specs, true ) );
				} {
					if( specs.includes( this.getSpec( key ).class ) ) {
						out = out.add( val );
					};
				}
			} {
				if( specs.includes( this.getSpec( key ).class ) ) {
					out = out.add( val );
				};
			}
		});
		^out;
	}

	indexOf { |obj|  // returns key of first arg found to be equal to object
		var keyIndex;
		keyIndex = this.values.indexOf( obj );
		^keyIndex !? { |x| this.keys[x] }
	}

	deepIndexOf { |obj|  // returns key of first arg found to be equal to object
		var key, val;
		^this.indexOf( obj ) ?? {
			this.args.pairsDo({ |key, value|
				var res;
				if( value.isUMap ) {
					res = value.deepIndexOf( obj );
					if( res.notNil ) {
						^[ key ] ++ res.asCollection;
					};
				};
			});
			nil
		};
	}

	isPlaying { ^(this.synths.size != 0) }

	printOn { arg stream;
		stream << this.class.name << "( " <<* this.argsForPrint  <<" )"
	}

	dontStoreArgNames { ^[ 'u_dur', 'u_doneAction', 'u_gate' ]
		++ this.def.dontStoreArgNames.value( this );
	}

	getInitArgs {
		var defArgs;
		defArgs = (this.def.args( this ) ? []).clump(2);
		^args.clump(2).select({ |item, i|
			(item != defArgs[i]) && { this.dontStoreArgNames.includes( item[0] ).not };
		 }).collect({ |item|
			 var umapArgs;
			 if( item[1].isUMap ) {
				 umapArgs = item[1].storeArgs;
				 if( umapArgs.size == 1 ) {
				 	[ item[0], umapArgs[0] ]
				 } {
					 [ item[0], umapArgs ]
				 };
			 } {
				 item
			 };
		 }).flatten(1);
	}

	argsForPrint {
        var initArgs, initDef;
        initArgs = this.getInitArgs;
        initDef = this.defName;
        if( initArgs.size > 0 ) {
            ^[ initDef, initArgs ];
        } {
            ^[ initDef ];
        };
    }

	storeArgs {
		var initArgs, initDef;
		initArgs = this.getInitArgs;
		initDef = if( this.def.class.callByName ) {
		    this.defName
		} {
		    this.def
		};
		if( (initArgs.size > 0) ) {
			^[ initDef, initArgs ];
		} {
			^[ initDef ];
		};
	}

	getSetArgs {
		var storeArgs;
		storeArgs = this.storeArgs;
		if( storeArgs.size == 1 ) {
			^storeArgs[0];
		} {
			^storeArgs;
		};
	}

	asUnit { ^this }

	prSyncCollection { |targets|
        targets.asCollection.do{ |t|
	        t.asTarget.server.sync;
	    };
	}

	waitTime { ^waitTime ?? { this.values.collect( _.u_waitTime ).sum } }

	valuesAsUnitArg {
		this.args.pairsDo({ |key, value| value.asUnitArg( this, key ); });
	}

	valuesSetUnit {
		this.args.pairsDo({ |key, value|
			if( value.respondsTo( \unit_ ) ) {
				value.unit = this;
			}
		});
	}

	valuesToPrepare {
		^this.values.select( _.respondsTo(\prepare) );
	}

	needsPrepare {
		^this.valuesToPrepare.size > 0 or: {
			this.subDef.needsPrepare;
		};
	}

	apxCPU { |target|
		if( target.isNil or: { this.shouldPlayOn( target.asTarget ) ? true } ) {
		 	^this.def.apxCPU
		 } {
			 ^0
		 };
	}

	prepare { |target, startPos = 0, action|
		var valuesToPrepare, act, firstAct, servers;

		parentChain = UChain.nowPreparingChain;

		target = target.asCollection.collect{ |t| t.asTarget( this.apxCPU ) };
		target = target.select({ |tg|
			this.shouldPlayOn( tg ) != false;
		});
		servers = target.collect(_.server);
		this.set( \u_gate, 1 );
		if( target.size > 0 ) {
	   	    act = { this.preparedServers = this.preparedServers.addAll( servers ); action.value };
		    if( loadDef) {
		        this.def.loadSynthDef( servers );
		    };
		    this.valuesSetUnit;
		    valuesToPrepare = this.valuesToPrepare;
		    if( valuesToPrepare.size > 0  ) {
			    act = MultiActionFunc( act );
			    firstAct = act.getAction;
			    valuesToPrepare.do({ |val|
				     val.prepare(servers, startPos, action: act.getAction)
			    });
			    this.def.prepare(servers, this, act.getAction, startPos);
			    firstAct.value;
		    } {
			    this.def.prepare(servers, this, act, startPos);
		    };
		} {
			this.def.doPrepareFunc( servers, this, act, startPos);
		};
		this.setUMapBuses;
	    ^target; // returns targets actually prepared for
    }

    prepareAnd { |target, action|
	    fork{
	        target = this.prepare(target);
	        this.prSyncCollection(target);
	        action.value( this, target );
	    }
    }

	prepareAndStart { |target|
	   this.prepareAnd( target, _.start(_) );
	}

	loadDefAndStart { |target|
	    fork{
	        this.def.loadSynthDef(target.collect{ |t| t.asTarget.server });
	        this.prSyncCollection(target);
	        this.start(target);
	    }
	}

	dispose {
	    this.free;
	    this.values.do{ |val|
	        if(val.respondsTo(\dispose)) {
	            val.dispose
	        }
	    };
	    this.preparedServers = nil;
	}

	disposeSynths {
		this.synths.copy.do(_.changed( \n_end ));
		  this.values.do{ |val|
	        if(val.respondsTo(\disposeSynths)) {
	            val.disposeSynths;
	        }
	    };
	}

	disposeArgsFor { |server|
		this.def !? _.disposeFor( this, server );
	    this.values.do{ |val|
	        if(val.respondsTo(\disposeFor)) {
	            val.disposeFor(server)
	        }
	    };
	    this.preparedServers.remove( server );
	    if( this.preparedServers.size == 0 ) {
		    parentChain = nil; // forget chain after disposing last server
			this.preparedServers = nil;
	    };
	}

	selectUMaps { |selectFunc|
		var f, c, selected = [];
		selectFunc = selectFunc ? true; // select all
		f = { |unit|
			unit.values.do({ |value|
				if( value.isUMap ) {
					if( selectFunc.( value ) == true ) {
						selected = selected ++ [ value ];
					};
					f.( value );
				};
			});
		};
		f.( this );
		^selected;
	}

	setUMapsActive { |active = true, verbose = true|
		var umaps;

		umaps = this.selectUMaps({ |umap|
			umap.keys.any(_ == \active);
		});

		umaps.do( _.set( \active, active ) );

		if( verbose ) {
			"% UMaps set to 'active' = %\n".postf( umaps.size, active );
		};
	}

	deactivateUMaps { |verbose = true|
		var umaps;

		umaps = this.selectUMaps({ |umap|
			umap.keys.any(_ == \active) && { umap.active == true }
		});

		umaps.do( _.deactivate );

		if( verbose && { umaps.size > 0 } ) {
			"% UMap% deactivated for %\n".postf(
				umaps.size,
				if( umaps.size > 1 ) { "s" } { "" },
				this
			);
		};
	}

	activate {
		if( this.subDef.respondsTo( \activateUnit ) ) {
			this.subDef.activateUnit( this );
		}
	}

	deactivate {
		if( this.subDef.respondsTo( \deactivateUnit ) ) {
			this.subDef.deactivateUnit( this );
		};
	}

	getGlobalBufferIDs {
		^this.getAllArgsForSpecClasses( [ BufSndFileSpec, MonoBufSndFileSpec, MultiSndFileSpec ] )
		.flatten(1).select(_.hasGlobal).collect(_.id);
	}
}

+ Object {
	asControlInputFor { |server, startPos| ^this.asControlInput } // may split between servers
	u_waitTime { ^0 }
	asUnitArg { |unit| ^this }
	isUdef { ^false }
}

+ Function {
	asControlInputFor { |server, startPos| ^this.value( server, startPos ) }
}

+ Symbol {
	asUnit { |args| ^U( this, args ) }
	asUdef { |defClass| ^(defClass ? Udef).fromName( this ); }
	asUnitArg { |unit, key|
		var umapdef, umap;
		if( unit.getSpec( key ).isNil or: {
			unit.getSpec( key ).default.isMemberOf( Symbol ).not
		}) {
			umapdef = this.asUdef( UMapDef );
			if( umapdef.isNil && { unit.getSpec( key ).isKindOf( UAdaptSpec ) } ) {
				^this;
			};
			if( unit.canUseUMap( key, umapdef ) ) {
				^UMap( this ).asUnitArg( unit, key );
			} {
				^this;
			};
		} {
			^this;
		};
	}
}

+ Array {
	asUnit {
		^if( this[0].isKindOf(SimpleNumber) ) {
			UX(this[0], this[1], *this[2..])
		}{
			U( this[0], *this[1..] )
		}
	}
	asUnitArg { |unit, key|
		var umapdef, umap;
		if( ( this[0].isMemberOf( Symbol ) or: this[0].isKindOf( UMapDef ) ) && {
			this[1].isArray
		} ) {
			umapdef = this[0].asUdef( UMapDef );
			if( umapdef.notNil && { unit.canUseUMap( key, umapdef ) } ) {
				^UMap( *this ).asUnitArg( unit, key );
			} {
				^unit.getDefault( key );
			};
		} {
			^this;
		};
	}
}
