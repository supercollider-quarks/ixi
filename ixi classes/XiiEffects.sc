XiiDelay {	
	var <>gui;
	
	*new { arg server, channels;
		^super.new.initXiiDelay(server, channels);
		}
		
	initXiiDelay {arg server, channels;

		var delayTimeSpec, delayTailSpec, params, s; 
		
		s = server ? Server.local;
		
		// mono
		SynthDef(\xiiDelay1x1, {arg inbus=0,
							outbus=0, 
							maxDelay=2, // hardcoded here
							delay=0.4, 
							feedback=0.0, 
							fxlevel = 0.7, 
							level=1.0;
							
		   var fx, sig; 
		   sig = In.ar(inbus,1); 
		   fx = sig + LocalIn.ar(1); 
		   fx = DelayC.ar(fx, maxDelay, delay); 
		   LocalOut.ar(fx * feedback); 
		   Out.ar(outbus, (fx * fxlevel) + (sig * level)) 
		}).load(s); 
		
		// stereo
		SynthDef(\xiiDelay2x2, {arg inbus=0,
							outbus=0, 
							maxDelay=2, // hardcoded here
							delay=0.4, 
							feedback=0.0, 
							fxlevel = 0.7, 
							level=1.0;
							
		   var fx, sig; 
		   sig = In.ar(inbus,2); 
		   fx = sig + LocalIn.ar(2); 
		   fx = DelayC.ar(fx, maxDelay, delay); 
		   LocalOut.ar(fx * feedback); 
   		   Out.ar(outbus, (fx * fxlevel) + (sig * level)) 
		}).load(s); 

		delayTailSpec = ControlSpec.new(0.01, 2, \exponential, 0, 1.2); 
		
		params = [ 
		   ["Delay", "Feedback", "Fx level", "Dry Level"], 
		   [ \delay, \feedback, \fxlevel, \level], 
		   [delayTailSpec, \amp, \amp, \amp], 
		   [0.4, 0.4, 0.8, 1]]; 
		
		gui = if(channels == 2, { 	// stereo
			XiiEffectGUI.new("Delay 2x2", \xiiDelay2x2, params, channels, this); /// 
			}, {				// mono
			XiiEffectGUI.new("Delay 1x1", \xiiDelay1x1, params, channels, this); /// 
		})
	}
}


XiiFreeverb {	
	var <>gui; // TESTING: TEMP can delete

	*new { arg server, channels;
		^super.new.initFreeverb(server, channels);
		}
		
	initFreeverb {arg server, channels;
	
		var mixSpec, params, s; 
		s = server ? Server.local;
		
		// mono
		SynthDef(\xiiFreeverb1x1, {| inbus=0, outbus=0, mix=0.25, room=0.15, damp=0.5, fxlevel=0.75, level=0 | 
		   var fx, sig; 
		   sig = In.ar(inbus, 1); 
		   fx = FreeVerb.ar(sig, mix, room, damp); 
		   Out.ar(outbus, (fx*fxlevel) + (sig * level)) // level 
		},[0,0,0.1,0.1,0,0]).load(s); 

		// stereo
		SynthDef(\xiiFreeverb2x2, {| inbus=0, outbus=0, mix=0.25, room=0.15, damp=0.5, fxlevel=0.75, level=0 | 
		   var fx, sig; 
		   sig = In.ar(inbus, 2); 
		   fx = FreeVerb.ar(sig, mix, room, damp); 
		   Out.ar(outbus, (fx*fxlevel) + (sig * level)) // level 
		},[0,0,0.1,0.1,0,0]).load(s); 
		
		mixSpec = ControlSpec.new(0, 1, \linear, 0.01, 0.75); 
		
		params = [ 
		   ["Room", "Damp", "Dry/Wet", "Fx Level", "Dry Level"], 
		   [\room, \damp, \mix, \fxlevel, \level], 
		   [\amp, \amp, mixSpec, \amp, \amp], 
		   [0.8, 0.8, 0.75, 0.8, 0.2 ]]; 
		
		gui = if(channels == 2, { 	// stereo
			XiiEffectGUI.new("Freeverb 2x2", \xiiFreeverb2x2, params, channels, this); 
			},{				// mono
			XiiEffectGUI.new("Freeverb 1x1", \xiiFreeverb1x1, params, channels, this); 
			});

	}
}


