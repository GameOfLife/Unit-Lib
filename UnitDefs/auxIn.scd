/*
\auxIn

Read sound from one of the 32 internal 'aux' buses. The signal can be sent from a different UChain using 'auxOut'. For this to work on every system, set the receiving UChain's addAction to \addToTail or \addAfter, and enable 'global'.

bus: the aux bus to read from (0-31)

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

Udef( \auxIn, { |bus = 0|
	var input;
	input = In.ar( bus + FirstPrivateBus.ir + 32 );
	UOut.ar( 0, input );
} ).setSpec( \bus, PositiveIntegerSpec( 0, 0, 32 ) )
	.category_( \io );