import com.swath.*;
//Command to warp a planet to a sector next to the given sector.
//Will pick the first available sector.
//Author Mind Dagger

public class AdjacentPlanetWarp extends UserDefinedCommand {
	private int[] n;
	private int s1;
	private Sector p = null;
	private boolean f;
	
	public String getName() {
		return "Adjacent Planet Warp";
	}

	public boolean initCommand() throws Exception {
		// Initialisation of the command is done in this method.
		// All parameters should be created and registered here.
		// If something goes wrong, throw a CommandException or return false.
		//printTrace("initCommand()");

		// Check that we are at the correct prompt
		while(!atPrompt(Swath.CITADEL_PROMPT)) {
			//throw new CommandException(this, "Not at correct prompt");
		}
		return true;
	}

	public void startCommand() throws Exception {
		n = Swath.getSector(s1).warpSectors();
		int[] o = Swath.getSector(s1).oneWays();
		f = false;
		int c = 0;
		while(!f && c < n.length){
			p = Swath.getSector(n[c]);
			if((p.ftrOwner().isYourCorporation() || p.ftrOwner().isYou() && o[c] != Sector.ONE_WAY)){
				sendString("p"+p.sector()+RETURN_KEY+"y");
				f = true;
			}
			c++;
		}
		
	}

	public void endCommand(boolean finished) throws Exception {
		if(!f){
			throw new Exception("Did Not Planet Warp, No Adjacent Fighter.");
		}
	}

	public void onText(String buffer, String text) throws Exception {
		// Incoming text is sent to this method when it arrives.
		// The text parameter only contains the new text that arrived
		// and the buffer contains all text received so far.
		// The buffer can be cleared or updated using
		// the skipBufferText or setTextBuffer methods.
		//Tools.TextRange incomingText;
	}

	public void onEvent(EventIfc event) throws Exception {
		
	}
	public static void exec(int sector) throws Exception	{
		AdjacentPlanetWarp cmd = new AdjacentPlanetWarp();
		cmd.initInstance();
		cmd.s1 = sector;
		cmd.execInstance();
	}
}