XiiAdCVerb {	

	var <>gui; // TESTING: TEMP can delete

	*new { arg server, channels;
		^super.new.initAdCVerb(server, channels);
		}
		
	initAdCVerb {arg server, channels;
	
		var roomSpec, mixSpec, params, s; 
		s = server ? Server.local;
		
		// mono
		SynthDef(\xiiAdcverb1x1, {| inbus=0, outbus=0, revtime=3, hfdamping=0.5, mix=0.1, level=0 | 
		   	var fx, fxIn, sig; 
		   	sig = In.ar(inbus, 1); 
			fxIn = LeakDC.ar(sig) * mix;
		    	fx = AdCVerb.ar(fxIn, revtime, hfdamping, nOuts: 1);

			Out.ar(outbus, LeakDC.ar(fx + (sig * level))) // Leak DC to fix Karplus-Strong problem
		},[0.1,0.1,0.1,0.1, 0.1]).load(s); 
		
		// stereo
		SynthDef(\xiiAdcverb2x2, {| inbus=0, outbus=0, revtime=3, hfdamping=0.5, mix=0.1, level=0 | 
		   	var fx, fxIn, sig; 
		   	sig = In.ar(inbus, 2); 
			fxIn = LeakDC.ar(sig.sum) * mix; // make a mono in, leakdc it
		    	fx = AdCVerb.ar(fxIn, revtime, hfdamping, nOuts: 2);
			Out.ar(outbus, LeakDC.ar((sig * level) + fx)) // level
		},[0,0,0,0]).load(s); // having a lag here would result in big blow of noise at start
				
		roomSpec = ControlSpec.new(0.1, 10, \exponential, 0.01, 3); 
		mixSpec = ControlSpec.new(0, 1, \amp, 0.01, 0.1); 
		
		params = [ 
		   ["RevTime", "Damp", "Fx level", "Dry Level"], 
		   [\revtime, \hfdamping, \mix, \level], 
		   [roomSpec, \amp, mixSpec, \amp], 
		   [8, 0.7, 0.1, 1 ]]; 
		
		gui = if(channels == 2, {	// stereo
			XiiEffectGUI.new("AdCVerb 2x2", \xiiAdcverb2x2, params, channels, this); 
			},{				// mono
			XiiEffectGUI.new("AdCVerb 1x1", \xiiAdcverb1x1, params, channels, this); 
		});

	}
}

XiiDistortion {	
	var <>gui; // TESTING: TEMP can delete

	*new { arg server, channels;
		^super.new.initDistortion(server,channels);
		}
		
	initDistortion {arg server, channels;
		var s, params, preGainSpec, postGainSpec; 
		s = server ? Server.local;

		// mono
		SynthDef(\xiiDistortion1x1, {| inbus=0, outbus=0, pregain=0.048, postgain=15, mix = 0.5, level=0 | 
			var sig, sigtocomp, fx, y, z;
			sig = In.ar(inbus, 1);
			sigtocomp = ((sig * pregain).distort * postgain).distort;
			fx = Compander.ar(sigtocomp, sigtocomp, 1, 0, 1 );
			Out.ar(outbus, LeakDC.ar((fx * mix) + (sig *level)) );
		},[0, 0, 0.1]).load(s); 
		
		// stereo
		SynthDef(\xiiDistortion2x2, {| inbus=0, outbus=0, pregain=0.048, postgain=15, mix = 0.5, level=0 | 
			var sig, sigtocomp, fx, y, z;
			sig = In.ar(inbus, 2);
			sigtocomp = ((sig * pregain).distort * postgain).distort;
			fx = Compander.ar(sigtocomp, sigtocomp, 1, 0, 1 );
			Out.ar(outbus, LeakDC.ar((fx * mix) + (sig *level)) );
		},[0, 0, 0.1]).load(s); 

		preGainSpec = ControlSpec.new(0.01, 20, \linear, 0, 1); 
		postGainSpec = ControlSpec.new(0.01, 20, \linear, 0, 1); 
		
		params = [ 
		   ["PreGain", "PostGain", "Fx Level", "Dry Level"], 
		   [\pregain, \postgain, \mix, \level], 
		   [preGainSpec, postGainSpec, \amp, \amp], 
		   [10, 10, 0.5, 0]]; 
		
		gui = if(channels == 2, {	// stereo
			XiiEffectGUI.new("Distortion 2x2", \xiiDistortion2x2, params, channels, this); 
			},{				// mono
			XiiEffectGUI.new("Distortion 1x1", \xiiDistortion1x1, params, channels, this); 
			});
	}
}

