import com.swath.*;
import com.swath.cmd.*;

import java.util.*;

public class MDGrid1_56 extends UserDefinedScript {
	private Parameter output;
	private List allSectors;
	private Parameter tooManySectorFigs;
	private Parameter fuel;
	private int startingSector;
	private Sector lastVisited;
	private Sector nextDestination;
	private Sector currentVisited;
	private boolean nextMoveSafe;
	protected Parameter maxFigAttack;
	protected Parameter stealth;
	protected Parameter dodgePhoton;
	protected Parameter figs;
	protected Parameter mines;
	protected Parameter limpets;
	protected Parameter beacon;
	protected Parameter beaconYes;
	private Parameter noCIM;
	private int remember;
	protected List memory = new ArrayList();
	private boolean safeAllAround;
	
	
	public String getName() {
		// Return the name of the script
		return "MD Gridder 1.56";
	}

	public boolean initScript() throws Exception {
		// Check that we are at the correct prompt
		if (!atPrompt(Swath.COMMAND_PROMPT)) return false;
		
		lastVisited = Swath.sector;
		nextDestination = null;
		currentVisited = Swath.sector;
		nextMoveSafe = false;
		
		output = new Parameter("Output Should Go To");
		output.setType(Parameter.CHOICE);
		output.addChoice(0,"Swath Console");
		output.addChoice(1,"SubSpace Radio");
		output.addChoice(2,"None");
		output.setCurrentChoice(0);
		
		fuel = new Parameter("Main Sector Fuel Source");
		fuel.setType(Parameter.CHOICE);
		fuel.addChoice(0,"Port: "+Swath.sector.portName());
		Planet[] planets = Swath.sector.planets();
		for(int i = 0;i < planets.length;i++){
			fuel.addChoice(planets[i].id(),"Planet #"+planets[i].id()+" "+planets[i].name());
		}
		fuel.setCurrentChoice(0);
		
		
		tooManySectorFigs = new Parameter("Min. Sector Figs To Avoid");
		tooManySectorFigs.setType(Parameter.INTEGER);
		tooManySectorFigs.setInteger(1000);
		
		maxFigAttack = new Parameter("Max Ship Fig Attack:");
		maxFigAttack.setType(Parameter.INTEGER);
		maxFigAttack.setInteger(60000);
		
		figs = new Parameter("Fighters Placed:");
		figs.setType(Parameter.INTEGER);
		figs.setInteger(1);
		
		mines = new Parameter("Mines Placed: (if avail)");
		mines.setType(Parameter.INTEGER);
		mines.setInteger(1);
		
		limpets = new Parameter("Limpets Placed: (if avail)");
		limpets.setType(Parameter.INTEGER);
		limpets.setInteger(2);
		
		stealth = new Parameter("Stealth Mode?");
		stealth.setType(Parameter.BOOLEAN);
		stealth.setBoolean(false);
		
		dodgePhoton = new Parameter("Dodge Photon?");
		dodgePhoton.setType(Parameter.BOOLEAN);
		dodgePhoton.setBoolean(false);

		beacon = new Parameter("Beacon Text");
		beacon.setType(Parameter.STRING);
		String b = "";
		if(Swath.you.corporation() != null){
			b = Swath.you.corporation().name()+" Territory";
		}
		else{
			b = Swath.you.name()+"'s Territory";
		}
		beacon.setString(b);
		
		beaconYes = new Parameter("Drop Beacon?");
		beaconYes.setType(Parameter.BOOLEAN);
		beaconYes.setBoolean(false);

		noCIM = new Parameter("Skip Info Download? (less accurate)");
		noCIM.setType(Parameter.BOOLEAN);
		noCIM.setBoolean(false);
	
		
		registerParam(output);
		registerParam(fuel);
		registerParam(tooManySectorFigs);
		registerParam(maxFigAttack);
		registerParam(figs);
		registerParam(mines);
		registerParam(limpets);
		registerParam(stealth);
		registerParam(dodgePhoton);
		registerParam(beaconYes);
		registerParam(beacon);
		registerParam(noCIM);
		
		return true;
	}

