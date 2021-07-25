import com.swath.*;
import com.swath.cmd.*;
import java.io.*;
import java.util.*;
import java.text.*;
import javax.swing.*;
import java.awt.*;

//MacroSSM alpha 1/29/03


// ansi animation off
// silence all messages
// haggle off
// autokill figs turned on
// try to have a good amount of experience like minimum 5000+, ideally 8000+
//
// go browse your shipcategory in the computer's C command first.
// then the script will automatically know your maxholds and maxfighters.
// if youre in an alien ship you will probably have to enter maxholds and maxfighters manually.
// ---------------------------------------------------------------------------------------------
// Alpha version: sometimes it fake busts, and the script still does not recover after a fake bust.
// report bugs on swath discussion forum, post ideas for new features there too,
// and especially ideas on how to increase credits per hour, or how to catch fake busts...
// you will probably get podded if you use this script when other people are online
// but then again that goes for any wssm script...
// ---------------------------------------------------------------------------------------------
// regarding the status reports that are generated:
// loop credits per hour measures the earning potential of the current SSM loop.
// it is how much you would expect to make if you kept looping at these two ports
// indefinitely without busting for an hour straight.  Use it like a speedometer.
// overall credits per hour measures how much you can actually expect to have at the end of 1 hour.
// it includes the time and credits wasted refurbing and hunting down evil pairs,
// and it wont really be useful till the script has been running 5-10 minutes or so.
// ---------------------------------------------------------------------------------------------
// thanks mongoose for the code to learn from, supg for his TWX WSSM script
// come play me unlimited at borg.exiled.org

public class MacroSSM extends UserDefinedScript {
	private Parameter m_maxholds;
	private Parameter m_maxfigs;
	private Parameter m_stealfactor;
	private Parameter m_class0port;
	int scred,sexp,salign;
	int lcred,lexp,lalign;
	long stime,ltime;
	
	final JLabel label1 = new JLabel("<html><b>Loop credits per hour:</b></html>");
	final JLabel label2 = new JLabel("$<computing>");
	final JLabel label3 = new JLabel("<html><b>Overall credits per hour:</b></html>");
	final JLabel label4 = new JLabel("$<computing>");
	final JLabel label5 = new JLabel("Credits at start of script:");
	final JLabel label6 = new JLabel("$<computing>");
	final JLabel label7 = new JLabel("Current credits:");
	final JLabel label8 = new JLabel("$<computing>");
	final JLabel label9 = new JLabel("Time elapsed in seconds");
	final JLabel label10 = new JLabel("<computing>");