XiiixiReverb {	
	var <>gui; // TESTING: TEMP can delete

	*new { arg server, channels;
		^super.new.initixiReverb(server, channels);
		}
		
	initixiReverb {arg server, channels;
		
		var s, predelSpec, combDecSpec, allpassDecSpec, params; 
		s = server ? Server.local;
		
		// mono
		SynthDef(\xiiReverb1x1, {| inbus=0,outbus=0,predelay=0.048,combdecay=15,allpassdecay=1,fxlevel=0.31,level=0 | 
			var sig, y, z;
			sig = In.ar(inbus, 1); 
			// predelay
			z = DelayN.ar(sig, 0.1, predelay);
			// 7 length modulated comb delays in parallel :
			y = Mix.ar(Array.fill(7,{ CombL.ar(z, 0.05, rrand(0.03, 0.05), combdecay) })); 
		
			6.do({ y = AllpassN.ar(y, 0.050, rrand(0.03, 0.05), allpassdecay) });
			Out.ar(outbus, (sig * level) + (y * (fxlevel*0.5))); // as fxlevel is 1 then I lower the vol a bit
		},[0,0,0.1,0.1,0,0]).load(s); 
		
		// stereo
		SynthDef(\xiiReverb2x2, {| inbus=0,outbus=0,predelay=0.048,combdecay=15,allpassdecay=1, fxlevel=0.31, level=0 | 
			var sig, y, z;
			sig = In.ar(inbus, 2); 
			// predelay
			z = DelayN.ar(sig, 0.1, predelay);
			// 7 length modulated comb delays in parallel :
			y = Mix.ar(Array.fill(7,{ CombL.ar(z, 0.05, rrand(0.03, 0.05), combdecay) })); 
		
			6.do({ y = AllpassN.ar(y, 0.050, rrand(0.03, 0.05), allpassdecay) });
			Out.ar(outbus, (sig*level) + (y * (fxlevel*0.5))); // as fxlevel is 1 then I lower the vol a bit
		},[0,0,0.1,0.1,0,0]).load(s); 
		
		
		predelSpec = ControlSpec.new(0.01, 0.1, \linear, 0, 0.045); 
		combDecSpec = ControlSpec.new(0.1, 15, \linear, 0, 15); 
		allpassDecSpec = ControlSpec.new(0.01, 5, \linear, 0, 1); 
		
		params = [ 
		   ["Predelay", "Combdecay", "Allpass", "Fx level", "Dry Level"], 
		   [\predelay, \combdecay, \allpassdecay, \fxlevel, \level], 
		   [predelSpec, combDecSpec, allpassDecSpec, \amp, \amp], 
		   [0.045, 15, 1, 0.31, 0.5]]; 
		
		gui = if(channels == 2, {	// stereo
			XiiEffectGUI.new("ixiReverb2x2", \xiiReverb2x2, params, channels, this);
			},{				// mono
			XiiEffectGUI.new("ixiReverb1x1", \xiiReverb1x1, params, channels, this);
		}); 
	}
}

XiiChorus {	
	var <>gui; // TESTING: TEMP can delete

	*new { arg server, channels;
		^super.new.initChorus(server, channels);
		}
		
	initChorus {arg server, channels;
	
		var s, params, preDelaySpec, depthSpec, speedSpec; 
		s = server ? Server.local;
		
		// mono
		SynthDef(\xiiChorus1x1, { arg inbus=0, outbus=0, predelay, speed, depth, ph_diff, fxlevel=0.6, level=0;
		   	var in, sig, mods, numDelays = 12;
		   	in = In.ar(inbus, 1);
		   	mods = { |i|
		      	SinOsc.kr(speed * rrand(0.92, 1.08), ph_diff * i, depth, predelay);
		  	} ! numDelays;
		   	sig = DelayL.ar(in, 0.5, mods);
		   	sig = Mix(sig); 
			Out.ar(outbus, (sig * fxlevel) + (in * level));
		},[0, 0, 0.1]).load(s); 
		
		// stereo
		SynthDef(\xiiChorus2x2, { arg inbus=0, outbus=0, predelay, speed, depth, ph_diff, fxlevel=0.6, level=0;
		   	var in, sig, mods, numOutChan=2, numDelays = 12;
		   	in = In.ar(inbus, 2);
		   	mods = { |i|
		      	SinOsc.kr(speed * rrand(0.92, 1.08), ph_diff * i, depth, predelay);
		  	} ! (numDelays * numOutChan);
		   	sig = DelayL.ar(in, 0.5, mods);
		   	sig = Mix(sig.clump(numOutChan)); 
			Out.ar(outbus, (sig * fxlevel) + (in * level));
		},[0, 0, 0.1]).load(s); 
		
		preDelaySpec = ControlSpec.new(0.0001, 0.2, \linear, 0, 0.1); 
		depthSpec = ControlSpec.new(0.0001, 0.1, \amp, 0, 0.5);
		speedSpec = ControlSpec.new(0.001, 0.5, \exponential, 0, 0.1); 
		
		params = [ 
		   ["PreDelay", "Depth", "Speed", "Fx Level", "Dry Level"], 
		   [\predelay, \depth, \speed, \fxlevel, \level], 
		   [preDelaySpec, depthSpec, speedSpec, \amp, \amp], 
		   [0.08, 0.05, 0.1, 0.5, 0]]; 
		
		gui = if(channels == 2, {	// stereo
			XiiEffectGUI.new("Chorus 2x2", \xiiChorus2x2, params, channels, this); 
			},{				// mono
			XiiEffectGUI.new("Chorus 1x1", \xiiChorus1x1, params, channels, this); 
		});
	}
}


