

XiiBufferPool {	

		var <>gui;

		var bufferList, bufferListNames, bufferListSelections;
	
		var s, window, <name, point;
		var recordingName, recButton, r, filename, timeText, secTask, inbus, numChannels;
		var stereoButt, monoButt, preRecButt;
		var <bufferPoolNum;
		var soundFileWindowsList, ram, ramview, fileramview, sendBufferPoolToWidgets;
		var cmdPeriodFunc;
		var txtv, ram;
		
	*new { arg server, poolname;
		^super.new.initXiiBufferPool(server, poolname);
		}
		
	initXiiBufferPool {arg server, poolname;
		var nameView, folderButt, fileButt, viewButt, saveButt;

		var bgColor, foreColor, spec, outbus;
		var refreshButton, playButton, r, filename, timeText, secTask;
		var soundfile, player, buffer, task, volSlider, openButton, volume;
		var folderSounds, addedSoundNames, sounds, soundNames;
		var soundFileWindowsList, stereomonoview;
		
		s = server;
		
		soundFileWindowsList = List.new;
		
		~bufferPoolNum = ~bufferPoolNum + 1; // the number of the pool
		bufferPoolNum = ~bufferPoolNum;  // make it a local var (it might have increased)
		
		bufferList = List.new;
		bufferListSelections = List.new; // this is for the selections of each buffer
		bufferListNames = []; // Cocoa dialog will fill this.

		name = if(poolname==2, {("bufferPool"+(~bufferPoolNum+1).asString)}, {poolname});
		filename = "";
		inbus = 0;
		numChannels = 2;
		ram = 0;

		point = XiiWindowLocation.new(name);

		bgColor = Color.new255(155, 205, 155);
		foreColor = Color.new255(103, 148, 103);
		outbus = 0;
		
		window = SCWindow.new(name, Rect(point.x, point.y, 220, 195), resizable:false).front;

		SCStaticText(window, Rect(10, 0, 40, 12))
			.font_(Font("Helvetica", 9))
			.string_("ram use:");

		ramview = SCStaticText(window, Rect(50, 0, 28, 12))
			.font_(Font("Helvetica", 9))
			.string_("0");

		SCStaticText(window, Rect(90, 0, 40, 12))
			.font_(Font("Helvetica", 9))
			.string_("file size:");

		fileramview = SCStaticText(window, Rect(130, 0, 28, 12))
			.font_(Font("Helvetica", 9))
			.string_("0");

		stereomonoview = SCStaticText(window, Rect(180, 0, 40, 12))
			.font_(Font("Helvetica", 9))
			.string_("");

		txtv = SCListView(window, Rect(10,15, 200, 145))
			.items_(bufferListNames)
			.background_(Color.new255(155, 205, 155, 60))
			.hiliteColor_(Color.new255(103, 148, 103))
			.selectedStringColor_(Color.black)
			.enterKeyAction_({|sbs|
				if(txtv.items.size > 0, {
					soundFileWindowsList.add(
						XiiSoundFileView.new(
							bufferList[sbs.value].path, 
							bufferList[sbs.value].bufnum,
							sbs.value, name);
					);
				});
			})
			.action_({ arg sbs; var f, filesize;
				if(bufferListNames.size>0, {
					f = SoundFile.new;
					f.openRead(bufferList[sbs.value].path);
					filesize = f.numFrames * f.numChannels * 2 * 2;
					stereomonoview.string_(if(f.numChannels == 1, {"mono"},{"stereo"}));
					fileramview.string_((filesize/1000/1000).round(0.01));
					f.close;
				});
			});

		saveButt = SCButton(window, Rect(31, 167, 40, 18))
			.states_([["save",Color.black, Color.clear]])
			.font_(Font("Helvetica", 9))			
			.canFocus_(false)
			.action_({ 
				~globalWidgetList.add(
					XiiPoolManager.new(Server.default, nil,
					Rect(window.bounds.left+window.bounds.width+10, window.bounds.top, 160, 80), 
					this);
				);
			});	

		fileButt = SCButton(window, Rect(74, 167, 40, 18))
			.states_([["free",Color.black, Color.clear]])
			.font_(Font("Helvetica", 9))			
			.canFocus_(false)
			.action_({ arg butt; var a, chNum, dur, filepath, pFunc;
				soundFileWindowsList.do(_.close);
				bufferList.do(_.free);
				bufferList = List.new;
				ram = 0;
				bufferListSelections = List.new;
				bufferListNames = [];
				txtv.items_(bufferListNames);
				~globalBufferDict.add(name.asSymbol -> 0);
			});	
				
		folderButt = SCButton(window, Rect(117, 167, 55, 18))
			.states_([["add file(s)",Color.black, Color.clear]])
			.font_(Font("Helvetica", 9))
			.focus(true)			
			.action_({ arg butt;
				CocoaDialog.getPaths({arg paths;
					this.loadBuffers(paths);
				});
			});	
			
		viewButt = SCButton(window, Rect(175, 167, 34, 18))
			.states_([["view",Color.black, Color.clear]])
			.font_(Font("Helvetica", 9))		
			.canFocus_(false)
			.action_({ arg butt;
				if(txtv.items.size > 0, {
					soundFileWindowsList.add(
						XiiSoundFileView.new(
								bufferList[txtv.value].path, 
								bufferList[txtv.value].bufnum,
								txtv.value,  // the number of the buffer in the list
								name
								);
						);
				});
			});	

		preRecButt = SCButton(window, Rect(10, 167, 18, 18))
			.states_([["R",Color.black, Color.clear], ["r",Color.black, Color.clear]])
			.font_(Font("Helvetica", 9))
			.canFocus_(false)
			.action_({ arg butt;
				if(butt.value == 1, {
					window.bounds_(Rect(window.bounds.left, window.bounds.top-90, 222, 285));
				}, {
					window.bounds_(Rect(window.bounds.left, window.bounds.top+90, 222, 195));
				});	
			});	

		stereoButt = OSCIIRadioButton(window, Rect(10,205,14,14), "stereo")
						.value_(1)
						.font_(Font("Helvetica", 9))
						.action_({ arg butt;
								if(butt.value == 1, {
								numChannels = 2;
								monoButt.value_(0);
								});
						});

		monoButt = OSCIIRadioButton(window, Rect(100,205,14,14), "mono")
						.value_(0)
						.font_(Font("Helvetica", 9))
						.action_({ arg butt;
								if(butt.value == 1, {
								numChannels = 1;
								stereoButt.value_(0);
								});
						});

		recordingName = SCTextView(window, Rect(10, 225, 160, 16))
				.hasVerticalScroller_(false)
				.autohidesScrollers_(true)
				.string_(filename);

		recButton = SCButton(window, Rect(114, 250, 56, 16))
			.states_([["Record",Color.black, Color.clear], ["Stop",Color.red,Color.red(alpha:0.2)]])
			.font_(Font("Helvetica", 9))
			.canFocus_(false)
			.action_({ arg butt; var file, f, filesize, buffer, loadBufTask;
				if(s.serverRunning == true, { // if the server is running
					if(butt.value == 1, {
						filename = recordingName.string;
						if(filename == "", {filename = Date.getDate.stamp.asString});
						recordingName.string_(filename);
						r = Record(s, inbus, numChannels);
						r.start("sounds/ixiquarks/"++filename++".aif");
						secTask.start;
					}, {
						r.stop;
						secTask.stop;
						file = "sounds/ixiquarks/"++filename++".aif";
						buffer = Buffer.read(s, file);
						bufferList.add(buffer);
						bufferListNames = bufferListNames.add(file.basename);
						txtv.items_(bufferListNames);

						~globalBufferDict.add(name.asSymbol -> [bufferList, bufferListSelections]);
						// when the buffer is loaded... send it to widgets
						if(s.serverRunning, {
						loadBufTask = Task({
							inf.do({ arg i;
							if(bufferList[bufferList.size-1].numChannels != nil, {
								// get soundfile frames when loaded into buffer
								f = SoundFile.new;
								f.openRead(file);									filesize = f.numFrames * f.numChannels * 2 * 2;
								ram = ram + (filesize/1000/1000).round(0.01);
								{ramview.string_(ram.asString)}.defer;
								bufferListSelections.add([0, f.numFrames-1]);								f.close;

								{sendBufferPoolToWidgets.value}.defer;
								loadBufTask.stop;
							});
							0.1.wait; 
							});
						}).start;
						});
					});
				}, {
					"ixi alert: you need to start the server in order to record".warn;
					recButton.value_(0);
				});
			});	
		
		timeText = SCStaticText(window, Rect(70, 250, 40, 16))
					.string_("00:00");

		// record busses
		SCPopUpMenu(window, Rect(10, 250, 44, 16))
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
		
		sendBufferPoolToWidgets = {
			" - > sending pool buffers to all active quarks < -".postln;
			~globalWidgetList.do({arg widget;
				{ // the various widgets that receive and use bufferpools
				if(widget.isKindOf(XiiBufferPlayer), {widget.updatePoolMenu;});
				if(widget.isKindOf(XiiGrainBox), {widget.updatePoolMenu;});
				if(widget.isKindOf(XiiPredators), {widget.updatePoolMenu;});
				if(widget.isKindOf(XiiPolyMachine), {widget.updatePoolMenu;});
				if(widget.isKindOf(XiiGridder), {widget.updatePoolMenu;});
				if(widget.isKindOf(XiiSoundScratcher), {widget.updatePoolMenu;});
				}.defer;
			});
		};
			
		cmdPeriodFunc = { recButton.valueAction_(0);};
		CmdPeriod.add(cmdPeriodFunc);

		window.onClose_({
			var t;
			recButton.valueAction_(0); // stop recording
			CmdPeriod.remove(cmdPeriodFunc);
			soundFileWindowsList.do(_.close);
			bufferList.do(_.free);
			
			~globalBufferDict.removeAt(name.asSymbol);
			sendBufferPoolToWidgets.value;
			
			~globalWidgetList.do({arg widget, i; if(widget === this, { t = i})});
			~globalWidgetList.removeAt(t);
			// write window position to archive.sctxar
			point = Point(window.bounds.left, window.bounds.top);
			XiiWindowLocation.storeLoc(name, point);
		});
	}
	
	loadBuffers {arg paths, selections; var f, filesize, buffer, loadBufTask;
		paths.do({ arg file;
			f = SoundFile.new;
			f.openRead(file);
			buffer = Buffer.read(s, file);
			bufferList.add(buffer);
			if(selections.isNil, {bufferListSelections.add([0, f.numFrames-1])});
			bufferListNames = bufferListNames.add(file.basename);
			filesize = f.numFrames * f.numChannels * 2 * 2;
			ram = ram + (filesize/1000/1000).round(0.01);
			f.close;
		});
		txtv.items_(bufferListNames);
		txtv.focus(true);
		
		if(selections.isNil.not, {bufferListSelections = selections});
		
		~globalBufferDict.add(name.asSymbol -> [bufferList, bufferListSelections]);
		ramview.string_(ram.asString);
		// update the pools list in active widgets
		
		if(s.serverRunning, {
			loadBufTask = Task({
				inf.do({arg i;
				if(bufferList[bufferList.size-1].numChannels != nil, {
					sendBufferPoolToWidgets.value;
					loadBufTask.stop;
				});
				"loading buffers ->  ".post; (i*100).post; " milliseconds".postln;
				0.1.wait; 
				});
			}).start;
		});

	}
	
	getFilePaths {
		var pathList;
		pathList = bufferList.collect({arg buffer; buffer.path});
		^pathList;
	}

	setName_ {arg name;
		name = name;
		window.name_(name);
	}
}

