


// NOTE on Trigger IDs: 
// BufferPlayer uses TriggerIDs from 50 to (number of instances * 50)
// AudioIn uses TriggerID number 800
// Recorder uses TriggerID nr 820
// Mushrooms uses TriggerID nr 840

// NEW IN VERSION 2:
// Amp slider in Recorder and in BufferPool
// Open sounds folder in Player
// two new instruments: LiveBuffers and Mushrooms
// Fixing loading of bufferpools (instruments would automatically load new sound)

// NEW IN VERSION 3:
// store settings
// bug fixes

// NEW IN VERSION 4:
// Relative tempi in PolyMachine and an increase up to 48 steps per track
// User definable number of tracks in PolyMachine
// User definable number of tracks in BufferPlayer
// PolyMachine: Fixing sc-code such that one does not have to submit code for each box
// BufferPool soundfile view now displays selections in the soundfile
// Fixing Gridder (the params argument so the transpose is set to 1 again)
// Fixing loadup of synthdefs in PolyMachine (removing from server)
// Optimising the distribution code
// Record fixed
// Fixing the route ordering of channels - now no need to restart effects
// Fixing amplifier
// Settings store bufferpools and their contents
// Effects remember their on/off state
// Refining small functions in SoundScratcher
// Fixing settings in the Quarks interface
// BufferPool and Recorder now get a new logical filename in text field when recording stops
// some new spectral effects
// new time domain effect called cyberpunk (thanks dan stowell for ugen)
// Added views that display frames, selection start and selection end in BufferPool SndFileView
// optimization of code
// got rid of all environmental variables and store envir vars in the XQ class
// soundfilefolder created on default if it doesn't exist
// new filter: Moog VCF

// NEW IN VERSION 5:
// new instrument: Sounddrops
// new tool: Theory (scales and chords)
// new instrument : Quanoon
// styles options in WaveScope
// keyboard grains mode in SoundScratcher instrument (both with drawn grains and without)
// Added a Function:record method. Now you can do {SinOsc.ar(222)}.record(3) // 3 sec file
// Added outbus in the SoundFilePlayer widget of BufferPool.
// Ported to SwingOSC
// new spectral effect: Speactral Delay
// Added better accessibility to bufferPools through the XQ class (good for live coding)
// fixing noise bug in synthdefs, (inserted a LPF instead of the RLPF)

// NEW IN VERSION 6:
// Adding a rec/play toggle button in the StratoSampler
// change in colours in Quanoon and adding keyboard for fundamental key
// fixing updates from bufferpools in instruments
// adding Z (undo) for grains in SoundScratcher
// adding pitchratio in Sounddrops (using the microtonal keyboard)
// Automation (path recording) of sliders. (Press A for automation, C for clearing)
// PolyMachine remebers states when stored in Settings
// "free" button in BufferPools GUI frees only the selected buffer not all buffers
// Adding Limiter to Recorder and BufferPool recording (thus no distortion possible)
// Preventing accidental stop (apple+dot) with a Warning window

