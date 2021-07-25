import com.swath.*;
import com.swath.cmd.*;

import java.util.*;

/**
 * Dana's Automatic Planet Trader 2.5
 *
 * @author Mind Dagger
 */
public class PlanetTrader2_51 extends UserDefinedScript {
		private Parameter output;
		private List allSectors;
		public final static int PORT_MAX = 65530;
		private Parameter planet;
		private Parameter noCIM;
		private Parameter xBB;
		private Parameter xxB;
		private Parameter xBx;
		private Parameter minProduct;
        private Planet[] planets;
        private Planet currentPlanet;
        private int firstSector;
        
        private int creditsStart;
        private int sectorCount = 0;
        private int organicsStart;
        private int equipmentStart;
        
        public String getName() {
            return "Dana's Planet Trader 2.51";
        }

        public boolean initScript() throws Exception {
            if (!atPrompt(Swath.COMMAND_PROMPT)) return false;
                 
            planets = Swath.sector.planets();
            planet = new Parameter("Planet With Product To Sell");
            for(int i=0;i < planets.length; i++)
            {
               	planet.addChoice(i, planets[i].toString());
            }
            planet.setCurrentChoice(0);
		
			minProduct = new Parameter("BUY Port Minimum Product");
			minProduct.setType(Parameter.INTEGER);
			minProduct.setInteger(1000);
		
			noCIM = new Parameter("Skip Info Download? (less accurate)");
			noCIM.setType(Parameter.BOOLEAN);
			noCIM.setBoolean(false);
		
			xBB = new Parameter("Visit xBB Ports");
			xBB.setType(Parameter.BOOLEAN);
			xBB.setBoolean(true);
		
			xxB = new Parameter("Visit xBx Ports");
			xxB.setType(Parameter.BOOLEAN);
			xxB.setBoolean(true);
		
			xBx = new Parameter("Visit xxB Ports");
			xBx.setType(Parameter.BOOLEAN);
			xBx.setBoolean(true);
		
			registerParam(planet);
        	registerParam(minProduct);
        	registerParam(xBB);
        	registerParam(xBx);
        	registerParam(xxB);
        	registerParam(noCIM);
           
        	return true;
        }

