import com.swath.*;
import com.swath.cmd.*;

/**
 * Fig Refresher
 *
 * @author Mind Dagger
 */
public class MDFigRefresh1_1 extends UserDefinedScript {
		
        public String getName() {
            return "MD Fig Refresh 1.1";
        }

        public boolean initScript() throws Exception {
            if (!atPrompt(Swath.COMMAND_PROMPT)) return false;
            
        	return true;
        }

   public boolean runScript() throws Exception {
	   SendSSRadioMessage.exec("(MD Fighter Refresh) "+FighterRefresh.exec()+" sectors gridded.");
	   return true;
	}
   
}
