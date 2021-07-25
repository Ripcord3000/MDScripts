import com.swath.*;
/**
 Author MD
 */
public class FastSDPhoton extends UserDefinedCommand {
	private int target;
	
	public String getName() {
		// Return the name of the command
		return "Fast SD Photon";
	}

	public boolean initCommand() throws Exception {
//		if(!atPrompt(Swath.STARDOCK_PROMPT)){
//			throw new Exception("Must be at Stardock Prompt.");
//		}
		if(Swath.ship.photonMissiles() <= 0){
			throw new Exception("Must have at least 1 Photon Missle.");
		}
		return true;
	}

	public void startCommand() throws Exception {
		sendString("qcpy"+target+RETURN_KEY+"q"+RETURN_KEY);
	}

	public void endCommand(boolean finished) throws Exception {
		
	}
	
	public void onText(String buffer, String text) throws Exception {
		
	}

	public void onEvent(EventIfc event) throws Exception {
//		// Here you can receive and process incoming events
//		printTrace("onEvent('" + event.getClassName() + "')");
		
	}
	public static void exec(int sector) throws Exception	{
		//Gets fuel from port in sector
		FastSDPhoton cmd = new FastSDPhoton();
		cmd.initInstance();
		cmd.target = sector;
		cmd.execInstance();
	}
	public static void exec(Sector sector) throws Exception	{
		//Gets fuel from planet in sector
		FastSDPhoton cmd = new FastSDPhoton();
		cmd.initInstance();
		cmd.target = sector.sector();
		cmd.execInstance();
	}
}
