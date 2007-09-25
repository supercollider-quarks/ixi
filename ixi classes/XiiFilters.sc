XiiBandpass {	
	var <>xiigui;
	
	*new { arg server, channels, setting = nil;
		^super.new.initXiiBandpass(server, channels, setting);
		}
		
	initXiiBandpass {arg server, channels, setting;

		var freqSpec, rqSpec, params, s; 
		
		s = server ? Server.local;
		
		// mono
		SynthDef(\xiiBPF1x1, {arg inbus=0,
							outbus=0, 
							freq=200, // hardcoded here
							rq=0.4, 
							fxlevel = 0.7, 
							level=1.0;
							
		� �var fx, sig; 
		� �sig = InFeedback.ar(inbus,1); 
		� �fx = BPF.ar(sig, freq, rq); 
		� �Out.ar(outbus, (fx * fxlevel) + (sig * level)) 
		}).load(s); 	

		// stereo
		SynthDef(\xiiBPF2x2, {arg inbus=0,
							outbus=0, 
							freq=200, // hardcoded here
							rq=0.4, 
							fxlevel = 0.7, 
							level=1.0;
							
		� �var fx, sig; 
		� �sig = InFeedback.ar(inbus, 2); 
		� �fx = BPF.ar(sig, freq, rq); 
		� �Out.ar(outbus, (fx * fxlevel) + (sig * level)) 
		}).load(s); 	

		freqSpec = ControlSpec.new(20, 20000, \exponential, 1, 2000); 
		rqSpec = ControlSpec.new(0.0001, 1, \exponential, 0.0001, 0.5); 
		
		
		params = [ 
		� �["Freq", "RQ", "Fx level", "Dry Level"], 
		� �[ \freq, \rq, \fxlevel, \level], 
		� �[freqSpec, rqSpec, \amp, \amp], 
		� �if(setting.isNil.not, {setting[5]}, {[2000, 0.5, 1, 0]})
		]; 
		
		xiigui = if(channels == 2, { 	// stereo
			XiiEffectGUI.new("Bandpass Filter 2x2", \xiiBPF2x2, params, channels, this, setting); /// 
			}, {				// mono
			XiiEffectGUI.new("Bandpass Filter 1x1", \xiiBPF1x1, params, channels, this, setting); /// 
		});
	}
}

XiiLowpass {	
	var <>xiigui;
	
	*new { arg server, channels, setting = nil;
		^super.new.initXiiLowpass(server, channels, setting);
		}
		
	initXiiLowpass {arg server, channels, setting;
		var freqSpec, params, s; 
		s = server ? Server.local;
		
		// mono
		SynthDef(\xiiLPF1x1, {arg inbus=0,
							outbus=0, 
							freq=200, // hardcoded here
							fxlevel = 0.7, 
							level=1.0;
							
		� �var fx, sig; 
		� �sig = InFeedback.ar(inbus,1); 
		� �fx = LPF.ar(sig, freq); 
		� �Out.ar(outbus, (fx * fxlevel) + (sig * level)) 
		}).load(s); 	

		// stereo
		SynthDef(\xiiLPF2x2, {arg inbus=0,
							outbus=0, 
							freq=200, // hardcoded here
							fxlevel = 0.7, 
							level=1.0;
							
		� �var fx, sig; 
		� �sig = InFeedback.ar(inbus, 2); 
		� �fx = LPF.ar(sig, freq); 
		� �Out.ar(outbus, (fx * fxlevel) + (sig * level)) 
		}).load(s); 	

		freqSpec = ControlSpec.new(20, 20000, \exponential, 1, 2000); 
		
		params = [ 
		� �["Freq", "Fx level", "Dry Level"], 
		� �[ \freq, \fxlevel, \level], 
		� �[freqSpec, \amp, \amp], 
		� �if(setting.isNil.not, {setting[5]}, {[2000, 1, 0]})
		]; 
		
		xiigui = if(channels == 2, { 	// stereo
			XiiEffectGUI.new("Lowpass Filter 2x2", \xiiLPF2x2, params, channels, this, setting); /// 
			}, {				// mono
			XiiEffectGUI.new("Lowpass Filter 1x1", \xiiLPF1x1, params, channels, this, setting); /// 
		})
	}
}

