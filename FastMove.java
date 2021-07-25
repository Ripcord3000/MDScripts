import com.swath.*;
//Command to warp a planet to a sector next to the given sector.
//Will pick the first available sector.
//Author Mind Dagger

public class FastMove extends UserDefinedCommand {
	private int s1;
	private int maxFigAttack;
	private boolean retreat;
	private int layfigs;
	private int layMines;
	private int layLimpets;
	private boolean lawnmower;
	
	public String getName() {
		return "Fast Move";
	}

	public boolean initCommand() throws Exception {
		// Initialisation of the command is done in this method.
		// All parameters should be created and registered here.
		// If something goes wrong, throw a CommandException or return false.
		
		// Check that we are at the correct prompt
		if (!atPrompt(Swath.COMMAND_PROMPT)) {
			throw new CommandException(this, "Not at correct prompt");
		}
		return true;
	}

	public void startCommand() throws Exception {
		String result = "";
		Sector next = Swath.getSector(s1);
		
		int attackWith;
		if(Swath.ship.shipCategory() != null && Swath.ship.shipCategory().offensiveOdds() >= 1){
			attackWith = 9*next.fighters();
		}
		else{
			attackWith = 15*next.fighters();
		}
		result += "m"+s1+RETURN_KEY+" ";
		if(lawnmower || (next.fighters() > 0 && next.ftrType() != Swath.OFFENSIVE_FTRS && !(next.ftrOwner().isYourCorporation() || next.ftrOwner().isYou()))){
			if(!next.isFedSpace()){
				result += " za";
				if(lawnmower){
					result += maxFigAttack;
				}
				else if( attackWith > Swath.ship.fighters() && attackWith < maxFigAttack){
					result = ""+Swath.ship.fighters();
				}
				else if(attackWith >= maxFigAttack ){
					result += maxFigAttack;
				}
				else{
					result += attackWith;
				}
				result += RETURN_KEY+" ";
			}
			else{
				
			}
			
			//handles mines in sector
			if((lawnmower && !next.isFedSpace())|| (next.armidMines() > 0 && !(next.armidOwner().isYou() || next.armidOwner().isYourCorporation()))){
				result += RETURN_KEY+" ";	
			}
			if(layfigs >= 1 && !next.isFedSpace()){
				result += layFigs(layfigs);
			}
			if(layMines > 0 && Swath.ship.armidMines() > 0 && next.armidMines() <= 0 && !next.isFedSpace()){
				if(layMines <= Swath.ship.armidMines()){
					result += layMines(layMines);
				}
				else{
					result += layMines(1);
				}
			}
			if(layLimpets > 0 && Swath.ship.limpetMines() > 0 && next.limpetMines() <= 0 && !next.isFedSpace()){
				if(layLimpets <= Swath.ship.limpetMines()){
					result += layLimpets(layLimpets);
				}
				else{
					result += layLimpets(1);
				}
			}
		}
		else if(next.fighters() > 0){
			//Your own fighters in sector
			if(next.armidMines() > 0 && !(next.armidOwner().isYou() || next.armidOwner().isYourCorporation())){
				result += " "+RETURN_KEY+" ";	
			}
			if(layfigs > Swath.sector.fighters() ){
				result += layFigs(layfigs);
			}
			if(layMines > 0 && Swath.ship.armidMines() > 0 && next.armidMines() <= 0){
				if(layMines <= Swath.ship.armidMines()){
					result += layMines(layMines);
				}
				else{
					result += layMines(1);
				}
			}
			if(layLimpets > 0 && Swath.ship.limpetMines() > 0 && next.limpetMines() <= 0){
				if(layLimpets <= Swath.ship.limpetMines()){
					result += layLimpets(layLimpets);
				}
				else{
					result += layLimpets(1);
				}
			}
		}
		else{
			if(!next.isFedSpace()){
				result += RETURN_KEY;	
				if(next.armidMines() > 0 && !(next.armidOwner().isYou() || next.armidOwner().isYourCorporation())){
					result += " "+RETURN_KEY+" ";	
				}
				if(layfigs > 0){
					result += layFigs(layfigs);
				}
				if(layMines > 0 && Swath.ship.armidMines() > 0 && next.armidMines() <= 0){
					if(layMines <= Swath.ship.armidMines()){
						result += layMines(layMines);
					}
					else{
						result += layMines(1);
					}
				}
				if(layLimpets > 0 && Swath.ship.limpetMines() > 0 && next.limpetMines() <= 0){
					if(layLimpets <= Swath.ship.limpetMines()){
						result += layLimpets(layLimpets);
					}
					else{
						result += layLimpets(1);
					}
				}
			}
		}
		if(retreat){
			if(Swath.sector.armidMines() > 0 && !(Swath.sector.armidOwner().isYourCorporation() || Swath.sector.armidOwner().isYou())){
				result += "<"+RETURN_KEY;
			}
			else{
				result += "<";
			}
		}
		sendString(result);
	}

