package sequenceDiagramGenerator.hypergraph;

import java.util.ArrayList;
import java.util.List;

import sequenceDiagramGenerator.pebbler.*;


public class HyperNode<T, A>
{
    public T data;
    public int uniqueId;

    public List<HyperEdge<A>> edges;
    public List<HyperEdge<A>> targetEdges;

    public HyperNode(T d, int i)
    {
        uniqueId = i;
        data = d;

        edges = new ArrayList<HyperEdge<A>>();
        targetEdges = new ArrayList<HyperEdge<A>>();
    }
   
    public int GetID(){
    	return uniqueId;
    }
    
    public void removeEdge(int eid){
    	for(int i = edges.size() -1; i >=0; i--){
    		if(edges.get(i).getUID() == eid){
    			edges.remove(i);
    		}
    	}
    	for(int i = targetEdges.size() -1; i >= 0; i--){
    		if(targetEdges.get(i).getUID() == eid){
    			targetEdges.remove(i);
    		}
    	}
    }
    
    public void AddEdge(HyperEdge<A> edge)
    {
        edges.add(edge);
    }

    public void AddTargetEdge(HyperEdge<A> edge)
    {
        if(edge.targetNode != uniqueId) {
        	  throw new IllegalArgumentException("Given node is not the target as advertised " + edge);
        }
        targetEdges.add(edge);
    }


    public PebblerHyperNode<T,A> CreatePebblerNode()
    {
        return new PebblerHyperNode<T,A>(data, uniqueId);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append(data.toString());
        sb.append("\t\t\t\t= { ");
        sb.append(uniqueId);
        sb.append("SuccE = {");

        boolean first = true;
        for(HyperEdge<A> edge : edges)
        {
            if(!first) sb.append(", ");
            else first = false;
            sb.append(edge.toString());
        }

        sb.append("} TargetE = { ");

        first = true;
        for(HyperEdge<A> edge : targetEdges)
        {
            if(!first) sb.append(", ");
            else first = false;

            sb.append(edge.toString());
        }
        sb.append(" } } ");

        return sb.toString();
    }
}
