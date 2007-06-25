XiiPredators {
	var <>gui;
	var selbPool, ldSndsGBufferList, sndNameList, bufferPop, gBufferPoolNum;
	var preyArray, a, poolname;
	
	*new {arg server;
		^super.new.initXiiPredators(server);
	}

	initXiiPredators {arg server;

		var w, ww, wview, outBus, soundFuncPop, bufferList;
		var predatorArray;
		var sampleNameField, pitchSampleField, keybButt;
		var playButt, cmdPeriodFunc;
		var createCodeWin, createAudioStreamBusWin, createEnvWin, synthDefPrototype, synthDefInUse;
		var inbus, createEnvButt, envButt;
		
		gBufferPoolNum = 0;
		preyArray = [];
		predatorArray = [];
		sndNameList = List.new;
		bufferList = List.new; // contains bufnums of buffers (not buffers)
		
		synthDefInUse = nil;
		synthDefPrototype = 
		{SynthDef(\xiiCode, {arg outbus=0, freq=440, pan=0, amp=1;
			var env, sine;
			env = EnvGen.ar(Env.perc, doneAction:2);
			sine = SinOsc.ar(freq, 0, env*amp);
			Out.ar(outbus, Pan2.ar(sine, pan));
		}).play(Server.default)}.asCompileString;

		w = SCWindow("ixi predators", Rect(408, 364, 640, 580), resizable:false);
		wview = w.view;
		
		a = XixiPainter.new(w, Rect(10, 5, 620, 470)); // 640 * 480 resolution
		
		3.do({
			preyArray = preyArray.add(
				XixiPrey.new(Point.new(100+(400.rand), 100+(200.rand)), Rect(10, 5, 620, 470)));
		});
		4.do({
			predatorArray = predatorArray.add(
				XixiPredator.new(Point.new(-10, -10), Rect(10, 5, 620, 470),preyArray));
		});
		
		predatorArray.do({|predator| predator.supplyPredatorArray(predatorArray)});
		preyArray.do({|prey| prey.supplyPredatorArray(predatorArray)});
		preyArray.do({|prey| prey.supplyPreyArray(preyArray)});
		preyArray.choose.selected = true;
		
		a.addToDrawList(preyArray);
		a.addToDrawList(predatorArray);
		a.frameRate = 0.05;

		ldSndsGBufferList = {arg argPoolName;
			poolname = argPoolName.asSymbol;
			if(try {~globalBufferDict.at(poolname)[0] } != nil, {
				sndNameList = [];
				bufferList = List.new;
				~globalBufferDict.at(poolname)[0].do({arg buffer;
					sndNameList = sndNameList.add(buffer.path.basename);
					bufferList.add(buffer.bufnum);
				 });
				 bufferPop.items_(sndNameList);
				 preyArray.do({|prey, i| prey.setRandomBuffer(selbPool.items[selbPool.value])});
			}, {
				"got no files".postln;
				sndNameList = [];
			});
		};

		// add predator
		SCButton(w, Rect(10, 485, 65, 18))
			.font_(Font("Helvetica", 9))
			.states_([["add predator",Color.black,Color.clear]])
			.action_({ var p;
				p = XixiPredator.new(Point.new(1, 1), Rect(10,10, 620, 470), preyArray);
				predatorArray = predatorArray.add(p);
				a.replaceDrawList(preyArray);
				a.addToDrawList(predatorArray);
				predatorArray.do({|predator| predator.supplyPredatorArray(predatorArray)});
				preyArray.do({|prey| prey.supplyPredatorArray(predatorArray)});
			});
			
		// delete predator
		SCButton(w, Rect(10, 506, 65, 18))
			.font_(Font("Helvetica", 9))
			.states_([["del predator",Color.black,Color.clear]])
			.action_({
				if(predatorArray.size > 1, {predatorArray.removeAt(0)});
				a.replaceDrawList(preyArray);
				a.addToDrawList(predatorArray);
				predatorArray.do({|predator| predator.supplyPredatorArray(predatorArray)});
				preyArray.do({|prey| prey.supplyPredatorArray(predatorArray)});
			});
		
		// add prey
		SCButton(w, Rect(78, 485, 50, 18))
			.font_(Font("Helvetica", 9))
			.states_([["add prey",Color.black,Color.clear]])
			.action_({ var p;
				p = XixiPrey.new(Point.new(150+(470.rand), 100+(280.rand)), Rect(10,10, 620, 470), soundFuncPop.value);
				p.supplyTextFields([sampleNameField, pitchSampleField]);
				p.setRandomBuffer(gBufferPoolNum); // new prey gets a random buffer
				preyArray = preyArray.add(p);
				a.replaceDrawList(preyArray);
				a.addToDrawList(predatorArray);
				preyArray.do({|prey| 
					prey.supplyPredatorArray(predatorArray);
					prey.supplyPreyArray(preyArray);
				});
				predatorArray.do({|predator| predator.supplyPreyArray(preyArray)});
			});
		
		// delete prey
		SCButton(w, Rect(78, 506, 50, 18))
			.font_(Font("Helvetica", 9))
			.states_([["del prey",Color.black,Color.clear]])
			.action_({
				if(preyArray.size > 1, {preyArray.removeAt(0)});
				a.replaceDrawList(preyArray);
				a.addToDrawList(predatorArray);
				preyArray.do({|prey| prey.supplyPreyArray(preyArray)});
				preyArray.do({|prey| prey.supplyPredatorArray(predatorArray)});
				predatorArray.do({|predator| predator.supplyPreyArray(preyArray)});
			});
				
		pitchSampleField = SCStaticText(w, Rect(275, 535, 60, 20))
				.font_(Font("Helvetica", 9))
				.string_("prey sample :");
				
		sampleNameField =	SCStaticText(w, Rect(340, 535, 100, 20))
				.font_(Font("Helvetica", 9))
				.string_("none");
		
		SCStaticText(w, Rect(265, 530, 205, 30))
				.background_(Color.new255(255, 100, 0, 30))
				.string_("");

		preyArray.do({|prey| prey.supplyTextFields([sampleNameField, pitchSampleField])});
		
		selbPool = SCPopUpMenu(w, Rect(265, 485, 102, 16)) // 530
				.font_(Font("Helvetica", 9))
				.items_( if(~globalBufferDict.keys.asArray == [], {["no pool"]}, {~globalBufferDict.keys.asArray.sort}) )
				.value_(0)
				.background_(Color.white)
				.action_({ arg item;
					try{
						ldSndsGBufferList.value(selbPool.items[item.value]);
						bufferPop.items_(sndNameList);
						preyArray.do({|prey, i| prey.setRandomBuffer(selbPool.items[item.value])});
					}
				});		
		
		bufferPop = SCPopUpMenu(w,Rect(265, 505, 102, 16)) // 550
				.font_(Font("Helvetica", 9))
				.items_(["no buffer 1", "no buffer 2"])
				.background_(Color.new255(255, 255, 255))
				.action_({ arg popup;
					preyArray.do({|prey| prey.setMyBuffer(gBufferPoolNum, popup.value)});
				})
				.addAction({bufferPop.action.value( bufferPop.value )}, \mouseDownAction);
		
		
		SCStaticText(w, Rect(375, 483, 80, 20))
			.font_(Font("Helvetica", 9))
			.string_("sound :");
		
		soundFuncPop = SCPopUpMenu(w, Rect(410, 485, 60, 16))
				.font_(Font("Helvetica", 9))
				.items_(["sample", "sine", "bells", "sines", "synth1", "ks_string", 
				"ixi_string", "impulse", "ringz", "klanks", "scode", "audiostream"])
				.background_(Color.new255(255, 255, 255))
				.action_({ arg popup;
					createEnvButt.value(false);
					if(soundFuncPop.items[popup.value] == "scode", {
						createCodeWin.value;
					}); 
					if(soundFuncPop.items[popup.value] == "audiostream", {
						createAudioStreamBusWin.value;
						createEnvButt.value(true);
					}); 
					if(soundFuncPop.items[popup.value] == "sample", {
						createEnvButt.value(true);
					}); 
					preyArray.do({|prey| prey.setAteFunc_(popup.value)});
				});
		
		SCStaticText(w, Rect(375, 505, 80, 20))
			.font_(Font("Helvetica", 9))
			.string_("outbus :");
		
		outBus = SCPopUpMenu(w, Rect(410, 507, 60,16))
				.font_(Font("Helvetica", 9))
				.items_(XiiACDropDownChannels.getStereoChnList)
				.background_(Color.new255(255, 255, 255))
				.action_({ arg popup; var outbus;
					preyArray.do({|prey| prey.setOutBus_(popup.value * 2)});
				});
		
		// fixed dynamic pitch
		SCButton(w, Rect(510, 485, 70, 18))
			.font_(Font("Helvetica", 9))
			.states_([["fixed pitch",Color.black,Color.clear], ["locative pitch",Color.black,Color.clear]])
			.action_({arg butt; 
				preyArray.do({|prey| prey.setPitchMode_(butt.value)});
				if(butt.value == 1, {
					keybButt = SCButton(w, Rect(477, 485, 26, 18))
						.font_(Font("Helvetica", 9))
						.canFocus_(false)
						.states_([["key",Color.black,Color.clear]])
						.action_({var func, k;
							func = {arg note; 	
								preyArray.do({|prey| prey.setPitch_(note)})
							};
							ww = SCWindow("set pitch", 
							Rect(w.bounds.left+400, w.bounds.top+230, 400, 80), resizable:false).front;
							ww.alwaysOnTop = true;
							k = MIDIKeyboard.new(ww, Rect(10, 5, 374, 60), 5, 24);
							k.keyDownAction_({arg note; func.value(note)});
							k.keyTrackAction_({arg note; func.value(note)});
						});
					w.refresh;
				}, {
					if(ww.isKindOf(SCWindow), {ww.close;});
					keybButt.remove;
					w.refresh;
				});
			});
			
		// start stop
		playButt = SCButton(w, Rect(585, 485, 45, 18))
			.font_(Font("Helvetica", 9))
			.states_([["start",Color.black,Color.clear], ["stop",Color.black, Color.green(alpha:0.2)]])
			.action_({arg butt;
				if(butt.value == 1, {
					a.start;
				}, {
					a.stop;
				});
			});
		
		OSCIISlider.new(w, Rect(510, 513, 117, 10), "- vol", 0, 1, 0.4, 0.0001, \amp)
			.font_(Font("Helvetica", 9))
			.action_({arg sl; 
				preyArray.do({|prey, i| prey.setVolume_(sl.value)});	});
		
		OSCIISlider.new(w, Rect(137, 485, 117, 10), "- aggression", 0.1, 6, 4, 0.01)
			.font_(Font("Helvetica", 9))
			.action_({arg sl; 
				predatorArray.do({|predator, i| predator.setAggression_(sl.value)});	});
		
		OSCIISlider.new(w, Rect(137, 515, 117, 10), "- friction", 5, 25, 18, 0.01)
			.font_(Font("Helvetica", 9))
			.action_({arg sl; 
				predatorArray.do({|predator, i| predator.friction = sl.value});	});
		
		OSCIISlider.new(w, Rect(137, 545, 117, 10), "- restless", 1, 50, 20, 1)
			.font_(Font("Helvetica", 9))
			.action_({arg sl; 
				predatorArray.do({|predator, i| predator.restlessSeed = sl.value});	});
		
		// -- stuff to do when GUIs are created
		ldSndsGBufferList.value(selbPool.items[0].asSymbol);
		try{preyArray.do({|prey, i| prey.setMyBuffer(gBufferPoolNum, i, true)})}; // loading = true
		
		createEnvButt = {arg state;
			if(state == true, {
				envButt = SCButton(w, Rect(477, 506, 26, 18))
					.font_(Font("Helvetica", 9))
					.canFocus_(false)
					.states_([["env",Color.black,Color.clear]])
					.action_({var func, k;
						createEnvWin.value;
					});
			}, {
				envButt.remove;
				w.refresh;
			})
		};
		
		createCodeWin = {
				var funcwin, func, subm, test, view;

				funcwin = SCWindow("scode", Rect(600,700, 440, 200)).front;
				funcwin.alwaysOnTop = true;
				
				view = funcwin.view;
				func = SCTextView(view, Rect(20, 10, 400, 140))
						.font_(Font("Monaco", 9))
						.resize_(5)
						.focus(true)
						.string_(
							if(synthDefInUse.isNil, { 
								synthDefPrototype
							},{
								synthDefInUse
							});
						);
				test = SCButton(view, Rect(280,160,50,18))
						.states_([["test",Color.black,Color.clear]])
						.resize_(9)
						.font_(Font("Helvetica", 9))
						.action_({
							func.string.interpret.value;
						});
						
				subm = SCButton(view, Rect(340,160,50,18))
						.states_([["submit",Color.black,Color.clear]])
						.resize_(9)
						.font_(Font("Helvetica", 9))
						.action_({
							func.string.interpret;
							synthDefInUse = func.string;
							funcwin.close;
						});

		};


		createEnvWin = {arg index;
			var win, envview, timesl, setButt, timeScale;
			var selectedprey;
			preyArray.do({|prey, i| if(prey.selected == true, {selectedprey = i})}); 
			timeScale = 1.0;
			
			win = SCWindow("asdr envelope", Rect(200, 450, 250, 130), resizable:false).front;
			win.alwaysOnTop = true;
			
			envview = SCEnvelopeView(win, Rect(10, 5, 230, 80))
				.drawLines_(true)
				.selectionColor_(Color.red)
				.canFocus_(false)
				.drawRects_(true)
				.background_(XiiColors.lightgreen)
				.fillColor_(XiiColors.darkgreen)
				.action_({arg b; })
				.thumbSize_(5)
				.env2viewFormat_(Env.new(preyArray[selectedprey].getEnv[0], preyArray[selectedprey].getEnv[1]))
				.setEditable(0, false);


			timesl = OSCIISlider.new(win, 
						Rect(10, 100, 130, 8), "- duration", 0.1, 10, preyArray[selectedprey].getEnv[2], 0.01)
					.font_(Font("Helvetica", 9))
					.action_({arg sl; });
			
			setButt = SCButton.new(win, Rect(160, 100, 60, 16))
					.states_([["set envelope", Color.black, Color.clear]])
					.focus(true)
					.font_(Font("Helvetica", 9))
					.action_({
						var env;
						env = envview.view2envFormat ++ timesl.value; // levels, times, duration
						preyArray[selectedprey].setEnvelope_(env);

						win.close;
					});
		};

		createAudioStreamBusWin = {arg index;
			var win, envview, timesl, setButt;
			win = SCWindow("audiostream inbus", Rect(200, 450, 250, 100), resizable:false).front;
			win.alwaysOnTop = true;
				
			SCStaticText(win, Rect(20, 55, 20, 16))
				.font_(Font("Helvetica", 9)).string_("in"); 

			SCPopUpMenu(win, Rect(35, 55, 50, 16))
				.items_(XiiACDropDownChannels.getStereoChnList)
				.value_(10)
				.font_(Font("Helvetica", 9))
				.background_(Color.white)
				.canFocus_(false)
				.action_({ arg ch; var inbus;
					inbus = ch.value * 2;
					preyArray.do({|prey| prey.setInBus_(inbus)});
				});

			setButt = SCButton.new(win, Rect(120, 55, 60, 16))
					.states_([["set inbus", Color.black, Color.clear]])
					.focus(true)
					.font_(Font("Helvetica", 9))
					.action_({
						win.close;
					});
		};

		createEnvButt.value(true);  // the default is sample, so we get the env button

		cmdPeriodFunc = { playButt.valueAction_(0);};
		CmdPeriod.add(cmdPeriodFunc);

		w.onClose_({ 
			var t;
			a.stop;
			a.remove;
			CmdPeriod.remove(cmdPeriodFunc);
			~globalWidgetList.do({arg widget, i; if(widget == this, {t = i})});
			~globalWidgetList.removeAt(t);
		});
 	}
	
	updatePoolMenu {
		selbPool.items_( ~globalBufferDict.keys.asArray );
		ldSndsGBufferList.value(selbPool.items[0].asSymbol);
	}
}