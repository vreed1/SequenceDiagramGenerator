package sequenceDiagramGenerator.sootAnalyzer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
	public static Hypergraph<soot.Type, EdgeAnnotation> AnalyzeAllReachableClasses(
			String FromClassName,
			String AppendToClassPath){

		if(AppendToClassPath != null ){
			if(!AppendToClassPath.isEmpty()){
				setSootClassPath(getSootClassPath() + ":" + AppendToClassPath);
			}
		}
		
		Hypergraph<soot.Type, EdgeAnnotation> toReturn = new Hypergraph<soot.Type, EdgeAnnotation>();
		
		//the addition of this line is probably evidence of paranoia on my part.
		//but it took me a long time to figure out these magic words.
		soot.PhaseOptions.v().setPhaseOptionIfUnset("jb", "use-original-names");
		
		SootClass c = Scene.v().loadClassAndSupport(FromClassName);

		c.setApplicationClass();
		Chain<SootClass> chainClasses = Scene.v().getClasses();
		
		Iterator<SootClass> iSootClasses = chainClasses.iterator();
		
		while(iSootClasses.hasNext()){
			SootClass aClass = iSootClasses.next();
			
			toReturn.AddNode(aClass.getType());
			
			List<SootMethod> listMethods = aClass.getMethods();
			for(int i = 0; i < listMethods.size(); i++){
				SootMethod m = listMethods.get(i);
				Type methodReturn = m.getReturnType();
				toReturn.AddNode(methodReturn);
				
				List<Type> listArgumentTypes = new ArrayList<Type>();
				listArgumentTypes.add(aClass.getType());
				
				int argCount = m.getParameterCount();
				for(int j = 0; j < argCount; j++){
					listArgumentTypes.add(m.getParameterType(j));
					toReturn.AddNode(m.getParameterType(j));
				}
				
				EdgeAnnotation methodAnnot = new EdgeAnnotation(m.getName(), true);
				toReturn.AddEdge(listArgumentTypes, methodReturn, methodAnnot);
			}
		}
		
		return toReturn;
	}
}
