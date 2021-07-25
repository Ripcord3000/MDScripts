import com.swath.*;
import com.swath.cmd.*;
import java.util.*;
/**
 *
 */
public class FillBubble extends UserDefinedScript {
	private Parameter mygate;
	private Parameter figs;
	private Parameter attackType;
	private Parameter teamType;
	
	public String getName() {
		// Return the name of the script
		return "MD's FillBubble 1.0";
	}

	public boolean initScript() throws Exception {
		// Initialisation of the script is done in this method.
		// All parameters should be created and registered here.
		// If something goes wrong, return false.

		// Check that we are at the correct prompt
		if (!atPrompt(Swath.COMMAND_PROMPT)) return false;

		
		mygate = new Parameter("Enterance To Bubble");
		mygate.setType(Parameter.INTEGER);
		figs = new Parameter("Number Of Figs To Drop");
		figs.setType(Parameter.INTEGER);
		attackType = new Parameter("Type Of Fighter To Place");
		attackType.addChoice(0,"Randomize");
		attackType.addChoice(1,"Offensive");
		attackType.addChoice(2,"Defensive");
		attackType.addChoice(3,"Toll");
		attackType.setCurrentChoice(0);
		
		teamType = new Parameter("Corporate or Personal");
		teamType.addChoice(Swath.CORPORATE,"Corporate");
		teamType.addChoice(Swath.PERSONAL,"Personal");
		teamType.setCurrentChoice(0);
		
		registerParam(mygate);
		registerParam(figs);
		registerParam(attackType);
		registerParam(teamType);
		
		return true;
	}

	public boolean runScript() throws Exception {
		Bubble[] mybubble = Swath.getBubbles(Bubble.TYPE_NORMAL);
		Random rand = new Random();
		
		int[] values = new int[3];
		if(attackType.getCurrentChoice() == 0){
			values[0] = Swath.DEFENSIVE_FTRS;
			values[1] = Swath.OFFENSIVE_FTRS;
			values[2] = Swath.TOLL_FTRS;
		}
		else if(attackType.getCurrentChoice() == 1){
			values[0] = Swath.OFFENSIVE_FTRS;
			values[1] = Swath.OFFENSIVE_FTRS;
			values[2] = Swath.OFFENSIVE_FTRS;
		}
		else if(attackType.getCurrentChoice() == 2){
			values[0] = Swath.DEFENSIVE_FTRS;
			values[1] = Swath.DEFENSIVE_FTRS;
			values[2] = Swath.DEFENSIVE_FTRS;
		}
		else{
			values[0] = Swath.TOLL_FTRS;
			values[1] = Swath.TOLL_FTRS;
			values[2] = Swath.TOLL_FTRS;
		}
		
		for (int i=0; i<mybubble.length; i++) {
			int bubblegate[] = mybubble[i].gates();
			if (bubblegate[0] == mygate.getInteger()) {
				int[] bubblesectors = mybubble[i].sectors();
				Move.exec(bubblegate[0]);
				DropTakeFighters.exec(figs.getInteger()+Swath.sector.fighters(), teamType.getCurrentChoice(), values[rand.nextInt(3)]);
                for (int j=0; j < bubblesectors.length; j++) {
					Move.exec(bubblesectors[j]);
					DropTakeFighters.exec(figs.getInteger()+Swath.sector.fighters(), teamType.getCurrentChoice(), values[rand.nextInt(3)]);
				}
			}
		}

		return true;
	}

	public void endScript(boolean finished) {
		// Do some clean up here if necessary.
		// Remember: In Java you don't need to free any memory
		// since all memory is garbage collected when not used.
	}
}
