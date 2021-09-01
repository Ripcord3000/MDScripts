import com.swath.*;
import com.swath.cmd.*;
import java.io.*;
import java.util.*;
import java.text.*;
import javax.swing.*;
import java.awt.*;


//VitoTrade 2/12/03

// this script will WorldTrade Vito style. 
// it has the new string macro functionality, give it a try
// the agressive setting is for when you dont mind walking over enemy
// toll figs, thereby revealing your location to them.
// if unchecked, the script will keep you invisible...
// I recommend you keep fedspace unchecked, the script has logic
// to restore figs at sector 1, but that functionality is broken until
// WaitForText starts returning the entire line...
// post bugs on the swath discussion forum
// and it goes without saying that ansi animation should be turned off....
// as well as all online messages that may interfere with script execution
// ------------------------------------------------------------------------
// important note, there is a crucial array called ugliness[] which keeps
// track of any sectors that we prefer not to visit.. for example if we trade a
// port down we really want to move on rather than come back to that sector.
// the most important function of ugliness is to ensure we do not fall
// into an endless loop where we warp back and forth between useless sectors...
// the script detects your swath game database directory (usually My Documents)
// and saves the ugly file there, as VitoTrade-ugly-<gamename>.txt
// ------------------------------------------------------------------------
// new for this version... 
// #1 speedometer reports credits earned per hour
// #2 holoscan is working
// #3 unattended mode works a lot better
// #4 added more delay fig options, "Vitos favorite" means it drops
// one toll fig in every sector, except for 20 offensive figs in empty sectors,
// thus giving the sector a density of 100 hehehehehehheheheheh
// ------------------------------------------------------------------------
// Thanks to CSG who wrote the exploration WorldTrade script and 
// whose code I learned from and borrowed to make this nonsense.
// Also thanks to Mongoose and his magnificent probehunter2.java,
// from which I learned how to read and write disk files...
// ------------------------------------------------------------------------
// Parameters:
// layfigs       = lay toll fighters as we go?
// unattended    = running unattended (leave unchecked if you are speed trading)
// verbose       = lots of debugging information
// speed         = inline macros (bypasses the swath ai so must run attended)
// fedspace      = allow the ship to enter fedspace and go to sector 1?
// keepalive     = if the script is about to end because it cant find anywhere safe to move,
//		   select this to keepalive by walking over enemy figs rather than ending script.
// agressive     = agressive mode (walks over enemy toll figs)
//		   (if unchecked, this script will keep you invisible to enemy forces)


public class VitoTrade extends UserDefinedScript {
	private Parameter m_verbose;
	private Parameter m_layfigs;
	private Parameter m_unattended;
	private Parameter m_holoscan;
	private Parameter m_speed;
	private Parameter m_fedspace;
	private Parameter m_agressive;
	
	int scred,sexp,salign;
	int lcred,lexp,lalign;
	long stime,ltime;
	
	final JLabel label1 = new JLabel("Credits per hour:");
	final JLabel label2 = new JLabel("$<computing>");

