UReplyKey {

	classvar <tag = '/u_reply';
	classvar <currentID = 0;
	classvar <dict, <specs, <storedSpecs, <storedSizes;
	classvar <running = false;

	var <key, <spec;

	*initClass {
		dict = IdentityDictionary();
		specs = IdentityDictionary();
		storedSpecs = IdentityDictionary();
		storedSizes = IdentityDictionary();
	}

	*register { |key, spec|
		if( key.isKindOf( Symbol ) ) {
			if( dict[ key ].isNil ) {
				this.put( key, currentID );
				currentID = currentID+1;
			};
			if( spec.notNil or: { specs[ key ] != spec } ) {
				specs[ key ] = spec.asSpec;
			};
		};
	}

	*new { |key, spec|
		^super.newCopyArgs( key, spec ).init;
	}

	*fromID { |id|
		^this.new( this.asKey( id ) );
	}

	init {
		if( spec.isNil ) {
			spec = this.class.getSpec( key );
		};
		spec = spec.asSpec;
		this.class.register( key, spec );
		this.class.startOSC;
	}

	id { ^this.class.asID( tag ) }

	key_ { |newKey|
		key = newKey;
		this.class.register( key, spec );
		this.changed( \key, key );
	}

	spec_ { |newSpec|
		spec = newSpec.asSpec;
		this.class.register( key, spec );
	}

	asControlInput { ^this.class.asID( key ) ? -1 }

	asUGenInput { ^this.class.asID( key ) ? -1 }

	asOSCArgEmbeddedArray { |array| ^this.asControlInput.asOSCArgEmbeddedArray(array) }

	*registeredTags {
		^dict.keys.select(_.isKindOf( Symbol ));
	}

	*registeredIDs {
		^dict.keys.select(_.isKindOf( Integer ));
	}

	*startOSC {
		if( running != true ) {
			OSCdef( this.tag, { |msg|
				this.setEnvir( msg[2], msg[3..] );
			}, this.tag ).permanent_( true );
			running = true;
		};
	}

	*endOSC {
		OSCdef( this.tag ).free;
		running = false;
	}

	*kr { |trig, value, id|
		id = id ? \key;
		if( id.isKindOf( Symbol ) ) {
			id = id.ukr(-1, UReplyKeySpec() );
		};
		^SendReply.kr( trig, this.tag, value, id.asUGenInput );
	}

	*ar { |trig, value, id|
		id = id ? \key;
		if( id.isKindOf( Symbol ) ) {
			id = id.ukr(-1, UReplyKeySpec() );
		};
		^SendReply.ar( trig, this.tag, value, id.asUGenInput );
	}

	*put { arg key, id;
		dict.put(id, key);
		dict.put(key, id);
	}

	*removeAt { arg key;
		var id;
		id = dict.at( key );
		dict.removeAt(key);
		dict.removeAt(id);
	}

	*remove { arg inID;
		this.removeAt( inID );
	}

	*at { |key|
		^dict.at( key );
	}

	*asKey { |something|
		switch( something.class,
			Symbol, { ^something },
			String, { ^something.asSymbol },
			Integer, { ^dict.at( something ) },
			{ ^nil }
		);
	}

	*asID { |something|
		switch( something.class,
			Symbol, { ^dict.at( something ) },
			String, { ^dict.at( something.asSymbol ) },
			Integer, { ^something },
			{ ^nil }
		);
	}

	*getSpec { |key|
		key = this.asKey( key );
		^specs[ key ];
	}

	*setEnvir { |id, value|
		var key, spec;
		key = this.asKey( id );
		if( value.size == 1 ) { value = value[0] };
		if( key.notNil ) {
			if( specs[ key ].isNil ) {
				specs[ key ] = [0,1].asSpec;
			};
			spec = specs[ key ];
			if( value.size > 0 ) {
				value = value.collect({ |val|
					spec.constrain( val );
				});
			} {
				value = spec.constrain( value );
			};
			if( (storedSpecs[ key ] == spec) and: {
				storedSizes[ key ] == value.size;
			}) {
				if( key.envirGet != value ) { key.uEnvirPut( value ); };
			} {
				key.uEnvirPut( value, spec );
				storedSpecs[ key ] = spec;
				storedSizes[ key ] = value.size;
			}
		};
	}

	storeArgs { ^[ key ] }

	asUReplyKey { |spec|
		if( spec.notNil ) {
			^this.spec_( spec );
		} {
			^this
		};
	}

}

UReplyKeySpec {

	var spec;

	*new { |spec|
		^super.newCopyArgs( spec )
	}

	asSpec { ^this }

	default { ^UReplyKey(spec: spec) }

	massEditSpec { ^nil }

	findKey {
		^Spec.specs.findKeyForValue(this);
	}

	constrain { |value|
		^value.asUReplyKey( spec );
	}

	map { |value| ^value }
	unmap { |value| ^value }

	viewNumLines { ^1 }

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
					action.value( vws, this.constrain( tf.value.asSymbol ) );
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
					action.value( vws, this.constrain( item.asSymbol ) );
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
		var string;
		value = this.constrain( value );
		string = (value.key ?? "").asString;
		{
			view[ \string ].value = string;
			view[ \setColor ].value;
			view[ \menu ].value = view[ \menu ].items.indexOfEqual( "~" ++ string ) ? 0;
		}.defer;
		if( active ) { view[ \string ].doAction };
	}
}

+ Symbol {

	asUReplyKey { |spec|
		^UReplyKey( this, spec );
	}
}

+ Object {

	asUReplyKey { |spec|
		^UReplyKey( nil, spec );
	}
}