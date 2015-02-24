package sequenceDiagramGenerator.hypergraph;

import java.util.List;
import java.util.AbstractMap.SimpleEntry;

import sequenceDiagramGenerator.GroupableStmt;

public class GroupableHypergraph<T,A> extends Hypergraph<T,A> {
	
    public GroupableHypergraph() {
		super(new GroupableHyperNodeFactory<T,A>());
	}
    
    public GroupableHypergraph(int capacity){
		super(capacity, new GroupableHyperNodeFactory<T,A>());
    }

	public boolean AddGroupableEdge(List<T> antecedent, T consequent, A annotation, GroupableStmt aSubSource)
    {
        //
        // Add a local representation of this edge to each node in which it is applicable
        //
    	
    	//Brian commenting this out as there are going to be relevant edges with 
    	//same antecedents and consequents under this system.
        //if (HasEdge(antecedent, consequent)) return false;

        SimpleEntry<List<Integer>, Integer> local = ConvertToLocal(antecedent, consequent);

        
        GroupableHyperEdge<A> edge = new GroupableHyperEdge<A>(local.getKey(), local.getValue(), annotation, aSubSource);

        for (int src : local.getKey()) {
            ((GroupableHyperNode<T,A>)vertices.get(src)).AddGroupableEdge(edge);
        }

        // Add this as a target edge to the target node.
        vertices.get(local.getValue()).AddTargetEdge(edge);
        edgeCount++;

        return true;
    }
}
