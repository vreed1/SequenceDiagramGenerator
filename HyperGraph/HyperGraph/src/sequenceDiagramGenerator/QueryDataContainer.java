package sequenceDiagramGenerator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import soot.PatchingChain;
import soot.Unit;
import soot.jimple.ArrayRef;
import soot.jimple.FieldRef;
import soot.jimple.InvokeExpr;
import soot.jimple.internal.AbstractStmt;

public class QueryDataContainer {
	public QueryDataContainer(PatchingChain<Unit> pcu){
		Iterator<Unit> i = pcu.iterator();
		//bit lazy, but i prefer a list representation to
		//a iterator representation because i want to be able to look
		//at objects by index.
		while(i.hasNext()){
			Unit u = i.next();
			if(!(u instanceof soot.jimple.internal.AbstractStmt))
			{
				//I don't believe this is possible, which reduces the amount of 
				//the subtree we have to deal with in practice.
				throw new java.lang.RuntimeException("Unit is not a subclass of soot.jimple.internal.AbstractStmt");
			}
			else{
				AbstractStmt aStmt = (soot.jimple.internal.AbstractStmt)u;
				if(aStmt.containsArrayRef()){
					ArrayRef ar = aStmt.getArrayRef();
				}
				if(aStmt.containsFieldRef()){
					FieldRef fr = aStmt.getFieldRef();
				}
				if(aStmt.containsInvokeExpr()){
					InvokeExpr ie = aStmt.getInvokeExpr();
				}
			}
			
		}
	} 
}
