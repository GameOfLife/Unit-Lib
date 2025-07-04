/*
    Unit Library
    The Game Of Life Foundation. http://gameoflife.nl
    Copyright 2006-2011 Miguel Negrao, Wouter Snoei.

    GameOfLife Unit Library: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    GameOfLife Unit Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with GameOfLife Unit Library.  If not, see <http://www.gnu.org/licenses/>.
*/

ListSpec : Spec {
	classvar <>modern = true;

	var <list;
	var <>defaultIndex = 0;
	var sortedList, indexMap;
	var <>labels;

	// handles only Symbols and Numbers, no repetitions

	*new { |list, defaultIndex = 0, labels|
		^super.newCopyArgs( list ? [] ).init.defaultIndex_( defaultIndex ).labels_( labels );
	}

	init {
		var tempList;
		tempList = list.collect({ |item|
			if( item.isNumber.not ) {
				item.asSymbol;
			} {
				item;
			};
		});
		sortedList = tempList.copy.sort;
		indexMap = sortedList.collect({ |item| tempList.indexOfEqual( item ); });
	}

	default { ^this.at( defaultIndex ) }
	default_ { |value| defaultIndex = this.unmap( value ); }

	at { |index| ^list.at( index ); }
	put { |index, value| list.put( index, value ); this.init; }
	add { |value| list = list.add( value ); this.init; }
	remove { |value| list.remove( value ); this.init; }

	list_ { |newList| list = newList; this.init }

	constrain { |value|
		^list[ this.unmap( value ) ];
	}

	unmap { |value|
		var index;
		index = list.indexOf( value ); // first try direct (faster)
		if( index.notNil ) {
			^index;
		} {
			if( value.isNumber.not ) { value = value.asSymbol; };
			^indexMap[ sortedList.indexIn( value ) ] ? defaultIndex;
		};
	}

	map { |value|
		^list[ value.asInteger ] ?? { list[ defaultIndex ] };
	}

	storeArgs { ^[list, defaultIndex] }

}

ArraySpec : Spec {
	// spec for array with any type of value
	var <>size;
	var <>originalSpec;

	constrain { |value|
		value = value.asArray;
		if( originalSpec.notNil ) {
			value = value.collect({ |x|
				originalSpec.constrain(x);
			});
		};
		if( size.notNil ) {
			^value.wrapExtend( size );
		} {
			^value
		}
	}

	uconstrain { |value| ^this.constrain( value ? this.default ); }

	*testObject { |obj|
		^obj.isArray
	}

	map { |value|
		^originalSpec !? _.map( value ) ? value;
	}

	unmap { |value|
		^originalSpec !? _.unmap( value ) ? value;
	}

}

ArrayControlSpec : ControlSpec {
	// spec for an array of values
	var <>size;
	var <>originalSpec;
	var <>sliderSpec;

	asRangeSpec {
		^RangeSpec.newFrom( this ).default_( this.default.asCollection.wrapAt([0,1]) );
	}
	asControlSpec { ^ControlSpec.newFrom( this ).default_( this.default.asCollection[0] ); }
	asArrayControlSpec { ^this }

	copy {
		^this.class.newFrom(this).originalSpec_( originalSpec.copy ).size_( size );
	}

	constrain { |value|
		var ctrlSpec = this.asControlSpec;
		if( value.isArray.not ) { value = [ value ] };
		if( size.notNil ) {
			^value.collect({ |x| ctrlSpec.constrain(x) })
				.wrapExtend( size );
		} {
			^value.collect{ |x| ctrlSpec.constrain(x) }
		}
	}

	map { arg value;
		// maps a value from [0..1] to spec range
		value = value.asArray;
		if( size.notNil ) {
			value = value.wrapExtend( size );
		};
		if( originalSpec.isNil ) {
			^warp.map(value.clip(0.0, 1.0)).round(step);
		} {
			^value.collect({ |val|
				originalSpec.map( val );
			})
		};
	}

	uconstrain { |value| ^this.constrain( value ? this.default ); }

	*testObject { |obj| ^obj.isArray && { obj.every(_.isNumber) } }

	storeModifiersOn { |stream|
		[ \size, \originalSpec ].do({ |key|
			if( this.perform( key ).notNil ) {
				stream << ".%_(".format( key ) <<< this.perform( key ) << ")";
			};
		});
	}
}

GenericMassEditSpec : Spec {
	var <>originalSpec;
	var <>size;
	var <>default;
	var <>operations;

	constrain { |value|
		if( size.notNil ) {
			^value.asCollection.collect({ |x| originalSpec.constrain(x) })
				.wrapExtend( size );
		} {
			^value.asCollection.collect{ |x| originalSpec.constrain(x) }
		}
	}

	map { |value| ^value }
	unmap { |value| ^value }

	storeModifiersOn { |stream|
		this.instVarDict.keysValuesDo({ |key, value|
			stream << ".%_(".format( key ) <<< value << ")";
		});
	}

}

StringSpec : Spec {

	var <>default = "";

	constrain { |value| ^(value ? default).asString }

	*testObject { |obj|
		^obj.isString
	}

	map { |value| ^value.asString }
	unmap { |value| ^value }

}

IPSpec : StringSpec {
	var <>default = "127.0.0.1";
}

SymbolSpec : StringSpec {

	var <>default = '';

	constrain { |value| ^(value ? default).asSymbol }

	*testObject { |obj|
		^obj.isKindOf( Symbol )
	}

	map { |value| ^value.asSymbol }

}