	public boolean runScript() throws Exception {
		//put the parameters into their working storage
		boolean verbose=m_verbose.getBoolean();
		int layfigs=m_layfigs.getCurrentChoice();
		boolean holoscan=m_holoscan.getBoolean();
		boolean unattended=m_unattended.getBoolean();
		boolean speed= m_speed.getBoolean();
 		boolean fedspace = m_fedspace.getBoolean();
		scred = Swath.you.credits();
		sexp = Swath.you.experience();
		salign = Swath.you.alignment();
		//stime = Calendar.getInstance().getTimeInMillis();
		stime = Calendar.getInstance().getTime().getTime();
		PrintTrace.exec("Starting at "+Calendar.getInstance().getTime().toString());
		Sector s;
		boolean success;
		int g,h,i,loopcount,warphighscore,uglylowscore;
		int[] ugliness;
		String str,speedstr,movestring="";

		int whereami = Swath.sector.sector();
		int class0port = 1;
		int loopssinceclass0 = 0;
		//**************************STATUS WINDOW***************************
	
		JPanel panel = Tools.createJPanel();
		JFrame frame = Tools.createJFrame("VitoTrade Status", 1, 1);
     		panel.setLayout(new GridLayout(0, 2));
		panel.add(label1);
		panel.add(label2);

		frame.getContentPane().add(panel);
		frame.pack();
		frame.setResizable(false);
		frame.setVisible(true);
		//*********************************************************************

		String filespec = "VitoTrade-ugly-"+Swath.main.gameName()+".txt";
		filespec = Swath.main.gamePath()+"\\"+filespec;
		if (verbose) {
			PrintText.exec("\nFILESPEC = "+filespec+"\n");
		}
		BufferedReader uglyin;
		BufferedWriter uglyout;
		
		ugliness = new int[Swath.main.sectors()+1];

		
		// load ugly.txt if it exists into memory
		if ( new File(filespec).exists() ) {
			PrintText.exec("\n"+filespec+" found.\n");

			uglyin = new BufferedReader(new FileReader(filespec));
	
			i=0;
			while ((str = uglyin.readLine()) != null) {
		
				try {
					ugliness[i]=Integer.parseInt(str);					
				}
				catch (Exception e) {
					ugliness[i]=0;
					PrintText.exec("ugly parse error: setting ugliness["+i+"]=0.\n");
				}
				if (i>1) {
					if (ugliness[i] > 2) {
						ugliness[i] = 2;
					}
				}
				++i;
			}
			uglyin.close();
		}
		else {
			PrintText.exec("\n"+filespec+" not found, creating Ugliness...");
			Arrays.fill(ugliness,0);
			//there is no sector 0 so in ugliness terms.. it is very ugly
			ugliness[0] = 999;
			//we only want to go to sector 1 if we are forced to ...
			ugliness[1] = 20;
			//set the URL
			if (verbose) {
				PrintText.exec("\nCreating "+filespec+"\n");
			}
		}

		//DisplayCurrentInfo.exec();
		loopcount=0;
		while ((Swath.you.turns() > 50) || (Swath.you.turns() == 0)) {
			status();
			++loopcount;
			speedstr = "";
			//if we like to lay fighters
			if (layfigs > 0) {
				//wait for a command prompt
				while (Swath.main.prompt() != Swath.COMMAND_PROMPT) {
					Thread.sleep(25);
				}
				//and if theres no fighter in sector (or its a personal)
				if ((Swath.sector.fighters() < 1) || ((Swath.you.corp() > 0) && !(Swath.sector.ftrOwner().isYourCorporation()))) {
					//and if this is not fedspace
					if (!(Swath.sector.spaceName().equals("The Federation."))) {
						//and if we have fighters onboard
						if (Swath.ship.fighters() > 0) {
							//then lay a toll fighter down
							if (speed) {
								switch (layfigs) {
									case 1:
										SendString.exec("F"+Math.max(Swath.sector.fighters(),1)+"\rCTD");
										break;
									case 2:
										if (Swath.getSector(whereami).portStatus() == Sector.PORT_AVAILABLE) {
											SendString.exec("F"+Math.max(Swath.sector.fighters(),1)+"\rCTD");
										}
										break;
									case 3:
										if (Math.IEEEremainder(loopcount,2) == 0) {
											SendString.exec("F"+Math.max(Swath.sector.fighters(),1)+"\rCTD");
										}
										break;
									case 4:
										if (Swath.getSector(whereami).density() == 0) {
											SendString.exec("F"+Math.max(Swath.sector.fighters(),20)+"\rCoD");
										}
										else {
											SendString.exec("F"+Math.max(Swath.sector.fighters(),1)+"\rCTD");
										}
										break;
								}
							}
							else{
								switch (layfigs) {
									case 1:
										DropTakeFighters.exec(Math.max(Swath.sector.fighters(),1),Swath.CORPORATE,Swath.TOLL_FTRS);
										break;
									case 2:
										if (Swath.sector.portStatus() == Sector.PORT_AVAILABLE) {
											DropTakeFighters.exec(Math.max(Swath.sector.fighters(),1),Swath.CORPORATE,Swath.TOLL_FTRS);
										}
										break;
									case 3:
										if (Math.IEEEremainder(loopcount,2) == 0) {
											DropTakeFighters.exec(Math.max(Swath.sector.fighters(),1),Swath.CORPORATE,Swath.TOLL_FTRS);
										}
										break;
									case 4:
										if (Swath.sector.density() == 0) {
											DropTakeFighters.exec(Math.max(Swath.sector.fighters(),20),Swath.CORPORATE,Swath.OFFENSIVE_FTRS);
										}
										else {
											DropTakeFighters.exec(Math.max(Swath.sector.fighters(),1),Swath.CORPORATE,Swath.TOLL_FTRS);
										}
										break;
								}
								
							}
							while (Swath.main.prompt() != Swath.COMMAND_PROMPT) {
								Thread.sleep(25);
							}
							if (!(speed)) {
								DisplaySector.exec();
							}						
						}
					}
				}
			}

	
			g = Swath.sector.portClass();
			int tre=0;
			int tro=0;
			int trf=0;
			int e=Swath.ship.equipment();
			int o=Swath.ship.organics();
			int f=Swath.ship.fuel();
			h=Swath.ship.holds();

	
			if (unattended) {
				if ((Swath.getSector(whereami).portClass() != 0) && (Swath.getSector(whereami).portClass() != 9) && Swath.getSector(whereami).portStatus() == Sector.PORT_AVAILABLE) {
				
					if (speed) {
						long t1 = -2;
						if (Swath.getSector(whereami).lastPortUpdate() != null) {
							t1 = Swath.getSector(whereami).lastPortUpdate().getTime();
						}
						
						SendString.exec("CR"+whereami+"\rQ");
						WaitForText.exec("Computer deactivated");
						long t2 = -3;
						while (t2 < t1) {
							if (Swath.getSector(whereami).lastPortUpdate() != null) {
								t2 = Swath.getSector(whereami).lastPortUpdate().getTime()-1;
							}
							Thread.sleep(25);
						}			
						
					}
					else {
					 	EnterComputer.exec();
						PortReport.exec(whereami);
						LeaveComputer.exec();
					}
				} 
			}

			int sec=whereami;

			int[] pamt=Swath.getSector(whereami).portAmounts();
			
			if (pamt[0] == -1) {
				//take a chance (set Swath to kill toll figs and you wont have this problem)
				pamt[0]=9999;
				pamt[1]=9999;
				pamt[2]=9999;
			}
			if (verbose) {
				PrintText.exec("\nPort amounts:\npamt[fuel]="+pamt[0]+"\npamt[org]="+pamt[1]+"\npamt[equ]="+pamt[2]+"\n");
			}
			//here is the logic that figures out how much to trade of each product
			//if we are in a sector with a port.
			g = Swath.getSector(whereami).portClass();
			
			switch (g){

				case 0: 

					//this mess maxes out your ship (within available credits) anytime it
					//arrives at a class 0 port. if you dont like it you will have to 
					//modify or delete this routine
					SendString.exec("PTY");
					String[][] class0waittext = { {"A  Cargo holds","wish to buy","Command"},{"B  Fighters","wish to buy","Command"},{"C  Shield","wish to buy","Command"} };
				
					str = WaitForText.exec(class0waittext[0]);
					int maxbuyholds,maxbuyfigs,maxbuyshields;
					try {
						maxbuyholds = Integer.parseInt(str.substring(55).trim());
					}
					catch (Exception excep) {
						maxbuyholds = 0;
						PrintText.exec("Unable to figure out maxbuyholds, skipping...");
					}
					Thread.sleep(1000);
					if (maxbuyholds > 5) {
						maxbuyholds = maxbuyholds - 5;
					}
					SendString.exec("A"+maxbuyholds+"\rY");
			
					str = WaitForText.exec(class0waittext[1]);
					try {
						maxbuyfigs = Integer.parseInt(str.substring(55).trim());
					}
					catch (Exception excep) {
						maxbuyfigs = 0;
						PrintText.exec("Unable to figure out maxbuyfigs, skipping...");
					}
					if (maxbuyfigs > 50) {
						maxbuyfigs = maxbuyfigs - 50;
					}
					Thread.sleep(1000);	
					SendString.exec("B"+maxbuyfigs+"\r");
	
					str = WaitForText.exec(class0waittext[2]);
					try {
						maxbuyshields = Integer.parseInt(str.substring(55).trim());
					}
					catch (Exception excep) {
						maxbuyshields = 0;
						PrintText.exec("Unable to figure out maxbuyshields, skipping...");
					}
					if (maxbuyshields > 50) {
						maxbuyshields = maxbuyshields - 50;
					}
					Thread.sleep(1000);
					SendString.exec("C"+maxbuyshields+"\rQ");
					WaitForPrompt.exec(Swath.COMMAND_PROMPT);
					DisplayCurrentInfo.exec();
					break;

				case 1: //BBS
					if (verbose) {
						PrintText.exec("\nCase 1\n");
					}
					if (f-1 < pamt[0]) {
						trf = -f;
					}
					else {
						trf = -pamt[0];
						ugliness[sec]=ugliness[sec]+1;
					}
					if (o-1 < pamt[1]) {
						tro = -o;
					}
					else {
						tro = -pamt[1];
						ugliness[sec]=ugliness[sec]+1;
					}
					if (h - (e + o + f + tro + trf) < pamt[2]) {
						tre = h - (e + o + f + tro + trf);
					}
					else {
						tre = pamt[2] - 1;
						ugliness[sec]=ugliness[sec]+1;
					}
					if (tre == -1) {
						tre = 0;
					}
					break;

				case 2: //BSB
					if (verbose) {
						PrintText.exec("\nCase 2\n");
					}
					if (f-1 < pamt[0]) {
						trf = -f;
					}
					else {
						trf = -pamt[0];
						ugliness[sec]=ugliness[sec]+1;
					}
					if (e-1 < pamt[2]) {
						tre = -e;
					}
					else {
						tre = -pamt[2];
						ugliness[sec]=ugliness[sec]+1;
					}
					if (h - (e + o + tre + f + trf) < pamt[1]) {
						tro = h - (e + o + tre + f + trf);
					}
					else {
						tro = pamt[1] - 1;
						ugliness[sec]=ugliness[sec]+1;
					}
					if (tro == -1) {
						tro = 0;
					}

					break;

				case 3: //SBB
					if (verbose) {
						PrintText.exec("\nCase 3\n");
					}
					if (o-1 < pamt[1]) {
						tro = -o;
					}
					else {
						tro = -pamt[1];
						ugliness[sec]=ugliness[sec]+1;
					}
					if (e-1 < pamt[2]) {
						tre = -e;
					}
					else {
						tre = -pamt[2];
						ugliness[sec]=ugliness[sec]+1;
					}
					if (h - (e + o + tre + f + tro) < pamt[0]) {
						trf = h - (e + o + tre + f + tro);
					}
					else {
						trf = pamt[0] - 1;
						ugliness[sec]=ugliness[sec]+1;
					}
					if (trf == -1) {
						trf = 0;
					}


					break;

				case 4: //SSB
					if (verbose) {
						PrintText.exec("\nCase 4\n");
					}
					if (e-1 < pamt[2]) {
						tre = -e;
					}
					else {
						tre = -pamt[2];
						ugliness[sec]=ugliness[sec]+1;
					}
				
					if (h - (e + o + f + tre) < pamt[1]) {
						tro = h - (e + o + f + tre);
					}
					else {
						tro = pamt[1] - 1;
						ugliness[sec]=ugliness[sec]+1;
					}
					if (h - (f + o + e + tre +tro) < pamt[0]) {
						trf = h - (f + o + e + tre +tro);
					}
					else {
						trf = pamt[0] - 1;
						ugliness[sec]=ugliness[sec]+1;
					}
					if (trf == -1) {
						trf = 0;
					}
					if (tro == -1) {
						tro = 0;
					}



					break;

				case 5: //SBS
					if (verbose) {
						PrintText.exec("\nCase 5\n");	
					}			
					if (o-1 < pamt[1]) {
						tro = -o;
					}
					else {
						tro = -pamt[1];
						ugliness[sec]=ugliness[sec]+1;
					}
				
					if (h - (e + o + f + tro) < pamt[2]) {
						tre = h - (e + o + tro + f);
					}
					else {
						tre = pamt[2] - 1;
						ugliness[sec]=ugliness[sec]+1;
					}
					if (h - (o + tre + e + f + tro) < pamt[0]) {
						trf = h - (o + tre + e + f + tro);
					}
					else {
						trf = pamt[0] - 1;
						ugliness[sec]=ugliness[sec]+1;
					}
					if (trf == -1) {
						trf = 0;
					}
					if (tre == -1) {
						tre = 0;
					}

					break;
				
				case 6: //BSS
					if (verbose) {
						PrintText.exec("\nCase 6\n");	
					}		
					if (f-1 < pamt[0]) {
						trf = -f;
					}
					else {
						trf = -pamt[0];
						ugliness[sec]=ugliness[sec]+1;
					}
				
					if (h - (e + o + f + trf) < pamt[2]) {
						tre = h - (e + o + trf + f);
					}
					else {
						tre = pamt[2] - 1;
						ugliness[sec]=ugliness[sec]+1;
					}
					if (h - (o + tre + e + f + trf) < pamt[1]) {
						tro = h - (o + tre + e + f + trf);
					}
					else {
						tro = pamt[1] - 1;
						ugliness[sec]=ugliness[sec]+1;
					}
					if (tro == -1) {
						tro = 0;
					}
					if (tre == -1) {
						tre = 0;
					}

					break;

				case 7: //SSS
					if (verbose) {
						PrintText.exec("\nCase 7\n");	
					}			
					if (h - e - f - o < pamt[2]) {
						tre = h - e - f - o;
					}
					else {
						tre = pamt[2] - 1;
						ugliness[sec]=ugliness[sec]+1;
					}
					if (h - e - f - o - tre < pamt[1]) {
						tro = h - e - f - o - tre;
					}
					else {
						tro = pamt[1] - 1;
						ugliness[sec]=ugliness[sec]+1;
					}
					if (h - (e + f + o + tre + tro) < pamt[0]) {
						trf = h - (e + f + o + tre + tro);
					}
					else {
						trf = pamt[0] - 1;
						ugliness[sec]=ugliness[sec]+1;
					}
					if (trf == -1) {
						trf = 0;
					}
					if (tro == -1) {
						tro = 0;
					}
					if (tre == -1) {
						tre = 0;
					}

				
					break;

				case 8: //BBB
					if (verbose) {
						PrintText.exec("\nCase 8\n");	
					}			
					if (f-1 < pamt[0]) {
						trf = -f;
					}
					else {
						trf = -pamt[0];
						ugliness[sec]=ugliness[sec]+1;
					}
					if (o-1 < pamt[1]) {
						tro = -o;
					}
					else {
						tro = -pamt[1];
						ugliness[sec]=ugliness[sec]+1;
					}
					if (e-1 < pamt[2]) {
						tre = -e;
					}
					else {
						tre = -pamt[2];
						ugliness[sec]=ugliness[sec]+1;
					}
				
					break;
			}

			//save ugly information every 10 sectors
			if (Math.IEEEremainder(loopcount,10) == 0 ) {
				if (verbose) {
					PrintText.exec("\nSaving ugly.txt information...");
				}

				uglyout = new BufferedWriter(new FileWriter(filespec));
				for (i=0;i<ugliness.length;i++) {
					uglyout.write(String.valueOf(ugliness[i]));
					uglyout.newLine();
				}
				uglyout.close();
			
				if (verbose) {
					PrintText.exec("Done.\n");
				}
			}
			
			if (verbose) {
				PrintText.exec("\n Trade Fuel="+trf+", Trade Organics="+tro+", Trade Equipment="+tre+"\n");
			}
			
				
			//Port and trade, but if trf = tro = tre = 0 then there is no reason to port
			if((trf != 0) || (tro != 0) || (tre != 0)) {
				if (speed) {
					speedstr = "PT";

					//selling fuel first
					if (trf < 0) {
						speedstr = speedstr+(-trf)+"\r\r";
					}
					if (tro < 0) {
						speedstr = speedstr+(-tro)+"\r\r";
					}
					if (tre < 0) {
						speedstr = speedstr+(-tre)+"\r\r";
					}
					if (trf > 0) {
						speedstr = speedstr+trf+"\r\r";
					}
					if ((trf == 0) && (pamt[0] > 0) && (Swath.getSector(whereami).portInfo()[0] == Sector.SELLING)) {
						speedstr = speedstr+"0\r";
					}
					if (tro > 0) {
						speedstr = speedstr+tro+"\r\r";
					}	
					if ((tro == 0) && (pamt[1] > 0) && (Swath.getSector(whereami).portInfo()[1] == Sector.SELLING)) {
						speedstr = speedstr+"0\r";
					}
					
					if (tre > 0) {
						speedstr = speedstr+tre+"\r\r";
					}
					if ((tre == 0) && (pamt[2] > 0) && (Swath.getSector(whereami).portInfo()[2] == Sector.SELLING)) {
						speedstr = speedstr+"0\r";
					}
					//later  SendString.exec(speedstr+"SDD");
				}
				else {
					Trade.exec(trf,tro,tre);
				}
			}
			
			
		
			//if were string macroing lets display the string we just sent to the port
			if (speed) {
				if (verbose) {
					if (speedstr.length() >0) {
						String displayme="";
						for (int zd=0;zd < speedstr.length();zd++) {
							if (speedstr.substring(zd,zd+1).equals("\r")) {
								displayme = displayme + "<enter>";
							}
							else {
								displayme = displayme + speedstr.substring(zd,zd+1);
							}
						}
						PrintText.exec("\n\n\nWe traded with string -->"+displayme+"<---\n\n\n");
					}
					else {
						PrintText.exec("\n\n\nno trade macro string was generated...\n\n\n");
					}
				}
			}		
			

			
			
			//figure out where to move.  We start out moving to sector 1, and each "PASS" or criterion
			//kicks in anytime it sees we're still moving to 1, and it will try to find something better.  
			//once a "pass" has found a better sector, the remaining passes will not kick in.
			//Thus the passes are prioritized (PASS A is the highest priority...PASS B next highest, etc)
			//you can really change the behavior of this script simply by cutting and pasting the passes around...
			//for instance if you want a script that always goes to unexplored ports, cut and paste PASS C
			//and place it above pass A.
					
			int adj[] = Swath.getSector(whereami).warpSectors();
			int goodsect = 1;
			if (verbose) {
				PrintText.exec("\nInitializing: Setting goodsect=1\n");
			}

			if (Math.IEEEremainder(loopcount,20) == 0) {
				SendString.exec("#");
			}

			if (speed) {
				if (holoscan) {
					SendString.exec(speedstr+"SHSD/");
				}
				else {
					SendString.exec(speedstr+"SD/");
				}
			}
			else {
				if (holoscan) {
					SendString.exec("SHSD/");
				}
				else {
					SendString.exec("SD/");
				}
			}
			try{
				WaitForText.exec("AtmDt",5000);
			}
			catch (Exception te) {
				PrintTrace.exec("Timeout, sending returns");
				SendString.exec("\r\r\r\r\r\r/");
			}
			//ScanSector.exec(ScanSector.DENSITY_SCAN);
			

			//PASS A: prefer xsb port if we have equipment and xbs port if we have organics
			for (i=0;i<adj.length;i++) {
				s = Swath.getSector(adj[i]);
				h = s.portClass();
				pamt = s.portAmounts();
				if (Swath.ship.equipment() > 0) {
					//if the sector has a xsb port
					if ((h==2)||(h==4)) {
						//if its not traded down
						if ((pamt[0] == -1) || (pamt[1] > Swath.ship.holds()) && (pamt[2] > Swath.ship.holds())) {
							//if it looks safe
							if (safe(s)) {
								//then its a good destination for us
								goodsect = adj[i];
								if (verbose) {
									PrintText.exec("\nPass A: Changing goodsect="+adj[i]+"\n");
								}
							}
						}
					}
				}
				else {
					//if we have organics in our holds
					if (Swath.ship.organics() > 0) {
						//if the sector has a xbs port
						if ((h==1) || (h == 5)) {
							//if its not traded down 
							if ((pamt[0] == -1) || (pamt[1] > Swath.ship.holds()) && (pamt[2] > Swath.ship.holds())) {
								//if it looks safe
								if (safe(s)) {
								//then its a good destination for us
								goodsect = adj[i];
									if (verbose) {
										PrintText.exec("\nPass A: Changing goodsect="+adj[i]+"\n");
									}
								}
							}
						}
					}
				}
			}


			//PASS C:if goodsect is still 1, then pass B failed to come up with anything
			//So next we choose to go to any unexplored sector with a port in it (density 100)
			if (goodsect == 1) {
				for (i=0;i<adj.length;i++) {
					s = Swath.getSector(adj[i]);
					if (s.isUnexplored()) {
						if ((s.density() == 100) && (s.fighters() == 0)) {
							goodsect = adj[i];
							if (verbose) {
								PrintText.exec("\nPass C: Changing goodsect="+adj[i]+"\n");
							}
						}
					}
				}
			}
	




		
			//PASS E: if goodsect is still 1 then D didnt find anything good
			//so now we just go to the unexplored sector with the maximum number of warps
			if (goodsect == 1) {
				warphighscore = 0;
				for (i=0;i<adj.length;i++) {
					s = Swath.getSector(adj[i]);
					if (s.isUnexplored()) {
						if (safe(s)) {
							if (s.warps() > warphighscore) {
								warphighscore = s.warps();
								goodsect = adj[i];
								if (verbose) {
									PrintText.exec("\nPass E: Changing goodsect="+adj[i]+"\n");
								}
							}
						}
					}
				}
			}



		

			//if goodsect is still 1 then the criteria didnt find any good place to go from here.
			//we dont want to repeat that situation, so we increase our current sector's
			//ugliness by 1.
			if (goodsect == 1) {
				ugliness[Swath.main.currSector()] = ugliness[Swath.main.currSector()] + 1;
			}
			uglylowscore = 0;


			//PASS Z:Now lets just get the heck out of here, picking an adjacent sector with
			//the lowest ugliness score. Passes A-E teach the computer how to pick a good
			//sector, so if we get to this point alot we may need to add more logic (passes F,G etc)
			//to get the computer better at picking destinations 
			if (goodsect == 1) {
				if (verbose) {
					PrintText.exec("\n<<<Resorting to pass Z>>>\n");
				}
				if (Swath.sector.spaceName().equals("The Federation.")) {
					uglylowscore = 999;
				}
				else {
					uglylowscore = 20;
				}
				for (i=0;i<adj.length;i++) {
					s = Swath.getSector(adj[i]);
					if (verbose) {
						PrintText.exec("\nUgliness of "+adj[i]+": "+ugliness[adj[i]]);
				  	}
					//never go to an unsafe sector
					if (safe(s)) {
						if (ugliness[adj[i]] < uglylowscore) {
							uglylowscore = ugliness[adj[i]];
							goodsect = adj[i];
							if (verbose) {
								PrintText.exec("\nPass Z: Changing goodsect="+adj[i]+"\n");
							}
						}
					}
				}
			}						
					
			//By now if goodsect is still 1 we might as well go to sector 1 and
			//get furbed anyway! Unless we've already just visited one.
			if ( (goodsect == class0port) && ((loopssinceclass0 < 500) || (!(fedspace)) )  ) {
				if (verbose) {
					PrintText.exec("\n\n\n\n <<<<<< Trying to go to class0, but was just there! >>>> \n\n\n\n");
				}
				adj = Swath.sector.warpSectors();
				for (i=0;i<adj.length;i++) {
					s = Swath.getSector(adj[i]);
					if (safe(s)) {
						goodsect = adj[i];
						if (verbose) {
							PrintText.exec("\nPass Z: Changing goodsect="+adj[i]+"\n");
						}
					}
					if (verbose) {
						PrintText.exec((s.sector())+" is safe? "+(safe(s))+"\n");
					}
				}

				if (goodsect == class0port) {
					
						PrintText.exec("No safe sectors to move to. Dont want to go to class 0. ending\n");
						return true;
					
				}
			
			}	


			//At this point we are moving to goodsect.
			//before we move, holoscan (if desired)
			//if (holoscan) {
			//	ScanSector.exec(ScanSector.HOLO_SCAN);
			//}


			//issue warning if we are warping to an ugly sector	
			if (ugliness[goodsect] > 3) {
				SetTextMode.exec(SetTextMode.COLOR_WHITE, SetTextMode.COLOR_RED, SetTextMode.MODE_NORMAL);

				PrintText.exec("\n <<<<<< WARNING: Ugliness of "+goodsect+" is "+ugliness[goodsect]+"!!!\n WATCH AND MAKE SURE YOU DO NOT STARTx LOOPING IN BAD SPACE >>>>>> \n");
			}	
	
			if (goodsect != class0port) {
				
				loopssinceclass0 = loopssinceclass0 + 1;
				if (speed) {
					SendString.exec(goodsect+"\r");
					try{
				WaitForText.exec(goodsect+"] (?=Help",5000);
			}
			catch (Exception te) {
				PrintTrace.exec("Timeout, sending returns");
				SendString.exec("\r\r\r\r\r\r/");
				WaitForText.exec("AtmDt");
			}
						
				}
				else {
					Move.exec(goodsect);
				}

			}
			else {
				PrintText.exec("\nWaiting 1 seconds\n");
				PrintText.exec("\nMoving to Class 0 port at "+class0port+"\n");
				Thread.sleep(1000);
				Move.exec(goodsect);
				loopssinceclass0 = 0;
			}
			
			whereami=goodsect;	
		}	
		
		PrintText.exec("Turns less than 50. Done.");
		return true;
	}