/*

// HOW TO WORK WITH BUFFERPOOL IN YOUR OWN WORK....


// first load some sounds into a bufferpool

// then lets look at the global buffer dictionary of ixiQuarks:
XQ.globalBufferDict

// or if you have lots of pools:
Post << XQ.globalBufferDict 

// let's look at what pools we have open:
XQ.poolNames

// get the buffers of a pool
a = XQ.buffers('bufferPool 1')
// get the selections of a pool
a = XQ.selections('bufferPool 1')
// get the buffers and the selections of a pool
a = XQ.bufferList('bufferPool 1')

// now you can play the first buffer in your buffer pool.
a[0].play

// or (if your buffer is a mono sound)
(
x = SynthDef("help-Buffer",{ arg out = 0, bufnum;
	Out.ar( out,
		PlayBuf.ar(1, bufnum, BufRateScale.kr(bufnum))
	)
}).play(s,[ \bufnum, a[0].bufnum ]);
)


 there are a couple of things you have to know to get sound out of SC. A)
Setting up an aggregate device is not enough to get your audio routed
correctly.  SC looks at the audio interface currently in use by core audio
and uses that device.  IOW - whatever core audio is using (selected in audio
midi prefs) is what SC will use.  B) if you have a firewire or USB audio interface,
you do not need to set up an aggregate device.  Simply select your firewire
device as your input and output device (in Audio Midi prefs) and SC will use
it as well.  You only need to set up an aggregate device if you plan to use
the built-in mic or input and built-in output.  This is because SC will only
accept one device for input and output and the new core audio divides them
into separate devices.  

// check for versions (Pipe locks the machine if it's not online)
(
var version, pipe;
// check if user is online: (will return a 4 digit number if not)
a = "curl http://www.ixi-audio.net/content/download/ixiquarks/version.txt".systemCmd;
// then get the version number (from a textfile with only one number in it)
if(a==0, {
	pipe = Pipe.new("curl http://www.ixi-audio.net/content/download/ixiquarks/version.txt", "r");
	version = pipe.getLine; 
	pipe.close;	
});
"current version is ".post; version.postln;
)


*/



