package sequenceDiagramGenerator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import soot.jimple.internal.AbstractStmt;

public class StatementGroup {
	private List<StatementGroup> theList;
	private AbstractStmt theSingleStmt;
	
	public StatementGroup(){
		theList = new ArrayList<StatementGroup>();
	}
	
	public StatementGroup(AbstractStmt aSingleStmt){
		theSingleStmt = aSingleStmt;
		theList = null;
	}
	
	public void SetSingleStmt(AbstractStmt aSingleStmt){
		theSingleStmt = aSingleStmt;
	}
	public AbstractStmt GetSingleStmt(){
		return theSingleStmt;
	}
	
	public void SetStmtGroup(int index, StatementGroup subGroup){
		theList.set(index, subGroup);
	}
	
	public void AppendStmtGroup(StatementGroup aGroup){
		theList.add(aGroup);
	}
	
	public StatementGroup GetStmtGroup(int index){
		return theList.get(index);
	}
	
	public Iterator<StatementGroup> GetGroupIterator(){
		return theList.iterator();
	}
}
