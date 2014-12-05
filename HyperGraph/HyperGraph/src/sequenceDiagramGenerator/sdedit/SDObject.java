package sequenceDiagramGenerator.sdedit;

import java.util.ArrayList;
import java.util.List;

public class SDObject
{
    private String name;
    private String type;
    private String label;
    private List<ObjectFlag> flags;
    
    public SDObject(String name, String type) {
        this.name = name;
        this.type = type;
        this.flags = new ArrayList<ObjectFlag>();
    }
    public SDObject(String name, String type, List<ObjectFlag> flags) {
        this.name = name;
        this.type = type;
        this.flags = flags;
    }
    public SDObject(String name, String type, String label) {
        this.name = name;
        this.type = type;
        this.flags = new ArrayList<ObjectFlag>();
        this.label = label;
    }
    public SDObject(String name, String type, List<ObjectFlag> flags, String label) {
        this.name = name;
        this.type = type;
        this.flags = flags;
        this.label = label;
    }
    
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
}
