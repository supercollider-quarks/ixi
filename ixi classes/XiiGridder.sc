XiiGridder {
	var <>xiigui, <>win, params;

	var selbPool, ldSndsGBufferList, bufferList, sndNameList, bufferPop, gBufferPoolNum;
	var grid, timerArray, tempoClockList, actorOnOffStateArray, actorColors;
	var breedFlag, poolName;
	var outbus, inbus, playFunc, globalvol, freq, pitchratio, sample, gridNodeSampleIndexArray;
	var playModeButt, playScaleOnlyModeButt, playMode, playScaleOnlyMode;
	
	*new {arg server, channels, setting = nil;
		^super.new.initXiiGridder(server, channels, setting);
	}

	initXiiGridder {arg server, channels, setting;
		
		var xgrid, ygrid, point;
		var statesPop, storeButt, clearButt, clearSeqButt, loadArchive, saveArchive;
		var fixedRadioButt, drawBoxGrids, trackVolumeSlider;
		var startSequencer, startButt, volumeSlider, tempoSlider, outbusPoP, scalePop;

		var soundFuncPop, resolutionSlider, startFunc;
		var actorRButtArray, actorTaskArray;
		var resolution, fundamental, selectedActor;
		var playFlag, createGrids;
		
		var tuningArray, scalelib, scalenames, scaleDict, scaleNameField, saveScaleButt;
		var viewKeybButt, midiKeyboard, keyboardVisible;
		var selActorRButtArray, actorTempoSl, stepSizeArray, actorStepSizeSl;
		var freqField, noteField;
		var maxFreq, maxFreqSl, setTuningButt, transposition, transposeSl;
		var cmdPeriodFunc, clearGridButt, fillGridButt, breedFlagButt;
		var selectedNodeField, selectedNode;
		var createCodeWin, synthDefPrototype, synthDefInUse;
		var pitchRatioArray, createAudioStreamBusWin;
		
		"loading XiiGridder".postln;
		playScaleOnlyMode = false;
		playMode = false;

		resolution = 12;
		fundamental = 110;
		timerArray = [0.5, 0.5, 0.5, 0.5];
		actorOnOffStateArray = [0, 0, 0, 0];
		stepSizeArray = [1, 1, 1, 1];
		selectedActor = 0;
		playFlag = false;
		keyboardVisible = false;
		maxFreq = 4000;
		transposition = 1;
		breedFlag = false;
		gridNodeSampleIndexArray = 0!resolution!resolution;
		outbus = 0;
		inbus = 20;
		globalvol = 1;
		sample = 0;

xiigui = nil;
point = if(setting.isNil, {Point(300, 500)}, {setting[1]});
params = if(setting.isNil, {[1, 0, 12, 4000, 1, 120, 1, 0, 0, 1, 0]}, {setting[2]});

		gBufferPoolNum = 0;
		sndNameList = List.new;
		bufferList = List.new; // contains bufnums of buffers (not buffers)
		selectedNode = [0,0];
		
		actorColors = [
			Color(1, 0.72549019607843, 0.058823529411765, 1),
			Color(1, 0.54901960784314, 0, 1),
			Color(0.80392156862745, 0.4078431372549, 0.22352941176471, 1),			Color(0.80392156862745, 0.33333333333333, 0.33333333333333, 1)
		];
		
		tuningArray = this.makeTuning(12, transposition, maxFreq).reverse;
		pitchRatioArray = this.makeRatioTuning(12, transposition).reverse;
		
		scaleDict = if(Object.readArchive("gridderScales.ixi") == nil, {
					().add(12 -> this.loadScales);
					}, {
					 Object.readArchive("gridderScales.ixi");
					});

		scalelib = this.loadScales;
		scalenames = Array.fill(scalelib.size, {arg i; scalelib[i][0]});
		this.setPlayFunc_(1);
		
		// for the scode (the live-coding window)		
		synthDefInUse = nil;
		synthDefPrototype = 
{SynthDef(\xiiGridder, {arg outbus=0, freq=440, pan=0, amp=1;
	var env, sine;
	env = EnvGen.ar(Env.perc, doneAction:2);
	sine = SinOsc.ar(freq, 0, env*amp);
	Out.ar(outbus, Pan2.ar(sine, pan));
}).play(Server.default)}.asCompileString;

		win = SCWindow("Gridder", Rect(point.x, point.y, 615, 500), resizable:false).front;
		
		viewKeybButt = SCButton(win, Rect(590, 476, 16, 16))
				.canFocus_(false)
				.font_(Font("Helvetica", 9))
				.states_([["k", Color.black, Color.clear], 
						["k", Color.black, Color.new255(103, 148, 103, 190)]])
				.action_({arg butt;
					if(butt.value == 1, {
						win.bounds_(Rect(win.bounds.left, win.bounds.top-65, 615, 565));
						win.refresh;
						keyboardVisible = true;
						freqField = SCStaticText(win, Rect(20, 510, 100, 14)).string_("")
									.font_(Font("Helvetica", 9));
						noteField= SCStaticText(win, Rect(20, 530, 100, 14)).string_("")
									.font_(Font("Helvetica", 9));
						midiKeyboard = MIDIKeyboard.new(win, Rect(120, 500, 460, 50), 7, 36)
						.keyDownAction_({arg note; 
							freqField.string_("freq : "+note.midicps.round(0.0001).asString);
							noteField.string_("note : "+note.midinote);
						})
						.keyTrackAction_({arg note; 
							freqField.string_("freq : "+note.midicps.round(0.0001).asString);
							noteField.string_("note : "+note.midinote);
						});
					}, {
						win.bounds_(Rect(win.bounds.left, win.bounds.top+65, 615, 500));
						win.refresh;
						keyboardVisible = false;
						midiKeyboard.remove;
						freqField.remove;
						noteField.remove;
					});

				});
		
		selbPool = SCPopUpMenu(win, Rect(10, 10, 100, 16))
			.font_(Font("Helvetica", 9))
			.items_(if(~globalBufferDict.keys.asArray == [], {["no pool"]}, {~globalBufferDict.keys.asArray}))
			.value_(0)
			.background_(Color.white)
			.action_({ arg item;
				gBufferPoolNum = item.value;
				ldSndsGBufferList.value(selbPool.items[item.value]);
			});

		bufferPop = SCPopUpMenu(win, Rect(10, 32, 100, 16)) // 550
				.font_(Font("Helvetica", 9))
				.items_(["no buffer 1", "no buffer 2"])
				.background_(Color.new255(255, 255, 255))
				.action_({ arg popup;
					gridNodeSampleIndexArray[selectedNode[1]][selectedNode[0]] = popup.value;
				})
				.addAction({bufferPop.action.value( bufferPop.value )}, \mouseDownAction);

		ldSndsGBufferList = {arg argPoolName;
			poolName = argPoolName.asSymbol;

			if(try {~globalBufferDict.at(poolName)[0] } != nil, {
				sndNameList = [];
				bufferList = List.new;
				~globalBufferDict.at(poolName)[0].do({arg buffer;
					sndNameList = sndNameList.add(buffer.path.basename);
					bufferList.add(buffer.bufnum);
				 });
				 bufferPop.items_(sndNameList);
			}, {
				sndNameList = [];
			});
			if(sndNameList.size > 0, { 
				gridNodeSampleIndexArray = Array.fill(resolution, {arg i;      // the octave
						Array.fill(resolution, {arg j; // the notes
							sndNameList.size.rand; // put a random sound into array 
						});
					});
			});
		};
		
		ldSndsGBufferList.value(selbPool.items[0].asSymbol);

		soundFuncPop = SCPopUpMenu(win, Rect(10, 54, 100, 16))
				.font_(Font("Helvetica", 9))
				.items_(["sample", "sine", "bells", "sines", "synth1", "ks_string", 
				"ixi_string", "impulse", "ringz", "klanks", "scode", "audiostream"])
				.background_(Color.new255(255, 255, 255))
				.value_(1)
				.action_({ arg popup;
					if(soundFuncPop.items[popup.value] == "scode", {
						createCodeWin.value;
					}); 
					if(soundFuncPop.items[popup.value] == "audiostream", {
						createAudioStreamBusWin.value;
					}); 
					this.setPlayFunc_(popup.value);
					params[0] = popup.value;
				});
		
		scalePop = SCPopUpMenu(win, Rect(10, 76, 100, 16))
				.font_(Font("Helvetica", 9))
				.items_(scalenames)
				.background_(Color.new255(255, 255, 255))
				.action_({ arg popup; var sclArray;
					grid.clearGrid;
					sclArray = scalelib[popup.value][1];
					
					if(scalelib[popup.value][2].isNil.not, {
						gridNodeSampleIndexArray = scalelib[popup.value][2];
					});
					
					if(sclArray.rank == 1, { // if it's a one dimensional array
						sclArray = Array.fill(resolution, {0});
						sclArray.size.do({arg i; 
							if(scalelib[popup.value][1].includes(i), {
								sclArray[i] = 1;
							});
						});
						grid.setNodeStates_((sclArray!resolution));
					}, { // else, it's an array saved from getGridNodeStates
						grid.setNodeStates_(sclArray);
					});
					params[1] = popup.value;
				});
		
		scaleNameField = SCTextView(win, Rect(10, 98, 66, 12))
				.font_(Font("Helvetica", 9))
				.string_("");

		saveScaleButt = SCButton(win, Rect(80, 98, 28, 14))
				.canFocus_(false)
				.font_(Font("Helvetica", 9))
				.states_([["save", Color.black, Color.clear]])
				.action_({arg butt;
					scalelib = scalelib.add([	scaleNameField.string, // name of setting
											grid.getNodeStates,    // states of grid
											gridNodeSampleIndexArray // bufnum on nodes
										]);
					scaleDict.add(resolution.asInteger -> scalelib);
					scalenames = Array.fill(scalelib.size, {arg i; scalelib[i][0]});
					scalePop.items_(scalenames);
					scalePop.value_(scalenames.size-1);
					
				});

		clearGridButt = SCButton(win, Rect(10, 125, 47, 16))
			.canFocus_(false)
			.font_(Font("Helvetica", 9))
			.states_([["clear", Color.black, Color.clear]])
			.action_({arg butt;
				grid.clearGrid; xgrid.clearGrid; ygrid.clearGrid;
			});

		fillGridButt = SCButton(win, Rect(60, 125, 47, 16))
			.canFocus_(false)
			.font_(Font("Helvetica", 9))
			.states_([["fill", Color.black, Color.clear]])
			.action_({arg butt;
				grid.fillGrid; xgrid.fillGrid; ygrid.fillGrid;
			});

		resolutionSlider = OSCIISlider.new(win, Rect(10, 150, 100, 8), "- resolution", 5, 48, resolution, 1)
						.font_(Font("Helvetica", 9))						.action_({arg sl;
							resolution = sl.value;
							startButt.valueAction_(0); // stop the actors and turn button off
							grid.remove; xgrid.remove; ygrid.remove;
							createGrids.value(resolution);
							gridNodeSampleIndexArray = 0!resolution!resolution;
							// -------- tunings and scales (setting popup menu) -------
							tuningArray = this.makeTuning(resolution, transposition, maxFreq).reverse;
							pitchRatioArray = this.makeRatioTuning(resolution, transposition).reverse;

							// get the resolution scales (if any)
							scalelib = scaleDict.at(resolution.asInteger);
							if(scalelib == nil, {scalelib = []});
							// detract the names
							scalenames = Array.fill(scalelib.size, {arg i; scalelib[i][0]});
							// put them in popup menu
							scalePop.items_(scalenames);
							// load sounds into the gridnodes.
							if(sndNameList.size > 0, { 
								gridNodeSampleIndexArray = Array.fill(resolution, {arg i;      // the octave
									Array.fill(resolution, {arg j; // the notes
										sndNameList.size.rand; // put a random sound into array 
									});
								});
							});
							params[2] = sl.value;
						});
						
		maxFreqSl = OSCIISlider.new(win, Rect(10, 180, 100, 8), "- max freq", 400, 8000, 4000, 1)
						.font_(Font("Helvetica", 9))						.action_({arg sl; 
							maxFreq = sl.value;
							params[3] = sl.value;
						});

		transposeSl = OSCIISlider.new(win, Rect(10, 210, 70, 8), "- transp", 0.25, 4, 1, 0.1)
						.font_(Font("Helvetica", 9))						.action_({arg sl; 
							transposition = sl.value;
							params[4] = sl.value;
						});

		setTuningButt = SCButton(win, Rect(83, 210, 28, 14))
				.canFocus_(false)
				.font_(Font("Helvetica", 9))
				.states_([["set", Color.black, Color.clear]])
				.action_({arg butt;
					tuningArray = this.makeTuning(resolution, transposition, maxFreq).reverse;
					pitchRatioArray = this.makeRatioTuning(resolution, transposition).reverse;
				});
				
		actorRButtArray = Array.fill(4, {arg i;
			OSCIIRadioButton(win, Rect(10+(i*25), 245, 12, 12), (i+1).asString)
				.font_(Font("Helvetica", 9))
				.value_(0)
				.color_(actorColors[i])
				.canFocus_(true)
				.action_({arg butt;
					if(butt.value == 1, {
						if(playFlag, {actorTaskArray[i].start});
					},{
						actorTaskArray[i].stop;
					});
					selectedActor = i;
					actorOnOffStateArray[i] = butt.value;
					selActorRButtArray.do({arg butt; butt.value_(0)});
					selActorRButtArray[i].value_(1);
					actorTempoSl.value_(60/ timerArray[selectedActor]);
				});
			});
			
		selActorRButtArray = Array.fill(4, {arg i;
			SCButton(win, Rect(10+(i*25), 260, 12, 12))
				.font_(Font("Helvetica", 9))
				.value_(0)
				.states_([["",Color.clear,  Color.clear],["", Color.clear,  actorColors[i]]])
				.canFocus_(false)
				.action_({arg butt;
					selActorRButtArray.do({arg butt, cnt; if(cnt != i, {butt.value_(0)})});
					actorRButtArray[i].focus(true);
					selectedActor = i;
					actorTempoSl.value_(60/ timerArray[selectedActor]);
					actorStepSizeSl.value_(stepSizeArray[i]);
				});
			});

		actorTempoSl = OSCIISlider.new(win, Rect(10, 280, 100, 8), "- tempo", 60, 480, 120, 1)
						.font_(Font("Helvetica", 9))						.action_({arg sl; 
							timerArray[selectedActor] = 60/sl.value;
							params[5] = sl.value;
						});
						
		actorStepSizeSl = OSCIISlider.new(win, Rect(10, 310, 100, 8), "- step size", 1, 4, 1, 1)
						.font_(Font("Helvetica", 9))						.action_({arg sl; 
							stepSizeArray[selectedActor] = sl.value;
							params[6] = sl.value;
						});

		breedFlagButt = OSCIIRadioButton(win, Rect(10, 340, 14, 14), "actor breed")
				.font_(Font("Helvetica", 9))
				.value_(0)
				.action_({arg butt;
					if(butt.value == 1, {
						breedFlag = true;
					},{
						breedFlag = false;
					});
				});

		SCStaticText(win, Rect(10, 358, 65, 18))
			.string_("selected node :")
			.font_(Font("Helvetica", 9));

		selectedNodeField = SCStaticText(win, Rect(80, 358, 60, 18))
			.string_(selectedNode.asString)
			.font_(Font("Helvetica", 9));

		playModeButt = OSCIIRadioButton(win, Rect(10, 380, 14, 14), "play mode")
				.font_(Font("Helvetica", 9))
				.value_(0)
				.action_({arg butt;
					if(butt.value == 1, {
						playMode = true;
					},{
						playMode = false;
					});
					params[7] = butt.value;
				});
				
		playScaleOnlyModeButt = OSCIIRadioButton(win, Rect(10, 400, 14, 14), "scale only")
				.font_(Font("Helvetica", 9))
				.value_(0)
				.action_({arg butt;
					if(butt.value == 1, {
						playScaleOnlyMode = true;
					},{
						playScaleOnlyMode = false;
					});
					params[8] = butt.value;
				});

		volumeSlider = OSCIISlider.new(win, Rect(10, 420, 100, 8), "- vol", 0, 1.0, 1, 0.01, \amp)
					.font_(Font("Helvetica", 9))
					.action_({arg sl; 
						globalvol = sl.value;
						params[9] = sl.value;
					});
						
		outbusPoP = SCPopUpMenu(win, Rect(10, 448, 50, 16))			.font_(Font("Helvetica", 9))
			.items_(XiiACDropDownChannels.getStereoChnList)
			.value_(0)
			.background_(Color.white)
			.action_({ arg ch;
				outbus = ch.value * 2;
				params[10] = ch.value;
			});

		startButt = SCButton(win, Rect(65, 447, 45, 18))
			.states_([["start", Color.black, Color.clear],
					["stop", Color.black, Color.green(alpha:0.2)]])
			.font_(Font("Helvetica", 9))			
			.action_({arg butt;
				if(butt.value == 1, {
					actorTaskArray.do({arg task, i; 
						if(actorOnOffStateArray[i] == 1, {
							task.start;
						});
					});
					playFlag = true;
				},{
					actorTaskArray.do({arg task; task.stop});
					playFlag = false;
				});
			});
		
		actorTaskArray = Array.fill(4, {arg i;
			Task({ var xl, yl, oldstate, oldcolor;
				xl = (resolution-2).rand;
				yl = (resolution-2).rand;
				oldstate = if(grid.getState(xl, yl) == 1, {1},{0});
				oldcolor = Color.new255(103, 148, 103);
				{
				grid.setFillColor_(Color.new255(103, 148, 103));
				grid.setNodeShape_("square");
				}.defer;
			
				inf.do({
					{
					// --------resetting to square (and on/off state) --------
					grid.setNodeShape_(xl, yl, "square");
					grid.setNodeColor_(xl, yl, Color.new255(103, 148, 103)); // was oldcolor
					grid.setState_(xl, yl, oldstate);
					// --------find new loc --------
					[{ xl = xl + stepSizeArray[i]}, { xl = xl - stepSizeArray[i]}, 
					{yl = yl + stepSizeArray[i]}, {yl = yl - stepSizeArray[i]}].choose.value;
					if(xl < 0, {xl = 1}); if(yl < 0, {yl = 1});
					if(xl > (resolution-1), {xl = resolution-2});
					if(yl > (resolution-1), {yl = resolution-2});
					// ------------------------------
					oldstate = if(breedFlag, {grid.getState(xl, yl)}, {grid.getRealState(xl, yl)});
					
					if(oldstate == 1, {
						freq = tuningArray[yl][xl];
						pitchratio = pitchRatioArray[yl][xl];
						playFunc.value([xl, yl]); // XXX
					});
					oldcolor = grid.getNodeColor(xl, yl);
					// --------setting to circle (and on/off state) --------
					grid.setNodeShape_(xl, yl, "circle");
					grid.setNodeColor_(xl, yl, actorColors[i]);
					grid.setState_(xl, yl, 1);
					}.defer;
					timerArray[i].wait;
				})
			});
		});
		
		createGrids = {arg resolution;
			grid = Grid.new(win,
				Rect(120, 10, 460, 460), resolution, resolution, border:true)
				.setBackgrColor_(Color.white)
				.setBorder_(true)
				.setNodeShape_("square")
				.setNodeSize_(((200+(resolution*2))/resolution).round(2))
				.setFillMode_(true)
				.setTrailDrag_(true, true)
				.setFillColor_(Color.new255(103, 148, 103))
				.nodeDownAction_({arg nodeloc; var color, note, microtone;
					selectedNode = [nodeloc[0], nodeloc[1]];
					selectedNodeField.string_(selectedNode.asString);
					freq = tuningArray[nodeloc[1]][nodeloc[0]];
					pitchratio = pitchRatioArray[nodeloc[1]][nodeloc[0]]; // for audiostream pitch manipulation
					
					sample = gridNodeSampleIndexArray[nodeloc[1]][nodeloc[0]];
					// 3 interactive modes (play and draw (default), play all, play scales)
					if(playMode == true, {
						if(playScaleOnlyMode == true, {
							if(grid.getState(nodeloc[0], nodeloc[1]) == 0, {
								playFunc.value(nodeloc); // PLAY THE SELECTED SYNTH/SOUND
								grid.setState_(nodeloc[0], nodeloc[1], 1);
							}, {
								grid.setState_(nodeloc[0], nodeloc[1], 0); // play nothing
							});
						},{
							if(grid.getState(nodeloc[0], nodeloc[1]) == 0, {
								playFunc.value(nodeloc); // PLAY THE SELECTED SYNTH/SOUND
								grid.setState_(nodeloc[0], nodeloc[1], 1);
							}, {
								playFunc.value(nodeloc); // PLAY THE SELECTED SYNTH/SOUND
								grid.setState_(nodeloc[0], nodeloc[1], 0); // play nothing
							});
						});
					},{
						playFunc.value(nodeloc); // PLAY THE SELECTED SYNTH/SOUND
					});
					bufferPop.value_(sample);
					if(keyboardVisible, {
						microtone = false;
						note = freq.cpsmidi;
						midiKeyboard.clear;
						if(note.midiIsMicroTone, {
							microtone = true;
							color = Color.red;
						}, {
							color = midiKeyboard.getType(note)
						});
						if(freq < 8000, { // max freq of keyboard
							midiKeyboard.keyDown(note);
							midiKeyboard.setColor(note, color);
							freqField.string_("freq : "+freq.round(0.0001).asString);
							if(microtone, {
								noteField.string_("note : ~"+note.midinote.asString);
							},{
								noteField.string_("note : "+note.midinote.asString);
							});
						});
					});
				})
				.nodeTrackAction_({arg nodeloc; var color, note, microtone;
					freq = tuningArray[nodeloc[1]][nodeloc[0]];
					pitchratio = pitchRatioArray[nodeloc[1]][nodeloc[0]];
					if(playMode == true, {
						if(playScaleOnlyMode == true, {
							if(grid.getState(nodeloc[0], nodeloc[1]) == 0, {
								playFunc.value(nodeloc); // PLAY THE SELECTED SYNTH/SOUND
								grid.setState_(nodeloc[0], nodeloc[1], 1);
							}, {
								grid.setState_(nodeloc[0], nodeloc[1], 0); // play nothing
							});
						},{
							if(grid.getState(nodeloc[0], nodeloc[1]) == 0, {
								playFunc.value(nodeloc); // PLAY THE SELECTED SYNTH/SOUND
								grid.setState_(nodeloc[0], nodeloc[1], 1);
							}, {
								playFunc.value(nodeloc); // PLAY THE SELECTED SYNTH/SOUND
								grid.setState_(nodeloc[0], nodeloc[1], 0); // play nothing
							});
						});
					},{
						playFunc.value(nodeloc); // PLAY THE SELECTED SYNTH/SOUND
					});

					if(keyboardVisible, {
						microtone = false;
						note = freq.cpsmidi;
						midiKeyboard.clear;
						if(note.midiIsMicroTone, {
							microtone = true;
							color = Color.red;
						}, {
							color = midiKeyboard.getType(note)
						});
						if(freq < 8000, { // max freq of keyboard
							midiKeyboard.keyDown(note);
							midiKeyboard.setColor(note, color);
							freqField.string_("freq : "+freq.round(0.0001).asString);
							if(microtone, {
								noteField.string_("note : ~"+note.midinote.asString);
							},{
								noteField.string_("note : "+note.midinote.asString);
							});
						});
					});

				})
				.nodeUpAction_({arg nodeloc; 
					if(keyboardVisible, {
						midiKeyboard.clear;
					});
				}); 
									
			xgrid = Grid.new(win, Rect(120, 476, 460, 14), resolution, 1, false)
				.setNodeShape_("square")
				.setNodeSize_(8)
				.setFillMode_(true)
				.setFillColor_(Color.new255(103, 148, 103))
				.setTrailDrag_(true, true)
				.nodeDownAction_({arg nodeloc; var freq, state;
					state = xgrid.getState(nodeloc[0], nodeloc[1]);
					resolution.do({arg i;
						grid.setState_(nodeloc[0], i,  state);
						grid.setRealState_(nodeloc[0], i,  state);
					});
				})
				.nodeTrackAction_({arg nodeloc; var freq, state;
					state = xgrid.getState(nodeloc[0], nodeloc[1]);
					resolution.do({arg i;
						grid.setState_(nodeloc[0], i,  state);
						grid.setRealState_(nodeloc[0], i,  state);
					});
				});
	
			ygrid = Grid.new(win, Rect(590, 10, 14, 460), 1, resolution, false)
				.setNodeShape_("square")
				.setNodeSize_(8)
				.setFillMode_(true)
				.setFillColor_(Color.new255(103, 148, 103))
				.setTrailDrag_(true, true)
				.nodeDownAction_({arg nodeloc; var freq, state;
					state = ygrid.getState(nodeloc[0], nodeloc[1]);
					resolution.do({arg i;
						grid.setState_(i, nodeloc[1], state);
						grid.setRealState_(i, nodeloc[1], state);
					});
				})
				.nodeTrackAction_({arg nodeloc; var freq, state;
					state = ygrid.getState(nodeloc[0], nodeloc[1]);
					resolution.do({arg i;
						grid.setState_(i, nodeloc[1], state);
						grid.setRealState_(i, nodeloc[1], state);
					});
				});
		};
		createGrids.value(12);
		
		createCodeWin = {
			var funcwin, func, subm, test, view;
			funcwin = SCWindow("scode", Rect(600,700, 440, 200)).front;
			funcwin.alwaysOnTop = true;
			
			view = funcwin.view;
			func = SCTextView(view, Rect(20, 10, 400, 140))
					.font_(Font("Monaco", 9))
					.resize_(5)
					.focus(true)
					.string_(
						if(synthDefInUse.isNil, { 
							synthDefPrototype
						},{
							synthDefInUse
						});
					);
			test = SCButton(view, Rect(280,160,50,18))
					.states_([["test",Color.black,Color.clear]])
					.resize_(9)
					.font_(Font("Helvetica", 9))
					.action_({
						func.string.interpret.value;
					});
					
			subm = SCButton(view, Rect(340,160,50,18))
					.states_([["submit",Color.black,Color.clear]])
					.resize_(9)
					.font_(Font("Helvetica", 9))
					.action_({
						func.string.interpret;
						synthDefInUse = func.string;
						funcwin.close;
					});
		};
		
		createAudioStreamBusWin = {
			var win, envview, timesl, setButt;
			win = SCWindow("audiostream inbus", Rect(200, 450, 250, 100), resizable:false).front;
			win.alwaysOnTop = true;

			SCStaticText(win, Rect(20, 55, 20, 16))
				.font_(Font("Helvetica", 9)).string_("in"); 

			SCPopUpMenu(win, Rect(35, 55, 50, 16))
				.items_(XiiACDropDownChannels.getStereoChnList)
				.value_(10)
				.font_(Font("Helvetica", 9))
				.background_(Color.white)
				.canFocus_(false)
				.action_({ arg ch; var inbus;
					inbus = ch.value * 2;
				});

			setButt = SCButton.new(win, Rect(120, 55, 60, 16))
					.states_([["set inbus", Color.black, Color.clear]])
					.focus(true)
					.font_(Font("Helvetica", 9))
					.action_({
						win.close;
					});
		};

		win.view.keyDownAction_({arg this, char, modifiers, unicode; 
			if(char.asString == "s", {
				playScaleOnlyMode = playScaleOnlyMode.not;
				playScaleOnlyModeButt.switchState;
			});
			if(char.asString == "p", {
				playMode = playMode.not;
				playModeButt.switchState;
			});
		});

		cmdPeriodFunc = { startButt.valueAction_(0)};
		CmdPeriod.add(cmdPeriodFunc);

		win.onClose_({
			var t;
			CmdPeriod.remove(cmdPeriodFunc);
			~globalWidgetList.do({arg widget, i; if(widget === this, {t = i})});
			try{~globalWidgetList.removeAt(t)};
			scaleDict.writeArchive("gridderScales.ixi"); 
		});
		
		//setting
		soundFuncPop.valueAction_(params[0]);
		scalePop.valueAction_(params[1]);
		resolutionSlider.valueAction_(params[2]);
		maxFreqSl.valueAction_(params[3]);
		transposeSl.valueAction_(params[4]);
		actorTempoSl.valueAction_(params[5]);
		actorStepSizeSl.valueAction_(params[6]);
		playModeButt.valueAction_(params[7]);
		playScaleOnlyModeButt.valueAction_(params[8]);
		volumeSlider.valueAction_(params[9]);
		outbusPoP.valueAction_(params[10]);	

	}

	makeTuning {arg resolution=12, transposition=1, maxFreq=4000;
				
		var freq, fundamental, l, findiiBase, iiBase, iiCounter;
		fundamental = 65.40639132515; // C
		iiBase = 0;
		iiCounter = 0;
		findiiBase = block{|break |
						resolution.do( {arg i; var freq;  // the octave
							freq = fundamental*2.pow(i/transposition);
							if(freq > maxFreq, {iiBase = i; break.value;});
						});
					};
		findiiBase.value;
		freq = 0;
		l = Array.fill(resolution, {arg i;   // the octave
				if(freq > maxFreq, {iiCounter = iiCounter + 1;}); // max fundamental freq
				Array.fill(resolution, {arg j;		   // the notes
			freq = fundamental*2.pow((i-(iiBase*iiCounter))/transposition)*2.pow(j/resolution);
				});
			});
		^l;
	}

	makeRatioTuning {arg resolution=12, transposition=1, maxFreq=5;
		var freq, fundamental, l, findiiBase, iiBase, iiCounter;
		fundamental = 0.25; // C
		iiBase = 0;
		iiCounter = 0;
		findiiBase = block{|break |
						resolution.do( {arg i; var freq;  // the octave
							freq = fundamental*2.pow(i/transposition);
							if(freq > maxFreq, {iiBase = i; break.value;});
						});
					};
		findiiBase.value;
		freq = 0;
		l = Array.fill(resolution, {arg i;   // the octave
				if(freq > maxFreq, {iiCounter = iiCounter + 1;}); // max fundamental freq
				Array.fill(resolution, {arg j;		   // the notes
			freq = fundamental*2.pow((i-(iiBase*iiCounter))/transposition)*2.pow(j/resolution);
				});
			});
		^l;
	}

	updatePoolMenu {
		var pool, poolindex;
		pool = selbPool.items.at(selbPool.value);        // get the pool name (string)
		selbPool.items_(~globalBufferDict.keys.asArray); // put new list of pools
		poolindex = selbPool.items.indexOf(pool);        // find the index of old pool in new array
		if(poolindex != nil, {
			selbPool.value_(poolindex); // so nothing changed, but new poolarray
		});
	}

	setPlayFunc_ {arg funcnr=0;
		playFunc = switch (funcnr,
			0, { {arg nodeloc; var myBuffer, selStart, selEnd, sample; // the sample player

				sample = gridNodeSampleIndexArray[nodeloc[1]][nodeloc[0]];
				if(try{~globalBufferDict.at(poolName)[0][sample]} != nil, {
					myBuffer = ~globalBufferDict.at(poolName)[0][sample];
					selStart = ~globalBufferDict.at(poolName)[1][sample][0];
					selEnd = selStart + ~globalBufferDict.at(poolName)[1][sample][1]-1;

					if(myBuffer.numChannels == 1, {
						Synth(\xiiPrey1x2, [	\outbus, outbus,
											\bufnum, myBuffer.bufnum, 
											\startPos, selStart, 
											\endPos, selEnd,
											\vol, globalvol
						])
					},{
						Synth(\xiiPrey2x2, [	\outbus, outbus,
											\bufnum, myBuffer.bufnum, 
											\startPos, selStart, 
											\endPos, selEnd,
											\vol, globalvol
						])
					});
				});
			} },
			1, { {
					Synth(\xiiSine, [		\outbus, outbus,
										\freq, freq,
										\phase, 1.0.rand,
										\amp, globalvol
					])
			} },
			2, { {
					Synth(\xiiBells, [		\outbus, outbus,
										\freq, freq,
										\amp, globalvol
					])
			} },
			3, { {
					Synth(\xiiSines, [		\outbus, outbus,
										\freq, freq,
										\amp, globalvol
					])
			} },
			4, { {
					Synth(\xiiSynth1, [	\outbus, outbus,
										\freq, freq,
										\amp, globalvol
					])
			} },
			5, { {
					Synth(\xiiKs_string, [	\outbus, outbus,
										\note, freq, 
										\pan, 0.7.rand2, 
										\rand, 0.1+0.1.rand, 
										\delayTime, 2+1.0.rand,
										\amp, globalvol
										]);
			} },
			6, { {
					Synth(\xiiString, [	\outbus, outbus,
										\freq, freq, 
										\pan, 0.7.rand2, 
										\amp, globalvol
										]);
			} },
			7, { {
					Synth(\xiiImpulse, [	\outbus, outbus,
										\pan, 0.7.rand2,
										\amp, globalvol
										]);
			} },
			8, { {
					Synth(\xiiRingz, [		\outbus, outbus,
										\freq, freq, 
										\pan, 0.7.rand2,
										\amp, globalvol
										]);
			} },
			9, { {
					Synth(\xiiKlanks, [	\outbus, outbus,
										\freq, freq, 
										\pan, 0.7.rand2,
										\amp, globalvol
										]);
			} },
			10, { {
					Synth(\xiiGridder, [	\outbus, outbus,
										\freq, freq, 
										\pan, 0.7.rand2,
										\amp, globalvol
										]);
			} },
			11, { { // the audio stream
					Synth(\xiiAudioStream,[	\outbus, outbus,
										\inbus, inbus,
										\pitchratio, pitchratio, 
										//\pan, 0.7.rand2,
										\amp, globalvol
										]);
			} }
			)
	}

	loadScales { // some jmc scales
		^[
		// 5 note scales
			["minorPentatonic", [0,3,5,7,10]],
			["majorPentatonic", [0,2,4,7,9]],
			["ritusen", [0,2,5,7,9]], // another mode of major pentatonic
			["egyptian", [0,2,5,7,10]], // another mode of major pentatonic
			
			["kumoi", [0,2,3,7,9]],
			["hirajoshi", [0,2,3,7,8]],
			["iwato", [0,1,5,6,10]], // mode of hirajoshi
			["chinese", [0,4,6,7,11]], // mode of hirajoshi
			["indian", [0,4,5,7,10]],
			["pelog", [0,1,3,7,8]],
			
			["prometheus", [0,2,4,6,11]],
			["scriabin", [0,1,4,7,9]],
			
		// 6 note scales
			["whole", (0,2..10)],
			["augmented", [0,3,4,7,8,11]],
			["augmented2", [0,1,4,5,8,9]],
			
			// hexatonic modes with no tritone
			["hexMajor7", [0,2,4,7,9,11]],
			["hexDorian", [0,2,3,5,7,10]],
			["hexPhrygian", [0,1,3,5,8,10]],
			["hexSus", [0,2,5,7,9,10]],
			["hexMajor6", [0,2,4,5,7,9]],
			["hexAeolian", [0,3,5,7,8,10]],
			
		// 7 note scales
			["ionian", [0,2,4,5,7,9,11]],
			["dorian", [0,2,3,5,7,9,10]],
			["phrygian", [0,1,3,5,7,8,10]],
			["lydian", [0,2,4,6,7,9,11]],
			["mixolydian", [0,2,4,5,7,9,10]],
			["aeolian", [0,2,3,5,7,8,10]],
			["locrian", [0,1,3,5,6,8,10]],
			
			["harmonicMinor", [0,2,3,5,7,8,11]],
			["harmonicMajor", [0,2,4,5,7,8,11]],
			
			["melodicMinor", [0,2,3,5,7,9,11]],
			["bartok", [0,2,4,5,7,8,10]], // jazzers call this the hindu scale
			
			// raga modes
			["todi", [0,1,3,6,7,8,11]], // maqam ahar kurd
			["purvi", [0,1,4,6,7,8,11]],
			["marva", [0,1,4,6,7,9,11]],
			["bhairav", [0,1,4,5,7,8,11]],
			["ahirbhairav", [0,1,4,5,7,9,10]],
			
			["superLocrian", [0,1,3,4,6,8,10]],
			["romanianMinor", [0,2,3,6,7,9,10]], // maqam nakriz
			["hungarianMinor", [0,2,3,6,7,8,11]],	
			["neapolitanMinor", [0,1,3,5,7,8,11]],
			["enigmatic", [0,1,4,6,8,10,11]],
			["spanish", [0,1,4,5,7,8,10]],
			
			// modes of whole tones with added note:
			["leadingWhole", [0,2,4,6,8,10,11]],
			["lydianMinor", [0,2,4,6,7,8,10]],
			["neapolitanMajor", [0,1,3,5,7,9,11]],
			["locrianMajor", [0,2,4,5,6,8,10]],
			
		// 8 note scales
			["diminished", [0,1,3,4,6,7,9,10]],
			["diminished2", [0,2,3,5,6,8,9,11]],
			
		// 12 note scales
			["chromatic", (0..11)]
		]
	}
	
	getState { // for save settings
		var point;
		point = Point(win.bounds.left, win.bounds.top);
		^[2, point, params];
	}

}