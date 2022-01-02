UMapOut {

	*kr { |channelsArray, map = true, clip = true|
		var spec;
		ReplaceOut.kr(
			UMap.busOffset + \u_mapbus.ir(0),
			if( map ) {
				Udef.addBuildSpec(
					ArgSpec(\u_spec, [0,1,\lin].asSpec, ControlSpecSpec(), true, \init )
				);
				Udef.addBuildSpec(
					ArgSpec(\u_useSpec, true, BoolSpec(true), true, \init )
				);
				spec = \u_spec.kr([0,1,1,-2,0]);
				if( UMapDef.useMappedArgs ) {
					Select.kr( \u_useSpec.ir(1), [
						if( clip ) { channelsArray.clip( 0, 1 ) } { channelsArray },
						spec.asSpecMapKr( channelsArray )
					]);
				} {
					Select.kr( \u_useSpec.ir(1), [
						spec.asSpecUnmapKr( channelsArray ),
						if( clip ) {
							channelsArray.clip( spec[0], spec[1] )
						} { channelsArray }
					]);
				};
			} { channelsArray };
		);
		Udef.buildUdef.numChannels = channelsArray.asCollection.size;
		Udef.buildUdef.outputIsMapped = map;
		Udef.addBuildSpec(
			ArgSpec(\u_mapbus, 0, PositiveIntegerSpec(), true, \init )
		);
	}

}