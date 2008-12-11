// used in teaching to draw stuff such as waves 

XiiDrawer {	

	*new {
		^super.new.initDrawer;
		}
		
	initDrawer {
		var w, txt, tmppoints, all, userview;
		tmppoints = [];
		
		w = SCWindow("ixi drawer", Rect(128, 64, 540, 460));
	
		userview = SCUserView(w,w.view.bounds)
			.mouseMoveAction_({|v,x,y|
				tmppoints = tmppoints.add(v.mousePosition);
				v.refresh;
		})
			.mouseUpAction_({|v,x,y|
				all = all.add(tmppoints.copy);
				tmppoints = [];
				v.refresh;
		})
			.drawFunc_{|me|
				Pen.use {	
					Color.white.set;
					Pen.fillRect(me.bounds.moveTo(0,0));			
					Pen.width = 1;
					Color.black.set;
		
					Pen.beginPath;
					
					tmppoints.do{	|p, i|
						if(i == 0){
						Pen.moveTo(p);
						}{
						Pen.lineTo(p);
						}
					};
					all.do{|points|
						points.do{|p, i|
							if(i == 0){
								Pen.moveTo(p);
							}{
								Pen.lineTo(p);
							}
						};
					};
					Pen.stroke;
				};
			};	
		userview.relativeOrigin = true;
			
		w.front;
	}
	
}