XiiOctave {	
	var <>gui; // TESTING: TEMP can delete

	*new { arg server, channels;
		^super.new.initOctave(server, channels);
		}
		
	initOctave {arg server, channels;
	
		var pitchSpec, params, s; 
		s = server ? Server.local;
		
		// mono
		SynthDef(\xiiOctave1x1, {| inbus=0, outbus=0, pitch1=1, pitch2=1, vol1=0.25, vol2=0.25, dispersion=0, fxlevel=0.5, level=0 | 
		   var fx1, fx2, sig; 
		   sig = In.ar(inbus, 1); 
		   fx1 = PitchShift.ar(sig, 0.2, pitch1, dispersion, 0.0001);
		   fx2 = PitchShift.ar(sig, 0.2, pitch2, dispersion, 0.0001);
		   Out.ar(outbus,  ( ((fx1 * vol1) + (fx2 * vol2)) * fxlevel) + (sig * level) ); 
		},[0,0,0.1,0.1,0,0]).load(s); 
		
		// stereo
		SynthDef(\xiiOctave2x2, {| inbus=0, outbus=0, pitch1=1, pitch2=1, vol1=0.25, vol2=0.25, dispersion=0, fxlevel=0.5, level=0 | 
		   var fx1, fx2, sig; 
		   sig = In.ar(inbus, 2); 
		   fx1 = PitchShift.ar(sig, 0.2, pitch1, dispersion, 0.0001);
		   fx2 = PitchShift.ar(sig, 0.2, pitch2, dispersion, 0.0001);
		   Out.ar(outbus,  ( ((fx1 * vol1) + (fx2 * vol2)) * fxlevel) + (sig * level) ); 
		},[0,0,0.1,0.1,0,0]).load(s); 
		
		pitchSpec = ControlSpec.new(0, 2, \linear, 0.01, 1); 
		
		params = [ 
		   ["Pitch1", "Vol1", "Pitch2", "Vol2", "Dispersion", "Fx level", "Dry Level"], 
		   [\pitch1, \vol1, \pitch2, \vol2, \dispersion, \fxlevel, \level], 
		   [pitchSpec, \amp, pitchSpec, \amp, \amp, \amp, \amp], 
		   [1.25, 0.25, 0.5, 1.0, 0, 1.0, 0.2 ]]; 
		
		gui = if(channels == 2, { 	// stereo
			XiiEffectGUI.new("Octave 2x2", \xiiOctave2x2, params, channels, this); 
			},{				// mono
			XiiEffectGUI.new("Octave 1x1", \xiiOctave1x1, params, channels, this); 
		});

	}
}

XiiTremolo {	
	var <>gui; // TESTING: TEMP can delete

	*new { arg server, channels;
		^super.new.initTremolo(server, channels);
		}
		
	initTremolo {arg server, channels;
	
		var freqSpec, params, s; 
		s = server ? Server.local;
		
		// mono
		SynthDef(\xiiTremolo1x1, {| inbus=0, outbus=0, freq=1, strength=1, fxlevel=0.5, level=0 | 
		   var fx, sig; 
		   sig = In.ar(inbus, 1); 
		   fx = sig * SinOsc.ar(freq, 0, strength, 1); 
		   Out.ar(outbus, (fxlevel * fx) + (sig * level)) // level 
		},[0,0,0.1,0.1,0,0]).load(s); 
		
		// stereo
		SynthDef(\xiiTremolo2x2, {| inbus=0, outbus=0, freq=1, strength=1, fxlevel=0.5, level=0 | 
		   var fx, sig; 
		   sig = In.ar(inbus, 2); 
		   fx = sig * SinOsc.ar(freq, 0, strength, 1); 
		   Out.ar(outbus, (fxlevel * fx) + (sig * level)) // level 
		},[0,0,0.1,0.1,0,0]).load(s); 

		freqSpec = ControlSpec.new(0.1, 12, \linear, 0, 2); 
		
		params = [ 
		   ["Freq", "Strength", "Fx level", "Dry Level"], 
		   [\freq, \strength, \fxlevel, \level], 
		   [freqSpec, \amp, \amp, \amp], 
		   [0.5, 0.4, 0.65, 0 ]]; 
		
		gui = if(channels == 2, {	// stereo
			XiiEffectGUI.new("Tremolo 2x2", \xiiTremolo2x2, params, channels, this);
			}, {				// mono
			XiiEffectGUI.new("Tremolo 1x1", \xiiTremolo1x1, params, channels, this);
		});
	}
}
	
