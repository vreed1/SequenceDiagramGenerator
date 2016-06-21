package sequenceDiagramGenerator.sdedit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.Type;
import utilities.Utilities;

public class SDObject
{
	public enum TaintState{Safe, Tainted};
	
	private TaintState tState = TaintState.Safe;
	
	public int ID;
    private String name;
    private String type;
    private String label;
    private List<ObjectFlag> flags;
    private boolean isConstructed;
    public boolean isStatic;
    
    private List<String> theCurrentNames;
    private List<String> theNameHistory;
    
    private Stack<List<String>> theCallStackNames;
    
    private Map<String, Integer> theFields;

    private static int idbase = 0;
    
    private SootClass theSootClass;
    
    private boolean nameFixed = false;

	public SDObject(Type sc, 
			String startName, 
			boolean aIsConstructed,
			boolean aIsStatic,
			TaintState inTState) {
		theCurrentNames = new ArrayList<String>();
		theNameHistory = new ArrayList<String>();
		theCallStackNames = new Stack<List<String>>();
		theFields = new HashMap<String, Integer>();
		AttachName(startName);
		type = sc.toString();
        this.flags = new ArrayList<ObjectFlag>();
        isConstructed = aIsConstructed;
        isStatic = aIsStatic;
        tState = inTState;
        if(isStatic){
        	label = "<static>:"+type;
        }
        ID = idbase;
        idbase++;
        try{
        	theSootClass = Scene.v().getSootClass(type);}
        catch(java.lang.RuntimeException ex){
        	Utilities.DebugPrintln("Unfound SootClass: " + type);
        }
	}
    
	public SDObject(
			SootClass aClass, 
			String startName, 
			boolean aIsConstructed, 
			boolean aIsStatic,
			TaintState inTState){
		theCurrentNames = new ArrayList<String>();
		theNameHistory = new ArrayList<String>();
		theCallStackNames = new Stack<List<String>>();
		theFields = new HashMap<String, Integer>();
		tState = inTState;
		AttachName(startName);
		type = aClass.getName();
        this.flags = new ArrayList<ObjectFlag>();
        isConstructed = aIsConstructed;
        isStatic = aIsStatic;
        if(isStatic){
        	label = "<static>:"+type;
        }
        ID = idbase;
        idbase++;
        theSootClass = aClass;
	}
	
	public SDObject(String typeName, 
			String startName,
			boolean isCons, 
			boolean isStat,
			TaintState inTState){
		theCurrentNames = new ArrayList<String>();
		theNameHistory = new ArrayList<String>();
		theCallStackNames = new Stack<List<String>>();
		theFields = new HashMap<String, Integer>();
		tState = inTState;
		AttachName(startName);
		type = typeName;
        this.flags = new ArrayList<ObjectFlag>();
        isConstructed = isCons;
        isStatic = isStat;
        if(isStatic){
        	label = "<static>:"+type;
        }
        ID = idbase;
        idbase++;
        theSootClass = null;
	}
	
	private SDObject(
			int aID,
			String aName,
			String aType,
			String aLabel,
			List<ObjectFlag> aFlags,
			boolean aIsConstructed,
			boolean aIsStatic,
			List<String> aCurrentNames,
			List<String> aNameHistory,
			Stack<List<String>> aCallStackNames,
			Map<String, Integer> aFields,
			SootClass sc,
			TaintState inTstate){
		ID = aID;
		name = aName;
		type = aType;
		label = aLabel;
		flags = aFlags;
		tState = inTstate;
		isConstructed = aIsConstructed;
		isStatic = aIsStatic;
		theCurrentNames = aCurrentNames;
		theNameHistory = aNameHistory;
		theCallStackNames = aCallStackNames;
		theFields = aFields;
		theSootClass = sc;
	}
	
