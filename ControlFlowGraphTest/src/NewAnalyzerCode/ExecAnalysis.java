package NewAnalyzerCode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import PastedSootExampleCode.SimpleVeryBusyExpressions;
import PastedSootExampleCode.VeryBusyExpressions;
import soot.Body;
import soot.PatchingChain;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.UnitBox;
import soot.jimple.InvokeExpr;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.shimple.Shimple;
import soot.shimple.ShimpleBody;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;

//This class contains static methods to execute different analyses.
//(Google says "analyses" is a word, but Eclipse spell check disagrees).
//The idea is to have a place where I can write a new method to test 
//a new way of doing things without disturbing a class structure.

//This is a new trivial change.

public class ExecAnalysis {
	
	//The soot class path is the class path that soot can look through
	//when you give it a class or method to analyze.  If you want to analyze
	//code outside your built project, you should set this.
	//these two methods are called by the UI.
	//Soot inherits the class path of the executable when it is started.
	//so you will already have the basic java class path defined by 
	//environment, and the class path relevant to whatever executable jar
	//or jvm that was run.
	public static String getSootClassPath(){
		return Scene.v().getSootClassPath();
	}
	
	public static void setSootClassPath(String val){
		Scene.v().setSootClassPath(val);
	}
	
	//A unit graph appears to be the soot class that best approximates a
	//control flow graph.  A unit graph is not specifically shimple.
	//this method is dead code but is left here as demonstration of 
	//how to get the most basic control flow graph on offer.
	public static UnitGraph getGraph(){
		SootClass c = Scene.v().loadClassAndSupport("ToBeAnalyzed.ToBeAnalyzed");
		c.setApplicationClass();
		
		SootMethod m = c.getMethodByName("NotMain");
		
		Body b = m.retrieveActiveBody();
		
		UnitGraph g = new ExceptionalUnitGraph(b);
		
		return g;
		
	}
	
	//this method is called by the UI currently to get a shimple
	//analysis of a method in a class, defined by the arguments.
	//if this class is not present in the classpath as it is defined above
	//you need to redefine the class path before calling this method
	//Notice that up until Body b = ... this is essentially the same
	//as the above method, except parameterized.
	
	//We can get the additional Shimple analysis from the Body object.
	//At that point we can pull out what seems to be the basic Shimple
	//control flow data structure, the PatchingChain.
	
	//The work done below PatchingChain<Unit> ... is just throwaway work
	//to prove that the functionality works at the moment.
	//that will change as we decide what to do with the shimple analysis.
	
	//Some of this work has been moved to the GetFromBody method
	//to allow us to follow analysis into sub classes.
	//This obviously might produce a problem if we couldn't enter that code.
	//but I think I've covered that with a try/catch.
	
	//note the very important line starting with
	//soot.PhaseOptions ...
	//This line forces the use of normal names in all analysis!  I don't know
	//a simpler way to do this.
	public static String ShimpleAnalyzer(String ClassName, String MethodName){
		
		StringBuilder toReturn = new StringBuilder();
		
		soot.PhaseOptions.v().setPhaseOptionIfUnset("jb", "use-original-names");
		SootClass c = Scene.v().loadClassAndSupport(ClassName);
		
	    //Map mp = soot.PhaseOptions.v().getPhaseOptions("jb");
				
		c.setApplicationClass();
		
		SootMethod m = c.getMethodByName(MethodName);
		
		Body b = m.retrieveActiveBody();
		
		toReturn.append(GetFromBody(b, ""));
		
		return toReturn.toString();
	}
	//removing this from above method allows us to call this part recursively
	//on sub methods found.
	//we can also note when we hit an invoke statement.
	private static String GetFromBody(Body b, String tabs){

			StringBuilder toReturn = new StringBuilder();
			ShimpleBody sb = Shimple.v().newBody(b);
			
			PatchingChain<Unit> pu = sb.getUnits();
			
			Unit aUnit = pu.getFirst();
			
			while(aUnit != null){
				
				if(!(aUnit instanceof soot.jimple.internal.AbstractStmt))
				{
					//I don't believe this is possible, which reduces the amount of 
					//the subtree we have to deal with in practice.
					toReturn.append(tabs + "Is not a abstractstmt");
					toReturn.append("\n");
				}
				else{
					soot.jimple.internal.AbstractStmt aStmt = (soot.jimple.internal.AbstractStmt)aUnit;
					if(aStmt.containsInvokeExpr())
					{
						toReturn.append(tabs + "A invoke present:");
						toReturn.append("\n");
						InvokeExpr ie = aStmt.getInvokeExpr();
						SootMethod sm = ie.getMethod();
						
						if(ie instanceof soot.jimple.internal.AbstractInvokeExpr){
							soot.jimple.internal.AbstractInvokeExpr jie = (soot.jimple.internal.AbstractInvokeExpr)ie;
							if(jie.getArgCount() > 0){
								jie.getArgBox(0);
								
							}
						}
						
						try{
							Body bsub = sm.retrieveActiveBody();
							toReturn.append(tabs + GetFromBody(bsub, tabs + "   "));
							toReturn.append("\n");
						}catch(RuntimeException ex){
							toReturn.append(tabs + "No Method Body Found Err:");
							toReturn.append(tabs + ex.getMessage());
							toReturn.append("\n");
						}
					}
					if(aStmt.branches()){
						toReturn.append(tabs + "Branching Stmt Below\n");
						String s = aStmt.toString();
					}
				}
				toReturn.append(tabs + aUnit.toString());
				toReturn.append("\n");
				aUnit = pu.getSuccOf(aUnit);
		}
		
		return toReturn.toString();
	}
	
	
	//This method is dead code to understand the more in depth Soot
	//(but NOT shimple) analysis, and what we would expect to get from it.
	public static UnitGraph Analyze(String ClassName, String MethodName){
		
		SootClass c = Scene.v().loadClassAndSupport(ClassName);
		c.setApplicationClass();
		
		SootMethod m = c.getMethodByName(MethodName);
		

		
		Body b = m.retrieveActiveBody();
		
		UnitGraph g = new ExceptionalUnitGraph(b);
		
		VeryBusyExpressions an = new SimpleVeryBusyExpressions(g);
		
		Iterator<Unit> i = g.iterator();
		while(i.hasNext()){
			Unit u = i.next();
			List IN = an.getBusyExpressionsBefore(u);
			List OUT = an.getBusyExpressionsAfter(u);
		}
		
		//CallGraph cg = Scene.v().getCallGraph();
		
		return g;
	}
}
