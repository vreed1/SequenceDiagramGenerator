package sequenceDiagramGenerator.InstanceInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class InstanceObjectDictionary {
	
	public Map<String, InstanceObject> theStaticObjects;
	
	private Stack<Map<String, InstanceObject>> theInstanceObjects;
	
	public InstanceObjectDictionary(){
		theStaticObjects = new HashMap<String, InstanceObject>();
	}
	
	public void PushFunction(String aClassName, String aFunctionName){
		
	}
}
