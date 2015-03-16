package sequenceDiagramGenerator.sdedit;

import java.util.ArrayList;
import java.util.List;

import soot.SootClass;

public class SDObject
{
    private String name;
    private String type;
    private String label;
    private List<ObjectFlag> flags;
    
    private List<String> theCurrentNames;
    private List<String> theNameHistory;
    
    public void AttachName(String newName){
    	theCurrentNames.add(newName);
    	theNameHistory.add(newName);
    }
    public void DetachName(String oldName){
    	if(theCurrentNames.contains(oldName)){
    		theCurrentNames.remove(oldName);
    	}
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
	
	@Override
	public boolean equals(Object obj){
		if(obj instanceof SDObject){
			SDObject other = (SDObject)obj;
			if(!other.name.equals(name)){
				return false;
			}
			if(!other.type.equals(type)){
				return false;
			}
			return true;
		}
		return false;
	}
	
//	public SDObject(SootClass aClass){
//		name = GetUniqueName();
//		type = aClass.getName();
//        this.flags = new ArrayList<ObjectFlag>();
//	}
	
	public SDObject(SootClass aClass, String startName){
		theCurrentNames = new ArrayList<String>();
		theNameHistory = new ArrayList<String>();
		AttachName(startName);
		type = aClass.getName();
        this.flags = new ArrayList<ObjectFlag>();
	}
	
	private static int uniqueName = 0;
	public static String GetUniqueName(){
		uniqueName++;
		return String.valueOf(uniqueName);
	}
}
