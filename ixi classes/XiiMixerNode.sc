XiiMixerNode {	

	var <>gui;

	*new { arg server, channels;
		^super.new.initXiiMixerNode(server, channels);
		}
		
	initXiiMixerNode {arg server, ch;
		var panLslider;
		var window, bgColor, foreColor, spec;
		var s, name, point;
		var stereoChList, monoChList, channels;
		var inbus, outbus, synth;
		var tgt, addAct;
		var onOffButt, cmdPeriodFunc;
		
		tgt = 1;
		addAct = \addToTail;
		
		s = server ? Server.local;
		if(ch==1, {name = "    MixerNode - 1x2"},{name = "    MixerNode - 2x1"});

		channels = ch;
		stereoChList = XiiACDropDownChannels.getStereoChnList;
		monoChList =   XiiACDropDownChannels.getMonoChnList;

		point = XiiWindowLocation.new(name);
		
		bgColor = Color.new255(155, 205, 155);
		foreColor = Color.new255(103, 148, 103);
		outbus = 0;
		
		window = SCWindow.new(name, Rect(point.x, point.y, 222, 70), resizable:false).front;
		
		SynthDef(\mixerNode1x2, { arg inbus, outbus, pan;
			var in;
			in = In.ar(inbus, 1);
			Out.ar(outbus, Pan2.ar(in, pan));
		}).load(s);
		
		SynthDef(\mixerNode2x1, { arg inbus, outbus, pan;
			var in;
			in = In.ar(inbus, 2);
			in = Balance2.ar(in[0], in[1], pan);
						
			Out.ar(outbus, Mix.ar(in));
		}).load(s);
				
		spec = ControlSpec(0, 1.0, \amp); // for amplitude in rec slider

		SCStaticText(window, Rect(10, 9, 40, 16)).string_("in");
		SCPopUpMenu(window,Rect(35, 10, 50, 16))
			.items_(if(channels==1, {monoChList},{stereoChList}))
			.value_(0)
			.background_(Color.white)
			.font_(Font("Helvetica", 9))
			.canFocus_(false)
			.action_({ arg ch;
				if(channels==1, {inbus = ch.value}, {inbus = ch.value * 2});
				synth.set(\inbus, inbus );
			});

		SCStaticText(window, Rect(10, 34, 40, 16)).string_("out");
		SCPopUpMenu(window,Rect(35, 35, 50, 16))
			.items_(if(channels==1, {stereoChList}, {monoChList}))
			.value_(0)
			.background_(Color.white)
			.font_(Font("Helvetica", 9))
			.canFocus_(false)
			.action_({ arg ch;
				if(channels==2, {outbus = ch.value}, {outbus = ch.value * 2});
				synth.set(\outbus, outbus );
			});
			
		// panning sliders
		panLslider = OSCIISlider.new(window, Rect(100, 10, 100, 10), "- pan", -1, 1, 0, 0.01)
			.action_({arg sl; synth.set(\pan, sl.value)});
		
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
					if(channels == 1, { //// HERE !!!
		        			synth = Synth.new(\mixerNode1x2, 
										[\inbus, inbus, \outbus, outbus], 
										target: tgt.asTarget,
										addAction: addAct); 
					},{
		        			synth = Synth.new(\mixerNode2x1, 
										[\inbus, inbus, \outbus, outbus], 
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
			// write window position to archive.sctxar
			point = Point(window.bounds.left, window.bounds.top);
			XiiWindowLocation.storeLoc(name, point);
		});
	}
}