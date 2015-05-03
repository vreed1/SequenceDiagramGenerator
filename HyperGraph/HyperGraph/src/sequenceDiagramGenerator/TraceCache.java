package sequenceDiagramGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TraceCache {
	private Map<String, List<TraceStatement>> theMap;
	private static final int MAXSIZE = 30;
	private List<String> oldAtTop;
	
	public TraceCache(){
		theMap = new HashMap<String, List<TraceStatement>>();
		oldAtTop = new ArrayList<String>();
	}
	
	public List<TraceStatement> Get(String key){
		if(theMap.containsKey(key)){
			oldAtTop.remove(key);
			oldAtTop.add(key);
			return theMap.get(key);
		}
		return null;
	}
	
	public void Set(String key, List<TraceStatement> list){
		if(theMap.size() >= MAXSIZE && !theMap.containsKey(key)){
			String badKey = oldAtTop.remove(0);
			theMap.remove(badKey);
		}
		theMap.put(key, list);
		oldAtTop.remove(key);
		oldAtTop.add(key);
	}
}
