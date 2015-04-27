package sequenceDiagramGenerator;

import java.util.ArrayList;
import java.util.List;

import sequenceDiagramGenerator.sootAnalyzer.Analyzer;
import soot.SootMethod;
import utilities.Utilities;

//BP
//Each node in our system is annotated with a specific method
//This class encapsulates both the SootMethod object which is the 
//highest level representation in soot, and contains references
//to everything which soot knows about the relevant method
//as well as a GroupableStmt which is the pointer to the first
//statement of the method, (which will then point to the next, 
//and so on).
public class MethodNodeAnnot {
	//public GroupableStmt theStmts;
	public SootMethod theMethod;
	private List<TraceStatement> theTraces;
	public BranchableStmt theBStmt;
	
	public MethodNodeAnnot(
			SootMethod aMethod,
			BranchableStmt aStmt){
		theBStmt = aStmt;
		theMethod = aMethod;
	}
	
	public MethodNodeAnnot(
			SootMethod aMethod){
		theMethod = aMethod;
		theBStmt= null;
		theTraces = null;
	}
	
	public List<TraceStatement> getTraces(){
		if(theTraces == null){
			if(theBStmt == null){
				theTraces = new ArrayList<TraceStatement>();
			}
			else{
			theTraces = Analyzer.GenerateAllTracesFromBranches(theBStmt, new ArrayList<BranchableStmt>());}
		}
		return theTraces;
	}
	
	@Override 
	public boolean equals(Object obj){
		if(obj instanceof MethodNodeAnnot){
			MethodNodeAnnot other = (MethodNodeAnnot) obj;
			return other.theMethod.equals(theMethod);
		}
		return false;
	}
	
	@Override
	public String toString(){
		return Utilities.getMethodString(theMethod);
	}
}
