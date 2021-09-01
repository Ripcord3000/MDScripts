import com.swath.*;
import com.swath.cmd.*;
import java.io.*;
import java.util.*;
import java.text.*;
import javax.swing.*;
import java.awt.*;


//VitoTrade 2/12/03
//Modified by Ripcord 2021

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
// New in version 2003-02-12:
// #1 speedometer reports credits earned per hour
// #2 holoscan is working
// #3 unattended mode works a lot better
// #4 added more delay fig options, "Vitos favorite" means it drops
// one toll fig in every sector, except for 20 offensive figs in empty sectors,
// thus giving the sector a density of 100 hehehehehehheheheheh
// ------------------------------------------------------------------------
// New in version 2021-09-01:
// - Added additional data to the stats pane:
//   - Total credits made for this run
//   - Experience earned
//   - # Fighters killed
//   - # Fighers dropped

// Updated "deploy fighters" logic:
//   - Simplified the "layfigs" logic, faster (doesn't do things like bother to re-drop figs which was just a waste of time)
//   - Added an option to deploy every 5 sectors

// - In aggressive mode, prefer sectors that have a few pesky figs.  Works as an "ungridder" while still trading if wanted.
// - Fixed an issue where we'd try to avoid sectors with our OWN fighters in them.

// - Fixed an issue where the script could hit a race condition and fail to move if the command prompt hadn't come back before we tried to move (non-speed mode)
// - Fixed an issue where the loop would exit early thinking you were out of turns on unlimited-turn servers
// - Fixed an issue where we'd try to move into avoided sectors.
// - Added an "explore-only" option, letting you just explore, drop figs (gridding), kill figs (ungrid), VERY quickly (similar to SWATH world trade but smarter and faster)
// - Moved a whole bunch of output, especially verbose, to the SWATH console instead of the main window
// - Improved description/help
// - Minor improvements to text output (additional info, spelling, formatting), etc.
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
	private Parameter m_explore;
	
	int scred,sexp,salign;
	int lcred,lexp,lalign;
	long stime,ltime;
	int figsKilled, figsDropped;
	
	final JLabel label1 = new JLabel("Credits per hour:");
	final JLabel label2 = new JLabel("$<computing>");
	final JLabel label3 = new JLabel("Credits total:");
	final JLabel label4 = new JLabel("<computing>");
	final JLabel label5 = new JLabel("Net experience:");
	final JLabel label6 = new JLabel("<computing>");
	final JLabel label7 = new JLabel("Fighters killed:");
	final JLabel label8 = new JLabel("<computing>");
	final JLabel label9 = new JLabel("Fighters dropped:");
	final JLabel label10 = new JLabel("<computing>");

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
		panel.add(label3);
		panel.add(label4);
		panel.add(label5);
		panel.add(label6);
		panel.add(label7);
		panel.add(label8);
		panel.add(label9);
		panel.add(label10);

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
			PrintTrace.exec("\n"+filespec+" not found, creating Ugliness...");
			Arrays.fill(ugliness,0);
			//there is no sector 0 so in ugliness terms.. it is very ugly
			ugliness[0] = 999;
			//we only want to go to sector 1 if we are forced to ...
			ugliness[1] = 20;
			//set the URL
			if (verbose) {
				PrintTrace.exec("Creating "+filespec);
			}
		}

		//DisplayCurrentInfo.exec();
		loopcount=0;
		PrintTrace.exec("Turns: "+Swath.you.turns());
		int turns = Swath.you.turns();
		while (turns > 100 || turns == 0) {
			PrintTrace.exec("Starting next loop");
			status();
			turns = Swath.you.turns();
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
							int numToDrop = 0;
							Boolean vitoDrop = false;
							switch (layfigs) {
								case 1:
									numToDrop = 1;
									break;
								case 2:
									if (Swath.getSector(whereami).portStatus() == Sector.PORT_AVAILABLE) {
										numToDrop = 1;
									}
									break;
								case 3:
									if (Math.IEEEremainder(loopcount,2) == 0) {
										numToDrop = 1;
									}
									break;
								case 4:
									if (Math.IEEEremainder(Swath.sector.sector(),5) == 0) {
										numToDrop = 1;
									}
									break;
								case 5:
									if (Swath.getSector(whereami).density() == 0) {
										numToDrop = 20;
										vitoDrop = true;
									}
									else {
										numToDrop = 1;
									}
									break;
							}

							if (numToDrop > 0) {
								numToDrop = Math.max(Swath.sector.fighters(), numToDrop);
							}


							if (numToDrop > 0) {
								if (speed) {
									if (vitoDrop) {
										SendString.exec("F"+numToDrop+"\rCoD");
									} else {
										SendString.exec("F"+numToDrop+"\rCTD");
									}
								} else {
									if (vitoDrop) {
										DropTakeFighters.exec(numToDrop, Swath.CORPORATE,Swath.OFFENSIVE_FTRS);
									} else {
										DropTakeFighters.exec(numToDrop, Swath.CORPORATE,Swath.TOLL_FTRS);
									}
								}
								figsDropped += numToDrop;
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
			if (verbose && !m_explore.getBoolean()) {
				PrintTrace.exec("Port amounts:\npamt[fuel]="+pamt[0]+"\npamt[org]="+pamt[1]+"\npamt[equ]="+pamt[2]);
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
						PrintTrace.exec("Case 1");
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
						PrintTrace.exec("Case 2");
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
						PrintTrace.exec("Case 3");
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
						PrintTrace.exec("Case 4");
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
						PrintTrace.exec("Case 5");
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
						PrintTrace.exec("Case 6");
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
						PrintTrace.exec("Case 7");
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
						PrintTrace.exec("Case 8");
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
					PrintTrace.exec("Saving ugly.txt information...");
				}

				uglyout = new BufferedWriter(new FileWriter(filespec));
				for (i=0;i<ugliness.length;i++) {
					uglyout.write(String.valueOf(ugliness[i]));
					uglyout.newLine();
				}
				uglyout.close();
			
				if (verbose) {
					PrintTrace.exec("Done.");
				}
			}
			
			if (verbose) {
				PrintTrace.exec(" Trade Fuel="+trf+", Trade Organics="+tro+", Trade Equipment="+tre);
			}
			
			if (!m_explore.getBoolean()) {
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
							PrintTrace.exec("We traded with string -->"+displayme+"<---");
						}
						else {
							PrintTrace.exec("No trade macro string was generated...");
						}
					}
				}
			} // Port and trade (if not exploring)
			

			
			
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
				PrintTrace.exec("Initializing: Setting goodsect=1");
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
			// And if we're not exploring
			if (goodsect == 1 && !m_explore.getBoolean()) {
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
										PrintTrace.exec("Pass A: Changing goodsect="+adj[i]+"");
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
											PrintTrace.exec("Pass A: Changing goodsect="+adj[i]+" - xsb port");
										}
									}
								}
							}
						}
					}
				}
			} // Pass A

			// PASS B:  If very aggressive, prefer any sector with other player fighters to kill
			// ("ungrid")
			if (goodsect == 1 && m_agressive.getBoolean()) {
				for (i=0;i<adj.length;i++) {
					s = Swath.getSector(adj[i]);
					if (s.fighters() > 0 && !(s.ftrOwner().isYou() || s.ftrOwner().isYourCorporation()) && safe(s)) {
						goodsect = adj[i];
						if (verbose) {
							PrintTrace.exec("Pass B: Changing goodsect="+adj[i]+" - taking out fighter");
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
								PrintTrace.exec("Pass C: Changing goodsect="+adj[i]+" - any unexplored with port");
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
									PrintTrace.exec("Pass E: Changing goodsect="+adj[i]+" - any unexplored");
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
					PrintTrace.exec("<<<Resorting to pass Z>>>");
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
						PrintTrace.exec("Ugliness of "+adj[i]+": "+ugliness[adj[i]]);
				  	}
					//never go to an unsafe sector
					if (safe(s)) {
						if (ugliness[adj[i]] < uglylowscore) {
							uglylowscore = ugliness[adj[i]];
							goodsect = adj[i];
							if (verbose) {
								PrintTrace.exec("Pass Z: Changing goodsect="+adj[i]+" - lowest ugliness");
							}
						}
					}
				}
			}

			//By now if goodsect is still 1 we might as well go to sector 1 and
			//get furbed anyway! Unless we've already just visited one.
			if ( (goodsect == class0port) && ((loopssinceclass0 < 500) || (!(fedspace)) )  ) {
				if (verbose) {
					PrintTrace.exec(" <<<<<< Trying to go to class0, but was just there! >>>> ");
				}
				adj = Swath.sector.warpSectors();
				for (i=0;i<adj.length;i++) {
					s = Swath.getSector(adj[i]);
					if (safe(s)) {
						goodsect = adj[i];
						if (verbose) {
							PrintTrace.exec("Pass Z: Changing goodsect="+adj[i]+"");
						}
					}
					if (verbose) {
						PrintTrace.exec((s.sector())+" is safe? "+(safe(s))+"");
					}
				}

				if (goodsect == class0port) {
					
						PrintTrace.exec("No safe sectors to move to. Dont want to go to class 0. ending");
						return true;
					
				}
			
			}


			//At this point we are moving to goodsect.
			//before we move, holoscan (if desired)
			//if (holoscan) {
			//	ScanSector.exec(ScanSector.HOLO_SCAN);
			//}


			//issue warning if we are warping to an ugly sector	
			if (ugliness[goodsect] > 6) {
				SetTextMode.exec(SetTextMode.COLOR_WHITE, SetTextMode.COLOR_RED, SetTextMode.MODE_NORMAL);

				PrintText.exec("\n <<<<<< WARNING: Ugliness of "+goodsect+" is "+ugliness[goodsect]+"!!!\n WATCH AND MAKE SURE YOU DO NOT STARTx LOOPING IN BAD SPACE >>>>>> \n");
			}

			if (goodsect != class0port) {
				int numSectFigs = 0;
				Sector gs = Swath.getSector(goodsect);
				if (gs.fighters() > 0 && !(gs.ftrOwner().isYou() || gs.ftrOwner().isYourCorporation())) {
					numSectFigs = gs.fighters();
				}
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
					Thread.sleep(50);
					SendString.exec("\r");
					WaitForPrompt.exec(Swath.COMMAND_PROMPT);
					Move.exec(goodsect);
				}
				figsKilled += numSectFigs;

			}
			else {
				PrintTrace.exec("Waiting 1 second");
				PrintTrace.exec("Moving to Class 0 port at "+class0port+"");
				Thread.sleep(1000);
				Move.exec(goodsect);
				loopssinceclass0 = 0;
			}

			whereami=goodsect;	
		}	
		
		PrintText.exec("Turns less than 50 :" + turns + ". Done.");
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
		label4.setText(""+netcred);
		label6.setText(""+netexp);
		label8.setText(""+figsKilled);
		label10.setText(""+figsDropped);
	}

	//this little bit determines whether it should be considered safe to enter a sector
	//modify according to taste
	public boolean safe(Sector s) throws Exception {
		boolean fedspace = m_fedspace.getBoolean();
		boolean agressive =  m_agressive.getBoolean();
		boolean retme = false;
		int dens = s.density();
		if ((!(s.spaceName().equals("The Federation."))) || (fedspace)) {
			if (!(s.anomaly()) && !(s.isAvoided())) {
				if (((dens == 5 || dens == 105) && (agressive)) || dens == 0 || dens == 100 || dens == 1 || dens == 101 || ((s.fighters() > 0 && s.fighters() < 100) && (s.ftrOwner().isYourCorporation() || (s.ftrOwner().isYou()) ))) {
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
			
			"<p>Vito Trade World 2021-09-01 </p>"+
			"<p>This script will WorldTrade Vito style. </p>"+
			"<p><b>Deploy figs:</b><br>" +
			"Drops fighters at the frequency you select.<br>" +
			"Note the choice " +
			"<b><font color=red>Vito's favorite</font></b> is a setting where 1 toll fig is dropped"+
			" in every sector, except for completely empty sectors "+
			" where it dropps 20 offensive figs, in order to give the sector"+
			" a density of 100.</p>" +
			"<p><b>Unattended mode:</b><br>" +
			"Slightly more cautious about making mistakes, at the expense of speed.</p>" +
			"<p><b>Speed Mode:</b><br>" +
			"Issues macro strings for speed instead of SWATH primitives.  Slightly more likely to be interrupted." +
			"If enabled, turn off haggling mode (no haggling will be done, but can interfere with the script)</p>" +
			"<p><b>Aggressive mode:</b><br>" +
			"Will cause you to attack sectors with a handful of fighters in them and clear them out.<br>" +
			"In fact, in this mode you will PREFER those sectors.  Using in combination with the 'Explore' mode is a very" +
			" effective anti-gridder</p>"+
			"<p><b>Verbose mode:</b><br>" +
			"Prints additional info to the SWATH console for debugging</p>" +
			"<p><b>Do not trade, just explore:</b><br>" +
			"Does just what it says.  VERY fast at exploring, gridding, anti-gridding.</p>" +

			"</html>";
	}

	public boolean initScript() throws Exception {

		if (!atPrompt(Swath.COMMAND_PROMPT)) {
			PrintText.exec("\n\n>>>Get to the main command prompt<<<\n\n");
			return false;
	  	}

		m_layfigs = new Parameter("Deploy Figs?");
			m_layfigs.setType(Parameter.CHOICE);
			m_layfigs.addChoice(0, "Do not drop any fighters");
			m_layfigs.addChoice(1, "1 toll fig, all sectors");
			m_layfigs.addChoice(2, "1 toll fig, under ports only");
			m_layfigs.addChoice(3, "1 toll fig, every other sector");
			m_layfigs.addChoice(4, "1 toll fig, every 5th sector");
			m_layfigs.addChoice(5, "Vitos favorite (see descrip)");
			m_layfigs.setCurrentChoice(0);
			registerParam(m_layfigs);


		m_holoscan = new Parameter("Holoscan?");
			m_holoscan.setType(Parameter.BOOLEAN);
			m_holoscan.setBoolean(true);
			registerParam(m_holoscan);

		m_unattended = new Parameter("Unattended mode?");
			m_unattended.setType(Parameter.BOOLEAN);
			m_unattended.setBoolean(false);
			registerParam(m_unattended);


		m_speed = new Parameter("Speed mode (Turn haggling off!!)?");
			m_speed.setType(Parameter.BOOLEAN);
			m_speed.setBoolean(true);
			registerParam(m_speed);


 		m_fedspace = new Parameter("Allow travel into Fedpsace?");
			m_fedspace.setType(Parameter.BOOLEAN);
			m_fedspace.setBoolean(false);
			registerParam(m_fedspace);

	
		m_agressive = new Parameter("Agressive mode?");
			m_agressive.setType(Parameter.BOOLEAN);
			m_agressive.setBoolean(true);
			registerParam(m_agressive);

		m_verbose = new Parameter("Be verbose?");
			m_verbose.setType(Parameter.BOOLEAN);
			m_verbose.setBoolean(false);
			registerParam(m_verbose);

		m_explore = new Parameter("Do not trade, just explore");
			m_explore.setType(Parameter.BOOLEAN);
			m_explore.setBoolean(false);
			registerParam(m_explore);
	
			return true;


        }

        public void endScript(boolean finished) {
        }

}