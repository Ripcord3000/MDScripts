import com.swath.*;
import com.swath.cmd.*;
  
public class MDSurround extends UserDefinedScript {
   
         
public String getName() { return "MD Surround Sector"; }

public boolean initScript() throws Exception {
        
		return true;
} 
	// END INITSCRIPT
public boolean runScript() throws Exception {
	int start = Swath.sector.sector();
	EnterComputer.exec();
	ChangeSettings.exec(ChangeSettings.SPACE);
	LeaveComputer.exec();
	FastSurround.exec();
	SendSSRadioMessage.exec("(MD Surround) Surrounded Sector "+start+".");
	return true;
}
	

}
 /* end class */
