package sequenceDiagramGenerator.hypergraph;

import java.util.HashMap;
import java.util.Map;

import soot.SootMethod;

public class GroupableHyperNode<T,A> extends HyperNode<T,A> {

	Map<SootMethod, GroupableHyperEdge<A>> theEdgeMap;
	
	public GroupableHyperNode(T d, int i) {
		super(d, i);
		theEdgeMap = new HashMap<SootMethod, GroupableHyperEdge<A>>();
	}

	public void AddGroupableEdge(GroupableHyperEdge<A> edge)
    {
		theEdgeMap.put(edge.theSubSource, edge);
		super.AddEdge(edge);
    }
	
	public GroupableHyperEdge<A> GetGroupableEdge(SootMethod aMethod){
		return theEdgeMap.get(aMethod);
	}
}
