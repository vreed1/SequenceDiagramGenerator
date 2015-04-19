package sequenceDiagramGenerator.hypergraph;

import java.util.List;

import soot.SootMethod;

public class GroupableHyperEdge<A> extends HyperEdge<A> {
	
	public SootMethod theSubSource;

	public GroupableHyperEdge(
			List<Integer> src, 
			int target, 
			A annot, 
			SootMethod aSubSource) {
		super(src, target, annot);
		theSubSource = aSubSource;
	}
	
	
}
