
// called from each effect in the XiiEffects.sc file
// NOTE: The creator argument is the effect object which
// is necessary to have here on closing the window in order to 
// free the reference to the object in the XQ.globalWidgetList

// the setting argument is passed from the settings of XiiQuarks.sc
// if the gui is run from a setting

XiiEffectGUI {
	var <>win;
	var <>slider, <>specs, <>param, channels, inbus, outbus, tgt, addAct;
	var bufnum, synth, fxOn;
	
	*new { arg name, synthdef, param, ch, creator, setting=nil;
		^super.new.initGUI(name, synthdef, param, ch, creator, setting);
		}
		
	initGUI { arg name, synthdef, par, ch, creator, setting; 
		var lay, moveSynth;
		var nodeLabel, help, synthParams, point, stereoChList, monoChList;
		var onOffButt, cmdPeriodFunc;
		
		param = par; // I need to return this from a func so put into a var
		tgt = if(setting.notNil, {setting[3]}, {1});
		inbus = if(setting.notNil, {setting[1]}, {0}); 
		outbus = if(setting.notNil, {setting[2]}, {0}); 
//		addAct = if(setting.notNil, {setting[4]}, {\addToTail}); 
		addAct = \addToTail; 
		fxOn = if(setting.notNil, {setting[6]}, { false }); 
		slider = Array.newClear(param[0].size); 
		bufnum = nil;
		specs = param[2];
		
		// mono or stereo?
		channels = ch;
		stereoChList = XiiACDropDownChannels.getStereoChnList;
		monoChList = 	 XiiACDropDownChannels.getMonoChnList;
					
		point = if(setting.isNil.not, {setting[4]}, {XiiWindowLocation.new(name)});
		
		win = GUI.window.new(name, 
				Rect(point.x, point.y, 310, (param[0].size * 20) + 50), 
				resizable:false); 
		win.view.decorator = lay = FlowLayout(win.view.bounds, 5@5, 5@5); 

		GUI.staticText.new(win, 12 @ 15).font_(GUI.font.new("Helvetica", 9))
			.string_("in").align_(\right); 
		GUI.popUpMenu.new(win, 40 @ 15)
			.items_(if(channels==1, {monoChList},{stereoChList}))
			.value_(inbus/ch) 
			.font_(GUI.font.new("Helvetica", 9))
			.background_(Color.white)
			.canFocus_(false)
			.action_({ arg ch;
				if(channels==1, {inbus = ch.value}, {inbus = ch.value * 2});
				if (fxOn, { synth.set(\inbus, inbus) });
			});

		GUI.staticText.new(win, 14 @ 15).font_(GUI.font.new("Helvetica", 9))
			.string_("out").align_(\right); 
		GUI.popUpMenu.new(win, 40 @ 15)
			.items_(if(channels==1, {monoChList},{stereoChList}))
			.value_(outbus/ch)
			.font_(GUI.font.new("Helvetica", 9))
			.background_(Color.white)
			.canFocus_(false)
			.action_({ arg ch;
				if(channels==1, {outbus = ch.value}, {outbus = ch.value * 2});
				if (fxOn, { synth.set(\outbus, outbus) });
			});
			
		GUI.staticText.new(win, 15 @ 15).font_(GUI.font.new("Helvetica", 9))
			.string_("Tgt").align_(\right); 
		GUI.numberBox.new(win, 25 @ 15).font_(GUI.font.new("Helvetica", 9))
			.value_(tgt).action_({|v| 
		� �v.value = 0.max(v.value); 
		� �tgt = v.value.asInteger; 
		� �moveSynth.value; 
		}); 
		
		GUI.popUpMenu.new(win, 60@15) 
		� �.font_(GUI.font.new("Helvetica", 9)) 
		� �.items_(["addToHead", "addToTail", "addAfter", "addBefore"]) 
		� �.value_(1) 
		� �.action_({|v| 
		� �� �addAct = v.items.at(v.value).asSymbol; 
		� �� �moveSynth.value; 
		� �}); 
		� � 
		GUI.button.new(win,12@15) 
		� �.font_(GUI.font.new("Helvetica", 9)) 
		� �.states_([["#"]]) 
		� �.action_({|v| 
		� �� �� �synthParams = Array.new; 
		� �� �� �synthParams = synthParams.add(param[1]); // synth params 
		� �� �� �synthParams = synthParams.add(param[3]); // argument values 
		� �� �� �synthParams = ['inbus', inbus].add(synthParams.flop).flat; 
		� �� �� �synthParams = ['outbus', outbus].add(synthParams.flop).flat.asCompileString; 
		� �� �� �("Synth.new(\\" ++ synthdef ++ ", " ++ synthParams ++ 
		� �� �� �� �", target: " ++ tgt ++ ", addAction: \\" ++ addAct ++ ")").postln; 
		� �}); 
		
		onOffButt = GUI.button.new(win, 30@15) 
		� �.font_(GUI.font.new("Helvetica", 9)) 
		� �.states_([["On", Color.black, Color.clear], 
					["Off", Color.black, Color.green(alpha:0.2)]]) 
		� �.action_({|v| 
		� �� �if ( v.value == 0, { 
		� �� �� �fxOn = false; 
		� �� �� �nodeLabel.string = "none"; 
		� �� �� �synth.free;
				param[0].size.do({|i| // if automation 
				� �slider[i].pause;
				});
		� �� �},{ 
		� �� �� �fxOn = true; 
		� �� �� �synthParams = Array.new; 
		� �� �� �synthParams = synthParams.add(param[1]); // synth params 
		� �� �� �synthParams = synthParams.add(param[3]); // argument values 
		� �� �� �synthParams = ['inbus', inbus].add(synthParams.flop).flat; 
		� �� �� �synthParams = ['outbus', outbus].add(synthParams.flop).flat; 
		� �� �� �synth = Synth.new(synthdef, synthParams, target: tgt.asTarget, addAction: addAct); 
		� �� �� �nodeLabel.string = synth.nodeID;
				param[0].size.do({|i| // if automation 
				� �slider[i].resume;
				});
		� �� � }) 
		� �}); 
				
		param[0].size.do({|i| 
		� �slider[i] = XiiEZSlider(win, 288@15, param[0][i], param[2][i], 
									labelWidth: 50, numberWidth: 40); 
		� �slider[i].labelView.font_(GUI.font.new("Helvetica", 9)); 
		� �slider[i].numberView.font_(GUI.font.new("Helvetica", 9)); 
		� �slider[i].sliderView.background_(Gradient(Color.new255(103, 148, 103, 0), 
		� �� �Color.new255(103, 148, 103, 200), \h, 31)); 
		� �slider[i].action = {|v| 
		� �� �param[3][i] = v.value; 
		� �� �if (fxOn, { synth.set(param[1][i], v.value) }) 
		� � }; 
		� �slider[i].value = param[3][i]; 
		� �lay.nextLine; 
		}); 
		
		// look for a buffer.bufnum - in those effects that use buffers (mrroque, multidelay)
		param[1].size.do({|i| 
			if(param[1][i] == \bufnum, { //"buffer found".postln; 
				bufnum = param[3][i];
			});
		});

		GUI.staticText.new(win,50 @ 15).font_(GUI.font.new("Helvetica", 9))
			.align_(\right).string_("nodeID"); 
		nodeLabel = GUI.staticText.new(win,50 @ 15)
				.font_(GUI.font.new("Helvetica", 9))
				.align_(\left).string_("none"); 
		moveSynth = { 
		� �if ( fxOn, { 
		� �� �case 
		� �� �� �{ addAct === \addToHead }� { synth.moveToHead(tgt.asTarget) } 
		� �� �� �{ addAct === \addToTail }� { synth.moveToTail(tgt.asTarget) } 
		� �� �� �{ addAct === \addAfter� }� { synth.moveAfter(tgt.asTarget)� } 
		� �� �� �{ addAct === \addBefore }� { synth.moveBefore(tgt.asTarget) } 
		� �}) 
		}; 
		win.view.keyDownAction = { arg ascii, char; 
		� �case 
		� �� �{char == $n} { Server.default.queryAllNodes } 
		}; 
		
		cmdPeriodFunc = { onOffButt.valueAction_(0)};
		CmdPeriod.add(cmdPeriodFunc);

		win.onClose_({
			var t;
			param[0].do({|slider| 
		� �		slider.stop;
			});
			if (fxOn, { synth.free; });
			CmdPeriod.remove(cmdPeriodFunc);
			if(bufnum != nil, { // if the effect is using a buffer
				Server.default.sendMsg(\b_free, bufnum);
				Server.default.bufferAllocator.free(bufnum);
			});
			
			XQ.globalWidgetList.do({arg widget, i; if(widget === creator, { t = i})});
			try{XQ.globalWidgetList.removeAt(t)};

			// write window position to archive.sctxar
			point = Point(win.bounds.left, win.bounds.top);
			XiiWindowLocation.storeLoc(name, point);
		}); 
		
		// on or off?
		if(fxOn, { onOffButt.valueAction_(1) });

		win.front; 
	} // end of initGui
	
	setSlider_ {arg slnum, val;
		if((slnum>=0) && (slnum<slider.size), {
			slider[slnum].valueAction_(val);
		});
	}
	
	getState {
		var point;
		point = Point(win.bounds.left, win.bounds.top);
		^[channels, inbus, outbus, tgt, point, param[3], fxOn];
	}
	
}


