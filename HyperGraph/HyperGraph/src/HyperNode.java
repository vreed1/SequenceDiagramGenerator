import java.util.ArrayList;
import java.util.List;


public class HyperNode<T,A> {

	public T data;
	public int id;
	
	public List<HyperEdge<A>> edges;
	
	public List<HyperEdge<A>> targetEdges;
	
	public void AddEdge(HyperEdge<A> edge){
		edges.add(edge);
	}
	
	public void AddTargetEdge(HyperEdge<A> edge){
		if(edge.targetNode != id){throw new IllegalArgumentException("Given node is not the target as advertised " + edge);}
		targetEdges.add(edge);
	}
	
	public HyperNode(T d, int i){
		id = i;
		data = d;
		
		edges = new ArrayList<HyperEdge<A>>();
		targetEdges = new ArrayList<HyperEdge<A>>();
	}
	
	public PebblerHyperNode<T,A> CreatePebblerNode(){
		return new PebblerHyperNode<T,A>(data, id);
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		
		sb.append(data.toString());
		sb.append("\t\t\t\t= { ");
		sb.append(id);
		sb.append("SuccE = {");
		
		boolean first = true;
		for(HyperEdge<A> edge : edges){
			if(!first){sb.append(", ");}else{first = false;}
			sb.append(edge.toString());
		}
		
		sb.append("} TargetE = { ");
		
		first = true;
		for(HyperEdge<A> edge : targetEdges){
			if(!first){sb.append(", ");}else{first = false;}
			sb.append(edge.toString());
		}
		sb.append(" } } ");
		
		return sb.toString();
	}
	
	
}
