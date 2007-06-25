XiiScaleSynth {
	var <>gui;

	*new {arg server;
		^super.new.initXiiScaleSynth(server);
	}

	initXiiScaleSynth {arg argserver;
		
		var win, xTable, yTable, trns1, trns2, synth, drone, tablet;
		var screenX, screenY, tabletScreenY;
		var setScale, setTrans, scales, scaleNames, scaleMenu;
		var boxColList, makeColorList, server;
		var transBuf, scaleBuf, outbus, outbusPoP;
		
		server = argserver ? Server.default;
		// -------------------------- THESE PARAMETERS MIGHT HAVE TO BE ADJUSTED ----------------
		screenX = SCWindow.screenBounds.asArray[2]; 
		screenY = SCWindow.screenBounds.asArray[3];
		
		trns1 = 5; // first row up from base scale (which is in C)
		trns2 = 7; // secon row up from the base scale.
		
		tabletScreenY = screenY - 40; // give space for the control bar at the bottom
		
		// --------  GUI 
		win = SCWindow(" - ", border: false);
		win.fullScreen;
		
			boxColList = List.new;
			17.do({arg j; var a;
				var tempR, tempG, tempB;
				a = 3 - (j%3);	
				tempR = (10-j)/60;
				tempG = ((a+1)*0.8)/10;
				tempB = (10-j)/60;
				boxColList.add(List.new;);
				15.do({ arg i; var r, g, b;
					r = i/40 + tempR;
					g = i/20 + tempG;
					b = i/40 + tempB;
					boxColList[j].add(Color.new(r,g,b, 0.7));
					});
				});
			
			
		win.drawHook = { 
			17.do({arg j;
			15.do({ arg i;
				Pen.use{
					boxColList[j][i].set;
					Pen.fillRect(Rect(3+(i*screenX/15), 2+(j*tabletScreenY/17), (screenX/15)-6, (tabletScreenY/17)-6));
					Color.grey.set;
					Pen.strokeRect(Rect(3+(i*screenX/15), 2+(j*tabletScreenY/17), (screenX/15)-6, (tabletScreenY/17)-6));
					}
				});
			});
		};
		
		tablet = SCTabletView(win,Rect(0, 0, screenX, tabletScreenY));
		tablet.background = Color.white;
		tablet.mouseDownAction = { arg  view, x, y, pressure, tiltx, tilty, deviceID, buttonNumber;
			synth.set(\vol, pressure);
			17.do({arg j; // y axis
				15.do({ arg i; var r; // x axis
				r = Rect(3+(i*screenX/15), 2+(j*tabletScreenY/17), (screenX/15)-6, (tabletScreenY/17)-6);
				if((x >= r.left) && (x <= (r.left + r.width)) && (y >= r.top) && (y <= (r.top + r.height))) {
				}
				});
			});
		};
		tablet.action = { arg  view,x,y,pressure,tiltx,tilty,deviceID, buttonNumber;
			synth.set(\vol, pressure);			
			17.do({arg j; // y axis
				15.do({ arg i; var r; // x axis
					r = Rect(3+(i*screenX/15), 2+(j*tabletScreenY/17), (screenX/15)-6, (tabletScreenY/17)-6);
					if((x >= r.left) && (x <= (r.left + r.width)) && (y >= r.top) && (y <= (r.top + r.height))) {
						boxColList[j][i].red = boxColList[j][i].red+0.05;
						win.refresh;
					}
				});
			});
		};
		tablet.mouseUpAction = { arg  view,x,y,pressure;
			synth.set(\vol, 0);
		};
		
		SCButton(win, Rect(6,screenY-34,40,20)).states_([["stop"]]).action = {win.close; 
		scaleBuf.free; transBuf.free; synth.free; drone.free;};
		OSCIISlider.new(win, Rect(60, screenY-34, 100, 10), "- decay", 0.1, 10, 4, 0.01)
			.action_({arg sl; synth.set(\decayTime, sl.value);});
		OSCIISlider.new(win, Rect(170, screenY-34, 100, 10), "- modul", 0.1, 6, 1, 0.01)
			.action_({arg sl; synth.set(\modPartial, sl.value);});
		OSCIISlider.new(win, Rect(280, screenY-34, 100, 10), "- drone", 0, 1, 0.1, 0.01)
			.action_({arg sl; drone.set(\vol, sl.value);});
		OSCIISlider.new(win, Rect(390, screenY-34, 100, 10), "- freq", 200, 600, 261, 1)
			.action_({arg sl; drone.set(\freq, sl.value);});
		OSCIISlider.new(win, Rect(590, screenY-34, 60, 10), "trans1", 0, 12, trns1, 1)
			.action_({arg sl; trns1 = sl.value; setTrans.value(trns1, trns2); });
		OSCIISlider.new(win, Rect(660, screenY-34, 60, 10), "trans2", 0, 12, trns2, 1)
			.action_({arg sl; trns2 = sl.value; drone.value(trns1, trns2);});
		
		outbusPoP = SCPopUpMenu(win, Rect(750, screenY-34, 50, 16))			.font_(Font("Helvetica", 9))
			.items_(XiiACDropDownChannels.getStereoChnList)
			.value_(0)
			.background_(Color.white)
			.action_({ arg ch;
				outbus = ch.value * 2;
				synth.set(\out, outbus);
				drone.set(\out, outbus);
			});

		win.front;
		
		// --- scale stuff ---
			scaleMenu = SCListView(win, Rect(500, screenY-34, 80, 30));
			scaleNames = ['ionian','dorian', 'phrygian', 'lydian', 'mixolydian', 'aeolian', 'locrian', 'default', 'bartok', 'todi', 'purvi', 'marva', 'bhairav', 'ahirbhairav', 'spanish'];
			scales = Dictionary[	'ionian'		-> [0,2,4,5,7,9,11], 
								'dorian' 		-> [0,2,3,5,7,9,10],
								'phrygian' 	-> [0,1,3,5,7,8,10],
								'lydian' 		-> [0,2,4,6,7,9,11],
								'mixolydian'	-> [0,2,4,5,7,9,10],
								'aeolian'		-> [0,2,3,5,7,8,10],
								'locrian'		-> [0,1,3,5,6,8,10],
								'default'		-> [0,1,4,5,7,8,11],
								'bartok' 		-> [0,2,4,5,7,8,10],
								'todi'		-> [0,1,3,6,7,8,11],
								'purvi'		-> [0,1,4,6,7,8,11],
								'marva' 		-> [0,1,4,6,7,9,11],
								'bhairav'		-> [0,1,4,5,7,8,11],
								'ahirbhairav'	-> [0,1,4,5,7,9,10],
								'spanish'		-> [0,1,4,5,7,8,10]
							];
			scaleMenu.items = scaleNames;
			scaleMenu.action = { arg sbs;
				setScale.value(scales.at(scaleNames.at(sbs.value)));
			};
		
		setScale = {arg scale;
			xTable = scale ++ (scale+12) ++ 24 + 60 +36; // two octaves of the scale + C being the base + 3 octaves up
			scaleBuf = Buffer(server, xTable.size, 1); // the x scale stored in a Buffer
			server.listSendMsg( scaleBuf.allocMsg( scaleBuf.setnMsg(0, xTable) ) );
			synth.set(\buf1, scaleBuf.bufnum);
			};
		
		setTrans = {arg trns, trns2;
			yTable = [ 0,trns1,trns2, 12,12+trns1,12+trns2, 24,24+trns1,24+trns2, 
					36,36+trns1,36+trns2, 48,48+trns1,48+trns2, 60,60+trns1,60+trns2 ];
			transBuf = Buffer(server, yTable.size, 1); // the y scale stored in a Buffer
			server.listSendMsg( transBuf.allocMsg( transBuf.setnMsg(0, yTable) ) );
			synth.set(\buf2, transBuf.bufnum);
		
		};
		
		setScale.value(scales.at('default'));
		setTrans.value(4, 9);
		// -------------------

SynthDef(\scaleSynth,{ arg out=0, buf1=0, buf2 = 1, vol=0, volLag=0.1, carPartial = 1, modPartial = 1, index = 3, mul = 0.5, delayTime=0.04, decayTime=4;
	var xPitch, yPitch, oscillator;
	var modulator, carrier, freq; // FM variables
	var signal; 
	
	xPitch = Index.kr(buf1, MouseX.kr(0, xTable.size));
	yPitch = Index.kr(buf2, MouseY.kr(0, yTable.size));
	freq = (xPitch - yPitch).midicps;
	
	// ======= the FM synth
	modulator = SinOsc.ar([freq * modPartial, 3 +(freq *modPartial)], 0, freq * index * LFNoise1.kr(5.reciprocal).abs );
	carrier = SinOsc.ar((freq * carPartial) + modulator, 0, mul*Lag.kr(vol, volLag));

	//oscillator = SinOsc.ar([freq,freq+2],0,Lag.kr(vol, volLag));
	// ======= the REVERB
	signal = OnePole.ar(carrier, 0.9);
	5.do({ 
		signal = AllpassN.ar(signal, 0.040, [0.04.rand, 0.04.rand], decayTime) 
	});
	// ======= OUTPUT
	Out.ar(out,
		Limiter.ar(carrier * 0.5 + (signal * 0.5) , 0.9)
	)
}).load(server);

SynthDef(\droneSynth,{ arg out=0, vol=0.2, volLag=0.1, freq = 261;
	var drone;
	freq = freq.cpsmidi; // too lazy to calculate this in freq and I want GUI to show Hz
	drone = RLPF.ar(LFPulse.ar([freq-24, freq-24+7].midicps, 0.18, 0.1, 0.3)       // lower drone pitches
        				+ LFPulse.ar([freq-12, (freq-12)].midicps, 0.162, 0.1, 0.3),  // upper drone pitches
   				SinOsc.kr(0.14, 0, 10, 100).midicps)
	// drone 5ths
	+ RLPF.ar(LFPulse.ar([freq,freq + 7].midicps, 0.15),
		SinOsc.kr(0.1, 0, 10, 72).midicps, 0.1, 0.1);	
	
	Out.ar(out,
		Limiter.ar(drone* vol, 0.9)
	)
}).load(server);

		synth = Synth(\scaleSynth, [\buf1, scaleBuf.bufnum, \buf2, transBuf.bufnum]);
		drone = Synth(\droneSynth, [\vol, 0]);
		
}

}