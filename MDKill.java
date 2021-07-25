import com.swath.*;
import com.swath.cmd.*;

public class MDKill extends UserDefinedScript {
       
        
public String getName() { return "MD Kill"; }

public boolean initScript() throws Exception { 
	return true;
} /* end initScript */


public boolean runScript() throws Exception {	
	DisplaySector.exec();
	DisplayCurrentInfo.exec(true);
	FastAttack.exec(Swath.ship.shipCategory().maxFightersPerAttack());
	return true;
	}
}