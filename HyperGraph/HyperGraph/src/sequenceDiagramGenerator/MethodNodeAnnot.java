package sequenceDiagramGenerator;

import soot.SootMethod;

public class MethodNodeAnnot {
	private GroupableStmt theStmts;
	private SootMethod theMethod;
	
	public MethodNodeAnnot(SootMethod aMethod, GroupableStmt aStmt){
		theMethod = aMethod;
		theStmts = aStmt;
	}
}
