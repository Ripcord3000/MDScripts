import com.swath.*;
//Command to surround a sector with fighters and limpets.
//Author Mind Dagger

public class FighterRefresh extends UserDefinedCommand {
	
	private int count;
//	private boolean start;
//	private boolean stop;
//	
	public String getName() {
		return "Fighter Refresh";
	}
	public boolean initCommand() throws Exception {
		// Initialisation of the command is done in this method.
		// All parameters should be created and registered here.
		// If something goes wrong, throw a CommandException or return false.
		
		// Check that we are at the correct prompt
		if (!atPrompt(Swath.COMMAND_PROMPT)) {
			throw new CommandException(this, "Not at correct prompt");
		}
		count = 0;
//		start = false;
//		stop = false;
		return true;
	}

	public void startCommand() throws Exception {
		setBufferText("");
		sendString("g");
	}
	public void endCommand(boolean result) throws Exception {
		//Minus 2 to handle 2 instances of r in deployed fighter scan titles.
		setResult(new Integer(count-2));
		
	}
	public void onText(String buffer, String text) throws Exception {
		// Incoming text is sent to this method when it arrives.
		// The text parameter only contains the new text that arrived
		// and the buffer contains all text received so far.
		// The buffer can be cleared or updated using the setTextBuffer method.
	//	Tools.TextRange range;

//		if((range = Tools.findText(buffer,"===========================================================","Command [")) != null){
//			String figs = Tools.getText(buffer,range);
//			int index = 0;
//			while(figs.indexOf("\n", index) >= 0){
//				count++;
//				index = figs.indexOf("\n",index)+1;
//			}
//		}
		
		//int index = 0;
		String copy = buffer;
		setBufferText(buffer.substring(copy.length()));
		String[] s = copy.split("\n");
		for(int i = 0; i < s.length; i++){
			if(s[i].indexOf("r") >= 0){
				count++;
			}
		}
		
			
	}
	public static int exec() throws Exception	{
		FighterRefresh cmd = new FighterRefresh();
		cmd.initInstance();
		return ((Integer)cmd.execInstance()).intValue();
	}
	

}