EnvirSpec : SymbolSpec {
}

MapSpec : Spec {

	var <>default = 'c0';

	constrain { |value| ^(value ? default).asSymbol }

	*testObject { |obj|
		^obj.class == Symbol
	}

	map { |value| ^value }
	unmap { |value| ^value }


}

AnythingSpec : Spec {
	var <>default;

	constrain { |value| ^value }

	*testObject { |obj|
		^true
	}

	map { |value| ^value }
	unmap { |value| ^value }
}

SMPTESpec : Spec {

	var <>minval = 0, <>maxval = inf;
	var <>fps = 1000;
	var <>default = 0;

	*new { |minval = 0, maxval = inf, fps = 1000, default = 0|
		^super.newCopyArgs.minval_( minval ).maxval_( maxval ).fps_( fps ).default_( default );
	}

	constrain { |value|
		if( value.isNumber.not ) { value = default };
		^value.clip( minval, maxval );
	}

	map { |value| ^value }
	unmap { |value| ^value }

	asControlSpec { ^ControlSpec( minval, maxval, \lin, 0, default ) }

	storeArgs { ^[minval, maxval, fps] }
}

TriggerSpec : Spec {
	var <>label, <>spec;

	*new { |label, spec|
		^super.newCopyArgs( label, spec );
	}

	default { ^(spec !? _.default) ? 1 }

	map { |value|
		^value;
	}

	unmap { |value|
		^value;
	}

	constrain { |value|
		^value;
	}

	asControlSpec { ^spec ? ControlSpec(0,1,\lin,1,0) }
}

BoolSpec : Spec {

	var <default = true;
	var <>trueLabel, <>falseLabel;

	*new { |default, trueLabel, falseLabel|
		^super.newCopyArgs( default ? true, trueLabel, falseLabel );
	}

	*testObject { |obj|
		^[ True, False ].includes( obj.class );
	}

	*newFromObject { |obj|
		^this.new( obj );
	}

	map { |value|
		switch( value.class,
			Boolean, { ^value },
			Integer, { ^value.booleanValue },
			Float, { ^value > 0.5 },
			{ ^true }
		)
	}

	unmap { |value|
		^value.binaryValue;
	}

	constrain { |value|
		if( value.size == 0 ) {
			^value.booleanValue;
		} {
			^value.mean.booleanValue;
		};
	}

	default_ { |value|
		default = this.constrain( value );
	}

	asControlSpec { ^ControlSpec(0,1,\lin,1,default.binaryValue) }

	storeArgs { ^[default, trueLabel, falseLabel] }
}

BoolArraySpec : BoolSpec {
	var <>size;
	// spec for an array of boolean values

	constrain { |value|
		if( size.notNil ) {
			value = value.asArray.wrapExtend( size );
		};
		^value.asArray.collect({ |val|
			switch( val.class,
				Boolean, { val },
				Integer, { val.booleanValue },
				Float, { val > 0.5 },
				{ true }
			)
		});
	}

	map { |val|
		val = val.asArray;
		if( size.notNil ) { val = val.wrapExtend( size ) };
		^val.collect({ |value|
			switch( value.class,
				Boolean, { value },
				Integer, { value.booleanValue },
				Float, { value > 0.5 },
				{ true }
			)
		});
	}

	unmap { |value|
		^value.asArray.collect(_.binaryValue);
	}
}

PointSpec : Spec {

	classvar <>defaultMode = \point;

	var <rect, <>step, >default, <>units, <>mode; // constrains inside rect
	var clipRect;

	// mode can be \point, \polar, \deg_cw, \deg_ccw
	// only for gui; output will always be Point

	*new { |rect, step, default, units, mode|
		^super.newCopyArgs( rect ? inf, (step ? 0).asPoint, default, units ? "", mode ? this.defaultMode ).init;
	}

	*testObject { |obj|
		^obj.class == Point;
	}

	*newFromObject { |obj|
		var cspecs;
		cspecs = obj.asArray.collect({ |item| ControlSpec.newFromObject( item ) });
		^this.new( Rect.fromPoints(
			(cspecs[0].minval)@(cspecs[1].minval),
			(cspecs[0].maxval)@(cspecs[1].maxval) ),
			(cspecs[0].step)@(cspecs[1].step),
			obj );
	}

	init {
		// number becomes radius
		if( rect.isNumber ) { rect = Rect.aboutPoint( 0@0, rect, rect ); };
		rect = rect.asRect;
		clipRect = Rect.fromPoints( rect.leftTop, rect.rightBottom );
	}

	default { ^default ?? { clipRect.center.round( step ); } }

	minval { ^rect.leftTop }
	maxval { ^rect.rightBottom }

	minval_ { |value|
		var x,y;
		#x, y = value.asPoint.asArray;
		rect = Rect.fromPoints( x@y, rect.rightBottom );
		this.init;
	}

	maxval_ { |value|
		var x,y;
		#x, y = value.asPoint.asArray;
		rect = Rect.fromPoints( rect.leftTop, x@y );
		this.init;
	}

	rect_ { |newRect| rect = newRect; this.init }

	asControlSpec {
		^ControlSpec(
			this.minval.asArray.mean.max( (2**24).neg ),
			this.maxval.asArray.mean.min( 2**24 ),
			\lin,
			0,
			default.asArray.mean,
			units
		);
	}

	clip { |value|
		^value.clip( clipRect.leftTop, clipRect.rightBottom );
	}

	constrain { |value|
		if( value.isCollection && { value.size != 2 } ) {
			value = value.first;
		};
		^(value ?? { 0@0 }).asPoint.clip( clipRect.leftTop, clipRect.rightBottom ); //.round( step );
	}

	map { |value|
		^this.constrain( value.asPoint.linlin(0, 1, rect.leftTop, rect.rightBottom, \none ) );
	}

	unmap { |value|
		^this.constrain( value.asPoint ).linlin( rect.leftTop, rect.rightBottom, 0, 1, \none );
	}

	storeArgs {
	    ^[rect, step, default, units, mode]
	}
}

