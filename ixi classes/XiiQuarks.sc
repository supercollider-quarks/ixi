XiiQuarks {	

	*new { 
		^super.new.initXiiQuarks;
	}
		
	initXiiQuarks {
		var win, txtv, quarks, serv, channels;
		var openButt, effectCodeString, monoButt, stereoButt, effect;
		var name, point;
		var midi, midiControllerNumbers, midiRotateWindowChannel, midiInPorts, midiOutPorts;
		var guistyle;
		var chosenWidget, effectnum, types, typesview, ixilogo;
		var prefFile, preferences;

		////////////// preferences ///////////////////
		Server.default = Server.local;
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
		
		XiiACDropDownChannels.numChannels_( 52 ); // NUMBER OF AUDIO BUSSES USED
				
		if(guistyle == "new", {
			XiiLoadSynthDefs.new(Server.default);
	
			name = "quarks";
			point = XiiWindowLocation.new(name);
			
			win = SCWindow(name, Rect(point.x, point.y, 275, 212), resizable:false).front;
			
			~globalWidgetList = List.new; // keep track of active widgets
			// (contains [List [buffers], [selstart, sellength]])
			~globalBufferDict = ();  // ICMC
			~bufferPoolNum = -1;
			
			quarks = [ 
				["AudioIn", "Recorder", "Player", "BufferPool", "PoolManager", 
				"FreqScope", "WaveScope", "EQMeter", "MixerNode", 
				"ChannelSplitter", "Amplifier", "TrigRecorder"],
		
				["SoundScratcher", "Predators", "Gridder", "BufferPlayer", "GrainBox", 
				"PolyMachine", "ScaleSynth"], 
				
				["Delay", "Freeverb", "AdCVerb", "Distortion", "ixiReverb", "Chorus",
				"Octave", "Tremolo", "Equalizer", "CombVocoder", "RandomPanner", "MRRoque",
				"MultiDelay"],
				
				["Bandpass", "Lowpass", "Highpass", "RLowpass", "RHighpass", "Resonant", "Klanks"],
				
				["Noise", "Oscillators"]
			];
			
			types = ["utilities", "instruments", "effects", "filters", "other"];
			
			ixilogo = [ // the ixi logo
				Point(1,7), Point(8, 1), Point(15,1), Point(15,33),Point(24, 23), Point(15,14), Point(15,1), 
				Point(23,1),Point(34,13), Point(45,1), Point(61,1), Point(66,6), Point(66,37), Point(59,43),
				Point(53,43), Point(53,12), Point(44,22), Point(53,33), Point(53,43), Point(42,43), Point(34,32),
				Point(24,43), Point(7,43), Point(1,36), Point(1,8)
				];
	
	
			channels = 2;
			effect = "AudioIn";
	
			typesview = SCListView(win,Rect(10,10, 120, 152))
				.items_(types)
				.hiliteColor_(XiiColors.darkgreen) //Color.new255(155, 205, 155)
				.background_(XiiColors.listbackground)
				.selectedStringColor_(Color.black)
				.action_({ arg sbs;
					txtv.items_(quarks[sbs.value]);
					txtv.value_(0);
					effect = quarks[sbs.value][txtv.value];
				})
				.enterKeyAction_{|view|
					txtv.value_(0);
					txtv.focus(true);
				};
			
			txtv = SCListView(win,Rect(140,10, 120, 152))
				.items_(quarks[0])
				.hiliteColor_(XiiColors.darkgreen) //Color.new255(155, 205, 155)
				.background_(XiiColors.listbackground)
				.selectedStringColor_(Color.black)
				.action_({ arg sbs;
					("Xii"++quarks[typesview.value][sbs.value]).postln;
					effect = quarks[typesview.value][sbs.value];
				})
				.enterKeyAction_{|view|
					effect = quarks[typesview.value][view.value];
					effectCodeString = "Xii"++effect++".new(Server.default,"++channels++")";
					~globalWidgetList.add(effectCodeString.interpret);
				};
	
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
				// set the Color
				//Color.new255(255, 100, 0).set;
				XiiColors.ixiorange.set;
				Pen.width = 3;
				Pen.translate(48,172);
				Pen.scale(0.6,0.6);
				Pen.moveTo(1@7);
				ixilogo.do({arg point;
					Pen.lineTo(point+0.5);
				});
				Pen.stroke
			};
			win.refresh;
		
		}, {
			// OLD GUI STYLE 
			
			XiiLoadSynthDefs.new(Server.default);
	
			name = "quarks";
			point = XiiWindowLocation.new(name);
			
			win = SCWindow(name, Rect(point.x, point.y, 140, 212), resizable:false).front;
			
			~globalWidgetList = List.new; // keep track of active widgets
			// (contains [List [buffers], [selstart, sellength]])
			~globalBufferDict = ();  // ICMC
			~bufferPoolNum = -1;
			
			quarks = [ 
			"AudioIn", "Recorder", "Player", "BufferPool", "PoolManager", 
			"FreqScope", "WaveScope", "EQMeter", "MixerNode", 
			"ChannelSplitter", "Amplifier", "TrigRecorder",
			"           --------     ",  
			"SoundScratcher", "Predators", "Gridder", "BufferPlayer", "GrainBox", 
			"PolyMachine", "ScaleSynth", 
			"           --------     ",
			"Delay", "Freeverb", "AdCVerb", "Distortion", "ixiReverb", "Chorus",
			"Octave", "Tremolo", "Equalizer", "CombVocoder", "RandomPanner", "MRRoque",
			"MultiDelay",
			"           --------     ",
			"Bandpass", "Lowpass", "Highpass", "RLowpass", "RHighpass", "Resonant", "Klanks",		"           --------     ",
			"Noise", "Oscillators"
			
			];
			
			channels = 2;
			effect = "AudioIn";
			
			txtv = SCListView(win,Rect(10,10, 120, 152))
				.items_(quarks)
				.hiliteColor_(XiiColors.darkgreen) //Color.new255(155, 205, 155)
				.background_(XiiColors.listbackground)
				.selectedStringColor_(Color.black)
				.action_({ arg sbs;
					("Xii"++quarks.at(sbs.value)).postln;
					if(quarks.at(sbs.value).contains("-").not, {
						effect = quarks.at(sbs.value);
					});
				})
				.enterKeyAction_{|view|
					if(quarks.at(txtv.value).contains("-").not, {
						effectCodeString = "Xii"++effect++".new(Server.default,"++channels++")";
						~globalWidgetList.add(effectCodeString.interpret);
					});
				};
	
			stereoButt = OSCIIRadioButton(win, Rect(10, 168, 14,14), "stereo")
						.value_(1)
						.action_({ arg butt;
								if(butt.value == 1, {
								channels = 2;
								monoButt.value_(0);
								});
						});
	
			monoButt = OSCIIRadioButton(win, Rect(10, 190, 14,14), "mono")
						.value_(0)
						.action_({ arg butt;
								if(butt.value == 1, {
									channels = 1;
									stereoButt.value_(0);
								});	
						});
									
			openButt = SCButton(win, Rect(80, 170, 50, 18))
					.states_([["Open",Color.black,Color.clear]])
					.action_({ arg butt;
						if(quarks.at(txtv.value).contains("-").not, {
							effectCodeString = "Xii"++effect++".new(Server.default,"++channels++")";
							~globalWidgetList.add(effectCodeString.interpret);
						});
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
		});
	}
}