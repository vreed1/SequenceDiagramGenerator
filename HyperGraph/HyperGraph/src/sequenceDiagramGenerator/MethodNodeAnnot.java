package sequenceDiagramGenerator;

import soot.SootMethod;

public class MethodNodeAnnot {
	private BranchableStmt theStmts;
	private SootMethod theMethod;
	
	public MethodNodeAnnot(SootMethod aMethod, BranchableStmt aStmt){
		theMethod = aMethod;
		theStmts = aStmt;
	}
}
