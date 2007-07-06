// audio channel drop down 

XiiACDropDownChannels {	

	classvar <>numChannels;
	
	*new { 
		^super.new.initXiiACDropDownChannels;
		}
		
	*setChannels {arg channels;
		numChannels = channels;
	}
	
	initXiiACDropDownChannels {
	
	
	}
	
	*getStereoChnList {
		var stereolist;
		
		//"numchannels is : ".post; numChannels.postln;
		stereolist = [];
		(numChannels/2).do({ arg i;
			//stereolist = stereolist.add($"++((i*2).asString++","+((i*2)+1).asString)++$"); 
			stereolist = stereolist.add(((i*2).asString++","+((i*2)+1).asString)); 
		});
		^stereolist;
		
//		^["0,1", "2,3", "4,5", "6,7", "8,9", "10,11", "12,13", "14,15", 
//		"16,17", "18,19", "20,21", "22,23", "24,25", "26,27", "28,29", 
//		"30,31", "32,33","34,35", "36,37", "38,39", "40,41" ]
	}

	*getMonoChnList {
		//"numchannels is : ".post; numChannels.postln;
		var monolist;
		
		monolist = [];
		numChannels.do({ arg i;
			//monolist = monolist.add(($"++(i*2).asString++$"++","+$"++((i*2)+1).asString++$")); 
			monolist = monolist.add(i.asString); 
		});
		^monolist;

		
//		^["0", "1", "2" ,"3", "4", "5", "6", "7", "8", "9", "10", "11", 
//		  "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", 
//		  "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", 
//		  "32", "33","34", "35", "36", "37", "38", "39", "40", "41" ]
	}

}
