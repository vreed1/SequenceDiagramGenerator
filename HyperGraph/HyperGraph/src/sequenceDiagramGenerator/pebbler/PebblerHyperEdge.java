package sequenceDiagramGenerator.pebbler;

import java.util.ArrayList;
import java.util.List;

public class PebblerHyperEdge<A>
{
    public List<Integer> sourceNodes;
    public int targetNode;

    // The original edge annotation purely for reference.
    public A annotation;

    // Contains all source nodes that have been pebbled: for each source node,
    // there is a 'standard edge' that must be pebbled
    public List<Integer> sourcePebbles;

    // Whether the edge source nodes have been completely pebbled or not.
    public boolean pebbled;

    public PebblerHyperEdge(List<Integer> src, int target, A annotation)
    {
        this.annotation = annotation;
        sourceNodes = src;
        sourcePebbles = new ArrayList<Integer>(); // If empty, we assume all false (not pebbled)
        targetNode = target;
        pebbled = false;
    }

    public boolean IsFullyPebbled()
    {
    	// Have we already checked that this node is pebbled; if so, complete check is not needed.
        if (pebbled) return true;
        
        if (sourceNodes.size() != sourcePebbles.size()) return false;
        
    	for (int srcNode : sourceNodes)
    	{
            if (!sourcePebbles.contains(srcNode)) return false;
        }

    	pebbled = true;
    	
        return true;
    }

    // The source nodes and target must be the same for equality.
    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object obj)
    {
    	PebblerHyperEdge<A> thatEdge = null;
        try {
            thatEdge = (PebblerHyperEdge<A>)obj;
        } catch(ClassCastException e)
        {
        	return false;
        }
        
        if (thatEdge == null) return false;

        if (targetNode != thatEdge.targetNode) return false;
        
        for (int src : sourceNodes)
        {
            if (!thatEdge.sourceNodes.contains(src)) return false;
        }
        
        return true; 
    }

    @Override
    public String toString()
    {
        String retS = " { ";
        for (int node : sourceNodes)
        {
            retS += node + ", ";
        }
        if (sourceNodes.size() != 0) retS = retS.substring(0, retS.length() - 2);
        retS += " } -> " + targetNode;
        return retS;
    }
}
