
XiiChannelSplitter {
	
	var <>xiigui;
	var <>win, params;

	var channels;
	
	*new { arg server, channels, setting = nil;
		^super.new.initXiiChannelSplitter(server, channels, setting);
		}
		
	initXiiChannelSplitter {arg server, ch, setting;
		var bgColor, foreColor, spec;
		var s, name, point;
		var stereoChList, monoChList;
		var inbus, outbus, synth;
		var tgt, addAct, amp;
		var onOffButt, cmdPeriodFunc;
		
		tgt = 1;
		addAct = \addToTail;
		s = server ? Server.local;

		// mono or stereo?
		channels = if(setting.isNil, {ch}, {setting[0]});

		if(ch==1, {name = "  ChannelSplitter - 1x1"}, {name = "  ChannelSplitter - 2x2"});


		stereoChList = XiiACDropDownChannels.getStereoChnList;
		monoChList =   XiiACDropDownChannels.getMonoChnList;

		point = if(setting.isNil, {XiiWindowLocation.new(name)}, {setting[1]});
		xiigui = nil; // not using window server class here
		params = if(setting.isNil, {[0,0,1]}, {setting[2]});
		
		bgColor = Color.new255(155, 205, 155);
		foreColor = Color.new255(103, 148, 103);
		inbus = params[0];
		outbus = params[1];
		amp = params[2];
		
		win = SCWindow.new(name, Rect(point.x, point.y, 222, 70), resizable:false).front;
		
		SynthDef(\xiiChannelSplitter1x1, { arg inbus, outbus, amp=1;
			var in;
			in = InFeedback.ar(inbus, 1);
			Out.ar(outbus, in*amp);
		}).load(s);
		
		SynthDef(\xiiChannelSplitter2x2, { arg inbus, outbus, amp=1;
			var in;
			in = InFeedback.ar(inbus, 2);
			Out.ar(outbus, in*amp);
		}).load(s);
				
		spec = ControlSpec(0, 1.0, \amp); // for amplitude in rec slider

		// channels dropdown - INPUT CHANNEL
		SCStaticText(win, Rect(10, 9, 40, 16)).string_("in");
		SCPopUpMenu(win,Rect(35, 10, 50, 16))
			.items_(if(channels==1, {monoChList},{stereoChList}))
			.value_(0)
			.background_(Color.white)
			.font_(Font("Helvetica", 9))
			.canFocus_(false)
			.action_({ arg ch;
				if(channels==1, {inbus = ch.value}, {inbus = ch.value * 2});
				synth.set(\inbus, inbus );
			});
			
		// channels dropdown - OUTPUT CHANNEL
		SCStaticText(win, Rect(10, 34, 40, 16)).string_("out");
		SCPopUpMenu(win,Rect(35, 35, 50, 16))
			.items_(if(channels==1, {monoChList}, {stereoChList}))
			.value_(0)
			.background_(Color.white)
			.font_(Font("Helvetica", 9))
			.canFocus_(false)
			.action_({ arg ch;
				if(channels==1, {outbus = ch.value}, {outbus = ch.value * 2});
				synth.set(\outbus, outbus );
			});
			
		// panning sliders
		OSCIISlider.new(win, Rect(100, 10, 100, 10), "- amp", 0, 1, 1, 0.01)
			.action_({arg sl; synth.set(\amp, sl.value)});

		SCPopUpMenu(win, Rect(100, 40, 66, 16)) 
		� �.font_(Font("Helvetica", 9)) 
		� �.items_(["addToHead", "addToTail", "addAfter", "addBefore"]) 
		� �.value_(1) 
		� �.action_({|v| 
		� �� �addAct = v.items.at(v.value).asSymbol; 
		� �}); 

		onOffButt = SCButton(win,Rect(172, 40, 27, 16))
		� �.font_(Font("Helvetica", 9)) 
			.states_([
					["On",Color.black, Color.clear],
					["Off",Color.black,bgColor]
				])
			.action_({ arg butt;
				if(butt.value == 1, {
					if(channels == 1, {
		� �� �� 			synth = Synth.new(\xiiChannelSplitter1x1, 
										[\inbus, inbus, \outbus, outbus, \amp, amp], 
										target: tgt.asTarget,
										addAction: addAct); 
					},{
		� �� �� 			synth = Synth.new(\xiiChannelSplitter2x2, 
										[\inbus, inbus, \outbus, outbus, \amp, amp], 
										target: tgt.asTarget,
										addAction: addAct); 
					});
				},{
					synth.free;
				});
			});
		
		cmdPeriodFunc = { onOffButt.valueAction_(0)};
		CmdPeriod.add(cmdPeriodFunc);
			
		win.onClose_({
			var t;
			onOffButt.valueAction_(0);
			CmdPeriod.remove(cmdPeriodFunc);
			~globalWidgetList.do({arg widget, i; if(widget === this, { t = i})});
			try{~globalWidgetList.removeAt(t)};
			synth.free;
			point = Point(win.bounds.left, win.bounds.top);
			XiiWindowLocation.storeLoc(name, point);
		});
	}
	
	getState { // for save settings
		var point;		
		point = Point(win.bounds.left, win.bounds.top);
		^[channels, point, params];
	}

}