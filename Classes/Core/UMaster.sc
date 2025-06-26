UMaster {

	// ULibMaster holds UEvent objects (UScore, UChain etc.) that are to be played
	// continuously. An example is binaural or ambisonic decoding chains or sound
	// pre-processing chains. All objects in the objects dictionary will be started
	// immediately after the server has booted and restart after cmd-..
	// Objects need to respond accordingly to .prepareAndStart and .stop and typically
	// would have infinite duration.
	//
	// They will also be included in any NRT rendering, unless explicitly excluded.
	// objects is a dictionary with user-definable keys to be able to keep track
	// of variants etc.

	classvar <>objects;
	classvar <hasStarted = false;
	classvar <isRunning = false;
	classvar <>verbose = true;

	*initClass {
		objects = IdentityDictionary();
	}

	*isRunning_ { |bool = true|
		isRunning = bool;
		this.changed( \isRunning, bool );
	}

	*hasStarted_ { |bool = true|
		hasStarted = bool;
		this.changed( \hasStarted, bool );
	}

	*put { |key, object|
		var oldObject;
		oldObject = objects.removeAt( key );
		objects.put( key, object );
		if( this.isRunning ) {
			oldObject.stop;
			object !? _.prepareAndStart;
			if( verbose ) {
				if( oldObject.notNil ) {
					"stopping UMaster for '%'\n".postf( key );
				};
				if( object.notNil ) {
					"starting UMaster for '%'\n".postf( key );
				};
			};
		};
	}

	*removeAt { |key|
		var oldObject;
		oldObject = objects.removeAt( key );
		if( this.isRunning ) {
			oldObject.stop;
			if( verbose ) {
				if( oldObject.notNil ) {
					"stopping UMaster for '%'\n".postf( key );
				}
			};
		};
		^oldObject;
	}

	*startObjectsIfRunning {
		if( ULib.allServers.every(_.serverRunning) ) {
			this.startObjects;
		};
	}

	*startObjects {
		objects.keysValuesDo({ |key, value|
			value.prepareAndStart;
			if( this.verbose ) { "starting UMaster for '%'\n".postf( key ); };
		});
		this.isRunning = true;
	}

	*stopObjects {
		objects.keysValuesDo({ |key, value|
			value.stop;
			if( this.verbose ) { "stopping UMaster for '%'\n".postf( key ); };
		});
		this.isRunning = false;
	}

	*doOnServerTree {
		{ this.startObjectsIfRunning; }.defer(0.1);
	}

	*doOnServerQuit {
		this.stopObjects;
	}

	*start { |force = false|
		ServerTree.add( this );
		ServerQuit.add( this );
		if( this.hasStarted.not or: { force == true } ) {
			this.startObjectsIfRunning;
			this.hasStarted = true;
		};
	}

	*stop {
		ServerTree.remove( this );
		ServerQuit.remove( this );
		this.stopObjects;
		this.hasStarted = false;
	}

	*collectOSCBundleFuncs { |server, startOffset = 0, infdur = 60|
		var array;
		if( this.objects.size > 0 ) {
			server = server ? Server.default;

			array = objects.values.collectAs({ |item|
				item.deepCopy.collectOSCBundleFuncs( server, item.startTime + startOffset, infdur );
			}, Array).flatten(1);

			^array.sort({ |a,b| a[0] <= b[0] });
		} {
			^[];
		};
	}
}