XiiEqualizer {
	var <>gui; // TESTING: TEMP can delete

	*new { arg server, channels;
		^super.new.initEqualizer(server, channels);
		}
		
	initEqualizer {arg server, channels;
	
		var s, win, size, bandSynthList, freqList;
		var mslwLeft, mslwTop;
		var signalGroup, eqGroup, msl;
		var lay, inbus, outbus, tgt, addAct, fxOn, cFreqWin, theQ; 
		var name = "Equalizer";
		var point;
		var stereoChList, monoChList;
		var onOffButt, cmdPeriodFunc;
		
		s = server ? Server.local;

		if ( (Archive.global.at(\win_position).isNil), {
			Archive.global.put(\win_position, IdentityDictionary.new);
		});
		// add pair if not there already else fetch the info
		if ( (Archive.global.at(\win_position).at(name.asSymbol).isNil), {
			point = Point(660,540);
			Archive.global.at(\win_position).put(name.asSymbol, point);
		}, {
			point = Archive.global.at(\win_position).at(name.asSymbol);
		});
		// END OF ARCHIVE CODE... Thank's blackrain again.
		
		// mono
		SynthDef(\xiiEqband1x1, { arg inbus=20, outbus=0, freq=333, rq=0.5, amp;
			var signal, in, srq;
			in = In.ar(inbus, 1);
			srq = rq.sqrt; // thanks BlackRain - Q is compensated
			signal = BPF.ar(BPF.ar(in, freq, srq), freq, srq, amp); // double BPF
			Out.ar(outbus, signal ); // 15 bands dividing 1 = 0.0666
		}).load(s);

		// stereo
		SynthDef(\xiiEqband2x2, { arg inbus=20, outbus=0, freq=333, rq=0.5, amp;
			var signal, in, srq;
			in = In.ar(inbus, 2);
			srq = rq.sqrt; // thanks BlackRain - Q is compensated
			signal = BPF.ar(BPF.ar(in, freq, srq), freq, srq, amp); // double BPF
			Out.ar(outbus, signal ); // 15 bands dividing 1 = 0.0666
		}).load(s);
				
		tgt = 1; 
		inbus = 20; 
		outbus = 0;
		addAct = \addToTail; 
		fxOn = false; 
		theQ = 0.5;
		signalGroup = Group.new(s, \addToTail);
		size = 31;
		mslwLeft = 10;
		mslwTop = 5;
		bandSynthList = List.new;
		freqList = [20, 25, 31.5, 40, 50, 63, 80, 100, 125, 160, 200, 250, 
		315, 400, 500, 630, 800, 1000, 1250, 1600, 2000, 2500, 3150, 4000, 
		5000, 6300, 8000, 10000, 12500, 16000, 20000]; // 1/3 octave bands
		
		stereoChList = ["0,1", "2,3", "4,5", "6,7", "8,9", "10,11", "12,13", "14,15", 
					"16,17", "18,19", "20,21", "22,23", "24,25", "26,27", "28,29", 
					"30,31", "32,33","34,35", "36,37", "38,39", "40,41" ];
		monoChList = ["0", "1", "2" ,"3", "4", "5", "6", "7", "8", "9", "10", "11", 
		"12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", 
		"25", "26", "27", "28", "29", "30", "31", "32", "33","34", "35", "36", "37", 
		"38", "39", "40", "41" ];

		win = SCWindow(name, Rect(point.x, point.y, 520, 243), resizable:false).front;
		
		msl = SCMultiSliderView(win, Rect(mslwLeft, mslwTop, 496, 200))
			.value_(Array.fill(size, 0.5))
			.isFilled_(false)
			.strokeColor_(Color.new255(10, 55, 10))
			.fillColor_(Color.green(alpha: 0.2))
			.valueThumbSize_(4.0)
			.indexThumbSize_(10.0)
			.gap_(6)
			.canFocus_(false)
			.background_(Color.white)
			.action_({arg xb; 
				//("index: " ++ xb.index ++ " value: " ++ xb.value.at(xb.index) ).postln;
				cFreqWin.string_(freqList.at(xb.index).asString);
				bandSynthList[xb.index].set(\amp, xb.value.at(xb.index));
			});

		SCStaticText(win, Rect(365, 215, 60, 16))
			.font_(Font("Helvetica", 9))
			.string_("band freq:"); 
		
		cFreqWin = SCStaticText(win, Rect(410, 215, 60, 16))
			.font_(Font("Helvetica", 9))
			.string_("0"); 
		
		OSCIISlider.new(win, Rect(445, 214, 60, 8), "- Q", 0.001, 1, 0.5, 0.001)
			.font_(Font("Helvetica", 9))
			.canFocus_(false)
			.action_({arg sl; 
					eqGroup.set(\rq, sl.value); 
					theQ = sl.value;
				});
						
		win.view.decorator = lay = FlowLayout(win.view.bounds, 5@215, 5@215); 
		
		// inBus
		SCStaticText(win, 30 @ 15).font_(Font("Helvetica", 9)).string_("inBus").align_(\right); 

		SCPopUpMenu(win, 40 @ 15)
			.items_(if(channels==1, {monoChList},{stereoChList}))
			.value_(0)
			.font_(Font("Helvetica", 9))
			.background_(Color.white)
			.canFocus_(false)
			.action_({ arg ch;
				if(channels==1, {inbus = ch.value}, {inbus = ch.value * 2});
				if (fxOn, { eqGroup.set(\inbus, inbus) });
			});

		// outBus
		SCStaticText(win, 30 @ 15).font_(Font("Helvetica", 9)).string_("outBus").align_(\right); 
		
		SCPopUpMenu(win, 40 @ 15)
			.items_(if(channels==1, {monoChList},{stereoChList}))
			.value_(0)
			.font_(Font("Helvetica", 9))
			.background_(Color.white)
			.canFocus_(false)
			.action_({ arg ch;
				if(channels==1, {outbus = ch.value}, {outbus = ch.value * 2});
				if (fxOn, { eqGroup.set(\outbus, outbus) });
			});
					
		// Target
		SCStaticText(win, 15 @ 15).font_(Font("Helvetica", 9)).string_("Tgt").align_(\right); 
		SCNumberBox(win, 40 @ 15).font_(Font("Helvetica", 9)).value_(tgt).action_({|v| 
		   v.value = 0.max(v.value); 
		   tgt = v.value.asInteger; 
		}); 
		
		// addAction
		SCPopUpMenu(win, 60@15) 
		   .font_(Font("Helvetica", 9)) 
		   .items_(["addToHead", "addToTail", "addAfter", "addBefore"]) 
		   .value_(1) 
		   .action_({|v| 
		      addAct = v.items.at(v.value).asSymbol; 
		   }); 
		
		// Print
		SCButton(win,18@15) 
		   .font_(Font("Helvetica", 9)) 
		   .states_([["#"]]) ;

		// on off
		onOffButt = SCButton(win, 40@15) 
		   .font_(Font("Helvetica", 9)) 
		   .states_([["On", Color.black, Color.clear],
					["Off", Color.black, Color.green(alpha:0.2)]]) 
		   .action_({|v| 
		      if ( v.value == 0, { 
		         eqGroup.free;
				bandSynthList = List.new;
		      },{ 
		         fxOn = true; 	
				eqGroup = Group.new(s, \addToTail); // was addAfter
				if(channels == 2, { 	// stereo
					size.do({arg i;
						bandSynthList.add(Synth(\xiiEqband2x2, 
									[\inbus, inbus, 
									 \outbus, outbus,
									 \freq, freqList[i], 
									 \rq, theQ, 
									 \amp, msl.value.at(i)], 
									target: eqGroup)); //addAction: \addToTail
					})
					}, {				// mono
					size.do({arg i;
						bandSynthList.add(Synth(\xiiEqband1x1, 
									[\inbus, inbus, 
									 \outbus, outbus,
									 \freq, freqList[i], 
									 \rq, theQ, 
									 \amp, msl.value.at(i)], 
									target: eqGroup)); //addAction: \addToTail
					})					
				}); // end if
		       }) 
		   }); 
		
		// drawing the line
		win.drawHook = {
				Color.new255(0, 100, 0, 100).set;
				31.do({arg i;
					Pen.moveTo(mslwLeft+ 6+ (i*16) @ (mslwTop));
					Pen.lineTo(mslwLeft+ 6+ (i*16) @ (mslwTop+200));
					Pen.stroke
				});
				Pen.moveTo(mslwLeft @ (mslwTop+100));
				Pen.lineTo(mslwLeft+496 @ (mslwTop+100));
				Pen.stroke;
			};
			
		cmdPeriodFunc = { onOffButt.valueAction_(0);};
		CmdPeriod.add(cmdPeriodFunc);

		win.onClose_({ 
			var t;
			size.do({arg i; bandSynthList[i].free}); 
			CmdPeriod.remove(cmdPeriodFunc);
			~globalWidgetList.do({arg widget, i; if(widget == this, {t = i})});
			~globalWidgetList.removeAt(t);
			point = Point(win.bounds.left, win.bounds.top);
			Archive.global.at(\win_position).put(name.asSymbol, point);
			}); 
		win.refresh;
	}
}
	