	public String getName() {
		return "Vito WorldSSM";
	}
	public String getDescription() {
		return "This script will WorldSSM.  It bypasses the Swath Command API\n"+
		       "and inlines macro strings for speed. 1/29/03";
	}
	public boolean initScript() throws Exception {
		int recholds = 0;
		int recfigs = 0;
		if (Swath.main.prompt() != Swath.COMMAND_PROMPT) {
			PrintText.exec("\nStart this script at the command prompt\n");
			return false;
		}
		try {
			recholds = Swath.ship.shipCategory().maxHolds();
		}
		catch (Throwable t) {
			PrintTrace.exec("Problem getting maxholds");
		}
		PrintTrace.exec("The ship category max is "+recholds+" holds.");
		if (recholds < 10) {
			recholds = 250;
		}

		try {
			recfigs = Swath.ship.shipCategory().maxFighters();
		}
		catch (Throwable t) {
			PrintTrace.exec("Problem getting maxfighters");
		}
		PrintTrace.exec("The ship category max is "+recfigs+" fighters.");
		if (recfigs < 10) {
			recfigs = 20000;
		}
		m_maxholds = new Parameter("Maximum holds your ship can have");
		m_maxholds.setType(Parameter.INTEGER);
		m_maxholds.setInteger(recholds);
		registerParam(m_maxholds);

		m_maxfigs = new Parameter("Maximum fighters your ship can have");
		m_maxfigs.setType(Parameter.INTEGER);
		m_maxfigs.setInteger(recfigs);
		registerParam(m_maxfigs);

		m_stealfactor = new Parameter("Steal factor");
		m_stealfactor.setType(Parameter.INTEGER);
		m_stealfactor.setInteger(25);
		registerParam(m_stealfactor);
		

		m_class0port = new Parameter("Class 0 port");
		m_class0port.setType(Parameter.INTEGER);
		m_class0port.setInteger(1);
		registerParam(m_class0port);
		return true;
		
	}
	public boolean endScript() throws Exception {
		
		throw(new Exception());
	}
	public boolean runScript() throws Exception {
		scred = Swath.you.credits();
		sexp = Swath.you.experience();
		salign = Swath.you.alignment();
		//stime = Calendar.getInstance().getTimeInMillis();
		stime = Calendar.getInstance().getTime().getTime();
		PrintTrace.exec("Starting at "+Calendar.getInstance().getTime().toString());
		int class0port = m_class0port.getInteger();
		int maxholds = m_maxholds.getInteger();
		
		Random r = new Random();
		int prevsector = Swath.sector.sector();

		


		//**************************STATUS WINDOW***************************
		//credit to mongoose for his getsectors script
		JPanel panel = Tools.createJPanel();
		JFrame frame = Tools.createJFrame("MacroSSM Status", 1, 1);
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

		label6.setText(NumberFormat.getCurrencyInstance().format(scred));
		label8.setText(NumberFormat.getCurrencyInstance().format(scred));
		//*********************************************************************


		while (true) {			
			if (maxholds > Swath.ship.holds()) {

				VitoMove(class0port);
				refurb();
				int rnd = r.nextInt(Swath.main.sectors());
				VitoMove(rnd);
			}
			while (Swath.main.prompt() != Swath.COMMAND_PROMPT) {
				Thread.sleep(25);
			}
			SendString.exec("#SHSD/");
			WaitForText.exec("AtmDt");
	

			boolean goodneighbor = false;
			int neighborsect = 0;
			//Am I at an evil twin?
			if (evilTwin(Swath.sector.sector())) {
				for (int i=0; i < Swath.sector.warps(); i++) {
					if (goodneighbor == false) {
						if (evilTwin(Swath.sector.warpSectors()[i])) {
							while (Swath.main.prompt() != Swath.COMMAND_PROMPT) {
								Thread.sleep(25);
							}
							SendString.exec("CF"+Swath.sector.warpSectors()[i]+"\r"+Swath.sector.sector()+"\rQ");
							while (Swath.main.prompt() != Swath.COMMAND_PROMPT) {
								Thread.sleep(25);
							}
							while (Tools.findRoute(Swath.sector.warpSectors()[i],Swath.sector.sector()).length < 1) {
								Thread.sleep(25);
							}
							if (Tools.findRoute(Swath.sector.warpSectors()[i],Swath.sector.sector()).length == 2) {
								goodneighbor = true;
								neighborsect = Swath.sector.warpSectors()[i];
							}
						}
					}
				}
			}		

			//if I got a evil twin next to me
			if (goodneighbor) {
				//then SSM
				boolean keepgoing = VitoSSM(Swath.sector.sector(),neighborsect);
				if (keepgoing) {					
						
				}
				else {
					frame.dispose();
					endScript();
				}
			}
			else {
				//credit to supg for this method in his script
				int bestmove = 1;
				int bestscore = 999999;
				for (int i=0; i < Swath.sector.warps(); i++) {
					int score = 999;
					Sector n = Swath.getSector(Swath.sector.warpSectors()[i]);
					
					// here's where you tally the score of sector n
					if (n.spaceName().equals("The Federation.")) {
						score = score + 99999;
					}
					if (n.anomaly()) {
						score = score + 99999;
						PrintTrace.exec("Anomaly in sector"+n.sector());
					}
					if ((n.warps() == 1) && (n.warpSectors()[0] == Swath.sector.sector())) {
						score = score + 99999;
					}
					if ((n.density() == 100) && (n.portStatus() == Sector.PORT_AVAILABLE)) {
						score = score - 2;
					}
					
					if (n.density() > 200) {
						score = score + n.density();
					}
					if (n.sector() == prevsector) {
						score = score + 75;
					}
					if (n.portStatus() == Sector.PORT_AVAILABLE) {
						score = score - 2;			
						if (n.portInfo()[2] == Sector.BUYING) {
							score = score - 6;
						}
					}
					if (n.busted() != null) {
						score = score + 100;
					}
					if (n.sector() == Swath.main.lastRobStealSector()) {
						score = score + 50;
					}
					score = score - r.nextInt(10);
					if (score < bestscore) {
						bestscore = score;
						bestmove = n.sector();
					}
				}
				while (Swath.main.prompt() != Swath.COMMAND_PROMPT) {
					Thread.sleep (25);
				}
				prevsector = Swath.sector.sector();
				VitoMove(bestmove);
				while (Swath.sector.sector() == prevsector) {
					Thread.sleep(25);
				}
			}
		}
	}

