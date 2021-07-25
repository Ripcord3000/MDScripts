import com.swath.*;

/**
 Fast Attack attacks first valid target in sector as quickly as possible.
 */
public class FastAttack extends UserDefinedCommand {
	
	private int result;
	private int emptyShipCount; 
	private String[] real;
	private int fake;
	private boolean isStopped;
	private int figs;
	private int maxAttack;
	public String getName() {
		// Return the name of the command
		return "Fast Attack";
	}

	public boolean initCommand() throws Exception {
		emptyShipCount = TargetingDaemon.emptyShipsTotal;
		real = TargetingDaemon.realTradersInOrder;
		fake = TargetingDaemon.fakeTradersInOrder.length;
		figs = Swath.ship.fighters();
		return true;
	}

	public void startCommand() throws Exception {
		do{
			attack();
		}while(figs > 0 && !isStopped);
		if(!atPrompt(Swath.COMMAND_PROMPT)){
			sendString(RETURN_KEY);
		}
		
	}

	public void endCommand(boolean finished) throws Exception {
		setResult(new Integer(result));
	}
	public void attack() throws Exception {
		String attackString = "";
		attackString += ("a");
		boolean isFound = false;
		if(figs > 0){
			if(!Swath.sector.isFedSpace() && Swath.sector.beaconMessage() != null){
				attackString += "n";
			}
		}
		if(figs > 0 && (emptyShipCount+fake+real.length) > 0){
			for(int i = 0; i < emptyShipCount+fake; i++){
				attackString += ("n");
			}
			int c = 0;
			while(c < real.length && !isFound){
				Trader temp = Swath.getTrader(real[c]);
				if((Swath.sector.isFedSpace() && temp.alignment() > 0 && temp.experience() < 1000) || (temp.corporation() != null && temp.corporation().isYourCorporation()) || temp.relation() == Swath.ALLIED){
					attackString += ("n");
				}
				else{
					isFound = true;
					attackString += ("y");
					
				}
				c++;
			}
		}
		else{
			isStopped = true;
		}
		if(isFound){
			if(figs < maxAttack){
				attackString += (figs+RETURN_KEY);
				figs = figs-figs;
			}
			else{
				attackString += (maxAttack+RETURN_KEY);
				figs = figs-maxAttack;
			}
			
		}
		else{
			isStopped = true;
		}
		sendString(attackString);

		
		

	}
	public void onText(String buffer, String text) throws Exception {
		// Incoming text is sent to this method when it arrives.
		// The text parameter only contains the new text that arrived
		// and the buffer contains all text received so far.
		// The buffer can be cleared or updated using the setTextBuffer method.
		
		
	}

	public void onEvent(EventIfc event) throws Exception {
//		// Here you can receive and process incoming events
//		printTrace("onEvent('" + event.getClassName() + "')");
		
	}
	public static int exec(int maxAttack) throws Exception	{
		// This is the static method that will make it possible to use
		// this command in a user defined script just like using any of
		// the given SWATH commands.
		// Several different exec methods could be defined if needed.

		FastAttack cmd = new FastAttack();
		cmd.initInstance();
		if(maxAttack <= 0){
			cmd.maxAttack = 9999;
		}
		else{
			cmd.maxAttack = maxAttack;
		}
		return ((Integer)cmd.execInstance()).intValue();
		//cmd.maxAttack = maxFigAttack;
		
	}
	
}