XiiRandomPanner {	
	var <>gui; // TESTING: TEMP can delete

	*new { arg server, channels;
		^super.new.initRandomPanner(server, channels);
		}
		
	initRandomPanner {arg server, channels;

		var volSpec, trigfreqSpec, strengthSpec, params, s; 
		
		s = server ? Server.local;
		
		// mono
		SynthDef(\xiiRandompanner1x2, { arg inbus=0, outbus=0, trigfreq=10, strenght=0.81;
			var sig, signal, pan, trig;
			trig = 	Dust.kr(trigfreq);
			sig =   	In.ar(inbus, 1);
			pan= 	TBetaRand.kr(-1, 1, strenght, strenght, trig);
			signal = 	Pan2.ar(sig, pan);
			Out.ar(outbus, signal);
		}).load(s);
		
		// stereo
		SynthDef(\xiiRandompanner2x2, { arg inbus=0, outbus=0, trigfreq=10, strenght=0.81;
			var sig, left, right, panL, panR, trig;
			trig = 	Dust.kr(trigfreq);
			sig =   	In.ar(inbus, 2);
			panL= 	TBetaRand.kr(-1, 1, strenght, strenght, trig);
			panR= 	TBetaRand.kr(-1, 1, strenght, strenght, trig);
			left =  	Pan2.ar(sig[0], panL);
			right = 	Pan2.ar(sig[1], panR);
			Out.ar(outbus, left);
			Out.ar(outbus, right);
		}).load(s);


		trigfreqSpec = ControlSpec.new(0.5, 10, \exponential, 0, 2); 
		strengthSpec = ControlSpec.new(1, 0.01, \linear, 0, 0.5); 

		params = [ 
		   ["Freq", "Strength"], 
		   [ \trigfreq, \strength], 
		   [trigfreqSpec, strengthSpec], 
		   [0.2, 0.5]]; 
		
		gui = if(channels == 2, { 	// stereo
			XiiEffectGUI.new("RandomPanner 2x2", \xiiRandompanner2x2, params, channels, this); 
			}, {				// mono
			XiiEffectGUI.new("RandomPanner 1x2", \xiiRandompanner1x2, params, channels, this);
		})
	}
}

