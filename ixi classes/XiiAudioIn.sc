XiiAudioIn {	

	var <>gui;
	
	*new { arg server;
		^super.new.initAudioIn(server);
		}
		
	initAudioIn {arg server;
		var responder, inmeterl, leftVol, inmeterr, rightVol, panLslider, panRslider;
		var window, bgColor, foreColor, spec, outbus;
		var audioInSynth, s, name, point;
		var onOffButt, cmdPeriodFunc;
		
		name = "          AudioIn 2x2";
		s = server ? Server.local;
		
		point = XiiWindowLocation.new(name);
		
		bgColor = XiiColors.lightgreen;
		foreColor = XiiColors.darkgreen;
		outbus = 0;
		
		window = SCWindow.new(name, Rect(point.x, point.y, 222, 106), resizable:false).front;
		
		spec = ControlSpec(0, 1.0, \amp); // for amplitude in rec slider
		
		inmeterl = SCRangeSlider(window, Rect(10, 10, 20, 80));
		inmeterl.background_(bgColor).knobColor_(foreColor);
		inmeterl.lo_(0).hi_(0.05);
		inmeterl.canFocus_(false);
		
		leftVol = SCSlider(window, Rect(40, 10, 10, 80));
		leftVol.canFocus_(false);
		leftVol.background_(bgColor).knobColor_(foreColor);
		leftVol.action_({ arg sl; audioInSynth.set(\volL, spec.map(sl.value)) });
		
		inmeterr = SCRangeSlider(window, Rect(60, 10, 20, 80));
		inmeterr.background_(bgColor).knobColor_(foreColor);
		inmeterr.lo_(0).hi_(0.05);
		inmeterr.canFocus_(false);
		
		rightVol = SCSlider(window, Rect(90, 10, 10, 80));
		rightVol.canFocus_(false);
		rightVol.background_(bgColor).knobColor_(foreColor);
		rightVol.action_({ arg sl; audioInSynth.set(\volR, spec.map(sl.value)) });
		
		responder = OSCresponderNode(s.addr, '/tr', { arg t, r, msg;
			{
				if(msg[2] == 800, { inmeterl.hi_(1-(msg[3].ampdb.abs * 0.01)) });
				if(msg[2] == 801, { inmeterr.hi_(1-(msg[3].ampdb.abs * 0.01)) });
			}.defer;
		}).add;
			
		panLslider = OSCIISlider.new(window, Rect(110, 10, 100, 10), "- L pan", -1, 1, -1, 0.01)
			.font_(Font("Helvetica", 9))
			.action_({arg sl; audioInSynth.set(\panL, sl.value)});
		panRslider = OSCIISlider.new(window, Rect(110, 40, 100, 10), "- R pan", -1, 1, 1, 0.01)
			.font_(Font("Helvetica", 9))		
			.action_({arg sl; audioInSynth.set(\panR, sl.value)});
		
		SCPopUpMenu(window,Rect(110, 75, 50, 16))
			.items_(XiiACDropDownChannels.getStereoChnList)
			.value_(0)
			.background_(Color.white)
			.font_(Font("Helvetica", 9))
			.canFocus_(false)
			.action_({ arg ch;
				outbus = ch.value * 2;
				audioInSynth.set(\out, outbus );
			});
			
		onOffButt = SCButton(window,Rect(170, 75, 36, 16))
			.states_([
					["on",Color.black, Color.clear],
					["off",Color.black,bgColor]
				])
			.font_(Font("Helvetica", 9))
			.action_({ arg butt;
				if(butt.value == 1, {
					audioInSynth = Synth(\xiiAudioIn, [\out, outbus, 
							\volL, leftVol.value, \volR, rightVol.value, 
							\panL, panLslider.value, \panR, panRslider.value]);
				},{
					audioInSynth.free;
					
					{inmeterl.hi_(0);
					inmeterr.hi_(0);}.defer(0.2);
				});
			});
		
		
		cmdPeriodFunc = { onOffButt.valueAction_(0)};
		CmdPeriod.add(cmdPeriodFunc);
			
		window.onClose_({
			var t;
			responder.remove;
			audioInSynth.free;
			CmdPeriod.remove(cmdPeriodFunc);
			
			~globalWidgetList.do({arg widget, i; if(widget === this, { t = i})});
			~globalWidgetList.removeAt(t);

			// write window position to archive.sctxar
			point = Point(window.bounds.left, window.bounds.top);
			XiiWindowLocation.storeLoc(name, point);
		});
	}
}