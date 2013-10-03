/* 
example:

(
UChain([ 'sine', [ 'freq', [ 'lfo_sine', [ 'range', [ 100, 10000 ] ] ] ] ], [ 'saw', [ 'freq', [ 'link', [ 'url', "0/freq" ] ], 'u_o_ar_0_bus', 1 ] ], [ 'output', [ 'numChannels', 2 ] ]).gui;
)

the example above links the 'freq' parameter of the first unit to that of the second unit. They now both use the same freq. Linking can only done to parameters earlier in the chain.

* this feature is still under development; it can be used, but there can be unknown limitations *

*/

FuncUMapDef( \link, { |unit, u_store, url|
	u_store.spec = unit.spec;
	u_store.url = url;
	u_store;
}, [ [ \u_store, ULink(), ULinkSpec(), true ], [ \url, "", StringSpec("") ] ], valueIsPrivate: true )
	.valueIsMapped_( false )
	.dontStoreValue_(true)
	.setSpec( \value, ULinkSpec() )
	.category_( 'input' )