		public void status() throws Exception {
		int ecred = Swath.you.credits();
		int eexp = Swath.you.experience();
		int ealign = Swath.you.alignment();
		//long etime = Calendar.getInstance().getTimeInMillis();
		long etime = Calendar.getInstance().getTime().getTime();

		int netcred = ecred - scred;
		int netexp = eexp - sexp;
		int netalign = ealign - salign;
		long nettime = (etime - stime)/1000 ;
		if (nettime < 1) {
			nettime = 1;
		}
		String netcph = NumberFormat.getCurrencyInstance().format(3600 * (netcred/nettime));
		//PrintTrace.exec("Overall credits     : "+netcred);
		//PrintTrace.exec("Overall experience  : "+netexp);
		//PrintTrace.exec("Overall alignment   : "+netalign);
		//PrintTrace.exec("Overall time in sec : "+nettime);
		//PrintTrace.exec("Overall credits per hour: "+netcph);
		label2.setText(netcph);


	}
	//this little bit determines whether it should be considered safe to enter a sector
	//modify according to taste
	public boolean safe(Sector s) throws Exception {
		boolean fedspace = m_fedspace.getBoolean();
		boolean agressive =  m_agressive.getBoolean();
		boolean retme = false;
		int dens = s.density();
		if ((!(s.spaceName().equals("The Federation."))) || (fedspace)) {
			if (!(s.anomaly())) {
				if (((dens == 5 || dens == 105) && (agressive)) || dens == 0 || dens == 100 || dens == 1 || dens == 101 || (s.ftrOwner().isYourCorporation() && s.fighters() > 0) || (s.ftrOwner().isYou() && s.fighters() > 0) ) {
					if (s.busted() == null) {
						retme = true;
					}
				}
			}
		}
		//report interesting sectors to the swath console
		if (s.anomaly()) {
			PrintTrace.exec("**********************************");
			PrintTrace.exec("Anomaly in sector "+(s.sector())+".");
			PrintTrace.exec("(density of "+(s.sector())+" is "+(s.density())+".)");
			PrintTrace.exec("**********************************");
		}
		if (s.density() == 140 || s.density() == 40 || s.density() == 45 || s.density() == 145) {
			PrintTrace.exec("**********************************");
			PrintTrace.exec("Possible trader in sector "+(s.sector())+".");
			PrintTrace.exec("(density of "+(s.sector())+" is "+(s.density())+".)");
			PrintTrace.exec("**********************************");	
		}
		return retme; 
	}


/***********************************************************************************************
 **************************************End of Script Logic**************************************
 ***********************************************************************************************/


