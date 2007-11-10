XiiSettings {	
	
	var settingsDict;
	
	*new { 
		^super.new.initXiiSettings;
	}
		
	initXiiSettings {
		var file;
		if(Object.readArchive("preferences/presets.ixi").isNil, {
			settingsDict = IdentityDictionary.new;
		}, {
			settingsDict = Object.readArchive("preferences/presets.ixi")
			
			// because readArchive is buggy I use File
			//file = File("preferences/presets.ixi","r");
			//settingsDict = file.readAllString.interpret;
			//file.close;
		});
	}
	
	storeSetting { arg settingName;
		var setting, file;
		"*********** STORE PRESET ******************".postln;
		setting = List.new; // not using dict because of name
		XQ.globalWidgetList.do({arg widget, i;
			if(widget.xiigui.isNil, { // if widget does not have a GUI abstraction
				setting.add([widget.asString.replace("a ",\), widget.getState]);
			}, {
				setting.add([widget.asString.replace("a ",\), widget.xiigui.getState]);
			});
		});
		//Post << [\setting, setting];
		settingsDict.add(settingName.asSymbol -> setting);
		settingsDict.writeArchive("preferences/presets.ixi");
		//file = File("preferences/presets.ixi","w");
		//file.write(settingsDict.asCompileString);
		//file.close;
	}	
	
	getSetting { arg name;
		var setting;
		setting = settingsDict.at(name.asSymbol);
		
	}
	
	getSettingsList {
		^settingsDict.keys.asArray;
	}
	
	loadSetting {arg name;
		var setting;
		"*********** LOAD PRESET ******************".postln;
		this.clearixiQuarks; // turn all quarks off and empty the screen
		setting = settingsDict.at(name.asSymbol);
		//Post << [\setting, setting];
		XQ.globalWidgetList = List.new;
		setting.do({arg widget, i;
			var channels, effectCodeString; 
			// [\number, i].postln;
			// Post << [\widget, widget];
			channels = widget[1][0];
			//effectCodeString = widget[0]++".new(Server.default,"++channels++","++widget[1]++")";
			effectCodeString = widget[0]++".new(Server.default,"++channels++","++widget[1].asCompileString++")";
			
			// Post << [\effectCodeString, effectCodeString];
			XQ.globalWidgetList.add( effectCodeString.interpret );
		});
	}
	
	clearixiQuarks {
		XQ.globalWidgetList.do({arg widget; // close all active windows
			if(widget.xiigui.isNil, { // if widget does not have a GUI abstraction
				widget.win.close;			
			}, {
				widget.xiigui.win.close;
			});
		});
	}
	
	removeSetting {arg settingName;
		settingsDict.removeAt(settingName.asSymbol);
		settingsDict.writeArchive("preferences/presets.ixi");
	}

}

