GroupWithChildren : Group {

	var <>children;

	addChild { |child|
		children = children.add( child );
	}

	removeChild { |child|
		^children.remove( child );
	}

	at { |index| ^children.at( index ) }

}