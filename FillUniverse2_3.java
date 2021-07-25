import com.swath.*;
import com.swath.cmd.*;

import java.util.*;
/**
 *
 */
public class FillUniverse2_3 extends UserDefinedScript {
	private Parameter figs;
	private Parameter maxFigs;
	private Parameter noworries;
	private Parameter silent;
    
	
	public String getName() {
		// Return the name of the script
		return "MD's FillUniverse 2.3";
	}

	public boolean initScript() throws Exception {
		// Initialisation of the script is done in this method.
		// All parameters should be created and registered here.
		// If something goes wrong, return false.

		// Check that we are at the correct prompt
		if (!atPrompt(Swath.COMMAND_PROMPT)) return false;
 
		
		figs = new Parameter("# Of Figs To Drop");
		figs.setType(Parameter.INTEGER);
		figs.setInteger(10);
		
		maxFigs = new Parameter("Max Fighter Attack");
		maxFigs.setType(Parameter.INTEGER);
		maxFigs.setInteger(60000);
		
		noworries = new Parameter("No Worries?");
		noworries.setType(Parameter.BOOLEAN);
		noworries.setBoolean(false);
		
		silent = new Parameter("Comms Off?");
        silent.setType(Parameter.BOOLEAN);
        silent.setBoolean(true);
        
				
		
		registerParam(figs);
		registerParam(maxFigs);
		registerParam(silent);
		registerParam(noworries);
		
		return true;
	}

	public boolean runScript() throws Exception {
				Set universe = new HashSet();
				EnterComputer.exec();
		        ChangeSettings.exec(ChangeSettings.SPACE);
		        if(silent.getBoolean()){
		        	ChangePersonalSettings.exec(Swath.NO_CHANGE,Swath.OFF,Swath.NO_CHANGE,Swath.NO_CHANGE,Swath.NO_CHANGE,Swath.NO_CHANGE,Swath.YES);
		        	SendSSRadioMessage.exec("Comms are off. (MD Fill Universe)");
		        }
				LeaveComputer.exec();
				Sector temp = null;
				Sector[] course = null;
				for(int i = 1; i <= Swath.main.sectors();i++){
					temp = Swath.getSector(i);
					
					if(!(universe.contains(temp)) && !(temp.isAvoided()) && !(temp.isFedSpace()) && !(temp.ftrOwner().isYou() || temp.ftrOwner().isYourCorporation())){
						try{	
							EnterComputer.exec();
							course = PlotCourse.exec(Swath.sector,temp);
							LeaveComputer.exec();
							if(noworries.getBoolean()){
								LawnMow.exec(course,maxFigs.getInteger(),figs.getInteger());
								for(int j = 0; j < course.length; j++){
									universe.add((Sector)course[j]);
								}
								
							}
							else{
								boolean noEnter = false;
								if(course.length > 0){
									int j = 1;
									while(j < course.length && !noEnter){
										Sector next = Swath.getSector(course[j].sector());
										if(next.density() > 5000){
											noEnter = true;
											SendSSRadioMessage.exec("High Density in Sector "+next.sector());
										}
										if(!noEnter){
											LawnMow.exec(next,maxFigs.getInteger(),figs.getInteger(),true);	
											j++;
											universe.add(temp);
										}
									}
									
										
								}
							}
							
						}
						catch(Exception e){
							if(atPrompt(Swath.COMPUTER_PROMPT)){
								LeaveComputer.exec();
							}
							UserAlert.exec("Can't get to sector "+temp.sector()+e,UserAlert.TYPE_INFORMATION);
						}
						
					
					}
					else{
						//SendSSRadioMessage.exec(i+"B");
						universe.add(temp);
						//SendSSRadioMessage.exec(i+"C");
						
					}
					
				}
				if(silent.getBoolean()){
					EnterComputer.exec();
					ChangePersonalSettings.exec(Swath.NO_CHANGE,Swath.NO_CHANGE,Swath.NO_CHANGE,Swath.NO_CHANGE,Swath.NO_CHANGE,Swath.NO_CHANGE,Swath.NO);
					LeaveComputer.exec();
			        SendSSRadioMessage.exec("Comms back on. (MD Fill Universe");
				}
		

		return true;
	}


	
}
