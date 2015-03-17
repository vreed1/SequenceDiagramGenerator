package sequenceDiagramGenerator;

import soot.jimple.internal.AbstractStmt;
import utilities.Utilities;

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
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(theStmt.toString());
		sb.append(Utilities.NEWLINE);
		if(theNext != null){
			sb.append(theNext.toString());
		}
		return sb.toString();
	}
}
