import java.util.ArrayList;
import java.util.List;


public class Hypergraph<T,A> {
	
	private int edgeCount = 0;
	private List<HyperNode<T,A>> vertices;
	
	public Hypergraph(){
		vertices = new ArrayList<HyperNode<T,A>>();
		edgeCount = 0;
	}
	public Hypergraph(int capacity){
		vertices = new ArrayList<HyperNode<T,A>>(capacity);
		edgeCount = 0;
	}
	
	public List<HyperNode<T,A>> get_vertices(){
		return vertices;
	}
	
	public int Size(){return vertices.size();}
	public int EdgeCount(){return edgeCount;}
	
	
}
