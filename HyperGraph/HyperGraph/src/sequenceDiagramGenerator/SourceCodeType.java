package sequenceDiagramGenerator;

public class SourceCodeType
{
    private int uniqueId;
    public soot.Type theSootType;
    private String className;
    
    //Brian Peterson created this method
    //it is an amalgamation of Chris's and my
    //method here, ultimately I don't know if
    //className has value as a variable but
    //this seemed like the fastest way to get
    //us back to one set of changes.
    public String getClassName(){
    	if(theSootType != null || className == null){
    		return this.toString();
    	}
    	return className;
    }
    
    public String getObjectName(){return "";}

    public int getId() { return uniqueId; }

    public SourceCodeType(String name, int id)
    {
    	this.className = name;
        this.uniqueId = id;
    }
    
    //Brian added code below.  It just seemed a like less work
    private static int nextUnused = 0;
    private static int GetNextInt(){
    	return nextUnused++;
    }
    public SourceCodeType(){
    	this.uniqueId = GetNextInt();
    }
    
    @Override
    public boolean equals(Object other){
    	if(other instanceof SourceCodeType){
    		SourceCodeType anOther = (SourceCodeType)other;
    		return anOther.theSootType.equals(this.theSootType);
    	}
    	return false;
    }
    
    @Override
    public String toString(){
    	if(theSootType == null){
    		return super.toString();
    	}
    	return theSootType.getClass().getName();
    }
}
