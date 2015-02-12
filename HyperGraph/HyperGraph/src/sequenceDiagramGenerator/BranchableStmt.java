package sequenceDiagramGenerator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


import soot.jimple.internal.AbstractStmt;

public class BranchableStmt {
	public AbstractStmt theStmt;
	public BranchableStmt theNext;
	public BranchableStmt theElse;
	
	public BranchableStmt(AbstractStmt aStmt){
		theStmt = aStmt;
	}
}
