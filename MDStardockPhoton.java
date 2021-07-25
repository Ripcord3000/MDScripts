import com.swath.*;
import com.swath.cmd.*;
import com.swath.event.ShipEnteredSector;

/**
 * @author Mind Dagger
 */
public class MDStardockPhoton extends UserDefinedScript {
     
        private int[] warps;
        private int attack;
        public String getName() {
            return "MD Stardock Photon";
        }

        public boolean initScript() throws Exception {   
            if(!atPrompt(Swath.COMMAND_PROMPT)){
            	throw new Exception("Must start at Command Prompt.");
            }
            if(atPrompt(Swath.COMMAND_PROMPT) && !Swath.sector.isStarDock()){
            	throw new Exception("Must be in StarDock Sector.");
            }
        	return true;
        }

        public boolean runScript() throws Exception {
        	
        	DisplaySector.exec();
        	//ShipEnteredSector info;
			warps = Swath.sector.warpSectors();
			while(true){
				LandOnStarDock.exec();
				if(Swath.ship.photonMissiles() <= 0){
	            	BuyItems.exec(BuyItems.PHOTON_MISSILES,1);
	            }
	            while(!isOutsideSD(attack = ((ShipEnteredSector)WaitForEvent.exec(ShipEnteredSector.class)).getSector())){
	            	//nothing
	            }
	            firePhoton();
	    		// FastSDPhoton.exec(attack);
	            
    			if(Swath.ship.hasHoloScanner() && Swath.you.turns() > 0){
    				ScanSector.exec(ScanSector.HOLO_SCAN);
    			}
            }
            
        	//return true;
        }
        private boolean isOutsideSD(int sector){
        	boolean found = false;
			int i = 0;
        	while(!found && i < warps.length){
				if(warps[i] == sector){
					found = true;
				}
				i++;
			}
        	return found;
        }
        private void firePhoton() throws Exception {
        	if(Swath.ship.photonMissiles() > 0){
        		SendString.exec("qcpy"+attack+SendString.RETURN_KEY+"q");
        		try{
        			WaitForPrompt.exec(Swath.COMMAND_PROMPT,1000);
        		}
        		catch(Exception t){
        			if(!atPrompt(Swath.COMMAND_PROMPT)){
        				SendString.exec("q");
        			}
        		}

        	}
        }
        }
