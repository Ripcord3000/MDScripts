import com.swath.*;
import com.swath.cmd.*;
import java.util.*;
/**
 ** Make sure to have a planet scanner on your ship before running this.
 * @author Mind Dagger
 */
public class ProductMover extends UserDefinedScript {
        private Parameter planet;
        private Parameter name;
        private Planet mainPlanet;
        private Planet[] planets;
        private Vector fuelPlanets;
        
        public String getName() {
                return "Mind Dagger's Express Product Truck";
        }
        public boolean initScript() throws Exception {
                if (!atPrompt(Swath.COMMAND_PROMPT)) return false;
                
                planets = Swath.sector.planets();
                planet = new Parameter("Planet To Fill");
                for(int i=0;i < planets.length; i++)
                {
                	if(planets[i].id() > 0){
                		planet.addChoice(i, "#"+planets[i].id()+" "+planets[i].name());
                	}
                }
                planet.setCurrentChoice(0);                        
                
                name = new Parameter("Planet To Fill");
                for(int i=0;i < planets.length; i++)
                {
                	if(planets[i].id() > 0){
                		name.addChoice(i, "#"+planets[i].id()+" "+planets[i].name());
                	}
                }
                name.setCurrentChoice(1);                        
                
				registerParam(planet);
                registerParam(name);
		        return true;
        }
        public boolean runScript() throws Exception {
            	if(name.getCurrentChoice() == planet.getCurrentChoice()){
            		UserAlert.exec("You chose the same planet twice!",UserAlert.TYPE_ERROR);
            		return false;
            	}
        		mainPlanet = planets[planet.getCurrentChoice()];
            	JettisonCargo.exec();
            	planets = Swath.sector.planets();
            	fuelPlanets = new Vector();
            	for(int i = 0;i < planets.length;i++){
            		if(planets[i].name().equalsIgnoreCase(name.getString())){
            			if(planets[i].id() > 0){
            				fuelPlanets.add(planets[i]);
            			}
            		}
		}
		Land.exec(mainPlanet);  //Used to get ID's for all planets in sector.
		LiftOff.exec();
            	while(!fuelPlanets.isEmpty()){
          	  		while(!(atPrompt(Swath.COMMAND_PROMPT))){
          	  			UserAlert.exec("Waiting for correct prompt..",UserAlert.TYPE_INFORMATION);	
          	  		}
              					Planet temp = (Planet)fuelPlanets.get(0);
            					SendMacro.exec("l "+temp.id()+"^Mq",1,false);               			
            					temp = Swath.getPlanet(temp.id());
            					
            					if(temp.productAmounts()[Swath.ORGANICS] < Swath.ship.holds() && temp.productAmounts()[Swath.EQUIPMENT] < Swath.ship.holds()){
            						SendMacro.exec("l "+temp.id()+"^Mtnt1^Mql "+mainPlanet.id()+"^Mtnl1^Mq",10,false);
            					}
            					else{
            						SendMacro.exec("l "+temp.id()+"^Mtnt1^Mql "+mainPlanet.id()+"^Mtnl1^Mql "+temp.id()+"^Mtnt2^Mql "+mainPlanet.id()+"^Mtnl2^Mql "+temp.id()+"^Mtnt3^Mql "+mainPlanet.id()+"^Mtnl3^Mq",10,false);               			
            					}
            					try{
            						temp = Swath.getPlanet(temp.id());
                					int products[] = temp.productAmounts();
            						if(products[Swath.FUEL_ORE] < Swath.ship.holds() && products[Swath.ORGANICS] < Swath.ship.holds() && products[Swath.EQUIPMENT] < Swath.ship.holds()){
										UserAlert.exec("Planet "+((Planet)fuelPlanets.get(0)).id()+" is empty. There are "+(fuelPlanets.size()-1)+" full planets left.",UserAlert.TYPE_INFORMATION);
                						fuelPlanets.remove(0);
            						}
            					}
            					catch(Exception e){
            						//nothing		
            					}
		     	}
            	if(fuelPlanets.size() <= 0){
            			UserAlert.exec("All planets in sector "+Swath.sector.sector()+" are emptied.",UserAlert.TYPE_INFORMATION);
           		}
					
        	        	
            return true;
        }
        
        public void endScript(boolean finished){

        }

}
