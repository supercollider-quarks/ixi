XiiPoolManager {

	var <>gui;

	*new { arg server, channels, rect, pool;
		^super.new.initXiiPoolManager(server, channels, rect, pool);
		}
		
	initXiiPoolManager {arg server, channels, argrect, pool;
		var win, rect;
		var selPool, txtv, saveButt, delPool, loadPool;
		var bufferDict, name, point;
		
		rect = argrect ? Rect(200, 100, 160, 56);
		name = "PoolManager";
		point = XiiWindowLocation.new(name);
		bufferDict = if(Object.readArchive("preferences/bufferPools.ixi").isNil,{
						()
					}, {
						Object.readArchive("preferences/bufferPools.ixi")					});
		
		win = SCWindow.new("PoolManager", Rect(point.x, point.y, rect.width, rect.height),
			 resizable:false).front;
		
		selPool = SCPopUpMenu(win, Rect(10, 5, 140, 16))
			.font_(Font("Helvetica", 9))
			.items_(bufferDict.keys.asArray)
			.value_(0)
			.background_(Color.white)
			.action_({ arg item;
			});

		delPool = SCButton(win, Rect(10, 27, 67, 16))
			.canFocus_(false)
			.font_(Font("Helvetica", 9))
			.states_([["delete pool", Color.black, Color.clear]])
			.action_({
				bufferDict.removeAt(selPool.items[selPool.value].asSymbol);
				selPool.items_(bufferDict.keys.asArray);
				bufferDict.writeArchive("preferences/bufferPools.ixi");
			});

		loadPool = SCButton(win, Rect(82, 27, 67, 16))
			.font_(Font("Helvetica", 9))
			.states_([["load pool", Color.black, Color.clear]])
			.action_({
				if(bufferDict.at(selPool.items[selPool.value]) != nil, {
					~globalWidgetList.add(
						// here sending bufferpaths and selection array
						XiiBufferPool.new(Server.default, selPool.items[selPool.value].asString)
							.loadBuffers(
								bufferDict.at(selPool.items[selPool.value])[0], // pathnames
								bufferDict.at(selPool.items[selPool.value])[1]  // selections
								);
					);
				});
			});

		// if the manager is created from a save button in the pool
		if(pool.isNil.not, {
			txtv = SCTextView(win, Rect(10, 51, 100, 14))
					.hasVerticalScroller_(false)
					.autohidesScrollers_(true)
					.focus(true)
					.font_(Font("Helvetica", 9))
					.string_("");
	
			saveButt = SCButton(win, Rect(115, 50, 34, 16))
				.states_([["save",Color.black, Color.clear]])
				.font_(Font("Helvetica", 9))
				.action_({ arg butt; var str, oldnamelist;
					str = if(txtv.string == "", {Date.getDate.stamp.asString}, {txtv.string});
					// saving filepaths and selection list into file
					bufferDict.add((str).asSymbol -> 
						[pool.getFilePaths, ~globalBufferDict.at(pool.name.asSymbol)[1]]);
					selPool.items_(bufferDict.keys.asArray);
					// CHANGING NAMES IN THE ~globalBufferDict
					// store the old bufferList
					oldnamelist = ~globalBufferDict.at(pool.name.asSymbol);
					// get rid of the old index key in ~globalBufferDict
					~globalBufferDict.removeAt(pool.name.asSymbol);
					// put it back as the new
					~globalBufferDict.add(str.asSymbol -> oldnamelist);
					// and rename the window
					pool.setName_(str);
					bufferDict.writeArchive("preferences/bufferPools.ixi");
				});
		});
		
		win.onClose_({
			var t;
			~globalWidgetList.do({arg widget, i; if(widget === this, { t = i})});
			~globalWidgetList.removeAt(t);
			
			//bufferDict.writeArchive("bufferPools.ixi");
			point = Point(win.bounds.left, win.bounds.top);
			XiiWindowLocation.storeLoc(name, point);
		});
	}
}
