package utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import sequenceDiagramGenerator.Query;
import soot.SootMethod;

public final class Utilities {
    private Utilities() {}
    
    public static final boolean DEBUG = true;
    
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
    
    public static String ReadEntireFile(String fileName){
    	File f = new File(fileName);
		if(!f.exists() || f.isDirectory()){return "";}
		try {
			Scanner s = new Scanner(f).useDelimiter("\\Z");
			String filecontents = s.next();
			s.close();
			return filecontents;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return "";
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
	
	public static String getMethodString(SootMethod m){
		StringBuilder sb = new StringBuilder();
		sb.append(m.getDeclaringClass().getName());
		sb.append(".");
		sb.append(m.getName());
		sb.append("(");
		for(int i = 0; i < m.getParameterCount(); i++){
			sb.append(m.getParameterType(i));
			if(i < m.getParameterCount() - 1){
				sb.append(",");
			}
		}
		sb.append(")");
		return sb.toString();
	}

	public static String firstFiveLetters(String string) {
		String s = string;
		StringBuilder sb = new StringBuilder();
		int i = 0;
		while(i < s.length() && sb.length() < 5){
			if(Character.isLetter(s.charAt(i))){
				sb.append(s.charAt(i));
			}
			i++;
		}
		return sb.toString();
	}

	public static void deleteDirectory(File aFile) {
		if(aFile.isDirectory()){
			for(File f : aFile.listFiles()){
				deleteDirectory(f);
			}
		}
		if(!aFile.delete()){
			Utilities.DebugPrintln("Couldn't Delete");
		}
	}

	public static void cleanUpDir(File aSubDir) {
		File[] lf = aSubDir.listFiles();
		for(File f : lf){
			if(f.length() > 0){
				return;
			}
		}
		deleteDirectory(aSubDir);
	}
	
	private static PrintStream debug_ps;
	public static void SetDebugFile(String fileName){
		File debugFile = new File(fileName);
		try {
			debug_ps = new PrintStream(debugFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			DebugPrintln("Debug File Not Found");
		}
	}
	
	public static void DebugPrintln(String s){
		if(DEBUG){
		if(debug_ps == null){
			System.out.println(s);
		}
		else{
			debug_ps.println(s);
		}}
	}
	
	public static final boolean PERFLOG = true;
	private static PrintStream perf_ps;
	public static void SetPerfFile(String fileName){
		File aFile = new File(fileName);
		try {
			perf_ps = new PrintStream(aFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			DebugPrintln("PerfLog File Not Found");
		}
	}
	
	public static void PerfLogPrintln(String s){
		if(PERFLOG){
		if(debug_ps == null){
			return;
		}
		else{
			perf_ps.println(s);
		}}
	}
	
	public static void cleanup() {
		if(debug_ps != null){
			debug_ps.flush();
			debug_ps.close();
			debug_ps = null;
		}
		if(perf_ps != null){
			perf_ps.flush();
			perf_ps.close();
			perf_ps = null;
		}
	}

	private static final int TRUNCLEN = 15;
	public static String Truncate(String name) {
		if(name.length() > TRUNCLEN){
			return name.substring(0,TRUNCLEN);
		}
		return name;
	}
}