CodeSpec : Spec {

	var <>default;

	*new { |default|
		^super.newCopyArgs( default );
	}

	constrain { |value| ^value }

	storeArgs { ^[ default ] }

}

UEnvSpec : Spec {
	var <>default;
	var <>spec;

	*new { |default, spec|
		^super.newCopyArgs( default ? Env(), spec );
	}

	*testObject { |obj|
		^obj.isKindOf( Env );
	}

	*newFromObject { |obj|
		^this.new( obj );
	}

	map { |value|
		^if( value.isKindOf( Env ) ) { value } { default.copy };
	}

	unmap { |value|
		^value;
	}

	constrain { |value|
		^value
	}

	storeArgs { ^[ default, spec] }

	asControlSpec { ^spec.asControlSpec }
}

RealVector3DSpec : Spec {

	classvar <>defaultMode = \point;

	var <nrect, <>step, >default, <>units, <>mode; // constrains inside rect

	// mode can be \point, \polar, \deg_cw, \deg_ccw
	// only for gui; output will always be Point

	*new { |nrect, step, default, units, mode|
		^super.newCopyArgs( nrect ? inf, (step ? 0).asRealVector3D, default, units ? "", mode ? \point ).init;
	}

	*testObject { |obj|
		^RealVector.subclasses.includes( obj.class );
	}

	*newFromObject { |obj|
		var cspecs;
		cspecs = obj.as(Array).collect({ |item| ControlSpec.newFromObject( item ) });
		^this.new( NRect(
			cspecs.collect(_.minval).as(RealVector3D),
			cspecs.collect(_.maxval).as(RealVector3D) ),
			cspecs.collect(_.step).as(RealVector3D),
			obj );
	}

	init {
		// number becomes radius
		if( nrect.isNumber ) { nrect = NRect.aboutPoint( 0.asRealVector3D, nrect.asRealVector3D ); };
		nrect = nrect.asNRect;
	}

	default { ^default ?? { nrect.center } }

	minval { ^nrect.origin }
	maxval { ^nrect.endPoint }

	minval_ { |value|
		nrect.origin = value;
	}

	maxval_ { |value|
		nrect.endPoint = value;
	}

	rect_ { |newNRect| nrect = newNRect; this.init }

	clip { |value|
		^nrect.clipVector(value);
	}

	constrain { |value|
		^nrect.clipVector( value.as(RealVector3D).asRealVector3D );
	}

	map { |value|
	    ^nrect.mapVector(value)
	}

	unmap { |value|
		^nrect.unmapVector(value)
	}

	storeArgs {
	    ^[nrect, step, default, units, mode]
	}
}

PolarSpec : Spec {

	var <>maxRadius, <step, >default, <>units; // constrains inside rect
	var clipRect;

	*new { |maxRadius, step, default, units|
		^super.newCopyArgs( maxRadius, (step ? 0), default, units ? "" ).init;
	}

	*testObject { |obj|
		^obj.class == Polar;
	}

	*newFromObject { |obj|
		var cspec;
		cspec = ControlSpec.newFromObject( obj.rho );
		^this.new( cspec.maxval, cspec.step, obj );
	}

	init {
		if( step.class != Polar ) {
			step = Polar( step ? 0, 0 );
		};
	}

	default { ^default ?? { clipRect.center.round( step ); } }

	step_ { |inStep| step = this.makePolar( inStep ) }

	makePolar { |value|
		if( value.class != Polar ) {
			if( value.isArray ) {
				^Polar( *value );
			} {
				^value.asPoint.asPolar;
			};
		} {
			^value.copy;
		};
	}

	clipRadius { |value|
		value = this.makePolar( value );
		if( maxRadius.notNil ) {
			value.rho = value.rho.clip2( maxRadius ); // can be negative too
		};
		^value;
	}

	roundToStep { |value|
		value = this.makePolar( value );
		value.rho = value.rho.round( step.rho );
		value.theta = value.theta.round( step.theta );
		^value;
	}

	scaleRho { |value, amt = 1|
		value = this.makePolar( value );
		value.rho = value.rho * amt;
		^value;
	}

	constrain { |value|
		value = this.clipRadius( value );
		^this.roundToStep( value );
	}

	map { |value|
		^this.constrain( this.scaleRho( value, maxRadius ? 1 ) );
	}

	unmap { |value|
		^this.scaleRho( this.constrain( value ), 1/(maxRadius ? 1));
	}

	storeArgs {
	    ^[maxRadius, step, default, units]
	}
}

