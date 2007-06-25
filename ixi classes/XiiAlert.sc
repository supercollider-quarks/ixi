XiiAlert {
	
	*new {arg message;
		^super.new.initXiiAlert(message);
	}

	initXiiAlert {arg message;
		var win, a, p;
		p = [ // the ixi logo
		Point(1,7), Point(8, 1), Point(15,1), Point(15,33),Point(24, 23), Point(15,14), Point(15,1), 
		Point(23,1),Point(34,13), Point(45,1), Point(61,1), Point(66,6), Point(66,37), Point(59,43),
		Point(53,43), Point(53,12), Point(44,22), Point(53,33), Point(53,43), Point(42,43), Point(34,32),
		Point(24,43), Point(7,43), Point(1,36), Point(1,8)
		];
		
		win = SCWindow("ixiQuarks alert!", Rect(128, 500, 400,160), resizable:false).front;
		win.drawHook = {
			// set the Color
			Color.new255(255, 100, 0).set;
			Pen.width = 3;
			Pen.translate(28,28);
			Pen.scale(1.6, 1.6);
			Pen.moveTo(1@7);
			p.do({arg point;
				Pen.lineTo(point+0.5);
			});
			Pen.stroke
		};
		a = SCStaticText(win, Rect(60, 120, 300, 20));
		a.string_(message);
		win.refresh;
	}
}

/*
XiiAlert.new("testing ixi alert window. How does it work?")
*/