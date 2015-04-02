package sequenceDiagramGenerator.sdedit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.Type;

public class SDObject
{
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

	public SDObject(Type sc, String startName, boolean aIsConstructed,
			boolean aIsStatic) {
		theCurrentNames = new ArrayList<String>();
		theNameHistory = new ArrayList<String>();
		theCallStackNames = new Stack<List<String>>();
		theFields = new HashMap<String, Integer>();
		AttachName(startName);
		type = sc.toString();
        this.flags = new ArrayList<ObjectFlag>();
        isConstructed = aIsConstructed;
        isStatic = aIsStatic;
        if(isStatic){
        	label = "<static>:"+type;
        }
        ID = idbase;
        idbase++;
        try{
        	theSootClass = Scene.v().getSootClass(type);}
        catch(java.lang.RuntimeException ex){
        	System.out.println("Unfound SootClass: " + type);
        }
	}
    
	public SDObject(SootClass aClass, String startName, boolean aIsConstructed, boolean aIsStatic){
		theCurrentNames = new ArrayList<String>();
		theNameHistory = new ArrayList<String>();
		theCallStackNames = new Stack<List<String>>();
		theFields = new HashMap<String, Integer>();
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
	
	public SDObject(String typeName, String startName, boolean isCons, boolean isStat){
		theCurrentNames = new ArrayList<String>();
		theNameHistory = new ArrayList<String>();
		theCallStackNames = new Stack<List<String>>();
		theFields = new HashMap<String, Integer>();
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
			SootClass sc){
		ID = aID;
		name = aName;
		type = aType;
		label = aLabel;
		flags = aFlags;
		isConstructed = aIsConstructed;
		isStatic = aIsStatic;
		theCurrentNames = aCurrentNames;
		theNameHistory = aNameHistory;
		theCallStackNames = aCallStackNames;
		theFields = aFields;
		theSootClass = sc;
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
				newObj = new SDObject(t, "", false, sf.isStatic());
			}
			else{
				newObj = new SDObject("UnknownType", "", false, false);
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
				newNameStack, aFields, theSootClass);
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
    
    @Override
    public String toString() {
        // Object in sdedit format
        StringBuilder obj = new StringBuilder();
        if(isConstructed){
        	obj.append("/");
        }
        obj.append(String.format("%s:%s", name, type));
        for (ObjectFlag f : flags) {
            obj.append(String.format("[%s]", f.tag()));
        }
        if (label != null) 
            obj.append(String.format("\"%s\"", label));
        return obj.toString();
    }
    private boolean nameFixed = false;
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
}
