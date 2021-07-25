import com.swath.*;
import com.swath.cmd.*;

/**
 * Sector Fig Dumper 
 *
 * Used to move fighters into a sector either grabbing them from a planet or buying them 
 * from Sector 1.
 *
 * @author Mind Dagger
 */
public class MDFigDump1_2 extends UserDefinedScript {
        private Parameter planet;
        private Parameter whereToGetFigs;
        private Parameter corpOrPersonal;
        private Parameter typeOfFigSet;
        private Parameter maxiFigs;
        private Parameter maxiHolds;
        private Parameter unlimited;
        private Parameter silent;
         private Planet[] planets;
        
        
        
        public String getName() {
                return "Fig Dump 1.2";
        }

        public boolean initScript() throws Exception {
                if (!atPrompt(Swath.COMMAND_PROMPT)) return false;
                planets = Swath.sector.planets();
                
                whereToGetFigs = new Parameter("Source of Fighters");
                whereToGetFigs.addChoice(0, "Sector Planet");
                whereToGetFigs.addChoice(1, "Buy From Sector 1 (Blue Only)");
                whereToGetFigs.setCurrentChoice(0);
		
                planet = new Parameter("Planet to get Fighters or Fuel for T-Warp");
                for(int i=0;i < planets.length; i++)
                {
                	planet.addChoice(i, planets[i].toString());
                }
                planet.setCurrentChoice(0);

                corpOrPersonal = new Parameter("Owner");
                corpOrPersonal.addChoice(0, "Corporate");
                corpOrPersonal.addChoice(1, "Personal");
                corpOrPersonal.setCurrentChoice(0);

                typeOfFigSet = new Parameter("Type of Fighter to Set");
                typeOfFigSet.addChoice(0, "Defensive");
                typeOfFigSet.addChoice(1, "Offensive");
                typeOfFigSet.addChoice(2, "Toll");
                typeOfFigSet.setCurrentChoice(0);
                
                maxiFigs = new Parameter("Maximum Fighters On Ship");
                maxiFigs.setType(Parameter.INTEGER);
                maxiFigs.setInteger(Swath.ship.shipCategory().maxFighters());
                
                maxiHolds = new Parameter("Holds On Ship");
                maxiHolds.setType(Parameter.INTEGER);
                maxiHolds.setInteger(Swath.ship.holds());
                
                unlimited = new Parameter("Unlimited Cash For Figs?");
                unlimited.setType(Parameter.BOOLEAN);
                unlimited.setBoolean(false);
                
                silent = new Parameter("Comms Off?");
                silent.setType(Parameter.BOOLEAN);
                silent.setBoolean(true);
                
                registerParam(whereToGetFigs);
                registerParam(planet);
                registerParam(corpOrPersonal);
                registerParam(typeOfFigSet);
                registerParam(maxiFigs);
                registerParam(maxiHolds);
                registerParam(unlimited);
                registerParam(silent);

                return true;
        }

