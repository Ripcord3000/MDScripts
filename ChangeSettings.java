import com.swath.*;
/**
 Fast Attack attacks first valid target in sector as quickly as possible.
 */
public class ChangeSettings extends UserDefinedCommand {
	public final static int ALL_KEYS = 1;
	public final static int SPACE = 2;
	private int setTo;
	public String getName() {
		// Return the name of the command
		return "Change Settings";
	}

	public boolean initCommand() throws Exception {
		if(!atPrompt(Swath.COMPUTER_PROMPT)){
			throw new Exception("Must be at Computer Prompt.");
		}
		return true;
	}

	public void startCommand() throws Exception {
		sendString("N");
	}

	public void endCommand(boolean finished) throws Exception {
		
	}
	
	public void onText(String buffer, String text) throws Exception {
		Tools.TextRange range;
		if ((range = ((Tools.findText(buffer, "<Set ANSI and misc settings>","Settings command (?=Help) [Q]")))) != null) {
			String prompt = Tools.getText(buffer, range);
			if((range = (Tools.findText(prompt,"(9) Abort display on keys    -","\n"))) != null){
				String type = Tools.getText(prompt,range);
				if(type.indexOf("ALL KEYS") >= 0 && setTo == SPACE){
					sendString("9");
				}
				else if(type.indexOf("SPACE") >= 0 && setTo == ALL_KEYS){
					sendString("9");
				}
			}
			sendString("Q");
			setBufferText("");
		}
		
	}

	public void onEvent(EventIfc event) throws Exception {
//		// Here you can receive and process incoming events
//		printTrace("onEvent('" + event.getClassName() + "')");
		
	}
	public static void exec(int mode) throws Exception	{
		// This is the static method that will make it possible to use
		// this command in a user defined script just like using any of
		// the given SWATH commands.
		// Several different exec methods could be defined if needed.

		ChangeSettings cmd = new ChangeSettings();
		cmd.setTo = mode;
		cmd.initInstance();
		cmd.execInstance();
	}
	
}
