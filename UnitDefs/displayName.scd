/*
\displayName

A simple Udef for showing a custom name in the Score editor. It produces no sound.

name: the name to display (String)

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

FreeUdef( \displayName, [
		[ \name, "name", StringSpec().default_( "name" ), false, \init ],
	])
	.apxCPU_( 0 )
	.createsSynth_( false )
	.nameFunc_({ |unit|
		unit.get( \name );	
	})
	.category_( 'utility' );