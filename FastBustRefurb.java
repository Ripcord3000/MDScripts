import com.swath.*;
/**
 Busts Planets
 Author MD
 */
public class FastBustRefurb extends UserDefinedCommand {
	private int number;

	private boolean isDone;
	public String getName() {
		// Return the name of the command
		return "Fast Bust Refurb";
	}
	

	public boolean initCommand() throws Exception {
		
		
		return true;
	}

	public void startCommand() throws Exception {
		sendString("h a "+number+RETURN_KEY+" t "+number+RETURN_KEY+" q ");
	}

	public void endCommand(boolean finished) throws Exception {
		setResult(new Boolean(isDone));
	}
	
	public void onText(String buffer, String text) throws Exception {
		
		
		
	}

	public void onEvent(EventIfc event) throws Exception {
//		// Here you can receive and process incoming events
//		printTrace("onEvent('" + event.getClassName() + "')");
		
	}
	
	public static boolean exec(int number) throws Exception	{
		// This is the static method that will make it possible to use
		// this command in a user defined script just like using any of
		// the given SWATH commands.
		// Several different exec methods could be defined if needed.

		FastBustRefurb cmd = new FastBustRefurb();
		cmd.initInstance();
		cmd.number = number;
		return ((Boolean)cmd.execInstance()).booleanValue();
	}
	
}
