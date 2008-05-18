// blackrain at realizedsound dot net
// mod from the original concept by thor and cylob / original behaviour from sc2 by James McCartney
// fix key modidiers bug and horizontral scroll action by Stephan Wittwer 08/2006
// handle a nil value by Wouter Snoei 08/2006
// and made GUI cross platformable by ixi 03/2008 (that's why this class is so strange)


XiiSNBox {
	var clipLo = -inf, clipHi = inf, hit, inc=1.0, <>scroll=true, <>shift_step=0.1, <>ctrl_step=10;
	var box, object;

	*viewClass { ^GUI.numberBox }
	
	*new {arg parent, bounds;
		^super.new.initSNBox(parent, bounds);
	
	}

	initSNBox { arg parent, bounds;
		box = GUI.numberBox.new(parent, bounds)
			.mouseDownAction_({ arg me, x, y, modifiers, buttonNumber, clickCount;
				//[me, x, y, modifiers].postln;
				hit = Point(x,y);
				if (scroll == true, {
					inc = 1.0;
					case
						{ modifiers & 131072 == 131072 } // shift defaults to step x 0.1
							{ inc = shift_step }
						{ modifiers & 262144 == 262144 } // control defaults to step x 10
							{ inc = ctrl_step };
				});			
			})
			.mouseMoveAction_({ arg me, x, y, modifiers;
			
				var direction;
								//[me, x, y, modifiers].postln;

				if (scroll == true, {
					direction = 1.0;
						// horizontal or vertical scrolling:
					if ( (x - hit.x) < 0 or: { (y - hit.y) > 0 }) { direction = -1.0; };
		
					box.valueAction = (box.value + (inc * box.step * direction));
					hit = Point(x, y);
				});			
			});
	}

	align_{arg a;
		box.align_(a);
	}
	value_ { arg val;
		//[\clipLo, clipLo, \clipHi, clipHi].postln;
		box.keyString = nil;
		box.stringColor = box.normalColor;
		//box.object = val !? { val.clip(clipLo, clipHi) };
		//box.object = val.clip(clipLo, clipHi) ;
		box.value = val.clip(clipLo, clipHi) ;
		//box.string = box.object.asString;
		box.string = box.value.asString;
	}	
	
	value {
		^box.value;
	}
	
	clipLo_{arg lo;
		clipLo = lo;
	}

	clipHi_{arg hi;
		clipHi = hi;
	}

	boxColor_ {arg color;
		box.boxColor_(color);
	}
	
	focusColor_ {arg color;
		box.focusColor_(color);
	} 
	
	background_ {arg bg;
		box.background_(bg);
	}
	step_{arg st;
		box.step_(st);
	}
	
	font_{arg f;
		box.font_(f);
	}

	action_{arg act;
		box.action_(act);
	}
	
	keyDownAction_ {arg act;
		box.keyDownAction_(act);
	}
	valueAction_ { arg val;
		var prev;
			//	[\clipLo, clipLo, \clipHi, clipHi].postln;
		prev = object;
		//box.value = val !? { val.clip(clipLo, clipHi) };
		box.value = val.clip(clipLo, clipHi) ;
		if (object != prev, { box.doAction });
	}
}

