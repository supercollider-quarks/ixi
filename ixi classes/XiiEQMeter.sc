XiiEQMeter {	

	var <>gui;

	var multislider, c, b, eqUpdateTask, name;
	var size, synth, cmdPeriodFunc, inbus, onOffButt, refreshTime;
	
	*new { arg server;
		^super.new.initXiiEQMeter(server);
		}
		
	initXiiEQMeter {arg argserver;
		var win, s, point;
		var strokeFlag, strokeRButt, fillRButt, freqText, cutfreqs;
		var speedButt;
		
		size = 32; // one extra band in multislider for aesthetic purposes
		s = argserver ? Server.default;
		c = Bus.control(s, size);
		inbus = 0;
		strokeFlag = false;
		refreshTime = 0.1;
		
		name = "ixi EQ Meter";
		point = XiiWindowLocation.new(name);
		
		cutfreqs = [20, 25, 32.5, 44, 54, 65, 80, 100, 125, 160, 200, 250, 315, 400, 500, 630, 800, 1000, 1250, 1600, 2000, 2500, 3150, 4000, 5000, 6300, 8000, 10000, 12500, 16000, 20000, 20000];

		win = SCWindow(name, Rect(point.x, point.y, 520, 243), resizable:false).front;
		multislider = SCMultiSliderView(win, Rect(10, 5, 496, 200))
						.value_(0.dup(size))
						.size_(size)
						.isFilled_(true)
						.indexThumbSize_(462/(size+1))
						.background_(Color.green(0.1))
						.canFocus_(false)
						.fillColor_(Color.green)
						.strokeColor_(Color.black)
						.xOffset_(2)
						.action_({arg xb;
							freqText.string_(cutfreqs[xb.index].asString);
						});
						
		b = Buffer.alloc(s, 2048*2, 1);
		
		SynthDef(\XiiEQMeter, { arg inbus; 
			var in, chain, powers, cutfreqs;
			in = In.ar(inbus, 2);
			in = Mix.ar(in);
			chain = FFT(b.bufnum, in);
			cutfreqs = [20, 25, 32.5, 44, 54, 65, 80, 100, 125, 160, 200, 250, 315, 400, 500, 630, 800, 1000, 1250, 1600, 2000, 2500, 3150, 4000, 5000, 6300, 8000, 10000, 12500, 16000, 20000, 20000];
			// original freq list:
			//cutfreqs = [20, 25, 31.5, 40, 50, 63, 80, 100, 125, 160, 200, 250, 315, 400, 500, 630, 800, 1000, 1250, 1600, 2000, 2500, 3150, 4000, 5000, 6300, 8000, 10000, 12500, 16000, 20000];
			powers = FFTSubbandPower.kr(chain, cutfreqs, 0);
			Out.kr(c.index, powers);
		}).load(s);

		// INBUS
		SCStaticText(win, Rect(12, 210, 50, 18))
			.string_("inbus")
			.font_(Font("Helvetica", 9));

		SCPopUpMenu(win, Rect(40, 212, 40, 14))
			.items_(XiiACDropDownChannels.getStereoChnList)
			.value_(0)
			.font_(Font("Helvetica", 9))
			.background_(Color.white)
			.canFocus_(false)
			.action_({ arg ch;
				inbus = ch.value * 2;
				synth.set(\inbus, inbus);
			});

		// DRAW STYLE
		SCStaticText(win, Rect(92, 210, 70, 18))
			.string_("bands")
			.font_(Font("Helvetica", 9));

		SCPopUpMenu(win, Rect(123, 212, 65, 14))
			.items_(["filled", "unfilled", "lines"])
			.value_(0)
			.font_(Font("Helvetica", 9))
			.background_(Color.white)
			.canFocus_(false)
			.action_({ arg ch;
				switch (ch.value)
					{0} { multislider.isFilled_(true);
						multislider.drawLines_(false);
						multislider.drawRects_(true);
						}
					{1} { multislider.isFilled_(false);
						 multislider.drawRects_(true);
						 multislider.drawLines_(false); 
						}
					{2} { multislider.drawLines_(true); 
						 multislider.drawRects_(false);
						 multislider.isFilled_(false)
						 };
			});

		// COLORS
		SCStaticText(win, Rect(200, 210, 50, 18))
			.string_("colors")
			.font_(Font("Helvetica", 9));
		
		strokeRButt = OSCIIRadioButton(win, Rect(230, 209, 12, 12), "stroke")
						.font_(Font("Helvetica", 9))
						.action_({
							fillRButt.switchState;
							strokeFlag = true;
							[\strokeFlag, strokeFlag].postln;
						});
		fillRButt = OSCIIRadioButton(win, Rect(230, 222, 12, 12), "fill")
						.font_(Font("Helvetica", 9))
						.value_(1)
						.action_({
							strokeRButt.switchState;
							strokeFlag = false;
							[\strokeFlag, strokeFlag].postln;
						});

		SCPopUpMenu(win, Rect(285, 212, 60, 14))
			.items_(["green", "black", "pink", "indian red", "light golen", "steel blue", "navajo white", "aqua marine", "bisque", "salmon"])
			.value_(0)
			.font_(Font("Helvetica", 9))
			.background_(Color.white)
			.canFocus_(false)
			.action_({ arg ch; var color;
				color = switch (ch.value)
				{0} { Color.green}
				{1} { Color.black} 
				{2} { Color(0.80392156862745, 0.37647058823529, 0.56470588235294, 1)}
				{3} { Color(0.93333333333333, 0.38823529411765, 0.38823529411765, 1)}
				{4} { Color(0.93333333333333, 0.86666666666667, 0.50980392156863, 1) }
				{5} { Color(0.63529411764706, 0.70980392156863, 0.80392156862745, 1) }
				{6} { Color(0.80392156862745, 0.70196078431373, 0.54509803921569, 1) }
				{7} { Color(0.4, 0.80392156862745, 0.66666666666667, 1) } 
				{8} { Color(0.80392156862745, 0.71764705882353, 0.61960784313725, 1) }
				{9} { Color(0.93333333333333, 0.50980392156863, 0.3843137254902, 1) };
				
				if(strokeFlag, {
					multislider.strokeColor_(color);
				}, {
					multislider.fillColor_(color);
				});
			});

		SCStaticText(win, Rect(360, 210, 60, 18))
			.string_("band freq :")
			.font_(Font("Helvetica", 9));

		freqText = SCStaticText(win, Rect(405, 210, 60, 18))
			.string_("25")
			.font_(Font("Helvetica", 9));

		speedButt = SCButton(win, Rect(430, 212, 36, 16))
			.states_([
					["fast",Color.black, Color.clear],
					["slow",Color.black, Color.green(alpha:0.2)]
				])
			.font_(Font("Helvetica", 9))
			.canFocus_(false)
			.action_({ arg butt;
				if(butt.value == 1, {refreshTime = 0.05}, {refreshTime = 0.1});
			});
			
		onOffButt = SCButton(win, Rect(470, 212, 36, 16))
			.states_([
					["on",Color.black, Color.clear],
					["off",Color.black, Color.green(alpha:0.2)]
				])
			.font_(Font("Helvetica", 9))
			.action_({ arg butt;
				if(butt.value == 1, {this.start}, {this.stop});
			});

		cmdPeriodFunc = { onOffButt.valueAction_(0)};
		CmdPeriod.add(cmdPeriodFunc);

		win.onClose_({
			var t;
			this.stop;
			b.free;
			c.free;
			CmdPeriod.remove(cmdPeriodFunc);
			~globalWidgetList.do({arg widget, i; if(widget === this, { t = i})});
			~globalWidgetList.removeAt(t);
			// write window position to archive.sctxar
			point = Point(win.bounds.left, win.bounds.top);
			XiiWindowLocation.storeLoc(name, point);
		});
	}
	
	start {
		synth = Synth(\XiiEQMeter, [\inbus, inbus], addAction: \addToTail);
		eqUpdateTask = Task({
			loop{
				c.getn(size, {|vals|
					{multislider.value_((vals.log2 * 0.2).max(0).min(1))}.defer;
				});
				refreshTime.wait; // I NEED TO EXPERIMENT WITH THIS.
			};
		}).start;
	}
	
	stop {
		eqUpdateTask.stop;
		synth.free;
	}
}