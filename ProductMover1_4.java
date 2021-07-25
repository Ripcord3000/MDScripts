import com.swath.*;
import com.swath.cmd.*;
/**
 ** Make sure to have a planet scanner on your ship before running this.
 * @author Mind Dagger
 */
public class ProductMover1_4 extends UserDefinedScript {
        private Parameter planet;
        private Parameter name;
        private Planet mainPlanet;
        private Planet[] planets;
        private Planet fuelPlanet;
        private Parameter fuel;
        private Parameter organics;
        private Parameter equipment;
        
        
        public String getName() {
                return "MD's Express Product Mover 1.4";
        }
        public boolean initScript() throws Exception {
                if (!atPrompt(Swath.COMMAND_PROMPT)){
                	UserAlert.exec("Must be command prompt!",UserAlert.TYPE_ERROR);
                	return false;
                }
                planets = Swath.sector.planets();
                if(planets == null || planets.length <= 1){
                	if(planets.length == 1){
                		UserAlert.exec("Only one planet in sector!",UserAlert.TYPE_ERROR);
                	}
                	else{
                		UserAlert.exec("No planets in sector!",UserAlert.TYPE_ERROR);
                	}
            		return false;
                }
                planet = new Parameter("Planet To Fill");
                for(int i=0;i < planets.length; i++)
                {
                	if(planets[i].id() > 0){
                		planet.addChoice(i, "#"+planets[i].id()+" "+planets[i].name());
                	}
                }
                planet.setCurrentChoice(0);                        
                
                name = new Parameter("Planet To Empty");
                for(int i=0;i < planets.length; i++)
                {
                	if(planets[i].id() > 0){
                		name.addChoice(i, "#"+planets[i].id()+" "+planets[i].name());
                	}
                }
                name.setCurrentChoice(0);                        
                
                fuel = new Parameter("Move Fuel Ore");
        		fuel.setType(Parameter.BOOLEAN);
        		fuel.setBoolean(true);
        		
        		organics = new Parameter("Move Organics");
        		organics.setType(Parameter.BOOLEAN);
        		organics.setBoolean(true);
        		
        		equipment = new Parameter("Move Equipment");
        		equipment.setType(Parameter.BOOLEAN);
        		equipment.setBoolean(true);
        		
				registerParam(planet);
				registerParam(name);
				registerParam(fuel);
				registerParam(organics);
				registerParam(equipment);
		        return true;
        }
        public boolean runScript() throws Exception {
            	if(name.getCurrentChoice() == planet.getCurrentChoice()){
            		UserAlert.exec("You chose the same planet twice!",UserAlert.TYPE_ERROR);
            		return false;
            	}
        		JettisonCargo.exec();
            	planets = Swath.sector.planets();
            	fuelPlanet = planets[name.getCurrentChoice()];
            	mainPlanet = planets[planet.getCurrentChoice()];
            	fuelPlanet = Swath.getPlanet(fuelPlanet.id());
				int products[] = fuelPlanet.productAmounts();
				
		    	while(products[Swath.FUEL_ORE] >= Swath.ship.holds() || products[Swath.ORGANICS] >= Swath.ship.holds() || products[Swath.EQUIPMENT] >= Swath.ship.holds()){
          	  		if(atPrompt(Swath.COMMAND_PROMPT)){
          	  				Land.exec(fuelPlanet);
          	  				LiftOff.exec();
          	  				fuelPlanet = Swath.getPlanet(fuelPlanet.id());
            					String result = "";
            					
            					if(fuelPlanet.productAmounts()[Swath.FUEL_ORE] > Swath.ship.holds() && fuel.getBoolean()){
            						result += "l "+fuelPlanet.id()+"^Mtnt1^Mql "+mainPlanet.id()+"^Mtnl1^Mq";
            					}
            					if(fuelPlanet.productAmounts()[Swath.ORGANICS] > Swath.ship.holds() && organics.getBoolean()){
            						result += "l "+fuelPlanet.id()+"^Mtnt2^Mql "+mainPlanet.id()+"^Mtnl2^Mq";
            					}
            					if(fuelPlanet.productAmounts()[Swath.EQUIPMENT] > Swath.ship.holds() && equipment.getBoolean()){
            						result += "l "+fuelPlanet.id()+"^Mtnt3^Mql "+mainPlanet.id()+"^Mtnl3^Mq";
            					}
            					SendMacro.exec(result,10,true);
            					
          	  		}
          	  	}
           return true;
        }
        
        public void endScript(boolean finished){

        }

}
