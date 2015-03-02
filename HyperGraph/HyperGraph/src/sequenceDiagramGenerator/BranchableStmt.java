package sequenceDiagramGenerator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


import soot.jimple.internal.AbstractStmt;

//BP
//This is the first level which I pull from the soot implementation
//This class is used to organize the directed statement graph
//we get from soot into branches.  Later we use GroupableStmt
//to group those branches and create blocks like if/else

//This class is a wrapper around the AbstractStmt we get from soot.
//It either proceeds to a single next statement, or it branches
//into a next and an else.
//The seen variable is used to make sure that we terminate loops.
public class BranchableStmt {
	public AbstractStmt theStmt;
	public BranchableStmt theNext;
	public BranchableStmt theElse;
	
	public GroupableStmt theEquiv;
	
	public int seen = 0;
	
	public BranchableStmt(AbstractStmt aStmt){
		theStmt = aStmt;
	}
}
