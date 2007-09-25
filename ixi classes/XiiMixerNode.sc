
XiiMixerNode {	

	var <>xiigui;
	var <>win, params;

	var channels;
	
	*new { arg server, channels, setting = nil;
		^super.new.initXiiMixerNode(server, channels, setting);
		}
		
	initXiiMixerNode {arg server, ch, setting;
		var panLslider;
		var bgColor, foreColor, spec;
		var s, name, point;
		var stereoChList, monoChList;
		var inbus, outbus, pan, synth;
		var tgt, addAct;
		var onOffButt, cmdPeriodFunc;
		
		tgt = 1;
		addAct = \addToTail;
		
		s = server ? Server.local;
		
		channels = if(setting.isNil, {ch}, {setting[0]});

		if(ch==1, {name = "    MixerNode - 1x2"},{name = "    MixerNode - 2x1"});

		stereoChList = XiiACDropDownChannels.getStereoChnList;
		monoChList =   XiiACDropDownChannels.getMonoChnList;


		point = if(setting.isNil, {XiiWindowLocation.new(name)}, {setting[1]});
		xiigui = nil; // not using window server class here
		params = if(setting.isNil, {[0,0,0]}, {setting[2]});
		
		bgColor = Color.new255(155, 205, 155);
		foreColor = Color.new255(103, 148, 103);
		inbus = params[0];
		outbus = params[1];
		pan = params[2];
		
		win = SCWindow.new(name, Rect(point.x, point.y, 222, 70), resizable:false).front;
		
		SynthDef(\mixerNode1x2, { arg inbus, outbus, pan;
			var in;
			in = InFeedback.ar(inbus, 1);
			Out.ar(outbus, Pan2.ar(in, pan));
		}).load(s);
		
		SynthDef(\mixerNode2x1, { arg inbus, outbus, pan;
			var in;
			in = InFeedback.ar(inbus, 2);
			in = Balance2.ar(in[0], in[1], pan);
						
			Out.ar(outbus, Mix.ar(in));
		}).load(s);
				
		spec = ControlSpec(0, 1.0, \amp); // for amplitude in rec slider

		// channels dropdown - INPUT CHANNEL
		SCStaticText(win, Rect(10, 9, 40, 16)).string_("in");
		SCPopUpMenu(win,Rect(35, 10, 50, 16))
			.items_(if(channels==1, {monoChList},{stereoChList}))
			.value_(params[0])
			.background_(Color.white)
			.font_(Font("Helvetica", 9))
			.canFocus_(false)
			.action_({ arg ch;
				if(channels==1, {inbus = ch.value}, {inbus = ch.value * 2});
				synth.set(\inbus, inbus );
				params[0] = ch.value;
			});
			
		// channels dropdown - OUTPUT CHANNEL
		SCStaticText(win, Rect(10, 34, 40, 16)).string_("out");
		SCPopUpMenu(win,Rect(35, 35, 50, 16))
			.items_(if(channels==1, {stereoChList}, {monoChList}))
			.value_(params[1])
			.background_(Color.white)
			.font_(Font("Helvetica", 9))
			.canFocus_(false)
			.action_({ arg ch;
				if(channels==2, {outbus = ch.value}, {outbus = ch.value * 2});
				synth.set(\outbus, outbus );
				params[1] = ch.value;
			});
			
		// panning sliders
		panLslider = OSCIISlider.new(win, Rect(100, 10, 100, 10), "- pan", -1, 1, params[2], 0.01)
			.action_({arg sl; synth.set(\pan, sl.value); params[2] = sl.value;});
		
		SCPopUpMenu(win, Rect(100, 40, 66, 16)) 
		� �.font_(Font("Helvetica", 9)) 
		� �.items_(["addToHead", "addToTail", "addAfter", "addBefore"]) 
		� �.value_(1) 
		� �.action_({|v| 
		� �� �addAct = v.items.at(v.value).asSymbol; 
		� �}); 

		onOffButt = SCButton(win,Rect(172, 40, 27, 16))
		� �  .font_(Font("Helvetica", 9)) 
			.states_([
					["On",Color.black, Color.clear],
					["Off",Color.black,bgColor]
				])
			.action_({ arg butt;
				if(butt.value == 1, {
					if(channels == 1, { //// HERE !!!
		� �� �� 			synth = Synth.new(\mixerNode1x2, 
										[\inbus, inbus, \outbus, outbus, \pan, pan], 
										target: tgt.asTarget,
										addAction: addAct); 
					},{
		� �� �� 			synth = Synth.new(\mixerNode2x1, 
										[\inbus, inbus, \outbus, outbus, \pan, pan], 
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