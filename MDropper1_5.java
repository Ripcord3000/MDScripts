import com.swath.*;
import com.swath.event.*;
import com.swath.cmd.*;
  
public class MDropper1_5 extends UserDefinedScript {
        private int as;
        private Parameter isPhotoning;
        private Parameter choice;
        private Parameter returnHome;
        private int startingSector;
	private boolean foundSector;
         
public String getName() { return "MDropper Complete 1.5"; }

public boolean initScript() throws Exception {
        if(Swath.main.prompt() != Swath.CITADEL_PROMPT) {
                throw new ScriptException("You must start in the citadel");
        }
        choice = new Parameter("What Type Of Planet Drop?");
        choice.setType(Parameter.CHOICE);
        choice.addChoice(0,"Plain Planet Drop");
        choice.addChoice(1,"Planet Drop, then Adjacent");
        choice.addChoice(2,"Adjacent Fighter Photon");
        choice.addChoice(3,"Adjacent Limpet Photon");
        choice.addChoice(4,"Adjacent Fig/Limp Photon");
         
        choice.setCurrentChoice(1);
		
        isPhotoning = new Parameter("Photon When Adjacent?");
		isPhotoning.setType(Parameter.BOOLEAN);
		isPhotoning.setBoolean(true);
		
		returnHome = new Parameter("Return Home?");
		returnHome.setType(Parameter.BOOLEAN);
		returnHome.setBoolean(true);
		
		
		registerParam(choice);
		registerParam(returnHome);
		registerParam(isPhotoning);
		
		
		return true;
} 
	// END INITSCRIPT
public boolean runScript() throws Exception {
	startingSector = Swath.sector.sector();
	
	if(choice.getCurrentChoice() == 0 || choice.getCurrentChoice() == 1){
		if(choice.getCurrentChoice() == 0){
			SendSSRadioMessage.exec("Starting MDropper, Standard Planet Drop...");
		}
		else{
			SendSSRadioMessage.exec("Starting MDropper, Planet Drop then Adjacent Photon...");
		}
		while(true){
			DisplayCurrentInfo.exec(true);
			boolean warped = false;
			if(((as = ((ShipEnteredSector)WaitForEvent.exec(ShipEnteredSector.class)).getSector())) != Swath.sector.sector()){
				try{
					PlanetWarp.exec(as);
					warped = true;
					if(returnHome.getBoolean()){
							Thread.sleep(5000);
							SendSSRadioMessage.exec("(MDropper) Going home"+startingSector);
							PlanetWarp.exec(startingSector);
						
					}
				}
				catch(Exception e){
						
					
				}
				if(!warped && choice.getCurrentChoice() == 1){
						try{
								AdjacentPlanetWarp.exec(as);
								if(isPhotoning.getBoolean() && Swath.ship.photonMissiles() > 0){
									CitadelPhoton.exec(as);
								}
								if(returnHome.getBoolean()){
										SendSSRadioMessage.exec("going home"+startingSector);
										sleep(500);
										PlanetWarp.exec(startingSector);
									
								}
						}
						catch(Exception f){
							SendSSRadioMessage.exec(f.toString());
						}
						
					
				}
				//SendString.exec("/");
				//DisplaySector.exec();
				
			}
		}
	}
	else if(choice.getCurrentChoice() == 2){
		SendSSRadioMessage.exec("Starting MDropper, Adjacent Fighter Photon...");
		ShipEnteredSector e;
		DisplaySector.exec();
		while(true){
			SendString.exec("/");
			WaitForPrompt.exec(Swath.CITADEL_PROMPT);
			if((e = ((ShipEnteredSector)WaitForEvent.exec(ShipEnteredSector.class))).getTrader().isRealPlayer() && (e.getSector() != Swath.sector.sector())){
				as = e.getSector();
				adjacentPlanetWarp();
				if(isPhotoning.getBoolean() && Swath.ship.photonMissiles() > 0 && foundSector == true && startingSector != Swath.sector.sector()){
					firePhoton();
				}
				DisplaySector.exec();
				SendString.exec("d");
				if(returnHome.getBoolean() && startingSector != Swath.sector.sector()){
					goHome();
				}
			}
			}
				
			
	}
	else if(choice.getCurrentChoice() == 3){
		SendSSRadioMessage.exec("Starting MDropper, Adjacent Limpet Photon...");
		SendString.exec("/");
		WaitForPrompt.exec(Swath.CITADEL_PROMPT);
		DisplaySector.exec();
		while(true){
			if((as = ((LimpetActivated)WaitForEvent.exec(LimpetActivated.class)).getSector()) != Swath.sector.sector()){
				adjacentPlanetWarp();
				if(isPhotoning.getBoolean() && Swath.ship.photonMissiles() > 0 && foundSector == true && startingSector != Swath.sector.sector()){
					firePhoton();
				}
				DisplaySector.exec();
				SendString.exec("d");
				if(returnHome.getBoolean() && startingSector != Swath.sector.sector()){
					goHome();
				}
			}
			
			
		}
	}
	else if(choice.getCurrentChoice() == 4){
		SendSSRadioMessage.exec("Starting MDropper, Adjacent Fighter And Limpet Photon...");
		DisplaySector.exec();
		boolean shouldAttack = true;
		Class list[] = new Class[2];
		list[0] = LimpetActivated.class;
		list[1] = ShipEnteredSector.class;
			
		while(true){
			DisplayCurrentInfo.exec(true);
			EventIfc event = WaitForEvent.exec(list);
			if(event.isEventClass(LimpetActivated.class)){
				as = ((LimpetActivated)event).getSector();
			}
			else{
				as = ((ShipEnteredSector)event).getSector();
				shouldAttack = ((ShipEnteredSector)event).getTrader().isRealPlayer();
			}
			
			if(as != Swath.sector.sector() && shouldAttack){
				adjacentPlanetWarp();
			}
			if(shouldAttack && isPhotoning.getBoolean() && Swath.ship.photonMissiles() > 0 && foundSector == true ){
				firePhoton();
			}
			DisplaySector.exec();
			if(returnHome.getBoolean() && startingSector != Swath.sector.sector()){
				goHome();
			}
			
		}
	}
	
	return true;
	// END RUNSCRIPT 
}
private void goHome() throws Exception {
	Thread.sleep(5000);
	try{
		WaitForPrompt.exec(Swath.CITADEL_PROMPT,1000);
	}
	catch(Exception t){
		if(Swath.main.prompt() == Swath.COMPUTER_PROMPT){
			LeaveComputer.exec();
		}
		else if(Swath.main.prompt() == Swath.CORP_PROMPT){
			LeaveCorporateMenu.exec();
		}
		else if(Swath.main.prompt() == Swath.PLANET_PROMPT){
			EnterCitadel.exec();
		}
		else{
			SendString.exec(SendString.RETURN_KEY);
		}
	}
	if(startingSector != Swath.sector.sector()){
		SendSSRadioMessage.exec("(MDropper) Going home: "+startingSector);
		PlanetWarp.exec(startingSector);
	}
}
private void firePhoton() throws Exception {
	if(Swath.ship.photonMissiles() > 0){
		SendString.exec("cpy"+as+SendString.RETURN_KEY+"q");
		try{
			WaitForPrompt.exec(Swath.CITADEL_PROMPT,1000);
		}
		catch(Exception t){
			if(!atPrompt(Swath.CITADEL_PROMPT)){
				SendString.exec("q");
			}
		}

	}
					
	
}
private void adjacentPlanetWarp() throws Exception{
	int[] neighborSectors;
	neighborSectors = Swath.getSector(as).warpSectors();
	int[] oneWaySectors = Swath.getSector(as).oneWays();
	Sector possible = null;
	foundSector = false;
	int count = 0;
	while(!foundSector && count < neighborSectors.length){
		possible = Swath.getSector(neighborSectors[count]);
		if((possible.ftrOwner().isYourCorporation() || possible.ftrOwner().isYou() && oneWaySectors[count] != Sector.ONE_WAY)){
			SendString.exec("p"+possible.sector()+SendString.RETURN_KEY+"y");
			foundSector = true;
		}
		count++;
	}
}

}
 /* end class */
