package sequenceDiagramGenerator.hypergraph;

import java.util.List;


public class HyperEdge<A>
{
    public List<Integer> sourceNodes;
    public int targetNode;
    public A annotation;
    
    public HyperEdge(List<Integer> src, int target, A annot)
    {
        sourceNodes = src;
        targetNode = target;
        annotation = annot;

        //Brian commenting this out per email around nov 16ish
        //Direct cycles seem inevitable with the seq. diagram project.
        //if(src.contains(new Integer(target))) {
        //    throw new IllegalArgumentException("There exists a direct cycle in a hyperedge" + this);
        //}
    }

    public boolean DefinesEdge(List<Integer> antecedent, int consequent)
    {
        if (targetNode != consequent) return false;
    	
    	if (this.sourceNodes.size() != antecedent.size()) return false;
    	
        for(Integer ante : antecedent)
        {
            if (!sourceNodes.contains(ante)) return false;
        }
        
        return true;
    }

    
    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object obj)
    {
    	HyperEdge<A> otherEdge = null;
        try {
            otherEdge = (HyperEdge<A>)obj;
        } catch(ClassCastException e)
        {
        	return false;
        }

        if (otherEdge == null) return false;

        //
        // Verify equality of target node then source nodes.
        //
        if (this.targetNode != otherEdge.targetNode) return false;
        
        for(Integer src : this.sourceNodes)
        {
            if(!otherEdge.sourceNodes.contains(src)) return false;
        }
        
        return true;
    }

    @Override
    public String toString()
    {
        String retS = " { ";
        for(Integer node : sourceNodes){
            retS += node + ",";
        }
        if(sourceNodes.size() != 0){retS = retS.substring(0, retS.length() - 1);}
        retS += " } -> " + targetNode;
        return retS;
    }
}
