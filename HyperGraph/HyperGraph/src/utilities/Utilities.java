package utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class Utilities {
    private Utilities() {}
    
    public static final boolean DEBUG = false;
    
    public static final String NEWLINE = System.getProperty("line.separator");
    
    private static String OS = System.getProperty("os.name").toLowerCase();

    //In GeoTutor, the pebbler accounted for restrictions on theorems and axioms to be used
    //Not sure if there would be any restrictions for sequence diagram generation, so using a placeholder for now
    public static boolean PLACEHOLDER_RESTRICTION = false;

    public static <T> boolean AddUnique(List<T> list, T obj) {
        if (list.contains(obj)) return false;

        list.add(obj);
        return true;
    }
    
    private static int NameCount = 1;
    public static String GetUniqueName(){
    	return "x" + NameCount++;
    }

    // Makes a list containing a single element
    public static <T> void AddUniqueList(List<T> list, List<T> objList) {
        for (T o : objList) {
            AddUnique(list, o);
        }
    }
    
    public static String GetClassPathDelim(){
    	if(isWindows()){
    		return ";";
    	}
    	return ":";
    }
    
    //Brian got this method of detecting os from:
    //http://stackoverflow.com/questions/14288185/detecting-windows-or-linux
    public static boolean isWindows(){
    	return (OS.indexOf("win") >= 0);
    }
    
    public static boolean isUnix(){
    	return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0|| OS.indexOf("aix") > 0 );
    }
    
    public static boolean isMac(){
    	return (OS.indexOf("mac") >= 0);
    }
    
    public static List<String> ListClassesInJar(File aJar){
    	List<String> listClassNames = new ArrayList<String>();
		try {
			ZipInputStream zStream = new ZipInputStream(new FileInputStream(aJar));
			ZipEntry ze = zStream.getNextEntry();
			while(ze != null){
				if(ze.getName().endsWith(".class") && !ze.isDirectory()){
					StringBuilder sb = new StringBuilder();
					String[] pathSplit = ze.getName().split("/");
					for(int i = 0; i < pathSplit.length; i++){
						if(sb.length() != 0){
							sb.append(".");
						}
						sb.append(pathSplit[i]);
						if(pathSplit[i].endsWith(".class")){
							sb.setLength(sb.length() - ".class".length());
						}
					}
					listClassNames.add(sb.toString());
				}
				ze = zStream.getNextEntry();
			}
			zStream.close();
			
			
			
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return listClassNames;
    }

	public static String endWithSlash(String saveDir) {
		if(isWindows()){
			if(saveDir.endsWith("\\")){
				return saveDir;
			}
			return saveDir + "\\";
		}
		if(saveDir.endsWith("/")){
			return saveDir;
		}
		return saveDir + "/";
	}
}