	public boolean evilTwin(int n) throws Exception {
		boolean b = false;
		Sector s = Swath.getSector(n);
		//if its an xxB port
		if (s.portInfo()[2] == Sector.BUYING) {
			//if not busted
			if (s.busted() == null) {
				//if not last R/S sector
				if (Swath.main.lastRobStealSector() != n) {
					//if not fedspace
					if (!(s.spaceName().equals("The Federation."))) {
						//if theres no enemy figs
						if (s.fighters() == 0 && (!(s.ftrOwner().isYou() || s.ftrOwner().isYourCorporation()))){
							//if theres no mines
							if (s.armidMines() == 0) {
								SendString.exec("CR"+n+"\rQ");
								WaitForText.exec("deactivated");
							
								while (s.portAmounts()[2] == -1) {								
									Thread.sleep(25);
								}
								while (Swath.main.prompt() != Swath.COMMAND_PROMPT) {
									Thread.sleep(25);
								}
								//if the port isnt drained
								if (s.portAmounts()[2] > 250) {
									b = true;
								}
							}
						}
					}
				}
			}
		}
		return b;
	}
	public void refurb() throws Exception {	
		int maxholds = m_maxholds.getInteger();
		int maxfigs = m_maxfigs.getInteger();
		while (Swath.main.prompt() != Swath.COMMAND_PROMPT) {
			Thread.sleep(25);
		}
		SendString.exec("DPTYA"+(maxholds-Swath.ship.holds())+"\rYB"+Math.min((Swath.you.credits()/300),maxfigs - Swath.ship.fighters() )+"\rQD/");
		WaitForText.exec("AtmDt");
		while (Swath.main.prompt() != Swath.COMMAND_PROMPT) {
			Thread.sleep(25);
		}
		return;
	}
	public void status() throws Exception {

		int ecred = Swath.you.credits();
		int eexp = Swath.you.experience();
		int ealign = Swath.you.alignment();
		//long etime = Calendar.getInstance().getTimeInMillis();
		long etime = Calendar.getInstance().getTime().getTime();
		label8.setText(NumberFormat.getCurrencyInstance().format(ecred));
		int netcred = ecred - scred;
		int netexp = eexp - sexp;
		int netalign = ealign - salign;
		long nettime = (etime - stime)/1000 ;
		if (nettime < 1) {
			nettime = 1;
		}
		label10.setText(nettime+" ");

		String netcph = NumberFormat.getCurrencyInstance().format(3600 * (netcred/nettime));
		
		label4.setText(netcph);

		int thiscred = ecred - lcred;
		int thisexp = eexp - lexp;
		int thisalign = ealign - lalign;
		long thistime = (etime - ltime)/1000 ;
		if (thistime < 1) {
			thistime = 1;
		}
		String thiscph = NumberFormat.getCurrencyInstance().format(3600 * (thiscred/thistime));
		
		label2.setText(thiscph);


	}
	public void VitoMove(int dest) throws Exception{
		int class0port=m_class0port.getInteger();
		int sf=m_stealfactor.getInteger();
		int here = Swath.main.currSector();

		while (Swath.main.prompt() != Swath.COMMAND_PROMPT) {
			Thread.sleep(25);
		}
		//Plot the course if not adjacent
		if ((Tools.findRoute(here,dest).length > 2) || (Tools.findRoute(here,dest).length == 0)) {
			SendString.exec("CF\r"+dest+"\rQ");
			Thread.sleep(1000);
		}
		while (Swath.main.prompt() != Swath.COMMAND_PROMPT){
			Thread.sleep(25);
		}
		int[] path = Tools.findRoute(here,dest);
		
		for (int i=1; i<path.length; i++) {
			while (Swath.main.prompt() != Swath.COMMAND_PROMPT) {
				Thread.sleep(25);
			}
			Move.exec(path[i]);
			while (Swath.main.prompt() != Swath.COMMAND_PROMPT) {
				Thread.sleep(25);
			}
			if (Swath.ship.equipment() < Math.min(Swath.ship.holds(),Swath.you.experience()/sf)) {
				if (Swath.sector.portStatus() == Sector.PORT_AVAILABLE) {
					if (Swath.sector.portInfo()[2] == Sector.SELLING) {
						if (Swath.sector.busted() == null) {
							if (Swath.sector.sector() != Swath.main.lastRobStealSector() ) {
								//then grab some equ
								try {
									SendString.exec("PR\rS3"+Math.min(Swath.ship.holds()-Swath.ship.equipment(),Swath.you.experience()/sf)+"\r/");
									String str =WaitForText.exec("AtmDt",3000);
								}
								catch (Throwable t) {
									if (t.toString().indexOf("Busted") > -1) {
										
										VitoMove(class0port);
									}
									else {
										endScript();
									}
								}
							}
						}
					}	
				}
			}
		}
		
		
		return;
	}
		
