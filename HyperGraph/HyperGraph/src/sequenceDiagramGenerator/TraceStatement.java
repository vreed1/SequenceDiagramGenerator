package sequenceDiagramGenerator;

import soot.jimple.internal.AbstractStmt;
import soot.jimple.internal.JGotoStmt;
import utilities.Utilities;

public class TraceStatement {
	public enum BranchStatus{
		NotBranch, TrueChosen, FalseChosen
	}
	public AbstractStmt theStmt;
	public TraceStatement theNext;
	public BranchStatus theBranchStatus;

	public TraceStatement(AbstractStmt aStmt, TraceStatement aNext){
		/*
		//This was a stupid addition that fixed nothign
		//BRIAN - kludgy fix to annoying 
		//goto stmt issue Aug 8 2016ish on taint trace
		if(aStmt instanceof JGotoStmt){
			aStmt = (AbstractStmt)((JGotoStmt)aStmt).getTarget();
		}*/
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
