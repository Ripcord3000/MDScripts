import com.swath.*;
//Command to surround a sector with fighters and limpets.
//Author Mind Dagger

public class FastSurround extends UserDefinedCommand {
	private int maxFigAttack;
	private int layfigs;
	private int layLimpets;
	private int sector;
	//private int displayCount;
	private int[] surroundingSectors;
	
	private final static int ONE_WAY = 1;
	private final static int BOTH_WAY = 0;
	private final static int AVOIDED = 2;
	private final static int NOT_AVOIDED = 3;
	private final static int ANOMALY = 4;
	private final static int NO_ANOMALY = 5;
	private int[] oneways;
	private int[] avoided;
	private int[] anomalies;
	private String[] sectors;
	boolean noScanner;
	
	public String getName() {
		return "Fast Surround";
	}
	public boolean initCommand() throws Exception {
		// Initialisation of the command is done in this method.
		// All parameters should be created and registered here.
		// If something goes wrong, throw a CommandException or return false.
		
		// Check that we are at the correct prompt
		if (!atPrompt(Swath.COMMAND_PROMPT)) {
			throw new CommandException(this, "Not at correct prompt");
		}
		//surroundingSectors = Swath.sector.warpSectors();
		sector = Swath.sector.sector();
		//displayCount = 0;
		noScanner = false;
		surroundingSectors = Swath.sector.warpSectors();
		oneways = new int[surroundingSectors.length];
		avoided = new int[surroundingSectors.length];
		anomalies = new int[surroundingSectors.length];
		sectors = new String[surroundingSectors.length];
		return true;
	}

