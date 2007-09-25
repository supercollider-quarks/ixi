
+ SimpleNumber {
	
	// checking if a MIDI note is microtone
	midiIsMicroTone { arg tolerance = 0.01;
		if(this.frac < tolerance, {^false}, {^true});
	}
	// checking if a frequency is microtone
	freqIsMicroTone { arg tolerance = 0.01;
		if(this.cpsmidi.frac < tolerance, {^false}, {^true});
	}

}

+ Point {
	distanceFrom { |other|
		^sqrt(([this.x, this.y] - [other.x, other.y]).squared.sum);
	}
}

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

/*
(
// e = Env.new([0, 1, 0.3, 0.8, 0], [1, 3, 1, 4],'linear').plot;
 e = Env.new([0.5, 1, 0.6, 0.6, 0], [0.1, 0.3, 0.81, 0.2],'linear').plot;

//e = Env.triangle(1, 1);
//e = Env.adsr(0.02, 0.2, 0.25, 1, 1, -4);

a = SCWindow("envelope", Rect(200 , 450, 250, 100));
a.view.decorator =  FlowLayout(a.view.bounds);

b = SCEnvelopeView(a, Rect(0, 0, 230, 80))
	.drawLines_(true)
	.selectionColor_(Color.red)
	.drawRects_(true)
	.resize_(5)
	.action_({arg b; [b.index,b.value].postln})
	.thumbSize_(5)
	.env2viewFormat_(e);
a.front;


)
b.view2envFormat

*/