	public static void exec(int sector,int maxFigAttack) throws Exception	{
		FastMove cmd = new FastMove();
		cmd.initInstance();
		cmd.s1 = sector;
		cmd.maxFigAttack = maxFigAttack;
		cmd.retreat = false;
		cmd.layfigs = 0;
		cmd.layMines = 0;
		cmd.layLimpets = 0;
		
		cmd.execInstance();
	}
	public static void exec(int sector,int maxFigAttack,int layfigs, int layMines, int layLimpets) throws Exception	{
		FastMove cmd = new FastMove();
		cmd.initInstance();
		cmd.s1 = sector;
		cmd.maxFigAttack = maxFigAttack;
		cmd.retreat = false;
		cmd.layfigs = layfigs;
		cmd.layMines = layMines;
		cmd.layLimpets = layLimpets;
		
		cmd.execInstance();
	}
	public static void exec(int sector,int maxFigAttack,int layfigs, int layMines, int layLimpets, boolean retreat) throws Exception	{
		FastMove cmd = new FastMove();
		cmd.initInstance();
		cmd.s1 = sector;
		cmd.maxFigAttack = maxFigAttack;
		cmd.retreat = retreat;
		cmd.layfigs = layfigs;
		cmd.layMines = layMines;
		cmd.layLimpets = layLimpets;
		
		cmd.execInstance();
	}
	public static void exec(int sector,int layfigs, int layMines, int layLimpets, boolean retreat, boolean lawnmow) throws Exception	{
		FastMove cmd = new FastMove();
		cmd.initInstance();
		cmd.s1 = sector;
		if((cmd.maxFigAttack = Swath.ship.shipCategory().maxFightersPerAttack()) <= 0){
			cmd.maxFigAttack = 9999;
		}
		cmd.retreat = retreat;
		cmd.layfigs = layfigs;
		cmd.layMines = layMines;
		cmd.layLimpets = layLimpets;
		cmd.lawnmower = lawnmow;
		
		cmd.execInstance();
	}
	private String layFigs(int numOfFigs) throws Exception{
		String result = new String();
		if(Swath.you.corporation() != null){
			result += "f "+numOfFigs+" "+RETURN_KEY+" c t ";
		}
		else{
			result += "f "+numOfFigs+" "+RETURN_KEY+" t ";
		}
		return result;

	}
	private String layMines(int numOfMines) throws Exception{
		String result = new String();
		if(Swath.you.corporation() != null){
			result += "h 1 "+numOfMines+" "+RETURN_KEY+" c ";
		}
		else{
			result += "h 1 "+numOfMines+" "+RETURN_KEY;
		}
		return result;

	}
	private String layLimpets(int numOfLimpets) throws Exception{
		String result = new String();
		if(Swath.you.corporation() != null){
			result += "h 2 "+numOfLimpets+" "+RETURN_KEY+" c ";
		}
		else{
			result += "h 2 "+numOfLimpets+" "+RETURN_KEY+" ";
		}
		return result;

	}
	
}
