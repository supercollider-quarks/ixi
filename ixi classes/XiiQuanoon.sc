
XiiQuanoon {

	var <>xiigui;
	var <>win, params;
		
	*new { arg server, channels, setting = nil;
		^super.new.initXiiQuanoon(server, channels, setting);
		}
		
	initXiiQuanoon {arg server, channels, setting;

	var strings, scale, scalenames, scaleObject;
	var thisX, lastX, thisY, lastY;
	var stringRecent, point, userView;
	var fundamental, octave, outbus, vol;
	var tmppoints;
	var playdstrings = {0} ! 8; // array counting how often strings are plucked
	var stringwidth = {1.3} ! 8; // widths of the strings
	var name = "quanoon maqamak";

		xiigui = nil; // not using window server class here
		point = if(setting.isNil, {XiiWindowLocation.new(name)}, {setting[1]});
		params = if(setting.isNil, {[0,0,1]}, {setting[2]});

	stringRecent = true;
	fundamental = 110;
	octave = 1;
	scalenames = [\ajam, \jiharkah, \shawqAfza, \sikah, \huzam, \iraq, \bastanikar, \mustar, \bayati, \karjighar,\husseini, \nahawand, \farahfaza, \murassah, \ushaqMashri, \rast, \suznak, \nairuz, \yakah, \mahur, \hijaz, \zanjaran, \zanjaran, \saba, \zamzam, \kurd, \kijazKarKurd, \nawaAthar, \nikriz, \atharKurd, \major, \ionian, \dorian, \phrygian, \lydian, \mixolydian, \aeolian, \minor, \locrian, \harmonicMinor, \harmonicMajor, \melodicMinor, \melodicMajor, \bartok, \todi, \purvi, \marva, \bhairav, \ahirbhairav, \superLocrian, \romanianMinor, \hungarianMinor, \neapolitanMinor, \enigmatic, \spanish];
	
	scaleObject = Scale.new;
	scale = scaleObject.scale_(scalenames[params[0]]).ratios.add(2);
	outbus = 0;
	vol = 1;
	
	strings = 8.collect({|i| Rect(30 +(i*30),10, 4, 680) });
	
	thisX=0;
	lastX=1;
	thisY=0;
	lastY=1;
	tmppoints = [];
	
	win = GUI.window.new(name, Rect(point.x, point.y, 290, 734), resizable:false).front;
	
	userView = GUI.userView.new(win, Rect(10,10,280, 680))
		.relativeOrigin_(true)
		.clearOnRefresh_(false)
		.canFocus_(false)
		.drawFunc_({
			Pen.translate(0.5,0.5);
			8.do({|i| Pen.line(Point(30+(i*30), 10), Point(30+(i*30), 670)) }); // strings
			6.do({|i| Pen.line(Point(10, 10+(i*132)), Point(260, 10+(i*132))) }); // octave bands
			Pen.line(Point(10, 6), Point(260, 6));
			Pen.line(Point(10, 674), Point(260, 674));
			Pen.stroke;
			userView.drawFunc_({nil}) // don't draw the strings again
		});
	
		
	GUI.tabletView.new(win, Rect(10,10,280, 680))
		.canFocus_(false)
		.background_(Color.clear)
		.mouseDownAction_({ arg  view, x, y, pressure;
			lastX = x; // jump between strings without playing all in between
			strings.do({|string, i|
				if(string.contains(Point(x, y)), {
					playdstrings[i] = playdstrings[i] + 1;
					Synth(\xiiQuanoon, [
						\freq, (fundamental*scale[i]*[1,2,4,8,16][(5-(y/132).floor(1))]), 
						\dur, ((y/135)-(y/135).floor(1)*6),
						\amp, pressure*vol, 
						\outbus, outbus]);
				});
			});
		})
		.action_({ arg view, x, y, pressure;
			thisX = x;
			thisY = y;
			//t.background = Color(x / 300,y / 300,pressure, 0.1);
			if((thisX-lastX).abs > 3, {
				Task({ // don't play all the strings at once
					strings.do({|str, i|
						if(	((str.left>lastX) && (str.left<thisX)) || 
							((str.left<lastX) && (str.left>thisX)), {
							playdstrings[i] = playdstrings[i] + 1;
							Synth(\xiiQuanoon, [
								\freq, (fundamental*scale[i]*[1,2,4,8,16][(4-(y/135).floor(1))]), 
								\dur, ((y/135)-(y/135).floor(1)*6),
								\amp, pressure*vol, 
								\outbus, outbus]);
							0.01.wait;
							lastX = x;
						});
					});
				}).play;
			});
			if((thisY-lastY).abs > 4, { // allow for sliding down string (repeated playing)
				strings.do({|str, i|
					if(	((str.left>(lastX-2)) && (str.left<(thisX+2))) || 
						((str.left<(lastX+2)) && (str.left>(thisX-2))), {
						playdstrings[i] = playdstrings[i] + 1;
						Synth(\xiiQuanoon, [
							\freq, (fundamental*scale[i]*[1,2,4,8,16][(4-(y/135).floor(1))]),
							\dur, ((y/135)-(y/135).floor(1)*6), 
							\amp, pressure*vol,
							\outbus, outbus]);
						lastY = y;
					});
				});
			});
		});
		
		GUI.popUpMenu.new(win, Rect(20, 700, 80, 16))
			.font_(Font("Helvetica", 9))
			.items_(scalenames)
			.value_(params[0])
			.background_(Color.new255(255, 255, 255))
			.action_({ arg popup; 
				scale = scaleObject.scale_(scalenames[popup.value]).ratios.add(2);
				params[0] = popup.value;
			});
	
		GUI.staticText.new(win, Rect(110, 700, 44, 16))
			.font_(Font("Helvetica", 9))
			.string_("out:");
			
		GUI.popUpMenu.new(win, Rect(130, 700, 44, 16)) // outbusses
			.items_( XiiACDropDownChannels.getStereoChnList )
			.value_(params[1])
			.font_(Font("Helvetica", 9))
			.background_(Color.white)
			.canFocus_(false)
			.action_({ arg ch; params[1] = ch.value; outbus = ch.value * 2; });
	
		OSCIISlider(win, Rect(184, 700, 88, 8), "vol", 0, 1, 1, 0.01)
			.canFocus_(false)
			.value_(params[2])
			.font_(Font("Helvetica", 9))
			.action_({arg slider; 
				vol = slider.value;
				params[2] = vol;
			});
		
		// plot the frequency of strings played
		win.view.keyDownAction_({|me, char|
			if(char == $p, {
				playdstrings.ixiplot(discrete:true);
			})	
		});
		
		win.onClose_({
			var t;
			XQ.globalWidgetList.do({arg widget, i; if(widget === this, { t = i})});
			try{XQ.globalWidgetList.removeAt(t)};
			// write window position to archive.sctxar
			point = Point(win.bounds.left, win.bounds.top);
			XiiWindowLocation.storeLoc(name, point);
		});

	}
	
	getState { // for save settings
		var point;		
		\before.postln;
		point = Point(win.bounds.left, win.bounds.top);
		\after.postln;
		^[2, point, params];
	}

}