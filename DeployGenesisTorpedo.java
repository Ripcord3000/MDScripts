import com.swath.*;
/**
 Makes a planet and returns whether it is the type that is being tried for.
 Author MD
 */
public class DeployGenesisTorpedo extends UserDefinedCommand {
	private String name;
	private PlanetClass[] c;
	private int owner;
	private boolean isDone;
	private String nameIfNotRight;
	private boolean busting;
	public String getName() {
		// Return the name of the command
		return "Deploy Genesis Torpedo";
	}

	public boolean initCommand() throws Exception {
		if(!atPrompt(Swath.COMMAND_PROMPT)){
			throw new Exception("Must be at Command Prompt.");
		}
		
		return true;
	}

	public void startCommand() throws Exception {
		sendString("uy ");
	}

	public void endCommand(boolean finished) throws Exception {
		setResult(new Boolean(isDone));
	}
	
	public void onText(String buffer, String text) throws Exception {
		Tools.TextRange range;
		if ((range = ((Tools.findText(buffer, "What do you want to name this planet? (",")")))) != null) {
			String input = Tools.getText(buffer,range);
			input = input.substring(input.indexOf("("),input.lastIndexOf(")"));
			isDone = false;
			int i = 0;
			while(!isDone && i < c.length){
				if(input.indexOf(c[i].name()) >= 0){
					sendString(name+RETURN_KEY);
					isDone = true;
				}
				i++;
			}
			if(!isDone){
				sendString(nameIfNotRight+RETURN_KEY);
			}
			if(owner == Swath.PERSONAL){
				if(Swath.you.corporation() != null){
					sendString("p");
				}
			}
			else{
				if(Swath.you.corporation() != null){
					sendString("c");
				}
			}
			setBufferText("");
			if(!isDone){
				sendString("l");
			}
			
		}
		else if ((range = ((Tools.findText(buffer, "WARNING! It is potentially hazardous to place more than","Do you wish to abort?")))) != null) {
			setBufferText("");
			sendString("n ");
		}
		else if ((range = ((Tools.findText(buffer, "<Atmospheric maneuvering system engaged>","Land on which planet <Q to abort>")))) != null) {
			String input = Tools.getText(buffer,range);
			int loc;
			if((loc = input.indexOf(nameIfNotRight)) < 0){
				throw new CommandException(this,"Invalid Planet");
			}
			else{
				input = input.substring(loc-8,loc).trim();
				input = input.substring(input.indexOf("<")+1,input.indexOf(">")).trim();
			}
			sendString(input+RETURN_KEY+" zdy ");
			isDone = false;
			
			setBufferText("");
		}
		
	}

	public void onEvent(EventIfc event) throws Exception {
//		// Here you can receive and process incoming events
//		printTrace("onEvent('" + event.getClassName() + "')");
		
	}
	public static boolean exec(String name, PlanetClass[] c, int owner, String nameIfNotRight) throws Exception	{
		// This is the static method that will make it possible to use
		// this command in a user defined script just like using any of
		// the given SWATH commands.
		// Several different exec methods could be defined if needed.

		DeployGenesisTorpedo cmd = new DeployGenesisTorpedo();
		cmd.initInstance();
		cmd.c = c;
		cmd.name = name;
		cmd.owner = owner;
		cmd.nameIfNotRight = nameIfNotRight;
		cmd.busting = false;
		return ((Boolean)(cmd.execInstance())).booleanValue();
	}
	public static boolean exec(boolean busting, String name) throws Exception	{
		// This is the static method that will make it possible to use
		// this command in a user defined script just like using any of
		// the given SWATH commands.
		// Several different exec methods could be defined if needed.

		DeployGenesisTorpedo cmd = new DeployGenesisTorpedo();
		cmd.initInstance();
		cmd.c = new PlanetClass[0];
		cmd.name = name;
		cmd.owner = Swath.PERSONAL;
		cmd.nameIfNotRight = name;
		cmd.busting = busting;
		return ((Boolean)(cmd.execInstance())).booleanValue();
	}
	public static boolean exec(String name, PlanetClass c, int owner, String nameIfNotRight) throws Exception	{
		// This is the static method that will make it possible to use
		// this command in a user defined script just like using any of
		// the given SWATH commands.
		// Several different exec methods could be defined if needed.

		DeployGenesisTorpedo cmd = new DeployGenesisTorpedo();
		cmd.initInstance();
		PlanetClass[] temp = new PlanetClass[1];
		temp[0] = c;
		cmd.c = temp;
		cmd.name = name;
		cmd.owner = owner;
		cmd.nameIfNotRight = nameIfNotRight;
		cmd.busting = false;
		return ((Boolean)(cmd.execInstance())).booleanValue();
	}
}
