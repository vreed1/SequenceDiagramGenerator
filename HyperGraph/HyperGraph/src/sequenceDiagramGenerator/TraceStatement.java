package sequenceDiagramGenerator;

import soot.jimple.internal.AbstractStmt;

public class TraceStatement {
	public enum BranchStatus{
		NotBranch, TrueChosen, FalseChosen
	}
	public AbstractStmt theStmt;
	public TraceStatement theNext;
	public BranchStatus theBranchStatus;

	public TraceStatement(AbstractStmt aStmt, TraceStatement aNext){
		theStmt = aStmt;
		theNext = aNext;
		theBranchStatus = BranchStatus.NotBranch;
	}
}
