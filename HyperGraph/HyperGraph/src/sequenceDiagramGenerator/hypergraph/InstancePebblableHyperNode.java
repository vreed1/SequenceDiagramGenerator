package sequenceDiagramGenerator.hypergraph;

import java.util.HashMap;
import java.util.Map;

import sequenceDiagramGenerator.InstanceInfo.*;

public class InstancePebblableHyperNode<T, A> extends HyperNode<T, A> {

	Map<Name,Value> thePebbles;
	
	public InstancePebblableHyperNode(T d, int i) {
		super(d, i);
		thePebbles = new HashMap<Name, Value>();
	}
	
	public void Pebble(Name aName, Value aValue){
		thePebbles.put(aName, aValue);
	}
	
	public boolean PebbleExists(Name aName){
		return thePebbles.containsKey(aName);
	}
	
	public Value GetPebbleValue(Name aName){
		return thePebbles.get(aName);
	}
	
	public void ClearPebbles(){
		thePebbles.clear();
	}

}
