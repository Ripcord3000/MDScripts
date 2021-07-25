import com.swath.*;
import com.swath.cmd.*;


public class FindEnemy extends UserDefinedScript {
        protected String title = "Find Enemy";
        protected String error = "Script Error";
        protected Parameter max;
        protected Parameter min;
        protected Parameter type;
        protected Parameter maxFigs;
        private Bubble[] bubbles;
        protected String possibles;
       
        
public String getName() { return title; }
public String getDescription() {
	return "This script searches all known bubbles of the specified type and size and\n"+
	       "then checks all the ones it can not get to.  In order for data to be correct\n"+
	       "ZTM must be completed and SWATH bubbles search must be done for the size\n"+
	       "bubbles you are looking for.  08/15/05 - Mind Dagger";
}
public boolean initScript() throws Exception {

        if(Swath.main.prompt() != Swath.COMMAND_PROMPT) {
                MessageBox.exec("You must be at the command prompt.",
                        error,
                        MessageBox.ICON_ERROR,
                        MessageBox.TYPE_OK);
                        return false;
        }
        
        min = new Parameter("Min Size To Search For");
        min.setType(Parameter.INTEGER);
        min.setInteger(3);
        
        max = new Parameter("Max Size To Search For");
        max.setType(Parameter.INTEGER);
        max.setInteger(8);
        
        type = new Parameter("Bubble or Tunnel?");
        type.setType(Parameter.CHOICE);
        type.addChoice(Bubble.TYPE_NORMAL,"Bubble");
        type.addChoice(Bubble.TYPE_DEAD_END,"Tunnel");
        type.setCurrentChoice(Bubble.TYPE_NORMAL);
        
        maxFigs = new Parameter("Max Fighter Attack");
        maxFigs.setType(Parameter.INTEGER);
        maxFigs.setInteger(Swath.ship.shipCategory().maxFightersPerAttack());
        
        registerParam(type);
        registerParam(min);
        registerParam(max);
        registerParam(maxFigs);
        
        return true;

} /* end initScript */


public boolean runScript() throws Exception {
		possibles = new String();
	 	bubbles = Swath.getBubbles(type.getCurrentChoice());
        for(int i = 0; i < bubbles.length; i++){
        	if(bubbles[i].size() >= min.getInteger() && bubbles[i].size() <= max.getInteger()){
        			int[] sectors = bubbles[i].sectors();
        			for(int j = 0; j < sectors.length; j++){
        				Sector temp = Swath.getSector(sectors[j]);
        				int s;
        				if(temp.warps() == 1 && (temp.fighters() <= 0 || (temp.fighters() > 0 && !(temp.ftrOwner().isYou() || temp.ftrOwner().isYourCorporation())))) {
        					if(Swath.ship.etherProbes() > 0){
        						s = LaunchEtherProbe.exec(sectors[j]);
        					}
        					else if(Swath.you.sector() == Swath.main.stardock() && Swath.sector.portStatus() != Sector.PORT_DESTROYED){
        						LandOnStarDock.exec();
    	        				BuyItems.exec(BuyItems.ETHER_PROBES,10);
    	        				LeaveStarDock.exec();
    	        				s = LaunchEtherProbe.exec(sectors[j]);
        					}
        					else{
        						throw new Exception("Out Of Ether Probes And Not At Stardock");
        					}
        					if(s < 0){
        						SendSSRadioMessage.exec("Fighters destroyed probe in sector "+ -1*s+" trying to get to sector "+sectors[j]+".");
        						possibles += (sectors[j]+",");
        					}
        				}
        			}
        		
        	}
        }
		SendSSRadioMessage.exec("Possible sectors to check: "+possibles);
		Sector[] course;
		int[] destination;
		String[] input;
		possibles = possibles.trim();
		input = possibles.split(",");
		destination = new int[input.length];
		for(int i = 0; i < input.length; i++){
			destination[i] = (Tools.getInteger(input[i]));
		}
		for(int i = 0; i < destination.length; i++){
			EnterComputer.exec();
			course = PlotCourse.exec(Swath.sector,Swath.getSector(destination[i]));
			LeaveComputer.exec();
			if(course.length > 0){
				boolean noEnter = false;
				int j = 0;
				while(j < course.length && !noEnter){
					ScanSector.exec(ScanSector.HOLO_SCAN);	
					Sector next = Swath.getSector(course[j].sector());
					
					if(next.fighters() > maxFigs.getInteger()*9  && !(next.ftrOwner().isYou() || next.ftrOwner().isYourCorporation())){
						noEnter = true;
						if(next.sector() != destination[i]){
							SendSSRadioMessage.exec(next.fighters()+" enemy figs in sector "+next.sector()+". (Sector I was checking..)");
						}
						else{
							SendSSRadioMessage.exec(next.fighters()+" enemy figs in sector "+next.sector()+". Too many for ship. (Enroute to "+destination[i]+")");
						}
					}
					else if(checkForShieldedPlanet(next)){
						noEnter = true;
						SendSSRadioMessage.exec("Shielded planet in sector "+next.sector()+".");
						if(next.sector() != destination[i]){
							SendSSRadioMessage.exec("Could not make it to check out sector "+destination[i]+".");
						}
					}
					if(next.sector() == destination[i] && Swath.getSector(destination[i]).traders().length > 0){
						SendSSRadioMessage.exec("Trader "+Swath.getSector(destination[i]).traders()[0].name()+" in destination sector "+destination[i]);
					}
					if(!noEnter){
						FastMove.exec(course[j].sector(),maxFigs.getInteger(),1,0,0,false);	
						j++;
					}
					
		}
				
      
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
} /* end runScript */

	
 /* end class */
