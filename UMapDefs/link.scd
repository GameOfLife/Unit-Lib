FuncUMapDef( \link, { |unit, u_store, url|
	u_store.spec = unit.spec;
	u_store.url = url;
	u_store;
}, [ [ \u_store, ULink(), ULinkSpec(), true ], [ \url, "", StringSpec("") ] ], valueIsPrivate: true )
	.valueIsMapped_( false )
	.dontStoreValue_(true)
	.setSpec( \value, ULinkSpec() )
	.category_( 'input' )