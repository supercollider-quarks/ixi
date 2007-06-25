XiiSoundFileView {	

	var window, sndfileview, soundfile;
	var point, fileNumInPool;
	var bgColor, foreColor;
	var chNum, synth, loop, loopButt, playButt,setSelButt, deSelButt, resolutionSlider, zoomSlider, volSlider;
	var synthRunFlag, timeCursorTask, resetclock; //, stopclock;
	var bufferPoolNum, vol;
	var start, end, seltime, logo;
	
	*new { arg path, bufnum, num=0, name;
		^super.new.initXiiSoundFileView(path, bufnum, num, name);
		}
		
	initXiiSoundFileView {arg path, bufnum, numInPool, name;
		[\NAME, name].postln;
		logo = [
Point(1,7), Point(8, 1), Point(15,1), Point(15,33),Point(24, 23), Point(15,14), Point(15,1), 
Point(23,1),Point(34,13), Point(45,1), Point(61,1), Point(66,6), Point(66,37), Point(59,43),
Point(53,43), Point(53,12), Point(44,22), Point(53,33), Point(53,43), Point(42,43), Point(34,32),
Point(24,43), Point(7,43), Point(1,36), Point(1,8)
];

		SynthDef(\playBufXSndFileView1x1, { arg out=0, bufnum=0, start=0, end= -1, loop=0, vol = 1;
			var z;
			z = Pan2.ar(LoopBuf.ar(1, bufnum, 1, 1, start, end, loop), 0.0) * vol;
			DetectSilence.ar(z, doneAction:2);
			Out.ar(out, z);
		}).load(Server.default);
		
		SynthDef(\playBufXSndFileView2x2, { arg out=0, bufnum=0, start=0, end= -1, loop=0, vol = 1;
			var z;
			z = LoopBuf.ar(2, bufnum, 1, 1, start, end, loop) * vol;
			DetectSilence.ar(z, doneAction:2);
			Out.ar(out, z)
		}).load(Server.default);

		window = SCWindow.new(path.basename+"  -  bufferPool: "+name, 
							Rect(200, 600, 800, 232), 
							resizable:false).front;	
		window.drawHook = {
			Color.new255(255, 100, 0).set;
			Pen.width = 3;
			Pen.translate(27,202);
			Pen.scale(0.35,0.35);
			Pen.moveTo(1@7);
			logo.do({arg point;
				Pen.lineTo(point+0.5);
			});
			Pen.stroke
		};

		bgColor = Color.new255(155, 205, 155);
		foreColor = Color.new255(103, 148, 103);
		loop = 0;
		synthRunFlag = false;
		fileNumInPool = numInPool;
		
		soundfile = SoundFile.new;
		soundfile.openRead(path);
		chNum = soundfile.numChannels;
		vol = 1;

		sndfileview = SCSoundFileView.new(window, Rect(10, 0, 780, 190))
			.soundfile_(soundfile)
			.read(0, soundfile.numFrames)
			.elasticMode_(true)
			.timeCursorOn_(true)
			.timeCursorColor_(Color.white)
			.timeCursorPosition_(0)
			.drawsWaveForm_(true)
			.gridOn_(true)
			.gridResolution_(1)

			.gridColor_(Color.white)
			.waveColors_([ foreColor, foreColor ])
			.background_(bgColor)
			.canFocus_(false)
			.setSelectionColor(0, Color.new255(105, 185, 125))
			.mouseUpAction_({
				if(synthRunFlag == true, { // IT'S LOOPING
					start = sndfileview.selections[0][0]; // the start
					end = start + sndfileview.selections[0][1]; // end of the selection
					if(sndfileview.selectionSize(0) < 10, {
					 	end = soundfile.numFrames-1;
						seltime = (soundfile.numFrames-start)/44100;
					}, { 
						seltime = sndfileview.selections[0][1]/44100;
					});

					synth.free;
					if(chNum == 1, {
						synth = Synth(\playBufXSndFileView1x1, 
									[\bufnum, bufnum, \start, start, \end, end, \loop, loop]);
					}, {
						synth = Synth(\playBufXSndFileView2x2, 
									[\bufnum, bufnum, \start, start, \end, end, \loop, loop]);
					});
					this.startTimeCursor;
				});
			});

		zoomSlider = SC2DSlider(window, Rect(78,193,176, 35)) // scroll w/zoom x - felix!
					.canFocus_(false)
					.action_({|sl| 
						var y;
						sndfileview.zoomToFrac(y = sl.y * 0.95 + 0.05);
						sndfileview.scrollTo(sl.x);
					})
					.y_(1).x_(0.5);

		resolutionSlider = OSCIISlider(window, Rect(268,196,100,8), "resolution", 0.1, 10, 1, 0.1)
					.canFocus_(false)
					.font_(Font("Helvetica", 9))
					.action_({arg slider; 
						sndfileview.gridResolution_(slider.value);
					});
		
		volSlider = OSCIISlider(window, Rect(378, 196, 100, 8), "vol", 0, 1, 1, 0.01)
					.canFocus_(false)
					.font_(Font("Helvetica", 9))
					.action_({arg slider; 
						vol = slider.value;
						if(synthRunFlag == true, {
							synth.set(\vol, vol);
						});
					});

		deSelButt = SCButton.new(window, Rect(520, 195, 70, 18))
			.states_([["deselect",Color.black,Color.clear]])
			.canFocus_(false)
			.font_(Font("Helvetica", 9))
			.action_({
				sndfileview.setSelectionStart(0, 0);
				sndfileview.setSelectionSize(0, 0);
				sndfileview.timeCursorPosition_(0);
				~globalBufferDict.at(name.asSymbol)[1][fileNumInPool] = [0, soundfile.numFrames];
			});
					
		setSelButt = SCButton.new(window, Rect(595, 195, 80, 18))
			.states_([["set selection",Color.black,Color.clear]])
			.canFocus_(false)
			.font_(Font("Helvetica", 9))
			.action_({arg butt; var start, end, seltime;
				start = sndfileview.selections[0][0]; // the start
				end = start + sndfileview.selections[0][1]; // end of the selection
				if( end == 0, { end = soundfile.numFrames; });
				if( start < 0, { start = 0 });
				if( end > soundfile.numFrames, { end = soundfile.numFrames - 10 });
				~globalBufferDict.at(name.asSymbol)[1][fileNumInPool] = [start, sndfileview.selections[0][1]];
			});
		
		loopButt = OSCIIRadioButton(window, Rect(686,197,14,14), "loop")
			.font_(Font("Helvetica", 9))
			.action_({arg val; 
				if(val==1, { loop = 1 }, { loop = 0 });
			});

		playButt = SCButton.new(window, Rect(740, 195, 43, 18))
			.states_([["play",Color.black,Color.clear], ["stop",Color.black,Color.clear]])
			.font_(Font("Helvetica", 9))
			.action_({arg butt;
				start = sndfileview.selections[0][0]; // the start
				end = start + sndfileview.selectionSize(0); // end of the selection
				if(sndfileview.selectionSize(0) < 10, {
				 	end = soundfile.numFrames-1;
					seltime = (soundfile.numFrames-start)/44100;
				}, { 
					seltime = sndfileview.selections[0][1]/44100;
				});
 				if(butt.value == 1, {
 					synthRunFlag = true;
					if(chNum == 1, {
						synth = Synth(\playBufXSndFileView1x1, 
						[\bufnum, bufnum, \start, start, \end, end, \loop, loop, \vol, vol]);
					}, {
						synth = Synth(\playBufXSndFileView2x2, 
						[\bufnum, bufnum, \start, start, \end, end, \loop, loop, \vol, vol]);
					});
					this.startTimeCursor;
				}, {					
					timeCursorTask.stop;
					resetclock.clear;
					synth.free; 
					synthRunFlag=false;
				});
			})
			.focus(true);
			
		window.onClose_({ 
			// write window position to archive.sctxar
			this.close;
			point = Point(window.bounds.left, window.bounds.top);
			Archive.global.at(\win_position).put(name.asSymbol, point);
		}); 		
	}	
	
	startTimeCursor {
		resetclock.clear;
		timeCursorTask.stop;
		
		timeCursorTask = Task({ var pos;		
			pos = 0;
			if(loop == 0, { // not looping
				resetclock = SystemClock.sched(seltime, { 
							{playButt.value_(0)}.defer; 
							if(synthRunFlag, {synth.free}); 							synthRunFlag=false; 
							{sndfileview.timeCursorPosition_(start)}.defer;
							timeCursorTask.stop;
						nil});
				inf.do({
					{
					window.isClosed.not.if({ // if window is not closed, update...
						sndfileview.timeCursorPosition_(start+pos)
					})
					}.defer;
					pos = pos + 4410;
					0.1.wait;
				});
			}, {			// looping
				resetclock = SystemClock.sched(seltime, {
					{sndfileview.timeCursorPosition_(start)}.defer;
					timeCursorTask.reset;
					pos = 0;
					nil;
				});
				
				inf.do({
					pos = pos + 4410;
					{sndfileview.timeCursorPosition_(start+pos)}.defer;
					0.1.wait;
				});
			})
		}).start;
	}
	
	close {	
		timeCursorTask.stop;	
		resetclock.clear;
		if(synthRunFlag, {synth.free});
		window.close;
	}
}

