XiiRecorder {	

	var <>gui;

	*new { arg server;
		^super.new.initXiiRecorder(server);
		}
		
	initXiiRecorder {arg server;
		var window, bgColor, foreColor, spec, outbus;
		var s, name, point;
		var txtv, recButton, r, filename, timeText, secTask, inbus, numChannels;
		var stereoButt, monoButt, cmdPeriodFunc;
		
		inbus = 0;
		numChannels = 2;
		filename = "";
		name = "      Sound Recorder";
		s = server ? Server.default;
		
		point = XiiWindowLocation.new(name);
		
		bgColor = Color.new255(155, 205, 155);
		foreColor = Color.new255(103, 148, 103);
		outbus = 0;
		
		window = SCWindow.new(name, Rect(point.x, point.y, 222, 80), resizable:false).front;
					
		stereoButt = OSCIIRadioButton(window, Rect(10,5,14,14), "stereo")
						.value_(1)
						.font_(Font("Helvetica", 9))
						.action_({ arg butt;
								if(butt.value == 1, {
								numChannels = 2;
								monoButt.value_(0);
								});
						});

		monoButt = OSCIIRadioButton(window, Rect(100,5,14,14), "mono")
						.value_(0)
						.font_(Font("Helvetica", 9))
						.action_({ arg butt;
								if(butt.value == 1, {
								numChannels = 1;
								stereoButt.value_(0);
								});
						});

		txtv = SCTextView(window, Rect(10, 25, 160, 16))
				.hasVerticalScroller_(false)
				.autohidesScrollers_(true)
				.string_(filename);

		recButton = SCButton(window, Rect(114, 50, 56, 16))
			.states_([	["Record",Color.black, Color.clear], 
						["Stop",Color.red,Color.red(alpha:0.2)]])
			.font_(Font("Helvetica", 9))
			.action_({ arg butt;
				if(s.serverRunning == true, {
					if(butt.value == 1, {
						filename = txtv.string;
						if(filename == "", {filename = Date.getDate.stamp.asString});
						txtv.string_(filename);
						r = Record(s, inbus, numChannels);
						r.start("sounds/ixiquarks/"++filename++".aif");
						secTask.start;
					}, {
						r.stop;
						secTask.stop;
					});
				}, {
					"ixi alert: you need to start the server in order to record".warn;
					recButton.value_(0);
				});
			});
		
		timeText = SCStaticText(window, Rect(70, 50, 40, 16))
					.string_("00:00");

		// record busses
		SCPopUpMenu(window, Rect(10, 50, 44, 16))
			.items_(XiiACDropDownChannels.getStereoChnList)
			.value_(0)
			.font_(Font("Helvetica", 9))
			.background_(Color.white)
			.canFocus_(false)
			.action_({ arg ch;
				inbus = ch.value * 2;
			});
			
		// updating the seconds text		
		secTask = Task({var sec, min, secstring, minstring;
			sec = 0;
			min = 0;
			inf.do({arg i; 
				sec = sec + 1;
				if(sec > 59, {min = min+1; sec = 0;});
				if(min < 10, {minstring = "0"++min.asString}, {minstring = min.asString});
				if(sec < 10, {secstring = "0"++sec.asString}, {secstring = sec.asString});
				{timeText.string_(minstring++":"++secstring)}.defer;
				1.wait;
			});
		});
		
		cmdPeriodFunc = { recButton.valueAction_(0);};
		CmdPeriod.add(cmdPeriodFunc);

		window.onClose_({
			var t;
			recButton.valueAction_(0); // stop recording
			CmdPeriod.remove(cmdPeriodFunc);
			~globalWidgetList.do({arg widget, i; if(widget === this, { t = i})});
			~globalWidgetList.removeAt(t);
			// write window position to archive.sctxar
			point = Point(window.bounds.left, window.bounds.top);
			XiiWindowLocation.storeLoc(name, point);
		});
	}
}