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
public class EaterOfWorlds extends UserDefinedScript {
      
        public String getName() {
                return "Eater Of Worlds";
        }

        public boolean initScript() throws Exception {
                if (!atPrompt(Swath.COMMAND_PROMPT)) return false;
               return true;
        }

        public boolean runScript() throws Exception {
                
        		SendSSRadioMessage.exec("Running Eater Of Worlds Script..");
        		while(Swath.ship.genesisTorpedos() > 0){
        			if(Swath.sector.planets().length < 3){
        				LaunchGenesisTorpedo.exec("Eating Worlds..Yumm!",Swath.CORPORATE);
                	}
        			else{
        				SendMacro.exec("uyn");
        				try{
        					WaitForText.exec("destroyed before you can create one.",100);
        					SendMacro.exec("Eating Worlds..Yumm!"+"^Mc");
        				}
        				catch(Exception e){
        					
        				}
        			}
        		}
        		
        		return true;
        }
        public void endScript(boolean finished){

        }
	
}