        public boolean runScript() throws Exception {
        EnterComputer.exec();
        ChangeSettings.exec(ChangeSettings.SPACE);
        if(silent.getBoolean()){
        	ChangePersonalSettings.exec(Swath.NO_CHANGE,Swath.OFF,Swath.NO_CHANGE,Swath.NO_CHANGE,Swath.NO_CHANGE,Swath.NO_CHANGE,Swath.YES);
        	SendSSRadioMessage.exec("Comms are off. (MD Fig Dump 1.2)");
        }
		LeaveComputer.exec();
		Planet currentPlanet = (Planet)(planets[planet.getCurrentChoice()]);
		int maxFigs = maxiFigs.getInteger();
		//int maxHolds = maxiHolds.getInteger();
		if(whereToGetFigs.getCurrentChoice() == 0)
		{
		    //Get Fighters From Planet In The Same Sector
		    if(Swath.ship.fighters() > 0)
		    {
		    	dropFigs();
		    }
		    while(currentPlanet.fighters() > 0 && Swath.sector.fighters() < (2000000000-maxiFigs.getInteger()))
		    {
		    	Land.exec(currentPlanet);
		    	if(currentPlanet.fighters() >= maxFigs)
		    	{
		    		TakeLeaveFighters.exec(maxFigs);			    
		    	}
		    	else{
		    		TakeLeaveFighters.exec(currentPlanet.fighters());			    
		    	}
		    	LiftOff.exec();
		    	dropFigs();
		    }
		
		
		}
		else if(whereToGetFigs.getCurrentChoice() == 1)
		{
			if(Swath.you.alignment() < 1000){
				throw new Exception("You must have at least 1000 alignment to buy figs from Sector 1.");
			}
			//Buy Fighters in Sector 1 using T-Warp
		    if(Swath.ship.fighters() > 0){
		    	dropFigs();
		    }
		    while(Swath.you.credits() >= 10000000 && Swath.sector.fighters() < (2000000000-maxiFigs.getInteger())){
		    		SendMacro.exec("l "+currentPlanet.id()+"^M t n t 1^M q 1^M y y p t b "+maxiFigs.getInteger()+"^M q "+currentPlanet.sector()+"^M y y /",1,false);
    		        dropFigs();
    		        if(unlimited.getBoolean() && Swath.you.credits() < 90000000){
    		        	SendMacro.exec("l "+currentPlanet.id()+"^M c t f 900000000^M q q ");
    		        }
 		    }
		}   
		if(silent.getBoolean()){
			EnterComputer.exec();
			ChangePersonalSettings.exec(Swath.NO_CHANGE,Swath.NO_CHANGE,Swath.NO_CHANGE,Swath.NO_CHANGE,Swath.NO_CHANGE,Swath.NO_CHANGE,Swath.NO);
			LeaveComputer.exec();
	        SendSSRadioMessage.exec("Comms back on. (MD Fig Dump 1.2");
		}
        
		if(Swath.sector.fighters() >= (2000000000-maxiFigs.getInteger())){
			UserAlert.exec("Sector filled with fighters, done.",UserAlert.TYPE_INFORMATION);
    	}
                return true;
        }
        public void endScript(boolean finished){
        
        }
	public void dropFigs(){
		try{
			
	    if(corpOrPersonal.getCurrentChoice() == 0){
		
	    	if(typeOfFigSet.getCurrentChoice() == 0){
	    		SendMacro.exec("f"+(Swath.ship.fighters()+Swath.sector.fighters())+"^M c d ");
	    	}	    
		else if(typeOfFigSet.getCurrentChoice() == 1){
			SendMacro.exec("f"+(Swath.ship.fighters()+Swath.sector.fighters())+"^M c o ");
			//DropTakeFighters.exec(Swath.ship.fighters()+Swath.sector.fighters(), Swath.CORPORATE, Swath.OFFENSIVE_FTRS);   	
		}
		else{
			SendMacro.exec("f"+(Swath.ship.fighters()+Swath.sector.fighters())+"^M c t ");
		//	DropTakeFighters.exec(Swath.ship.fighters()+Swath.sector.fighters(), Swath.CORPORATE, Swath.TOLL_FTRS);     	
		}
	    }
	    else{
		    if(typeOfFigSet.getCurrentChoice() == 0){
		    	SendMacro.exec("f"+(Swath.ship.fighters()+Swath.sector.fighters())+"^M d ");
				//DropTakeFighters.exec(Swath.ship.fighters()+Swath.sector.fighters(), Swath.PERSONAL, Swath.DEFENSIVE_FTRS);   
		    }	    
		    else if(typeOfFigSet.getCurrentChoice() == 1){
		    	SendMacro.exec("f"+(Swath.ship.fighters()+Swath.sector.fighters())+"^M o ");
				//DropTakeFighters.exec(Swath.ship.fighters()+Swath.sector.fighters(), Swath.PERSONAL, Swath.OFFENSIVE_FTRS);   	
		    }
		    else{
		    	SendMacro.exec("f"+(Swath.ship.fighters()+Swath.sector.fighters())+"^M t ");
				//DropTakeFighters.exec(Swath.ship.fighters()+Swath.sector.fighters(), Swath.PERSONAL, Swath.TOLL_FTRS);     	
		    }
	     }
		}
		catch(Exception e){
		
		}
	}
}
