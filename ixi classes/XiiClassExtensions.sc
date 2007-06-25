
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