import com.swath.*;
import java.util.*;
/**
 Busts Planets
 Author MD
 */
public class FastBustPlanet extends UserDefinedCommand {
	private String name;
	private String result;
	private int[] res;
	private int id;
	public String getName() {
		// Return the name of the command
		return "Fast Bust Planet";
	}
	

	public boolean initCommand() throws Exception {
		result = ".";
		res = new int[0];
		id = -1;
		return true;
	}

	public void startCommand() throws Exception {
		String personal = "q  u  y  n\b  "+name+"\rl\r  z  d  y  p  s";
		String corp = "q  u  y  n\b  "+name+"\rpl\r  z  d  y  p  s";
		String idOnly = "q  l  "+id+"\r  n  z  n  d  y  \r  p  s ";
			if(id < 0 && Swath.you.corporation() != null){
				sendString(corp);
			}
			else if(id < 0){
				sendString(personal);
			}
			else if(id > 0){
				sendString(idOnly);
			}
		
		
	}

	public void endCommand(boolean finished) throws Exception {
		setResult(res);
	}
	
	public void onText(String buffer, String text) throws Exception {
		Tools.TextRange range;
		List ids = new ArrayList();
			if ((range = ((Tools.findText(buffer, "Registry# and Planet Name","Land on which planet <Q to abort>")))) != null) {
				result = Tools.getText(buffer,range);
				String[] planets = result.split("\n");
				for(int i = 0; i < planets.length; i++){
					planets[i] = planets[i].trim();
					if(!planets[i].equals("") && planets[i].charAt(0) == '<'){
						planets[i] = (planets[i].substring(1,planets[i].indexOf(">"))).trim();
						ids.add(planets[i]);
					}
					else{
						planets[i] = "";
					}
				}
				res = new int[ids.size()];
				for(int i = 0; i < res.length; i++){
					res[i] = new Integer((String)ids.get(i)).intValue();
				}
				
			}
		}

	public void onEvent(EventIfc event) throws Exception {
//		// Here you can receive and process incoming events
//		printTrace("onEvent('" + event.getClassName() + "')");
		
	}
	
	public static int[] exec(String name) throws Exception	{
		// This is the static method that will make it possible to use
		// this command in a user defined script just like using any of
		// the given SWATH commands.
		// Several different exec methods could be defined if needed.

		FastBustPlanet cmd = new FastBustPlanet();
		cmd.initInstance();
		//cmd.number = number;
		cmd.name = name;
		return ((int[])(cmd.execInstance()));
	}
	public static int[] exec(int id) throws Exception	{
		// This is the static method that will make it possible to use
		// this command in a user defined script just like using any of
		// the given SWATH commands.
		// Several different exec methods could be defined if needed.

		FastBustPlanet cmd = new FastBustPlanet();
		cmd.initInstance();
		cmd.id = id;
		//cmd.name = name;
		return ((int[])(cmd.execInstance()));
	}
}