	public void startCommand() throws Exception {
		String input = "d";
		
		if(Swath.ship != null && Swath.ship.hasDensityScanner()){
			input += "sd";
		}
		
		if(Swath.ship != null && Swath.ship.hasHoloScanner()){
			input += "sh";
		}
		else{
			input += "?";
			noScanner = true;
		}
		
		
		input += "cx";
		if(Swath.ship == null || !Swath.ship.hasHoloScanner()){
			input += "q^";
			for(int i = 0; i < surroundingSectors.length; i++){
				input += "f"+surroundingSectors[i]+RETURN_KEY+Swath.sector.sector()+RETURN_KEY;
			}
		}
		else{
			for(int i = 0; i < surroundingSectors.length; i++){
				input += "i"+surroundingSectors[i]+RETURN_KEY;
			}
		}
		input += "q?";
		//input += "'Starting MD Surround.."+RETURN_KEY+"?";
		sendString(input);
		
		//sendString(input);
	}
	public void endCommand(boolean result) throws Exception {
		
		//sendString("'(MD Surround) Sector "+sector+" surrounded."+RETURN_KEY);
	}
	public void onText(String buffer, String text) throws Exception {
		// Incoming text is sent to this method when it arrives.
		// The text parameter only contains the new text that arrived
		// and the buffer contains all text received so far.
		// The buffer can be cleared or updated using the setTextBuffer method.
		Tools.TextRange range;

			
			
		if((range = Tools.findText(buffer,"Relative Density Scan","Computer command [")) != null){
			String info = Tools.getText(buffer, range);
			for(int i = 0; i < surroundingSectors.length; i++){
				String[] data = info.split("\n");
				for(int j = 0; j < data.length; j++){
					if(data[j].indexOf(surroundingSectors[i]+"  ==>") >= 0 && data[j].indexOf("Anom : Yes") >= 0){
						anomalies[i] = ANOMALY;
					}
					else if(data[j].indexOf(surroundingSectors[i]+"  ==>") >= 0 && data[j].indexOf("Anom : No") >= 0){
						anomalies[i] = NO_ANOMALY;
					}
					else{
						anomalies[i] = NO_ANOMALY;
					}
				}
				
			}
		}
		
		
		if ((range = ((Tools.findText(buffer, "<Computer activated>","<Computer deactivated>")))) != null) {
			String info = Tools.getText(buffer, range);
			range = Tools.findText(info,"<List Avoided Sectors>","Computer command [");
			String avoidText = Tools.getText(info,range);
			for(int i = 0; i < surroundingSectors.length; i++){
				if(avoidText.indexOf(" "+surroundingSectors[i]+" ") >= 0 || avoidText.indexOf(" "+surroundingSectors[i]+"\n") >= 0){
					avoided[i] = AVOIDED;
				}
				else{
					avoided[i] = NOT_AVOIDED;
				}
			}
			if(avoidText.indexOf(" "+Swath.sector.sector()+" ") >= 0 || avoidText.indexOf(" "+Swath.sector.sector()+"\n") >= 0){
				throw new CommandException(this,"Can't Run FastSurround From An Avoided Sector.. ("+Swath.sector.sector()+")");
			}
			if(noScanner || (Swath.ship != null && !Swath.ship.hasHoloScanner())){
			}
			else{
				
				String[] data = info.split("\n");
				if(data != null){
					int count = 0;
					for(int i = 0; i < data.length; i++){
						if(data[i].indexOf(" has warps to sector(s) :") < 0){
							data[i] = "";
						}
						else{
							sectors[count] = data[i]+"#";
							count++;
						}
					}
					for(int i = 0; i < sectors.length; i++){
						if(sectors[i] != null){
							if(sectors[i].indexOf(" "+sector+" ") >= 0 || sectors[i].indexOf(sector+"#") >= 0 || sectors[i].indexOf("warps to sector(s) :  "+sector) >= 0){
								oneways[i] = BOTH_WAY;
							}
							else{
								oneways[i] = ONE_WAY;
							}
							
						}
						
					}
				}
				setBufferText("Starting MD Surround..");
				Move();
			}
			
		}
		if((range = Tools.findText(buffer,"FM >",": ENDINTERROG")) != null){
			String info = Tools.getText(buffer, range);
			
			for(int i = 0; i < surroundingSectors.length; i++){
				if(info.indexOf(surroundingSectors[i]+" > "+sector) >= 0){
					oneways[i] = BOTH_WAY;
				}
				else{
					oneways[i] = ONE_WAY;
				}
			}
			setBufferText("Starting MD Surround..");
			Move();
		}
		if((range = Tools.findText(buffer,"Starting MD Surround..","(MD Surround)")) != null){
			printTrace("Here..");
		}
			

			
	}
	public static void exec() throws Exception	{
		FastSurround cmd = new FastSurround();
		cmd.initInstance();
		if(Swath.ship != null && Swath.ship.shipCategory() != null && (cmd.maxFigAttack = Swath.ship.shipCategory().maxFightersPerAttack()) > 0){
			
		}
		else{
			cmd.maxFigAttack = 9999;
		}
		cmd.layfigs = 1;
		cmd.layLimpets = 2;
		cmd.execInstance();
	}
	public static void exec(int maxFigAttack,int layfigs, int layLimpets) throws Exception	{
		FastSurround cmd = new FastSurround();
		cmd.initInstance();
		cmd.maxFigAttack = maxFigAttack;
		cmd.layfigs = layfigs;
		cmd.layLimpets = layLimpets;
		
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
	private String layLimpets(int numOfLimpets) throws Exception{
		String result = new String();
		if(Swath.you.corporation() != null){
			result += "h 2 z"+numOfLimpets+" "+RETURN_KEY+" c q"+RETURN_KEY;
		}
		else{
			result += "h 2 z"+numOfLimpets+" "+RETURN_KEY+" ";
		}
		return result;

	}
	private void Move() throws Exception {
		for(int i = 0; i < surroundingSectors.length; i++){
			Sector next = Swath.getSector(surroundingSectors[i]);
			if(noScanner){
				if(oneways[i] == BOTH_WAY && avoided[i] == NOT_AVOIDED){
					sendString("m"+surroundingSectors[i]+RETURN_KEY+" za");
					if( maxFigAttack > Swath.ship.fighters()){
						sendString(""+Swath.ship.fighters());
					}
					else{
						sendString(maxFigAttack+"");
					}
					sendString(RETURN_KEY+" "+RETURN_KEY+" ");	
					
					if(!next.isFedSpace()){
						if(layfigs >= 1){
							sendString(layFigs(layfigs));
						}
						if(layLimpets > 0 && Swath.ship.limpetMines() > 0){
							if(layLimpets <= Swath.ship.limpetMines() && next.limpetMines() <= 0){
								sendString(layLimpets(layLimpets));
							}
							else{
								sendString(layLimpets(1));
							}
						}
						
					}
					sendString("m"+sector+RETURN_KEY+" "+RETURN_KEY+" ");
				}
					
			}
			else{
				if(oneways[i] == BOTH_WAY && avoided[i] == NOT_AVOIDED && ((!next.isFedSpace() && (next.fighters() <= 0 || !(next.ftrOwner().isYou() || next.ftrOwner().isYourCorporation()) || (anomalies[i] == NO_ANOMALY && Swath.ship.limpetMines() > 0))))){
					sendString("m"+surroundingSectors[i]+RETURN_KEY+" za");
					if( maxFigAttack > Swath.ship.fighters()){
						sendString(""+Swath.ship.fighters());
					}
					else{
						sendString(maxFigAttack+"");
					}
					sendString(RETURN_KEY+" "+RETURN_KEY+" ");	
					
					if(!next.isFedSpace()){
						if(layfigs >= 1){
							sendString(layFigs(layfigs));
						}
						if(layLimpets > 0 && Swath.ship.limpetMines() > 0){
							if(layLimpets <= Swath.ship.limpetMines() && next.limpetMines() <= 0){
								sendString(layLimpets(layLimpets));
							}
							else{
								sendString(layLimpets(1));
							}
						}
						
					}
					sendString("m"+sector+RETURN_KEY+" "+RETURN_KEY+" ");
				}
			}
			
		}
		//sendString("'(MD Surround) Sector "+sector+" surrounded."+RETURN_KEY);
	}
}
