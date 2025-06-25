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
	classvar <isRunning = false;

	*initClass {
		objects = IdentityDictionary();
	}

	*isRunning_ { |bool = true|
		isRunning = bool;
		this.changed( \isRunning, bool );
	}

	*put { |key, object|
		var oldObject;
		oldObject = objects.removeAt( key );
		objects.put( key, object );
		if( this.isRunning ) {
			oldObject.stop;
			object !? _.prepareAndStart;
		};
	}

	*removeAt { |key|
		var oldObject;
		oldObject = objects.removeAt( key );
		if( this.isRunning ) {
			oldObject.stop;
		};
		^oldObject;
	}

	*startObjects {
		objects.do(_.prepareAndStart);
	}

	*stopObjects {
		objects.do(_.stop);
	}

	*doOnServerTree {
		{ this.startObjects; }.defer(0.1);
	}

	*doOnServerQuit {
		this.stopObjects;
	}

	*start {
		ServerTree.add( this, \default );
		ServerQuit.add( this, \default );
		this.startObjects;
		this.isRunning = true;
	}

	*stop {
		ServerTree.remove( this );
		ServerQuit.remove( this );
		this.stopObjects;
		this.isRunning = false;
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