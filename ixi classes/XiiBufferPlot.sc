XiiBufferPlot {

	var plotter, txt, chanArray, unlaced, val, minval, maxval, window, thumbsize, zoom, width, 
		layout, write=false, chanPlotter, bounds;
	var bufferFloatArray, theBuffer;
	
	*new { arg buffer, window, bounds, discrete=false, numChannels = 2;
		^super.new.initXiiBufferPlot(buffer, window, bounds, discrete, numChannels);
	}
		
	initXiiBufferPlot { arg buffer, window, bounds, discrete, numChannels;
		theBuffer = buffer;
		bufferFloatArray = theBuffer.loadToFloatArray(
				action: { |array, buf| {
					this.initGUI(array, window, bounds, buf.numChannels, discrete) }.defer;
				});
	}
	
	redraw {
		bufferFloatArray = theBuffer.loadToFloatArray(
				action: { |array, buf| {
					this.replot(array, theBuffer.numChannels) }.defer;
				});
	}
	
	replot {arg array, numChannels;
		minval = array.minItem;
		maxval = array.maxItem;
		unlaced = array.unlace(numChannels);
		chanArray = Array.newClear(numChannels);
		unlaced.do({ |chan, j|
			val = Array.newClear(width);
			width.do { arg i;
				var x;
				x = chan.blendAt(i / zoom);
				val[i] = x.linlin(minval, maxval, 0.0, 1.0);
			};
			chanArray[j] = val;
		});
		numChannels.do({ |i|
			chanPlotter[i].value_(chanArray[i]);
		});
	}
	
	initGUI {arg array, argwindow, argbounds, numChannels, discrete;
	
		bounds = argbounds ?  Rect(10, 5, 715, 300);
		chanPlotter = List.new;
		width = bounds.width-8;
		zoom = (width / (array.size / numChannels));
		
		if(discrete) {
			thumbsize = max(1.0, zoom);
		}{
			thumbsize = 1;
		};

		minval = array.minItem;
		maxval = array.maxItem;
		unlaced = array.unlace(numChannels);
		chanArray = Array.newClear(numChannels);
		unlaced.do({ |chan, j|
			val = Array.newClear(width);
			width.do { arg i;
				var x;
				x = chan.blendAt(i / zoom);
				val[i] = x.linlin(minval, maxval, 0.0, 1.0);
			};
			chanArray[j] = val;
		});
		
		window = argwindow ? SCWindow("ixi buffer plot", Rect(bounds.left, bounds.height, 			bounds.width+20, bounds.height+20), resizable: false);
		numChannels.do({ |i|
			chanPlotter.add(
				SCMultiSliderView(window, Rect(bounds.left, bounds.top + ((bounds.height/numChannels)*i),
											 bounds.width, bounds.height/numChannels))
				.readOnly_(true)
				.drawLines_(discrete.not)
				.drawRects_(discrete)
				.canFocus_(false)
				.thumbSize_(thumbsize) 
				.valueThumbSize_(1)
				.background_(XiiColors.lightgreen)
				.colors_(XiiColors.darkgreen, Color.blue(1.0,1.0))
				.action_({|v| 
					var curval;
					curval = v.currentvalue.linlin(0.0, 1.0, minval, maxval);
				})
				.keyDownAction_({ |v, char|
					if(char === $l) { write = write.not; v.readOnly = write.not;  };
				})
				.value_(chanArray[i])
				.resize_(5)
				.elasticMode_(1);
			);
				
		});
		
		^window.front;
	}
}