RectSpec : Spec {

	var <rect, >default, <>units; // constrains inside rect
	var clipRect;

	// mode can be \point, \polar, \deg_cw, \deg_ccw
	// only for gui; output will always be Point

	*new { |rect, default, units|
		^super.newCopyArgs( rect ? inf, default, units ? "" ).init;
	}

	*testObject { |obj|
		^obj.class == Rect;
	}

	*newFromObject { |obj|
		var cspecs;
		cspecs = obj.asArray.collect({ |item| ControlSpec.newFromObject( item ) });
		^this.new( 200 );
	}

	init {
		// number becomes radius
		if( rect.isNumber ) { rect = Rect.aboutPoint( 0@0, rect, rect ); };
		rect = rect.asRect;
		clipRect = Rect.fromPoints( rect.leftTop, rect.rightBottom );
	}

	default { ^default ?? { Rect.aboutPoint( 0@0, 5, 5 ); } }

	minval { ^clipRect.leftTop }
	maxval { ^clipRect.rightBottom }

	minval_ { |value|
		var x,y;
		#x, y = value.asPoint.asArray;
		rect.left = x;
		rect.top = y;
		this.init;
	}

	maxval_ { |value|
		var x,y;
		#x, y = value.asPoint.asArray;
		rect.right = x;
		rect.top = y;
		this.init;
	}

	rect_ { |newRect| rect = newRect; this.init }

	clip { |value|
		//^value.clip( clipRect.leftTop, clipRect.rightBottom );
		^value
	}

	constrain { |value|
		^value.asRect; //.clip( clipRect.leftTop, clipRect.rightBottom ); //.round( step );
	}

	map { |value|
		^this.constrain( value.asRect.linlin(0, 1, rect.leftTop, rect.rightBottom, \none ) );
	}

	unmap { |value|
		^this.constrain( value.asRect ).linlin( rect.leftTop, rect.rightBottom, 0, 1, \none );
	}

	storeArgs {
	    ^[rect, default, units]
	}
}

DualValueSpec : ControlSpec {
	var realDefault;

	// a range is an Array of two values [a,b], where:
	// a <= b, maxRange >= (b-a) >= minRange
	// the spec is a ControlSpec or possibly a ListSpec with numbers

	*new { |minval=0.0, maxval=1.0, warp='lin', step=0.0,
			 default, units|
		^super.new( minval, maxval, warp, step, default ? [minval,maxval], units )
	}

	*newFrom { arg similar; // can be ControlSpec too
		^this.new(similar.minval, similar.maxval,
			similar.warp.asSpecifier,
			similar.step, similar.default, similar.units)
	}

	*testObject { |obj|
		^obj.isArray && { (obj.size == 2) && { obj.every(_.isNumber) } };
	}

	*newFromObject { |obj|
		var cspecs;
		cspecs = obj.collect({ |item| ControlSpec.newFromObject( item ) });
		^this.new(
			cspecs.collect(_.minval).minItem,
			cspecs.collect(_.maxval).maxItem,
			\lin,
			cspecs.collect(_.step).minItem,
			obj
			);
	}


	default_ { |range| realDefault = default = this.constrain( range ); }
	default { ^realDefault ??
		{ realDefault = this.constrain( default ? [minval, maxval] ); } } // in case of a bad default

	storeArgs { ^[minval, maxval, warp.asSpecifier, step, this.default, units] }

	constrain { arg value;
		var array;
		array = value.asArray;
		if( array.size != 2 ) { array = array.extend( 2, array.last ); };
		array = array.collect({ |item| item.asFloat.clip( clipLo, clipHi ); });
		^array.round(step); // step may mess up the min/maxrange
	}

	uconstrain { |val| ^this.constrain( val ) }

	map { arg value;
		// maps a value from [0..1] to spec range
		^this.constrain( warp.map(value) );
	}

	unmap { arg value;
		// maps a value from spec range to [0..1]
		^warp.unmap( this.constrain(value) );
	}

	asControlSpec {
		if( this.units == " Hz" ) {
			^FreqSpec.newFrom( this ).default_( this.default[0] );
		} {
			^ControlSpec.newFrom( this ).default_( this.default[0] );
		};
	}
	asArrayControlSpec { ^ArrayControlSpec.newFrom( this ); }
}