	public boolean runScript() throws Exception {
		JettisonCargo.exec();
		remember = Swath.sector.sector();
		checkOutTraderShip();
		checkOutStartingSector();
		startingSector = Swath.sector.sector();
		findAllSafeFuelGridPoints();
		findNextFuelGridPoint();
		gotoFuelGridPoint();
		//Must be at destination or script has ended before this point.
		if(Swath.ship.fuel() < Swath.ship.holds()){
			refuel();
		}
		currentVisited = nextDestination;
		//Check Surroundings
		while(!allSectors.isEmpty()){
			ScanSector.exec(ScanSector.HOLO_SCAN);
			ScanSector.exec(ScanSector.DENSITY_SCAN);
			int next = decideNextSector();
			if(next > 0){
				if(nextMoveSafe){
					remember = Swath.sector.sector();
					FastMove.exec(next,maxFigAttack.getInteger(),figs.getInteger(),mines.getInteger(),limpets.getInteger(),false);
					dropBeacon();
				}
				else{
					remember = Swath.sector.sector();
					rememberAttacking(Swath.getSector(next).ftrOwner());
					if(dodgePhoton.getBoolean()){
						while(!atPrompt(Swath.COMMAND_PROMPT)){
							
						}
						FastMove.exec(next,maxFigAttack.getInteger(),0,0,0,true);
						sleep(500);
						ScanSector.exec(ScanSector.DENSITY_SCAN);
						ScanSector.exec(ScanSector.HOLO_SCAN);
					}
					if(anomalySafe(Swath.getSector(next)) && dodgePhoton.getBoolean()){
						FastMove.exec(next,maxFigAttack.getInteger(),figs.getInteger(),mines.getInteger(),limpets.getInteger(),false);
						dropBeacon();
					}
					
				}
			}
			else{
				lastVisited = currentVisited;
				goHome();
				if(Swath.ship.fuel() < Swath.ship.holds()){
					refuelAtHome();	
				}
				findNextFuelGridPoint();
				gotoFuelGridPoint();
				if(Swath.ship.fuel() < Swath.ship.holds()){
					refuel();
				}
			}
		}
		return true;
	}
	
private void dropBeacon() throws Exception {
	if(Swath.ship.beacons() > 0 && beaconYes.getBoolean() && !Swath.sector.isFedSpace()){
		if(Swath.sector.beaconMessage() != null && !Swath.sector.beaconMessage().equalsIgnoreCase(beacon.getString())){
			try{
				ReleaseBeacon.exec(beacon.getString());
			}
			catch(Exception g){
				
			}
		}
		else if(Swath.sector.beaconMessage() == null){
			try{
				ReleaseBeacon.exec(beacon.getString());
			}
			catch(Exception g){
				
			}
		}
	}
}

private void goHome() throws Exception {
	try{
		TransWarp.exec(startingSector);
	}
	catch(Exception e){
		TransWarp.exec(currentVisited);
		refuel();
		TransWarp.exec(startingSector);
	}
	
}
private void refuelAtHome() throws Exception {
	if(fuel.getCurrentChoice() == 0){
		refuel();
	}
	else{
		Land.exec(fuel.getCurrentChoice());
		TakeLeaveProducts.exec(Swath.ship.emptyHolds(),0,0);
		LiftOff.exec();
	}
}

private int decideNextSector() throws Exception {
		Sector[] possible = Swath.getSectors(Swath.sector.warpSectors());
		nextMoveSafe = false;
		safeAllAround = true;
		int result = -1;
		boolean isStillLooking = true;
		for(int i = 0; i < possible.length; i++){
			if(possible[i].fighters() > 0){
				safeAllAround = false;
			}
		}
		if(safeAllAround){
			FastSurround.exec();
		}
		for(int i = 0; i < possible.length; i++){
			List maybe = new ArrayList();
			if(!possible[i].isFedSpace() && possible[i].density() <= 0 && !possible[i].isAvoided()){
				maybe.add(possible[i]);
			}
			if(maybe.size() > 0){
				isStillLooking = false;
				nextMoveSafe = true;
				result = ((Sector)maybe.get(new Random().nextInt(maybe.size()))).sector();
			}
		}
		if(isStillLooking){
			List maybe = new ArrayList();
			for(int i = 0; i < possible.length; i++){
				if(!possible[i].isFedSpace() && anomalySafe(possible[i]) && possible[i].fighters() <= 0 && !possible[i].isAvoided()){
					if(!checkForShieldedPlanet(possible[i]) && !checkForTraders(possible[i])  && !checkForTraders(possible[i])){
						maybe.add(possible[i]);
					}
				}
			}
			if(maybe.size() > 0){
				isStillLooking = false;
				nextMoveSafe = true;
				result = ((Sector)maybe.get(new Random().nextInt(maybe.size()))).sector();
			}
		}
//		if aggressive
		if(isStillLooking && !stealth.getBoolean()){
			//Enemy fighters, no limpets
			List maybe = new ArrayList();
			for(int i = 0; i < possible.length; i++){
				if(possible[i].fighters() < tooManySectorFigs.getInteger() && Swath.sector.oneWays()[i] == Sector.BOTH_WAY && possible[i].fighters() > 0 && remember != possible[i].sector() && anomalySafe(possible[i]) && !(possible[i].ftrOwner().isYou()||possible[i].ftrOwner().isYourCorporation()) && !possible[i].isAvoided()){
					if(!checkForShieldedPlanet(possible[i])  && !checkForTraders(possible[i]) && !haveRecentlyAttacked(possible[i].ftrOwner())){
						maybe.add(possible[i]);
					}
					
				}
			}
			if(maybe.size() > 0){
				isStillLooking = false;
				nextMoveSafe = false;
				result = ((Sector)maybe.get(new Random().nextInt(maybe.size()))).sector();
				output("Attacking sector, no limpets owned by "+Swath.getSector(result).ftrOwner().toString());
			}
			
		}
		if(isStillLooking && !stealth.getBoolean()){
			//Enemy fighters, with possible limpets
			List maybe = new ArrayList();
			for(int i = 0; i < possible.length; i++){
				if(possible[i].fighters() < tooManySectorFigs.getInteger() && Swath.sector.oneWays()[i] == Sector.BOTH_WAY && possible[i].fighters() > 0 && remember != possible[i].sector() && !(possible[i].ftrOwner().isYou()||possible[i].ftrOwner().isYourCorporation()) && !possible[i].isAvoided()){
					if(!checkForTraders(possible[i]) && !checkForShieldedPlanet(possible[i]) && !haveRecentlyAttacked(possible[i].ftrOwner())){
						maybe.add(possible[i]);
					}
					
				}
			}
			if(maybe.size() > 0){
				isStillLooking = false;
				nextMoveSafe = false;
				result = ((Sector)maybe.get(new Random().nextInt(maybe.size()))).sector();
				output("Attacking sector, some limpets owned by "+Swath.getSector(result).ftrOwner().toString());
			}
			
		}

		if(isStillLooking){
			List maybe = new ArrayList();
			for(int i = 0; i < possible.length; i++){
				if(!possible[i].isFedSpace() && remember != possible[i].sector() && anomalySafe(possible[i]) && possible[i].fighters() >= 0 && (possible[i].ftrOwner().isYou()||possible[i].ftrOwner().isYourCorporation()) && !possible[i].isAvoided()){
					if(!checkForShieldedPlanet(possible[i]) && !checkForTraders(possible[i])){
						maybe.add(possible[i]);
					}
				}
			}
			if(maybe.size() > 0){
				isStillLooking = false;
				nextMoveSafe = true;
				result = ((Sector)maybe.get(new Random().nextInt(maybe.size()))).sector();
			}
			
		}
		
		return result;
	}
	private void clearMemory() throws Exception {
		memory = new ArrayList();
	}
	private void rememberAttacking(Identity who) throws Exception {
		if(who.isCorporation()){
			memory.add(who);
		}
		else{
			if(who.getCorporation() == null){
				memory.add(who);
			}
			else{
				memory.add(who.getCorporation());
			}
		}
		if(!memory.contains(currentVisited)){
			memory.add(currentVisited);
		}
	}
	private boolean haveRecentlyAttacked(Identity who) throws Exception {
		boolean result = false;
		if(who.isCorporation()){
			result = memory.contains(who);
		}
		else{
			if(who.getCorporation() == null){
				result = memory.contains(who);
			}
			else{
				result = memory.contains(who.getCorporation());
			}
		}
		return result;
	}
	
