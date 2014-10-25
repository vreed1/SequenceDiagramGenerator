package pebbler;

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
    //public PebblerColorType pebbleColor;

    // Whether the node has been pebbled or not.
    public boolean pebbled;

    public PebblerHyperEdge(List<Integer> src, int target, A annotation) {
        this.annotation = annotation;
        sourceNodes = src;
        sourcePebbles = new ArrayList<Integer>(); // If empty, we assume all false (not pebbled)
        targetNode = target;
        pebbled = false;
    }

    public boolean IsFullyPebbled() {
        for (int srcNode : sourceNodes) {
            if (!sourcePebbles.contains(srcNode)) return false;
        }

        return sourceNodes.size() == sourcePebbles.size();
    }

    //public void SetColor(PebblerColorType color)
    //{
    //    pebbleColor = color;
    //}

    // The source nodes and target must be the same for equality.
    @Override
    public boolean equals(Object obj) {
    	try {
	        PebblerHyperEdge<A> thatEdge = (PebblerHyperEdge<A>)obj;
	        if (thatEdge == null) return false;
	        for (int src : sourceNodes){
	            if (!thatEdge.sourceNodes.contains(src)) return false;
	        }
	        return targetNode == thatEdge.targetNode;
    	}catch(ClassCastException e){return false;} 
    }

    @Override
    public int hashCode() { return super.hashCode(); }

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