RangeSpec : ControlSpec {
	var <>minRange, <>maxRange;
	var realDefault;

	// a range is an Array of two values [a,b], where:
	// a <= b, maxRange >= (b-a) >= minRange
	// the spec is a ControlSpec or possibly a ListSpec with numbers

	*new { |minval=0.0, maxval=1.0, minRange=0, maxRange = inf, warp='lin', step=0.0,
			 default, units|
		^super.new( minval, maxval, warp, step, default ? [minval,maxval], units )
			.minRange_( minRange ).maxRange_( maxRange )
	}

	*newFrom { arg similar; // can be ControlSpec too
		^this.new(similar.minval, similar.maxval,
			similar.tryPerform( \minRange ) ? 0,
			similar.tryPerform( \maxRange ) ? inf,
			similar.warp.asSpecifier,
			similar.step, similar.default, similar.units)
	}

	*testObject { |obj|
		^obj.isArray && { (obj.size == 2) && { obj.every(_.isNumber) } };
	}

	*newFromObject { |obj|
		var cspecs;
		cspecs = obj.collect({ |item| ControlSpec.newFromObject( item ) });
		^this.new(
			cspecs.collect(_.minval).minItem,
			cspecs.collect(_.maxval).maxItem,
			0, inf, \lin,
			cspecs.collect(_.step).minItem,
			obj
			);
	}


	default_ { |range| realDefault = default = this.constrain( range ); }
	default { ^realDefault ??
		{ realDefault = this.constrain( default ? [minval, maxval] ); } } // in case of a bad default

	storeArgs { ^[minval, maxval, minRange, maxRange, warp.asSpecifier, step, this.default, units] }

	constrain { arg value;
		var array;
		array = value.asArray.copy.sort;
		if( array.size != 2 ) { array = array.extend( 2, array.last ); };
		array = array.collect({ |item| item.asFloat.clip( clipLo, clipHi ); });
		case { (array[1] - array[0]) < minRange } {
			//"clipped minRange".postln;
			array = array.mean + ( minRange * [-0.5,0.5] );
			case { array[0] < clipLo } {
				array = array + (clipLo-array[0]);
			} {  array[1] > clipHi } {
				array = array + (clipHi-array[1]);
			};
		} { (array[1] - array[0]) > maxRange } {
			//"clipped maxRange".postln;
			array = array.mean + ( maxRange * [-0.5,0.5] );
			case { array[0] < clipLo } {
				array = array + (clipLo-array[0]);
			} {  array[1] > clipHi } {
				array = array + (clipHi-array[1]);
			};
		};
		^array.round(step); // step may mess up the min/maxrange
	}

	uconstrain { |val| ^this.constrain( val ) }

	map { arg value;
		// maps a value from [0..1] to spec range
		^this.constrain( warp.map(value) );
	}

	unmap { arg value;
		// maps a value from spec range to [0..1]
		^warp.unmap( this.constrain(value) );
	}

	asRangeSpec { ^this }
	asControlSpec {
		if( this.units == " Hz" ) {
			^FreqSpec.newFrom( this ).default_( this.default[0] );
		} {
			^ControlSpec.newFrom( this ).default_( this.default[0] );
		};
	}
	asArrayControlSpec { ^ArrayControlSpec.newFrom( this ); }

}

MultiRangeSpec : ControlSpec {
	var <>minRange, <>maxRange;
	var <>size;
	var <>originalSpec;

	*new { |minval=0.0, maxval=1.0, minRange=0, maxRange = inf, warp='lin', step=0.0,
			 default, units|
		^super.new( minval, maxval, warp, step, default ? [[minval,maxval]], units )
			.minRange_( minRange ).maxRange_( maxRange )
	}

	asRangeSpec {
		^RangeSpec.newFrom( this ).default_( this.default[0] );
	}
	asControlSpec { ^ControlSpec.newFrom( this ).default_( this.default.flat.mean ); }
	asArrayControlSpec { ^this }

	copy {
		^this.class.newFrom(this);
	}

	constrain { |value|
		var ctrlSpec = originalSpec ?? { this.asRangeSpec; };
		if( size.notNil ) {
			^value.collect({ |x| ctrlSpec.constrain(x) })
				.wrapExtend( size );
		} {
			^value.collect{ |x| ctrlSpec.constrain(x) }
		}
	}

	uconstrain { |value| ^this.constrain( value ? this.default ); }

	*testObject { |obj| ^obj.isArray && { obj.every(_.isArray) } }

}

ColorSpec : Spec {

	classvar <>presetManager;

	var >default;

	*initClass {
		Class.initClassTree( PresetManager );
		Class.initClassTree( Color );
		presetManager = PresetManager( Color );
		presetManager.presets = Color.web16.getPairs( Color.web16.keys.as(Array).sort );
		presetManager.applyFunc_( { |object, preset|
			 	if( object === Color ) {
				 	preset.deepCopy;
			 	} {
				 	object.red = preset.red;
				 	object.green = preset.green;
				 	object.blue = preset.blue;
				 	object.alpha = preset.alpha;
				 }
		 	} );
	}

	*new { |default|
		^super.newCopyArgs( default ).init;
	}

	*testObject { |obj|
		^obj.class == Color;
	}

	*newFromObject { |obj|
		^this.new( obj.asColor );
	}

	init {
	}

	default { ^default ?? { Color.gray(0.5) } }

	clip { |value|
		^value
	}

	constrain { |value|
		^value.asColor;
	}

	storeArgs {
	    ^[ default ]
	}

}

ColorArraySpec : ColorSpec {
	var <>size;

	init {
		size = default !? _.size;
	}

	constrain { |value|
		var colorSpec = ColorSpec();
		if( size.notNil ) {
			^value.collect({ |x| colorSpec.constrain(x) })
				.wrapExtend( size );
		} {
			^value.collect{ |x| colorSpec.constrain(x) }
		}
	}

	uconstrain { |value| ^this.constrain( value ? this.default ); }

	*testObject { |obj| ^obj.isArray && { obj.every(_.isKindOf( Color ) ) } }

}

RichBufferSpec : Spec {

	var <>numChannels = 1; // fixed number of channels
	var <numFrames;
	var <>editMode; // nil (no gui), \seconds
	classvar <>useServerSampleRate = true; // override stored samplerate for duration display


	*new { |numChannels = 1, numFrames, editMode|
		^super.newCopyArgs( numChannels, numFrames, editMode ).init;
	}

	*testObject { |obj|
		^obj.class == RichBuffer;
	}

	*newFromObject { |obj|
		^this.new( obj.numChannels );
	}

	init {
		if( numFrames.isNumber ) { numFrames = [numFrames,numFrames].asSpec }; // single value
		if( numFrames.isNil ) { numFrames = [0,inf,\lin,1,44100].asSpec }; // endless
		numFrames = numFrames.asSpec;
	}

	constrain { |value|
		if( value.class == RichBuffer ) {
			value.numFrames = numFrames.constrain( value.numFrames );
			value.numChannels = numChannels.asCollection.first;
			^value;
		} {
			^RichBuffer( numFrames.default, numChannels );
		};
	}

	default {
		^RichBuffer( numFrames.default, numChannels );
	}

	storeArgs { ^[numChannels, numFrames] }

}