	private boolean checkForShieldedPlanet(Sector possible) throws Exception {
		boolean result = false;
		if(possible.planets().length > 0){
			int p = 0;
			boolean isRunning = true;
			while(isRunning && p < possible.planets().length){
				if(possible.planets()[p].level() >= 5){
					isRunning = false;
					result = true;
					output("Shielded planet avoided in sector "+possible+"..");
				}
				p++;
			}
		}
		return result;
	}
	private boolean checkForTraders(Sector possible) throws Exception {
		if(possible.traders().length > 0){
			output(possible.traders()[0].name()+" avoided in sector "+possible.sector()+" in a "+possible.traders()[0].ship().type()+".");
		}
		return possible.traders().length > 0;
	}
	private void refuel() throws Exception {
		DisplayCurrentInfo.exec(true);
		//Refuel
		try{
			Trade.exec(Swath.ship.emptyHolds(),0,0);
		}
		catch(Exception e){
			UpgradePort.exec(Swath.FUEL_ORE,26);
			try{
				Trade.exec(Swath.ship.emptyHolds(),0,0);
			}
			catch(Exception f){
				
			}
		}
	}
	private void gotoFuelGridPoint() throws Exception {
		while(!Swath.sector.equals(nextDestination) || allSectors.size() <= 0){
			try{
				TransWarp.exec(nextDestination);
			}
			catch(Exception e){
				allSectors.remove(nextDestination);
				findNextFuelGridPoint();
			}
		}
		if(allSectors.size() <= 0){
			throw new ScriptException("Ran out of sectors to visit..");
		}
	}
	private void findNextFuelGridPoint() throws Exception {
		clearMemory();
		while((nextDestination = ((Sector)allSectors.get(new Random().nextInt(allSectors.size())))).equals(lastVisited) || memory.contains(nextDestination)){
			//try for a new sector
		}	
	}
	private void checkOutStartingSector() throws Exception {
		EnterComputer.exec();
		PortReport.exec(Swath.sector);
		LeaveComputer.exec();
		if(fuel.getCurrentChoice() == 0 && Swath.sector.portInfo()[Swath.FUEL_ORE] != Sector.SELLING){
			throw new ScriptException("Starting Sector Must Be A Safe Fuel Grid Point..");
		}
		if(fuel.getCurrentChoice() > 0){
			refuelAtHome();
			if(!(Swath.getPlanet(fuel.getCurrentChoice()).productAmounts()[Swath.FUEL_ORE] > Swath.ship.holds())){
				throw new ScriptException("Not Enough Fuel On Planet..");
			}
		}
		if(Swath.sector.fighters() <= 0 || !(Swath.sector.ftrOwner().isYou() || Swath.sector.ftrOwner().isYourCorporation())){
			throw new ScriptException("You must place fighters that you own in the starting sector..");
		}
		if(fuel.getCurrentChoice() == 0){
			refuel();
		}
	}
		
