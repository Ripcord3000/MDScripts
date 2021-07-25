import javax.swing.BoxLayout;
import java.text.NumberFormat;
import java.awt.Font;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.swath.*;
import com.swath.cmd.*;
  
public class MDShipAnalyzer extends UserDefinedScript {
	final JLabel label1 = new JLabel("|            Ship Type             |Max Figs|Shields|Holds|  Price  |Xport|Attack |Defense|Photons?|");
	final JLabel label2 = new JLabel("|--------------------------------------------------------------------------------------------------|");
	
public String getName() { return "MD Ship Analyzer"; }

public boolean initScript() throws Exception {
	label1.setFont(new Font("Monospaced",Font.PLAIN,12));
	label2.setFont(new Font("Monospaced",Font.PLAIN,12));
	
	return true;
} 
	// END INITSCRIPT
public boolean runScript() throws Exception {
	
	String shiptype = "|                                  |";
	String figs = "?       |";
	String shields = "?      |";
	String holds = "?    |";
	String price = "?        |";
	String transport = "?    |";
	String attack = "???:1.0|";
	String defense = "???:1.0|";
	String photons = "??      |";
	
//	final JLabel label4 = new JLabel("|Resolve                 |400000  |16000  |255  |999999999|  80 |8.0:1.0|8.0:1.0|YES|  NO  |");
//	final JLabel label5 = new JLabel("|Resolve                 |400000  |16000  |255  |999999999|  80 |8.0:1.0|8.0:1.0|YES|  NO  |");
//	final JLabel label6 = new JLabel("|Resolve                 |400000  |16000  |255  |999999999|  80 |8.0:1.0|8.0:1.0|YES|  NO  |");
//	final JLabel label7 = new JLabel("|Resolve                 |400000  |16000  |255  |999999999|  80 |8.0:1.0|8.0:1.0|YES|  NO  |");
//	final JLabel label8 = new JLabel("|Resolve                 |400000  |16000  |255  |999999999|  80 |8.0:1.0|8.0:1.0|YES|  NO  |");
//	final JLabel label9 = new JLabel("|Resolve                 |400000  |16000  |255  |999999999|  80 |8.0:1.0|8.0:1.0|YES|  NO  |");
//	final JLabel label10 = new JLabel("|Resolve                 |400000  |16000  |255  |999999999|  80 |8.0:1.0|8.0:1.0|YES|  NO  |");
	EnterComputer.exec();
	ReadCatalog.exec();
	LeaveComputer.exec();
	ShipCategory[] ships = Swath.getAllShipCategories();
	JPanel panel = Tools.createJPanel();
	JFrame frame = Tools.createJFrame("MD Ship Analyzer", 1, 1);
 	panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));
	panel.add(label1);
	panel.add(label2);
	for(int i = 0; i < ships.length; i++){
		ShipCategory temp = ships[i];
		if(temp != null){
			shiptype = "|"+temp.type()+shiptype.substring(temp.type().length()+1);
			if(temp.maxFighters() > 0){
				figs = temp.maxFighters()+figs.substring((""+temp.maxFighters()).length());
			}
			if(temp.maxShields() > 0){
				shields = temp.maxShields()+shields.substring((""+temp.maxShields()).length());
			}
			if(temp.maxHolds() > 0){
				holds = temp.maxHolds()+holds.substring((""+temp.maxHolds()).length());
			}
			transport = temp.transportRange()+transport.substring((""+temp.transportRange()).length());
			if(temp.offensiveOdds() > 0){
				attack = temp.offensiveOdds()+attack.substring((""+temp.offensiveOdds()).length());
			}
			if(temp.defensiveOdds() > 0){
				defense = temp.defensiveOdds()+defense.substring((""+temp.defensiveOdds()).length());
			}
			if(temp.photonMissiles()){
				photons = "  YES   |";
			}
			else{
				photons = "   NO   |";
			}
			
		}
		final JLabel label = new JLabel(shiptype+figs+shields+holds+price+transport+attack+defense+photons);
		label.setFont(new Font("Monospaced",Font.PLAIN,12));
		panel.add(label);
		shiptype = "|                                  |";
		figs = "?       |";
		shields = "?      |";
		holds = "?    |";
		price = "?        |";
		transport = "?    |";
		attack = "???:1.0|";
		defense = "???:1.0|";
		photons = "??      |";
	}
	
	
	
	
	
	
	
	
	frame.getContentPane().add(panel);
	frame.pack();
	frame.setResizable(true);
	frame.setVisible(true);

	//label6.setText(NumberFormat.getCurrencyInstance().format(3443434));
	return true;
}
	

}
 /* end class */
