import com.swath.*;
//Command to photon from citadel to given sector.
//Author Mind Dagger

public class CitadelPhoton extends UserDefinedCommand {
	private int s1;
	
	public String getName() {
		return "Citadel Photon";
	}

	public boolean initCommand() throws Exception {
		// Initialisation of the command is done in this method.
		// All parameters should be created and registered here.
		// If something goes wrong, throw a CommandException or return false.
		
		// Check that we are at the correct prompt
		if (!atPrompt(Swath.CITADEL_PROMPT)) {
			throw new CommandException(this, "Not at correct prompt");
		}
		if(Swath.ship.photonMissiles() <= 0){
			throw new CommandException(this,"No Photon Missles.");
		}
		return true;
	}

	public void startCommand() throws Exception {
		sendString("cpy"+s1+RETURN_KEY+"q");
	}

	public void endCommand(boolean finished) throws Exception {
		if(atPrompt(Swath.COMPUTER_PROMPT)){
			sendString("q");
		}
		
	}

	public void onText(String buffer, String text) throws Exception {
		// Incoming text is sent to this method when it arrives.
		// The text parameter only contains the new text that arrived
		// and the buffer contains all text received so far.
		// The buffer can be cleared or updated using
		// the skipBufferText or setTextBuffer methods.
		// Tools.TextRange incomingText;
	}

	public void onEvent(EventIfc event) throws Exception {
		
	}
	public static void exec(int sector) throws Exception	{
		CitadelPhoton cmd = new CitadelPhoton();
		cmd.initInstance();
		cmd.s1 = sector;
		cmd.execInstance();
	}
}