	public SDObject(JSONObject jobj){
		
		ID = Integer.parseInt((String)jobj.get("ID"));
		name = (String)jobj.get("name");
		type = (String)jobj.get("type");
		label = (String)jobj.get("label");
		
		isConstructed = Boolean.parseBoolean((String)jobj.get("isConstructed"));
		isStatic = Boolean.parseBoolean((String)jobj.get("isStatic"));
		if(jobj.containsKey("TaintState")){
			String tString = (String)jobj.get("TaintState");
			if(tString == "Tainted"){
				this.tState = TaintState.Tainted;
			}
			else{
				this.tState = TaintState.Safe;
			}
		}

        this.flags = new ArrayList<ObjectFlag>();
        
		JSONArray jarr = (JSONArray)jobj.get("theNameHistory");
		theNameHistory = new ArrayList<String>();
		
		for(int i = 0; i < jarr.size(); i++){
			theNameHistory.add((String)jarr.get(i));
		}
		
		JSONObject jobjFields = (JSONObject)jobj.get("theFields");
		Iterator<String> i = (Iterator<String>)jobjFields.keySet().iterator();
		
		theFields = new HashMap<String, Integer>();
		while(i.hasNext()){
			String s = i.next();
			theFields.put(s, Integer.parseInt((String)jobjFields.get(s)));
		}
		
		nameFixed = Boolean.parseBoolean((String)jobj.get("nameFixed"));
	    
	}
	
	public void SetTaintState(SequenceDiagram sd, TaintState input){
		this.tState = input;
		for(int i = 0; i < theFields.values().size(); i++){
			sd.GetObjectFromID(theFields.get(i)).SetTaintState(sd, input);
		}
	}
	
	public JSONObject toJSONObject(){
		JSONObject jobj = new JSONObject();
		
		jobj.put("ID", Integer.toString(ID));
		jobj.put("name", name);
		jobj.put("type", type);
		jobj.put("label", label);

		jobj.put("TaintState", tState.toString());
		//I don't serialize
	    //List<ObjectFlag> flags;
		//because it is essentially vestigial and unused.
		
		jobj.put("isConstructed", Boolean.toString(isConstructed));
		jobj.put("isStatic", Boolean.toString(isStatic));
	    
		//I don't serialize
	    //List<String> theCurrentNames;
	    //private Stack<List<String>> theCallStackNames;
	    //private SootClass theSootClass;
		//because they are only relevant during generation.
		
		JSONArray jarr = new JSONArray();
		for(int i = 0; i < theNameHistory.size(); i++){
			jarr.add(theNameHistory.get(i));
		}
		jobj.put("theNameHistory", jarr);
		
		JSONObject jobjFields = new JSONObject();
		Iterator<String> iFieldKeys = theFields.keySet().iterator();
		while(iFieldKeys.hasNext()){
			String sKey = iFieldKeys.next();
			jobjFields.put(sKey, Integer.toString(theFields.get(sKey)));
		}
	    
	    jobj.put("theFields", jobjFields);
	    
	    jobj.put("nameFixed", Boolean.toString(nameFixed));
	    return jobj;
	}
	
	public SDObject getField(String fname, SequenceDiagram sd){
		if(!theFields.containsKey(fname)){
			SDObject newObj;
			if(theSootClass != null){
				SootField sf = null;
				Iterator<SootField> isf = theSootClass.getFields().iterator();
				//this doesn't work.
				//SootField sf = theSootClass.getField(fname);
				while(isf.hasNext()){
					SootField asf = isf.next();
					if(asf.getName().equals(fname)){
						sf = asf;
						break;
					}
				}
				Type t = sf.getType();
				newObj = new SDObject(t, "", false, sf.isStatic(), tState);
			}
			else{
				newObj = new SDObject("UnknownType", "", false, false, tState);
			}
			sd.AddObject(newObj);
			theFields.put(fname, newObj.ID);
		}
		return sd.GetObjectFromID(theFields.get(fname));
	}

