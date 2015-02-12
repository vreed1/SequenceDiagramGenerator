package sequenceDiagramGenerator;

import java.util.ArrayList;
import java.util.List;

import soot.jimple.internal.AbstractStmt;

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

}
