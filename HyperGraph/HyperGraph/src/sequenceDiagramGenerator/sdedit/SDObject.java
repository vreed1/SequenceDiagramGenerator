package sequenceDiagramGenerator.sdedit;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import soot.SootClass;

public class SDObject
{
	public int ID;
    private String name;
    private String type;
    private String label;
    private List<ObjectFlag> flags;
    private boolean isConstructed;
    private boolean isStatic;
    
    private List<String> theCurrentNames;
    private List<String> theNameHistory;
    
    private Stack<List<String>> theCallStackNames;

    private static int idbase = 0;
	public SDObject(SootClass aClass, String startName, boolean aIsConstructed, boolean aIsStatic){
		theCurrentNames = new ArrayList<String>();
		theNameHistory = new ArrayList<String>();
		theCallStackNames = new Stack<List<String>>();
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
			Stack<List<String>> aCallStackNames){
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
		
		SDObject anObj = new SDObject(ID,name,type,label,flags,
				isConstructed,isStatic,newCurrentNames, newNameHistory,
				newNameStack);
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
	public String GetName() {
		return name;
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
		String bestFound = GetUniqueName();
		for(int i = 0; i < theNameHistory.size(); i++){
			String testName = theNameHistory.get(i);
			if(listUsedStrings.contains(testName)){
				continue;
			}
			if(testName.startsWith("$")){
				continue;
			}
			if(testName.length() > bestFound.length()){
				bestFound = testName;
			}
		}
		if(bestFound.startsWith("$")){
			String s = GetFinalDefaultName();
			while(listUsedStrings.contains(s)){
				s = GetFinalDefaultName();
			}
			bestFound = s;
		}
		name = bestFound;
	}
}