XiiCombVocoder {	
	var <>gui; // TESTING: TEMP can delete

	*new { arg server, channels;
		^super.new.initCombVocoder(server, channels);
		}
		
	initCombVocoder {arg server, channels;
		var volSpec, delayTailSpec, params, s; 
		s = server ? Server.local;
		// mono
		SynthDef(\xiiCombvocoder1x1, {arg inbus=0,
							outbus=0, 
							maxDelay=2, // hardcoded here
							delay=0.4, 
							feedback=0.0, 
							fxlevel = 0.7, 
							level=1.0;
		   var fx, sig; 
		   sig = In.ar(inbus,1); 
		   fx = sig + LocalIn.ar(1); 
		   fx = DelayC.ar(fx, maxDelay, delay); 
		   LocalOut.ar(fx * feedback); 
		   Out.ar(outbus, (fx * fxlevel) + (sig * level)) 
		},[0.2,0.2,0.1,0.1]).load(s); 
		
		// stereo
		SynthDef(\xiiCombvocoder2x2, {arg inbus=0,
							outbus=0, 
							maxDelay=2, // hardcoded here
							delay=0.4, 
							feedback=0.0, 
							fxlevel = 0.7, 
							level=1.0;
		   var fx, sig; 
		   sig = In.ar(inbus,2); 
		   fx = sig + LocalIn.ar(2); 
		   fx = DelayC.ar(fx, maxDelay, delay); 
		   LocalOut.ar(fx * feedback); 
		   Out.ar(outbus, (fx * fxlevel) + (sig * level)) 
		},[0.2,0.2,0.1,0.1]).load(s); 

		delayTailSpec = ControlSpec.new(0.3, 0.001, \exponential, 0, 1.2); 
		volSpec = ControlSpec.new(0.3, 0.999, \exponential, 0, 1.2); 

		params = [ 
		   ["Delay", "Feedback", "Fx level", "Dry Level"], 
		   [ \delay, \feedback, \fxlevel, \level], 
		   [delayTailSpec, volSpec, \amp, \amp], 
		   [0.025, 0.87, 1.0, 0]]; 
		
		gui = if(channels == 2, { 	// stereo
			XiiEffectGUI.new("CombVocoder 2x2", \xiiCombvocoder2x2, params, channels, this); 
			}, {				// mono
			XiiEffectGUI.new("CombVocoder 1x1", \xiiCombvocoder1x1, params, channels, this);
		})
	}
}

XiiMRRoque {	
	var <>gui;

	*new { arg server, channels;
		^super.new.initMRRoque(server, channels);
		}
		
	initMRRoque {arg server, channels;

		var rateSpec, timeSpec, params, s, buffer; 
		
		s = server ? Server.local;
		
		// mono
		SynthDef(\xiiMrRoque1x1, {|inbus=0, outbus=0, mix = 0.25, room = 0.15, damp = 0.5, 
				outmix = 0.25, outroom = 0.15, outdamp = 0.5, 
				bufnum, rate=1, end = 4, vol=1|
			
			var in, reverb, reverb2, signal;
			
			in = In.ar(inbus, 1);
			reverb = FreeVerb.ar(in, mix, room, damp);
			BufWr.ar(reverb, bufnum, Phasor.ar(0, 1, 0, 44100*end));
			signal = BufRd.ar(1, bufnum, Phasor.ar(0, BufRateScale.kr(0) * rate, 0, 44100*end));
			reverb2 = FreeVerb.ar(signal, outmix, outroom, outdamp);	
			Out.ar(0, (signal+reverb2) * vol);
		},[0.2,0.2,0.1,0.1]).load(s); 
				
		// stereo
		SynthDef(\xiiMrRoque2x2, {|inbus=0, outbus=0, mix = 0.25, room = 0.15, damp = 0.5, 
				outmix = 0.25, outroom = 0.15, outdamp = 0.5, 
				bufnum, rate=1, end = 4, vol=1|
			
			var in, reverb, reverb2, signal;
			
			in = In.ar(inbus, 2);
			reverb = FreeVerb.ar(in, mix, room, damp);
			BufWr.ar(reverb, bufnum, Phasor.ar(0, 1, 0, 44100*end));
			signal = BufRd.ar(2, bufnum, Phasor.ar(0, BufRateScale.kr(0) * rate, 0, 44100*end));
			reverb2 = FreeVerb.ar(signal, outmix, outroom, outdamp);	
			Out.ar(0, (signal+reverb2) * vol);
		},[0.2,0.2,0.1,0.1]).load(s); 

		timeSpec = ControlSpec.new(1.0, 4.0, \lin, 0.1, 4.0); 
		rateSpec = ControlSpec.new(-1.5, 1.5, \lin, 0.1, -1.0); 

		if(channels == 2, { 	// stereo
			buffer = Buffer.alloc(s, 44100 * 4.0, 2); // a four second 2 channel Buffer
		}, {
			buffer = Buffer.alloc(s, 44100 * 4.0, 1); // a four second 1 channel Buffer
		});

		params = [ 
		   ["Time", "Rate", "PreMix", "PreRoom", "PreDamp", "Mix", "Room", "Damp", "Volume"], 
		   [ \end, \rate, \mix, \room, \damp, \outmix, \outroom, \outdamp, \vol, \bufnum], 
		   [timeSpec, rateSpec, \amp, \amp, \amp, \amp, \amp, \amp, \amp], 
		   [4, -1.0, 0.4, 0.4, 0.2, 0.4, 0.4, 0.2, 1.0, buffer.bufnum]]; 
		
		gui = if(channels == 2, { 	// stereo
			XiiEffectGUI.new("MR Roque 2x2", \xiiMrRoque2x2, params, channels, this); 
			}, {				// mono
			XiiEffectGUI.new("MR Roque 1x1", \xiiMrRoque1x1, params, channels, this);
		})
	}
}