XiiHighpass {	
	var <>xiigui;
	
	*new { arg server, channels, setting = nil;
		^super.new.initXiiHighpass(server, channels, setting);
		}
		
	initXiiHighpass {arg server, channels, setting;
		var freqSpec, params, s; 
		s = server ? Server.local;
		
		// mono
		SynthDef(\xiiHPF1x1, {arg inbus=0,
							outbus=0, 
							freq=200, // hardcoded here
							fxlevel = 0.7, 
							level=1.0;
							
		� �var fx, sig; 
		� �sig = InFeedback.ar(inbus,1); 
		� �fx = HPF.ar(sig, freq); 
		� �Out.ar(outbus, (fx * fxlevel) + (sig * level)) 
		}).load(s); 	

		// stereo
		SynthDef(\xiiHPF2x2, {arg inbus=0,
							outbus=0, 
							freq=200, // hardcoded here
							fxlevel = 0.7, 
							level=1.0;
							
		� �var fx, sig; 
		� �sig = InFeedback.ar(inbus, 2); 
		� �fx = HPF.ar(sig, freq); 
		� �Out.ar(outbus, (fx * fxlevel) + (sig * level)) 
		}).load(s); 	

		freqSpec = ControlSpec.new(20, 20000, \exponential, 1, 2000); 
		
		params = [ 
		� �["Freq", "Fx level", "Dry Level"], 
		� �[ \freq, \fxlevel, \level], 
		� �[freqSpec, \amp, \amp], 
		� �if(setting.isNil.not, {setting[5]}, {[2000, 1, 0]})
		]; 
		
		xiigui = if(channels == 2, { 	// stereo
			XiiEffectGUI.new("Highpass Filter 2x2", \xiiHPF2x2, params, channels, this, setting); /// 
			}, {				// mono
			XiiEffectGUI.new("Highpass Filter 1x1", \xiiHPF1x1, params, channels, this, setting); /// 
		})
	}
}


XiiRLowpass {	
	var <>xiigui;
	
	*new { arg server, channels, setting = nil;
		^super.new.initXiiRLowpass(server, channels, setting);
		}
		
	initXiiRLowpass {arg server, channels, setting;

		var freqSpec, rqSpec, params, s; 
		
		s = server ? Server.local;
		
		// mono
		SynthDef(\xiiRLPF1x1, {arg inbus=0,
							outbus=0, 
							freq=200, // hardcoded here
							rq=0.4, 
							fxlevel = 0.7, 
							level=1.0;
							
		� �var fx, sig; 
		� �sig = InFeedback.ar(inbus,1); 
		� �fx = RLPF.ar(sig, freq, rq); 
		� �Out.ar(outbus, (fx * fxlevel) + (sig * level)) 
		}).load(s); 	

		// stereo
		SynthDef(\xiiRLPF2x2, {arg inbus=0,
							outbus=0, 
							freq=200, // hardcoded here
							rq=0.4, 
							fxlevel = 0.7, 
							level=1.0;
							
		� �var fx, sig; 
		� �sig = InFeedback.ar(inbus, 2); 
		� �fx = RLPF.ar(sig, freq, rq); 
		� �Out.ar(outbus, (fx * fxlevel) + (sig * level)) 
		}).load(s); 	

		freqSpec = ControlSpec.new(20, 20000, \exponential, 1, 2000); 
		rqSpec = ControlSpec.new(0.01, 1, \exponential, 0.01, 0.5); 
		
		params = [ 
		� �["Freq", "RQ", "Fx level", "Dry Level"], 
		� �[ \freq, \rq, \fxlevel, \level], 
		� �[freqSpec, rqSpec, \amp, \amp], 
		� �if(setting.isNil.not, {setting[5]}, {[2000, 0.5, 1, 0]})
		]; 
		
		xiigui = if(channels == 2, { 	// stereo
			XiiEffectGUI.new("Resonant Lowpass Filter 2x2", \xiiRLPF2x2, params, channels, this, setting);
			}, {				// mono
			XiiEffectGUI.new("Resonant Lowpass Filter 1x1", \xiiRLPF1x1, params, channels, this, setting); 
		})
	}
}


