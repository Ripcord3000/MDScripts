import com.swath.*;
import com.swath.cmd.*;

/**
 * @author Mind Dagger
 */
public class MDGenesis extends UserDefinedScript {
        private Parameter planet;
        private Parameter number;
        private Parameter name;
        private Parameter increment;
        private Parameter startNumber;
        private int owner;
        private Parameter fuel;
        private Parameter SD;
        private Parameter twarp;
        private Parameter atomic;
        private Parameter adjacent;
        private Planet[] p;
            
        private PlanetClass[] planets;
        
        
        
        public String getName() {
                return "MD Genesis";
        }

        public boolean initScript() throws Exception {
                if(!atPrompt(Swath.COMMAND_PROMPT)){
                	throw new Exception("Must be at Command Prompt.");
                }
                planets = Swath.getAllPlanetClasses();
                planet = new Parameter("Type of planet to make.");
                for(int i=0;i < planets.length; i++)
                {
                	if(!planets[i].name().equals("")){
                		planet.addChoice(i, planets[i].name());
                	}
                }
                planet.setCurrentChoice(0);
                
                fuel = new Parameter("Fuel Source");
        		fuel.setType(Parameter.CHOICE);
        		fuel.addChoice(0,"Port: "+Swath.sector.portName());
        		p = Swath.sector.planets();
        		for(int i = 0;i < p.length;i++){
        			fuel.addChoice(p[i].id(),"Planet #"+p[i].id()+" "+p[i].name());
        		}
        		fuel.setCurrentChoice(0);
        			if(Swath.you.alignment() < 1000){
        				twarp = new Parameter("Twarp next to SD for refills?");
        			}
        			else{
        				twarp = new Parameter("Twarp to SD for refills?");
        			}
                    twarp.setType(Parameter.BOOLEAN);
                    twarp.setBoolean(false);
                    
                   	SD = new Parameter("StarDock Sector?");
                    SD.setType(Parameter.INTEGER);
                    SD.setInteger(Swath.main.stardock());
                    SD.setIntegerRange(1,20000);
                    
                    if(Swath.you.alignment() < 1000){
                    	adjacent = new Parameter("Adjacent SD Sector:");
                    	adjacent.setType(Parameter.INTEGER);
                    	adjacent.setInteger(0);
                    	adjacent.setIntegerRange(1,20000);
                    }
                   
                    atomic = new Parameter("Number of torps and detonators to buy?");
                    atomic.setType(Parameter.INTEGER);
                    atomic.setInteger(1);
                    atomic.setIntegerRange(1,20);
                   
        		
     
        		number = new Parameter("How many to make?");
                number.setType(Parameter.INTEGER);
                number.setInteger(1);
                number.setIntegerRange(1,80);
                 
                name = new Parameter("Name for planet(s)?");
                name.setType(Parameter.STRING);
                name.setString("xXx");
                name.setMaxStringLength(15);
                
                increment = new Parameter("Add Numbers To Name?");
                increment.setType(Parameter.BOOLEAN);
                increment.setBoolean(false);
                
                startNumber = new Parameter("Start Numbers From:");
                startNumber.setType(Parameter.INTEGER);
                startNumber.setInteger(1);
                startNumber.setIntegerRange(0,10000);
                
            
                
                if(Swath.you.corporation() != null){
                	owner = Swath.CORPORATE;
                }
                else{
                	owner = Swath.PERSONAL;
                }
                
        	registerParam(planet);
        	registerParam(name);
        	registerParam(number);
        	registerParam(increment);
        	registerParam(startNumber);
        	registerParam(twarp);
            registerParam(fuel);
            registerParam(atomic);
            registerParam(SD);
        	
        	if(Swath.you.alignment() < 1000){
        		registerParam(adjacent);
        	}
        	return true;
        }

        public boolean runScript() throws Exception {
        int count = 0;
        int startingSector = Swath.sector.sector();
        DisplayCurrentInfo.exec(true);
        if(twarp.getBoolean() && Swath.ship.fuel() < Swath.ship.holds()){
        	try{
        		JettisonCargo.exec();
        	}
        	catch(Exception e){
        		
        	}
        }
        String planetName = name.getString();
		PlanetClass currentPlanet = (PlanetClass)(planets[planet.getCurrentChoice()]);
        boolean isDone = false;
        if(Swath.ship.transWarpDrive() != Ship.NO_TWD && (Swath.ship.atomicDevices() <= 0 || Swath.ship.genesisTorpedos() <= 0) && twarp.getBoolean()){
			refuel();
			if(Swath.you.alignment() >= 1000 && startingSector != SD.getInteger()){
				TransWarp.exec(SD.getInteger());
			}
			else if(Swath.you.alignment() < 1000 && Swath.sector.sector() !=  SD.getInteger() && !Swath.sector.isStarDock()){
				TransWarp.exec(adjacent.getInteger());
				Move.exec(SD.getInteger());
			}
			LandOnStarDock.exec(true);
			BuyItems.exec(BuyItems.ATOMIC_DETONATORS,atomic.getInteger());
			BuyItems.exec(BuyItems.GENESIS_TORPEDOES,atomic.getInteger());
			LeaveStarDock.exec();
			if(startingSector != SD.getInteger()){
				TransWarp.exec(startingSector);
			}
		}
        while(Swath.ship.atomicDevices() > 0 && Swath.ship.genesisTorpedos() > 0 && !isDone){
			DisplayCurrentInfo.exec(true);
			if(increment.getBoolean()){
				planetName = name.getString()+" "+(count+startNumber.getInteger());
			}
			if(FastCreatePlanet.exec(planetName,currentPlanet,owner,"++ MD Genesis 1.0 ++")){
				count++;
				if(count >= number.getInteger()){
					isDone = true;
				}
			}
			if(Swath.ship.transWarpDrive() != Ship.NO_TWD && (Swath.ship.atomicDevices() <= 0 || Swath.ship.genesisTorpedos() <= 0) && twarp.getBoolean()){
				refuel();
				if(Swath.you.alignment() >= 1000 && startingSector != SD.getInteger()){
					TransWarp.exec(SD.getInteger());
				}
				else if(Swath.you.alignment() < 1000 && Swath.sector.sector() !=  SD.getInteger() && !Swath.sector.isStarDock()){
					TransWarp.exec(adjacent.getInteger());
					Move.exec(SD.getInteger());
				}
				LandOnStarDock.exec(true);
				BuyItems.exec(BuyItems.ATOMIC_DETONATORS,atomic.getInteger());
				BuyItems.exec(BuyItems.GENESIS_TORPEDOES,atomic.getInteger());
				LeaveStarDock.exec();
				if(startingSector != SD.getInteger()){
					TransWarp.exec(startingSector);
				}
			}
		}
		if(!isDone){
			if(Swath.ship.atomicDevices() <= 0){
				SendSSRadioMessage.exec("(MD Genesis) Out of atomic detonators.");
			}
			if(Swath.ship.genesisTorpedos() <= 0){
				SendSSRadioMessage.exec("(MD Genesis) Out of genesis torpedos.");
			}
		}
		return true;
        }
        private void refuel() throws Exception {
    		DisplayCurrentInfo.exec(true);
    		//Refuel
    		try{
    			Trade.exec(Swath.ship.emptyHolds(),0,0);
    		}
    		catch(Exception e){
    			UpgradePort.exec(Swath.FUEL_ORE,26);
    			try{
    				Trade.exec(Swath.ship.emptyHolds(),0,0);
    			}
    			catch(Exception f){
    				
    			}
    		}
    	}
}
