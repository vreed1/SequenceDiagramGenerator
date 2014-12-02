package sequenceDiagramGenerator.sootAnalyzer;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import sequenceDiagramGenerator.SourceCodeType;
import sequenceDiagramGenerator.hypergraph.EdgeAnnotation;
import sequenceDiagramGenerator.hypergraph.Hypergraph;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.util.Chain;
import soot.Type;

public class Analyzer {
	
	//The soot class path is the class path that soot can look through
	//when you give it a class or method to analyze.  If you want to analyze
	//code outside this built project, you should set this.
	
	//Soot inherits the class path of the executable when it is started.
	//so you will already have the basic java class path defined by 
	//environment, and the class path relevant to whatever executable jar
	//or jvm that was run to execute this method.
	public static String getSootClassPath(){
		return Scene.v().getSootClassPath();
	}
	
	public static void setSootClassPath(String val){
		Scene.v().setSootClassPath(val);
	}
	
	public static Hypergraph<SourceCodeType, EdgeAnnotation> AnalyzeSpecificClasses(
			String ApplicationClassName,
			String ClassDirectory,
			String AppendToClassPath){
		List<String> listOfFiles = new ArrayList<String>();
		ListAllFilesInFolder("", ClassDirectory, listOfFiles);
		return AnalyzeSpecificClasses(
				ApplicationClassName,
				listOfFiles,
				AppendToClassPath);
	}
	
	
	private static String RemoveCountFromEnd(String s, int c){
		return s.substring(0, s.length() - c );
	}
	
	private static void ListAllFilesInFolder(
			String prereq,
			String folderName,
			List<String> ListAllFiles){
		File dir = new File(folderName);
		File[] fList = dir.listFiles();
		prereq = prereq + dir.getName() + ".";
		for(File f : fList){
			if(f.isFile()){
				if(f.getName().endsWith(".class")){
					
					ListAllFiles.add(prereq + RemoveCountFromEnd(f.getName(), 6));
				}
			}
			else if(f.isDirectory()){
				ListAllFilesInFolder(prereq, f.getAbsolutePath(), ListAllFiles);
			}
		}
	}

	public static Hypergraph<SourceCodeType, EdgeAnnotation> AnalyzeSpecificClasses(
			List<String> listClassNames,
			String AppendtoClassPath){

		if(AppendtoClassPath != null ){
			if(!AppendtoClassPath.isEmpty()){
				setSootClassPath(getSootClassPath() + ":" + AppendtoClassPath);
			}
		}

		Hypergraph<SourceCodeType, EdgeAnnotation> toReturn = new Hypergraph<SourceCodeType, EdgeAnnotation>();
		
		//the addition of this line is probably evidence of paranoia on my part.
		//but it took me a long time to figure out these magic words.
		soot.PhaseOptions.v().setPhaseOptionIfUnset("jb", "use-original-names");
		
		for(int i= 0; i < listClassNames.size(); i++){
			SootClass c = Scene.v().loadClassAndSupport(listClassNames.get(i));
			
			AddClassToHypergraph(
					toReturn,
					c);
		}
		return toReturn;
		
	}
	
	public static Hypergraph<SourceCodeType, EdgeAnnotation> AnalyzeSpecificClasses(
			String ApplicationClassName,
			List<String> otherClassNames,
			String AppendToClassPath){
		
		if(AppendToClassPath != null ){
			if(!AppendToClassPath.isEmpty()){
				setSootClassPath(getSootClassPath() + ":" + AppendToClassPath);
			}
		}
		
		Hypergraph<SourceCodeType, EdgeAnnotation> toReturn = new Hypergraph<SourceCodeType, EdgeAnnotation>();
		
		//the addition of this line is probably evidence of paranoia on my part.
		//but it took me a long time to figure out these magic words.
		soot.PhaseOptions.v().setPhaseOptionIfUnset("jb", "use-original-names");
		
		SootClass c = Scene.v().loadClassAndSupport(ApplicationClassName);

		c.setApplicationClass();
		
		Chain<SootClass> chainClasses = Scene.v().getClasses();
		
		Iterator<SootClass> iSootClasses = chainClasses.iterator();
		
		while(iSootClasses.hasNext()){
			SootClass aClass = iSootClasses.next();
			if(otherClassNames.contains(aClass.getName())){
				AddClassToHypergraph(
					toReturn,
					aClass);
			}
		}
		
		return toReturn;
	}
	
	//The idea here is to take a Class to let soot load, and then grab as much as we can.
	//Loading the class and support will load other classes that are relied upon.
	//Any class that is loaded will be scanned and put in the hypergraph.
	//other approaches are possible, including a trace of called methods
	//I (Brian) figured we could just go back later and trim out if this 
	//is pulling too much.
	//For best results, use the class with the static void main call
	//that will necessarily pull in all dependencies of the program that
	//are accessible given the defined class path at call time.
	//This doesn't use any Shimple functionality, because all we care about is types
	//and Shimple isn't invoked by soot analysis until we start pulling
	//method bodies.
	public static Hypergraph<SourceCodeType, EdgeAnnotation> AnalyzeAllReachableClasses(
			String FromClassName,
			String AppendToClassPath){

		if(AppendToClassPath != null ){
			if(!AppendToClassPath.isEmpty()){
				setSootClassPath(getSootClassPath() + ":" + AppendToClassPath);
			}
		}
		
		Hypergraph<SourceCodeType, EdgeAnnotation> toReturn = new Hypergraph<SourceCodeType, EdgeAnnotation>();
		
		//the addition of this line is probably evidence of paranoia on my part.
		//but it took me a long time to figure out these magic words.
		soot.PhaseOptions.v().setPhaseOptionIfUnset("jb", "use-original-names");
		
		SootClass c = Scene.v().loadClassAndSupport(FromClassName);

		c.setApplicationClass();
		Chain<SootClass> chainClasses = Scene.v().getClasses();
		
		Iterator<SootClass> iSootClasses = chainClasses.iterator();
		
		while(iSootClasses.hasNext()){
			SootClass aClass = iSootClasses.next();
			
			AddClassToHypergraph(
					toReturn,
					aClass);
		}
		
		return toReturn;
	}
	
	private static void AddClassToHypergraph(
			Hypergraph<SourceCodeType, EdgeAnnotation> hg,
			SootClass aClass){
		soot.Type aType = aClass.getType();
		SourceCodeType sct = new SourceCodeType();
		sct.theSootType = aType;
		hg.AddNode(sct);
		
		List<SootMethod> listMethods = aClass.getMethods();
		for(int i = 0; i < listMethods.size(); i++){
			SootMethod m = listMethods.get(i);
			
			Type methodReturn = m.getReturnType();
			SourceCodeType sctMethodRet = new SourceCodeType();
			sctMethodRet.theSootType = methodReturn;
			hg.AddNode(sctMethodRet);
			
			List<SourceCodeType> listArgumentTypes = new ArrayList<SourceCodeType>();
			
			listArgumentTypes.add(sct);
			
			int argCount = m.getParameterCount();
			for(int j = 0; j < argCount; j++){
				Type tParam = m.getParameterType(j);
				SourceCodeType sctParam = new SourceCodeType();
				sctParam.theSootType = tParam;
				listArgumentTypes.add(sctParam);
				hg.AddNode(sctParam);
			}
			
			EdgeAnnotation methodAnnot = new EdgeAnnotation(m.getName(), true);
			methodAnnot.theSootMethod = m;
			hg.AddEdge(listArgumentTypes, sctMethodRet, methodAnnot);
		}
	}
}
