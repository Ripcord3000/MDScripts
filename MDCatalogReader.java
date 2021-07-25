import com.swath.*;
import com.swath.cmd.*;

public class MDCatalogReader extends UserDefinedScript {
       
        
public String getName() { return "MD Catalog Reader"; }

public boolean initScript() throws Exception { 
		return true;
} /* end initScript */


public boolean runScript() throws Exception {	
	EnterComputer.exec();
	ReadCatalog.exec();
	LeaveComputer.exec();
	return true;
	
}
}