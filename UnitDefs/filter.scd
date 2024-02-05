/*
\filter

A set of basic filters. Arguments vary per type, but all of them have a \freq argument. Please note that the Udefs with the filters are saved as separate files in the 'private' category.

type: possible types; \lowPass, \highPass, \bandPass, \bandStop, \allPass, \lowShelf, \highShelf, \peak

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

MultiUdef( \filter,
	[ \lowPass, \highPass, \bandPass, \bandStop, \allPass, \lowShelf, \highShelf, \peak ].collect(_.asUdef), \filter, \type, false );