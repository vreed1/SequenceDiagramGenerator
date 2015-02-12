package sequenceDiagramGenerator.hypergraph;

import java.util.List;

import sequenceDiagramGenerator.GroupableStmt;

public class GroupableHyperEdge<A> extends HyperEdge<A> {
	
	public GroupableStmt theSubSource;

	public GroupableHyperEdge(List<Integer> src, int target, A annot, GroupableStmt aSubSource) {
		super(src, target, annot);
		theSubSource = aSubSource;
	}
	
	
}