   public boolean runScript() throws Exception {
        
                     
		currentPlanet = (Planet)(planets[planet.getCurrentChoice()]);
		firstSector = Swath.sector.sector();
		allSectors = new ArrayList();
		DisplayCurrentInfo.exec();
        creditsStart = Swath.you.credits();
        
        findAllBuyingPorts();	
     		 
		Iterator itr = allSectors.iterator();
		Sector tempSector;
		Land.exec(currentPlanet);
		LiftOff.exec();
		organicsStart = currentPlanet.productAmounts()[Swath.ORGANICS];
        equipmentStart = currentPlanet.productAmounts()[Swath.EQUIPMENT];
		int[] products = currentPlanet.productAmounts();
		
		while((itr.hasNext()) && (products[Swath.ORGANICS] > 1000 || products[Swath.EQUIPMENT] > 1000))
		{
				tempSector = (Sector)itr.next();
				Land.exec(currentPlanet);
				EnterCitadel.exec();
				try{
					PlanetWarp.exec(tempSector.sector());
					sectorCount++;
					
				}
				catch(Exception e){
					output("Sector "+tempSector+" does not have friendly fighters in it.  Skipping this sector.");
				}
				LeaveCitadel.exec();
				LiftOff.exec();
				DisplaySector.exec();
				EnterComputer.exec();
				PortReport.exec(Swath.sector);
				LeaveComputer.exec();
				if(Swath.sector.portStatus() == Sector.PORT_AVAILABLE){
					PlanetaryTrade.exec(currentPlanet,0,Swath.sector.portAmounts()[Swath.ORGANICS],Swath.sector.portAmounts()[Swath.EQUIPMENT]);
					currentPlanet = Swath.getPlanet(currentPlanet.id());
					products = currentPlanet.productAmounts();
					if(products[Swath.FUEL_ORE] < 20000){
						DisplaySector.exec();
						EnterComputer.exec();
						PortReport.exec(Swath.sector.sector());
						LeaveComputer.exec();
						while(Swath.sector.portAmounts()[Swath.FUEL_ORE] > 500 && Swath.sector.portInfo()[Swath.FUEL_ORE] == Sector.SELLING && products[Swath.FUEL_ORE] < 50000){
							Trade.exec(Swath.ship.holds(),0,0);
							Land.exec(currentPlanet);
							TakeLeaveProducts.exec(-1*Swath.ship.fuel(),0,0);
							LiftOff.exec();
							currentPlanet = Swath.getPlanet(currentPlanet.id());
							products = currentPlanet.productAmounts();
							
						}
					}
				}
			
			
		}
		Land.exec(currentPlanet);
		EnterCitadel.exec();
		try{
			PlanetWarp.exec(firstSector);
		}
		catch (Exception e){
			SendSSRadioMessage.exec("Can not return to starting sector, no fighters there.");
		}
		LeaveCitadel.exec();
		LiftOff.exec();
		output("\nSectors visited: "+sectorCount+"\nCredits Made: "+(Swath.you.credits()-creditsStart)+"\nOrganics Sold: "+(organicsStart-currentPlanet.productAmounts()[Swath.ORGANICS])+"\nEquipment Sold: "+(equipmentStart-currentPlanet.productAmounts()[Swath.EQUIPMENT]));
		output("Sectors visited: "+sectorCount+",Credits Made: "+(Swath.you.credits()-creditsStart));
        return true;
	}
   private void findAllBuyingPorts() throws Exception{
	if(!noCIM.getBoolean()){
		output("Downloading all recent port data..");
		CIMDownload.exec(false,true);
		output("Downloading current fighter deployment..");
		ShowDeployedFighters.exec();
	}
	
	
	Tools.PortSearchParameters xBBpass  = new Tools.PortSearchParameters();
	xBBpass.setMaxAmount(Swath.EQUIPMENT,PORT_MAX);
	xBBpass.setMinAmount(Swath.EQUIPMENT,minProduct.getInteger());
	xBBpass.setMaxAmount(Swath.ORGANICS,PORT_MAX);
	xBBpass.setMinAmount(Swath.ORGANICS,minProduct.getInteger());
	xBBpass.setPortOption(Swath.EQUIPMENT,true);
	xBBpass.setPortOption(Swath.ORGANICS,true);
	
	Tools.PortSearchParameters xxBpass  = new Tools.PortSearchParameters();
	xxBpass.setMaxAmount(Swath.EQUIPMENT,PORT_MAX);
	xxBpass.setMinAmount(Swath.EQUIPMENT,minProduct.getInteger());
	xxBpass.setPortOption(Swath.EQUIPMENT,true);
	
	Tools.PortSearchParameters xBxpass  = new Tools.PortSearchParameters();
	xxBpass.setMaxAmount(Swath.ORGANICS,PORT_MAX);
	xxBpass.setMinAmount(Swath.ORGANICS,minProduct.getInteger());
	xxBpass.setPortOption(Swath.ORGANICS,true);
	

	
	output("Searching known ports..");
	int[] xBBResults,xBxResults,xxBResults;
	xBBResults = null;
	xBxResults = null;
	xxBResults = null;
	
	List throwAway = new ArrayList();
	allSectors = null;
	if(xBB.getBoolean()){
		output("Starting xBB pass...");
		xBBResults = Tools.findPorts(xBBpass,true,-1);
		output("Pass completed.. "+xBBResults.length+" Buying sectors found.");		
	}
	if(xxB.getBoolean()){
		output("Starting xxB pass...");
		xxBResults = Tools.findPorts(xxBpass,true,-1);
		output("Pass completed.. "+xxBResults.length+" Buying sectors found.");	
	}
	if(xBx.getBoolean()){
		output("Starting xBx pass...");
		xBxResults = Tools.findPorts(xBxpass,true,-1);
		output("Pass completed.. "+xBxResults.length+" Buying sectors found.");	
	}
	
	allSectors = new ArrayList();
	if(xBB.getBoolean()){
		for(int i = 0; i < xBBResults.length; i++){
			if(!allSectors.contains(Swath.getSector(xBBResults[i]))){
				allSectors.add(Swath.getSector(xBBResults[i]));
			}
		}
	}
	if(xxB.getBoolean()){
		for(int i = 0; i < xxBResults.length; i++){
			if(!allSectors.contains(Swath.getSector(xxBResults[i]))){
				allSectors.add(Swath.getSector(xxBResults[i]));
			}
		}
	}
	if(xBx.getBoolean()){
		for(int i = 0; i < xBxResults.length; i++){
			if(!allSectors.contains(Swath.getSector(xBxResults[i]))){
				allSectors.add(Swath.getSector(xBxResults[i]));
			}
		}
	}
	
	
	Iterator itr2 = allSectors.iterator();
	while(itr2.hasNext()){
		Sector temp = (Sector)itr2.next();
		if(temp.isFedSpace()){
			throwAway.add(temp);
		}
		else if((temp.fighters() > 0 && !(temp.ftrOwner().isYou() || temp.ftrOwner().isYourCorporation())) || temp.fighters() <= 0){
			throwAway.add(temp);
		}
		else if(temp.busted() != null){
			throwAway.add(temp);
		}
		
		
	}
	output(allSectors.size()+" total Buying Ports found before removing bad sectors.");
	output("Removing invalid sectors..");
	Iterator throwAwayItr = throwAway.iterator();
	while(throwAwayItr.hasNext()){
		allSectors.remove((Sector)throwAwayItr.next());
	}
	output(allSectors.size()+" total good Buying Ports found.");
	
}
private void output(String message) throws Exception{
	SendSSRadioMessage.exec(message);
	
}
}
