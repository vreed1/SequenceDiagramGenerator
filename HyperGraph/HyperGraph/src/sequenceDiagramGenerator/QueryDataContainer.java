package sequenceDiagramGenerator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import soot.PatchingChain;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.jimple.ArrayRef;
import soot.jimple.FieldRef;
import soot.jimple.InvokeExpr;
import soot.jimple.internal.AbstractStmt;
import utilities.Utilities;



public class QueryDataContainer {

	public List<Type> theListRefTypes;
	public List<SootMethod> theListCalledMethods;
	
	public QueryDataContainer(TraceStatement tc){
		theListRefTypes = new ArrayList<Type>();
		theListCalledMethods = new ArrayList<SootMethod>();
		//bit lazy, but i prefer a list representation to
		//a iterator representation because i want to be able to look
		//at objects by index.
		TraceStatement tStmt = tc;
		while(tStmt != null){
			try
			{
				AbstractStmt aStmt = tStmt.theStmt;
				if(aStmt.containsArrayRef()){
					ArrayRef ar = aStmt.getArrayRef();
					Type t = ar.getType();
					if(!theListRefTypes.contains(t)){
						theListRefTypes.add(t);
					}
				}
				if(aStmt.containsFieldRef()){
					FieldRef fr = aStmt.getFieldRef();
					Type t= fr.getType();
					if(!theListRefTypes.contains(t)){
						theListRefTypes.add(t);
					}
				}
				if(aStmt.containsInvokeExpr()){
					InvokeExpr ie = aStmt.getInvokeExpr();
					SootMethod sm = ie.getMethod();
					if(!theListCalledMethods.contains(sm)){
						theListCalledMethods.add(sm);
					}
				}
			}
			catch(Exception ex){
				Utilities.DebugPrintln("Error in query data construction:");
				Utilities.DebugPrintln(ex.getMessage());
			}
			tStmt = tStmt.theNext;
		}
	} 
}