	public SDObject clone(){
		
		List<String> newCurrentNames = new ArrayList<String>(theCurrentNames);
		List<String> newNameHistory = new ArrayList<String>(theNameHistory);
		Stack<List<String>> newNameStack = new Stack<List<String>>();
		Stack<List<String>> backward = new Stack<List<String>>();
		
		while(!theCallStackNames.isEmpty()){
			backward.push(theCallStackNames.pop());
		}
		
		while(!backward.isEmpty()){
			List<String> l = backward.pop();
			List<String> lCopy = new ArrayList<String>(l);
			theCallStackNames.push(l);
			newNameStack.push(lCopy);
		}
		Map<String, Integer> aFields = new HashMap<String, Integer>();
		for(String fKey : theFields.keySet()){
			aFields.put(fKey, theFields.get(fKey));
		}
		
		SDObject anObj = new SDObject(ID,name,type,label,flags,
				isConstructed,isStatic,newCurrentNames, newNameHistory,
				newNameStack, aFields, theSootClass, tState);
		return anObj;
	}
	
	public void PushNames(){
		theCallStackNames.push(CloneNames());
		theCurrentNames = new ArrayList<String>();
	}
	
	public void PopNames(){
		if(theCallStackNames.size() > 0){
			theCurrentNames = theCallStackNames.pop();
		}
		else{
			theCurrentNames = new ArrayList<String>();
		}
	}
	
	private List<String> CloneNames(){
		List<String> toReturn = new ArrayList<String>();
		for(int i = 0; i < theCurrentNames.size(); i++){
			toReturn.add(theCurrentNames.get(i));
		}
		return toReturn;
	}
    
    public void AttachName(String newName){
    	theCurrentNames.add(newName);
    	theNameHistory.add(newName);
    }
    public void DetachName(String oldName){
    	if(theCurrentNames.contains(oldName)){
    		theCurrentNames.remove(oldName);
    	}
    }
    public boolean MatchesName(String testName){
    	return (theCurrentNames.contains(testName));
    }
    
    public String GetTypeName(){
    	return type;
    }
    
    @Override
    public String toString() {
        // Object in sdedit format
        StringBuilder obj = new StringBuilder();
        if(isConstructed){
        	obj.append("/");
        }
        String useType = type;
        if(isTerse){
        	useType = useType.substring(useType.lastIndexOf(".")+1);
        }
        obj.append(String.format("%s:%s", name, useType));
        for (ObjectFlag f : flags) {
            obj.append(String.format("[%s]", f.tag()));
        }
        if(tState == TaintState.Tainted){
        	if(label == null){
        		label = ":Tainted";
        	}
        	else{
        		label = label + ":Tainted";
        	}
        }
        if (label != null) 
            obj.append(String.format("\"%s\"", label));
        return obj.toString();
    }
	public String GetName() {
		if(nameFixed){
			return name;}
		else{
			if(this.theCurrentNames.size() == 0){
				this.AttachName(SDObject.GetUniqueName());
			}
			return this.theCurrentNames.get(0);
		}
	}
	
	public static int uniqueName = 0;
	public static String GetUniqueName(){
		uniqueName++;
		return "$" + String.valueOf(uniqueName);
	}
	public static int finalUnnamed = 0;
	private static String GetFinalDefaultName(){
		finalUnnamed++;
		return "Unnamed " + String.valueOf(finalUnnamed);
	}
	public void fixFinalName(List<String> listUsedStrings) {
		String bestFound = "";
		for(int i = 0; i < theNameHistory.size(); i++){
			String testName = theNameHistory.get(i);
			if(listUsedStrings.contains(testName)){
				continue;
			}
			if(testName.startsWith("$")){
				continue;
			}
			if(testName.startsWith("@")){
				continue;
			}
			if(testName.equals("this")){
				continue;
			}
			if(testName.length() > bestFound.length()){
				bestFound = testName;
			}
		}
		if(bestFound.equals("") || bestFound.startsWith("$")){
			String s = GetFinalDefaultName();
			while(listUsedStrings.contains(s)){
				s = GetFinalDefaultName();
			}
			bestFound = s;
		}
		name = bestFound;
		nameFixed =true;
	}

	public void setField(String key, SDObject value) {
		theFields.put(key, value.ID);
	}

	public boolean isEquivalent(SDObject otherObj) {
		if(this.isStatic != otherObj.isStatic){
			return false;
		}
		if(!this.GetTypeName().equals(otherObj.GetTypeName())){
			return false;
		}
		return true;
	}

	private boolean isTerse = false;
	public void SetTerse(boolean terse) {
		isTerse = terse;
	}
}
