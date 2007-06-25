// audio channel drop down 
XiiACDropDownChannels {	

	classvar <>numChannels;
	
	*new { 
		^super.new.initXiiACDropDownChannels;
		}
		
	*setChannels {arg channels;
		numChannels = channels;
	}

	*getStereoChnList {
		var stereolist;
		stereolist = [];
		(numChannels/2).do({ arg i;
			stereolist = stereolist.add(((i*2).asString++","+((i*2)+1).asString)); 
		});
		^stereolist;
	}

	*getMonoChnList {
		var monolist;
		
		monolist = [];
		numChannels.do({ arg i;
			monolist = monolist.add(i.asString); 
		});
		^monolist;
	}
}
