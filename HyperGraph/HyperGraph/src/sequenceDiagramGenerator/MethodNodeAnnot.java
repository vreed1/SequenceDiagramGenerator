package sequenceDiagramGenerator;

import soot.SootMethod;

public class MethodNodeAnnot {
	public GroupableStmt theStmts;
	public SootMethod theMethod;
	
	public MethodNodeAnnot(SootMethod aMethod, GroupableStmt aStmt){
		theMethod = aMethod;
		theStmts = aStmt;
	}
	
	@Override 
	public boolean equals(Object obj){
		if(obj instanceof MethodNodeAnnot){
			MethodNodeAnnot other = (MethodNodeAnnot) obj;
			return other.theMethod.equals(theMethod);
		}
		return false;
	}
}
