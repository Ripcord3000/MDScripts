import com.swath.*;
import com.swath.cmd.*;

import java.util.*;
/**
 *
 */
public class FillUniverse2_2 extends UserDefinedScript {
	private Parameter figs;
	private Parameter limpets;
	private Parameter mines;
	private Parameter maxFigs;
	private Parameter start;
	private Parameter end;
	private Parameter avoidTraders;
	private Parameter avoidFigs;
	
	
	public String getName() {
		// Return the name of the script
		return "MD's FillUniverse 2.2";
	}

	public boolean initScript() throws Exception {
		// Initialisation of the script is done in this method.
		// All parameters should be created and registered here.
		// If something goes wrong, return false.

		// Check that we are at the correct prompt
		if (!atPrompt(Swath.COMMAND_PROMPT)) return false;

		
		figs = new Parameter("# Of Figs To Drop");
		figs.setType(Parameter.INTEGER);
		figs.setInteger(10);
		
		limpets = new Parameter("# Of Limpets To Drop");
		limpets.setType(Parameter.INTEGER);
		limpets.setInteger(2);
		
		mines = new Parameter("# Of Mines To Drop");
		mines.setType(Parameter.INTEGER);
		mines.setInteger(1);
		
		maxFigs = new Parameter("Max Fighter Attack");
		maxFigs.setType(Parameter.INTEGER);
		maxFigs.setInteger(60000);
		
		avoidFigs = new Parameter("Min Figs To Avoid");
		avoidFigs.setType(Parameter.INTEGER);
		avoidFigs.setInteger(50000);
		
		avoidTraders = new Parameter("Avoid Traders?");
		avoidTraders.setType(Parameter.BOOLEAN);
		avoidTraders.setBoolean(true);
		
		start = new Parameter("Starting Sector");
		start.setType(Parameter.INTEGER);
		start.setInteger(1);
		
		end = new Parameter("Ending Sector");
		end.setType(Parameter.INTEGER);
		end.setInteger(Swath.main.sectors());
		
				
		
		registerParam(figs);
		registerParam(mines);
		registerParam(limpets);
		registerParam(maxFigs);
		registerParam(avoidFigs);
		registerParam(avoidTraders);
		registerParam(start);
		registerParam(end);
		
		return true;
	}

	public boolean runScript() throws Exception {
		Vector universe = new Vector();
		//ShowDeployedFighters.exec();
		for(int i = start.getInteger(); i <= end.getInteger();i++){
		
			Sector temp = Swath.getSector(i);
			
			if(!temp.isAvoided()&&!temp.partOfMajorSpaceLane()&&!temp.isFedSpace()&&!temp.isStarDock()&&!(temp.ftrOwner().isCorporation()||temp.ftrOwner().isYou())){
				universe.add(temp);
				UserAlert.exec((int)(((double)i/(end.getInteger()-start.getInteger()))*100.0)+"% loading complete..",UserAlert.TYPE_INFORMATION);
				
			}
			
		}
		
			    Sector temp = null;
				Iterator itr = universe.iterator();
				Sector[] course = null;
				while(itr.hasNext() && (Swath.ship.fighters() >= 50000)){
					try{
						temp = (Sector)itr.next();
						EnterComputer.exec();
						course = PlotCourse.exec(Swath.sector,temp);
						LeaveComputer.exec();
						boolean noEnter = false;
						if(course.length > 0){
							int i = 0;
							while(i < course.length && !noEnter){
								ScanSector.exec(ScanSector.HOLO_SCAN);	
								Sector next = Swath.getSector(course[i].sector());
								if(next.fighters() > avoidFigs.getInteger() && !(next.ftrOwner().isYou() || next.ftrOwner().isYourCorporation())){
									noEnter = true;
								}
								else if(checkForShieldedPlanet(next)){
									noEnter = true;
								}
								else if(avoidTraders.getBoolean() && next.traders().length > 0){
									noEnter = true;
								}
								if(!noEnter){
									FastMove.exec(next.sector(),maxFigs.getInteger(),figs.getInteger(),mines.getInteger(),limpets.getInteger(),false);	
									i++;
									universe.remove(temp);
								}
								universe.remove(next);
								itr = universe.iterator();
							}
							
								
						}
					}
					catch(Exception e){
						if(atPrompt(Swath.COMPUTER_PROMPT)){
							LeaveComputer.exec();
						}
						UserAlert.exec("Can't get to sector "+temp.sector()+e,UserAlert.TYPE_INFORMATION);
						universe.remove(temp);
						itr = universe.iterator();
					}
					
				}
			
		

		return true;
	}

	private boolean checkForShieldedPlanet(Sector possible) throws Exception {
		boolean result = false;
		if(possible.planets().length > 0){
			int p = 0;
			boolean isRunning = true;
			while(isRunning && p < possible.planets().length){
				if(possible.planets()[p].level() >= 5 && !(possible.planets()[p].owner().isYou()||possible.planets()[p].owner().isYourCorporation())){
					isRunning = false;
					result = true;
				}
				p++;
			}
		}
		return result;
	}
	
}
