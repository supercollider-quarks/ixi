

+ SCEnvelopeView {
	// an Env has times in sec for each point, an EnvView has points (x,y) in the view (0 to 1)
	// this method formats that
	
	env2viewFormat_ {arg env; // an envelope of the Env class passed in
		var times, levels, timesum, lastval; 
		times = [0.0]++env.times.normalizeSum; // add the first point (at 0)
		levels = env.levels;
		timesum = 0.0;
		lastval = 0.0;
		times = times.collect({arg item, i; lastval = item; timesum = timesum+lastval; timesum});
		[\times, times.asFloat, \levels, levels.asFloat].postln;
		this.value_([times.asFloat, levels.asFloat]);
	}
	
	view2envFormat {
		var times, levels, scale, lastval, timesum;
		times = this.value[0];
		levels = this.value[1];
		times = times.drop(1);
		timesum = 0.0;
		lastval = 0.0;
		times = times.collect({arg item, i; lastval = item; timesum = lastval-timesum; timesum});
		^[levels, times];
	}
}


+ SCSlider {
					
	defaultKeyDownAction { arg char, modifiers, unicode, keycode;
		// standard keydown
		if (char == $r, { this.valueAction = 1.0.rand; ^this });
		if (char == $n, { this.valueAction = 0.0; ^this });
		if (char == $x, { this.valueAction = 1.0; ^this });
		if (char == $c, { this.valueAction = 0.5; ^this });
		if (char == $], { this.increment; ^this });
		if (char == $[, { this.decrement; ^this });
		// [modifiers, unicode, keycode].postln;
		if(modifiers&262144==262144, { // check if Ctrl is down first
			if (unicode == 16rF700, { this.incrementCtrl; ^this });
			if (unicode == 16rF703, { this.incrementCtrl; ^this });
			if (unicode == 16rF701, { this.decrementCtrl; ^this });
			if (unicode == 16rF702, { this.decrementCtrl; ^this });
		}, { // if not, then normal
			if (unicode == 16rF700, { this.increment; ^this });
			if (unicode == 16rF703, { this.increment; ^this });
			if (unicode == 16rF701, { this.decrement; ^this });
			if (unicode == 16rF702, { this.decrement; ^this });
		});
		^nil		// bubble if it's an invalid key
	}
	
	incrementCtrl { ^this.valueAction = this.value + 0.001 }
	decrementCtrl { ^this.valueAction = this.value - 0.001 }


}
