package NewAnalyzerCode;
import java.util.Iterator;
import java.util.List;

import PastedSootExampleCode.SimpleVeryBusyExpressions;
import PastedSootExampleCode.VeryBusyExpressions;
import soot.Body;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;


public class ExecAnalysis {
	
	public static String getSootClassPath(){
		return Scene.v().getSootClassPath();
	}
	
	public static void setSootClassPath(String val){
		Scene.v().setSootClassPath(val);
	}
	
	public static UnitGraph getGraph(){
		SootClass c = Scene.v().loadClassAndSupport("ToBeAnalyzed.ToBeAnalyzed");
		c.setApplicationClass();
		
		SootMethod m = c.getMethodByName("NotMain");
		
		Body b = m.retrieveActiveBody();
		
		UnitGraph g = new ExceptionalUnitGraph(b);
		
		return g;
		
	}
	
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
		
		return g;
	}
}
