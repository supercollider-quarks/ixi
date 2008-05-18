
// NOTE ! When using this class remember to stop it on win.onClose (the parent).

XiiEZSlider {
	var <>labelView, <>sliderView, <>numberView, <>controlSpec, <>action, <value;
	var <>round = 0.001;
	var recTask, playTask, pathList;
	var startRecFlag = true;
	
	*new { arg window, dimensions, label, controlSpec, action, initVal, 
			initAction=false, labelWidth=80, numberWidth = 80;
		^super.new.initXiiEZSlider(window, dimensions, label, controlSpec, action, initVal, 
			initAction, labelWidth, numberWidth);
	}
	initXiiEZSlider { arg window, dimensions, label="", argControlSpec, argAction, initVal, 
			initAction, labelWidth, numberWidth;
		var	decorator = window.asView.tryPerform(\decorator),
			gap = decorator.tryPerform(\gap);
		
		gap.notNil.if({
			(dimensions = dimensions.copy).x_(dimensions.x - (2*gap.x));
		});

		labelView = GUI.staticText.new(window, labelWidth @ dimensions.y);
		sliderView = GUI.slider.new(window, (dimensions.x - labelWidth - numberWidth) @ dimensions.y);
		numberView = GUI.numberBox.new(window, numberWidth @ dimensions.y);
		labelView.string = label;
		labelView.align = \right;
		
		controlSpec = argControlSpec.asSpec;
		initVal = initVal ? controlSpec.default;
		action = argAction;
		
		sliderView.action = {
			this.valueAction_(controlSpec.map(sliderView.value));
		};
		if (controlSpec.step != 0) {
			sliderView.step = (controlSpec.step / (controlSpec.maxval - controlSpec.minval));
		};

		sliderView.receiveDragHandler = { arg slider;
			slider.valueAction = controlSpec.unmap(GUI.view.currentDrag);
		};
		
		sliderView.beginDragAction = { arg slider;
			controlSpec.map(slider.value)
		};
		
		sliderView.keyDownAction = {arg me, char, modifiers, unicode, keycode;
			if (char == $a, { this.recPlayPath(true); });
			if (char == $c, { this.stop; });
			if (char == $r, { this.valueAction = 1.0.rand; ^this });
			if (char == $n, { this.valueAction = 0.0; ^this });
			if (char == $x, { this.valueAction = 1.0; ^this });
			//if (char == $c, { this.valueAction = 0.5; ^this });
			if (char == $], { this.increment; ^this });
			if (char == $[, { this.decrement; ^this });
			if(modifiers == 8651009, { // check if Ctrl is down first
				if (unicode == 16rF700, { this.incrementCtrl; ^this });
				if (unicode == 16rF703, { this.incrementCtrl; ^this });
				if (unicode == 16rF701, { this.decrementCtrl; ^this });
				if (unicode == 16rF702, { this.decrementCtrl; ^this });
			}, { // if not, then normal
				if (unicode == 16rF700, { this.increment; ^this });
				if (unicode == 16rF703, { this.increment; ^this });
				if (unicode == 16rF701, { this.decrement; ^this });
				if (unicode == 16rF702, { this.decrement; ^this });
			});
		};
		
		sliderView.keyUpAction = {arg me, char, modifiers, unicode, keycode;
			if (char == $a, { this.recPlayPath(false, window); });
			
		};

		numberView.action = { this.valueAction_(numberView.value) };
		
		if (initAction) {
			this.valueAction_(initVal);
		}{
			this.value_(initVal);
		};
	}
	
	value_ { arg val; 
		value = controlSpec.constrain(val);
		numberView.value = value.round(round);
		sliderView.value = controlSpec.unmap(value);
	}
	valueAction_ { arg val; 
		this.value_(val);
		this.doAction;
	}
	doAction { action.value(this) }

	set { arg label, spec, argAction, initVal, initAction=false;
		labelView.string = label;
		controlSpec = spec.asSpec;
		action = argAction;
		initVal = initVal ? controlSpec.default;
		if (initAction) {
			this.value = initVal;
		}{
			value = initVal;
			sliderView.value = controlSpec.unmap(value);
			numberView.value = value.round(round);
		};
	}
	
	visible { ^sliderView.visible }
	visible_ { |bool| [labelView, sliderView, numberView].do(_.visible_(bool)) }
	
	enabled {  ^sliderView.enabled } 
	enabled_ { |bool| [sliderView, numberView].do(_.enabled_(bool)) }
	
	remove { [labelView, sliderView, numberView].do(_.remove) }
	
	recPlayPath {arg rec=true, window;
		if(rec, { // recording the path
			if(startRecFlag == true, {
				if(pathList.size == 0, {pathList = List.new;}); // if 
				if(recTask.isNil, {
					recTask = Task({
						inf.do({
							pathList.add(this.value);
							0.07.wait;
						});
					}).play;
					startRecFlag = false;
				});
			});
		},{ // playing the path
			recTask.stop;
			recTask = nil;
			startRecFlag = false;
			if(playTask.isNil, {
				playTask = Task({
					inf.do({arg i;
						{if(window.isClosed.not,{this.valueAction_(pathList.wrapAt(i))})}.defer;
						0.07.wait;
					});
				}).play;
			});
		});
	}
	
	stop {
		playTask.stop;
		playTask = nil;
		recTask.stop;
		recTask = nil;
		{
		startRecFlag = true;
		pathList = List.new;
		}.defer(0.3);
	}
}