	private void checkOutTraderShip() throws Exception {
		DisplayCurrentInfo.exec(true);
//		if(Swath.ship.fuel() != Swath.ship.holds()){
//			throw new ScriptException("Fill up the holds with fuel..");
//		}
		if(Swath.ship.transWarpDrive() == Ship.NO_TWD){
			throw new ScriptException("Ship MUST have transwarp drive..");
		}
		if(Swath.ship.fighters() < 100){
			throw new ScriptException("Put at least 100 fighters on your vessel..");
		}
		if(!Swath.ship.hasPlanetScanner()){
			throw new ScriptException("Ship MUST have planet scanner..");
		}
		if(!Swath.ship.hasHoloScanner()){
			throw new ScriptException("Ship MUST have holoscanner..");
		}
		if(Swath.you.credits() < 100000){
			throw new ScriptException("Please carry at least 100,000 credits..");
		}
	}
	private boolean anomalySafe(Sector aSector) throws Exception{
		return !aSector.anomaly() || (aSector.limpetMines() > 0 
				&& (aSector.limpetOwner().isYou() || aSector.limpetOwner().isYourCorporation()));
	}
	private void findAllSafeFuelGridPoints() throws Exception{
		if(!noCIM.getBoolean()){
			output("Downloading all recent port and sector data..");
			CIMDownload.exec(true,true);
		}
		
		Tools.SectorSearchParameters pass1  = new Tools.SectorSearchParameters();
		pass1.setFighterOwner(Swath.you.getCorporation());
		pass1.setPortType(7);
		pass1.setFighterAmount(true,1);
		
		Tools.SectorSearchParameters pass2  = new Tools.SectorSearchParameters();
		pass2.setFighterOwner(Swath.you.getCorporation());
		pass2.setPortType(3);
		pass2.setFighterAmount(true,1);
		
		Tools.SectorSearchParameters pass3  = new Tools.SectorSearchParameters();
		pass3.setFighterOwner(Swath.you.getCorporation());
		pass3.setPortType(4);
		pass3.setFighterAmount(true,1);
		
		Tools.SectorSearchParameters pass4  = new Tools.SectorSearchParameters();
		pass4.setFighterOwner(Swath.you.getCorporation());
		pass4.setPortType(5);
		pass4.setFighterAmount(true,1);
		List throwAway = new ArrayList();
		
		output("Searching known ports..");
		int[] p1,p2,p3,p4;
		allSectors = null;
		output("Starting pass 1...");
		p1 = Tools.findSectors(pass1);
		output("Pass 1 completed.. "+p1.length+" fuel sectors found.");
		output("Starting pass 2...");
		p2 = Tools.findSectors(pass2);
		output("Pass 2 completed.. "+p2.length+" fuel sectors found.");
		output("Starting pass 3...");
		p3 = Tools.findSectors(pass3);
		output("Pass 3 completed.. "+p3.length+" fuel sectors found.");
		output("Starting pass 4...");
		p4 = Tools.findSectors(pass4);
		output("Pass 4 completed.. "+p4.length+" fuel sectors found.");
		output("Consolidating sectors..");
		
		allSectors = new ArrayList();
		int count = 0;
		for(int i = 0; i < p1.length; i++){
			allSectors.add(Swath.getSector(p1[i]));
			if(count >= 100){
				output(""+(int)(((double)allSectors.size()/(double)(p1.length+p2.length+p3.length+p4.length))*100)+" % completed.");
				count = 0;
			}
			count++;
		}
		for(int i = 0; i < p2.length; i++){
			allSectors.add(Swath.getSector(p2[i]));
			if(count >= 100){
				output(""+(int)(((double)allSectors.size()/(double)(p1.length+p2.length+p3.length+p4.length))*100)+" % completed.");
				count = 0;
			}
			count++;
		}
		for(int i = 0; i < p3.length; i++){
			allSectors.add(Swath.getSector(p3[i]));
			if(count >= 100){
				output(""+(int)(((double)allSectors.size()/(double)(p1.length+p2.length+p3.length+p4.length))*100)+" % completed.");
				count = 0;
			}
			count++;
		}
		for(int i = 0; i < p4.length; i++){
			allSectors.add(Swath.getSector(p4[i]));
			if(count >= 100){
				output(""+(int)(((double)allSectors.size()/(double)(p1.length+p2.length+p3.length+p4.length))*100)+" % completed.");
				count = 0;
			}
			count++;
		}
		output("Finding Invalid Sectors..");
		Iterator itr2 = allSectors.iterator();
		count = 0;
		while(itr2.hasNext()){
			Sector temp = (Sector)itr2.next();
			if(temp.isFedSpace()){
				throwAway.add(temp);
			}
			else if(!(temp.ftrOwner().isYou() || temp.ftrOwner().isYourCorporation())){
				throwAway.add(temp);
			}
			else if(temp.limpetMines() > 0 && !(temp.limpetOwner().isYou() || temp.limpetOwner().isYourCorporation())){
				throwAway.add(temp);
				
			}
			else if(temp.armidMines() > 0 && !(temp.limpetOwner().isYou() || temp.limpetOwner().isYourCorporation())){
				throwAway.add(temp);
				
			}
			else if(temp.busted() != null){
				throwAway.add(temp);
				
			}
			else if(Tools.getDistance(startingSector,temp.sector(),true) > Swath.ship.oneShipTransWarpRange()){
				throwAway.add(temp);
				output("Removing sector "+temp.sector()+". (Distance too far: "+Tools.getDistance(startingSector,temp.sector(),true)+" hops)");
			}
			else if(temp.portAmounts()[Swath.FUEL_ORE] < Swath.ship.holds()){
				throwAway.add(temp);
				
			}
			if(count%100.0 == 0){	
				output(""+(int)(((double)count/(double)allSectors.size())*100)+" % completed.");
			}
			count++;
		}
		output(allSectors.size()+" total Fuel Grid Points found before removing bad sectors.");
		output("Removing invalid sectors..");
		Iterator throwAwayItr = throwAway.iterator();
		while(throwAwayItr.hasNext()){
			allSectors.remove((Sector)throwAwayItr.next());
		}
		allSectors.remove(Swath.getSector(startingSector));
		output(allSectors.size()+" total safe Fuel Grid Points found atfer removing bad sectors.");
		if(allSectors.size() <= 5){
			throw new ScriptException("Not enough fuel sectors known to run gridder..");
		}
	}
	private void output(String message) throws Exception{
		String m = message;
		if(output.getCurrentChoice() == 0){
			UserAlert.exec(m,UserAlert.TYPE_INFORMATION);
		}
		else if(output.getCurrentChoice() == 1){
			SendSSRadioMessage.exec(m);
		}
		else{
			//No Output
		}
	}
	
	public void endScript(boolean finished) {
		// Do some clean up here if necessary.
		// Remember: In Java you don't need to free any memory
		// since all memory is garbage collected when not used.
	}
}