XiiRHighpass {	
	var <>xiigui;
	
	*new { arg server, channels, setting = nil;
		^super.new.initXiiRHighpass(server, channels, setting);
		}
		
	initXiiRHighpass {arg server, channels, setting;

		var freqSpec, rqSpec, params, s; 
		
		s = server ? Server.local;
		
		// mono
		SynthDef(\xiiRHPF1x1, {arg inbus=0,
							outbus=0, 
							freq=200, // hardcoded here
							rq=0.4, 
							fxlevel = 0.7, 
							level=1.0;
							
		� �var fx, sig; 
		� �sig = InFeedback.ar(inbus,1); 
		� �fx = RHPF.ar(sig, freq, rq); 
		� �Out.ar(outbus, (fx * fxlevel) + (sig * level)) 
		}).load(s); 	

		// stereo
		SynthDef(\xiiRHPF2x2, {arg inbus=0,
							outbus=0, 
							freq=200, // hardcoded here
							rq=0.4, 
							fxlevel = 0.7, 
							level=1.0;
							
		� �var fx, sig; 
		� �sig = InFeedback.ar(inbus, 2); 
		� �fx = RHPF.ar(sig, freq, rq); 
		� �Out.ar(outbus, (fx * fxlevel) + (sig * level)) 
		}).load(s); 	

		freqSpec = ControlSpec.new(20, 20000, \exponential, 1, 2000); 
		rqSpec = ControlSpec.new(0.01, 1, \exponential, 0.01, 0.5); 
		
		params = [ 
		� �["Freq", "RQ", "Fx level", "Dry Level"], 
		� �[ \freq, \rq, \fxlevel, \level], 
		� �[freqSpec, rqSpec, \amp, \amp], 
		� �if(setting.isNil.not, {setting[5]}, {[2000, 0.5, 1, 0]})
		]; 
		
		xiigui = if(channels == 2, { 	// stereo
			XiiEffectGUI.new("Resonant Highpass Filter 2x2", \xiiRHPF2x2, params, channels, this, setting);
			}, {				// mono
			XiiEffectGUI.new("Resonant Highpass Filter 1x1", \xiiRHPF1x1, params, channels, this, setting);
		})
	}
}


XiiResonant {	
	var <>xiigui;
	
	*new { arg server, channels, setting = nil;
		^super.new.initXiiResonant(server, channels, setting);
		}
		
	initXiiResonant {arg server, channels, setting;

		var freqSpec, rqSpec, params, s; 
		
		s = server ? Server.local;
		
		// mono
		SynthDef(\xiiResonant1x1, {arg inbus=0,
							outbus=0, 
							freq=200, // hardcoded here
							rq=0.4, 
							fxlevel = 0.7, 
							level=1.0;
							
		� �var fx, sig; 
		� �sig = InFeedback.ar(inbus,1); 
		� �fx = Resonz.ar(sig, freq, rq); 
		� �Out.ar(outbus, (fx * fxlevel) + (sig * level)) 
		}).load(s); 	

		// stereo
		SynthDef(\xiiResonant2x2, {arg inbus=0,
							outbus=0, 
							freq=200, // hardcoded here
							rq=0.4, 
							fxlevel = 0.7, 
							level=1.0;
							
		� �var fx, sig; 
		� �sig = InFeedback.ar(inbus, 2); 
		� �fx = Resonz.ar(sig, freq, rq); 
		� �Out.ar(outbus, (fx * fxlevel) + (sig * level)) 
		}).load(s); 	

		freqSpec = ControlSpec.new(20, 20000, \exponential, 1, 2000); 
		rqSpec = ControlSpec.new(0.0001, 1, \exponential, 0.0001, 0.5); 
		
		params = [ 
		� �["Freq", "RQ", "Fx level", "Dry Level"], 
		� �[ \freq, \rq, \fxlevel, \level], 
		� �[freqSpec, rqSpec, \amp, \amp], 
		� �if(setting.isNil.not, {setting[5]}, {[2000, 0.5, 1, 0]})
		]; 
		
		xiigui = if(channels == 2, { 	// stereo
			XiiEffectGUI.new("Resonant Filter 2x2", \xiiResonant2x2, params, channels, this, setting);
			}, {				// mono
			XiiEffectGUI.new("Resonant Filter 1x1", \xiiResonant1x1, params, channels, this, setting);
		})
	}
}


