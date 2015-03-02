package sequenceDiagramGenerator;

import java.util.ArrayList;
import java.util.List;

import soot.jimple.internal.AbstractStmt;

//BP this class is meant to let us look at statement groups
//as they will be looked at in a sequence diagram
//It will point to a next statement, but it will also
//"contain" two possible branches.  The distinction is that
//we are able to understand when a branch of execution is over
//and we've returned to code common between two branches.
//the true and false branches should always be traversed first
//if they exist, then finally the next branch, which represents
//execution after any branches or loops are completed.

//This matches with the sequence diagram system, in which
//if or else or loop relevant statements are encapsulated
//in boxes.

//EndsLoop and StartsLoop are used when creating this representation
//to allow traversal to work correctly.
public class GroupableStmt {
	public AbstractStmt theStmt;
	public GroupableStmt theNext;
	public GroupableStmt theTrueBranch;
	public GroupableStmt theFalseBranch;
	
	public boolean EndsLoop;
	public boolean StartsLoop;
	
	public GroupableStmt(boolean LoopsTo, AbstractStmt aStmt){
		theStmt = aStmt;
		EndsLoop = LoopsTo;
		StartsLoop = false;
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj instanceof GroupableStmt){
			GroupableStmt other = (GroupableStmt)obj;
			return other.theStmt.equals(theStmt);
		}
		return false;
	}

}
