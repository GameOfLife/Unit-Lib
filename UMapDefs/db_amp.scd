/*
\db_amp

Creates an UMap for converting db values to amplitude

db: the db value
mute: if true amp becomes zero

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

HybridUMapDef( \db_amp, { |db = 64, mute = 0|
	UMapDef.useMappedArgs = false; // use raw values instead of mapped (0-1)
	UMapOut.kr( db.dbamp * (1-mute) );
}, { |unit, db = 64, mute = 0|
	 db.dbamp * (1-mute.binaryValue)
}
)
	.mappedArgs_([ \db ])
	.setSpec( \db, UAdaptSpec({ |spec|
		ControlSpec( spec.minval.max(-96.dbamp).ampdb, spec.maxval.max(-96.dbamp).ampdb, \lin, 0, 0 );
	}) )
	.setSpec( \mute, BoolSpec(false) )
	.canUseUMapFunc_({ |unit, key, umapdef|
		var spec;
		spec = unit.getSpec( key );
		spec.isKindOf( ControlSpec ) && { (spec.minval == 0) && { spec.maxval < inf } };
	})
	.category_( 'convert' );