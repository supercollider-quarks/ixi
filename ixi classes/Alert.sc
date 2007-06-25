

Alert {

*new { arg message, rect; 
	^super.new.initAlert(message, rect);
	}

initAlert { arg message, rect;
	var awindow, okbutt, damnbutt;
	awindow = SCWindow.new("alert", rect ? Rect(380, 520, 400, 150)).front;
	SCStaticText(awindow, Rect(30, 20, 130, 25)).string_("ixi Alert :");
	SCStaticText(awindow, Rect(30, 50, 1130, 15)).string_(message);
	
	okbutt = SCButton(awindow,Rect(300,100,60,20));		okbutt.states = [["ok then",Color.black,Color.new255(160, 170, 155, 100)]];
	okbutt.action_({awindow.close;});
	
	damnbutt = SCButton(awindow,Rect(236,100,60,20));		damnbutt.states = [["damn it",Color.black,Color.new255(160, 170, 155, 100)]];
	damnbutt.action_({awindow.close;});
	
	awindow.view.background = Color(1,1,1);
	}
}
