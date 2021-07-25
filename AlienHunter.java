import com.swath.*;
import com.swath.event.*;
import com.swath.cmd.*;
import java.util.*;

public class AlienHunter extends UserDefinedScript {
        protected String title = "Alien Hunter";
        protected String error = "Alien Hunter Error";
        protected Parameter planet;
        protected Parameter firePhotons;
        private Planet[] planets;
        private Sector startingSector;
        private Sector attackingSector;
        private Trader victim;
        private EventIfc info;
        private ShipEnteredSector shipInfo;
        
public String getName() { return title; }

public boolean initScript() throws Exception {

        if(Swath.main.prompt() != Swath.COMMAND_PROMPT) {
                MessageBox.exec("You must be at the command prompt.",
                        error,
                        MessageBox.ICON_ERROR,
                        MessageBox.TYPE_OK);
                        return false;
        }
        planets = Swath.sector.planets();
        planet = new Parameter("Which Planet To Attack With");
        for(int i = 0; i < planets.length; i++){
        	 planet.addChoice(i,((Planet)planets[i]).name()+"::"+((Planet)planets[i]).id());
        }
        planet.setCurrentChoice(0);

        registerParam(planet);
        return true;

} /* end initScript */


public boolean runScript() throws Exception {
		startingSector = Swath.getSector(Swath.sector.sector());
		SendSSRadioMessage.exec("Starting Alien Hunter..");
		while(startingSector.sector() == Swath.sector.sector()){
			Class[] events = new Class[1];
			events[0] = ShipEnteredSector.class;
			
			info = (EventIfc)WaitForEvent.exec(events);
			if(info.getClass().equals(ShipEnteredSector.class)){
				shipInfo = (ShipEnteredSector)info;
				victim = shipInfo.getTrader();
				attackingSector = Swath.getSector(shipInfo.getSector());
			}
			
		
		try{
			if(!victim.isRealPlayer()){
				Move.exec(attackingSector);
				ScanSector.exec(ScanSector.HOLO_SCAN);
				int[] sectors = attackingSector.warpSectors();
				if(isAlienInSector(attackingSector)){
					//nothing
				}
				else{
					for(int i = 0; i < attackingSector.warps(); i++){
						Sector temp = Swath.getSector(sectors[i]);
						if(isAlienInSector(temp)){
							attackingSector = temp;
						}
					}
				}
			}
			else{
				UserAlert.exec("Not an alien.",UserAlert.TYPE_WARNING);
			}
		}
		catch(Exception e){
				
		}
		}
		Move.exec(attackingSector);
		Trader[] targets = getAliensInSector();
		if(Swath.sector.planets().length <= 0 && targets.length > 0){
			LaunchGenesisTorpedo.exec(".",Swath.CORPORATE);
		}
		
		boolean isDone = false;
		boolean firstAttack = true;
		while(!isDone){
			isDone = true;
			for(int i = 0; i < targets.length; i++){
				if(!firstAttack){
					if(AttackTrader.exec(null,1) == AttackTrader.ATTACKED){
						isDone = false;
					}
					sleep(2000);
				}
				else{
					if(AttackTrader.exec(null,16000) == AttackTrader.ATTACKED){
						isDone = false;
						firstAttack = false;
					}
					sleep(2000);
				}
			}	
		}
		
        return true;

} /* end runScript */
private boolean isAlienInSector(Sector sector) throws Exception{
	Trader[] check = Swath.getSector(sector.sector()).traders();
	boolean result = false;
		for(int i = 0;i < check.length; i++){
			if(!check[i].isRealPlayer()){
				result = true;
			}
		}
	
	return result;
}
private Trader[] getAliensInSector() throws Exception{
	Trader[] result = null;
	List temp = new ArrayList();
	Trader[] people = Swath.sector.traders();
	for(int i = 0; i < people.length; i++){
		if((!people[i].isRealPlayer())){
			temp.add(people[i]);
		}
	}
	result = new Trader[temp.size()];
	for(int i = 0; i < temp.size(); i++){
		result[i] = (Trader)temp.get(i);
	}
	return result;
}

} /* end class */