        public String getName() {
                return "VitoTrade World";
        }
	public String getDescription() {
		return "<html>"+
			
			"Vito Trade World 2/12/03 <p>"+ 
			"this script will WorldTrade Vito style. <p>"+
			"see the .java file for a commentary... <p>"+
			"when selecting delay fig settings, the choice "+
			"<b><font color=red>Vito's favorite</font></b> is a setting where 1 toll fig is dropped"+
			" in every sector, except for completely empty sectors "+
			" where it dropps 20 offensive figs, in order to give the sector"+
			" a density of 100." +
		
			"</html>";
	}
        public boolean initScript() throws Exception {



              if (!atPrompt(Swath.COMMAND_PROMPT)) {
                       PrintText.exec("\n\n>>>Get to the main command prompt<<<\n\n");
                        return false;
	  	}

		m_layfigs = new Parameter("Delay Figs?");
		m_layfigs.setType(Parameter.CHOICE);
		m_layfigs.addChoice(0, "Do not drop any fighters");
		m_layfigs.addChoice(1, "1 toll fighter all sectors");
		m_layfigs.addChoice(2, "1 toll fighter under ports only");
		m_layfigs.addChoice(3, "1 toll fighter every other sector");
		m_layfigs.addChoice(4, "Vitos favorite (see descrip.)");
		m_layfigs.setCurrentChoice(4);
		registerParam(m_layfigs);


                m_holoscan = new Parameter("Holoscan?");
                m_holoscan.setType(Parameter.BOOLEAN);
                m_holoscan.setBoolean(false);
                registerParam(m_holoscan);

                m_unattended = new Parameter("Unattended mode?");
                m_unattended.setType(Parameter.BOOLEAN);
                m_unattended.setBoolean(false);
                registerParam(m_unattended);


                m_speed = new Parameter("Macro String for speed? (Turn haggling off!!)");
                m_speed.setType(Parameter.BOOLEAN);
                m_speed.setBoolean(true);
                registerParam(m_speed);


 		m_fedspace = new Parameter("Allow travel into Fedpsace?");
                m_fedspace.setType(Parameter.BOOLEAN);
                m_fedspace.setBoolean(false);
                registerParam(m_fedspace);

	
		m_agressive = new Parameter("Agressive mode (walk over enemy tollfigs)?");
                m_agressive.setType(Parameter.BOOLEAN);
                m_agressive.setBoolean(true);
                registerParam(m_agressive);

                m_verbose = new Parameter("Be verbose?");
                m_verbose.setType(Parameter.BOOLEAN);
                m_verbose.setBoolean(false);
                registerParam(m_verbose);


            
                return true;
        }

        public void endScript(boolean finished) {
        }

}