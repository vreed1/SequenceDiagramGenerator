package sequenceDiagramGenerator.hypergraph;

import java.util.HashMap;
import java.util.Map;

import sequenceDiagramGenerator.GroupableStmt;

public class GroupableHyperNode<T,A> extends HyperNode<T,A> {

	Map<GroupableStmt, GroupableHyperEdge<A>> theEdgeMap;
	
	public GroupableHyperNode(T d, int i) {
		super(d, i);
		theEdgeMap = new HashMap<GroupableStmt, GroupableHyperEdge<A>>();
	}

	public void AddGroupableEdge(GroupableHyperEdge<A> edge)
    {
		theEdgeMap.put(edge.theSubSource, edge);
		super.AddEdge(edge);
    }
	
	public GroupableHyperEdge<A> GetGroupableEdge(GroupableStmt aStmt){
		return theEdgeMap.get(aStmt);
	}
}
