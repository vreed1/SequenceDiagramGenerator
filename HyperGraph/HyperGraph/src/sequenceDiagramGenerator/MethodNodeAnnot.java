package sequenceDiagramGenerator;

import soot.SootMethod;

public class MethodNodeAnnot {
	public GroupableStmt theStmts;
	public SootMethod theMethod;
	
	public MethodNodeAnnot(SootMethod aMethod, GroupableStmt aStmt){
		theMethod = aMethod;
		theStmts = aStmt;
	}
}