BufSndFileSpec : RichBufferSpec {

	*testObject { |obj|
		^obj.isKindOf( BufSndFile );
	}

	constrain { |value|
		value = value.asBufSndFile;
		if( numChannels.notNil ) {
			if( numChannels.asCollection.includes( value.numChannels ).not ) {
				if( numChannels.asCollection.includes( value.useChannels.size ).not ) {
					value.useChannels = (..numChannels.asCollection[0]-1)
						.wrap( 0, value.numChannels );
				};
			};
		};
		^value;
	}

	map { |in| ^this.constrain( in ) }
	unmap { |in| ^in }

	default {
		^nil.asBufSndFile;
	}

	*newFromObject { |obj|
		^this.new( obj.numChannels );
	}

}

MonoBufSndFileSpec : BufSndFileSpec {

	*testObject { |obj|
		^obj.isKindOf( MonoBufSndFile );
	}

	constrain { |value|
		^value.asMonoBufSndFile;
	}

	default {
		^nil.asMonoBufSndFile;
	}

	*newFromObject { |obj|
		^this.new();
	}

}

DiskSndFileSpec : BufSndFileSpec {

	*testObject { |obj|
		^obj.isKindOf( DiskSndFile );
	}

	constrain { |value|
		value = value.asDiskSndFile;
		if( numChannels.notNil ) {
			if(  numChannels.asCollection.includes( value.numChannels ).not ) {
				"DiskSndFileSpec - soundfile '%' has an unsupported number of channels (%)"				.format( value.path.basename, value.numChannels )
					.warn;
			};
		};
		^value;
	}

	default {
		^nil.asDiskSndFile;
	}

	*newFromObject { |obj|
		^this.new( obj.numChannels );
	}

}

MultiSndFileSpec : Spec {

	// array of points instead of a single point

	var <>default, <>fixedAmount = false, <>sndFileClass;

	*new { |default, fixedAmount = false, sndFileClass|
		^super.new
			.default_( default )
			.fixedAmount_( fixedAmount )
			.sndFileClass_( sndFileClass ? BufSndFile )
			;
	}

	*testObject { |obj|
		^obj.isCollection && { obj[0].isKindOf(AbstractSndFile) };
	}

	constrain { |value|
		^value;
	}

	*newFromObject { |obj|
		^this.new;
	}

}

SplitBufSndFileSpec : BufSndFileSpec {

	var >default;

	*testObject { |obj|
		^obj.isKindOf( SplitBufSndFile );
	}

	constrain { |value|
		value = value.asSplitBufSndFile;
		if( numChannels.notNil ) {
			if( numChannels.asCollection.includes( value.numChannels ).not ) {
				if( numChannels.asCollection.includes( value.useChannels.size ).not ) {
					value.useChannels = (..numChannels.asCollection[0]-1)
						.wrap( 0, value.numChannels );
				};
			};
		};
		^value;
	}

	map { |in| ^this.constrain( in ) }
	unmap { |in| ^in }

	default {
		^default ?? { nil.asBufSndFile.asSplitBufSndFile; };
	}


}

URecSndFileSpec : BufSndFileSpec {

	*testObject { |obj|
		^obj.isKindOf( URecSndFile );
	}

	constrain { |value|
		//value = value.asDiskSndFile;
		if( value.isKindOf( URecSndFile ).not ) {
			value = URecSndFile( "~/Desktop/test.aif", numChannels );
		} {
			if( numChannels.notNil ) {
				value.numChannels = numChannels;
			};
		};
		^value;
	}

	default {
		^URecSndFile( "~/Desktop/test.aiff", numChannels );
	}

	*newFromObject { |obj|
		^this.new( obj.numChannels );
	}

}

PartConvBufferSpec : RichBufferSpec {

	*new {
		^super.newCopyArgs().init;
	}

	*testObject { |obj|
		^obj.isKindOf( PartConvBuffer );
	}

	constrain { |value|
		^value.asPartConvBuffer;
	}

	default {
		^nil.asBufSndFile;
	}

	*newFromObject { |obj|
		^this.new( obj.numChannels );
	}

}

MultiPartConvBufferSpec : Spec {

	var <>default, <>fixedAmount = true;

	*new { |default, fixedAmount = false|
		^super.new
			.default_( default )
			.fixedAmount_( fixedAmount );
	}

	*testObject { |obj|
		^obj.isCollection && { obj[0].isKindOf(PartConvBuffer) };
	}

	constrain { |value|
		^value;
	}

	*newFromObject { |obj|
		^this.new;
	}


}


UMIDIFileSpec : Spec {

	*testObject { |obj|
		^obj.isKindOf( UMIDIFile );
	}

	constrain { |value|
		value = value.asUMIDIFile;
		^value;
	}

	map { |in| ^this.constrain( in ) }
	unmap { |in| ^in }

	default {
		^nil.asUMIDIFile;
	}

	*newFromObject { |obj|
		^this.new( );
	}
}

