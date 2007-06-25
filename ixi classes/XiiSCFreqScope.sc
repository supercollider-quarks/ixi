// SCFreqScope and FreqScope
// by Lance Putnam
// lance@uwalumni.com

XiiSCFreqScope : SCScope {

	classvar <server;
	var <scopebuf, <fftbuf;
	var <active, <node, <inBus, <dbRange, dbFactor, rate, <freqMode;
	
	*viewClass { ^SCScope }
	
	*initClass { server = Server.default }
	
	*new { arg parent, bounds;
		bounds.width = 511;
		^super.new(parent, bounds).initSCFreqScope
	}
	
	initSCFreqScope {
		active=false;
		inBus=0;
		dbRange = 96;
		dbFactor = 2/dbRange;
		rate = 4;
		freqMode = 0;
		server = Server.default;
		
		node = server.nextNodeID;
	}
	
	sendSynthDefs {
		// dbFactor -> 2/dbRange
		
		// linear
		SynthDef(\XiiFreqScope0, { arg in=0, fftbufnum=0, scopebufnum=1, rate=4, phase=1, dbFactor = 0.02;
			var signal, chain, result, phasor, numSamples, mul, add;
			mul = 0.00285;
			numSamples = (BufSamples.kr(fftbufnum) - 2) * 0.5; // 1023 (bufsize=2048)
			signal = In.ar(in, 2);
			signal = Mix.ar(signal);
			chain = FFT(fftbufnum, signal);
			chain = PV_MagSmear(chain, 1);
			// -1023 to 1023, 0 to 2046, 2 to 2048
			phasor = LFSaw.ar(rate/BufDur.kr(fftbufnum), phase, numSamples, numSamples + 2);
			phasor = phasor.round(2); // the evens are magnitude
			ScopeOut.ar( ((BufRd.ar(1, fftbufnum, phasor, 1) * mul).ampdb * dbFactor) + 1, scopebufnum);
		}).send(server);
		
		// logarithmic
		SynthDef(\XiiFreqScope1, { arg in=0, fftbufnum=0, scopebufnum=1, rate=4, phase=1, dbFactor = 0.02;
			var signal, chain, result, phasor, halfSamples, mul, add;
			mul = 0.00285;
			halfSamples = BufSamples.kr(fftbufnum) * 0.5;
			signal = In.ar(in, 2);
			signal = Mix.ar(signal);
			chain = FFT(fftbufnum, signal);
			chain = PV_MagSmear(chain, 1);
			phasor = halfSamples.pow(LFSaw.ar(rate/BufDur.kr(fftbufnum), phase, 0.5, 0.5)) * 2; // 2 to bufsize
			phasor = phasor.round(2); // the evens are magnitude
			ScopeOut.ar( ((BufRd.ar(1, fftbufnum, phasor, 1) * mul).ampdb * dbFactor) + 1, scopebufnum);
		}).send(server);
		
//		SynthDef("freqScope2", { arg in=0, fftbufnum=0, scopebufnum=1, rate=4, phase=1, dbFactor = 0.02;
//			var signal, chain, result, phasor, numSamples, mul, add;
//			mul = 0.00285;
//			numSamples = (BufSamples.kr(fftbufnum)) - 2;
//			signal = In.ar(in);
//			chain = FFT(fftbufnum, signal);
//			chain = PV_MagSmear(chain, 1);
//			phasor = ((LFSaw.ar(rate/BufDur.kr(fftbufnum), phase, 0.5, 0.5).squared * numSamples)+1).round(2);
//			ScopeOut.ar( ((BufRd.ar(1, fftbufnum, phasor, 1) * mul).ampdb * dbFactor) + 1, scopebufnum);
//		}).send(server);
//		
//		SynthDef("freqScope3", { arg in=0, fftbufnum=0, scopebufnum=1, rate=4, phase=1, dbFactor = 0.02;
//			var signal, chain, result, phasor, numSamples, mul, add;
//			mul = 0.00285;
//			numSamples = (BufSamples.kr(fftbufnum)) - 2;
//			signal = In.ar(in);
//			chain = FFT(fftbufnum, signal);
//			chain = PV_MagSmear(chain, 1);
//			phasor = ((LFSaw.ar(rate/BufDur.kr(fftbufnum), phase, 0.5, 0.5).cubed * numSamples)+1).round(2);
//			ScopeOut.ar( ((BufRd.ar(1, fftbufnum, phasor, 1) * mul).ampdb * dbFactor) + 1, scopebufnum);
//		}).send(server);

		"XiiSCFreqScope: SynthDefs sent".postln;
	}
	
	allocBuffers {
		
		scopebuf = Buffer.alloc(server, 2048, 1, 
			{ arg sbuf;
				this.bufnum = sbuf.bufnum;
				fftbuf = Buffer.alloc(server, 2048, 1,
				{ arg fbuf;
					("XiiSCFreqScope: Buffers allocated (" 
						++ sbuf.bufnum.asString ++ ", "
						++ fbuf.bufnum.asString ++ ")").postln;
				});
			});
	}
	
	freeBuffers {
		if( scopebuf.notNil && fftbuf.notNil, {
			("XiiSCFreqScope: Buffers freed (" 
				++ scopebuf.bufnum.asString ++ ", "
				++ fftbuf.bufnum.asString ++ ")").postln;
			scopebuf.free; scopebuf = nil;
			fftbuf.free; fftbuf = nil;
		});
	}
	
	start {

		// sending bundle messes up phase of LFSaw in SynthDef (????)
//		server.sendBundle(server.latency, 
//			["/s_new", "freqScope", node, 1, 0, 
//				\in, inBus, \mode, mode, 
//				\fftbufnum, fftbuf.bufnum, \scopebufnum, scopebuf.bufnum]);
		node = server.nextNodeID; // get new node just to be safe
		server.sendMsg("/s_new", "XiiFreqScope" ++ freqMode.asString, node, 1, 0, 
				\in, inBus, \dbFactor, dbFactor,
				\fftbufnum, fftbuf.bufnum, \scopebufnum, scopebuf.bufnum);
	}
	
	kill {
		this.eventSeq(0.5, {this.active_(false)}, {this.freeBuffers});
	}
	
	// used for sending in order commands to server
	eventSeq { arg delta ... funcs;
		Routine.run({
			(funcs.size-1).do({ arg i;
				funcs[i].value;
				delta.wait;
			});
			funcs.last.value;
			
		}, 64, AppClock);
	}
	
	active_ { arg bool;
		server.serverRunning.postln;
		if(server.serverRunning, { // don't do anything unless server is running
		
		if(bool, {
			if(active.not, {
				CmdPeriod.add(this);
				if((scopebuf.isNil) || (fftbuf.isNil), { // first activation
					this.eventSeq(0.5, {this.sendSynthDefs}, {this.allocBuffers}, {this.start; "starting".postln;});
				}, {
					this.start; "Starting freqscope".postln;
				});
			});
		}, {
			if(active, {
				server.sendBundle(server.latency, ["/n_free", node]);
				CmdPeriod.remove(this);
			});
		});
		active=bool;
		
		});
		^this
	}
	
	inBus_ { arg num;
		inBus = num;
		if(active, {
			server.sendBundle(server.latency, ["/n_set", node, \in, inBus]);
		});
		^this
	}
	
	dbRange_ { arg db;
		dbRange = db;
		dbFactor = 2/db;
		if(active, {
			server.sendBundle(server.latency, ["/n_set", node, \dbFactor, dbFactor]);
		});		
	}
	
	freqMode_ { arg mode;
		freqMode = mode.asInteger.clip(0,1);
		if(active, {
			server.sendMsg("/n_free", node);
			node = server.nextNodeID;
			this.start;
		});		
	}
	
	cmdPeriod {
		this.changed(\cmdPeriod);
		if(active == true, {
			CmdPeriod.remove(this);
			active = false;
			node = server.nextNodeID;
			this.active_(true);
		});
	}
	
}
/*
FreqScope {
	
	var <scope, <window;

	*new { arg height=300, busNum=0, scopeColor, bgColor;
		//make scope
		var rect, scope, window, pad, font, freqLabel, freqLabelDist, dbLabel, dbLabelDist;
		var setFreqLabelVals, setDBLabelVals;
		if(scopeColor.isNil, { scopeColor = Color.green });
		if(bgColor.isNil, { bgColor = Color.green(0.1) });
		
		rect = Rect(0, 0, 511, height);
		pad = [30, 38, 14, 10]; // l,r,t,b
		font = Font("Monaco", 9);
		freqLabel = Array.newClear(12);
		freqLabelDist = rect.width/(freqLabel.size-1);
		dbLabel = Array.newClear(17);
		dbLabelDist = rect.height/(dbLabel.size-1);
		
		setFreqLabelVals = { arg mode, bufsize;
			var kfreq, factor, halfSize;
			
			factor = 1/(freqLabel.size-1);
			halfSize = bufsize * 0.5;
			
			freqLabel.size.do({ arg i;
				if(mode == 1, {
					kfreq = (halfSize.pow(i * factor) - 1)/(halfSize-1) * 22.05;
				},{
					kfreq = i * factor * 22.05;
				});
					
				if(kfreq > 1.0, {
					freqLabel[i].string_( kfreq.asString.keep(4) ++ "k" )
				},{
					freqLabel[i].string_( (kfreq*1000).asInteger.asString)
				});
			});
		};
		
		setDBLabelVals = { arg db;
			dbLabel.size.do({ arg i;
				dbLabel[i].string = (i * db/(dbLabel.size-1)).asInteger.neg.asString;
			});
		};

		window = SCWindow("Freq Analyzer", rect.resizeBy(pad[0] + pad[1] + 4, pad[2] + pad[3] + 4), false);
		
		freqLabel.size.do({ arg i;
			freqLabel[i] = SCStaticText(window, Rect(pad[0] - (freqLabelDist*0.5) + (i*freqLabelDist), pad[2] - 10, freqLabelDist, 10))
				.font_(font)
				.align_(0)
			;
			SCStaticText(window, Rect(pad[0] + (i*freqLabelDist), pad[2], 1, rect.height))
				.string_("")
				.background_(scopeColor.alpha_(0.25))
			;
		});
		
		dbLabel.size.do({ arg i;
			dbLabel[i] = SCStaticText(window, Rect(0, pad[2] + (i*dbLabelDist), pad[0], 10))
				.font_(font)
				.align_(1)
			;
			SCStaticText(window, Rect(pad[0], dbLabel[i].bounds.top, rect.width, 1))
				.string_("")
				.background_(scopeColor.alpha_(0.25))
			;		
		});
		
		scope = SCFreqScope(window, rect.moveBy(pad[0], pad[2]));
		
		setFreqLabelVals.value(scope.freqMode, 2048);
		setDBLabelVals.value(scope.dbRange);

		SCButton(window, Rect(pad[0] + rect.width, pad[2], pad[1], 16))
			.states_([["Power", Color.white, Color.green(0.5)], ["Power", Color.white, Color.red(0.5)]])
			.action_({ arg view;
				if(view.value == 0, {
					scope.active_(true);
				},{
					scope.active_(false);
				});
			})
			.font_(font)
			.canFocus_(false)
		;
		
		SCStaticText(window, Rect(pad[0] + rect.width, pad[2]+20, pad[1], 10))
			.string_("BusIn")
			.font_(font)
		;

		SCNumberBox(window, Rect(pad[0] + rect.width, pad[2]+30, pad[1], 14))
			.action_({ arg view;
				view.value_(view.value.asInteger.clip(0, Server.internal.options.numAudioBusChannels));
				scope.inBus_(view.value);
			})
			.value_(busNum)
			.font_(font)
		;

		SCStaticText(window, Rect(pad[0] + rect.width, pad[2]+48, pad[1], 10))
			.string_("FrqScl")
			.font_(font)
		;
		SCPopUpMenu(window, Rect(pad[0] + rect.width, pad[2]+58, pad[1], 16))
			.items_(["lin", "log"])
			.action_({ arg view;
				scope.freqMode_(view.value);
				setFreqLabelVals.value(scope.freqMode, 2048);
			})
			.canFocus_(false)
			.font_(font)
		;
		
		SCStaticText(window, Rect(pad[0] + rect.width, pad[2]+76, pad[1], 10))
			.string_("dbCut")
			.font_(font)
		;
		SCPopUpMenu(window, Rect(pad[0] + rect.width, pad[2]+86, pad[1], 16))
			.items_(Array.series(12, 12, 12).collect({ arg item; item.asString }))
			.action_({ arg view;
				scope.dbRange_((view.value + 1) * 12);
				setDBLabelVals.value(scope.dbRange);
			})
			.canFocus_(false)
			.font_(font)
			.value_(7)
		; 

		scope
			.background_(bgColor)
			.style_(1)
			.waveColors_([scopeColor.alpha_(1)])
			.inBus_(busNum)
			.active_(true)
			.canFocus_(false)
		;
		
		window.onClose_({ scope.kill }).front;
		^this.newCopyArgs(scope, window)
	}

}
*/

