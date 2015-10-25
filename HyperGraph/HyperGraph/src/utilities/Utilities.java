package utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import sequenceDiagramGenerator.SimpleQuery;
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
		else if(perf_ps != null){
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
	
	private static Set<Character> UnsafeSet = new HashSet<Character>(Arrays.asList(new Character[] {'\\', '/', ':', '*', '?', '"', '<', '>', '|'}));
	
	public static String MakeFileSafe(String name){
		if(name == null){return null;}
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < name.length(); i++){
			if(UnsafeSet.contains(name.charAt(i))){
				sb.append("");
			}
			else{
				sb.append(name.charAt(i));
			}
		}
		return sb.toString();
	}

	private static final int TRUNCLEN = 15;
	public static String Truncate(String name) {
		if(name.length() > TRUNCLEN){
			return name.substring(0,TRUNCLEN);
		}
		return name;
	}
	
	public static String GetArgument(String[] args, String tag){
		for(int i = 0; i < args.length-1; i++){
			if(args[i].equals(tag)){
				return args[i+1];
			}
		}
		return "";
	}
	
	public static String twoDecimal(double d){
		String s= Double.toString(d);
		if(!s.contains(".")){return s;}
		int ind = s.indexOf('.');
		if(s.length() - ind < 4){return s;}
		return s.substring(0, ind+3);
	}
	
	public static boolean isInteger(String str){
		try{
			int x = Integer.parseInt(str);
		}
		catch(NumberFormatException nfe){
			return false;
		}
		return true;
	}

	public static boolean StartsWithAny(String string, List<String> startswith) {
		for(int i = 0; i < startswith.size(); i++){
			if(string.startsWith(startswith.get(i))){
				return true;
			}
		}
		return false;
	}

	public static String ConcatePaths(String one, String two) {
		return endWithSlash(one) + two;
	}

	public static String compareDirectoriesJSONTest(String testPath,
			String baselinePath) {
		StringBuilder sb = new StringBuilder();
		File ftest = new File(testPath);
		File fbase = new File(baselinePath);
		
		if(ftest.isDirectory() && fbase.isDirectory()){
			File[] testsub = ftest.listFiles();
			File[] basesub = fbase.listFiles();
			if(testsub.length != basesub.length){
				sb.append("Compared Directories have different file counts.\nTest:");
				sb.append(testsub.length);
				sb.append("\nBase:");
				sb.append(basesub.length);
				sb.append("\n");
			}
			for(int i = 0; i < testsub.length; i++){
				boolean foundmatch = false;
				for(int j = 0; j < basesub.length; j++){
					if(testsub[i].getName() == basesub[j].getName()){
						foundmatch = true;
						sb.append(compareDirectoriesJSONTest(testsub[i].getAbsolutePath(), basesub[j].getAbsolutePath()));
						break;
					}
				}
				if(!foundmatch){
					sb.append("No match for testfile:\n");
					sb.append(testsub[i].getAbsolutePath());
					sb.append("\nfound");
				}
			}
		}
		else if(ftest.isFile() && fbase.isFile()){
			if(ftest.getName().endsWith("json") && fbase.getName().endsWith("json")){
				sb.append(CompareJSONFiles(ftest, fbase));
			}
		}
		else{
			sb.append("Dir/File match failure\n");
			sb.append("Test file:\n");
			sb.append(ftest.getAbsolutePath());
			sb.append("\nBase file:\n");
			sb.append(fbase.getAbsolutePath());
		}
		
		return sb.toString();
	}

	private static String CompareJSONFiles(File ftest, File fbase) {
		FileReader frtest = null, frbase = null;
		StringBuilder sb = new StringBuilder();
		try {
			frtest = new FileReader(ftest);
			frbase = new FileReader(fbase);
		
			
			String info = CompareOpenedFiles(frtest, frbase);
			if(info != null && info.length() > 0){
				sb.append("Diff found while comparing jsons, \nTest:\n");
				sb.append(ftest.getAbsolutePath());
				sb.append("\nBase:\n");
				sb.append(fbase.getAbsolutePath());
				sb.append("\nInfo:\n");
				sb.append(info);
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			sb.append("Error while comparing jsons, \nTest:\n");
			sb.append(ftest.getAbsolutePath());
			sb.append("\nBase:\n");
			sb.append(fbase.getAbsolutePath());
			sb.append(e.getMessage());
		}
		finally{
			if(frtest != null){
				try {
					frtest.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					sb.append("Error while comparing jsons, \nTest:\n");
					sb.append(ftest.getAbsolutePath());
					sb.append("\nBase:\n");
					sb.append(fbase.getAbsolutePath());
					sb.append(e.getMessage());
				}
				frtest = null;
			}
			if(frbase != null){
				try {
					frbase.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					sb.append("Error while comparing jsons, \nTest:\n");
					sb.append(ftest.getAbsolutePath());
					sb.append("\nBase:\n");
					sb.append(fbase.getAbsolutePath());
					sb.append(e.getMessage());
				}
				frbase = null;
			}
		}
		return sb.toString();
		
	}


	private static String CompareOpenedFiles(FileReader frtest,
			FileReader frbase) {
		BufferedReader brtest = new BufferedReader(frtest);
		BufferedReader brbase = new BufferedReader(frbase);
		StringBuilder sb = new StringBuilder();
		try {
			int i = 0;
			while(true){
				String test = brtest.readLine();
				String base = brbase.readLine();
				if(test == null && base == null){
					break;
				}
				if(test != base){
					sb.append("Mismatch on line:");
					sb.append(i);
				}
				if(test == null || base == null){
					sb.append("Error, files of different line length");
					break;
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			sb.append("Error while reading files");
			sb.append(e.getMessage());
		}
		return sb.toString();
	}

}