MultiSpec : Spec {

	// an ordered and named collection of specs, with the option to re-map to another spec

	var <names, <specs, <>defaultSpecIndex = 0;
	var <>toSpec;

	*new { |...specNamePairs|
		specNamePairs = specNamePairs.clump(2).flop;
		^super.newCopyArgs( specNamePairs[0], specNamePairs[1] ).init;
	}

	init {
		names = names.asCollection.collect(_.asSymbol);
		specs = specs.asCollection;
		specs = names.collect({ |item, i| specs[i].asSpec });
	}

	findSpecForName { |name| // name or index
		name = name ? defaultSpecIndex;
		if( name.isNumber.not ) { name = names.indexOf( name.asSymbol ) ? defaultSpecIndex };
		^specs[ name ];
	}

	default { |name| // each spec has it's own default
		^this.findSpecForName(name).default;
	}

	defaultName { ^names[ defaultSpecIndex ] }
	defaultName_ { |name| defaultSpecIndex = names.indexOf( name.asSymbol ) ? defaultSpecIndex }

	defaultSpec { ^specs[ defaultSpecIndex ] }

	constrain { |value, name|
		^this.findSpecForName(name).constrain( value );
	}

	map { |value, name|
		if( toSpec.notNil ) { value = toSpec.asSpec.unmap( value ) };
		^this.findSpecForName(name).map( value );
	}

	unmap { |value, name|
		if( toSpec.notNil ) { value = toSpec.asSpec.map( value ) };
		^this.findSpecForName(name).unmap( value );
	}

	mapToDefault { |value, from|
		if( from.isNil ) { value = this.unmap( value, from ); };
		^this.map( value, defaultSpecIndex );
	}

	unmapFromDefault { |value, to|
		value = this.unmap( value, defaultSpecIndex );
		if( to.isNil ) {
			^this.map( value, to );
		} {
			^value
		};
	}

	mapFromTo { |value, from, to|
		^this.map( this.unmap( value, from ), to );
	}

	unmapFromTo { |value, from, to|
		^this.mapFromTo( value, to, from );
	}
}

IntegerSpec : Spec {

	var <default = 0;
	var <>step = 1;
	var <>alt_step = 1;
	var <>minval = -inf;
	var <>maxval = inf;
	var <>units;

	*new{ |default = 0, minval = -inf, maxval = inf|
        ^super.new.minval_( minval ).maxval_( maxval ).default_(default);
	}

	*testObject { |obj|
		^obj.class == Integer;
	}

	constrain { |value|
		^value.clip(minval, maxval).asInteger;
	}

	range { ^maxval - minval }
	ratio { ^maxval / minval }

	default_ { |value|
		default = this.constrain( value );
	}

	warp { ^LinearWarp( this ) }

	floatMinMax {
		^minval.max( (2**24).neg )
	}

	map { |value|
		^value.linlin( 0, 1,
			this.minval.max( (2**24).neg ),
			maxval.min( 2**24 ), \minmax
		).round( step ).asInteger;
	}

	unmap { |value|
		^value.round(step).linlin(
			this.minval.max( (2**24).neg ),
			maxval.min( 2**24 ),
			0,1, \minmax
		);
	}

	asControlSpec {
		^ControlSpec( this.minval.max( (2**24).neg ), maxval.min( 2**24 ), \lin, 1, default )
	}


    storeArgs { ^[default, minval, maxval] }
}

PositiveIntegerSpec : IntegerSpec {

	constrain { |value|
		^value.clip(minval.max(0), maxval).asInteger;
	}

	minval { ^minval.max(0) }

}

HardwareBusSpec : PositiveIntegerSpec {

	classvar <>inDeviceDict, <>outDeviceDict;

	var <>type = \output, <>numChannels = 1;

	*initClass {
		StartUp.defer({ this.readPrefs });
	}

	*readPrefs {
		var paths;
		paths = [
			"HardwareBusSpec-inDeviceDict.scd",
			"HardwareBusSpec-outDeviceDict.scd",
		].collect({ |fileName|
			thisProcess.platform.userAppSupportDir +/+ fileName;
		});
		if( File.exists( paths[0] ) ) {
			this.inDeviceDict = paths[0].load;
		};
		if( File.exists( paths[1] ) ) {
			this.outDeviceDict = paths[1].load;
		};
	}

	*new{ |type = \output,  numChannels = 1, maxval = 512|
		^super.new.minval_( 0 ).type_( type ).maxval_( maxval ).numChannels_( numChannels ).default_( 0 );
	}

	constrain { |value|
		^value.clip(0, maxval).asInteger;
	}

	*addDeviceLabels { |type = \output, device, labels|
		device = device ? 'system';
		switch( type,
			\output, {
				if( outDeviceDict.isNil ) { outDeviceDict = () };
				outDeviceDict[ device.asSymbol ] = labels;
			},
			\input, {
				if( inDeviceDict.isNil ) { inDeviceDict = () };
				inDeviceDict[ device.asSymbol ] = labels;
			}
		);
	}

	getDeviceLabels {
		var dict, list, device;
		switch( type,
			\output, {
				dict = outDeviceDict ?? {()};
				device = Server.default.options.outDevice ? 'system';
			},
			\input, {
				dict = inDeviceDict ?? {()};
				device = Server.default.options.inDevice ? 'system';
			}
		);
		^dict[ device.asSymbol ] ?? {
			[ [ type, (1..Server.default.options.numOutputBusChannels) ] ];
		}
	}

	*makeDeviceLabelsList { |labels|
		^labels.collect({ |label| label.asArray.flop }).flatten(1).collect({ |item|
			item.join(" ").asSymbol
		});
	}

	storeArgs { ^[type, maxval] }

}

