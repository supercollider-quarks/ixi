XiiAmplifier {	
	var <>gui;
	*new { arg server, channels;
		^super.new.initXiiAmplifier(server, channels);
		}
		
	initXiiAmplifier {arg server, ch;
		var ampSlider;
		var window, bgColor, foreColor, spec;
		var s, name, point;
		var stereoChList, monoChList, channels;
		var inbus, outbus, synth;
		var tgt, addAct;
		var onOffButt, cmdPeriodFunc;
		
		tgt = 1;
		addAct = \addToTail;
		
		s = server ? Server.local;
		name = "Amplifier";
		channels = ch;
		stereoChList = XiiACDropDownChannels.getStereoChnList;
		monoChList =   XiiACDropDownChannels.getMonoChnList;

		point = XiiWindowLocation.new(name);
		
		bgColor = Color.new255(155, 205, 155);
		foreColor = Color.new255(103, 148, 103);
		outbus = 0;
		
		window = SCWindow.new(name, Rect(point.x, point.y, 222, 70), resizable:false).front;
		
		SynthDef(\xiiAmplifier1x1, { arg inbus, outbus, pan, amp=1;
			var in;
			in = In.ar(inbus, 1) * amp;
			Out.ar(outbus, in);
		}).load(s);
		
		SynthDef(\xiiAmplifier2x2, { arg inbus, outbus, pan, amp=1;
			var in;
			in = In.ar(inbus, 2) * amp;
			Out.ar(outbus, in);
		}).load(s);
				
		spec = ControlSpec(0, 10.0, \amp); // for amplitude

		// channels dropdown - INPUT CHANNEL
		SCStaticText(window, Rect(10, 9, 40, 16)).string_("in");
		SCPopUpMenu(window,Rect(35, 10, 50, 16))
			.items_(if(channels==1, {monoChList}, {stereoChList}))
			.value_(0)
			.background_(Color.white)
			.font_(Font("Helvetica", 9))
			.canFocus_(false)
			.action_({ arg ch;
				if(channels==1, {inbus = ch.value}, {inbus = ch.value * 2});
				synth.set(\inbus, inbus );
			});
			
		// channels dropdown - OUTPUT CHANNEL
		SCStaticText(window, Rect(10, 34, 40, 16)).string_("out");
		SCPopUpMenu(window,Rect(35, 35, 50, 16))
			.items_(if(channels==1, {monoChList}, {stereoChList}))
			.value_(0)
			.background_(Color.white)
			.font_(Font("Helvetica", 9))
			.canFocus_(false)
			.action_({ arg ch;
				if(channels==2, {outbus = ch.value}, {outbus = ch.value * 2});
				synth.set(\outbus, outbus );
			});
			
		// amp slider
		ampSlider = OSCIISlider.new(window, Rect(100, 10, 100, 10), "- amp", 0, 10, 1, 0.01, \amp)
			.action_({arg sl; synth.set(\amp, sl.value)});
		
		SCPopUpMenu(window, Rect(100, 40, 66, 16)) 
		   .font_(Font("Helvetica", 9)) 
		   .items_(["addToHead", "addToTail", "addAfter", "addBefore"]) 
		   .value_(1) 
		   .action_({|v| 
		      addAct = v.items.at(v.value).asSymbol; 
		   }); 

		onOffButt = SCButton(window,Rect(172, 40, 27, 16))
		   .font_(Font("Helvetica", 9)) 
			.states_([
					["On",Color.black, Color.clear],
					["Off",Color.black,bgColor]
				])
			.action_({ arg butt;
				if(butt.value == 1, {
					if(channels == 1, {
		        			synth = Synth.new(\xiiAmplifier1x1, 
										[\inbus, inbus, 
										\outbus, outbus, 
										\amp, ampSlider.value], 
										target: tgt.asTarget,
										addAction: addAct); 
					},{
		        			synth = Synth.new(\xiiAmplifier2x2, 
										[\inbus, inbus, 
										\outbus, outbus,
										\amp, ampSlider.value], 
										target: tgt.asTarget,
										addAction: addAct); 
					});
				},{
					synth.free;
				});
			});

		cmdPeriodFunc = { onOffButt.valueAction_(0)};
		CmdPeriod.add(cmdPeriodFunc);
			
		window.onClose_({
			var t;
			onOffButt.valueAction_(0);
			CmdPeriod.remove(cmdPeriodFunc);
			~globalWidgetList.do({arg widget, i; if(widget === this, { t = i})});
			~globalWidgetList.removeAt(t);
			synth.free;
			point = Point(window.bounds.left, window.bounds.top);
			XiiWindowLocation.storeLoc(name, point);
		});
	}
}