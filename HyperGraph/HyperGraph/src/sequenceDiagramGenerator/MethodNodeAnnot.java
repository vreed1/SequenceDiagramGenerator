package sequenceDiagramGenerator;

import java.util.List;

import soot.SootMethod;

//BP
//Each node in our system is annotated with a specific method
//This class encapsulates both the SootMethod object which is the 
//highest level representation in soot, and contains references
//to everything which soot knows about the relevant method
//as well as a GroupableStmt which is the pointer to the first
//statement of the method, (which will then point to the next, 
//and so on).
public class MethodNodeAnnot {
	public GroupableStmt theStmts;
	public SootMethod theMethod;
	public List<TraceStatement> theTraces;
	
	public MethodNodeAnnot(
			SootMethod aMethod, 
			GroupableStmt aStmt,
			List<TraceStatement> aTraces){
		theMethod = aMethod;
		theStmts = aStmt;
		theTraces = aTraces;
	}
	
	@Override 
	public boolean equals(Object obj){
		if(obj instanceof MethodNodeAnnot){
			MethodNodeAnnot other = (MethodNodeAnnot) obj;
			return other.theMethod.equals(theMethod);
		}
		return false;
	}
	
	@Override
	public String toString(){
		return theMethod.getDeclaringClass().getName() + "." + theMethod.getName();
	}
}
