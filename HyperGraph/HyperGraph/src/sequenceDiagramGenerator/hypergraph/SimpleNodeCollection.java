package sequenceDiagramGenerator.hypergraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleNodeCollection<T> {
	
	private Map<Integer, GroupableHyperNode<T>> theMap;
	private GroupableHyperNodeFactory<T> theFactory;
	
	public SimpleNodeCollection(){
		theMap = new HashMap<Integer, GroupableHyperNode<T>>();
		theFactory = new GroupableHyperNodeFactory<T>();
	}
	
	public void AddNode(GroupableHyperNode<T> aNode){
		theMap.put(aNode.data.hashCode(), aNode);
	}
	
	public GroupableHyperNode<T> GetNode(T data){
		return GetNode(data.hashCode());
	}
	
	public GroupableHyperNode<T> GetNode(int aHashCode){
		return theMap.get(aHashCode);
	}
	
	public GroupableHyperNode<T> GetNode(String methodName){
		return GetNode(methodName.hashCode());
	}
	
	public void AddNewNode(T data){
		theMap.put(data.hashCode(), (GroupableHyperNode<T>) theFactory.Generate(data));
	}
	
	public List<GroupableHyperNode<T>> getNodes(){
		return new ArrayList<GroupableHyperNode<T>>(theMap.values());
	}

	public int size(){
		return theMap.size();
	}
}