	public boolean VitoSSM(int s1, int s2) throws Exception{

		int sf=m_stealfactor.getInteger();
		Random r = new Random();
		String str=" ";
		String[] waitforme = {"aren't that many","Success"};


		//Move to the other sector to help clear any possible crap
		while (Swath.main.prompt() != Swath.COMMAND_PROMPT) {
			Thread.sleep(25);
		}
		Move.exec(s2);
		while (Swath.main.prompt() != Swath.COMMAND_PROMPT) {
			Thread.sleep(25);
		}
		try {		
			//Upgrade port and steal initial product if need be, otherwise just sell steal
			if ((Swath.ship.equipment() + 5 < Math.min(Swath.ship.holds(),Swath.you.experience()/sf)) || (Swath.ship.fuel() > 0) || (Swath.ship.organics() > 0)) {
				if (Swath.ship.fuel() > 0 || Swath.ship.organics() > 0) {
					JettisonCargo.exec();
				}
				SendString.exec("<O3"+Math.min(((Swath.ship.holds()-Swath.ship.equipment())/10)+2,(Swath.you.experience()/(sf*10))+2)+"\rQPR\rS3"+Math.min(Swath.ship.holds(),Swath.you.experience()/sf)+"\r/");
				str =WaitForText.exec(waitforme,3000);
				while (str.indexOf("aren't that many") > -1) {
					SendString.exec("O31\rQPR\rS3"+Math.min(Swath.ship.holds(),Swath.you.experience()/sf)+"\r/");
					str = WaitForText.exec(waitforme,5000);
				}
				
			}
			
			lcred = Swath.you.credits();
			lexp = Swath.you.experience();
			lalign = Swath.you.alignment();
			//ltime = Calendar.getInstance().getTimeInMillis();
			ltime = Calendar.getInstance().getTime().getTime();
			//now start the loop		
			while (true) {

				SendString.exec("<PT\r\r0\r0\rPR\rS3"+Math.min(Swath.ship.holds(),Swath.you.experience()/sf)+"\r/");
				str =WaitForText.exec(waitforme,5000);
				Thread.sleep(25);

				while (str.indexOf("aren't that many") > -1) {
					SendString.exec("O31\rQPR\rS3"+Math.min(Swath.ship.holds(),Swath.you.experience()/sf)+"\r/");
					str = WaitForText.exec(waitforme,5000);
				}
				
				status();
			}
		}
		catch (Throwable t) {
			

			if (t.toString().indexOf("Busted") > -1) {
						
				return true;
			}
			if (t.toString().indexOf("Timeout") > -1) {
				PrintTrace.exec("Maybe this port is maxed out");
				while (Swath.main.prompt() != Swath.COMMAND_PROMPT) {
					PrintTrace.exec("Sleeping in VitoSSM Timeout");
					Thread.sleep(25);
				}
				EnterComputer.exec();
				PortReport.exec(Swath.sector.sector());
				LeaveComputer.exec();	
				int rnd = r.nextInt(Swath.main.sectors());
				VitoMove(rnd);
				return true;
			}
			

			return false;
		}
	}
}

	
			
		
		


	