XiiQuarks {	

	*new { 
		^super.new.initXiiQuarks;
	}
		
	initXiiQuarks {
	
		var win, txtv, quarks, serv, channels;
		var openButt, widgetCodeString, monoButt, stereoButt, widget;
		var name, point;
		var midi, midiControllerNumbers, midiRotateWindowChannel, midiInPorts, midiOutPorts;
		var openSndFolder;
		var chosenWidget, widgetnum, types, typesview, ixilogo;
		var settingRegister, settingNameView, storeSettingButt, comingFromFieldFlag, settingName;
		var storedSettingsPop, loadSettingButt, deleteSettingButt, clearScreenButt;
		var prefFile, preferences;
		
		settingRegister = XiiSettings.new; // activate the settings registry
		
		XQ.new; // A class containing all the settings and environment maintenance
		
		XQ.preferences; // retrieve preferences from the "preferences.ixi" file
		Server.default.options.device = XQ.pref.device; // the audio device (soundcard)
		midi = XQ.pref.midi; // if you want to use midi or not (true or false)
		midiControllerNumbers = XQ.pref.midiControllerNumbers; // evolution mk-449c
		midiRotateWindowChannel = XQ.pref.midiRotateWindowChannel;
		midiInPorts = XQ.pref.midiInPorts;
		midiOutPorts = XQ.pref.midiOutPorts;
		if(XQ.pref.emailSent == false, {
			"open preferences/email.html".unixCmd;
		});

		XiiACDropDownChannels.numChannels_( XQ.pref.numberOfChannels ); // NUMBER OF AUDIO BUSSES

		//////////////////////////////////////////////

		XiiSynthDefs.new(Server.default);
	
		name = " ixi quarks";
		point = XiiWindowLocation.new(name);
		
		win = GUI.window.new(name, Rect(point.x, point.y, 275, 224), resizable:false);
		
		comingFromFieldFlag = false;
		settingName = "preset_0";
		
		quarks = [ 
			["AudioIn", "Recorder", "Player", "BufferPool", "PoolManager", 
			"FreqScope", "WaveScope", "EQMeter", "MixerNode", 
			"ChannelSplitter", "Amplifier", "TrigRecorder", "MusicTheory"],
	
			["SoundScratcher", "StratoSampler", "Sounddrops", "Mushrooms", "Predators", 
			"Gridder", "PolyMachine", "GrainBox", "Quanoon", "BufferPlayer", "ScaleSynth"], 
			
			["Delay", "Freeverb", "AdCVerb", "Distortion", "ixiReverb", "Chorus",
			"Octave", "CyberPunk", "Tremolo", "Equalizer", "CombVocoder", "RandomPanner", 
			"MRRoque", "MultiDelay"],
			
			["Bandpass", "Lowpass", "Highpass", "RLowpass", "RHighpass", 
			"Resonant", "Klanks", "MoogVCF", "MoogVCFFF"],
			
			["SpectralEQ", "MagClip", "MagSmear", "MagShift", "MagFreeze", 
			"RectComb", "BinScramble", "BinShift", "SpectralDelay"],
			
			["Noise", "Oscillators"]
			
		];
		
		types = ["utilities", "instruments", "effects", "filters", "spectral", "other"];
		
		ixilogo = [ // the ixi logo
			Point(1,7), Point(8, 1), Point(15,1), Point(15,33),Point(24, 23), Point(15,14), 
			Point(15,1), Point(23,1),Point(34,13), Point(45,1), Point(61,1), Point(66,6), 
			Point(66,37), Point(59,43), Point(53,43), Point(53,12), Point(44,22), Point(53,33), 
			Point(53,43), Point(42,43), Point(34,32),Point(24,43), Point(7,43), Point(1,36), Point(1,8)
			];

		channels = 2;
		widget = "AudioIn";

		typesview = GUI.listView.new(win,Rect(10,10, 120, 108))
			.items_(types)
			.hiliteColor_(XiiColors.darkgreen) //Color.new255(155, 205, 155)
			.background_(XiiColors.listbackground)
			.selectedStringColor_(Color.black)
			.action_({ arg sbs;
				txtv.items_(quarks[sbs.value]);
				txtv.value_(0);
				widget = quarks[sbs.value][txtv.value];
			})
			.enterKeyAction_({|view|
				txtv.value_(0);
				txtv.focus(true);
			});
		if(GUI.id == \cocoa, {	typesview.focusColor_(XiiColors.darkgreen.alpha_(0.9)) });

		storedSettingsPop = GUI.popUpMenu.new(win, Rect(10, 128, 78, 16)) // 550
			.font_(GUI.font.new("Helvetica", 9))
			.canFocus_(false)
			.items_(settingRegister.getSettingsList)
			.background_(Color.white);

		loadSettingButt = GUI.button.new(win, Rect(95, 128, 35, 17))
			.states_([["load", Color.black, Color.clear]])
			.font_(GUI.font.new("Helvetica", 9))
			.canFocus_(false)
			.action_({arg butt; 
				settingRegister.loadSetting(storedSettingsPop.items[storedSettingsPop.value]);
			});

		settingNameView = GUI.textView.new(win, Rect(10, 151, 78, 14))
			.font_(GUI.font.new("Helvetica", 9))
			.string_(settingName = PathName(settingName).nextName)
			.keyDownAction_({arg view, key, mod, unicode; 
				if(unicode ==13, {
					comingFromFieldFlag = true;
					storeSettingButt.focus(true);
				});
			});
		
		storeSettingButt = GUI.button.new(win, Rect(95, 150, 35, 17))
			.states_([["store", Color.black, Color.clear]])
			.font_(GUI.font.new("Helvetica", 9))
			.canFocus_(false)
			.action_({arg butt; 
				settingName = PathName(settingNameView.string).nextName;
				settingRegister.storeSetting(settingNameView.string);
				storedSettingsPop.items_(settingRegister.getSettingsList);
				settingNameView.string_(settingName);
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
				settingName = PathName(settingNameView.string).nextName;
				settingNameView.string_(settingName);
			});

		deleteSettingButt = GUI.button.new(win, Rect(95, 172, 35, 17))
			.states_([["delete", Color.black, Color.clear]])
			.font_(GUI.font.new("Helvetica", 9))
			.canFocus_(false)
			.action_({arg butt; 
				settingRegister.removeSetting(storedSettingsPop.items[storedSettingsPop.value]);
				storedSettingsPop.items_(settingRegister.getSettingsList);
			});

		clearScreenButt = GUI.button.new(win, Rect(95, 194, 35, 17))
			.states_([["clear", Color.black, Color.clear]])
			.font_(GUI.font.new("Helvetica", 9))
			.canFocus_(false)
			.action_({arg butt; 
				settingRegister.clearixiQuarks;
			});

		txtv = GUI.listView.new(win,Rect(140,10, 120, 152))
			.items_(quarks[0])
			.hiliteColor_(XiiColors.darkgreen) //Color.new255(155, 205, 155)
			.background_(XiiColors.listbackground)
			.selectedStringColor_(Color.black)
			.action_({ arg sbs;
				("Xii"++quarks[typesview.value][sbs.value]).postln;
				widget = quarks[typesview.value][sbs.value];
			})
			.enterKeyAction_({|view|
				widget = quarks[typesview.value][view.value];
				widgetCodeString = "Xii"++widget++".new(Server.default,"++channels++")";
				XQ.globalWidgetList.add(widgetCodeString.interpret);
			})
			.keyDownAction_({arg view, char, modifiers, unicode;
				if(unicode == 13, {
					widget = quarks[typesview.value][view.value];
					widgetCodeString = "Xii"++widget++".new(Server.default,"++channels++")";
					XQ.globalWidgetList.add(widgetCodeString.interpret);
				});
				if (unicode == 16rF700, { txtv.valueAction = txtv.value - 1;  });
				if (unicode == 16rF703, { txtv.valueAction = txtv.value + 1;  });
				if (unicode == 16rF701, { txtv.valueAction = txtv.value + 1;  });
				if (unicode == 16rF702, { typesview.focus(true);  });
			});
			
		if(GUI.id == \cocoa, {	txtv.focusColor_(XiiColors.darkgreen.alpha_(0.9)) });
		
		stereoButt = OSCIIRadioButton(win, Rect(140, 174, 12, 12), "stereo")
					.value_(1)
					.font_(GUI.font.new("Helvetica", 9))
					.action_({ arg butt;
							if(butt.value == 1, {
							channels = 2;
							monoButt.value_(0);
							});
					});

		monoButt = OSCIIRadioButton(win, Rect(140, 192, 12, 12), "mono ")
					.value_(0)
					.font_(GUI.font.new("Helvetica", 9))
					.action_({ arg butt;
							if(butt.value == 1, {
								channels = 1;
								stereoButt.value_(0);
							});	
					});

		openSndFolder = GUI.button.new(win, Rect(195, 184, 13, 18))
				.states_([["f",Color.black,Color.clear]])
				.font_(GUI.font.new("Helvetica", 9))
				.canFocus_(false)
				.action_({ arg butt;
					"open sounds/ixiquarks/".unixCmd
				});
								
		openButt = GUI.button.new(win, Rect(210, 184, 50, 18))
				.states_([["Open",Color.black,Color.clear]])
				.font_(GUI.font.new("Helvetica", 9))
				.canFocus_(false)
				.action_({ arg butt;
					widgetCodeString = "Xii"++widget++".new(Server.default,"++channels++")";
					XQ.globalWidgetList.add(widgetCodeString.interpret);
				});
				
		// MIDI control of sliders		
		if(midi == true, {
			"MIDI is ON".postln;
			MIDIIn.control = { arg src, chan, num, val;
				var wcnt;					
				if(num == midiRotateWindowChannel, {
					{
					wcnt = GUI.window.allWindows.size;
					if(XQ.globalWidgetList.size > 0, {
						chosenWidget = val % wcnt;
						GUI.window.allWindows.at(chosenWidget).front;
						XQ.globalWidgetList.do({arg widget, i;
							if(widget.xiigui.isKindOf(XiiEffectGUI), {
								if(GUI.window.allWindows.at(chosenWidget) === widget.xiigui.win, {
									widgetnum = i;
								});
							});
						});
					});
					}.defer;
				},{
				{
				XQ.globalWidgetList[widgetnum].xiigui.setSlider_(
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
			GUI.pen.color = XiiColors.ixiorange;
			GUI.pen.width = 3;
			GUI.pen.translate(30,182);
			GUI.pen.scale(0.6,0.6);
			GUI.pen.moveTo(1@7);
			ixilogo.do({arg point;
				GUI.pen.lineTo(point+0.5);
			});
			GUI.pen.stroke;
		};
		win.refresh;
		win.front;
	}
}