XiiKlanks {	
	var <>xiigui;
	
	*new { arg server, channels, setting = nil;
		^super.new.initXiiKlanks(server, channels, setting);
		}
		
	initXiiKlanks {arg server, channels, setting;

		var freqSpec, gainSpec, ringSpec, params, s; 
		
		s = server ? Server.local;
		
		// mono
		SynthDef(\xiiKlanks1x1, {arg inbus=0,
							outbus=0, gain=0.01,
							freq1, freq2, freq3, freq4,
							amp1, amp2, amp3, amp4,
							ring1, ring2, ring3, ring4,
							fxlevel = 0.7, 
							level=0;
							
		� �var fx1, fx2, fx3, fx4, sig; 
		� �sig = InFeedback.ar(inbus, 1); 
		� �fx1 = Ringz.ar(sig*gain, freq1, ring1, amp1); 
		� �fx2 = Ringz.ar(sig*gain, freq2, ring2, amp2); 
		� �fx3 = Ringz.ar(sig*gain, freq3, ring3, amp3); 
		� �fx4 = Ringz.ar(sig*gain, freq4, ring4, amp4); 
		� �Out.ar(outbus, ((fx1+fx2+fx3+fx4) *fxlevel) + (sig * level)) 
		}).load(s); 	

		// stereo
		SynthDef(\xiiKlanks2x2, {arg inbus=0,
							outbus=0, gain=0.01,
							freq1, freq2, freq3, freq4,
							amp1, amp2, amp3, amp4,
							ring1, ring2, ring3, ring4,
							fxlevel = 0.7, 
							level=0;
							
		� �var fx1, fx2, fx3, fx4, sig; 
		� �sig = InFeedback.ar(inbus, 2); 
		� �fx1 = Ringz.ar(sig*gain, freq1, ring1, amp1); 
		� �fx2 = Ringz.ar(sig*gain, freq2, ring2, amp2); 
		� �fx3 = Ringz.ar(sig*gain, freq3, ring3, amp3); 
		� �fx4 = Ringz.ar(sig*gain, freq4, ring4, amp4); 
		� �Out.ar(outbus, ((fx1+fx2+fx3+fx4) *fxlevel) + (sig * level)) 
		}).load(s); 	

		freqSpec = ControlSpec.new(20, 20000, \exponential, 1, 2000); 
		gainSpec = ControlSpec.new(0.001, 1, \exponential, 0.001, 0.01); 
		ringSpec = ControlSpec.new(0.01, 4, \linear, 0.01, 1); 
		
		params = [ 
		� �["Gain", "Freq1", "Amp1", "Ring1", "Freq2", "Amp2", "Ring2", "Freq3", "Amp3", "Ring3", 
			"Freq4", "Amp4", "Ring4", "Fx level", "Dry Level"], 
		� �[\gain, \freq1, \amp1, \ring1, \freq2, \amp2, \ring2, \freq3, \amp3, \ring3, 
			\freq4, \amp4, \ring4, \fxlevel, \level], 
		� �[gainSpec, freqSpec, \amp, ringSpec, freqSpec, \amp, ringSpec, 
			freqSpec, \amp, ringSpec, freqSpec, \amp, ringSpec, \amp, \amp], 
		� �if(setting.isNil.not, {setting[5]}, 
				{[0.004, 400, 1.0, 1.0, 600, 0.8, 0.9, 800, 0.7, 1.0, 1000, 0.8, 0.6, 0.4, 0]})
		]; 
		
		xiigui = if(channels == 2, { 	// stereo
			XiiEffectGUI.new("Klank Filters 2x2", \xiiKlanks2x2, params, channels, this, setting);
			}, {				// mono
			XiiEffectGUI.new("Klank Filters 1x1", \xiiKlanks1x1, params, channels, this, setting);
		})
	}
}