MIDIOutSpec : HardwareBusSpec {

	getDeviceLabels {
		var dict, list, device;
		if( MIDIClient.initialized.not ) {
			MIDIIn.connectAll;
		};
		dict = OEM();

		MIDIClient.destinations.collect({ |dest|
			dict[ dest.device.asSymbol ] = dict[ dest.device.asSymbol ].add( dest.name );
		});

		^dict.collectAs({ |val,i| [ dict.keys[i], val ] }, Array);
	}

}

SharedValueIDSpec : PositiveIntegerSpec {

	*umap_name { ^'shared_in' }

	*new{ |default = 0|
        ^super.new.minval_( 0 ).maxval_( 99 ).default_(default);
	}

	storeArgs { ^[ default ] }
}

SharedBufferIDSpec : SharedValueIDSpec {
	 *umap_name { ^'shared_buffer_in' }
}

PositiveRealSpec : Spec {

	var <default = 0;

    *new{ |default = 0|
        ^super.new.default_(default)
    }

	constrain { |value|
		^value.max(0);
	}

	default_ { |value|
		default = this.constrain( value );
	}

	storeArgs { ^[default] }
}

FreqSpec : ControlSpec {

	classvar <>mode = 'hz'; // \hz, \midi, \note - gui only

	*new { arg minval=20, maxval=20000, warp='exp', step=0.0, default = 440, units = " Hz", grid;
		^super.newCopyArgs(minval, maxval, warp, step,
				default ? minval, units ? "", grid
			).init
	}

	*initClass {
		specs.put( \freq, FreqSpec() ); // replace default freq spec with this
	}

}

AngleSpec : ControlSpec {

	classvar <mode = 'rad'; // \rad, \deg

	*new { arg minval= -pi, maxval= pi, warp='lin', step=0.0, default = 0, units, grid;
		^super.newCopyArgs(minval, maxval, warp, step,
				default ? minval, units ? "", grid
			).init
	}

	*mode_ { |newMode| mode = newMode; this.changed( \mode ) }
}

AngleArraySpec : ArrayControlSpec { }

FactorSpec : ControlSpec {

	*new { arg minval= 0.0625, maxval= 16, warp='exp', step=0.0, default = 1, units, grid;
		^super.newCopyArgs(minval, maxval, warp, step,
				default ? minval, units ? "", grid
			).init
	}
}

DisplaySpec : Spec { // a spec for displaying a value that should not be user-edited
	var <>spec;
	var <>formatFunc;

	*new { |spec, formatFunc|
		^this.newCopyArgs( (spec ? [0,1]).asSpec, formatFunc ? _.asString );
	}

	doesNotUnderstand { |selector ...args|
		var res;
		res = spec.perform( selector, *args );
		if( res != this ) {
			^res;
		};
	}
}

ControlSpecSpec : Spec {

	*new {
		^super.newCopyArgs();
	}

	*testObject { |obj|
		^obj.isKindOf( ControlSpec );
	}

	constrain { |value|
		^value.asControlSpec;
	}

	default {
		^nil.asControlSpec;
	}

}

+ Spec {
	*testObject { ^false }

	*forObject { |obj|
		var specClass;
		specClass = [ ControlSpec, RangeSpec, BoolSpec, PointSpec, PolarSpec,
				BufSndFileSpec, DiskSndFileSpec ]
			.detect({ |item| item.testObject( obj ) });
		if( specClass.notNil ) {
			^specClass.newFromObject( obj );
		} {
			^nil;
		};
	}

	*newFromObject { ^this.new }

	uconstrain { |...args| ^this.constrain( *args ) }
}


+ ControlSpec {
	asRangeSpec { ^RangeSpec.newFrom( this ) }
	asControlSpec { ^this }
	asArrayControlSpec { ^ArrayControlSpec.newFrom( this ).originalSpec_( this.copy ) }

	*testObject { |obj| ^obj.isNumber }

	uconstrain { |val|
		if( val.size == 0 ) {
			^this.constrain( val );
		} {
			^this.constrain( val.mean );
		};
	}

	*newFromObject { |obj| // float or int
		var range;

		if( obj.isNegative ) {
			range = obj.abs.ceil.asInteger.nextPowerOfTwo.max(1) * [-1,1];
		} {
			range = [ 0, obj.ceil.asInteger.nextPowerOfTwo.max(1) ];
		};

		if( obj.isFloat ) {
			^this.new( range[0], range[1], \lin, 0, obj );
		} {
			^this.new( range[0], range[1], \lin, 1, obj );
		};
	}
}

+ Nil {
	asRangeSpec { ^RangeSpec.new }
	asControlSpec { ^this.asSpec; }
	asArrayControlSpec { ^this.asSpec.asArrayControlSpec }
}

+ Symbol {
	asRangeSpec { ^this.asSpec.asRangeSpec }
	asControlSpec { ^this.asSpec; }
	asArrayControlSpec { ^this.asSpec.asArrayControlSpec }
}

+ Array {
	asRangeSpec { ^RangeSpec.newFrom( *this ) }
	asControlSpec { ^this.asSpec; }
	asArrayControlSpec { ^ArrayControlSpec( this.minItem, this.maxItem, \lin, 0, this ); }
}