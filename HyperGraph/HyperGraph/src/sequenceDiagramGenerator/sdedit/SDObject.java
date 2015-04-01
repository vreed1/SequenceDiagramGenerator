package sequenceDiagramGenerator.sdedit;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import soot.SootClass;

public class SDObject
{
    private String name;
    private String type;
    private String label;
    private List<ObjectFlag> flags;
    private boolean isConstructed;
    private boolean isStatic;
    
    private List<String> theCurrentNames;
    private List<String> theNameHistory;
    
    private Stack<List<String>> theCallStackNames;

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
    
//    public SDObject(String name, String type) {
//        this.name = name;
//        this.type = type;
//        this.flags = new ArrayList<ObjectFlag>();
//    }
//    public SDObject(String name, String type, List<ObjectFlag> flags) {
//        this.name = name;
//        this.type = type;
//        this.flags = flags;
//    }
//    public SDObject(String name, String type, String label) {
//        this.name = name;
//        this.type = type;
//        this.flags = new ArrayList<ObjectFlag>();
//        this.label = label;
//    }
//    public SDObject(String name, String type, List<ObjectFlag> flags, String label) {
//        this.name = name;
//        this.type = type;
//        this.flags = flags;
//        this.label = label;
//    }
    
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
	
//	@Override
//	public boolean equals(Object obj){
//		if(obj instanceof SDObject){
//			SDObject other = (SDObject)obj;
//			if(!other.name.equals(name)){
//				return false;
//			}
//			if(!other.type.equals(type)){
//				return false;
//			}
//			return true;
//		}
//		return false;
//	}
	
//	public SDObject(SootClass aClass){
//		name = GetUniqueName();
//		type = aClass.getName();
//        this.flags = new ArrayList<ObjectFlag>();
//	}
	
	
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