XiiMultiDelay {	
	var <>gui;

	*new { arg server, channels;
		^super.new.initXiiMultiDelay(server, channels);
		}
		
	initXiiMultiDelay {arg server, channels;

		var delay1Spec, delay2Spec, delay3Spec, delay4Spec, params, s; 
		var buffer;
		
		s = server ? Server.local;
		
		// mono
		SynthDef(\xiiMultidelay1x1, {arg outbus=0, inbus=0, amp = 1, bufnum,
				dtime1=1, d1amp = 0.6, 
				dtime2=3, d2amp = 0.6,
				dtime3=4, d3amp = 0.6,
				dtime4=4.5, d4amp = 0.6;
			var in, delays, d1, d2, d3, d4;
			in = In.ar(inbus, 1); 
			d1 = BufDelayN.ar(bufnum, in, dtime1) * d1amp;
			d2 = BufDelayN.ar(bufnum, in, dtime2) * d2amp;
			d3 = BufDelayN.ar(bufnum, in, dtime3) * d3amp;
			d4 = BufDelayN.ar(bufnum, in, dtime4) * d4amp;
			Out.ar(outbus, in + d1 + d2 + d3 + d4);
		}).load(s);

		// stereo
		SynthDef(\xiiMultidelay2x2, {arg outbus=0, inbus=0, amp = 1, bufnum,
				dtime1=1, d1amp = 0.6, 
				dtime2=3, d2amp = 0.6,
				dtime3=4, d3amp = 0.6,
				dtime4=4.5, d4amp = 0.6;
				
			var in, delays, d1, d2, d3, d4;
			in = In.ar(inbus, 2); 
			d1 = BufDelayN.ar(bufnum, in, dtime1) * d1amp;
			d2 = BufDelayN.ar(bufnum, in, dtime2) * d2amp;
			d3 = BufDelayN.ar(bufnum, in, dtime3) * d3amp;
			d4 = BufDelayN.ar(bufnum, in, dtime4) * d4amp;
			Out.ar(outbus, in + d1 + d2 + d3 + d4);
		}).load(s);

		if(channels == 2, { 	// stereo
			buffer = Buffer.alloc(s, 44100 * 10.0, 2); // a 10 second 2 channel Buffer
		}, {
			buffer = Buffer.alloc(s, 44100 * 10.0, 1); // a 10 second 1 channel Buffer
		});

		delay1Spec = ControlSpec.new(0.1, 10, \lin, 0.1, 1); 
		delay2Spec = ControlSpec.new(0.1, 10, \lin, 0.1, 2); 
		delay3Spec = ControlSpec.new(0.1, 10, \lin, 0.1, 3.5); 
		delay4Spec = ControlSpec.new(0.1, 10, \lin, 0.1, 4); 
		
		params = [ 
		   ["Dry vol", "Delay 1", "vol", "Delay 2", "vol", "Delay 3", "vol", "Delay 4", "vol"], 
		   [ \amp, \dtime1, \d1amp, \dtime2, \d2amp, \dtime3, \d3amp, \dtime4, \d4amp, \bufnum], 
		   [\amp, delay1Spec, \amp, delay2Spec, \amp, delay3Spec, \amp, delay4Spec, \amp], 
		   [1, 1, 1, 2, 1, 3.5, 1, 4, 1, buffer.bufnum]]; 
		
		gui = if(channels == 2, { 	// stereo
			XiiEffectGUI.new("MultiDelay 2x2", \xiiMultidelay2x2, params, channels, this);
			}, {				// mono
			XiiEffectGUI.new("MultiDelay 1x1", \xiiMultidelay1x1, params, channels, this);		})
	}
}


