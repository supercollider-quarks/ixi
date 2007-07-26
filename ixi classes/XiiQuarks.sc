// IMPORTANT IN THIS CLASS:
// ~globalWidgetList
// ~globalBufferDict - a.keys (.asArray)
// the presets are handled in a class called XiiSettings

// NEW IN VERSION 2:
// Amp slider in Recorder and in BufferPool
// Open sounds folder in Player
// two new instruments: LiveBuffers and Mushrooms
// Fixing loading of bufferpools (instruments would automatically load new sound)

// NEW IN VERSION 3:
// store settings
// bug fixes


XiiQuarks {	

	*new { 
		^super.new.initXiiQuarks;
	}
		
	initXiiQuarks {
	
		var win, txtv, quarks, serv, channels;
		var openButt, effectCodeString, monoButt, stereoButt, effect;
		var name, point;
		var midi, midiControllerNumbers, midiRotateWindowChannel, midiInPorts, midiOutPorts;
		var guistyle, openSndFolder;
		var chosenWidget, effectnum, types, typesview, ixilogo;
		var settingRegister, settingNameView, storeSettingButt, comingFromFieldFlag, settingName;
		var storedSettingsPop, loadSettingButt, deleteSettingButt, clearScreenButt;
		
		settingRegister = XiiSettings.new; // activate the settings registry

		////////////// preferences ///////////////////
		Server.default = Server.local; // EXPERIMENTAL !!!!
		//Server.default.boot;
		
		XiiACDropDownChannels.numChannels = 52; // NUMBER OF AUDIO BUSSES USED
		guistyle = "new";
		
		midi = false; // if you want to use midi or not (true or false)
		midiControllerNumbers = [73, 72, 91, 93, 74, 71, 5, 84, 7]; // evolution mk-449c
		//midiControllerNumbers = [97,98,99,100,101,102,103,104]; // behringer bcr2000
		midiRotateWindowChannel = 10;
		//midiRotateWindowChannel = 89; // behringer
		midiInPorts = 2;
		midiOutPorts = 2;
		//////////////////////////////////////////////
		
		XiiLoadSynthDefs.new(Server.default);
		
	
		name = "quarks";
		point = XiiWindowLocation.new(name);
		
		win = SCWindow(name, Rect(point.x, point.y, 275, 212), resizable:false).front;
		
		~globalWidgetList = List.new; // keep track of active widgets
		// (contains [List [buffers], [selstart, sellength]])
		~globalBufferDict = ();  // ICMC
		//~globalBufferList.add(0); // ICMC
		~bufferPoolNum = -1;
		comingFromFieldFlag = false;
		settingName = "preset_0";
		
		quarks = [ 
			["AudioIn", "Recorder", "Player", "BufferPool", "PoolManager", 
			"FreqScope", "WaveScope", "EQMeter", "MixerNode", 
			"ChannelSplitter", "Amplifier", "TrigRecorder"],
	
			["SoundScratcher", "StratoSampler", "Mushrooms", "Predators", 
			"Gridder", "PolyMachine", "GrainBox", "BufferPlayer", "ScaleSynth"], 
			
			["Delay", "Freeverb", "AdCVerb", "Distortion", "ixiReverb", "Chorus",
			"Octave", "Tremolo", "Equalizer", "CombVocoder", "RandomPanner", 
			"MRRoque", "MultiDelay"],
			
			["Bandpass", "Lowpass", "Highpass", "RLowpass", "RHighpass", 
			"Resonant", "Klanks"],
			
			["Noise", "Oscillators"]
		];
		
		types = ["utilities", "instruments", "effects", "filters", "other"];
		
		ixilogo = [ // the ixi logo
			Point(1,7), Point(8, 1), Point(15,1), Point(15,33),Point(24, 23), Point(15,14), 
			Point(15,1), Point(23,1),Point(34,13), Point(45,1), Point(61,1), Point(66,6), 
			Point(66,37), Point(59,43), Point(53,43), Point(53,12), Point(44,22), Point(53,33), 
			Point(53,43), Point(42,43), Point(34,32),Point(24,43), Point(7,43), Point(1,36), Point(1,8)
			];


		channels = 2;
		effect = "AudioIn";

		typesview = SCListView(win,Rect(10,10, 120, 96))
			.items_(types)
			.hiliteColor_(XiiColors.darkgreen) //Color.new255(155, 205, 155)
			.background_(XiiColors.listbackground)
			.selectedStringColor_(Color.black)
			.action_({ arg sbs;
				txtv.items_(quarks[sbs.value]);
				txtv.value_(0);
				effect = quarks[sbs.value][txtv.value];
			})
			.enterKeyAction_({|view|
				txtv.value_(0);
				txtv.focus(true);
			});
			
			
		storedSettingsPop = SCPopUpMenu(win, Rect(10, 116, 78, 16)) // 550
			.font_(Font("Helvetica", 9))
			.canFocus_(false)
			.items_(settingRegister.getSettingsList)
			.background_(Color.white);

		loadSettingButt = SCButton(win, Rect(95, 116, 35, 17))
			.states_([["load", Color.black, Color.clear]])
			.font_(Font("Helvetica", 9))
			.canFocus_(false)
			.action_({arg butt; 
				settingRegister.loadSetting(storedSettingsPop.items[storedSettingsPop.value]);
			});

		settingNameView = SCTextView.new(win, Rect(10, 139, 78, 14))
			.font_(Font("Helvetica", 9))
			.string_(settingName = PathName(settingName).nextName)
			.keyDownAction_({arg view, key, mod, unicode; 
				if(unicode ==13, {
					comingFromFieldFlag = true;
					storeSettingButt.focus(true);
				});
			});

		
		storeSettingButt = SCButton(win, Rect(95, 138, 35, 17))
			.states_([["store", Color.black, Color.clear]])
			.font_(Font("Helvetica", 9))
			.canFocus_(false)
			.action_({arg butt; 
				settingRegister.storeSetting(settingNameView.string);
				storedSettingsPop.items_(settingRegister.getSettingsList);
				settingNameView.string_(settingName = PathName(settingName).nextName);
			})
			.keyDownAction_({arg view, key, mod, unicode; // if RETURN on bufNameView
				if(unicode == 13, {
					if(comingFromFieldFlag, {
						"not storing setting".postln;
						comingFromFieldFlag = false;
					},{
						settingRegister.storeSetting(settingNameView.string);
						storedSettingsPop.items_(settingRegister.getSettingsList);
					})
				});
				settingNameView.string_(settingName = PathName(settingName).nextName);
			});

		
		deleteSettingButt = SCButton(win, Rect(95, 160, 35, 17))
			.states_([["delete", Color.black, Color.clear]])
			.font_(Font("Helvetica", 9))
			.canFocus_(false)
			.action_({arg butt; 
				settingRegister.removeSetting(storedSettingsPop.items[storedSettingsPop.value]);
				storedSettingsPop.items_(settingRegister.getSettingsList);
			});

		
		clearScreenButt = SCButton(win, Rect(95, 182, 35, 17))
			.states_([["clear", Color.black, Color.clear]])
			.font_(Font("Helvetica", 9))
			.canFocus_(false)
			.action_({arg butt; 
				settingRegister.clearixiQuarks;
			});

		txtv = SCListView(win,Rect(140,10, 120, 152))
			.items_(quarks[0])
			.hiliteColor_(XiiColors.darkgreen) //Color.new255(155, 205, 155)
			.background_(XiiColors.listbackground)
			.selectedStringColor_(Color.black)
			.action_({ arg sbs;
				("Xii"++quarks[typesview.value][sbs.value]).postln;
				effect = quarks[typesview.value][sbs.value];
			})
			.enterKeyAction_({|view|
				effect = quarks[typesview.value][view.value];
				effectCodeString = "Xii"++effect++".new(Server.default,"++channels++")";
				~globalWidgetList.add(effectCodeString.interpret);
			})
			.keyDownAction_({arg view, char, modifiers, unicode;
				if(unicode == 13, {
					effect = quarks[typesview.value][view.value];
					effectCodeString = "Xii"++effect++".new(Server.default,"++channels++")";
					~globalWidgetList.add(effectCodeString.interpret);
				});
				if (unicode == 16rF700, { txtv.valueAction = txtv.value - 1;  });
				if (unicode == 16rF703, { txtv.valueAction = txtv.value + 1;  });
				if (unicode == 16rF701, { txtv.valueAction = txtv.value + 1;  });
				if (unicode == 16rF702, { typesview.focus(true);  });
			});

		stereoButt = OSCIIRadioButton(win, Rect(140, 172, 12, 12), "stereo")
					.value_(1)
					.font_(Font("Helvetica", 9))
					.action_({ arg butt;
							if(butt.value == 1, {
							channels = 2;
							monoButt.value_(0);
							});
					});

		monoButt = OSCIIRadioButton(win, Rect(140, 190, 12, 12), "mono")
					.value_(0)
					.font_(Font("Helvetica", 9))
					.action_({ arg butt;
							if(butt.value == 1, {
								channels = 1;
								stereoButt.value_(0);
							});	
					});

		openSndFolder = SCButton(win, Rect(195, 178, 13, 18))
				.states_([["f",Color.black,Color.clear]])
				.font_(Font("Helvetica", 9))
				.canFocus_(false)
				.action_({ arg butt;
					//settingRegister.storeSetting;
					"open sounds/ixiquarks/".unixCmd
				});
								
		openButt = SCButton(win, Rect(210, 178, 50, 18))
				.states_([["Open",Color.black,Color.clear]])
				.font_(Font("Helvetica", 9))
				.canFocus_(false)
				.action_({ arg butt;
					effectCodeString = "Xii"++effect++".new(Server.default,"++channels++")";
					~globalWidgetList.add(effectCodeString.interpret);
				});
				
		// MIDI control of sliders		
		if(midi == true, {
			MIDIIn.control = { arg src, chan, num, val; 
				var wcnt;					
				if(num == midiRotateWindowChannel, {
					{
					wcnt = SCWindow.allWindows.size;
					if(~globalWidgetList.size > 0, {
						chosenWidget = val % wcnt;
						SCWindow.allWindows.at(chosenWidget).front;
						~globalWidgetList.do({arg widget, i;
							if(widget.gui.isKindOf(XiiEffectGUI), {
								if(SCWindow.allWindows.at(chosenWidget) === widget.gui.win, {
									effectnum = i;
								});
							});
						});
					});
					}.defer;
				},{
				{
				~globalWidgetList[effectnum].gui.setSlider_(
					midiControllerNumbers.detectIndex({arg i; i == num}), val/127);
				}.defer;
				});
			};
			
			MIDIClient.init(midiInPorts,midiOutPorts);
			midiInPorts.do({ arg i; 
				MIDIIn.connect(i, MIDIClient.sources.at(i));
			});
		});
		
		win.onClose_({ 
			point = Point(win.bounds.left, win.bounds.top);
			XiiWindowLocation.storeLoc(name, point);
		}); 
	
		txtv.focus(true);
		
		win.drawHook = {
			XiiColors.ixiorange.set;
			Pen.width = 3;
			Pen.translate(30,170);
			Pen.scale(0.6,0.6);
			Pen.moveTo(1@7);
			ixilogo.do({arg point;
				Pen.lineTo(point+0.5);
			});
			Pen.stroke
		};
		win.refresh;
	
	}
}
