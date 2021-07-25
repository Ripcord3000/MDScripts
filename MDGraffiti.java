import com.swath.*;
import com.swath.cmd.*;
import java.util.*;
import java.io.*;

public class MDGraffiti extends UserDefinedScript {
	private Parameter ss;
	private Parameter file;
	private List files;
	
	public String getName() {
		return "MD Graffiti";
	}
	public String getDescription() {
		return "This script will send the contents of a text file to\n"+
		       "Fed Space or Sub Space.  You need to put all picture " +
		       "files in a directory under swath installation called" +
		       "Images and the script will let you pick from one. Pics" +
		       "must be the correct width for fed com and SS com and" +
		       "must not have any characters like ~ that TW doesn't " +
		       "allow." +
		       "Author - Mind Dagger                         09/07/05";
	}
	public boolean initScript() throws Exception {
		files = getFileListing(new File(Swath.main.swathPath()+File.separator+"Images"));
		file = new Parameter("Pick file to show:");
		file.setType(Parameter.CHOICE);
		for(int i = 0; i < files.size(); i++){
			file.addChoice(i,((File)files.get(i)).getName());
		}
		file.setCurrentChoice(0);
		
		ss = new Parameter("Subspace? (Otherwise Fed)");
		ss.setType(Parameter.BOOLEAN);
		ss.setBoolean(false);
		
		registerParam(file);
        registerParam(ss);
        
		return true;
	}
	public boolean endScript() throws Exception {
		
		throw(new Exception());
		
	}
	public boolean runScript() throws Exception {
		 try {
		        BufferedReader in = new BufferedReader(new FileReader(((File)files.get(file.getCurrentChoice()))));
		        String str;
		        if(ss.getBoolean()){
		        	SendString.exec("'"+SendString.RETURN_KEY+" "+SendString.RETURN_KEY);
		        }
		        else{
		        	SendString.exec("`"+SendString.RETURN_KEY+" "+SendString.RETURN_KEY);
		        }
		        while ((str = in.readLine()) != null) {
		        	if(str.length() > 60){
		        		str = str.substring(0,60);
		        	}
		            SendString.exec(" "+str+SendString.RETURN_KEY);
		        }
		        SendString.exec(" "+SendString.RETURN_KEY+SendString.RETURN_KEY);
		        in.close();
		    } catch (IOException e) {
		    }
				
			
		return true;
	}	
	static public List getFileListing( File aStartingDir ) throws FileNotFoundException{
	    validateDirectory(aStartingDir);
	    List result = new ArrayList();

	    File[] filesAndDirs = aStartingDir.listFiles();
	    List filesDirs = Arrays.asList(filesAndDirs);
	    Iterator filesIter = filesDirs.iterator();
	    File file = null;
	    while ( filesIter.hasNext() ) {
	      file = (File)filesIter.next();
	      if (!file.isFile()) {
	        //nothing
	      }
	      else{
	    	  result.add(file); 
	      }

	    }
	    Collections.sort(result);
	    return result;
	  }

	  /**
	  * Directory is valid if it exists, does not represent a file, and can be read.
	  */
	  static private void validateDirectory (File aDirectory) throws FileNotFoundException {
	    if (aDirectory == null) {
	      throw new IllegalArgumentException("Need an Image directory under the scripts directory in Swath.");
	    }
	    if (!aDirectory.exists()) {
	      throw new FileNotFoundException("Directory does not exist: " + aDirectory+". Need an Image directory under the scripts directory in Swath.");
	    }
	    if (!aDirectory.isDirectory()) {
	      throw new IllegalArgumentException("Is not a directory: " + aDirectory);
	    }
	    if (!aDirectory.canRead()) {
	      throw new IllegalArgumentException("Directory cannot be read: " + aDirectory);
	    }
	  }
}

	
			
		
		


	